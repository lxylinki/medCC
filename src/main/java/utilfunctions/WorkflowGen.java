package utilfunctions;

/**
 * Generate workflows based on CCR and VLR
 * @author Linki
 *
 */
public class WorkflowGen {

	// default generating
	public static void defaultGen(int numOfMods, int numOfEdges) {
		if (numOfEdges < numOfMods -1) {
			System.out.println("Error: too few edges!");
			System.exit(1);
		}
	}
	
	// need some knowledge about network: let base proc=1, base bw 1
	public static void workflowGen(int numOfMods, double VLR, long avgworkload, double CCR) {
		String dirprefix = "src/main/resources/input/workflowdata2014/";
		int layers = (int) Math.ceil(numOfMods/VLR);
		long avgdatasize = (long) Math.ceil(avgworkload*CCR);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
