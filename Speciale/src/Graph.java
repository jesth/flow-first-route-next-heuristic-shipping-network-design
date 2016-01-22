import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Graph {
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private Data data;
	
	public Graph() throws FileNotFoundException {
		data = new Data("Demand_Baltic.csv", "fleet_Baltic.csv");
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		createCentroids();
		createOmissionEdges();
	}
	
	private void createCentroids(){
		//Sets the number of centroids in the Node class once and for all, and is then garbage collected.
		new Node(data.getPorts().size());
		for(Port i : data.getPorts().values()){
			Node newCentroid = new Node(i);
			nodes.add(newCentroid);
			i.setCentroidNode(newCentroid);
		}
	}
	
	private void createOmissionEdges(){
		for(Demand i : data.getDemands()){
			Node fromCentroid = i.getOrigin().getCentroidNode();
			Node toCentroid = i.getDestination().getCentroidNode();
			Edge newOmissionEdge = new Edge(fromCentroid, toCentroid, i.getRate());
			edges.add(newOmissionEdge);
		}
	}
	
	public Rotation createRotation(ArrayList<Port> ports, VesselClass vesselClass, boolean suez, boolean panama){
		Rotation rotation = new Rotation(vesselClass);
		createRotationEdges(ports, vesselClass, rotation, suez, panama);
		createLoadUnloadEdges(rotation);
//		createTransshipmentEdges(Rotation rotation);
		return rotation;
	}
	
	private Node createRotationNode(Port port, Rotation rotation, boolean departure){
		Node newNode = new Node(port, rotation, departure);
		rotation.addRotationNode(newNode);
		nodes.add(newNode);
		return newNode;
	}
	
	private void createRotationEdges(ArrayList<Port> ports, VesselClass vesselClass, Rotation rotation, boolean suez, boolean panama){
		//Rotation opened at port 0 outside of for loop.
		Node firstNode = createRotationNode(ports.get(0), rotation, true);
		Node depNode = firstNode;
		Node arrNode;
		int distanceKM;
		for(int i = 1; i < ports.size(); i++){
			arrNode  = createRotationNode(ports.get(i), rotation, false);
			Distance distance = data.getDistance(depNode.getPortId(), arrNode.getPortId());
			distanceKM = distance.getDistance(suez, panama);
			createRotationEdge(rotation, depNode, arrNode, distanceKM, 0, vesselClass.getCapacity(), i, suez, panama);
			depNode = createRotationNode(ports.get(i), rotation, true);
			createRotationEdge(rotation, arrNode, depNode, distanceKM, 0, vesselClass.getCapacity(), -1, suez, panama);
		}
		//Rotation closed at port 0 outside of for loop.
		arrNode  = createRotationNode(ports.get(0), rotation, false);
		Distance distance = data.getDistance(depNode.getPortId(), arrNode.getPortId());
		distanceKM = distance.getDistance(suez, panama); 
		createRotationEdge(rotation, depNode, arrNode, distanceKM, 0, vesselClass.getCapacity(), ports.size(), suez, panama);
		createRotationEdge(rotation, arrNode, firstNode, distanceKM, 0, vesselClass.getCapacity(), -1, suez, panama);
	}
	
	private void createRotationEdge(Rotation rotation, Node fromNode, Node toNode, int distanceKM, int cost, int capacity, int noInRotation, boolean suez, boolean panama){
		Edge newEdge = new Edge(fromNode, toNode, distanceKM, cost, capacity, true, rotation, noInRotation, suez, panama);
		rotation.addRotationEdge(newEdge);
		edges.add(newEdge);
	}
	
	private void createLoadUnloadEdges(Rotation rotation){
		for(Node i : rotation.getRotationNodes()){
			if(i.isArrival()){
				createLoadUnloadEdge(i, i.getPort().getCentroidNode());
			} else if(i.isDeparture()){
				createLoadUnloadEdge(i.getPort().getCentroidNode(), i);
			} else {
				throw new RuntimeException("Tried to create load/unload edge that does not match definition.");
			}
		}
	}
	
	private void createLoadUnloadEdge(Node fromNode, Node toNode){
		int loadUnloadCost = fromNode.getPort().getMoveCost();
		Edge newEdge = new Edge(fromNode, toNode, 0, loadUnloadCost, Integer.MAX_VALUE, false, null, -1, false, false);
		edges.add(newEdge);
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public Data getData() {
		return data;
	}
}
