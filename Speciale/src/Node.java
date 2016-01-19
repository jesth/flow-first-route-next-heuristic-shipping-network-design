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
	 * @param port
	 * @param rotation
	 */
	public Node(Port port, Rotation rotation, boolean departure){
		super();
		this.port = port;
		this.rotation = rotation;
		this.centroid = false;
		this.departure = departure;
		this.arrival = !departure;
		this.distances = new int[noOfCentroids];
		this.predecessors = new Edge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}
	
	/** Constructor for centroids.
	 * @param port
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
	 * @return the port
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * @return the rotation
	 */
	public Rotation getRotation() {
		return rotation;
	}

	/**
	 * @return the centroid
	 */
	public boolean isCentroid() {
		return centroid;
	}
	
	public boolean isDeparture() {
		return departure;
	}

	public boolean isArrival() {
		return arrival;
	}
	
	/**
	 * @param centroidId
	 * @param distance
	 * @param predecessor
	 */
	public void setLabels(int centroidId, int distance, Edge predecessor){
		distances[centroidId] = distance;
		predecessors[centroidId] = predecessor;
	}
	
	public void setUnprocessed(int centroidId){
		BellmanFord.addUnprocessedNode(this);
		unprocessed[centroidId] = true;
	}
	
	public void setProcessed(int centroidId){
		BellmanFord.removeUnprocessedNode(this);
		unprocessed[centroidId] = false;
	}
	
	public boolean isUnprocessed(int centroidId){
		return unprocessed[centroidId];
	}
	
	public int getDistance(int centroidId){
		return distances[centroidId];
	}
	
	public Edge getPredecessor(int centroidId){
		return predecessors[centroidId];
	}
	
	public int[] getDistances() {
		return distances;
	}

	public Edge[] getPredecessors() {
		return predecessors;
	}
	
	public void addIngoingEdge(Edge ingoingEdge){
		ingoingEdges.add(ingoingEdge);
	}
	
	public void addOutgoingEdge(Edge outgoingEdge){
		outgoingEdges.add(outgoingEdge);
	}
	
	public ArrayList<Edge> getIngoingEdges() {
		return ingoingEdges;
	}
	
	public ArrayList<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}
	
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
