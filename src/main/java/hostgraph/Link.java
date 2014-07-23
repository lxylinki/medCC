package hostgraph;

import java.util.HashMap;

public class Link {
	// link id
	private int[] endHosts = {-1, -1};
	
	private int maxbandwidth = 1;
	
	private HashMap<Double, Integer> bandwidth = null;

	public Link(int maxbandwidth, boolean dynamic) {
		this.setMaxbandwidth(maxbandwidth);
		if (dynamic) {
			this.setBandwidth(new HashMap<Double, Integer>());
		}
	}

	// there is no src and dest diff in end hosts: just for convenience
	public int getSrcHostId() {
		return this.endHosts[0];
	}
	
	public int getDstHostId() {
		return this.endHosts[1];
	}
	
	public int getBandwidthAt(Double timepoint) {
		int availbw = -1;
		// if in static graph
		if (this.bandwidth == null) {
			availbw = this.maxbandwidth;
		} else {
			for (Double changetime: this.bandwidth.keySet()) {
				if (changetime <= timepoint) {
					availbw = this.bandwidth.get(changetime);
					break;
				}
			}// end for
		}// end if
		return availbw;
	}
	
	public int[] getEndHosts() {
		return endHosts;
	}

	public void setEndHosts(int src, int dest) {
		this.endHosts[0] = src;
		this.endHosts[1] = dest;
	}


	public int getMaxbandwidth() {
		return maxbandwidth;
	}


	public void setMaxbandwidth(int maxbandwidth) {
		this.maxbandwidth = maxbandwidth;
	}


	public HashMap<Double, Integer> getBandwidth() {
		return bandwidth;
	}


	public void setBandwidth(HashMap<Double, Integer> bandwidth) {
		this.bandwidth = bandwidth;
	}


}
