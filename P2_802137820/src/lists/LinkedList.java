package lists;

public interface LinkedList<E> {
	
	int length(); 

	/**
	 * Returns the node before the node given as parameter.
	 * @param target the node given as parameter
	 * @return the node before target.
	 * @throws NodeOutOfBoundsException if target node doesn't
	 * have a node before.
	 */
	Node<E> getNodeBefore(Node<E> target) throws NodeOutOfBoundsException; 
	
	/**
	 * Returns the node after the node given as parameter.
	 * @param target the node given as parameter
	 * @return the node before target node
	 * @throws INodeOutOfBoundsException if target node doesn't
	 * have a node after.
	 */
	Node<E> getNodeAfter(Node<E> target) throws NodeOutOfBoundsException; 
	
	/**
	 *  @return reference to the first node of the linked list
	 *  @throws INodeOutOfBoundsException if the linked list is empty
	 */
	Node<E> getFirstNode() throws NodeOutOfBoundsException; 
	
	/**
	 * Returns the last node of the list
	 * @return the last node of the list
	 * @throws INodeOutOfBoundsException if list is empty
	 */
	Node<E> getLastNode() throws NodeOutOfBoundsException; 
	
	/**
	 * Add a node the beginning of the list.
	 * @param nuevo the node to add.
	 */
	void addFirstNode(Node<E> nuevo); 
	
	/**
	 * Add a new node after the targeted node.
	 * @param target the node to target
	 * @param nuevo the node to set after target
	 */
	void addNodeAfter(Node<E> target, Node<E> nuevo); 
	
	/**
	 * Add a new node before the targeted node.
	 * @param target the node to target
	 * @param nuevo the node to set before target
	 */
	void addNodeBefore(Node<E> target, Node<E> nuevo); 
	
	/**
	 * Remove the first node of the list
	 * @return the node removed
	 * @throws INodeOutOfBoundsException if list is empty
	 */
	Node<E> removeFirstNode() throws NodeOutOfBoundsException; 
	
	/**
	 * Remove the last node of the list
	 * @return the node removed
	 * @throws INodeOutOfBoundsException if list is empty
	 */
	Node<E> removeLastNode() throws NodeOutOfBoundsException; 
	
	/**
	 * Remove the targeted node.
	 * @param target the node to remove
	 */
	void removeNode(Node<E> target); 
	
	/**
	 * Remove node that is after the targeted node
	 * @param target the node before the one to remove
	 * @return the node to remove
	 * @throws INodeOutOfBoundsException if there's no node after target
	 */
	Node<E> removeNodeAfter(Node<E> target) throws NodeOutOfBoundsException; 
	
	/**
	 * Remove node that is before the targeted node.
	 * @param target the node after the one to remove.
	 * @return the node to remove
	 * @throws INodeOutOfBoundsException if there's no node before target
	 */
	Node<E> removeNodeBefore(Node<E> target) throws NodeOutOfBoundsException; 
	
	/**
	 * Creates a new node instance of the type of nodes that the linked list
	 * uses. The new node will have all its instance fields initialized to
	 * null. The new node is not linked to the list in any way.
	 * @return reference to the new node instance. 
	 */
	Node<E> createNewNode(); 

}
