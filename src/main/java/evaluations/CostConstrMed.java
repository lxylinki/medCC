package evaluations;

import java.util.ArrayList;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.CG2;
import vmMap.HBCS;
import filereaders.Workflowreaderlite;

/**
 * Evaluate various sched algs on min end-to-end delay under cost constraint
 * 1.Critical-Greedy
 * 2.ScaleStar
 * TODO:
 * 3.GAIN3
 * 4.LOSS3
 * @author Linki
 *
 */
public class CostConstrMed {

	private static int budgetlevels = 20;
	
	public static void varBudgetLevel(Workflow workflow, List<VMtype> vmtypes) {
		CG2.profile(workflow, vmtypes);
		
		double maxcost = CG2.getMaxCost(workflow, vmtypes);
		double mincost = CG2.getMinCost(workflow, vmtypes);
		System.out.printf("Max cost %.2f\n", maxcost);
		System.out.printf("Min cost %.2f\n", mincost);
				
		// min delay
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			VMtype vprime = mod.getMinDelayType(vmtypes);
			mod.setVmtype(null);
			mod.setVmtype(vprime);
		}

		double mindelay = workflow.getEd();
		System.out.printf("Min delay %.2f\n", mindelay);
		
		
		for (Module mod: workflow.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			VMtype vmin = mod.getMinCostType(vmtypes);
			mod.setVmtype(null);
			mod.setVmtype(vmin);
		}
		double maxdelay = workflow.getEd();
		System.out.printf("Max delay %.2f\n", maxdelay);	
		
		// init
		for (Module mod: workflow.getModList()) {
			mod.setVmtype(null);
		}
		
		double budgetInc = (maxcost-mincost)/budgetlevels;	
		for (int i=1; i<=budgetlevels; i++) {
			
			double budget = mincost + (i*budgetInc);
			
			// add algs here			
			double cg2 = CG2.cg2(workflow, vmtypes, budget);
			double hbcs = HBCS.hbcs(workflow, vmtypes, budget);
			//System.out.printf("cost %.2f:\tHBCS %.2f\tUtil %.2f\n", budget, hbcs, workflow.getCost()/budget);
			
			double imp = (hbcs - cg2)/hbcs;
	
			
			System.out.printf("cost %.2f:\tHBCS %.2f\tCG2 %.2f\tImp %.2f\tUtil %.2f\n", budget, hbcs, cg2, imp,
					workflow.getCost()/budget);
					
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(8);
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 20, 80, 8, false);
		varBudgetLevel(mytest, vmtypes);
	}

}
