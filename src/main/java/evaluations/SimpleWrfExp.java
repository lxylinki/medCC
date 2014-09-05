package evaluations;

import java.util.ArrayList;
import java.util.List;

import taskgraph.Module;
import taskgraph.Module.execInfo;
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
public class SimpleWrfExp {

	private static int budgetlevels = 20;
	
	private static int numOfMods = 8;
	
	private static int numOfEdges = 10;
	
	// file id: 0-9
	
	private static int numOfTypes = 3;
	
	private static List<String> algnames = null;
	
	public static void varBudgetLevel(Workflow workflow, List<VMtype> vmtypes, String filename, List<Double> algresults) {
	
		double maxcost = CG2.getMaxCost(workflow, vmtypes);
		double mincost = CG2.getMinCost(workflow, vmtypes);
		
	
		// init
		/**
		for (Module mod: workflow.getModList()) {
			mod.setVmtype(null);
		}*/

		double budgetInc = (maxcost-mincost)/budgetlevels;	
		
		for (int i=0; i<budgetlevels; i++) {
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
			
			System.out.printf("cost %.2f: SS %.2f\tHBCS %.2f\tCG2 %.2f\tOPT %.2f\n", budget, ss, hbcs, cg2, opt);
			
			//workflow.printSched();
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
		
		// exp workflow
		Workflow workflow = new Workflow(false);
		int fileid = 0;
		Workflowreaderlite.readliteworkflow12(workflow, numOfMods, numOfEdges, fileid, false);
		
		// exp vm types
		List<VMtype> vmtypes = new ArrayList<VMtype>();
		vmtypes = VmTypesGen.vmTypeList(73, 0.5, numOfTypes);
		
		// mod w1
		Module mod1 = workflow.getModList().get(1);
		execInfo e10 = new execInfo(0, 43.8, 4.38);
		execInfo e11 = new execInfo(1, 19.2, 7.68);
		execInfo e12 = new execInfo(2, 12.0, 9.6);
		//execInfo e12 = new execInfo(2, 8.5, 6.8);
		mod1.getProfiles().put(vmtypes.get(0), e10);
		mod1.getProfiles().put(vmtypes.get(1), e11);
		mod1.getProfiles().put(vmtypes.get(2), e12);
		mod1.setProfiles(mod1.getProfiles());
		
		// mod w2
		Module mod2 = workflow.getModList().get(2);
		execInfo e20 = new execInfo(0, 22.7, 2.27);
		execInfo e21 = new execInfo(1, 9.6, 3.84);
		execInfo e22 = new execInfo(2, 10.1, 4.04);
		//execInfo e22 = new execInfo(2, 8.1, 6.48);
		mod2.getProfiles().put(vmtypes.get(0), e20);
		mod2.getProfiles().put(vmtypes.get(1), e21);
		mod2.getProfiles().put(vmtypes.get(2), e22);
		mod2.setProfiles(mod2.getProfiles());
		
		// mod w3
		Module mod3 = workflow.getModList().get(3);
		execInfo e30 = new execInfo(0, 13.8, 1.38);
		execInfo e31 = new execInfo(1, 7.0, 2.8);
		execInfo e32 = new execInfo(2, 7.2, 5.76);
		//execInfo e32 = new execInfo(2, 4.6, 3.68);
		mod3.getProfiles().put(vmtypes.get(0), e30);
		mod3.getProfiles().put(vmtypes.get(1), e31);
		mod3.getProfiles().put(vmtypes.get(2), e32);
		mod3.setProfiles(mod3.getProfiles());
		
		// mod w4
		Module mod4 = workflow.getModList().get(4);
		execInfo e40 = new execInfo(0, 47.0, 4.70);
		execInfo e41 = new execInfo(1, 30.0, 12.0);
		execInfo e42 = new execInfo(2, 19.4, 15.52);
		//execInfo e42 = new execInfo(2, 18.0, 14.4);
		mod4.getProfiles().put(vmtypes.get(0), e40);
		mod4.getProfiles().put(vmtypes.get(1), e41);
		mod4.getProfiles().put(vmtypes.get(2), e42);
		mod4.setProfiles(mod4.getProfiles());
		
		// mod w5
		Module mod5 = workflow.getModList().get(5);
		execInfo e50 = new execInfo(0, 752.6, 75.26);
		execInfo e51 = new execInfo(1, 241.6, 96.64);
		execInfo e52 = new execInfo(2, 143.2, 114.56);
		//execInfo e52 = new execInfo(2, 93.8, 75.04);
		mod5.getProfiles().put(vmtypes.get(0), e50);
		mod5.getProfiles().put(vmtypes.get(1), e51);
		mod5.getProfiles().put(vmtypes.get(2), e52);
		mod5.setProfiles(mod5.getProfiles());
		
		// mod w6
		Module mod6 = workflow.getModList().get(6);
		execInfo e60 = new execInfo(0, 377.8, 37.78);
		execInfo e61 = new execInfo(1, 123.1, 49.24);
		execInfo e62 = new execInfo(2, 119.7, 95.76);
		//execInfo e62 = new execInfo(2, 84.2, 67.36);
		mod6.getProfiles().put(vmtypes.get(0), e60);
		mod6.getProfiles().put(vmtypes.get(1), e61);
		mod6.getProfiles().put(vmtypes.get(2), e62);
		mod6.setProfiles(mod6.getProfiles());
		
		
		
		
		List<Double> algresults = new ArrayList<Double>();
		algresults.add(0.0);
		algresults.add(0.0);
		algresults.add(0.0);
		algresults.add(0.0);
		
		
		// prepare files to write
		String resfilename = MedAlgResultswriter.resfilename( numOfMods, numOfEdges, fileid, false);
		System.out.println(resfilename);
		MedAlgResultswriter.touch(resfilename, algnames);
		
		varBudgetLevel(workflow, vmtypes, resfilename, algresults);
		
	}

}
