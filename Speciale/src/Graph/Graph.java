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
	private Result result;

	public Graph(String demandFileName, String fleetFileName) throws FileNotFoundException {
		data = new Data(demandFileName, fleetFileName);
		result = new Result(this);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		createCentroids();
		createOmissionEdges();
	}

	private void createCentroids(){
		//Sets the number of centroids in the Node class once and for all, and is then garbage collected.
		new Node(data.getPortsMap().size());
		for(Port i : data.getPortsMap().values()){
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
		result.addRotation(rotation);
		return rotation;
	}

	public Rotation createRotationFromPorts(ArrayList<Integer> ports, VesselClass vesselClass){
		ArrayList<DistanceElement> distances = findDistances(ports, vesselClass);
		Rotation rotation = createRotation(distances, vesselClass);
		return rotation;
	}

	private ArrayList<DistanceElement> findDistances(ArrayList<Integer> ports, VesselClass vesselClass){
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		for(int i = 0; i < ports.size() - 1; i++){
			int port1 = ports.get(i);
			int port2 = ports.get(i+1);
			distances.add(data.getBestDistanceElement(port1, port2, vesselClass));
		}
		int lastPort = ports.get(ports.size()-1);
		int firstPort = ports.get(0);
		distances.add(data.getBestDistanceElement(lastPort, firstPort, vesselClass));
		return distances;
	}

	private void createTransshipmentEdges(Rotation rotation){
		ArrayList<Node> rotationNodes = rotation.getRotationNodes();
		createTransshipmentEdges(rotationNodes, rotation);
	}

	private void createTransshipmentEdges(Edge e){
		ArrayList<Node> rotationNodes = new ArrayList<Node>();
		rotationNodes.add(e.getFromNode());
		rotationNodes.add(e.getToNode());
		createTransshipmentEdges(rotationNodes, e.getRotation());
	}

	private void createTransshipmentEdges(ArrayList<Node> rotationNodes, Rotation rotation){
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
		checkDistances(distances, vesselClass);
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

	public Edge createRotationEdge(Rotation rotation, Node fromNode, Node toNode, int cost, int capacity, int noInRotation, DistanceElement distance){
		Edge newEdge = new Edge(fromNode, toNode, cost, capacity, true, rotation, noInRotation, distance);
		rotation.addRotationEdge(newEdge);
		edges.add(newEdge);
		return newEdge;
	}


	public void insertPort(Rotation r, Edge e, Port p) {
		System.out.println("Inserting " + p.getUNLocode() + " on rotation " + r.getId() + " between " + e.getFromPortUNLo() + " and " + e.getToPortUNLo());
		Node fromNode = e.getFromNode();
		Node toNode = e.getToNode();
		deleteEdge(e);
		r.incrementNoInRotation(e.getNoInRotation());
		Node newArrNode = createRotationNode(p, r, false);
		Node newDepNode = createRotationNode(p, r, true);
		DistanceElement newIngoing = data.getBestDistanceElement(fromNode.getPort(), newArrNode.getPort(), r.getVesselClass());
		DistanceElement newOutgoing = data.getBestDistanceElement(newDepNode.getPort(), toNode.getPort(), r.getVesselClass());
		createRotationEdge(r, fromNode, newArrNode, 0, r.getVesselClass().getCapacity(), e.getNoInRotation(), newIngoing);
		Edge dwell = createRotationEdge(r, newArrNode, newDepNode, 0, r.getVesselClass().getCapacity(), -1, null);
		createRotationEdge(r, newDepNode, toNode, 0, r.getVesselClass().getCapacity(), e.getNoInRotation()+1, newOutgoing);
		createTransshipmentEdges(dwell);
		createLoadUnloadEdges(dwell);
		r.calcOptimalSpeed();
	}

	public void removePort(Edge dwell){
		Rotation r = dwell.getRotation();
		if(!dwell.isDwell()){
			throw new RuntimeException("Passed edge is not dwell.");
		}
		System.out.println("Removing port " + dwell.getFromPortUNLo() + " from rotation " + r.getId() + " with noInRotation from " + dwell.getPrevEdge().getNoInRotation());
		Edge ingoingEdge = dwell.getPrevEdge();
		Edge outgoingEdge = dwell.getNextEdge();
		r.decrementNoInRotation(outgoingEdge.getNoInRotation());
		Node fromNode = dwell.getFromNode();
		Node toNode = dwell.getToNode();
		deleteNode(fromNode);
		deleteNode(toNode);
		Port prevPort = ingoingEdge.getFromNode().getPort();
		Port nextPort = outgoingEdge.getToNode().getPort();
		if(prevPort.equals(nextPort)){
			Edge newDwell = null;
			for(Edge e : outgoingEdge.getToNode().getOutgoingEdges()){
				if(e.isDwell()){
					newDwell = e;
					break;
				}
			}
			System.out.println("Removing port " + newDwell.getFromPortUNLo() + " from rotation " + r.getId());
			outgoingEdge = newDwell.getNextEdge();
			r.decrementNoInRotation(outgoingEdge.getNoInRotation());
			deleteNode(newDwell.getFromNode());
			deleteNode(newDwell.getToNode());
			nextPort = outgoingEdge.getToNode().getPort();
		}
		fromNode = ingoingEdge.getFromNode();
		toNode = outgoingEdge.getToNode();
		if(fromNode.getPort().equals(toNode.getPort())){
			deleteNode(fromNode);
			deleteNode(toNode);
			r.delete();
			result.removeRotation(r);
			System.out.println("Rotation no. " + r.getId() + " deleted. Last remaining port: " + fromNode.getPort().getUNLocode());

		} else {
			DistanceElement distance = data.getBestDistanceElement(fromNode.getPort(), toNode.getPort(), r.getVesselClass());
			createRotationEdge(r, fromNode, toNode, 0, r.getVesselClass().getCapacity(), ingoingEdge.getNoInRotation(), distance);
			r.calcOptimalSpeed();
		}
	}

	public void deleteEdge(Edge e){
		e.delete();
		edges.remove(e);
	}

	public void deleteNode(Node i){
		ArrayList<Edge> ingoingEdges = i.getIngoingEdges();
		for(int j = ingoingEdges.size()-1; j >= 0; j--){
			Edge e = ingoingEdges.remove(j);
			deleteEdge(e);
		}
		ArrayList<Edge> outgoingEdges = i.getOutgoingEdges();
		for(int j = outgoingEdges.size()-1; j >= 0; j--){
			Edge e = outgoingEdges.remove(j);
			deleteEdge(e);
		}
		i.getRotation().getRotationNodes().remove(i);
		nodes.remove(i);
	}

	private void checkDistances(ArrayList<DistanceElement> distances, VesselClass vesselClass){
		Port firstPort = distances.get(0).getOrigin();
		for(int i = 1; i < distances.size(); i++){
			Port portA = distances.get(i-1).getDestination();
			Port portB = distances.get(i).getOrigin();
			if(portA.getPortId() != portB.getPortId()){
				throw new RuntimeException("The distances are not compatible.");
			}
			if(portA.getDraft() < vesselClass.getDraft()){
				throw new RuntimeException("The draft at " + portA.getUNLocode() + " is exceeded.");
			}
		}
		Port lastPort = distances.get(distances.size()-1).getDestination();
		if(firstPort.getPortId() != lastPort.getPortId()){
			throw new RuntimeException("The rotation is not closed.");
		}
		if(lastPort.getDraft() < vesselClass.getDraft()){
			throw new RuntimeException("The draft at " + lastPort.getUNLocode() + " is exceeded.");
		}
		for(DistanceElement d : distances){
			if(d.getDraft() < vesselClass.getDraft()){
				throw new RuntimeException("The draft between " + d.getOrigin().getUNLocode() + " and " + d.getDestination().getUNLocode() + " is exceeded.");
			}
		}
	}

	private void createLoadUnloadEdges(Rotation rotation){
		createLoadUnloadEdges(rotation.getRotationNodes(), rotation);
	}

	private void createLoadUnloadEdges(Edge edge){
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(edge.getFromNode());
		nodes.add(edge.getToNode());
		System.out.println("No of edges to create load/unload for: " + nodes.size());
		createLoadUnloadEdges(nodes, edge.getRotation());
	}

	private void createLoadUnloadEdges(ArrayList<Node> rotationNodes, Rotation rotation){
		for(Node i : rotationNodes){
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

	public Port getPort(int portId){
		return data.getPort(portId);
	}

	public Result getResult(){
		return result;
	}

}
