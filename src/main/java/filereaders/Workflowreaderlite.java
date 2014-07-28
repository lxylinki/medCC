package filereaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import taskgraph.Module;
import taskgraph.Workflow;
import utilfunctions.CriticalPath;

// read workflow files where no data transfer is count
// in both formats (2012, 2014)
public class Workflowreaderlite {

	private static BufferedReader input;
	
	/** read file workflow_{order}_{size}_{id}.txt in new format and fill the info to new workflow
	 * 
	 * @param workflow
	 * @param order
	 * @param size
	 * @param id
	 * @param tiny
	 */
	public static void readliteworkflow(Workflow workflow, int order, int size, int id, boolean tiny) {
		if (workflow != null) {
			if ( !(workflow.getModList().isEmpty())) {
				workflow.setModList(new ArrayList<Module>());
			}
		}
		
		String dirprefix = "src/main/resources/input/workflowdata2014/";
		String fileformat = ".txt";
		String filename;	
		
		if (tiny) {
			filename = dirprefix + "tinyworkflow_" + order + "_" + size + "_" + id + fileformat;
		} else {
			filename = dirprefix + "workflow_" + order + "_" + size + "_" + id + fileformat;	
		}
		
		//System.out.println(filename);
		try {
			input = new BufferedReader(new FileReader(filename));
			String line;
			// read the workload segment
			//System.out.println("Mod info start:");
			while ( (line = input.readLine()) != null ) {
				if (line.length() > 0) {
					if (Character.isDigit(line.charAt(0))) {
						//System.out.println(line);
						// parse mod id and workload
						StringTokenizer onemod = new StringTokenizer(line);
						int modId = Integer.parseInt(onemod.nextToken());
						long modLoad = Long.parseLong(onemod.nextToken());
						// construct module
						Module mod = new Module(modId, modLoad);
						workflow.addModule(mod);
					}					
				}
				// break at data info start
				if (line.equals("src    dst")) {
					break;
				}
			}// end while
			
			// continue to read data info segment
			//System.out.println("Data info start:");			
			while ( (line = input.readLine()) != null ) {
				if (line.length() > 0) {
					if (Character.isDigit(line.charAt(0))) {
						//System.out.println(line);
						// parse mod id and workload
						StringTokenizer onedata = new StringTokenizer(line);
						
						int srcModId = Integer.parseInt(onedata.nextToken());
						int dstModId = Integer.parseInt(onedata.nextToken());
						
						Module srcMod = workflow.getModule(srcModId);
						Module dstMod = workflow.getModule(dstModId);
						
						srcMod.addSucMod(dstMod);
						dstMod.addPreMod(srcMod);					
					}					
				}
			} // end while
			
		} catch (IOException ioe) {
			System.out.printf("IOException: %s", ioe);
			System.exit(1);
		}
		
	}

	/** read file {tiny}workflow_{order}_{size}_{id}.txt in old format (2012) and fill the info to new workflow
	 * 
	 * @param workflow
	 * @param order
	 * @param size
	 * @param id
	 * @param tiny
	 */
	public static void readliteworkflow12( Workflow workflow, int order, int size, int id, boolean tiny) {
		
		if (workflow != null) {
			if ( !(workflow.getModList().isEmpty())) {
				workflow.setModList(new ArrayList<Module>());
			}
		}
	
		String dirprefix = "src/main/resources/input/workflowdata2012/";
		String fileformat = ".txt";
		String filename;
		
		if (tiny) {
			filename = dirprefix + "tinyworkflow_" + order + "_" + size + "_" + id + fileformat;
		} else {
			filename = dirprefix + "workflow_" + order + "_" + size + "_" + id + fileformat;	
		}
		
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
			
			// fix multiple exits issue in old format files
			for (Module mod: workflow.getModList()) {
				
				if ( (mod.getOutdegree() == 0) && (mod.getModId() != exitMod.getModId()) ) {
					mod.addSucMod(exitMod);
					exitMod.addPreMod(mod);
				}
			}
		} catch (IOException ioe) {
			System.out.printf("IOException: %s", ioe);
			System.exit(1);
		}
	}

	/**
	 * testing readerlite funcs
	 */
	public static void main(String[] args) {
		
		Workflow mytest = new Workflow(false);
		//readliteworkflow12(mytest, 10, 15, 0, false);	
		readliteworkflow(mytest, 20, 90, 1, false);	
		
		Module mods = mytest.getModule(0);
		int maxId = mytest.getOrder()-1;
		Module mode = mytest.getModule(maxId);
		CriticalPath.topologicalSort(mytest);
		
		List<Module> CP = new ArrayList<Module>();
		double cplen = CriticalPath.longestpathlen(mods, mode, mytest, CP);
		
		System.out.printf("%d mods in CP: %.2f\n", CP.size(), cplen);
		//mytest.printTimeInfo();
		//mytest.printStructInfo();		
		
	}
}
