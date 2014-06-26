package vmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import filereaders.Workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.ScaleStar.blevelComparator;

/**
 * HBCS alg often consume more than given budget.
 * @author linki
 *
 */
public class HBCS {

	public static void calcublevel(Module mod, Workflow sortedworkflow) {
		double blev = CriticalPath.longestpathlen(mod, sortedworkflow.getExitMod(), sortedworkflow, null);
		mod.setBlevel(blev);
	}
	
	public static boolean unschededMod(Workflow workflow) {
		boolean result = false;
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			
			if (mod.getVmtypeid() == -1) {
				result = true;
				break;
			}
		}
		return result;		
	}
	
	
	public static double TimeRate(Module mod, VMtype vmj, List<VMtype> vmtypes) {
		VMtype worst = mod.getMaxDelayType(vmtypes);
		VMtype best = mod.getMinDelayType(vmtypes);
		
		double FTworst = mod.getTimeOn(worst);
		double FT = mod.getTimeOn(vmj);
		double FTbest = mod.getTimeOn(best);
		
		double timerate = (FTworst - FT)/(FTworst - FTbest);
		return timerate;
	}
	
	public static double CostRate(Module mod, VMtype vmj, List<VMtype> vmtypes) {
		VMtype best = mod.getMinDelayType(vmtypes);
		VMtype highest = mod.getMaxCostType(vmtypes);
		VMtype lowest = mod.getMinCostType(vmtypes);
		
		double Costbest = mod.getCostOn(best);
		double Cost = mod.getCostOn(vmj);
		double Costhighest = mod.getCostOn(highest);
		double Costlowest = mod.getCostOn(lowest);
		
		double costrate = (Costbest - Cost)/(Costhighest - Costlowest);
		return costrate;
	}

	public static double hbcs( Workflow workflow, List<VMtype> vmtypes, double budget) {
		// profile all exec info
		CG2.profile(workflow, vmtypes);
		
		// check budget input
		double mincost = CG2.getMinCost(workflow, vmtypes);
		if (budget < mincost) {
			System.out.printf("Error: budget %.2f below min cost %.2f.\n", budget, mincost);
			System.exit(1);
		}
		
		// directly apply best sched if budget is redundant
		double maxcost = CG2.getMaxCost(workflow, vmtypes);
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
		
		// sort by b-level
		CriticalPath.topologicalSort(workflow);
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
		Collections.sort( workflow.getModList(), new blevelComparator());
		
		double currentCost = 0;
		double RB = budget;
		double RCB = CG2.getMinCost(workflow, vmtypes);
		
		while(unschededMod(workflow)) {
	
			for (Module mod: workflow.getModList()) {
				
				if (mod.getVmtypeid()==-1) {
					VMtype mincosttype = mod.getMinCostType(vmtypes);
					double localmincost = mod.getCostOn(mincosttype);
					
					RCB -= localmincost;
					double costcoeff = RCB/RB;
					double maxworthiness = Double.MIN_VALUE;
					VMtype selectedtype = null;
					
					// select type with max worthiness
					for (VMtype type: vmtypes) {
						VMtype best = mod.getMinDelayType(vmtypes);
						double thiscost = mod.getCostOn(type);
						
						if (thiscost > mod.getCostOn(best)) {
							continue;
						}
						
						if (thiscost > (RB - RCB)) {
							continue;
						}
						
						// added budget check
						if ( thiscost > (budget - currentCost) ) {
							continue;
						}
						
						double worthiness = CostRate(mod, type, vmtypes)*costcoeff + TimeRate(mod, type, vmtypes);

						if (worthiness >= maxworthiness) {
							maxworthiness = worthiness;
							selectedtype = type;
						}
					}// end types check for loop
					
					// use min cost type if not found
					if (selectedtype == null) {
						selectedtype = mod.getMinCostType(vmtypes);
					}
					
					mod.setVmtype(selectedtype);
					RB -= mod.getCost();
					currentCost += mod.getCost();
				}
			}// end mod check for loop
		}// end while
		
		double ed = workflow.getEd();
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
		double ed = hbcs(workflow, vmtypes, budget);
		workflow.printSched();
		System.out.printf("ED=%.2f, Util %.2f\n", ed, workflow.getCost()/budget);

	}

}
