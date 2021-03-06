package taskgraph;

public class DataTrans {
	// the ids of its end mods
	private int[] endMods = {-1, -1};
	
	private long dataSize = 1;
	
	// trans time and cost
	private double cost = 0;
	
	private double time = 0;
	
	// CP related
	private boolean critical = false;
		
	private double layer = 0;
		
	private double est = 0;
		
	private double eft = 0;
		
	private double lst = 0;
		
	private double lft = 0;
	
	// longest path len to exist mod
	private double blevel = 0;
		
	// longest path len from entry mod
	private double tlevel = 0;
	
	// construct only by size
	public DataTrans(long datasize) {
		this.setDataSize(datasize);
		// default as datasize
		this.setTime(datasize);
	}

	public long getDatasize() {
		return dataSize;
	}

	public int getSrcModId() {
		return this.endMods[0];
	}

	public void setSrcModId(int srcModId) {
		this.endMods[0] = srcModId;
	}

	public int getDstModId() {
		return this.endMods[1];
	}

	public void setDstModId(int dstModId) {
		this.endMods[1] = dstModId;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}


	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public double getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public double getEst() {
		return est;
	}

	public void setEst(double est) {
		this.est = est;
	}

	public double getEft() {
		return eft;
	}

	public void setEft(double eft) {
		this.eft = eft;
	}

	public double getLst() {
		return lst;
	}

	public void setLst(double lst) {
		this.lst = lst;
	}

	public double getLft() {
		return lft;
	}

	public void setLft(double lft) {
		this.lft = lft;
	}

	public double getBlevel() {
		return blevel;
	}

	public void setBlevel(double blevel) {
		this.blevel = blevel;
	}

	public double getTlevel() {
		return tlevel;
	}

	public void setTlevel(double tlevel) {
		this.tlevel = tlevel;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	public int[] getEndMods() {
		return endMods;
	}

	public void setEndMods(int[] endMods) {
		this.endMods = endMods;
	}

}
