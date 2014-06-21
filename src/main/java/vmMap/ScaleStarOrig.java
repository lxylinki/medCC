package vmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import filereaders.workflowreaderlite;

public class ScaleStarOrig {
	
	// sort in decreasing order of blevel
	static class blevelComparator implements Comparator<Module> {
		public int compare(Module mod1, Module mod2) {
			int bleveldiff;
			bleveldiff = (int) Math.ceil (mod2.getBlevel()-mod1.getBlevel());
			return bleveldiff;
		}
	}

	
	// cost of exec mod on vmtype
	public static double calcuCost(Module mod, VMtype vmtype) {
		double exectime = Math.ceil(mod.execTime(vmtype));
		double cost = exectime*(vmtype.getPrice());
		return cost;
	}
	
	
	public static double CA1(Module mod, VMtype vmtype1, VMtype vmtype2) {
		double costdiffrate = (calcuCost(mod, vmtype2) - calcuCost(mod, vmtype1))/calcuCost(mod, vmtype1);
		double timediffrate = (mod.execTime(vmtype2)-mod.execTime(vmtype1))/mod.execTime(vmtype1);
		double ca1value = costdiffrate + timediffrate;
		return ca1value;
	}
	
	
	// vmtype2 is newer type
	public static double CA2(Module mod, VMtype vmtype1, VMtype vmtype2) {
		double timediff = mod.execTime(vmtype2)-mod.execTime(vmtype1);
		double costdiff = calcuCost(mod, vmtype1)-calcuCost(mod, vmtype2);
		if ((costdiff <= 0) || (timediff > 0)) {
			return 0;
		} else {
			return (timediff/costdiff);
		}
	}
	
	// set blevel value for mod
	public static void calcublevel(Module mod, Workflow sortedworkflow) {
		double blev = CriticalPath.longestpathlen(mod, sortedworkflow.getExitMod(), sortedworkflow);
		mod.setBlevel(blev);
	}
	
	// set tlevel value for mod
	public void calcutlevel(Module mod, Workflow sortedworkflow) {
		double tlev = CriticalPath.longestpathlen(sortedworkflow.getEntryMod(), mod, sortedworkflow);
		mod.setTlevel(tlev);
	}
	
	// element in arrayA
	static class Aij {
		// index i, mod id
		int i = -1;
		
		// index j, vm type id
		int j = -1;
		
		// element value: ca2
		double val = 0;
		
		Aij(int i, int j, double ca2) {
			this.i = i;
			this.j = j;
			this.val = ca2;
		}
	}
	
	

	// sort in increasing order of ca2 value
	static class aijComparator implements Comparator<Aij> {
		public int compare(Aij a1, Aij a2) {
			int ca2diff;
			ca2diff = (int) (a1.val - a2.val);
			return ca2diff;
		}
	}
	
	// ScaleStar main algorithm
	public static void scalestar(Workflow workflow, List<VMtype> vmtypes, double budget) {
		
		for (Module mod: workflow.getModList()) {
			// init label with avg exec time
			double avgtime = 0;
			for (VMtype type: vmtypes) {
				avgtime += mod.execTime(type);
			}
			avgtime = avgtime / (vmtypes.size());
			mod.setTime(avgtime);			
		}
		
		// sort by blevel
		CriticalPath.topologicalSort(workflow);
		for (Module mod: workflow.getModList()) {
			calcublevel(mod, workflow);
		}
		Collections.sort( workflow.getModList(), new blevelComparator());
		
		/**
		for (Module mod: workflow.getModList()) {
			System.out.printf("mod%d, b-level=%.2f\n", mod.getModId(), mod.getBlevel());
			System.out.printf("mod%d, time=%.2f\n", mod.getModId(), mod.getTime());
		}*/
		
		// build array A
		Aij[][] arrayA;
		int N = workflow.getOrder();
		int V = vmtypes.size();
		arrayA = new Aij[N][V];	
		
		// vprime is the best type i.e. max procpower
		VMtype vprime = vmtypes.get(V-1);
		
		for (int j=0; j<V; j++) {
			
			VMtype vmj = vmtypes.get(j);
			//System.out.printf("checking type %d ...\n", j);
			
			// skip entry and exit mod
			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				//System.out.printf("checking mod %d ...\n", i);
				
				double CA1a = CA1(mod, vmj, vprime);
				double CA1b = CA1(mod, vprime, vmj);
				
				//System.out.printf("CA1(mod %d, vmtype %d, vmtype %d) = %.2f\n", i, j, V-1, CA1a);
				//System.out.printf("CA1(mod %d, vmtype %d, vmtype %d) = %.2f\n", i, V-1, j, CA1b);
				
				if (CA1a > CA1b) {
					// change to type vprime
					mod.setVmtype(vprime);
					// this is makespanNew
					double makespannew = mod.execTime(vprime);
					mod.setTime(makespannew);
					mod.setCost(calcuCost(mod, vprime));
				} else {
					// set to vmj
					mod.setVmtype(vmj);
					// this is makespanOld
					double makespanold = mod.execTime(vmj);
					mod.setTime(makespanold);
					mod.setCost(calcuCost(mod, vmj));
				}
				
			}
		}
		
		for (int j=0; j<V; j++) {
			VMtype vmj = vmtypes.get(j);

			for (int i=1; i<N-1; i++) {
				Module mod = workflow.getModule(i);
				//System.out.printf("mod %d --- vmtype %d\n", mod.getModId(), mod.getVmtypeid());

				if (mod.getVmtypeid() == vmj.getTypeid()) {
					arrayA[i][j] = new Aij(i,j,0);
				} else {
					// assume map to type j
					double ca2val = CA2(mod, mod.getVmtype(), vmj);
					arrayA[i][j] = new Aij(i,j,ca2val);
				}
			}
		}	
		
		// collect non-zero values in arrayA
		List<Aij> setA = new ArrayList<Aij>();
		for (int j=0; j<V; j++) {
			
			for (int i=1; i<N-1; i++) {
				System.out.printf("A[%d][%d] = %.2f ", i, j, arrayA[i][j].val);
				
				if (arrayA[i][j].val != 0) {
					setA.add(arrayA[i][j]);
				}
			}
			System.out.print("\n");
		}		

		// current cost
		double currentCost = 0;
		for (Module mod: workflow.getModList()) {
			currentCost += mod.getCost();
		}
		
		// sort setA in increasing order of ca2val
		if (setA.size() > 0) {
			Collections.sort(setA, new aijComparator());
		}
		
		// while setA is not empty
		while (setA.size() > 0) {
			/**
			for (Double element: setA) {
				System.out.printf(".2f\n", element);
			}*/
			
			if (currentCost > budget) {
				int modid = setA.get(0).i;
				int typeid = setA.get(0).j;
				
				Module modi = workflow.getModule(modid);
				VMtype typej = vmtypes.get(typeid);
				
				double costdiff = calcuCost(modi, typej) - modi.getCost();
				if (costdiff <= 0) {
					// reassign
					modi.setVmtype(typej);
					double timeonj = modi.execTime(typej);
					modi.setTime(timeonj);
					modi.setCost(calcuCost(modi, typej));
				}
				
				// remove element
				setA.remove(0);
			}
			
			if (currentCost <= budget) {
				int maxindex = setA.size()-1;
				int modid = setA.get(maxindex).i;
				int typeid = setA.get(maxindex).j;
				
				Module modi = workflow.getModule(modid);
				VMtype typej = vmtypes.get(typeid);
				
				double costdiff = calcuCost(modi, typej) - modi.getCost();
				currentCost += costdiff;
				if (currentCost <= budget ) {
					// reassign
					modi.setVmtype(typej);
					double timeonj = modi.execTime(typej);
					modi.setTime(timeonj);
					modi.setCost(calcuCost(modi, typej));
				}
				
				// remove element
				setA.remove(maxindex);
			}
			
		}	
		
		if (currentCost > budget) {
			currentCost = 0;
			System.out.println("Cost exceeds budget.\n");
			// use chepest mapping
			VMtype mintype = vmtypes.get(0);
			
			for (Module mod: workflow.getModList()) {
				mod.setVmtype(mintype);
				double maxtime = mod.execTime(mintype);
				mod.setTime(maxtime);
				mod.setCost(calcuCost(mod, mintype));
				currentCost += mod.getCost();
				System.out.printf("mod %d --- vmtype %d\n", mod.getModId(), mod.getVmtypeid());
			}
		}
		
		double ed = CriticalPath.longestpathlen(workflow.getEntryMod(), workflow.getExitMod(), workflow);
		System.out.printf("\nED: %.2f, cost %.2f\n", ed, currentCost);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List <VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(7);
		Workflow mytest = new Workflow(false);
		workflowreaderlite.readliteworkflow(mytest, 10, 15, 3, false);
		scalestar(mytest, vmtypes, 5.69);
	}

}
