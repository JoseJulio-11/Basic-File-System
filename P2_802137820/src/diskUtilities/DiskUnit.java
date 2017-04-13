package diskUtilities;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidParameterException;

import diskExceptions.ExistingDiskException;
import diskExceptions.FullDiskException;
import diskExceptions.InvalidBlockException;
import diskExceptions.InvalidBlockNumberException;
import diskExceptions.NonExistingDiskException;
import lists.LLQueue;

public class DiskUnit {
	private static final int 
	DEFAULT_CAPACITY = 256;								//default number of blocks
	private static final int 
	DEFAULT_BLOCK_SIZE = 32;							//default number of bytes per block	

	private int capacity;								//number of blocks of current disk instance
	private int blockSize;								//size of each block of current disk instance
	private RandomAccessFile disk;						//disk instance
	private static LLQueue<INode> freeINodesList;		//list that will manage the free i-nodes of the disk
	private static LLQueue<INode> takenINodesList;		//list that will manage the taken i-nodes of the disk
	private static int firstFreeBlock;					//the first free block of the disk
	private static int firstFreeBlockIndex;				//the first index of the first free block
	private static int firstFreeINodeIndex;				//pointer to the first free i-node
	private static int numberOfINodes;					//total number of i-nodes


	// the constructor -- PRIVATE
	/**
	 * @param name is the name of the disk
	 */
	private DiskUnit(String name){
		try{
			disk = new RandomAccessFile(name, "rw");
		} catch (IOException e){
			System.err.println("Unable to start the disk");
			System.exit(1);
		}
	}

	/**
	 * Writes the content of block b into the disk block corresponding to blockNum.
	 * @param blockNum is the number corresponding to the location 
	 * of the disk block that b will overwrite.
	 * @param b is the disk block to be written on.
	 * @throws InvalidBlockNumberException whenever the block 
	 * number received is not valid for the current disk instance.
	 * @throws InvalidBlockException whenever b does not represent a 
	 * valid disk block for the current disk instance.
	 */
	public void write(int blockNum, VirtualDiskBlock b) 
			throws InvalidBlockNumberException, InvalidBlockException{
		if(blockNum <= 0 || blockNum >= capacity)
			throw new InvalidBlockNumberException("Invalid block location: " + blockNum);
		if(b == null)
			throw new InvalidBlockException("Invalid block: does not match disk.");
		else if(b.getCapacity() != this.getBlockSize())
			throw new InvalidBlockException("Invalid block: does not match disk.");
		else{
			try {
				disk.seek(blockNum * b.getCapacity());				//the file-pointer is set to blockNum
				for(int i = 0; i < b.getCapacity(); i++)
					disk.write(b.getElement(i));					//write the byte array of b into the block of the RAF
			} catch (IOException e) {			
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads the given block parameter from the disk.
	 * @param blockNum is the number corresponding to the location of disk block b.
	 * @param b is the disk block to read.
	 * @throws InvalidBlockNumberException whenever the block 
	 * number received is not valid for the current disk instance.
	 * @throws InvalidBlockException whenever b does not represent a 
	 * valid disk block for the current disk instance.
	 */
	public void read(int blockNum, VirtualDiskBlock b) 
			throws InvalidBlockNumberException, InvalidBlockException{
		if(blockNum < 0 || blockNum >= capacity)
			throw new InvalidBlockNumberException("Invalid block location: " + blockNum);
		if(b == null)
			throw new InvalidBlockException("Invalid bock: does not match disk");
		else if(b.getCapacity() != this.getBlockSize())
			throw new InvalidBlockException("Invalid block: does not match disk.");
		else{
			try {
				disk.seek(blockNum * b.getCapacity());
				for(int i = 0; i < b.getCapacity(); i++)
					b.setElement(i, disk.readByte());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	/**
	 * Returns a nonnegative integer value corresponding to
	 * the number of valid blocks (unused + used) that 
	 * the current disk instance has.
	 * @return the number of valid blocks
	 */
	public int getCapacity(){
		return capacity;
	}

	/**
	 * Returns a nonnegative integer value which corresponds to
	 * the size (number of character elements) of a block
	 * in the current disk instance.
	 * @return the size of the block
	 */
	public int getBlockSize(){
		return blockSize;
	}

	/**Returns a nonnegative integer value which corresponds to
	 * the first block in the collection of free data blocks 
	 * in the current disk instance. 
	 * @return the first free block
	 */
	public int getFirstFreeBlock(){
		return firstFreeBlock;
	}

	/**
	 * Returns a nonnegative integer value which corresponds to
	 * the index of the first free block in the current disk instance.
	 * @return the index of the first free block
	 */
	public int getFirstFreeBlockIndex(){
		return firstFreeBlockIndex;
	}

	/**
	 * Returns a nonnegative integer value which corresponds to
	 * the index of the first free i-node in the list of free i-nodes
	 * in the current disk instance.
	 * @return the first free i-node
	 */
	public int getFirstFreeINodeIndex(){
		return firstFreeINodeIndex;
	}	

	/**
	 *  Returns a nonnegative integer value which corresponds to
	 *  the total number of i-nodes in the current disk instance.
	 * @return the number of i-nodes
	 */
	public int getNumOfINodes(){
		numberOfINodes = freeINodesList.size() + takenINodesList.size();
		return numberOfINodes;
	}	

	/**
	 * Returns the first free i-node index in the list of free i-nodes for file
	 * or directory assignment. Next free i-node becomes the first free i-node.
	 * @return the first free i-node in the list of free i-nodes
	 * @throws FullDiskException if pointer to next free i-node is 0.
	 */
	public int getFreeINode() throws FullDiskException{
		if(firstFreeINodeIndex == 0)
			throw new FullDiskException("Disk is full");
		int freeNodeToReturn = firstFreeINodeIndex;						//get the first free i-node index to return from free i-nodes list
		firstFreeINodeIndex = freeINodesList.front().getFirstBlock();	//next i-node in line is first free i-node
		takenINodesList.enqueue(freeINodesList.front());			//add i-node to the list of i-nodes for files and directories
		freeINodesList.dequeue();									//remove last first free i-node from list of free i-nodes

		return freeNodeToReturn;
	}

	/**
	 * Returns the reference to the most recent free block in the disk.
	 * @param disk the disk from where the free block will be returned
	 * @param blockSize the size of the blocks in the disk
	 * @return the reference to the first 4 bytes of the free block
	 */
	public int getFreeBlockNumber(RandomAccessFile disk, int blockSize){
		int bn;
		if(firstFreeBlock == 0)
			throw new FullDiskException("Disk is full");

		//disk has space
		if(firstFreeBlockIndex != 0){
			bn = firstFreeBlock;
			firstFreeBlockIndex--;
		}
		else{
			//free block to return is the root free block
			bn = firstFreeBlock;			
			try {
				disk.seek(firstFreeBlock);
				firstFreeBlock = disk.readInt();
			} catch (IOException e) {
				e.printStackTrace();
			}
			firstFreeBlockIndex = (blockSize/4) - 1;
		}
		return bn;
	}

	/**
	 * Formats the disk. This operation visits every “physical block”
	 * in the disk and fills with zeroes all those that are valid.
	 */
	public void lowLevelFormat(){
		try {
			disk.seek(blockSize);
			for(int i = blockSize; i < capacity; i++)
				disk.writeByte(0);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/** Simulates shutting-off the disk. Just closes the corresponding RAF. **/
	public void shutdown(){
		try{
			disk.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Writes a free block into the tree-like structure
	 * of free blocks in the disk.
	 * @param disk the disk that will register the blocks
	 * @param bn the reference for the block to register
	 * @param blockSize the size of each block in the disk
	 */
	public static void registerFreeBlock(RandomAccessFile disk, int bn, int blockSize) {		
		//case where bn is the first free block in the disk
		if(firstFreeBlock == 0){		
			firstFreeBlock = bn;			
			try {
				disk.seek(firstFreeBlock);
				disk.writeInt(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			firstFreeBlockIndex = 0;
		}
		//case where the current root free block directory is full
		else if(firstFreeBlockIndex == (blockSize/4) - 1){
			try {
				//writes reference to root free block directory on the bn-th free block
				disk.seek(bn);
				disk.writeInt(firstFreeBlock);
			} catch (IOException e) {
				e.printStackTrace();
			}
			firstFreeBlockIndex = 0;			
		}
		else{
			firstFreeBlockIndex++;
			try {
				disk.seek(firstFreeBlockIndex);
				disk.writeInt(bn);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Turns on an existing disk unit whose name is given. If successful, it makes
	 * the particular disk unit available for operations suitable for a disk unit.
	 * @param name the name of the disk unit to activate
	 * @return the corresponding DiskUnit object
	 * @throws NonExistingDiskException whenever no
	 *    ¨disk¨ with the specified name is found.
	 */
	public static DiskUnit mount(String name) throws NonExistingDiskException{
		File file = new File("src\\DiskUnits\\" + name);
		if (!file.exists())
			throw new NonExistingDiskException("No disk has name : " + name);

		DiskUnit dUnit = new DiskUnit("src\\DiskUnits\\" + name);	

		// get the capacity, block size, and other important data of the disk from the file
		// representing the disk
		try {
			dUnit.disk.seek(0);	
			dUnit.capacity = dUnit.disk.readInt();			// bytes 0 to 3
			dUnit.blockSize = dUnit.disk.readInt();			// bytes 4 to 7
			firstFreeBlock = dUnit.disk.readInt();			// bytes 8 to 11
			firstFreeBlockIndex = dUnit.disk.readInt();		// bytes 12 to 15
			firstFreeINodeIndex = dUnit.disk.readInt();			// bytes 16 to 19
			numberOfINodes = dUnit.disk.readInt();			// bytes 20 to 23			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dUnit;  	
	}	

	/***
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as having default capacity (number of blocks), each of default
	 * size (number of bytes). Those values are: DEFAULT_CAPACITY and
	 * DEFAULT_BLOCK_SIZE. The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @throws ExistingDiskException whenever the name attempted is
	 * already in use.
	 */
	public static void createDiskUnit(String name) throws ExistingDiskException {
		createDiskUnit(name, DEFAULT_CAPACITY, DEFAULT_BLOCK_SIZE);
	}

	/**
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as with the specified capacity (number of blocks), each of specified
	 * size (number of bytes).  The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @param capacity number of blocks in the new disk
	 * @param blockSize size per block in the new disk
	 * @throws ExistingDiskException whenever the name attempted is
	 * already in use.
	 * @throws InvalidParameterException whenever the values for capacity
	 *  or blockSize are not valid according to the specifications
	 */
	public static void createDiskUnit(String name, int capacity, int blockSize) 
			throws ExistingDiskException, InvalidParameterException{
		File file = new File(name);
		if (file.exists())
			throw new ExistingDiskException("Disk name is already used: " + name);

		RandomAccessFile disk = null;
		if (capacity < 0 || blockSize < 32 ||
				!Utils.powerOf2(capacity) || !Utils.powerOf2(blockSize))
			throw new InvalidParameterException("Invalid values: " +
					" capacity = " + capacity + " block size = " +
					blockSize);

		// disk parameters are valid... hence create the file to represent the
		// disk unit.
		try {
			disk = new RandomAccessFile("src\\DiskUnits\\" + name, "rw");
		}
		catch (IOException e) {
			System.err.println ("Unable to start the disk");
			System.exit(1);
		}

		reserveDiskSpace(disk, capacity, blockSize);

		// after creation, just leave it in shutdown mode - just
		// close the corresponding file
		try {
			disk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reserves the desired space of memory for the disk.
	 * @param disk which will have the memory reserved for.
	 * @param capacity of the whole disk unit
	 * @param blockSize the size of each disk block
	 */
	private static void reserveDiskSpace(RandomAccessFile disk, int capacity, int blockSize){
		try {
			disk.setLength(blockSize * capacity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Initializing list and variables
		firstFreeBlock = 0;
		firstFreeINodeIndex = blockSize;		
		//		// Creating i-node 0 for root directory
		//		INode node = new INode(0);

		// reserving space and registering i-nodes 
		reserveINodeSpace(disk, capacity, blockSize);	

		int blocksToReserve = (int) Math.floor(capacity * 0.01);

		// registering free blocks in the disk
		for(int i = blocksToReserve + 1; i <= capacity - 1; i++)
			registerFreeBlock(disk, i, blockSize);	

		// write disk parameters (number of blocks, bytes per block) in
		// block 0 of disk space
		try {
			disk.seek(0);
			disk.writeInt(capacity);  					// bytes 0 to 3
			disk.writeInt(blockSize); 					// bytes 4 to 7
			disk.writeInt(firstFreeBlock); 				// bytes 8 to 11
			disk.writeInt(firstFreeBlockIndex);			// bytes 12 to 15
			disk.writeInt(firstFreeINodeIndex);				// bytes 16 to 19
			disk.writeInt(freeINodesList.size()); 		// bytes 20 to 23			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reserves and writes the space needed for i-nodes in the disk
	 * @param disk the current disk to write the i-nodes
	 * @param capacity the capacity of the disk
	 * @param blockSize the size of each block of the disk
	 * @return a list containing all the free i-nodes that were reserved
	 */
	private static void reserveINodeSpace(RandomAccessFile disk, int capacity, int blockSize){
		//FORMULAS NEEDED TO RESERVE AND ASSIGN I-NODE SPACE
		int blocksToReserve = (int) Math.floor(capacity * 0.01);
		int totalOfINodes = (blockSize * blocksToReserve) / 9;
		int iNodesPerBlock = blockSize / 9;

		//CONTROL VARIABLES
		int iNodesCreated = 0;
		int unusedBytesOnBlock = blockSize - (iNodesPerBlock * 9);
		int cursor = blockSize;

		takenINodesList = new LLQueue<>();
		freeINodesList = new LLQueue<>();	

		try{
			//this part of the code goes through every block and writes the free i-nodes
			for(int i = 0; i < blocksToReserve; i++){

				//each iteration sets the cursor to the beginning of the next block
				disk.seek(cursor = blockSize + (blockSize * i)); 	

				//writes the amount of i-nodes that the block can hold
				for(int j = 0; j <= iNodesPerBlock; j++){
					//if the total number of i-nodes has been created then create i-node 0
					if(iNodesCreated == 1){
						INode node = new INode(0);
						node.setType((byte)1);
						disk.seek(cursor + 8);
						disk.writeByte(1);
						takenINodesList.enqueue(node);						
					}
					if(iNodesCreated == totalOfINodes){
						break;
					}
					//check if the node to be created on this iteration is the last node on the block 
					if(cursor + 9 == (blockSize + blockSize * i+1) - unusedBytesOnBlock){
						INode node = new INode(cursor += 9 + unusedBytesOnBlock);

						//write the pointer to the next free i-node in the physical block
						disk.writeInt(node.getFirstBlock());				

						//add the i-node to a collection for management later on
						freeINodesList.enqueue(node);						

					}
					else{
						//create a node with a pointer to the next free i-node
						INode node = new INode(cursor += 9);

						//write the pointer to the next free i-node in the physical block
						disk.writeInt(node.getFirstBlock());				

						//add the i-node to a collection for management later on
						freeINodesList.enqueue(node);

						//update the amount of nodes created
						iNodesCreated++;

					}				
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}	
}
