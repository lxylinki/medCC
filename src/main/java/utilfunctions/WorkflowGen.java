package utilfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import taskgraph.Module;

/**
 * Generate workflows based on CCR and VLR
 * @author Linki
 *
 */
public class WorkflowGen {
	
	// default generating
	public static void defaultGen(int numOfMods, int numOfEdges) {
		int minedges = numOfMods -1;
		int maxedges = numOfMods*(numOfMods-1)/2;
		
		if ( (numOfEdges < minedges) || (numOfEdges > maxedges)) {
			System.out.printf("Edges number range: %d, %d\n", minedges, maxedges);
			System.exit(1);
		}
		//TODO
	}
	
	// fill into a list of mods
	public static void modListGen(int numOfMods, long minworkload, long loadrange, Random myrandom, List<Module> mods) {
		if ( !(mods.isEmpty()) || mods == null) {
			mods = new ArrayList<Module>();
		}
		
		for (int i=0; i<numOfMods; i++) {
			Module mod;
			// if entry/exit: trivial load
			if (i==0 || i==numOfMods-1) {
				mod = new Module(i, 1);
			} else {
				long frac = (long)(loadrange*myrandom.nextDouble());
				mod = new Module(i, (minworkload + frac));
			}
			mods.add(mod);
			System.out.printf("mod %d: workload %d\n", i, mod.getWorkload());
		}
	}
	
	// 
	public static void numAtEachLayer() {
		
	}
	
	/** need some knowledge about network: 1 is the base in all work
	 * let base proc=1, base bw 1
	 * 
	 * @param numOfMods
	 * @param VLR: pipeline-parallel
	 * @param avgworkload (>10)
	 * @param CCR: computation-communication
	 */
	public static void workflowGen(int numOfMods, double VLR, long avgworkload, double CCR) {
		// dir to write to
		String dirprefix = "src/main/resources/input/workflowdata2014/";
		
		// get number of layers and avg datasize
		int numOfLayers = (int) Math.ceil(numOfMods/VLR);
		long avgdatasize = (long) Math.ceil(avgworkload*CCR);
		
		// always pick 10 as min load and avg*2 as max
		long minworkload = 10;
		long maxworkload = avgworkload*2;	
		long loadrange = maxworkload - minworkload;
		
		Random myrandom = new Random();
		
		// generate a list of mods
		List<Module> mods = new ArrayList<Module>();
		modListGen(numOfMods, minworkload, loadrange, myrandom, mods);
		
		// tmp array to track the num of mods at each layer
		int[] layers = new int[numOfLayers];
		
		// first random assign some numbers
		int modcount = 0;
		for (int i=0; i<numOfLayers; i++) {
			if ((i==0) || (i==numOfLayers-1)) {
				layers[i] = 1;
			} else {
				layers[i] = myrandom.nextInt(numOfLayers);
			}			
			// count total number
			modcount += layers[i];
		}
		
		if (modcount < numOfMods ) {
			//TODO
		} else if (modcount > numOfMods) {
			
		}
		
		// assgin layers
		for (Module mod: mods) {
		}
		
		// generate a list of data according to mods
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		workflowGen(20, 5.0, 100, 0.5);

	}

}
