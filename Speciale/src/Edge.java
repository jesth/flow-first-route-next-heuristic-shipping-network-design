
public class Edge {
	private Node fromNode;
	private Node toNode;
	private int cost; 
	private int capacity;
	private int load;
	private double travelTime;
	private boolean omission;
	private boolean sail;
	private boolean dwell;
	private boolean transshipment;
	private boolean loadUnload;
	
	public Edge(){
	}

	/** Constructor for rotation edges. I.e. not omission, load/unload or transshipment. 
	 * @param fromNode
	 * @param toNode
	 * @param cost
	 * @param capacity
	 */
	public Edge(Node fromNode, Node toNode, int cost, int capacity, double travelTime){
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = cost;
		this.capacity = capacity;
		this.travelTime = travelTime;
		this.omission = false;
		this.transshipment = false;
		this.loadUnload = false;
		if(fromNode.isDeparture() && toNode.isArrival()){
			this.sail = true;	
			this.dwell = false;
		} else if(fromNode.isArrival() && toNode.isDeparture()){
			this.sail = false;	
			this.dwell = true;
		} else {
			throw new RuntimeException("Tried to construct a rotation edge where both "
					+ "from and to node are of the same type (dep or arr).");
		}
		toNode.addIngoingEdge(this);
		fromNode.addOutgoingEdge(this);
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
		this.travelTime = 0;
		this.omission = true;
		this.sail = false;
		this.dwell = false;
		this.transshipment = false;
		this.loadUnload = false;
		toNode.addIngoingEdge(this);
		fromNode.addOutgoingEdge(this);
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
	
	public double getTravelTime() {
		return travelTime;
	}
	
	public void resetLoad(){
		load = 0;
	}
	
	public void addLoad(int load){
		this.load += load;
	}

	public int getLoad() {
		return load;
	}
	
	public String getFromPortUNLo(){
		return fromNode.getPort().getUNLocode();
	}
	
	public String getToPortUNLo(){
		return toNode.getPort().getUNLocode();
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
