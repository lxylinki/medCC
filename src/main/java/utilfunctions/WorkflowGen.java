package utilfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import taskgraph.DataTrans;
import taskgraph.Module;
import taskgraph.Workflow;

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
	
	// util func in case mods are out of index order
	public static Module getModFromList(int modId, List<Module> mods) {
		if (mods.get(modId).getModId() == modId) {
			return mods.get(modId);
		} else { // if not in order
			for (Module mod: mods) {
				if (mod.getModId() == modId) {
					return mod;
				}
			}// end for
		}// end else
		return null;
	}
	
	
	// randomly select a mod from list
	public static Module randomModChoice(List<Module> mods, Random selector) {
		int len = mods.size();
		int localidx = selector.nextInt(len);
		return mods.get(localidx);
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
			
			// TODO: generate resource requirements and alpha value
			mods.add(mod);
			System.out.printf("mod %d: workload %d\n", i, mod.getWorkload());
		}
	}
	
	// assign a number of mods to each layer
	// layers: an array containing the number of mods at each layer
	public static void modsNumAtEachLayer(int numOfMods, int[] layers, int numOfLayers, Random myrandom) {
		// tmp array to track the num of mods at each layer
		if (layers == null) {
			layers = new int[numOfLayers];
		}
		
		// first random assign some numbers
		int modcount = 0;
		for (int i=0; i<numOfLayers; i++) {
			if ((i==0) || (i==numOfLayers-1)) {
				layers[i] = 1;
			} else {
				// a number from 1 to VLR*2
				double VLR = numOfMods/numOfLayers;
				layers[i] = 1 + myrandom.nextInt((int) Math.floor(VLR*2));
			}			
			// count total number
			modcount += layers[i];
			//System.out.printf("layers[%d] %d\n", i, layers[i]);
		}
		
		// adjust
		if (modcount < numOfMods ) {
			int numToAdd = numOfMods-modcount;
			for (int i=0; i<numToAdd; i++) {
				// pick a layer that is not 0 and numOfLayers-1
				int j = 1 + myrandom.nextInt(numOfLayers-2);
				// increase current mod num by 1
				layers[j] += 1;
			}
		} else if (modcount > numOfMods) {
			int numToDec = modcount-numOfMods;
			for (int i=0; i<numToDec; i++) {
				// pick a layer that is not 0 and numOfLayers-1
				int j = 1 + myrandom.nextInt(numOfLayers-2);
				// if there are more than 1 mod at this layer
				if (layers[j]>1) {
					// decrease current mod num by 1
					layers[j] -= 1;
				} else {
					// do not count this loop
					i--;
				}
			}
		}// end else if	
		
		for (int i=0; i<numOfLayers; i++) {
			System.out.printf("layers[%d] %d\n", i, layers[i]);
		}
	}
	
	public static void connect(Module pre, Module suc, DataTrans data) {
		// record info in mods
		suc.addPreMod(pre);
		pre.addSucMod(suc);
		
		suc.addDataFrom(pre.getModId(), data);
		pre.addDataTo(suc.getModId(), data);
				
		// set data ids
		data.setSrcModId(pre.getModId());
		data.setDstModId(suc.getModId());	
	}
	
	// adjust CCR 
	public static void adjustCCR(Workflow workflow, double targetCCR) {
		double zero = 0.00000001;	
		double currentCCR = workflow.getCCR();
		
		if (Math.abs(currentCCR-targetCCR) > zero) {
			
			double ratio = (currentCCR/targetCCR);
			// if current data too heavy
			if (ratio > 1) {
				//System.out.println("\nincreasing workload...");
				
				// need to inc mod workload by ratio
				for (Module mod: workflow.getModList()) {
					// skip entry/exit: alwasy 1 unit workload
					if ((mod.getModId()==0) || (mod.getModId()==workflow.getOrder()-1)) {
						continue;
					}
					
					long currentworkload = mod.getWorkload();
					long newworkload = (long)(currentworkload*ratio);
					
					//System.out.printf("mod %d: old %d, new %d\n", mod.getModId(), currentworkload, newworkload);
					mod.setWorkload(newworkload);
				}
			} else if (ratio < 1) { // if current comp too heavy
				System.out.println("increasing datasizes...");
				
				// need to inc data sizes by 1/ratio
				for (DataTrans data: workflow.getDataList()) {
					long currentdatasize = data.getDatasize();
					long newdatasize = (long)(currentdatasize/ratio);
					
					//System.out.printf("data {%d, %d}: old %d, new %d\n", data.getSrcModId(), data.getDstModId(), currentdatasize, newdatasize);
					data.setDataSize(newdatasize);
				}
			}
		}
	}

	/**
	 * 
	 * @param dsrange: data size range
	 * @param workflow: workflow with mod layers marked
	 * @param myrandom
	 */
	public static void buildBasicDataEdges( long mindatasize, long dsrange, Workflow workflow, Random myrandom) {
		/** each mod has at least: 
		 * 1 incoming data from mod in layer mod.layer-1
		 * 
		 * 1 outgoing data to mod in layer mod.layer+1 or larger:
		 * let the first outgoing data dest to mod.layer+1
		 * then may add outgoing edges to later layers (TODO not in this func)
		 */
		for (Module mod: workflow.getModList()) {
			// skip entry mod
			if (mod.getModId()==0) {
				continue;
			}
			int mylayer = mod.getLayer();
			List<Module> preLayer = workflow.getModsAtLayer(mylayer-1);		
			Module pre = randomModChoice(preLayer, myrandom);

			// generate data
			long indatasize = mindatasize + (long)(dsrange*myrandom.nextDouble());
			DataTrans indata = new DataTrans(indatasize);
			
			// build dependency
			connect(pre, mod, indata);
	
			// add data to workflow
			workflow.addData(indata);
				
		}
		
		// add outgoing edge
		for (Module mod: workflow.getModList()) {
			
			//skip entry and exit mod
			if ( (mod.getModId()==0) || (mod.getModId() == workflow.getOrder()-1)) {
				continue;
			}
			
			// if have no outgoing edge add one
			if (mod.getOutdegree()==0) {
				int mylayer = mod.getLayer();
				List<Module> sucLayer = workflow.getModsAtLayer(mylayer+1);
				Module suc = randomModChoice(sucLayer, myrandom);
				
				long outdatasize = mindatasize + (long)(dsrange*myrandom.nextDouble());
				DataTrans outdata = new DataTrans(outdatasize);
				
				connect(mod, suc, outdata);
				workflow.addData(outdata);	
			}	
		}		
	}
	
	/**
	 * Build cross-layer data edges
	 * @param mindatasize
	 * @param dsrange
	 * @param workflow
	 * @param myrandom
	 */
	public static void buildExtraDataEdges( long mindatasize, long dsrange, Workflow workflow, Random myrandom) {
		/**
		 * each mod has 1 outgoing data to a mod for each layer > mod.layer+1
		 */
		
		for (Module mod: workflow.getModList()) {
			//skip entry and exit mod
			if ( (mod.getModId()==0) || (mod.getModId() == workflow.getOrder()-1)) {
				continue;
			}
			int mylayer = mod.getLayer();
			int maxlayer = workflow.getNumOfLayers()-1;
			
			for (int i=mylayer+2; i<=maxlayer; i++) {
				List<Module> farLayer = workflow.getModsAtLayer(i);
				Module suc = randomModChoice(farLayer, myrandom);
				// generate data
				long outdatasize = mindatasize + (long)(dsrange*myrandom.nextDouble());
				DataTrans outdata = new DataTrans(outdatasize);
				connect(mod, suc, outdata);
				workflow.addData(outdata);	
			}
		}
	}
	
	/** need some knowledge about network: 1 is the base in all work
	 * let base proc=1, base bw=1
	 * 
	 * @param numOfMods
	 * @param VLR (>1): pipeline-parallel
	 * @param avgworkload (>10)
	 * @param CCR: computation-communication
	 */
	public static void workflowGen(int numOfMods, double VLR, long avgworkload, double CCR, boolean dense) {
		// dir to write to
		//String dirprefix = "src/main/resources/input/workflowdata2014/";
		
		// get number of layers and avg datasize
		int numOfLayers = (int) Math.ceil(numOfMods/VLR);
		
		// always pick 10 as min load and avg*2 as max
		long minworkload = 10;
		long maxworkload = avgworkload*2;	
		long loadrange = maxworkload - minworkload;
		
		// random num generator
		Random myrandom = new Random();
		
		Workflow workflow = new Workflow(true);
		// generate mods
		List<Module> mods = workflow.getModList();
		modListGen(numOfMods, minworkload, loadrange, myrandom, mods);
		
		// determine how many mods at each layer
		int[] modsAtLayer = new int[numOfLayers];
		modsNumAtEachLayer(numOfMods, modsAtLayer, numOfLayers, myrandom);
		
		
		// assgin mods to layers while preserve modId order
		int[] left = new int[numOfLayers];
		int[] right = new int[numOfLayers];
		
		// calcu index intervals
		for (int i=0; i<numOfLayers; i++) {
			if (i==0) {
				left[i] = 0;
				right[i] = left[i]+modsAtLayer[i]-1;
			} else {
				left[i] = right[i-1]+1;
				right[i] = left[i]+modsAtLayer[i]-1;
			}
			//System.out.printf("[%d, %d]\n", left[i], right[i]);
			
			// all modules within interval are assgined to corresponding layer
			for (int j=left[i]; j<=right[i]; j++) {
				Module mod = getModFromList(j, mods);
				mod.setLayer(i);
				//System.out.printf("mod %d: layer %d\n", j, mod.getLayer());
			}
		}// end for: calcu index interval
					
		// generate data according to mods
		long avgdatasize = (long) Math.ceil(avgworkload*CCR);
		// always pick 10 as min size and avg*2 as max
		long mindatasize = 10;
		long maxdatasize = avgdatasize*2;	
		long dsrange = maxdatasize - mindatasize;
		
		// add necessary number of data edges to form a DAG
		buildBasicDataEdges(mindatasize, dsrange, workflow, myrandom);
		
		// add more cross-layer data edges to have a dense structure
		if (dense) {
			buildExtraDataEdges(mindatasize, dsrange, workflow, myrandom);
		}
		
		// adjust datasize/workload to meet target CCR
		adjustCCR(workflow, CCR);
		
		/**
		workflow.printStructInfo();
		System.out.println(workflow.getCCR());
		System.out.println(workflow.getAvgDegree());
		*/
		
		// write to file
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		workflowGen(20, 3.5, 100, 2.50, true);
	}

}
