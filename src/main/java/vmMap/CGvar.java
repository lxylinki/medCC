package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;

/**
 * 
 * @author Linki
 *
 */
public class CGvar {
	
	public static VMtype selectByTimedec(Module mod, double budgetleft, List<VMtype> vmtypes) {
		VMtype newtype = null;
		double timedecmax = Double.MIN_VALUE;
		
		for (VMtype type: vmtypes) {
			// skip if no time dec
			if (mod.getTimeOn(type) >= mod.getTime()) {
				continue;
			}
			// skip if cost inc exceeds budget left
			double costinc = mod.getCostOn(type) - mod.getCost();
			if (costinc > budgetleft) {
				continue;
			}
			// select max time dec
			double timedec = mod.getTime() - mod.getTimeOn(type);
			if (timedec > timedecmax) {
				//System.out.printf("time dec: %.2f\n", timedec);
				timedecmax = timedec;
				newtype = type;
			}						
		}
		// if null remain current sched
		return newtype;
	}
		
	public static double cgvar(Workflow workflow, double initbudgetleft, double ed, List<Module> currentCP, List<VMtype> vmtypes ) {
		
		double budgetleft = initbudgetleft;
		
		while (budgetleft > 0) {
			
			// update current CP
			ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, currentCP);
			
			System.out.printf("Ed %.2f\n", ed);
			
			double budgetForThisRound = 0;
			double spendRatio  = 1-budgetleft/initbudgetleft;
			
			// initially do not have a reference
			if (spendRatio == 0) {
				budgetForThisRound = budgetleft/2;
			} else {
				// balanced by spendratio
				budgetForThisRound = budgetleft*spendRatio;
			}
			 
			System.out.printf("budgetleft: %.2f, budgetforthisround: %.2f\n", budgetleft, budgetForThisRound);
			
			// number of new reschedules
			double numOfResched = 0;
			double maxtimedecOnCP = 0;
			
			// resched one mod per new CP
			int targetModId = -1;
			int targetVmtypeId = -1;

			// modules on CP
			for(Module mod: currentCP) {

				// skip entry/exit mod
				if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
					continue;
				}

				//System.out.printf("checking critical mod%d\n", mod.getModId());
				VMtype newtype = selectByTimedec(mod, budgetForThisRound, vmtypes);
				if (newtype != null) {
					//System.out.printf("If reschedule mod%d to vmtype%d\n", mod.getModId(), newtype.getTypeid());
					// new reschedule found
					numOfResched++;
					double timedec = mod.getTime()-mod.getTimeOn(newtype);
					if (timedec > maxtimedecOnCP) {
						maxtimedecOnCP = timedec;
						targetModId = mod.getModId();
						targetVmtypeId =  newtype.getTypeid();
					}
				} 
			}
			
			// no more resched found this round
			if (numOfResched == 0) {
				break;				
			}
			
			Module targetMod = workflow.getModule(targetModId);
			VMtype targetVmtype = vmtypes.get(targetVmtypeId);
			
			// consume budget left
			double costinc = targetMod.getCostOn(targetVmtype)-targetMod.getCost();
			budgetleft -= costinc;
			//System.out.printf("budget left: %.2f\n", budgetleft);
			
			// resched
			targetMod.setVmtype(targetVmtype);	
			targetMod.setRescheduled(targetMod.getRescheduled()+1);
			
			//System.out.printf("Reschedule mod%d to vmtype%d\n", targetMod.getModId(), targetVmtype.getTypeid());

		}// end while
		
		return ed;
		
	}
	
	
	// main algorithm
	public static double criticalgreedy(Workflow workflow, List<VMtype> vmtypes, double budget) {
		
		// number of modules = N
		int N = workflow.getOrder();

		// max cost: just calculation, no actual mapping
		double maxcost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
			double localmax = Double.MIN_VALUE;
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) > localmax) {
					localmax = mod.getCostOn(vt);
				}
			}
			maxcost += localmax;
		}
		
		// skip entry/exit mod
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			//mod.printProfiles();
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
					// init with min cost
					mod.setVmtype(vt);
				}
			}
			//System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
			mincost += mod.getCost();
		}
		
		// check budget input
		if (budget < mincost) {
			System.out.printf("Error: budget %.2f below min cost %.2f.\n", budget, mincost);
			System.exit(1);
		}
		
		// directly apply best sched if budget is redundant
		if (budget >= maxcost) {
			int V = vmtypes.size();
			VMtype vprime = vmtypes.get(V-1);
			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				mod.setVmtype(vprime);
			}
			return workflow.getEd();
		}
		
		// System.out.printf("budget %.2f, min cost %.2f, max cost %.2f.\n", budget, mincost, maxcost);
		// keep track of current cost
		double ed = workflow.getEd();
		double budgetleft = budget - mincost;
		List<Module> currentCP = new ArrayList<Module>();
	
		//System.out.printf("init ED: %.2f, init cost %.2f\n", ed, currentCost);

		//workflow.printStructInfo();
		//workflow.printTimeInfo();
		
		ed = cgvar(workflow, budgetleft, ed, currentCP, vmtypes);
	
		//workflow.printSched();
		//System.out.printf("ED=%.2f, budget left: %.2f, CP recomputed %d times.\n", ed, budgetleft, CPchange);
		return ed;		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// numerical example
		
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(14);
		
		Workflow workflow = new Workflow(false);
		Workflowreaderlite.readliteworkflow(workflow, 50, 500, 3, false);
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}		
		double ed = criticalgreedy(workflow, vmtypes, 20.62);
		workflow.printSched();
		System.out.printf("ED=%.2f\n", ed);
	}
}
