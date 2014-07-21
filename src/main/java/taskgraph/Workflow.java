package taskgraph;

import java.util.ArrayList;
import java.util.List;

import utilfunctions.CriticalPath;

public class Workflow {

	private List<Module> modList = null;	
	private List<DataTrans> dataList = null;
	
	
	public Workflow( boolean data ) {
		setModList(new ArrayList<Module>());
		// if data is needed
		if (data) {
			setDataList(new ArrayList<DataTrans>());
		}
	}
	
	public void addModule (Module mod) {
		modList.add(mod);
	}
	
	public void addData (DataTrans data) {
		dataList.add(data);
	}
	
	// get a module by its id
	public Module getModule(int modId) {
		for(Module mod: this.modList) {
			if (mod.getModId() == modId) {
				return mod;
			}
		}
		return null;
	}
	
	// get a data transfer by its end mod ids
	public DataTrans getData(int srcId, int dstId) {
		for(DataTrans data: this.dataList) {
			if ( (data.getSrcModId()==srcId) && (data.getDstModId()==dstId) ) {
				return data;
			}
		}
		return null;
	}

	// total number of modules
	public int getOrder() {
		return modList.size();
	}
	
	// total number of edges
	public int getSize() {
		int size = 0;
		for (Module mod: this.getModList()) {
			size += (mod.getIndegree() + mod.getOutdegree());
		}
		size = (size/2);
		return size;
	}	
	
	public Module getEntryMod() {
		return modList.get(0);
	}
	
	public Module getExitMod() {
		int exitid = (modList.size() - 1);
		return modList.get(exitid);
	}
	
	public void printTimeInfo() {
		if (this.getModList() != null) {
			for (Module mod: this.getModList()) {
				System.out.printf("mod%d est:  %.2f,  eft: %.2f,  lst: %.2f,  lft: %.2f; " +
						"f: %d, b %d times, buffer: %.2f\n", 
						mod.getModId(), mod.getEst(), mod.getEft(), mod.getLst(), mod.getLft(), 
						mod.getForvisited(), mod.getBackvisited(), mod.getBuffertime());
			}
		}
	}
	
	public void printStructInfo() {
		// if data transfer is not counted
		if (this.getDataList() == null) {
			if (this.getModList() != null) {
				System.out.printf("|V|=%d, |E|=%d\n", this.getOrder(), this.getSize());
				for (Module mod: this.getModList()) {
					//System.out.printf("mod %d\n", mod.getModId());
					if ( mod.getOutdegree() > 0) {
						for (Module suc: mod.getSucMods()) {
							System.out.printf("mod%d(load %d, layer %d) ---> mod%d(load %d, layer %d)\n", 
									mod.getModId(), mod.getWorkload(), mod.getLayer(),
									suc.getModId(), suc.getWorkload(), suc.getLayer());
						}
					} else {
						System.out.printf("mod%d(load %d, layer %d)\n",
						mod.getModId(), mod.getWorkload(), mod.getLayer());
					}
					System.out.print("\n");
				}
			} else {
				System.out.println("empty workflow");
			}
		} else {
			//TODO: test this
			if (this.getModList() != null) {
				System.out.printf("|V|=%d, |E|=%d\n", this.getOrder(), this.getSize());
				for (DataTrans data: this.getDataList()) {
					Module srcMod = this.getModule(data.getSrcModId());
					Module dstMod = this.getModule(data.getDstModId());
					// print datasizes
					System.out.printf("mod%d(%d) ---> mod%d(%d), %d\n", 
							srcMod.getModId(), srcMod.getWorkload(),
							dstMod.getModId(), dstMod.getWorkload(), data.getDatasize());
				}
			} else {
				System.out.println("empty workflow");
			}
		}
		
	}
	
	public void printSched() {
		for (Module mod: this.getModList()) {
			System.out.printf("mod%d - vmtype%d\n", mod.getModId(), mod.getVmtypeid());
		}
	}
	
	public void printCost() {
		double cost = 0;
		for (Module mod: this.getModList()) {
			cost += mod.getCost();
		}
		System.out.printf("Total cost: %.2f\n", cost);
	}
	
	public double getCost() {
		double cost = 0;
		for (Module mod: this.getModList()) {
			if (mod.getPreMods().isEmpty() || mod.getSucMods().isEmpty()) {
				continue;
			}
			
			if (mod.getVmtypeid()==-1) {
				continue;
			}
			cost += mod.getCost();
		}
		return cost;
	}
	
	public double getEd() {
		CriticalPath.topologicalSort(this);
		double ed = CriticalPath.longestpathlen(getEntryMod(), getExitMod(), this, null);
		return ed;
	}
	
	
	public List<Module> getModList() {
		return modList;
	}

	public void setModList(List<Module> modList) {
		this.modList = modList;
	}

	public List<DataTrans> getDataList() {
		return dataList;
	}

	public void setDataList(List<DataTrans> dataList) {
		this.dataList = dataList;
	}

}
