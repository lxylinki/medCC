package virtualnet;

import org.cloudbus.cloudsim.Vm;

public class VM extends Vm {
	
	private double price = 0;
	
	private int type = 0;
	
	// vmid differs from typeid
	public VM(int vmid, int typeid, double chargingrate, double processingpower) {
		super(vmid, 1, processingpower, 1, 10, 10, 10, "xen", null);
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

}
