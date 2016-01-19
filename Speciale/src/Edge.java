
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
	private Rotation rotation;
	private int noInRotation;
	
	public Edge(){
	}

	/** Constructor for rotation, load/unload and transshipment edges, i.e. not omission edges. 
	 * @param fromNode
	 * @param toNode
	 * @param cost
	 * @param capacity
	 */
	public Edge(Node fromNode, Node toNode, int cost, int capacity, double travelTime, boolean rotationEdge, Rotation rotation, int noInRotation){
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = cost;
		this.capacity = capacity;
		this.travelTime = travelTime;
		this.omission = false;
		this.sail = false;	
		this.dwell = false;
		this.transshipment = false;
		this.loadUnload = false;
		this.rotation = rotation;
		this.noInRotation = noInRotation;
		if(fromNode.isDeparture() && toNode.isArrival() && rotationEdge){
			this.sail = true;	
		} else if(fromNode.isArrival() && toNode.isDeparture() && rotationEdge){
			this.dwell = true;
		} else if(fromNode.isArrival() && toNode.isDeparture() && !rotationEdge) {
			this.transshipment = true;
		} else if(fromNode.isArrival() && toNode.isCentroid() || fromNode.isCentroid() && toNode.isDeparture()){
			this.loadUnload = true;
		} else {
			throw new RuntimeException("Tried to construct an edge that does not fit "
					+ "with either sail, dwell, transshipment or load/unload definitions.");
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
		this.rotation = null;
		this.noInRotation = -1;
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
	
	public boolean isSail() {
		return sail;
	}

	public boolean isDwell() {
		return dwell;
	}

	public boolean isTransshipment() {
		return transshipment;
	}

	public boolean isLoadUnload() {
		return loadUnload;
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
	
	public Rotation getRotation(){
		return this.rotation;
	}
	
	public int getNoInRotation(){
		return this.noInRotation;
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
