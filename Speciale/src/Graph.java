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
	
	public void createCentroids(){
		//Sets the number of centroids in the Node class once and for all, and is then garbage collected.
		new Node(data.getPorts().size());
		for(Port i : data.getPorts().values()){
			Node newCentroid = new Node(i);
			nodes.add(newCentroid);
			i.setCentroidNode(newCentroid);
		}
	}
	
	
	public void createOmissionEdges(){
		for(Demand i : data.getDemands()){
			Node fromCentroid = i.getOrigin().getCentroidNode();
			Node toCentroid = i.getDestination().getCentroidNode();
			Edge newOmissionEdge = new Edge(fromCentroid, toCentroid);
			edges.add(newOmissionEdge);
		}
	}
	
	public Rotation createRotation(ArrayList<Port> ports, VesselClass vesselClass){
		Rotation rotation = new Rotation(vesselClass);
		Node firstNode = new Node(ports.get(0), rotation, true);
		Node depNode = firstNode;
		for(int i = 1; i < ports.size(); i++){
			Node arrNode  = new Node(ports.get(i), rotation, false);
//			double travelTime = data.getDistances()
			Edge sailEdge = new Edge(depNode, arrNode, 0, vesselClass.getCapacity(), 24);
		}
		
		
		
		return rotation;
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
