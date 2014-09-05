package evaluations;

import java.util.ArrayList;
import java.util.List;

import taskgraph.Workflow;
import utilfunctions.VmTypesGen;
import virtualnet.VMtype;
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
public class CostConstrMed {

	private static int budgetlevels = 20;
	
	private static int[] numOfMods = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
		55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
	
	private static int[] numOfEdges = {6, 15, 60, 80, 200, 300, 500, 500, 580, 500, 
		800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000};
	
	// file id: 0-9
	
	private static int[] numOfTypes = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
	
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
		

		double budgetInc = (maxcost-mincost)/budgetlevels;	
		
		for (int i=1; i<=budgetlevels; i++) {
			double budget = mincost + (i*budgetInc);
			
			// add algs here			
			double cg2 = CG2.cg2(workflow, vmtypes, budget);
			double hbcs = HBCS.hbcs(workflow, vmtypes, budget);
			double ss = ScaleStar.scalestar(workflow, vmtypes, budget);
			//double opt = BruteForce.bruteforce(workflow, vmtypes, budget);
			
			algresults.set(0, cg2);
			algresults.set(1, hbcs);
			algresults.set(2, ss);
			//algresults.set(3, opt);
			
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
		//algnames.add("OPT");
		
		Workflow workflow = new Workflow(false);
		List<VMtype> vmtypes = new ArrayList<VMtype>();
		
		List<Double> algresults = new ArrayList<Double>();
		algresults.add(0.0);
		algresults.add(0.0);
		algresults.add(0.0);
		
		//List <VMtype> vmtypes = new ArrayList<VMtype>();

		// 20 problem index
		for (int i=0; i<20; i++) {
			// new: 50 file indexes
			for (int fileid=0; fileid<50; fileid++) {
				int mods = numOfMods[i];
				int edges = numOfEdges[i];
				int typenum = numOfTypes[i];
				
				// construct workflow and vmtypes
				Workflowreaderlite.readliteworkflow(workflow, mods, edges, fileid, false);
				vmtypes = VmTypesGen.vmTypeList(typenum);
				
				// prepare files to write
				String resfilename = MedAlgResultswriter.resfilename(mods, edges, fileid, false);
				MedAlgResultswriter.touch(resfilename, algnames);
				
				varBudgetLevel(workflow, vmtypes, resfilename, algresults);
			}
		}// end for
	}
}
