package filewriters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

public class MedAlgResultswriter {
	
	// generate filename for results
	public static String resfilename(int order, int size, int id, boolean opt) {
		String dirprefix = "src/main/resources/output/evalresults2014/";
		String fileformat = ".txt";
		String filename;
		
		if (opt) {
			dirprefix = dirprefix + "opt/" + order + "_" + size + "/";
			filename = dirprefix + "workflow_" + order + "_" + size + "_" + id + fileformat;
		} else {
			dirprefix = dirprefix + order + "_" + size + "/";
			filename = dirprefix + "workflow_" + order + "_" + size + "_" + id + fileformat;	
		}
		System.out.println(filename);
		return filename;
	}
	
	// create parent folders if needed and init file with colume names
	public static void touch(String filename, List<String> algnames) {
		File file = new File(filename);
		file.getParentFile().mkdirs();
		
		try {
			String header = "cost";
			PrintWriter line = new PrintWriter(file);
			
			for (String algname: algnames) {
				header = header + "        " + algname;
			}
			header = header + "\n";
			// erase previous contents
			line.print("");
			line.printf(header);
			line.close();
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	public static void medwrite(String filename, int budgetlevel, List<Double> algresults) {
		
		DecimalFormat dft = new DecimalFormat("###.##");
		String resultline = Integer.toString(budgetlevel);
		
		try {
			BufferedWriter line = new BufferedWriter(new FileWriter(filename, true));
			
			for (Double result: algresults) {
				resultline = resultline + "        " + dft.format(result);
			}
			resultline = resultline + "\n";
			line.write(resultline);
			line.close();
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		resfilename(5, 6, 3, false);
		resfilename(5, 6, 3, true);
	}

}
