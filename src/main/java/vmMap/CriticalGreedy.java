package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import virtualnet.VMtype;

public class CriticalGreedy {

		
	// main algorithm
	public static void criticalgreedy(Workflow workflow, List<VMtype> vmtypes, double budget) {
		
		// number of modules = N
		int N = workflow.getOrder();
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				mod.profiling(type);
			}
		}
		
		// init with min cost
		// skip entry/exit mod
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			mod.printProfiles();
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
					mod.setVmtype(vt);
				}
			}
			System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
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
		System.out.printf("budget %.2f, min cost %.2f, max cost %.2f.\n", budget, mincost, maxcost);	
				
		// keep track of current cost
		double currentCost = mincost;
		
		CriticalPath.topologicalSort(workflow);
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow);
		System.out.printf("init ED: %.2f, init cost %.2f\n", ed, currentCost);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// numerical example
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
		
		// TODO Auto-generated method stub

	}

}
