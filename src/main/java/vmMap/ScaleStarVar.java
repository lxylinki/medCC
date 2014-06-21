package vmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import filereaders.workflowreaderlite;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.ScaleStarOrig.blevelComparator;

public class ScaleStarVar {

	// cost of exec mod on vmtype
	public static double calcuCost(Module mod, VMtype vmtype) {
		double exectime = Math.ceil(mod.execTime(vmtype));
		double cost = exectime*(vmtype.getPrice());
		return cost;
	}
		
	// comparative advantage
	public static double CA1(Module mod, VMtype vmtype1, VMtype vmtype2) {
		double costdiffrate = (calcuCost(mod, vmtype2) - calcuCost(mod, vmtype1))/calcuCost(mod, vmtype1);
		double timediffrate = (mod.execTime(vmtype2)-mod.execTime(vmtype1))/mod.execTime(vmtype1);
		double ca1value = costdiffrate + timediffrate;
		return ca1value;
	}
	
	// set blevel value for mod
	public static void calcublevel(Module mod, Workflow sortedworkflow) {
		double blev = CriticalPath.longestpathlen(mod, sortedworkflow.getExitMod(), sortedworkflow);
		mod.setBlevel(blev);
	}
	
	// ScaleStar main algorithm
	public static void scalestar(Workflow workflow, List<VMtype> vmtypes, double budget) {
		// pick min-cost and min-delay vm types
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
		int N = workflow.getOrder();
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			mod.setVmtype(mintype);
			double maxtime = mod.execTime(mintype);
			mod.setTime(maxtime);
			mod.setCost(calcuCost(mod, mintype));
			minCost += mod.getCost();
			System.out.printf("min cost (mod %d, type%d): %.2f\n", 
					mod.getModId(), mintype.getTypeid(), minCost);
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
		
		CriticalPath.topologicalSort(workflow);
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow);
		System.out.printf("init ED: %.2f, init cost %.2f\n", ed, currentCost);
		
		
		
		// compute b-level
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
	
		// larger b-level first
		Collections.sort( workflow.getModList(), new blevelComparator());
		
		
		for (Module mod: workflow.getModList()) {
			System.out.printf("mod%d, init b-level=%.2f\n", mod.getModId(), mod.getBlevel());
			System.out.printf("mod%d, init time=%.2f\n", mod.getModId(), mod.getTime());
		}
		
		// reassign new vm types
		//int N = workflow.getOrder();
		for (VMtype newtype: vmtypes) {
			
			// skip entry and exit mod
			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				
				double ca1 = CA1(mod, mod.getVmtype(), newtype);
				
				//System.out.printf("mod%d on type%d: CA1=%.2f\n", mod.getModId(), type.getTypeid(), ca1);
				if (ca1 > 0) {
					double newcost = calcuCost(mod, newtype);
					double costdiff = newcost - mod.getCost();

					if ( (currentCost + costdiff) <= budget) {
						mod.setVmtype(newtype);
						double newtime = mod.execTime(newtype);
						mod.setTime(newtime);
						mod.setCost(newcost);
						currentCost += costdiff;
						//System.out.printf("mod %d ---> vmtype %d\n", mod.getModId(), mod.getVmtypeid());
						mod.setRescheduled(mod.getRescheduled()+1);
					} 
				}
			}
		}
		
		// update b-level
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
		
		double med = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow);
		
		
		for (Module mod: workflow.getModList()) {
			//System.out.printf("mod%d, b-level=%.2f\n", mod.getModId(), mod.getBlevel());
			//System.out.printf("mod%d, time=%.2f\n", mod.getModId(), mod.getTime());
			System.out.printf("mod%d, rescheduled %d times\n", mod.getModId(), mod.getRescheduled());
		}
		
		System.out.printf("\nED: %.2f, cost %.2f\n", med, currentCost);
	
	}
	
	public static void main(String[] args) {
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
		workflowreaderlite.readliteworkflow(mytest, 8, 10, 0, false);
		
		scalestar(mytest, testtypes, 100);
		/**
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(9);
		
		Workflow mytest = new Workflow(false);
		workflowreaderlite.readliteworkflow(mytest, 95, 1600, 3, false);
		
		scalestar(mytest, vmtypes, 250);
		*/
	}
}
