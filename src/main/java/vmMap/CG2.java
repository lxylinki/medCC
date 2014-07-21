package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;
import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;

public class CG2 {
	
	// profile exec info
	public static void profile(Workflow workflow, List<VMtype> vmtypes) {
		// mark layers
		CriticalPath.topologicalSort(workflow);
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
				mod.setVmtype(null);
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
			VMtype maxcosttype = mod.getMaxCostType(vmtypes);
			maxcost += mod.getCostOn(maxcosttype);
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
			VMtype mincosttype = mod.getMinCostType(vmtypes);
			mincost += mod.getCostOn(mincosttype);
		}
		return mincost;
	}
	
	public static double getMinDelay(Workflow workflow, List<VMtype> vmtypes) {
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			VMtype vprime = mod.getMinDelayType(vmtypes);
			mod.setVmtype(null);
			mod.setVmtype(vprime);
		}
		double mindelay = workflow.getEd();
		//System.out.printf("Min delay %.2f\n", mindelay);
		
		for (Module mod: workflow.getModList()) {
			mod.setVmtype(null);
		}
		
		return mindelay;
	}
	
	
	public static double getMaxDelay(Workflow workflow, List<VMtype> vmtypes) {
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			VMtype vmin = mod.getMaxDelayType(vmtypes);
			mod.setVmtype(null);
			mod.setVmtype(vmin);
		}
		double maxdelay = workflow.getEd();
		//System.out.printf("Max delay %.2f\n", maxdelay);
		
		for (Module mod: workflow.getModList()) {
			mod.setVmtype(null);
		}
		
		return maxdelay;
	}
	

	// select init type based on budget level
	public static VMtype selectInitType(Module mod, List<VMtype> vmtypes, double budgetlevel) {
		
		VMtype vtmax = mod.getMinDelayType(vmtypes);
		VMtype vtmin = mod.getMaxDelayType(vmtypes);
		VMtype vtselected = null;
		
		double targetcost = mod.getCostOn(vtmin) + (mod.getCostOn(vtmax)-mod.getCostOn(vtmin))*budgetlevel;

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
			return getMinDelay(workflow, vmtypes);
		}		
		
	
		double budgetlevel = (budget - mincost)/(maxcost - mincost);	
		
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

		// tuning based on init sched
		// it may exceed budget 
		if (initialcost > budget) {
			// rollback by cgrev
			double costToDec = initialcost - budget;
			ed = CGrev.cgrev(workflow, costToDec, vmtypes);
		}
		
		// now there may be some cost left
		double currentCost = workflow.getCost();
		
		if (currentCost < budget) {
			// spend it by cg
			double costToSpend = budget - currentCost;
			List<Module> currentCP = new ArrayList<Module>();
			ed = CG.cg(workflow, vmtypes, costToSpend, currentCP);
		}
	
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
		
		double budget = 3.8;
		double ed = cg2(workflow, vmtypes, budget);
		workflow.printSched();
		System.out.printf("ED=%.2f, Util %.2f\n", ed, workflow.getCost()/budget);
	}

}
