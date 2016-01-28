import java.util.ArrayList;

public class Node {
	private Port port;
	private Rotation rotation;
	private boolean centroid;
	private boolean departure;
	private boolean arrival;
	private int[] distances;
	private Edge[] predecessors;
	private boolean[] unprocessed;
	private static int noOfCentroids;
	private ArrayList<Edge> ingoingEdges;
	private ArrayList<Edge> outgoingEdges;
	
	/** Constructor for setting the number of centroids. To be used one time only!
	 * @param noOfCentroids
	 */
	public Node(int noOfCentroids){
		Node.noOfCentroids = noOfCentroids;
	}

	/** Constructor for nodes, i.e. not centroids.
	 * @param port - the port in which the node is located.
	 * @param rotation - the rotation that the node represents.
	 * @param departure - boolean, true if departure and false if arrival.
	 */
	public Node(Port port, Rotation rotation, boolean departure){
		super();
		this.port = port;
		this.rotation = rotation;
		this.centroid = false;
		this.departure = departure;
		this.arrival = !departure;
		this.distances = new int[noOfCentroids+1];
		this.predecessors = new Edge[noOfCentroids+1];
		this.unprocessed = new boolean[noOfCentroids+1];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}
	
	/** Constructor for centroids.
	 * @param port - the port that the node represents.
	 */
	public Node(Port port){
		super();
		this.port = port;
		this.rotation = null;
		this.centroid = true;
		this.departure = false;
		this.arrival = false;
		this.distances = new int[noOfCentroids];
		this.predecessors = new Edge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}

	/**
	 * @return The port.
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * @return The rotation.
	 */
	public Rotation getRotation() {
		return rotation;
	}

	/**
	 * @return Whether this is a centroid node.
	 */
	public boolean isCentroid() {
		return centroid;
	}
	
	/**
	 * @return Whether this is a departure node.
	 */
	public boolean isDeparture() {
		return departure;
	}

	/**
	 * @return Whether this is an arrival node.
	 */
	public boolean isArrival() {
		return arrival;
	}
	
	/** Sets the labels of the node to the specified inputs. Used in Bellman Ford.
	 * @param centroidId - the id of the centroid <i>from</i> which the shortest path is computed.
	 * @param distance - the distance from the centroid via the shortest path.
	 * @param predecessor - the predecessor edge in the shortest path.
	 */
	public void setLabels(int centroidId, int distance, Edge predecessor){
		distances[centroidId] = distance;
		predecessors[centroidId] = predecessor;
	}
	
	/** Sets this node unprocessed for the Bellman Ford algorithm.
	 * @param centroidId - the centroid <i>from</i> which the shortest path must be processed again.
	 */
	public void setUnprocessed(int centroidId){
		BellmanFord.addUnprocessedNode(this);
		unprocessed[centroidId] = true;
	}
	
	/** Sets this node processed through the Bellman Ford algorithm.
	 * @param centroidId - the centroid <i>from</i> which the shortest path has been processed.
	 */
	public void setProcessed(int centroidId){
		unprocessed[centroidId] = false;
	}
	
	/**
	 * @param centroidId - the centroid <i>from</i> where the shortest path originates.
	 * @return Whether this node is unprocessed from the input centroid.
	 */
	public boolean isUnprocessed(int centroidId){
		return unprocessed[centroidId];
	}
	

	public boolean allNodesProcessed() {
		for(boolean i : unprocessed){
			if(i){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param centroidId - the centroid <i>from</i> where the shortest path originates.
	 * @return The distance from the input centroid.
	 */
	public int getDistance(int centroidId){
		return distances[centroidId];
	}
	
	/**
	 * @param centroidId - the centroid <i>from</i> where the shortest path originates.
	 * @return The predecessor in the shortest path from the input centroid.
	 */
	public Edge getPredecessor(int centroidId){
		return predecessors[centroidId];
	}
	
	/**
	 * @return Distances from all centroids to this node.
	 */
	public int[] getDistances() {
		return distances;
	}

	/**
	 * @return Predecessors to this node in the shortest path from all centroids.
	 */
	public Edge[] getPredecessors() {
		return predecessors;
	}
	
	/** Adds an ingoing edge to the node.
	 * @param ingoingEdge - the edge to be added.
	 */
	public void addIngoingEdge(Edge ingoingEdge){
		ingoingEdges.add(ingoingEdge);
	}
	
	/** Adds an outgoing edge to the node.
	 * @param outgoingEdge - the edge to be added.
	 */
	public void addOutgoingEdge(Edge outgoingEdge){
		outgoingEdges.add(outgoingEdge);
	}
	
	/**
	 * @return A list of all ingoing edges.
	 */
	public ArrayList<Edge> getIngoingEdges() {
		return ingoingEdges;
	}
	
	/**
	 * @return A list of all outgoing edges.
	 */
	public ArrayList<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}
	
	/**
	 * @return The id of the port to which the node belongs.
	 */
	public int getPortId(){
		return port.getPortId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node [port=" + port.getName() + ", rotation=" + rotation + ", centroid=" + centroid + "]";
	}

	
}
