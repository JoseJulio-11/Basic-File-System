package theSystem;

import java.io.File;
import java.util.ArrayList;

import diskExceptions.NonExistingDiskException;
import diskUtilities.DiskManager;
import diskUtilities.DiskUnit;
import diskUtilities.VirtualDiskBlock;
import systemGeneralClasses.Command;
import systemGeneralClasses.CommandActionHandler;
import systemGeneralClasses.CommandProcessor;
import systemGeneralClasses.FixedLengthCommand;
import systemGeneralClasses.SystemCommand;
import stack.IntStack;


/**
 * 
 * @author Pedro I. Rivera-Vega
 *
 */
public class SystemCommandsProcessor extends CommandProcessor { 


	//NOTE: The HelpProcessor is inherited...

	// To initially place all lines for the output produced after a 
	// command is entered. The results depend on the particular command. 
	private ArrayList<String> resultsList; 

	SystemCommand attemptedSC; 
	// The system command that looks like the one the user is
	// trying to execute. 

	boolean stopExecution; 
	// This field is false whenever the system is in execution
	// Is set to true when in the "administrator" state the command
	// "shutdown" is given to the system.

	////////////////////////////////////////////////////////////////
	// The following are references to objects needed for management 
	// of data as required by the particular actions of the command-set..
	// The following represents the object that will be capable of
	// managing the different lists that are created by the system
	// to be implemented as a lab exercise. 
	private DiskManager diskManager = new DiskManager();
	private DiskUnit mountedDisk = null;
	private String mountedDiskName = "null";


	/**
	 *  Initializes the list of possible commands for each of the
	 *  states the system can be in. 
	 */
	public SystemCommandsProcessor() {

		// stack of states
		currentState = new IntStack(); 

		// The system may need to manage different states. For the moment, we
		// just assume one state: the general state. The top of the stack
		// "currentState" will always be the current state the system is at...
		currentState.push(GENERALSTATE); 

		// Maximum number of states for the moment is assumed to be 1
		// this may change depending on the types of commands the system
		// accepts in other instances...... 
		createCommandList(1);    // only 1 state -- GENERALSTATE

		// commands for the state GENERALSTATE	

		add(GENERALSTATE, SystemCommand.getFLSC("createdisk name int int", new CreateDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("deletedisk name", new DeleteDiskProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("mount name", new MountProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("unmount ", new UnmountProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("showdisks", new ShowDisksProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("loadfile name name", new LoadFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cp name name", new CopyFileProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("ls", new ShowDirectoryProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("cat name", new ShowFileContentProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("dparams", new DiskParametersProcessor()));
		add(GENERALSTATE, SystemCommand.getFLSC("exit", new ShutDownProcessor())); 
		add(GENERALSTATE, SystemCommand.getFLSC("help", new HelpProcessor())); 

		// need to follow this pattern to add a SystemCommand for each
		// command that has been specified...
		// ...

		// set to execute....
		stopExecution = false; 

	}

	/*
	 * Returns list of strings that create the results.
	 */
	public ArrayList<String> getResultsList() { 
		return resultsList; 
	}

	// INNER CLASSES -- ONE FOR EACH VALID COMMAND --
	/**
	 *  The following are inner classes. Notice that there is one such class
	 *  for each command. The idea is that enclose the implementation of each
	 *  command in a particular unique place. Notice that, for each command, 
	 *  what you need is to implement the internal method "execute(Command c)".
	 *  In each particular case, your implementation assumes that the command
	 *  received as parameter is of the type corresponding to the particular
	 *  inner class. For example, the command received by the "execute(...)" 
	 *  method inside the "LoginProcessor" class must be a "login" command. 
	 *
	 */
	private class ShutDownProcessor implements CommandActionHandler { 
		public ArrayList<String> execute(Command c) { 

			resultsList = new ArrayList<String>(); 
			resultsList.add("SYSTEM IS SHUTTING DOWN!!!!");
			stopExecution = true;
			return resultsList; 
		}
	}

	/**
	 * Calls the method of class DiskUnit that creates the disk.
	 * @author josej
	 *
	 */
	private class CreateDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;			

			String name = fc.getOperand(1);
			int nBlocks = Integer.parseInt(fc.getOperand(2));
			int bSize = Integer.parseInt(fc.getOperand(3));			

			if(diskManager.findDiskNameIndex(name) != -1){
				resultsList.add("Error: A disk already exists with that name.");
			}
			else{
				if(nBlocks < 256)
					resultsList.add("Error: Disk capacity can't be less than 256 bytes.");
				if(bSize < 32)
					resultsList.add("Error: Block size can't be less than 32 bytes.");
				else
					DiskUnit.createDiskUnit(name, nBlocks, bSize);				
				diskManager.addDisk(name);
			}
			return resultsList;
		}

	}

	/**
	 * Deletes the disk that has the given name. If disk is mounted
	 * it needs to be unmounted first.
	 * @author josej
	 *
	 */
	private class DeleteDiskProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;

			String name = fc.getOperand(1);

			if(mountedDisk == null)
				resultsList.add("No disk mounted.");
			else if(diskManager.findDiskNameIndex(name) == -1)
				resultsList.add("Error: Disk with that name doesn't exist.");
			else if(mountedDiskName.equals(name))
				resultsList.add("Error: Disk is mounted. To delete unmount first.");
			else{	
				diskManager.deleteDisk(name);
				resultsList.add("Deleted successfully.");
			}
			return resultsList;
		}

	}

	/**
	 * Mounts the disk with the given name.
	 * @author josej
	 *
	 */
	private class MountProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;

			String name = fc.getOperand(1);

			if(diskManager.findDiskNameIndex(name) == -1)
				resultsList.add("Error: Disk with that name doesn't exist.");
			else if(mountedDiskName.equals(name))
				resultsList.add("Error: Disk already mounted.");
			else{
				try {
					mountedDisk = DiskUnit.mount(name);
					mountedDiskName = name;
					resultsList.add("Mounted succesfully.");
				} catch (NonExistingDiskException e) {
					e.printStackTrace();
				}
			}
			return resultsList;
		}
	}

	/**
	 * Unmounts the disk that is currently mounted.
	 * Can't unmount if there is no disk mounted.
	 * @author josej
	 *
	 */
	private class UnmountProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();			
			if(mountedDisk == null)
				resultsList.add("Error: No disk mounted.");
			else{
				mountedDisk.shutdown();
				mountedDisk = null;
				mountedDiskName = "null";
				resultsList.add("Unmounted successfully");
			}
			return resultsList;
		}

	}

	/**
	 * Shows the list of disks that are currently available.
	 * @author josej
	 *
	 */
	private class ShowDisksProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();

			ArrayList<String> disks = diskManager.getDiskNames(); 
			if(disks.isEmpty())
				resultsList.add("No disks to show.");
			else{
				resultsList.add("Existing disks are: ");

				for(int i = 0; i < disks.size(); i++)			
					resultsList.add(diskManager.getDiskNames().get(i));	
			}

			return resultsList;
		}

	}

	/**
	 * Attempts to read a new file into the current directory
	 * in the current working disk unit.
	 * @author josej
	 *
	 */
	private class LoadFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;

			String fileName = fc.getOperand(1);
			String newFileName = fc.getOperand(2);

			if(mountedDisk == null){
				resultsList.add("No disk mounted.");
				return resultsList;
			}			
			int freeINode = mountedDisk.getFreeINode();
			int bs = mountedDisk.getBlockSize();
			VirtualDiskBlock vdb = new VirtualDiskBlock(bs);		

			File ext_file = new File("src\\" + fileName);			

			if(!ext_file.exists())
				resultsList.add("Error: Such file does not exist in current directory.");
			else if(mountedDisk == null){
				resultsList.add("Error: No disk mounted.");
			}
			else if(freeINode == 0){
				resultsList.add("Error: Disk is full.");
			}
			else{
			}
			return resultsList;
		}

	}

	/**
	 * Copies on internal file to another internal file.
	 * @author josej
	 *
	 */
	private class CopyFileProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;


			return resultsList;
		}

	}

	/**
	 * List the names and sizes of all the files 
	 * and directories that are part of the current directory.
	 * @author josej
	 *
	 */
	private class ShowDirectoryProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();


			return resultsList;
		}

	}

	/**
	 * Displays the content of the given internal file.
	 * @author josej
	 *
	 */
	private class ShowFileContentProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();
			FixedLengthCommand fc = (FixedLengthCommand) c;


			return resultsList;
		}

	}	

	/**
	 * Displays important information of the disk.
	 * @author josej
	 *
	 */
	private class DiskParametersProcessor implements CommandActionHandler {
		public ArrayList<String> execute(Command c) {
			resultsList = new ArrayList<String>();

			resultsList.add("Metadata on disk: " + mountedDisk);
			resultsList.add("Number of blocks: " + mountedDisk.getCapacity());
			resultsList.add("Blocks size: " + mountedDisk.getBlockSize());
			resultsList.add("Free blocks root: " + mountedDisk.getFirstFreeBlock());
			resultsList.add("Index at free block root: " + mountedDisk.getFirstFreeBlockIndex());
			resultsList.add("First free i-node starts at byte: " + mountedDisk.getFirstFreeINodeIndex());
			resultsList.add("Number of i-nodes: " + mountedDisk.getNumOfINodes());
			resultsList.add("");

			return resultsList;
		}
	}

	/**
	 * 
	 * @return true if in shutdown mode, false otherwise.
	 */
	public boolean inShutdownMode() {
		return stopExecution;
	}

}