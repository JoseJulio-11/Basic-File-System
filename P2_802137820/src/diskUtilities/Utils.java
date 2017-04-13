package diskUtilities;

public class Utils {
	
	/**
	 * Returns true if the input number is a 
	 * power of 2, and false otherwise.
	 * @param n the input number
	 * @return true or false
	 */
	public static boolean powerOf2(int n){
		if(n > 0 && (n & (n - 1)) == 0)		//checks that the number is positive 
											//and that the binary representation of n bitwise AND n-1 equals zero. 
			return true;
		else
			return false;
	}
	
	public static void copyNextBNToBlock(VirtualDiskBlock vdb, int value) { 
		int lastPos = vdb.getCapacity()-1;

		for (int index = 0; index < 4; index++) { 
			vdb.setElement(lastPos - index, (byte) (value & 0x000000ff)); 	
			value = value >> 8; 
		}

	}

}
