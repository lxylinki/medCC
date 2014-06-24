package taskgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

import virtualnet.VM;
import virtualnet.VMtype;

public class Module extends Cloudlet {
	
	// DAG structural parameters 
	private String role = "Module";
	
	private List<Module> preMods = null; // preceding modules 
	
	private List<Module> sucMods = null; // succeeding modules
	
	private List<DataTrans> dataIn = null;	// incoming data transfers
	
	private List<DataTrans> dataOut = null; // outgoing data transfers
	
	// resource requirement
	private int wcpu = 1;
	
	private int wram = 1;
	
	private int wdisk = 1;
	
	//current exec time and cost
	private double time = 0;
	
	private double cost = 0;
		
	// CP related
	private boolean critical = false;
	
	private int layer = 0;
	
	private double est = 0;
	
	private double eft = 0;
	
	private double lst = 0;
	
	private double lft = 0;
	
	private double buffertime = 0;
	
	private int forvisited = 0;
	
	private int backvisited = 0;
	
	// longest path len to exist mod
	private double blevel = 0;
	
	// longest path len from entry mod
	private double tlevel = 0;
	
	// parallel factor
	private double alpha = 0.85;
	
	// sched related
	private int vmtypeid = -1;
	
	private VMtype vmtype = null;
	
	private VM vm = null;
	
	private int rescheduled = 0;
	
	// execution info on all types
	private HashMap<VMtype, execInfo> profiles = null;
	
	/**
	 * @param cloudletId
	 * @param cloudletLength: workload
	 */
	
	public Module(int modId, long workload) {
		
		super(modId, workload, 1, 0, 0, null, null, null);		

		setPreMods(new ArrayList<Module>());
		setSucMods(new ArrayList<Module>());
		
		// exec by procpower 1
		setTime(getWorkload());
		setMappings(new HashMap<VMtype, execInfo>());
	}
	
	// execution time 
	private double execTime( VMtype vmtype )
	{
		double workload = this.getWorkload();
		
		if (vmtype.getCorenum() > 1) {
			return ( workload*this.getAlpha()/vmtype.getMaxpower() + 
					workload*(1-this.getAlpha())/vmtype.getUnitprocpower());
		} else {
			return ( workload/vmtype.getUnitprocpower() );
		}
	}
	
	// the execution information on a vm type
	private static class execInfo  {
		int typeid;
		double execTime;		
		double execCost;
		
		execInfo(int vmtypeid, double time, double cost) {
			typeid = vmtypeid;
			execTime = time;
			execCost = cost;
		}
	}
	
	// profile execution info on each type
	public void profiling(VMtype type) {
		int typeid = type.getTypeid();
		double exectime = this.execTime(type);
		double execcost = type.getPrice()*(Math.ceil(exectime));
		// execInfo
		execInfo info = new execInfo(typeid, exectime, execcost);
		this.profiles.put(type, info);
	}
	
	// print mapping contents
	public void printProfiles() {
		if (this.profiles.isEmpty()) {
			System.out.println("This mod has no profile info yet.\n");
		}
		for (VMtype type: this.profiles.keySet()) {
			int id = this.profiles.get(type).typeid;
			double time = this.profiles.get(type).execTime;
			double cost = this.profiles.get(type).execCost;
			System.out.printf( "mod %d exec on vmtype %d: time=%.2f, cost=%.2f\n", 
					this.getModId(), id, time, cost);
		}
	}
	
	public void initTime() {
		this.setEst(0);
		this.setEft(0);
		this.setLst(0);
		this.setLft(0);
		this.setBuffertime(0);
	}
	
	// lookup in the profile map
	public double getCostOn(VMtype type) {
		double cost = this.profiles.get(type).execCost;
		return cost;
	}
	
	// lookup in the profile map
	public double getTimeOn(VMtype type) {
		double time = this.profiles.get(type).execTime;
		return time;
	}
	
	
	public void addSucMod(Module mod) {
		this.sucMods.add(mod);
	}
	
	public void addPreMod(Module mod) {
		this.preMods.add(mod);
	}

	public int getModId() {
		return this.getCloudletId();
	}
	
	public long getWorkload() {
		return this.getCloudletLength();
	}
	
	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public int getWcpu() {
		return wcpu;
	}

	public void setWcpu(int wcpu) {
		this.wcpu = wcpu;
	}

	public int getWram() {
		return wram;
	}

	public void setWram(int wram) {
		this.wram = wram;
	}

	public int getWdisk() {
		return wdisk;
	}

	public void setWdisk(int wdisk) {
		this.wdisk = wdisk;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
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

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public VM getVm() {
		return vm;
	}

	public void setVm(VM vm) {
		this.vm = vm;
	}

	public int getIndegree() {
		return this.getPreMods().size();
	}

	public int getOutdegree() {
		return this.getSucMods().size();
	}

	public List<Module> getPreMods() {
		return preMods;
	}

	public void setPreMods(List<Module> preMods) {
		this.preMods = preMods;
	}

	public List<Module> getSucMods() {
		return sucMods;
	}

	public void setSucMods(List<Module> sucMods) {
		this.sucMods = sucMods;
	}

	public List<DataTrans> getDataIn() {
		return dataIn;
	}

	public void setDataIn(List<DataTrans> dataIn) {
		this.dataIn = dataIn;
	}

	public List<DataTrans> getDataOut() {
		return dataOut;
	}

	public void setDataOut(List<DataTrans> dataOut) {
		this.dataOut = dataOut;
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

	public int getForvisited() {
		return forvisited;
	}

	public void setForvisited(int forvisited) {
		this.forvisited = forvisited;
	}

	public int getBackvisited() {
		return backvisited;
	}

	public void setBackvisited(int backvisited) {
		this.backvisited = backvisited;
	}

	public VMtype getVmtype() {
		return vmtype;
	}

	// actually map to a type
	public void setVmtype(VMtype vmtype) {
		if (vmtype != null) {
			this.vmtype = vmtype;
			this.setVmtypeid(vmtype.getTypeid());
			this.setTime(this.profiles.get(vmtype).execTime);
			this.setCost(this.profiles.get(vmtype).execCost);
		} else {
			this.vmtype = null;
			this.setVmtypeid(-1);
			this.setTime(0);
			this.setCost(0);
		}

	}

	public int getVmtypeid() {
		return vmtypeid;
	}

	public void setVmtypeid(int vmtypeid) {
		this.vmtypeid = vmtypeid;
	}

	public int getRescheduled() {
		return rescheduled;
	}

	public void setRescheduled(int rescheduled) {
		this.rescheduled = rescheduled;
	}

	public HashMap<VMtype, execInfo> getProfiles() {
		return profiles;
	}

	public void setMappings(HashMap<VMtype, execInfo> profiles) {
		this.profiles = profiles;
	}

	public double getBuffertime() {
		return buffertime;
	}

	public void setBuffertime(double buffertime) {
		this.buffertime = buffertime;
	}

}
