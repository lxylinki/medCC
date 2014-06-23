package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;

public class CriticalGreedy {
	
	public static VMtype selectNewType(Module mod, double budgetleft, List<VMtype> vmtypes) {
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
		
	// main algorithm
	public static void criticalgreedy(Workflow workflow, List<VMtype> vmtypes, double budget) {
		
		// number of modules = N
		int N = workflow.getOrder();
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for(Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}
		
		// init with min cost
		// skip entry/exit mod
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			//mod.printProfiles();
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
					mod.setVmtype(vt);
				}
			}
			//System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
			mincost += mod.getCost();
		}
		
		// max cost: just calculation, no actual mapping
		double maxcost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmax = Double.MIN_VALUE;
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) > localmax) {
					localmax = mod.getCostOn(vt);
				}
			}
			maxcost += localmax;
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
			CriticalPath.topologicalSort(workflow);
			double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);	
			System.out.printf("ED=%.2f, budget left: %.2f\n", ed, budget-maxcost);
			workflow.printSched();
			System.exit(0);
		}
		
		System.out.printf("budget %.2f, min cost %.2f, max cost %.2f.\n", budget, mincost, maxcost);
				
		// keep track of current cost
		double currentCost = mincost;
		double budgetleft = budget - currentCost;
		List<Module> currentCP = new ArrayList<Module>();
		
		CriticalPath.topologicalSort(workflow);		
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, currentCP);
		
		System.out.printf("init ED: %.2f, init cost %.2f\n", ed, currentCost);
		
		//workflow.printStructInfo();
		//workflow.printTimeInfo();
		
		while (budgetleft >= 0) {
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
				VMtype newtype = selectNewType(mod, budgetleft, vmtypes);
				
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
			// no more resched found
			if (numOfResched == 0) {
				break;
			}
			
			Module targetMod = workflow.getModule(targetModId);
			VMtype targetVmtype = vmtypes.get(targetVmtypeId);
			
			// consume budget left
			double costinc = targetMod.getCostOn(targetVmtype)-targetMod.getCost();
			budgetleft -= costinc;
			System.out.printf("budget left: %.2f\n", budgetleft);
			
			// resched
			targetMod.setVmtype(targetVmtype);	
			System.out.printf("Reschedule mod%d to vmtype%d\n", targetMod.getModId(), targetVmtype.getTypeid());
			
			// update current CP
			ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, currentCP);
			
		}// end while	
		workflow.printSched();
		System.out.printf("ED=%.2f, budget left: %.2f\n", ed, budgetleft);
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// numerical example
		/**
		List <VMtype> testtypes = new ArrayList<VMtype>();
		VMtype vt1 = new VMtype(0, 1, 3, 1);
		testtypes.add(vt1);
		
		VMtype vt2 = new VMtype(1, 5, 3, 0.8);
		testtypes.add(vt2);
		
		VMtype vt3 = new VMtype(2, 10, 3, 0.8);
		testtypes.add(vt3);
		
		for (VMtype newtype: testtypes) {
			System.out.printf("VM type %d: num of cores %d, maxprocpower %.2f, charging rate %.2f\n", 
					newtype.getTypeid(), newtype.getCorenum(), newtype.getMaxpower(), newtype.getPrice());
		}
		
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 8, 10, 0, false);
		
		criticalgreedy(mytest, testtypes, 52);
		*/
		
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(5);
		
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 20, 80, 6, false);
		
		criticalgreedy(mytest, vmtypes, 10);
	}
}
