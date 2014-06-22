package filereaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import taskgraph.DataTrans;
import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;

// read old workflow files no data transfer is count
public class Workflowreaderlite {

	private static BufferedReader input;

	// read file {tiny}workflow_{order}_{size}_{id}.txt and fill the info to newworkflow
	public static void readliteworkflow( Workflow workflow, int order, int size, int id, boolean tiny) {
		
		if (workflow != null) {
			if (workflow.getModList() != null) {
				workflow.setModList(new ArrayList<Module>());
			}
			if (workflow.getDataList() != null) {
				workflow.setDataList(new ArrayList<DataTrans>());
			}
		}
		
		String dirprefix = "src/main/resources/input/workflowdata2012/Cases/";
		String fileformat = ".txt";
		String filename;
		
		if (tiny) {
			filename = dirprefix + "tinyworkflow_" + order + "_" + size + "_" + id + fileformat;
		}
		filename = dirprefix + "workflow_" + order + "_" + size + "_" + id + fileformat;	
		
		//System.out.println(filename);
		try {
			input = new BufferedReader(new FileReader(filename));
			String line;
			// read the workload segment
			while ( (line = input.readLine()) != null ) {
				//System.out.println(line);	
				if (Character.isDigit(line.charAt(0))) {
					StringTokenizer onemod = new StringTokenizer(line);
					int modId = Integer.parseInt(onemod.nextToken());
					long workload = 0;
					if(line.charAt(4) == ':') {
						String weight = line.substring(5);
						workload = Long.parseLong(weight);	
					}
					Module newmod = new Module(modId, workload);
					workflow.addModule(newmod);
				}
				if (line.equals("Suclist")) {
					break;
				}
			}// end while
			
			// continue to read suclist segment
			while ( (line = input.readLine()) != null ) {
				//System.out.println(line);
				if(line.charAt(5) == '>') {
					StringTokenizer onemod = new StringTokenizer(line);
					int modId = Integer.parseInt(onemod.nextToken());					
					
					String tmpsucId = line.substring(6);
					int sucId = Integer.parseInt(tmpsucId);
					
					Module srcMod = workflow.getModule(modId);
					Module dstMod = workflow.getModule(sucId);
	
					srcMod.addSucMod(dstMod);
					dstMod.addPreMod(srcMod);					
					//System.out.printf("src mod id: %d, dst mod id: %d\n", modId, sucId);
				}
				
				if (line.equals("Prelist")) {
					break;
				}
			} // end while
			
			Module exitMod = workflow.getExitMod();
			
			// fix multiple exits issue
			for (Module mod: workflow.getModList()) {
				
				if ( (mod.getOutdegree() == 0) && (mod.getModId() != exitMod.getModId()) ) {
					mod.addSucMod(exitMod);
					exitMod.addPreMod(mod);
				}
			}
		} catch (IOException ioe) {
			System.out.printf("IOException: %s", ioe);
		}
	}

	/**
	 * testing readerlite funcs
	 */
	public static void main(String[] args) {
		
		Workflow mytest = new Workflow(false);
		readliteworkflow(mytest, 10, 15, 3, false);	
		
		
		Module mods = mytest.getModule(4);
		Module mode = mytest.getModule(9);
		CriticalPath.topologicalSort(mytest);
		double cplen = CriticalPath.longestpathlen(mods, mode, mytest);
		
		System.out.printf("%.2f\n", cplen);
		mytest.printTimeInfo();
		mytest.printStructInfo();
		
	}
}
