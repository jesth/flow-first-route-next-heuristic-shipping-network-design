
public class Edge {
	private Node fromNode;
	private Node toNode;
	private int cost; 
	private int capacity;
	private boolean omission;
	
	public Edge(){
	}

	/** Constructor for "real" edges. I.e. not omission or transshipment.
	 * @param fromNode
	 * @param toNode
	 * @param cost
	 * @param capacity
	 */
	public Edge(Node fromNode, Node toNode, int cost, int capacity){
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = cost;
		this.capacity = capacity;
		this.omission = false;
	}

	/** Constructor for omission edges.
	 *  Cost is set default 1000$.
	 *  Capacity is set default to Integer.MAX_VALUE.
	 * @param fromNode
	 * @param toNode
	 */
	public Edge(Node fromNode, Node toNode){
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = 1000;
		this.capacity = Integer.MAX_VALUE;
		this.omission = true;
	}
	
	/**
	 * @return the fromNode
	 */
	public Node getFromNode() {
		return fromNode;
	}

	/**
	 * @return the toNode
	 */
	public Node getToNode() {
		return toNode;
	}

	/**
	 * @return the cost
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @return the omission
	 */
	public boolean isOmission() {
		return omission;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Edge [fromNode=" + fromNode + ", toNode=" + toNode + ", cost=" + cost + ", capacity=" + capacity
				+ ", omission=" + omission + "]";
	}
	
}
