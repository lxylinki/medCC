package filereaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import taskgraph.DataTrans;
import taskgraph.Module;
import taskgraph.Workflow;

// read workflow files in new format
public class Workflowreader {
	
	private static BufferedReader input;
	
	public static void readWorkflow(Workflow workflow, int order, int size, double VLR, double CCR, int id) {
		if (workflow != null) {
			if ( !(workflow.getModList().isEmpty())) {
				workflow.setModList(new ArrayList<Module>());
			}
			
			if (!(workflow.getDataList().isEmpty())) {
				workflow.setDataList(new ArrayList<DataTrans>());
			}
		} else {
			workflow = new Workflow(false);
		}
		String dirprefix = "src/main/resources/input/workflowdata2014/All_Info/";
		String fileformat = ".txt";
		String filename = dirprefix + "workflow_" + order + "_" + size + "_" + VLR  + "_"  + CCR + "_" + id + fileformat;
		
		//System.out.println(filename);
		try {
			input = new BufferedReader(new FileReader(filename));
			String line;
			while((line=input.readLine()) != null) {
				// if line is not empty
				if (line.length() > 0) {
					if (Character.isDigit(line.charAt(0))) {
						StringTokenizer onemod = new StringTokenizer(line);
						int modId = Integer.parseInt(onemod.nextToken());
						long workload = Integer.parseInt(onemod.nextToken());
						// construct module
						Module mod = new Module(modId, workload);
						
						double alpha = Double.parseDouble(onemod.nextToken());
						int ram = Integer.parseInt(onemod.nextToken());
						int disk = Integer.parseInt(onemod.nextToken());
						int layer = Integer.parseInt(onemod.nextToken());
						
						mod.setAlpha(alpha);
						mod.setWram(ram);
						mod.setWdisk(disk);
						mod.setLayer(layer);
						// add to workflow
						workflow.addModule(mod);
					}
				}
				
				// break when data section start
				if (line.equals("src    dst    datasize")) {
					break;
				}
			}// end while
			
			// continue to read data info segment
			//System.out.println("Data info start:");			
			while ( (line = input.readLine()) != null ) {
				if (line.length() > 0) {
					if (Character.isDigit(line.charAt(0))) {
						StringTokenizer onedata = new StringTokenizer(line);
						int srcModId = Integer.parseInt(onedata.nextToken());
						int dstModId = Integer.parseInt(onedata.nextToken());
						long datasize = Long.parseLong(onedata.nextToken());
						
						DataTrans data = new DataTrans(datasize);
						data.setSrcModId(srcModId);
						data.setDstModId(dstModId);
						
						// build module dependency
						Module srcMod = workflow.getModule(srcModId);
						Module dstMod = workflow.getModule(dstModId);
						srcMod.addDataTo(dstModId, data);
						dstMod.addDataFrom(srcModId, data);
						srcMod.addSucMod(dstMod);
						dstMod.addPreMod(srcMod);
						
						// add to workflow
						workflow.addData(data);
					}
				}
			}
		} catch (IOException ioe) {
			System.out.printf("IO Exception: %s\n", ioe);
			System.exit(1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Workflow mytest = new Workflow(true);
		readWorkflow(mytest, 20, 24, 3.33, 5.01, 3);
		mytest.printStructInfo();
		readWorkflow(mytest, 20, 26, 3.33, 5.01, 0);
		mytest.printStructInfo();

	}

}
