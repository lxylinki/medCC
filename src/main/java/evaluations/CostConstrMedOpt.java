package evaluations;

import java.util.ArrayList;
import java.util.List;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
import vmMap.BruteForce;
import vmMap.CG2;
import vmMap.HBCS;
import vmMap.ScaleStar;
import filereaders.Workflowreaderlite;
import filewriters.MedAlgResultswriter;

/**
 * Evaluate various sched algs on min end-to-end delay under cost constraint
 * 1.CriticalGreedy2
 * 2.ScaleStar
 * 3.HBCS
 * 4.BruteForce
 * 
 * @author Linki
 *
 */
public class CostConstrMedOpt {

	private static int budgetlevels = 20;
	
	private static int[] numOfMods = {5, 6, 7, 8};
	
	private static int[] numOfEdges = {6, 11, 14, 21};
	
	// file id: 0-99
	
	private static int[] numOfTypes = {5, 6, 7, 8};
	
	private static List<String> algnames = null;
	
	public static void varBudgetLevel(Workflow workflow, List<VMtype> vmtypes, String filename, List<Double> algresults) {
		
		CG2.profile(workflow, vmtypes);
		
		double maxcost = CG2.getMaxCost(workflow, vmtypes);
		double mincost = CG2.getMinCost(workflow, vmtypes);
		
		/**
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
		*/	
		
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
			double ss = ScaleStar.scalestar(workflow, vmtypes, budget);
			double opt = BruteForce.bruteforce(workflow, vmtypes, budget);
			
			algresults.set(0, cg2);
			algresults.set(1, hbcs);
			algresults.set(2, ss);
			algresults.set(3, opt);
			
			//System.out.printf("cost %.2f: SS %.2f\tHBCS %.2f\tCG2 %.2f\tOPT %.2f\n", budget, ss, hbcs, cg2, opt);
			MedAlgResultswriter.medwrite(filename, i, algresults);
		}
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		algnames = new ArrayList<String>();
		// algs to run
		algnames.add("CG2");
		algnames.add("HBCS");
		algnames.add("ScaleStar");
		algnames.add("OPT");
		
		Workflow workflow = new Workflow(false);
		List<VMtype> vmtypes = new ArrayList<VMtype>();
		
		List<Double> algresults = new ArrayList<Double>();
		algresults.add(0.0);
		algresults.add(0.0);
		algresults.add(0.0);
		algresults.add(0.0);
		
		//List <VMtype> vmtypes = new ArrayList<VMtype>();
		
		// 4 problem index: 5, 6, 7, 8
		for (int i=0; i<4; i++) {
			int mods = numOfMods[i];
			int edges = numOfEdges[i];
			int typenum = numOfTypes[i];
			
			// 100 file index
			for (int fileid=0; fileid<100; fileid++) {
				// construct workflow and vmtypes
				Workflowreaderlite.readliteworkflow12(workflow, mods, edges, fileid, true);
				vmtypes = VmTypesGen.vmTypeList(typenum);
				
				// prepare files to write
				String resfilename = MedAlgResultswriter.resfilename(mods, edges, fileid, true);
				MedAlgResultswriter.touch(resfilename, algnames);
				
				varBudgetLevel(workflow, vmtypes, resfilename, algresults);
			}
		}// end for	

	}

}
