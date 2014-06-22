package vmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.ScaleStarOrig.blevelComparator;
import filereaders.Workflowreaderlite;

public class ScaleStarVar {
	
	// comparative advantage
	public static double CA1(Module mod, VMtype vmtype1, VMtype vmtype2) {
		double costdiffrate = (mod.getCostOn(vmtype2) - mod.getCostOn(vmtype1))/mod.getCostOn(vmtype1);
		double timediffrate = (mod.getTimeOn(vmtype2) - mod.getTimeOn(vmtype1))/mod.getTimeOn(vmtype1);
		double ca1value = costdiffrate + timediffrate;
		return ca1value;
	}
	
	// set blevel value for mod
	public static void calcublevel(Module mod, Workflow sortedworkflow) {
		double blev = CriticalPath.longestpathlen(mod, sortedworkflow.getExitMod(), sortedworkflow, null);
		mod.setBlevel(blev);
	}
	
	// ScaleStar main algorithm
	public static void scalestar(Workflow workflow, List<VMtype> vmtypes, double budget) {
		// number of modules = N
		int N = workflow.getOrder();
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}
		
		// init with min cost
		// skip entry/exit mod
		double mincost = 0;
		for (int i=1; i<N-1; i++) {
			Module mod = workflow.getModule(i);
			double localmin = Double.MAX_VALUE;
			//mod.printProfiles();
			for (VMtype vt: vmtypes) {
				if (mod.getCostOn(vt) < localmin) {
					localmin = mod.getCostOn(vt);
					mod.setVmtype(vt);
				}
			}
			//System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
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
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);
		System.out.printf("init ED: %.2f, init cost %.2f\n", ed, currentCost);
		
		
		// compute b-level
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
	
		// larger b-level first
		Collections.sort( workflow.getModList(), new blevelComparator());
		
		/**
		for (Module mod: workflow.getModList()) {
			System.out.printf("mod%d, init b-level=%.2f\n", mod.getModId(), mod.getBlevel());
			System.out.printf("mod%d, init time=%.2f\n", mod.getModId(), mod.getTime());
		}*/
		
		// reassign new vm types
		//int N = workflow.getOrder();
		for (VMtype newtype: vmtypes) {
			
			// skip entry and exit mod
			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				
				double ca1 = CA1(mod, mod.getVmtype(), newtype);				
				//System.out.printf("mod%d on type%d: CA1=%.2f\n", mod.getModId(), newtype.getTypeid(), ca1);
				if (ca1 > 0) {
					double costdiff = mod.getCostOn(newtype)-mod.getCost();

					if ( (currentCost + costdiff) <= budget) {
						mod.setVmtype(newtype);
						currentCost += costdiff;
						//System.out.printf("mod %d ---> vmtype %d\n", mod.getModId(), mod.getVmtypeid());
						mod.setRescheduled(mod.getRescheduled()+1);
					} 
				}
			}
		}

		double med = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);
		
		/**
		for (Module mod: workflow.getModList()) {
			System.out.printf("mod%d, b-level=%.2f\n", mod.getModId(), mod.getBlevel());
			System.out.printf("mod%d, time=%.2f\n", mod.getModId(), mod.getTime());
			System.out.printf("mod%d, rescheduled %d times\n", mod.getModId(), mod.getRescheduled());
		}*/
		
		System.out.printf("\nED: %.2f, cost %.2f\n", med, currentCost);
		workflow.printSched();
	
	}
	
	public static void main(String[] args) {
		// numerical example
		/**
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
		
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 8, 10, 0, false);
		
		scalestar(mytest, testtypes, 64);
		*/
		
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(4);
		
		Workflow mytest = new Workflow(false);
		Workflowreaderlite.readliteworkflow(mytest, 10, 15, 3, false);
		
		scalestar(mytest, vmtypes, 8);
	}
}
