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
	
	private int forvisited = 0;
	
	private int backvisited = 0;
	
	// longest path len to exist mod
	private double blevel = 0;
	
	// longest path len from entry mod
	private double tlevel = 0;
	
	// parallel factor
	private double alpha = 1.0;
	
	// sched related
	private int vmtypeid = -1;
	
	private VMtype vmtype = null;
	
	private VM vm = null;
	
	private int rescheduled = 0;
	
	// execution info on all types
	private HashMap<VMtype, execInfo> mappings = null;
	
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
	public double execTime( VMtype vmtype )
	{
		long workload = this.getWorkload(); 		
		// count alpha factor
		double alpha = this.getAlpha();
		int vcpu = vmtype.getCorenum();
		double unitpp = vmtype.getUnitprocpower();		
		double exectime = ( workload / unitpp );
		
		if (vcpu > 1) {
			exectime = ( exectime * alpha ) / vcpu + exectime * ( 1 - alpha );
		}				
		return exectime;
	}
	
	// the execution information on a vm type
	public static class execInfo  {
		int typeid;
		double execTime;		
		double execCost;
		
		execInfo(int vmtypeid, double time, double cost) {
			typeid = vmtypeid;
			execTime = time;
			execCost = cost;
		}
	}
	
	public void initTime() {
		this.setEst(0);
		this.setEft(0);
		this.setLst(0);
		this.setLft(0);
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

	public void setVmtype(VMtype vmtype) {
		this.vmtype = vmtype;
		this.setVmtypeid(vmtype.getTypeid());
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

	public HashMap<VMtype, execInfo> getMappings() {
		return mappings;
	}

	public void setMappings(HashMap<VMtype, execInfo> mappings) {
		this.mappings = mappings;
	}

}