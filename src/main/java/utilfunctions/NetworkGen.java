package utilfunctions;

/** generate network graph (snapshot) based on 
 *  1.full link bandwidth
 *  2.max host cpu, ram, disk
 *  
 *  All graphs are complete since low available resources <=> component down.
 * @author Linki
 *
 */
public class NetworkGen {
	// all use 1 as the base
	public static int minbandwidth = 1;
	
	public static int mincpu = 1;
	
	public static int minram = 1;
	
	public static int mindisk = 1;
	
	// uniform full capacities, let avg be max/2
	public static int maxbandwidth = 100;
	
	public static int maxcpu = 100;
	
	public static int maxram = 100;
	
	public static int maxdisk = 100;
	

	
	// a static network graph with each component has fixed resouce value
	public static void staticNetworkGen(int numOfHosts) {
		
	}
	
	// generate availability table for cpu/ram/disk/link
	public static void timeSeriesGen(long maxtime, int maxvalue) {
		
	}

	/**
	 * 
	 * @param numOfHosts
	 */
	public static void dynamicNetworkGen(int numOfHosts) {
		// TODO Auto-generated constructor stub
	} 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
