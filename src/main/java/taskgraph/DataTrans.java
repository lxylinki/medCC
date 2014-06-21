package taskgraph;

import org.cloudbus.cloudsim.Cloudlet;

public class DataTrans extends Cloudlet {
	
	private String role = "Data";
	
	// structure related
	private int srcModId = -1;
	
	private int dstModId = -1;
	
	// exec time and cost
	private double cost = 0;
	
	private double time = 0;
	
	// CP related
	private boolean critical = false;
		
	private int layer = 0;
		
	private double est = 0;
		
	private double eft = 0;
		
	private double lst = 0;
		
	private double lft = 0;
	
	// longest path len to exist mod
	private double blevel = 0;
		
	// longest path len from entry mod
	private double tlevel = 0;
	

	public DataTrans(int dataId, long datasize) {
		super(dataId, datasize, 1, 0, 0, null, null,null);
	}

	public long getDatasize() {
		return this.getCloudletLength();
	}

	public int getSrcModId() {
		return srcModId;
	}

	public void setSrcModId(int srcModId) {
		this.srcModId = srcModId;
	}

	public int getDstModId() {
		return dstModId;
	}

	public void setDstModId(int dstModId) {
		this.dstModId = dstModId;
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

	public int getLayer() {
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

}
