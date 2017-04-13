package diskUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DiskManager {

	private ArrayList<String> disks;
	private RandomAccessFile diskNames;
	
	
	/**
	 * Creates a manager that will handle the names
	 * of the disks that get created.
	 */
	public DiskManager(){		
		try {
			diskNames = new RandomAccessFile("src\\DiskNames", "rw");
			disks = new ArrayList<String>();

			if(new File("DiskNames").exists()){
				diskNames.seek(0);
				while(diskNames.getFilePointer() < diskNames.length())
					disks.add(diskNames.readLine());
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return an array of strings corresponding to 
	 * the names of the disks created.
	 * @return
	 */
	public ArrayList<String> getDiskNames(){		
		return disks;
	}

	/**
	 * Add the name of a disk to the list of disk names.
	 * @param diskName the name of the disk to be added.
	 */
	public void addDisk(String diskName){
		disks.add(diskName);
		try {			
			diskNames.writeBytes(diskName + "\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a disk from the directory of disks called "DiskUnits".
	 * After that, the name is removed from the array of disk names.
	 * @param diskName the name of the disk to be deleted.
	 */
	public void deleteDisk(String diskName){
		//searches for the disk name
		int index = findDiskNameIndex(diskName);
		disks.remove(index);

		File dtr = new File("src\\DiskUnits\\" + diskName);
		dtr.delete();

		try {
			diskNames.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File("DiskNames");
		file.delete();
		try {
			diskNames = new RandomAccessFile("DiskNames", "rw");
			for(String dName: disks)				
				diskNames.writeBytes(dName + "\n");				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search for the name of a disk in the array of disk names.
	 * @param diskName
	 * @return the index of the position where the disk name is,
	 * returns -1 if it doesn't exist.
	 */
	public int findDiskNameIndex(String diskName){
		for(int i = 0; i < disks.size(); i++){
			if(disks.get(i).equals(diskName))
				return i;
		}
		return -1;
	}

}
