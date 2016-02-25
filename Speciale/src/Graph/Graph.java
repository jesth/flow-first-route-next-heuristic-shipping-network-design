package Graph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Data.Data;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Results.Result;
import Results.Rotation;

public class Graph {
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private Data data;

	public Graph(String demandFileName, String fleetFileName) throws FileNotFoundException {
		data = new Data(demandFileName, fleetFileName);
		Result.initialize(this);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		createCentroids();
		createOmissionEdges();
	}

	private void createCentroids(){
		//Sets the number of centroids in the Node class once and for all, and is then garbage collected.
		new Node(data.getPorts().size());
		for(Port i : data.getPorts().values()){
			if(i.isActive()){
				Node fromCentroid = new Node(i, true);
				Node toCentroid = new Node(i, false);
				nodes.add(fromCentroid);
				nodes.add(toCentroid);
			}
		}
	}

	private void createOmissionEdges(){
		for(Demand i : data.getDemands()){
			Node fromCentroid = i.getOrigin().getFromCentroidNode();
			Node toCentroid = i.getDestination().getToCentroidNode();
			Edge newOmissionEdge = new Edge(fromCentroid, toCentroid, i.getRate());
			edges.add(newOmissionEdge);
		}
	}

	public Rotation createRotation(ArrayList<DistanceElement> distances, VesselClass vesselClass){
		Rotation rotation = new Rotation(vesselClass);
		createRotationEdges(distances, rotation, vesselClass);
		createLoadUnloadEdges(rotation);
		createTransshipmentEdges(rotation);
		rotation.calcOptimalSpeed();
		return rotation;
	}

	private void createTransshipmentEdges(Rotation rotation){
		ArrayList<Node> rotationNodes = rotation.getRotationNodes();
		for(Node i : rotationNodes){
			Port p = i.getPort();
			if(i.isDeparture()){
				for(Node j : p.getArrivalNodes()){
					if(!j.getRotation().equals(rotation)){
						createTransshipmentEdge(j, i);
					}
				}
			} else {
				for(Node j : p.getDepartureNodes()){
					if(!j.getRotation().equals(rotation)){
						createTransshipmentEdge(i, j);
					}
				}
			}
		}
	}

	private void createTransshipmentEdge(Node fromNode, Node toNode){
		int transshipCost = fromNode.getPort().getTransshipCost();
		Edge newEdge = new Edge(fromNode, toNode, transshipCost, Integer.MAX_VALUE, false, null, -1, null);
		edges.add(newEdge);
	}

	private void createRotationEdges(ArrayList<DistanceElement> distances, Rotation rotation, VesselClass vesselClass){
		checkDistances(distances);
		//Rotation opened at port 0 outside of for loop.
		DistanceElement currDist;
		Port firstPort = distances.get(0).getOrigin();
		Port depPort = firstPort;
		Port arrPort;
		Node firstNode = createRotationNode(depPort, rotation, true);
		Node depNode = firstNode;
		Node arrNode;
		for(int i = 0; i < distances.size()-1; i++){
			currDist = distances.get(i);
			arrPort = currDist.getDestination();
			arrNode = createRotationNode(arrPort, rotation, false);
			createRotationEdge(rotation, depNode, arrNode, 0, vesselClass.getCapacity(), i, currDist);
			depPort = arrPort;
			depNode = createRotationNode(depPort, rotation, true);
			createRotationEdge(rotation, arrNode, depNode, 0, vesselClass.getCapacity(), -1, null);
		}
		//Rotation closed at port 0 outside of for loop.
		currDist = distances.get(distances.size()-1);
		arrPort = currDist.getDestination();
		arrNode  = createRotationNode(arrPort, rotation, false);
		createRotationEdge(rotation, depNode, arrNode, 0, vesselClass.getCapacity(), distances.size()-1, currDist);
		createRotationEdge(rotation, arrNode, firstNode, 0, vesselClass.getCapacity(), -1, null);
	}

	public Node createRotationNode(Port port, Rotation rotation, boolean departure){
		Node newNode = new Node(port, rotation, departure);
		rotation.addRotationNode(newNode);
		nodes.add(newNode);
		return newNode;
	}

	public void createRotationEdge(Rotation rotation, Node fromNode, Node toNode, int cost, int capacity, int noInRotation, DistanceElement distance){
		Edge newEdge = new Edge(fromNode, toNode, cost, capacity, true, rotation, noInRotation, distance);
		rotation.addRotationEdge(newEdge);
		edges.add(newEdge);
	}

	private void checkDistances(ArrayList<DistanceElement> distances){
		Port firstPort = distances.get(0).getOrigin();
		for(int i = 1; i < distances.size(); i++){
			Port portA = distances.get(i-1).getDestination();
			Port portB = distances.get(i).getOrigin();
			if(portA.getPortId() != portB.getPortId()){
				throw new RuntimeException("The distances are not compatible.");
			}
		}
		Port lastPort = distances.get(distances.size()-1).getDestination();
		if(firstPort.getPortId() != lastPort.getPortId()){
			throw new RuntimeException("The rotation is not closed.");
		}
	}

	private void createLoadUnloadEdges(Rotation rotation){
		for(Node i : rotation.getRotationNodes()){
			if(i.isArrival()){
				createLoadUnloadEdge(i, i.getPort().getToCentroidNode());
			} else if(i.isDeparture()){
				createLoadUnloadEdge(i.getPort().getFromCentroidNode(), i);
			} else {
				throw new RuntimeException("Tried to create load/unload edge that does not match definition.");
			}
		}
	}

	public void createLoadUnloadEdge(Node fromNode, Node toNode){
		int loadUnloadCost = fromNode.getPort().getMoveCost();
		Edge newEdge = new Edge(fromNode, toNode, loadUnloadCost, Integer.MAX_VALUE, false, null, -1, null);
		edges.add(newEdge);
	}

	public void saveOPLData(String fileName){
		try {
			int noOfNodes = nodes.size();
			int noOfDemands = data.getDemands().size();
			int[][] capacity = new int[noOfNodes][noOfNodes];
			int[][] cost = new int[noOfNodes][noOfNodes];
			int[] demandFrom = new int[noOfDemands];
			int[] demandTo = new int[noOfDemands];
			int[] demand = new int[noOfDemands];

			for(Edge e : edges){
				int fromNode = e.getFromNode().getId();
				int toNode = e.getToNode().getId();
				capacity[fromNode][toNode] = e.getCapacity();
				cost[fromNode][toNode] = e.getRealCost();
			}
			for(int i = 0; i < noOfDemands; i++){
				Demand d = data.getDemands().get(i);
				demandFrom[i] = d.getOrigin().getFromCentroidNode().getId();
				demandTo[i] = d.getDestination().getToCentroidNode().getId();
				demand[i] = d.getDemand();

			}
			File fileOut = new File(fileName);
			BufferedWriter out;

			out = new BufferedWriter(new FileWriter(fileOut));

			out.write("n = " + noOfNodes + ";");
			out.newLine();
			out.write("d = " + noOfDemands + ";");
			out.newLine();
			out.newLine();

			out.write("u = [");
			writeDouble(out, capacity, noOfNodes);
			out.newLine();
			out.newLine();

			out.write("c = [");
			writeDouble(out, cost, noOfNodes);
			out.newLine();
			out.newLine();

			out.write("dFrom = [");
			writeSingle(out, demandFrom, noOfDemands);
			out.newLine();
			out.newLine();

			out.write("dTo = [");
			writeSingle(out, demandTo, noOfDemands);
			out.newLine();
			out.newLine();

			out.write("D = [");
			writeSingle(out, demand, noOfDemands);
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void writeSingle(BufferedWriter out, int[] array, int number) throws IOException{
		for(int i = 0; i < number; i++){
			out.write(array[i] + " ");				
		}
		out.write("];");
	}

	private void writeDouble(BufferedWriter out, int[][] array, int number) throws IOException{
		for(int i = 0; i < number; i++){
			out.write("[");
			for(int j = 0; j < number; j++){
				out.write(array[i][j] + " ");				
			}
			out.write("]");
			if(i < number-1){
				out.newLine();
			}
		}
		out.write("];");
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
