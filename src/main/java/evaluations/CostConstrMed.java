package evaluations;

import java.util.ArrayList;
import java.util.List;

import vmMap.CriticalGreedy;
import vmMap.ScaleStarOrig;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
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
		CriticalPath.topologicalSort(workflow);
		double mindelay = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);
		//workflow.printSched();
		System.out.printf("Min delay %.2f\n", mindelay);
		
		
		VMtype vbasic = vmtypes.get(0);
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(null);
			mod.setVmtype(vbasic);
		}		
		
		double maxdelay = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);
		System.out.printf("Max delay %.2f\n", maxdelay);		
		
		
		double budgetInc = (maxcost-mincost)/budgetlevels;	
		for (int i=0; i<budgetlevels; i++) {
			double budget = mincost + (i*budgetInc);
			double ss = ScaleStarOrig.scalestar(workflow, vmtypes, budget);
			double cg = CriticalGreedy.criticalgreedy(workflow, vmtypes, budget);
			System.out.printf("bud %.2f: CG %.2f\tSS %.2f\n", budget, cg, ss);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(5);
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 20, 80, 6, false);
		varBudgetLevel(mytest, vmtypes);
	}

}
