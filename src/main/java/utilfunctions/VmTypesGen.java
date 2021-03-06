package utilfunctions;

import java.util.ArrayList;
import java.util.List;

import virtualnet.VMtype;

public class VmTypesGen {

	// unit processing power in a VM i.e. the proc power of one vcpu
	public static int basemips = 1;
	
	// charging rate of one proc unit
	private static double baseprice = 0.001;
	
	// list of types increasing in procpower and price
	public static List<VMtype> vmTypeList(int numOfTypes) {
		List <VMtype> types = new ArrayList<VMtype>();
		
		for (int i=0; i<numOfTypes; i++) {
			// typeid is the index in typelist
			VMtype newtype;
			
			//VMtype newtype = new VMtype(i, (int) (Math.pow(2, i)), basemips, baseprice);
			if ( i == 0) {
				newtype = new VMtype(i, 1, basemips, baseprice);
			} else {
				newtype = new VMtype(i, 2*i, basemips, baseprice);
				//newtype = new VMtype(i, (int) (Math.pow(2, i)), basemips, baseprice);
			}
			/**
			System.out.printf("VM type %d: num of cores %d, maxprocpower %.2f, charging rate %.2f\n", 
					newtype.getTypeid(), newtype.getCorenum(), newtype.getMaxpower(), newtype.getPrice());
					*/
			types.add(newtype);                                                                                                                         
		}
		return types;
	}
	
	// list of types increasing in procpower and price
	// Only for tmp usage in exp calculation
	public static List<VMtype> vmTypeList(int basemips, double baseprice, int numOfTypes) {
		List <VMtype> types = new ArrayList<VMtype>();
		
		for (int i=0; i<numOfTypes; i++) {
			// typeid is the index in typelist
			VMtype newtype;
			
			//VMtype newtype = new VMtype(i, (int) (Math.pow(2, i)), basemips, baseprice);
			if ( i == 0) {
				newtype = new VMtype(i, 1, basemips, baseprice);
			} else {
				//newtype = new VMtype(i, 2*i, basemips, baseprice);
				newtype = new VMtype(i, (int) (Math.pow(2, i+1)), basemips, baseprice);
			}
			
			System.out.printf("VM type %d: num of cores %d, maxprocpower %.2f, charging rate %.2f\n", 
					newtype.getTypeid(), newtype.getCorenum(), newtype.getMaxpower(), newtype.getPrice());
					
			types.add(newtype);                                                                                                                         
		}
		return types;
	}
	
	// get a type by its id
	public static VMtype getType(int typeid, List<VMtype> alltypes) {
		for (VMtype type: alltypes) {
			if (type.getTypeid() == typeid ) {
				return type;
			}
		}
		return null;		
	}
}
