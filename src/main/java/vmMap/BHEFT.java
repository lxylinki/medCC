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

public class BHEFT {

	// set blevel value for mod
	public static void calcublevel(Module mod, Workflow sortedworkflow) {
		double blev = CriticalPath.longestpathlen(mod, sortedworkflow.getExitMod(), sortedworkflow, null);
		mod.setBlevel(blev);
	}
	
	public static double avgCost(Module mod, List<VMtype> vmtypes) {
		double avgcost = 0;
		for (VMtype type: vmtypes) {
			avgcost += mod.getCostOn(type);
		}
		avgcost = avgcost/vmtypes.size();
		return avgcost;
	}
	
	public static double SAB(double budget, Workflow workflow) {
		double mappedcost = 0; // this is already consumed
		double unmappedcost = 0; // this is an estimation
		
		for (Module mod: workflow.getModList()) {
			
			// skip entry/exit mods
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			
			// if unmapped
			if (mod.getVmtypeid() == -1) {
				unmappedcost += mod.getCost();
			} else {
				// already mapped
				mappedcost += mod.getCost();
			}
		}	
		double sab = budget - mappedcost - unmappedcost;
		return sab;
	}

	public static double AF( Module targetmod, Workflow workflow, double sab) {
		/**
		if (sab < 0) {
			return 0;
		}*/
		
		double unmappedcost = 0; // this is an estimation
		for (Module mod: workflow.getModList()) {
			// skip entry/exit mods
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			// if unmapped
			if (mod.getVmtypeid() == -1) {
				unmappedcost += mod.getCost();
			} 
		}
		
		double af = targetmod.getCost()/unmappedcost;
		return af;
	}
	
	// targetmod is unmapped mod which is currently being scheded
	public static double CTB(Module targetmod, Workflow workflow, double budget) {
		double avgcost = targetmod.getCost();
		double sab = SAB(budget, workflow);
		double af = AF(targetmod, workflow, sab);
		return (avgcost + sab*af);
	}
	
	public static double bheft(Workflow workflow, List<VMtype> vmtypes, double budget) {
		VMtype vmin = vmtypes.get(0);
		VMtype vprime = vmtypes.get(vmtypes.size()-1);
		
		// sort by blevel
		CriticalPath.topologicalSort(workflow);
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
		Collections.sort( workflow.getModList(), new blevelComparator());
		
		// init unmapped mod with avg cost
		for (Module mod: workflow.getModList()) {
			double avgcost = avgCost(mod, vmtypes);
			mod.setVmtype(null);
			mod.setCost(avgcost);
		}
		
		// TODO: add strict budget check
		for (Module mod: workflow.getModList()) {
			// skip entry/exit mods
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			
			double sab = SAB(budget, workflow);
			double ctb = CTB(mod, workflow, budget);
			
			//System.out.printf("SAB%d %.2f\n", mod.getModId(), sab);
			
			// affordable set
			List<VMtype> sk = new ArrayList<VMtype>();
			for (VMtype type: vmtypes) {
				if (mod.getCostOn(type) <= ctb) {
					sk.add(type);
				}
			}
			
			if (sk.isEmpty()) {
				if (sab >= 0) {
					mod.setVmtype(vprime);
				} else {
					mod.setVmtype(vmin);
				}
			} else { // select affordable best
				double mintime = Double.MAX_VALUE;
				VMtype afvprime = null;
				for (VMtype aftype: sk) {
					if (mod.getTimeOn(aftype)<=mintime) {
						mintime = mod.getTimeOn(aftype);
						afvprime = aftype;
					}
				}
				mod.setVmtype(afvprime);
			}
		}
		
		CriticalPath.topologicalSort(workflow);		
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow, null);
		
		return ed;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(5);
		Workflow workflow = new Workflow(false);
		Workflowreaderlite.readliteworkflow(workflow, 20, 80, 6, false);
		
		// profiling: collect mod-vmtype execution info
		for (VMtype type: vmtypes) {
			for (Module mod: workflow.getModList()) {
				mod.profiling(type);
			}
		}

		double ed = bheft(workflow, vmtypes, 9.52);
		workflow.printSched();
		workflow.printCost();
		System.out.printf("ED=%.2f\n", ed);		

	}

}
