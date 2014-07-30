package utilfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import taskgraph.DataTrans;
import taskgraph.Module;
import taskgraph.Workflow;

import utilfunctions.NetworkGen;
import filewriters.WorkflowWriter;

/**
 * Generate workflows based on CCR and VLR
 * @author Linki
 *
 */
public class WorkflowGen {
	// lower bound when randomly generating values
	private static long minworkload = 10;
	
	private static long mindatasize = 10;
	
	private static int minram = 5;
	
	private static int mindisk = 5;
	
	// based on network parameters: for generating resource requirement
	public static int avg_procpower = NetworkGen.maxcpu/2;
	
	public static int avg_ram = NetworkGen.maxram/2;
	
	public static int avg_disk = NetworkGen.maxdisk/2;
	
	
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
		
		//System.out.println("id   workload   alpha   ram   disk");
		for (int i=0; i<numOfMods; i++) {
			Module mod;
			// if entry/exit: trivial load, no parallel, default resource requirement:1
			if (i==0 || i==numOfMods-1) {
				mod = new Module(i, 1);
				mod.setAlpha(0.0);
			} else {
				long frac = (long)(loadrange*myrandom.nextDouble());
				mod = new Module(i, (minworkload + frac));
				
				// parallel: 0.1-0.9
				double alpha = 0.1 + 0.8*myrandom.nextDouble();
				mod.setAlpha(alpha);
				
				// resource requirement: min to avg
				int wram = minram + myrandom.nextInt(avg_ram);
				int wdisk = mindisk + myrandom.nextInt(avg_disk);
				
				mod.setWram(wram);
				mod.setWdisk(wdisk);
			}
			mods.add(mod);
			//System.out.printf("%d\t%d\t%.2f\t%d\t%d\n", i, mod.getWorkload(), mod.getAlpha(), mod.getWram(), mod.getWdisk());
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
		
		/**
		for (int i=0; i<numOfLayers; i++) {
			System.out.printf("layers[%d] %d\n", i, layers[i]);
		}*/
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
				//System.out.println("increasing datasizes...");
				
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
	 * Build only necessary number of data edges to form a DAG
	 * 
	 * @param dsrange: data size range
	 * @param workflow: workflow with mod layers marked
	 * @param myrandom
	 */
	public static void buildMinDataEdges( long mindatasize, long dsrange, Workflow workflow, Random myrandom) {
		/** ensure each mod has: 
		 * 1 incoming data from mod in layer mod.layer-1
		 * 1 outgoing data to mod in layer mod.layer+1
		 * in a uniform way: min number of edges only
		 */
		if (workflow.getModDistOnLayers() == null) {
			System.out.println("Error: module distribution not set.");
			System.exit(1);
		}
		for (Module mod: workflow.getModList()) {
			// skip entry mod
			if (mod.getModId()==0) {
				continue;
			}
			
			int mylayer = mod.getLayer();
			
			int modsInMyLayer = workflow.getModDistOnLayers()[mylayer];
			int modsInPreLayer = workflow.getModDistOnLayers()[mylayer-1];
			
			// out degree bound on pre mods
			int minMaxDeg;
			if (modsInPreLayer >= modsInMyLayer) {
				minMaxDeg = 1;
			} else {
				minMaxDeg = (int) Math.ceil( (double)modsInMyLayer/modsInPreLayer);
			}
			
			List<Module> preLayer = workflow.getModsAtLayer(mylayer-1);		
			//Module pre = randomModChoice(preLayer, myrandom);
			Module pre = null;
			for (Module premod: preLayer) {
				if (premod.getOutdegree() < minMaxDeg) {
					pre = premod;
					break;
				}
			}

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
			
			// skip those already have outgoing data
			if (mod.getOutdegree()>0) {
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
	 * Build cross-layer data edges for dense DAG
	 * 
	 * @param mindatasize
	 * @param dsrange
	 * @param workflow
	 * @param myrandom
	 */
	public static void buildExtraDataEdges( long mindatasize, long dsrange, Workflow workflow, Random myrandom, int edgesToAdd) {

		int targetNum = edgesToAdd;
		//System.out.println(targetNum);

		while (targetNum > 0) {
			//System.out.println(targetNum);
			// input workflow to this func already got min num of edges		
			for (Module mod: workflow.getModList()) {
				
				//skip exit mod
				if ( mod.getModId() == workflow.getOrder()-1 ) {
					continue;
				}
				
				int mylayer = mod.getLayer();
				int maxlayer = workflow.getExitMod().getLayer();
				
				// start from next layer
				for (int i=mylayer+1; i<=maxlayer; i++) {
					
					List<Module> farLayer = workflow.getModsAtLayer(i);
					
					// collect mods that are not already suc in that layer
					List<Module> sucCands = new ArrayList<Module>();
					
					for (Module farmod: farLayer) {
						if ( !(mod.getSucMods().contains(farmod))) {
							sucCands.add(farmod);
						} 					
					}// end suc for
					
					// no more mods can be added from this layer
					if (sucCands.isEmpty()) {
						// continue to look into next layer
						continue;						
					}
					
					Module suc = randomModChoice(sucCands, myrandom);
					
					// generate data
					long outdatasize = mindatasize + (long)(dsrange*myrandom.nextDouble());
					DataTrans outdata = new DataTrans(outdatasize);
					connect(mod, suc, outdata);
					workflow.addData(outdata);	
					
					// decrement target num
					targetNum --;
					if (targetNum == 0) {
						break;
					}
				}// end layers for
				
				if (targetNum == 0) {
					break;
				}
			}// end mods for
			
		}// end while
	}
	
	
	/**
	 * Calculate edge number bounds given mod distribution across layers
	 * 
	 * @param numOfLayers
	 * @param modsAtLayer
	 * @return
	 */
	public static int calcuMinNumOfEdges(int numOfLayers, int[] modsAtLayer) {
		int minNumOfEdges = 0;
		for (int i=0; i<numOfLayers-1; i++) {
			int minbetween;
			//System.out.printf("%d %d\n", modsAtLayer[i], modsAtLayer[i+1]);
			if (modsAtLayer[i] > modsAtLayer[i+1]) {
				minbetween = modsAtLayer[i];
			} else {
				minbetween = modsAtLayer[i+1];
			}
			minNumOfEdges += minbetween;
		}
		//System.out.print(minNumOfEdges);
		return minNumOfEdges;
	}
	
	
	public static int calcuMaxNumOfEdges( int numOfLayers, int[] modsAtLayer ) {
		int maxNumOfEdges = 0;
		//skip exit mod 
		for (int i=0; i<numOfLayers-1; i++) {
			int numAfter = 0;
			// calcu the num of mods after this layer
			for (int j=i+1; j<numOfLayers; j++) {
				numAfter += modsAtLayer[j];
			}
			// max num of outgoing edges from current layer
			//System.out.printf("%d\n", numAfter);
			int edgesOut = modsAtLayer[i]*numAfter;
			maxNumOfEdges += edgesOut;
		}
		return maxNumOfEdges;
	}
	
	/** default generating */
	public static void workflowDefaultGen(Workflow workflow, long avgworkload, int numOfMods, int numOfEdges) {
		// check input
		if ((workflow == null) || (workflow.getOrder()>0)) {
			workflow = new Workflow(false);
		}
		
		// check max edges
		int maxNumOfEdges = numOfMods*(numOfMods-1)/2;
		if (numOfEdges > maxNumOfEdges) {
			System.out.println("Error: too many edges requested.");
			System.exit(1);
		}
		
		// always pick 10 as min load and avg*2 as max
		long maxworkload = avgworkload*2;	
		long loadrange = maxworkload - minworkload;
		
		// random num generator
		Random myrandom = new Random();
		
		// generate mods
		List<Module> mods = new ArrayList<Module>();
		modListGen(numOfMods, minworkload, loadrange, myrandom, mods);
		workflow.setModList(mods);
		
		// id: 0-modId, modId-(numOfMods-1)
		int maxId = numOfMods-1;
		
		// randomly choose a pre mod and a suc mod in terms of mod id
		for (Module mod: workflow.getModList()) {
			int myId = mod.getModId();
			// entry mod only have outgoing edges
			if (myId == 0) {
				int sucId = myId + 1 + myrandom.nextInt(maxId);
				Module suc = workflow.getModule(sucId);
				mod.addSucMod(suc);
				suc.addPreMod(mod);	
				
			} else if (myId == maxId) {
				if (mod.getIndegree() == 0) {
					// exit mod only have incoming edges
					int preId = myrandom.nextInt(myId);
					Module pre = workflow.getModule(preId);
					mod.addPreMod(pre);
					pre.addSucMod(mod);
				}
	
			} else {
				// regular mods
				if (mod.getIndegree() == 0) {
					int preId = myrandom.nextInt(myId);
					Module pre = workflow.getModule(preId);
					// add pre mod
					mod.addPreMod(pre);
					pre.addSucMod(mod);
				}
				
				if (mod.getOutdegree() == 0) {
					int sucId = myId + 1 + myrandom.nextInt(maxId-myId);
					Module suc = workflow.getModule(sucId);
					
					// add suc mod
					mod.addSucMod(suc);
					suc.addPreMod(mod);		
				}		
			}
		}// end for
	
		if (workflow.getSize() < numOfEdges) {
			int edgesToAdd = numOfEdges - workflow.getSize();
			
			while (edgesToAdd > 0) {
				for (Module mod: workflow.getModList()) {
					int myId = mod.getModId();
					Module suc = null;
					for (int sucId = myId+1; sucId <= maxId; sucId++) {
						suc = workflow.getModule(sucId);
						if (!(mod.getSucMods().contains(suc))) {
							break;
						}
					}// end suc for
					if (suc == null) {
						// no more suc for this mod
						// check next mod
						continue;
					}
					mod.addSucMod(suc);
					suc.addPreMod(mod);
					edgesToAdd--;
					if (edgesToAdd == 0) {
						break;
					}
				}// end mod for
			}// end while
			
		} else if (numOfEdges < (numOfMods-1)) {
			System.out.println("Error: too few edges requested.");
			System.exit(1);
		}		
	}
	
	
	/** need some knowledge about network: 1 is the base in all work
	 * let base proc=1, base bw=1
	 * 
	 * @param numOfMods
	 * @param VLR (>1):  pipeline(1)-parallel(numOfMods-2)
	 * @param avgworkload (>10)
	 * @param CCR: computation-communication
	 * @param dense: 0.0(min num of edges)-1.0(max num of edges)
	 */
	public static void workflowGen( Workflow workflow, int numOfMods, double VLR, long avgworkload, double CCR, double dense) {
		// check input
		if ((workflow == null) || (workflow.getOrder()>0)) {
			workflow = new Workflow(true);
		}
		
		// get number of layers and avg datasize
		int numOfLayers = (int) Math.ceil(numOfMods/VLR);
		
		// always pick 10 as min load and avg*2 as max
		long maxworkload = avgworkload*2;	
		long loadrange = maxworkload - minworkload;
		
		// random num generator
		Random myrandom = new Random();
		
		// generate mods
		List<Module> mods = new ArrayList<Module>();
		modListGen(numOfMods, minworkload, loadrange, myrandom, mods);
		workflow.setModList(mods);
		
		// determine module distribution i.e. how many mods in each layer
		int[] modsAtLayer = new int[numOfLayers];
		modsNumAtEachLayer(numOfMods, modsAtLayer, numOfLayers, myrandom);
		
		// calculate bounds on edge number		
		int minNumOfEdges = calcuMinNumOfEdges(numOfLayers, modsAtLayer);
		int maxNumOfEdges = calcuMaxNumOfEdges(numOfLayers, modsAtLayer);
		//System.out.printf("min %d, max %d\n",  minNumOfEdges, maxNumOfEdges);
		
		// set it to workflow
		workflow.setModDistOnLayers(modsAtLayer);

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
		long maxdatasize = avgdatasize*2;	
		long dsrange = maxdatasize - mindatasize;
		
		// add necessary number of data edges to form a DAG
		buildMinDataEdges(mindatasize, dsrange, workflow, myrandom);
		
		// add more cross-layer data edges to have a dense structure
		if (dense > 0.0) {
			int edgesToAdd = (int) ( (maxNumOfEdges - minNumOfEdges)*Math.floor(dense) );
			buildExtraDataEdges(mindatasize, dsrange, workflow, myrandom, edgesToAdd);
		}
		
		// adjust datasize/workload to meet target CCR
		adjustCCR(workflow, CCR);
	}
	
	
	// generate workflow data set in tcc14 simulation
	public static void tcc14_dataset() {
		// file id: 0-9
		int[] numOfMods = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
			55, 60, 65, 70, 75, 80, 85, 90, 95, 100};
		
		int[] numOfEdges = {6, 15, 60, 80, 200, 300, 500, 500, 580, 500, 
			800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000};
		
		// just the array length
		int scales = 20;
		
		// file id: 0-maxFileId
		int maxFileId = 50;
		
		long avgload = 500;
		
		Workflow workflow = null;
		
		for (int i=0; i<scales; i++) {
			int modNum = numOfMods[i];
			int edgeNum = numOfEdges[i];
			// file id: instances
			for (int j=0; j<maxFileId; j++) {
				workflow = new Workflow(false);
				workflowDefaultGen(workflow, avgload, modNum, edgeNum);
				System.out.printf("%d, %d, %d\n", workflow.getOrder(), workflow.getSize(), j);
				WorkflowWriter.writeLiteWorkflow(workflow, j);
			}
		}// end for		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long avgload = 300;
		for (int i=0; i<10; i++) {
			Workflow workflow = new Workflow(true);
			workflowGen(workflow, 20, 3.5, avgload, 5, 0.8);
			WorkflowWriter.writeWorkflow(workflow, i);
			System.out.printf("file %d\n", i);
		}
	}

}
