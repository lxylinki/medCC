package vmMap;

import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import virtualnet.VMtype;

public class CriticalGreedy {

	// cost of exec mod on vmtype
	public static double calcuCost(Module mod, VMtype vmtype) {
		double exectime = Math.ceil(mod.execTime(vmtype));
		double cost = exectime*(vmtype.getPrice());
		return cost;
	}
	
	
		
	// main algorithm
	public static void criticalgreedy(Workflow workflow, List<VMtype> vmtypes, double budget) {
		
		//TODO: pick min-cost and min-delay vm types
		VMtype mintype = vmtypes.get(0);
		
		/**
		VMtype maxtype = vmtypes.get(vmtypes.size()-1);
		
		// compute max cost
		double maxCost = 0;				
		for (Module mod: workflow.getModList()) {
			maxCost += calcuCost(mod, maxtype);
		}*/
		
		// compute min cost and init with cheapest mapping
		double minCost = 0;
		for (Module mod: workflow.getModList()) {
			mod.setVmtype(mintype);
			double maxtime = mod.execTime(mintype);
			mod.setTime(maxtime);
			mod.setCost(calcuCost(mod, mintype));
			minCost += mod.getCost();
		}		
		
		if (budget < minCost) {
			System.out.printf("Error: budget %.2f below min cost %.2f.\n", budget, minCost);
			System.exit(1);
		}		
		// keep track of current cost
		double currentCost = minCost;
		
		/**
		System.out.printf("Min cost %.2f\n", minCost);
		System.out.printf("Costing %.2f\n", currentCost);
		System.out.printf("Max cost %.2f\n", maxCost);
		*/
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
