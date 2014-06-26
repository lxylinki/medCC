package evaluations;

import java.util.ArrayList;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.BHEFT;
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
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}
		
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
		System.out.printf("Max cost %.2f\n", maxcost);
		
		
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
		System.out.printf("Min cost %.2f\n", mincost);
				
		// min delay
		int V = vmtypes.size();
		VMtype vprime = vmtypes.get(V-1);
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
			mod.setVmtype(vprime);
		}
		double mindelay = workflow.getEd();
		//workflow.printSched();
		System.out.printf("Min delay %.2f\n", mindelay);
		
		
		VMtype vbasic = vmtypes.get(0);
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
			mod.setVmtype(vbasic);
		}		
		double maxdelay = workflow.getEd();
		System.out.printf("Max delay %.2f\n", maxdelay);	
		
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
		}	
		
		double budgetInc = (maxcost-mincost)/budgetlevels;	
		for (int i=1; i<=budgetlevels; i++) {
			double budget = mincost + (i*budgetInc);
			// add algs here
			//double cgvar = CGvar.criticalgreedy(workflow, vmtypes, budget);
			//double cgrev = CGrev.criticalgreedy(workflow, vmtypes, budget);

			//double ss = ScaleStar.scalestar(workflow, vmtypes, budget);
			//double bheft = BHEFT.bheft(workflow, vmtypes, budget);
			double cg2 = CG2.cg2(workflow, vmtypes, budget);
			double hbcs = HBCS.hbcs(workflow, vmtypes, budget);
			
			double imp = (hbcs - cg2)/hbcs;
			//double imp = (cgvar - cgrev)/cgvar;
			//double otherimp = (ss - bheft)/ss;
			//double imp = (bheft-cg2)/bheft;
			
			//System.out.printf("cost %.2f:\tCG %.2f\tSS %.2f\tImp %.2f\n", budget, cg, ss, imp);
			//System.out.printf("cost %.2f:\tSS %.2f\tCG2 %.2f\tImp %.2f\tUtil %.2f\tToDec %.2f\n", budget, ss, cg2, imp,
			//		workflow.getCost()/budget, workflow.getCost()-budget);
			
			System.out.printf("cost %.2f:\tHBCS %.2f\tCG2 %.2f\tImp %.2f\tUtil %.2f\n", budget, hbcs, cg2, imp,
					workflow.getCost()/budget);
			
			//System.out.printf("cost %.2f:\tBHEFT %.2f\tSS %.2f\tImp %.2f\tutil %.2f\n", budget, bheft, ss, otherimp, 
			//		workflow.getCost()/budget);
			//System.out.printf("Ed %.2f\tUtil %.2f\n", cg, workflow.getCost()/budget);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(9);
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 20, 80, 12, false);
		varBudgetLevel(mytest, vmtypes);
	}

}
