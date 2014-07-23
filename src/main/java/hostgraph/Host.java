package hostgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Host {
	private final int hostId;
	
	private int maxprocpower;
	
	private int maxram;
	
	private int maxdisk;

	private List<Host> neighbors = null;
	
	private List<Link> links = null;
	
	// parameters for reproducible resource availablity tables:
	// range for generating timepoints, set according to workflow: ensure it can be finished
	private long upTime = -1;
	
	// <key: timepoints, value: resources>
	private HashMap<Double, Integer> procpower = null;
	
	private HashMap<Double, Integer> ram = null;
	
	private HashMap<Double, Integer> disk = null;

	// constructor: full resources
	public Host(final int hostId, int maxprocpower, int maxram, int maxdisk, boolean dynamic) {
		this.hostId = hostId;
		this.setMaxprocpower(maxprocpower);
		this.setMaxram(maxram);
		this.setMaxdisk(maxdisk);	
		this.setNeighbors(new ArrayList<Host>());
		
		// if in dynamic graph
		if (dynamic) {
			// init maps
			this.procpower = new HashMap<Double, Integer>();
			this.ram = new HashMap<Double, Integer>();
			this.disk = new HashMap<Double, Integer>();
		}
	}
	
	public int getProcPowerAt(Double timepoint) {
		int availpp = -1;
		// if static graph
		if (procpower == null) {
			availpp = this.maxprocpower;
		} else {
			for (Double changepoint: procpower.keySet()) {
				if (changepoint <= timepoint) {
					availpp = procpower.get(changepoint);
					break;
				}
			}
		}// end if
		return availpp;
	}
	

	public int getMaxprocpower() {
		return maxprocpower;
	}


	public void setMaxprocpower(int maxprocpower) {
		this.maxprocpower = maxprocpower;
	}


	public int getHostId() {
		return hostId;
	}


	public int getMaxram() {
		return maxram;
	}


	public void setMaxram(int maxram) {
		this.maxram = maxram;
	}


	public int getMaxdisk() {
		return maxdisk;
	}


	public void setMaxdisk(int maxdisk) {
		this.maxdisk = maxdisk;
	}

	public long getUpTime() {
		return upTime;
	}

	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}

	public HashMap<Double, Integer> getRam() {
		return ram;
	}

	public void setRam(HashMap<Double, Integer> ram) {
		this.ram = ram;
	}

	public HashMap<Double, Integer> getDisk() {
		return disk;
	}

	public void setDisk(HashMap<Double, Integer> disk) {
		this.disk = disk;
	}

	public List<Host> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(List<Host> neighbors) {
		this.neighbors = neighbors;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
}
