package virtualnet;

public class VM {
	private final int vmId;
	
	private VMtype vmtype = null;
	
	private int currentHostId = -1;
	
	// vmid differs from typeid
	public VM(final int vmid, VMtype vmtype) {
		this.vmId = vmid;
		this.vmtype = vmtype;
	}

	// VM Id
	public int getVmId() {
		return vmId;
	}
	
	// type Id
	public int getTypeID() {
		return this.vmtype.getTypeid();
	}
	
	// id of current host
	public int getCurrentHostId() {
		return currentHostId;
	}

	public void setCurrentHostId(int currentHostId) {
		this.currentHostId = currentHostId;
	}

}
