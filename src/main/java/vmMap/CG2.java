package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;

public class CG2 {
	
	// profile exec info
	public static void profile(Workflow workflow, List<VMtype> vmtypes) {
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}		
	}
	
	// upperbound of exec cost
	public static double getMaxCost( Workflow workflow, List<VMtype> vmtypes ) {
		int N = workflow.getModList().size();
		// max cost
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
		return maxcost;
	}
	
	// lowerbound of exec cost
	public static double getMinCost( Workflow workflow, List<VMtype> vmtypes ) {
		int N = workflow.getModList().size();
		// min cost
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
				}
			}
			mincost += localmin;
		}
		return mincost;
	}
	
	public static double getMeanCost(Workflow workflow, List<VMtype> vmtypes) {
		// profiling: collect mod-vmtype execution info
		double meancost = 0;
		for (Module mod: workflow.getModList()) {
			double localavg = 0;
			for (VMtype type: vmtypes) {
				localavg += mod.getCostOn(type);
			}
			localavg = localavg/vmtypes.size();
			meancost += localavg;
		}
		return meancost;
	}
	
	// select init type based on budget level
	public static VMtype selectInitType(Module mod, List<VMtype> vmtypes, double budgetlevel) {
		VMtype vtmax = mod.getMaxCostType(vmtypes);
		VMtype vtmin = mod.getMinCostType(vmtypes);
		VMtype vtselected = null;
		
		double targetcost = (mod.getCostOn(vtmax)-mod.getCostOn(vtmin))*budgetlevel;
		double diff = Double.MAX_VALUE;
		
		for (VMtype type: vmtypes) {
			if ( Math.abs(mod.getCostOn(type) - targetcost) <= diff ) {
				diff = Math.abs(mod.getCostOn(type) - targetcost);
				vtselected = type;
			}
		}
		return vtselected;
	}
	
	public static double cg2(Workflow workflow, List<VMtype> vmtypes, double budget) {
		// profile all exec info
		profile(workflow, vmtypes);
		
		// check budget input
		double mincost = getMinCost(workflow, vmtypes);
		if (budget < mincost) {
			System.out.printf("Error: budget %.2f below min cost %.2f.\n", budget, mincost);
			System.exit(1);
		}
		
		// directly apply best sched if budget is redundant
		double maxcost = getMaxCost(workflow, vmtypes);
		if (budget >= maxcost) {
			for (Module mod: workflow.getModList()) {
				// skip entry/exit mod
				if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
					continue;
				}
				// get min delay type
				VMtype vprime = mod.getMinDelayType(vmtypes);
				mod.setVmtype(vprime);
			}
			return workflow.getEd();
		}		
		
		double budgetlevel = budget/(maxcost - mincost);
		// set initial sched according to budgetlevel
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			VMtype vt = selectInitType(mod, vmtypes, budgetlevel);
			mod.setVmtype(vt);
		}
		
		double ed = workflow.getEd();
		double initialcost = workflow.getCost();
				
		// it may exceed budget 
		if (initialcost > budget) {
			// rollback by cgrev
			double costToDec = initialcost - budget;
			ed = CGrev.cgrev(workflow, costToDec, vmtypes);
		}
		
		// now there is some cost left
		double currentCost = workflow.getCost();
		if (currentCost > budget) {
			// spend it by cg
			double costToSpend = budget - currentCost;
			List<Module> currentCP = new ArrayList<Module>();	
			ed = CG.cg(workflow, vmtypes, costToSpend, currentCP);			
		}

		//workflow.printSched();
		return ed;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		double budget = 5;
		double ed = cg2(workflow, vmtypes, budget);
		workflow.printSched();
		System.out.printf("ED=%.2f, Util %.2f\n", ed, workflow.getCost()/budget);
	}

}
