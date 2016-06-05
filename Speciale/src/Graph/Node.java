package Graph;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import Data.Port;
import Methods.BellmanFord;
import Results.Rotation;

public class Node {
	private int id;
	private int sequenceId;
	private boolean active;
	private Port port;
	private Rotation rotation;
	private boolean fromCentroid;
	private boolean toCentroid;
	private boolean departure;
	private boolean arrival;
	private int[] distances;
	private Edge[] predecessors;
	private boolean[] unprocessed;
	private int[] distancesRep;
	private Edge[] predecessorsRep;
	private boolean[] unprocessedRep;
	private static int noOfCentroids;
	private ArrayList<Edge> ingoingEdges;
	private ArrayList<Edge> outgoingEdges;

	/** Constructor for nodes, i.e. not centroids.
	 * @param port - the port in which the node is located.
	 * @param rotation - the rotation that the node represents.
	 * @param departure - true if departure and false if arrival.
	 */
	public Node(Port port, Rotation rotation, boolean departure, int id){
		super();
		this.id = id;
		this.active = true;
		this.port = port;
		if(departure){
			port.addDepartureNode(this);
		} else {
			port.addArrivalNode(this);
		}
		this.rotation = rotation;
		this.fromCentroid = false;
		this.toCentroid = false;
		this.departure = departure;
		this.arrival = !departure;
		this.distances = new int[noOfCentroids];
		this.predecessors = new Edge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.distancesRep = new int[noOfCentroids];
		this.predecessorsRep = new Edge[noOfCentroids];
		this.unprocessedRep = new boolean[noOfCentroids];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}
	
	/** Constructor for centroids.
	 * @param port - the port that the node represents.
	 * @param fromCentroid - whether this is a fromCentroid.
	 */
	public Node(Port port, boolean fromCentroid, int id){
		super();
		this.id = id;
		this.active = true;
		this.port = port;
		this.rotation = null;
		this.fromCentroid = fromCentroid;
		this.toCentroid = !fromCentroid;
		if(fromCentroid){
			this.port.setFromCentroidNode(this);
		} else {
			this.port.setToCentroidNode(this);
		}
		this.departure = false;
		this.arrival = false;
		this.distances = new int[noOfCentroids];
		this.predecessors = new Edge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.distancesRep = new int[noOfCentroids];
		this.predecessorsRep = new Edge[noOfCentroids];
		this.unprocessedRep = new boolean[noOfCentroids];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}
	
	public Node(Port port, Rotation rotation, Node copyNode){
		super();
		this.id = copyNode.getId();
		this.active = true;
		this.port = port;
		if(copyNode.isDeparture()){
			this.port.addDepartureNode(this);
			this.departure = true;
		} else if(copyNode.isArrival()) {
			this.port.addArrivalNode(this);
			this.arrival = true;
		} else if(copyNode.isFromCentroid()){
			this.port.setFromCentroidNode(this);
			this.fromCentroid = true;
		} else {
			this.port.setToCentroidNode(this);
			this.toCentroid = true;
		}
		this.rotation = rotation;
		if(rotation != null){
			rotation.addRotationNode(this);
		}
		this.distances = new int[noOfCentroids];
		this.predecessors = new Edge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.distancesRep = new int[noOfCentroids];
		this.predecessorsRep = new Edge[noOfCentroids];
		this.unprocessedRep = new boolean[noOfCentroids];
		this.ingoingEdges = new ArrayList<Edge>();
		this.outgoingEdges = new ArrayList<Edge>();
	}
	
	public static void setNoOfCentroids(int newNoOfCentroids){
		noOfCentroids = newNoOfCentroids;
	}
	
	public void setInactive(){
		active = false;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public boolean isEqualTo(Node i){
		if(fromCentroid != i.isFromCentroid() || toCentroid != i.isToCentroid() || arrival != i.isArrival() || departure != i.isDeparture()){
			return false;
		}
		int prevNoInRotation = getPrevEdge().getNoInRotation();
		int nextNoInRotation = getNextEdge().getNoInRotation();
		if(prevNoInRotation != i.getPrevEdge().getNoInRotation() || nextNoInRotation != i.getNextEdge().getNoInRotation()){
			return false;
		}
		int prevRotationId = getPrevEdge().getRotation().getId();
		int nextRotationId = getNextEdge().getRotation().getId();
		if(prevRotationId != i.getPrevEdge().getRotation().getId() || nextRotationId != i.getNextEdge().getRotation().getId()){
			return false;
		}
		if(!port.getPortData().equals(i.getPort().getPortData())){
			return false;
		}
		return true;
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
	 * @return Whether this is a fromCentroid node.
	 */
	public boolean isFromCentroid() {
		return fromCentroid;
	}
	
	/**
	 * @return Whether this is a toCentroid node.
	 */
	public boolean isToCentroid() {
		return toCentroid;
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
	
	public void setLabelsRep(int centroidId, int distance, Edge predecessor){
//		System.out.println("Setting labels for centroidId " + centroidId + ", distance " + distance + " and predecessor " + predecessor);
		distancesRep[centroidId] = distance;
		predecessorsRep[centroidId] = predecessor;
	}
	
	/** Sets this node unprocessed for the Bellman Ford algorithm.
	 * @param centroidId - the centroid <i>from</i> which the shortest path must be processed again.
	 */
	public void setUnprocessed(BellmanFord bellmanFord, int centroidId){
		bellmanFord.addUnprocessedNode(this);
		unprocessed[centroidId] = true;
	}
	
	public void setUnprocessedRep(BellmanFord bellmanFord, int centroidId){
		bellmanFord.addUnprocessedNodeRep(this);
		unprocessedRep[centroidId] = true;
	}
	
	/** Sets this node processed through the Bellman Ford algorithm.
	 * @param centroidId - the centroid <i>from</i> which the shortest path has been processed.
	 */
	public void setProcessed(int centroidId){
		unprocessed[centroidId] = false;
	}
	
	public void setProcessedRep(int centroidId){
		unprocessedRep[centroidId] = false;
	}
	
	/**
	 * @param centroidId - the centroid <i>from</i> where the shortest path originates.
	 * @return Whether this node is unprocessed from the input centroid.
	 */
	public boolean isUnprocessed(int centroidId){
		return unprocessed[centroidId];
	}
	
	public boolean isUnprocessedRep(int centroidId){
		return unprocessedRep[centroidId];
	}
	

	/**
	 * @return Whether all spaces of the unprocessed array has been processed in the Bellman-Ford algorithm.
	 */
	public boolean allCentroidsProcessed() {
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
	
	public int getDistanceRep(int centroidId){
		return distancesRep[centroidId];
	}
	
	/**
	 * @param centroidId - the centroid <i>from</i> where the shortest path originates.
	 * @return The predecessor in the shortest path from the input centroid.
	 */
	public Edge getPredecessor(int centroidId){
		return predecessors[centroidId];
	}
	
	public Edge getPredecessorRep(int centroidId){
		return predecessorsRep[centroidId];
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
	
	public Edge getPrevEdge(){
		Edge prevEdge = null;
		if(isDeparture()){
			for(Edge e : ingoingEdges){
				if(e.isDwell()){
					return e;
				}
			}
		} else if (isArrival()){
			int counter = 0;
			for(Edge e : ingoingEdges){
				if(e.isActive() && e.isSail()){
					prevEdge = e;
					counter++;
				}
			}
			if(counter > 1 || counter < 1){
//				System.out.println("Ingoing active sail edge counter for arrival node = " + counter );
			}
			if(counter > 0){
				return prevEdge;	
			}
			
		}
//		if(ingoingEdges.size() != 1){
////			System.out.println("Removing in port " + port.getUNLocode() + " with ingoing edges size " + ingoingEdges.size());
////			System.out.println("Rotation id " + rotation.getId());
////			throw new RuntimeException("More than one or no ingoing edge.");
//			System.out.println("Zero or multiple ingoing edges in " + port.getUNLocode());
//			for(int i = 0; i < ingoingEdges.size(); i++){
//				System.out.print(i + " " + ingoingEdges.get(i).simplePrint());
//				if(ingoingEdges.get(i).isActive()){
//					System.out.println(" active");
//				} else {
//					System.out.println(" inactive");
//				}
//				
//			}
//		}
		return ingoingEdges.get(0);
	}
	
	public Node getPrevNode(){
		return getPrevEdge().getFromNode();
	}
	
	/**
	 * @return A list of all outgoing edges.
	 */
	public ArrayList<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}
	
	public Edge getNextEdge(){
		Edge nextEdge = null;
		if(isArrival()){
			for(Edge e : outgoingEdges){
				if(e.isDwell()){
					return e;
				}
			}
		} else if (isDeparture()){
			int counter = 0;
			for(Edge e : outgoingEdges){
				if(e.isActive() && e.isSail()){
					nextEdge = e;
					counter++;
				}
			}
			if(counter > 1 || counter < 1){
//				System.out.println("Outgoing active sail edge counter for " + port.getUNLocode() + " departure node = " + counter );
			}
			if(counter > 0){
				return nextEdge;
			}
		}
//		if(outgoingEdges.size() != 1){
//			System.out.println("Removing in port " + port.getUNLocode() + " with ingoing edges size " + ingoingEdges.size());
//			System.out.println("Rotation id " + rotation.getId());
//			throw new RuntimeException("More than one or no outgoing edge.");
//		}
		
//		if(outgoingEdges.size() != 1){
//			System.out.println("Removing in port " + port.getUNLocode() + " with outgoing edges size " + ingoingEdges.size());
//			System.out.println("Rotation id " + rotation.getId());
//			throw new RuntimeException("More than one or no outgoing edge.");
//			System.out.println("Zero or multiple outgoing edges in " + port.getUNLocode());
//			for(int i = 0; i < outgoingEdges.size(); i++){
//				System.out.print(i + " " + outgoingEdges.get(i).simplePrint());
//				if(outgoingEdges.get(i).isActive()){
//					System.out.println(" active");
//				} else {
//					System.out.println(" inactive");
//				}
//				
//			}
//		}
		return outgoingEdges.get(0);
	}
	
	public Node getNextNode(){
		return getNextEdge().getToNode();
	}
	
	/**
	 * @return The id of the port to which the node belongs.
	 */
	public int getPortId(){
		return port.getPortId();
	}
	
	public int getId(){
		return id;
	}
	
	public int getSequenceId(){
		return sequenceId;
	}
	
	public void setSequenceId(int sequenceId){
		this.sequenceId = sequenceId;
	}
	
	public void removeIngoingEdge(Edge e){
		ingoingEdges.remove(e);
	}
	
	public void removeOutgoingEdge(Edge e){
		outgoingEdges.remove(e);
	}
	
	public int getLoadedFFE(){
		int load = 0;
		for(Edge e : ingoingEdges){
			if(e.isLoadUnload() && e.isActive()){
				load += e.getLoad();
			}
		}
		return load;
	}
	
	public int getTransshippedToFFE(){
		int transshippedTo = 0;
		for(Edge e : ingoingEdges){
			if(e.isTransshipment() && e.isActive()){
				transshippedTo += e.getLoad();
			}
		}
		return transshippedTo;
	}
	
	public int getUnloadedFFE(){
		int unload = 0;
		for(Edge e : outgoingEdges){
			if(e.isLoadUnload() && e.isActive()){
				unload += e.getLoad();
			}
		}
		return unload;
	}
	
	public int getTransshippedFromFFE(){
		int transshippedFrom = 0;
		for(Edge e : outgoingEdges){
			if(e.isTransshipment() && e.isActive()){
				transshippedFrom += e.getLoad();
			}
		}
		return transshippedFrom;
	}

	public static int getNoOfCentroids() {
		return noOfCentroids;
	}
	
	//DO NOT USE!
	/*
	public void decrementId(){
		id--;
	}
	*/
	
	public String simplePrint(){
		String str = "";
		if(fromCentroid){
			str += "fromCentroid ";
		} else if(toCentroid){
			str += "toCentroid ";
		} else if(departure){
			str += "Departure ";
		} else if(arrival){
			str += "Arrival ";
		}
		str += "at port " + port.getUNLocode();
		return str;
	}

	public Edge getFeeder(Node toNode) {
		for(Edge e : outgoingEdges){
			if(e.getToNode().equals(toNode) && e.isFeeder()){
				return e;
			}
		}
		return null;
	}

	
}
