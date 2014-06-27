package vmMap;

import java.util.ArrayList;
import java.util.List;

import filereaders.Workflowreaderlite;
import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.BruteForceTree;
import utilfunctions.BruteForceTree.node;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;

/**
 * Optimal sched for tiny workflows (|V|<10)
 * @author linki
 *
 */
public class BruteForce {
	
	public static double calcuCost(Workflow workflow, node mapping, List<VMtype> vmtypes) {
		// number of mods
		int N = workflow.getOrder();
		double cost = 0;
		// skip entry/exit mod
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			int vmtypeid = mapping.getPath().get(i);
			VMtype type = VmTypesGen.getType(vmtypeid, vmtypes);
			cost += mod.getCostOn(type);
		}
		return cost;
	}
	
	public static double calcuEd(Workflow workflow, node mapping, List<VMtype> vmtypes) {
		// number of mods
		int N = workflow.getOrder();
		double ed = 0;
		
		// skip entry/exit mod
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			int vmtypeid = mapping.getPath().get(i);
			VMtype type = VmTypesGen.getType(vmtypeid, vmtypes);
			mod.setVmtype(null);
			mod.setVmtype(type);
		}
		
		ed = workflow.getEd();
		return ed;		
	}

	public static double bruteforce(Workflow workflow, List<VMtype> vmtypes, double budget) {
		// profile first
		CG2.profile(workflow, vmtypes);
		
		// construct brute force tree
		int N = workflow.getOrder();
		int V = vmtypes.size();
		BruteForceTree allMappings = BruteForceTree.bruteforcetree(N-1,V);
		
		// each leaf node contains a mapping
		double med = Double.MAX_VALUE;
		node optmap = null;
		
		// search
		for (node leaf: allMappings.getLeafs()) {
			double cost = calcuCost(workflow, leaf, vmtypes);
			//System.out.printf("cost = %.2f\n", cost);
			//leaf.printpath();
			
			if (cost > budget) {
				//System.out.println("exceed budget");
				continue;
			}
			
			double ed = calcuEd(workflow, leaf, vmtypes);
			if (ed <= med) {
				med = ed;
				optmap = leaf;
			}
		}// end for
		
		// budget too low
		if (optmap == null) {
			System.out.print("Error: budget below min cost\n");
			System.exit(1);
		}
		
		//optmap.printpath();
		med = calcuEd(workflow, optmap, vmtypes);
		return med;
	}

	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(3);
		
		Workflow workflow = new Workflow(false);
		Workflowreaderlite.readliteworkflow(workflow, 5, 6, 0, true);
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}		
		double budget = 0.6;
		double ed = bruteforce(workflow, vmtypes, budget);
		workflow.printSched();
		System.out.printf("ED=%.2f, Util %.2f\n", ed, workflow.getCost()/budget);
	}

}
