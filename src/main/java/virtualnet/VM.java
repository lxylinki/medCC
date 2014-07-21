package virtualnet;

public class VM {
	private int vmId = 0;
	
	private double mips = 1;
	
	private double price = 0;
	
	private int type = 0;
	
	// vmid differs from typeid
	public VM(int vmid, int typeid, double chargingrate, double processingpower) {
		//super(vmid, 1, processingpower, 1, 10, 10, 10, "xen", null);
		setPrice(chargingrate);
		setType(typeid);
	}

	public double getProcessingPower() {
		return this.getMips();
	}
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getMips() {
		return mips;
	}

	public void setMips(double mips) {
		this.mips = mips;
	}

	public int getVmId() {
		return vmId;
	}

	public void setVmId(int vmId) {
		this.vmId = vmId;
	}

}
