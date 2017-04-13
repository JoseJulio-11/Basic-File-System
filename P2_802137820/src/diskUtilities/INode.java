package diskUtilities;


public class INode {
	private byte[] iNode;			//the i-node array
	private static final int
	INODE_SIZE = 9;					//number of bytes per i-node	
	
	//constructor made specially for the creation of i-nodes
	public INode(int nextINode){
		iNode = new byte[INODE_SIZE];
		DiskUtils.copyIntToBytesArray(iNode, 0, nextINode);
	}	

	public byte getType() {
		return iNode[8];
	}

	public void setType(byte type) {
		iNode[8] = type;
	}

	public int getSize() {
		return DiskUtils.getIntFromBytesArray(iNode, 4);
	}

	public void setSize(int size) {
		DiskUtils.copyIntToBytesArray(iNode, 4, size);
	}

	public int getFirstBlock() {
		return DiskUtils.getIntFromBytesArray(iNode, 0);
	}

	public void setFirstBlock(int firstBlock) {
		DiskUtils.copyIntToBytesArray(iNode, 0, firstBlock);
	}
}
