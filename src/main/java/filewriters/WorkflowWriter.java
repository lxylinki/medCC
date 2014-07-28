package filewriters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import taskgraph.DataTrans;
import taskgraph.Module;
import taskgraph.Workflow;

public class WorkflowWriter {

	// dir to write to
	public static String dirprefix = "src/main/resources/input/workflowdata2014/";
	
	public static String format = ".txt";
	
	// generate filename
	public static String filename(Workflow workflow, int fileId, boolean full) {
		int modnum = workflow.getOrder();
		int edgenum = workflow.getSize();
		String name = null;
		
		if (full) {
			int vlr = (int) workflow.getVLR();
			int ccr = (int) workflow.getCCR();
			name = "workflow_" + modnum + "_" + edgenum + "_" + vlr + "_" + ccr + "_" + fileId + format;
		} else {
			name = "workflow_" + modnum + "_" + edgenum + "_" + fileId + format;
		}
		
		return name;
	}
	
	
	// no data info
	public static void writeLiteWorkflow(Workflow workflow, int fileId) {
		System.out.println(filename(workflow, fileId, false));
	}
	
	// full workflow
	public static void writeWorkflow(Workflow workflow, int fileId) {
		// full file name
		String filename = dirprefix + filename(workflow, fileId, true);
		
		File file = new File(filename);
		file.getParentFile().mkdirs();
		
		// formatting
		DecimalFormat dft = new DecimalFormat("###.##");
		String tab = "      ";
		
		try {
			// not appending but rewrite as new
			BufferedWriter line = new BufferedWriter(new FileWriter(filename, false));
			// workflow file header
			line.write("|V|=" + workflow.getOrder() +
					 ", |E|=" + workflow.getSize() 	+ 
					 ", |L|=" + workflow.getNumOfLayers() 	+ 
					 ", CCR=" + dft.format(workflow.getCCR()) + 
					 ", AvgDeg=" + dft.format(workflow.getAvgDegree()) + "\n\n");
			
			// mod info header line
			line.write("id   workload   alpha   ram   disk   layer\n");
			for (Module mod: workflow.getModList()) {
				String modinfo = mod.getModId() + tab +  mod.getWorkload() + tab +
						dft.format(mod.getAlpha()) + tab + mod.getWram() +tab + 
						mod.getWdisk() + tab + mod.getLayer() +"\n";
				line.write(modinfo);
			}
			line.write("\n");
			
			// data info header line
			line.write("src    dst    datasize\n");
			for (DataTrans data: workflow.getDataList()) {
				String datainfo = data.getSrcModId() + tab + data.getDstModId() + tab + data.getDatasize() + "\n";
				line.write(datainfo);
			}
			line.close();
			
		} catch (IOException ioe) {
			System.out.println(ioe);
			System.exit(1);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
