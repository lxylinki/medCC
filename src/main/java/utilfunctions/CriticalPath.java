package utilfunctions;

import java.util.Collections;
import java.util.Comparator;

import taskgraph.Module;
import taskgraph.Workflow;

public class CriticalPath {

	// set each module/link's topological layer number
	public static void topologicalSort(Workflow myworkflow) {
		
		// init
		for (Module mod: myworkflow.getModList()) {
			mod.setLayer(0);
		}
		
		for (Module mod: myworkflow.getModList()) {
			if (mod.getOutdegree() > 0) {
				for (Module suc: mod.getSucMods()) {
					suc.setLayer( mod.getLayer()+1);
				}
			}
		}
		Collections.sort(myworkflow.getModList(), new topoLayerComparator());
	}
	
	// sort in increasing order of layer number
	static class topoLayerComparator implements Comparator<Module> {
		public int compare(Module mod1, Module mod2) {
			int layerdiff;
			layerdiff = mod1.getLayer() - mod2.getLayer();
			return layerdiff;
		}
	}
	
	
	
	// return longestpath starts with srcMod and ends at dstMod, if no path return null
	// TODO: need more tests
	public static double longestpathlen(Module srcMod, Module dstMod, Workflow sortedworkflow) {
		
		// check input
		if (dstMod.getLayer() < srcMod.getLayer()) {
			//return null;
			System.out.println("Error in input.\n");
			System.exit(1);
		}
		
		// forward check
		for (Module mod: sortedworkflow.getModList()) {
			// don't go beyond dstMod
			if (mod.getLayer() > dstMod.getLayer()) {
				break;
			}
			// skip modules prior to srcMod
			if (mod.getLayer() < srcMod.getLayer()) {
				mod.setForvisited(0);
				mod.initTime();
				continue;
			}
			
			if (mod.getLayer() == srcMod.getLayer()) {
				if (mod.getModId()!=srcMod.getModId()) {
					mod.setForvisited(0);
					mod.initTime();
					continue;
				} else {
					// srcMod
					mod.setEst(0);
					mod.setEft(mod.getTime());
					mod.setForvisited(mod.getForvisited()+1);
				}
			} else {
				// calculate eft, est
				double maxeftinpre = Double.MIN_VALUE;
				int visitedpre = 0;
				for (Module pre: mod.getPreMods()) {
					if ( pre.getForvisited() > 0 ) {
						visitedpre++;
						if(pre.getEft() >= maxeftinpre) {
							maxeftinpre = pre.getEft();
						}						
					}
				}
				if (visitedpre > 0) {
					mod.setEst(maxeftinpre);
					mod.setEft(mod.getEst()+mod.getTime());
					mod.setForvisited(mod.getForvisited()+1);
				}
			}
		}

		for (int i=sortedworkflow.getOrder()-1; i>=0; i--) {
			Module rmod = sortedworkflow.getModule(i);
			
			if (rmod.getLayer() < srcMod.getLayer()) {
				break;
			}
			
			// skip modules after dstMod
			if (rmod.getLayer() > dstMod.getLayer()) {
				rmod.setBackvisited(0);
				rmod.initTime();
				continue;
			}
			
			if (rmod.getLayer() == dstMod.getLayer()) {
				if (rmod.getModId()!=dstMod.getModId()) {
					rmod.setBackvisited(0);
					rmod.initTime();
					continue;
				}else{
					// dstMod
					rmod.setLft(rmod.getEft());
					rmod.setLst(rmod.getEst());
					rmod.setBackvisited(rmod.getBackvisited()+1);
				}
			} else {
				// calculate lft, lst
				double minlstinsuc = Double.MAX_VALUE;
				int visitedsuc = 0;
				for (Module suc: rmod.getSucMods()) {
					if (suc.getBackvisited() > 0) {
						visitedsuc++;
						if ( suc.getLst() <= minlstinsuc ) {
							minlstinsuc = suc.getLst();
						}
					} 
				}
				if (visitedsuc > 0) {
					rmod.setLft(minlstinsuc);
					rmod.setLst(rmod.getLft()-rmod.getTime());
					rmod.setBackvisited(rmod.getBackvisited()+1);
				}
			}	
		}
		
		/**
		//return resultpath;					
		List<Cloudlet> resultpath = new ArrayList<Cloudlet>();
		for (Module mod: sortedworkflow.getModList()) {
			
			if ( (mod.getForvisited() == 0) || (mod.getBackvisited()==0)) {
				continue;
			}
			
			double buf1 = mod.getLst()-mod.getEst();
			double buf2 = mod.getLft()-mod.getEft();
			
			if ( (Math.abs(buf1) < Float.MIN_VALUE) && (Math.abs(buf2) < Float.MIN_VALUE)) {
				System.out.printf("mod%d\n", mod.getModId());
				resultpath.add(mod);
			}
		}
		return resultpath;	
		*/
		
		return dstMod.getEft();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
