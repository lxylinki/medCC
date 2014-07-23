package virtualnet;

public class VMtype {
	private final int typeid;
	
	// number of cores
	private int corenum = 0;
	
	// processing power per core
	private double unitprocpower = 0;
	
	// total price per time unit
	private double price = 0;
	
	// max procpower that can be utilized
	private double maxpower = 0;

	public VMtype(final int typeid, int corenum, double unitprocpower, double unitprice) {
		this.typeid = typeid;
		setCorenum(corenum);
		setUnitprocpower(unitprocpower);
		
		// linear to num of cores
		setPrice(unitprice*corenum);
		setMaxpower(unitprocpower*corenum);
	}

	public int getCorenum() {
		return corenum;
	}

	public void setCorenum(int corenum) {
		this.corenum = corenum;
	}

	public double getUnitprocpower() {
		return unitprocpower;
	}

	public void setUnitprocpower(double unitprocpower) {
		this.unitprocpower = unitprocpower;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getMaxpower() {
		return maxpower;
	}

	public void setMaxpower(double maxpower) {
		this.maxpower = maxpower;
	}

	public int getTypeid() {
		return typeid;
	}
	
}
