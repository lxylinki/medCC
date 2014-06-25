package vmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import filereaders.Workflowreaderlite;

public class CGrev {
	
	// sort by buffer time in decreasing order 
	static class bufferTimeComparator implements Comparator<Module> {
		public int compare(Module mod1, Module mod2) {
			int bufferdiff;
			bufferdiff = (int) Math.abs(mod2.getBuffertime()-mod1.getBuffertime());
			return bufferdiff;
		}		
	}
	
	public static void updateBufferTime(Workflow workflow) {
		for (Module mod: workflow.getModList()) {
			if (mod.isCritical()) {
				mod.setBuffertime(0);
			} else {
				mod.setBuffertime(mod.getLst() - mod.getEst());
			}
		}
	}
	
	// select reschedable mod with max buffertime from a sorted list
	public static Module selectMod( Workflow sortedworkflow, double targetcostdec, List<VMtype> vmtypes) {
		Module targetMod = null;
		for (Module mod: sortedworkflow.getModList()) {
			// skip entry/exit mod
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			
			if (selectByCostDec(mod, targetcostdec, vmtypes) == null) {
				continue;
			}
			targetMod = mod;
			break;
		}
		return targetMod;
	}



	public static VMtype selectByCostDec(Module mod, double targetcostdec, List<VMtype> vmtypes) {
		double maxdec = Double.MIN_VALUE;
		VMtype newtype = null;
		
		// strict check
		for (VMtype type: vmtypes) {
			double costdec = mod.getCost() - mod.getCostOn(type);
			double timeinc = mod.getTimeOn(type) - mod.getTime();
			
			// skip if no cost dec
			if (costdec <= 0) {
				continue;
			}
			
			if (costdec >= maxdec) {
				// skip if time inc too much
				if (timeinc > mod.getBuffertime()) {
					continue;
				}
				maxdec = costdec;
				newtype = type;
			}
			
			// if cost dec can meet target
			if (costdec > targetcostdec) {
				break;
			}
		}
		
		// if no type can be selected under strict check
		if (newtype == null) {
			// a more relaxed check consider only cost
			for (VMtype type: vmtypes) {
				double costdec = mod.getCost() - mod.getCostOn(type);
				
				// skip if no cost dec
				if (costdec <= 0) {
					continue;
				}
				
				if (costdec >= maxdec) {
					maxdec = costdec;
					newtype = type;
				}
				
				// if cost dec can meet target
				if (costdec > targetcostdec) {
					break;
				}				
			}
		}
		return newtype;
	}
	
	// a reverse procedure to extract cost by downgrading vmtype
	public static double cgrev(Workflow workflow, double targetCostToDec, List<VMtype> vmtypes) {
		double ed = workflow.getEd();
		double costToDec = targetCostToDec;
		
		while (costToDec > 0) {
			/**
			double costToDecForThisRound = 0;
			double decRatio = 1-costToDec/(maxcost-budget);
			
			if (decRatio == 0) {
				costToDecForThisRound = costToDec/2;
			} else {
				costToDecForThisRound = costToDec*decRatio;
			}*/

			// update buffer time and sorting 
			
			updateBufferTime(workflow);
			Collections.sort(workflow.getModList(), new bufferTimeComparator());
			//workflow.printTimeInfo();
			
			/**
			Module targetMod = selectMod(workflow, costToDecForThisRound, vmtypes);
			VMtype newtype = selectByCostDec(targetMod, costToDecForThisRound, vmtypes);
			*/
			Module targetMod = selectMod(workflow, costToDec, vmtypes);
			VMtype newtype = selectByCostDec(targetMod, costToDec, vmtypes);
			
			if (newtype == null) {
				break;
			}
			
			double costdec = targetMod.getCost() - targetMod.getCostOn(newtype);
			costToDec -= costdec; 
			//System.out.printf("Cost to dec: %.2f", costToDec);
			
			// resched
			targetMod.setVmtype(newtype);
			
			//System.out.printf("mod%d - vmtype%d, costdec %.2f\n", targetMod.getModId(), newtype.getTypeid(), costdec);
			ed = workflow.getEd();		
			//System.out.printf("ED=%.2f\n", ed);
		}				
		return ed;
	}
	
	public static double criticalgreedy(Workflow workflow, List<VMtype> vmtypes, double budget) {
		// start from min-delay 
		// number of modules = N
		int N = workflow.getOrder();
		// skip entry/exit mod
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			//mod.printProfiles();
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
				}
			}
			//System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
			mincost += localmin;
		}
		
		// max cost: just calculation, no actual mapping
		double maxcost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
			double localmax = Double.MIN_VALUE;
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) > localmax) {
					localmax = mod.getCostOn(vt);
					// init with min delay
					mod.setVmtype(vt);
				}
			}
			maxcost += mod.getCost();
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
		//System.out.printf("budget %.2f, min cost %.2f, max cost %.2f.\n", budget, mincost, maxcost);
		
		double ed = workflow.getEd();
		double costToDec = workflow.getCost()-budget;
		//System.out.printf("Cost to dec: %.2f\n", costToDec);
		
		ed = cgrev(workflow, costToDec, vmtypes);

		return ed;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// numerical example
		
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(7);
		
		Workflow workflow = new Workflow(false);
		Workflowreaderlite.readliteworkflow(workflow, 15, 60, 3, false);
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}	
		
		double ed = criticalgreedy(workflow, vmtypes, 4);
		workflow.printSched();
		System.out.printf("ED=%.2f\n", ed);

	}

}
