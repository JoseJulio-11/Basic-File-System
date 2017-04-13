package diskUtilities;

import diskExceptions.InvalidBlockNumberException;

public class VirtualDiskBlock {
	private static final int DEFAULT_BLOCK_SIZE = 256;
	private byte[] data;
	
	/**
	 * Creates a block of size equal to 256 bytes.
	 */
	public VirtualDiskBlock(){
		data = new byte[DEFAULT_BLOCK_SIZE];
	}
	
	/**
	 * Creates a block of size(number of bytes) equal to blockCapacity.
	 * @param blockCapacity the desired size of the array.
	 */
	public VirtualDiskBlock(int blockCapacity) throws InvalidBlockNumberException{
		if(blockCapacity < 32)
			throw new InvalidBlockNumberException("Invalid capacity: " + blockCapacity);
		data = new byte[blockCapacity];
	}
	
	/**
	 * Returns a positive integer value that corresponds
	 * to the capacity of the current instance block.
	 * @return the capacity of the current instance block.
	 */
	public int getCapacity(){
		return data.length;
	}
	
	/**
	 * Changes the contents of element at position index
	 * to that of nuevo in the current block instance
	 * @param index the current block instance
	 * @param nuevo the new content for the element.
	 */
	public void setElement(int index, byte nuevo){
		data[index] = nuevo;
	}
	
	/**
	 * Returns a copy of the character at the position index
	 * in the current block instance. 
	 * @param index the current block instance
	 * @return a copy of the character
	 */
	public byte getElement(int index){
		return data[index];		
	}
	
}
