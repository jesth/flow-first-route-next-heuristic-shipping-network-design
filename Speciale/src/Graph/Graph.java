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
import Data.ReadData;
import Data.VesselClass;
import Methods.MulticommodityFlowThreads;
import Results.Result;
import Results.Rotation;
import Results.Route;
import RotationFlow.RotationDemand;
import RotationFlow.RotationEdge;
import RotationFlow.RotationGraph;
import RotationFlow.RotationNode;

public class Graph {
	public static final double DOUBLE_TOLERANCE = 0.0000000001;

	private ArrayList<Node> nodes;
	private ArrayList<Node> fromCentroids;
	//	private ArrayList<Node> toCentroids;
	private ArrayList<Edge> edges;
	private Result result;
	private MulticommodityFlowThreads mcf;
	private Demand[][] demandsMatrix;
	private ArrayList<Demand> demandsList;

	public Graph(String demandFileName) throws FileNotFoundException {
		result = new Result(this);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.fromCentroids = new ArrayList<Node>();
		//		this.toCentroids = new ArrayList<Node>();
		Node.setNoOfCentroids(Data.getPortsMap().size());
		readDemands(demandFileName);
		createCentroids();
		createOmissionEdges();
		mcf = new MulticommodityFlowThreads(this);
	}

	public Graph(Rotation rotation) {
		result = new Result(this);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.fromCentroids = new ArrayList<Node>();
		//		this.toCentroids = new ArrayList<Node>();
		Node.setNoOfCentroids(Data.getPortsMap().size());
		createDemands(rotation);
		createCentroids();
		createOmissionEdges();
		Rotation subRotation = createRotation(rotation);
		rotation.setSubRotation(subRotation);
		mcf = new MulticommodityFlowThreads(this);
	}

	public void runMcf() throws InterruptedException{
		mcf.run();
	}

	public void readDemands(String demandFileName) throws FileNotFoundException{
		demandsList = ReadData.readDemands(demandFileName, Data.getPortsMap());
		demandsMatrix = createDemandsMatrix();
	}

	private void createDemands(Rotation rotation){
		ArrayList<Route> orgRoutes = new ArrayList<Route>();
		demandsList = new ArrayList<Demand>();
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				for(Route r : e.getRoutes()){
					if(!orgRoutes.contains(r)){
						orgRoutes.add(r);
					}
				}
			}
		}
		for(Route r : orgRoutes){
			Demand d = r.getDemand();
			if(!demandsList.contains(d)){
				demandsList.add(d);
			}
		}
		demandsMatrix = createDemandsMatrix();
	}

	private Demand[][] createDemandsMatrix(){
		Demand[][] demands = new Demand[Data.getPortsMap().size()][Data.getPortsMap().size()];
		for(Demand d : demandsList){
			int fromPortId = d.getOrigin().getPortId();
			int toPortId = d.getDestination().getPortId();
			demands[fromPortId][toPortId] = d;
		}
		return demands;
	}

	private void createCentroids(){
		for(Port i : Data.getPortsMap().values()){
			if(i.isActive()){
				Node fromCentroid = new Node(i, true);
				Node toCentroid = new Node(i, false);
				fromCentroids.add(fromCentroid);
				nodes.add(fromCentroid);
				nodes.add(toCentroid);
			}
		}
	}

	private void createOmissionEdges(){
		for(Demand i : getDemands()){
			Node fromCentroid = i.getOrigin().getFromCentroidNode();
			Node toCentroid = i.getDestination().getToCentroidNode();
			Edge newOmissionEdge = new Edge(fromCentroid, toCentroid, i.getRate());
			edges.add(newOmissionEdge);
		}
	}

	public Rotation createRotation(ArrayList<DistanceElement> distances, VesselClass vesselClass){
		Rotation rotation = new Rotation(vesselClass, this);
		createRotationEdges(distances, rotation, vesselClass);
		createLoadUnloadEdges(rotation);
		createTransshipmentEdges(rotation);
		rotation.calcOptimalSpeed();
		result.addRotation(rotation);
		return rotation;
	}

	public Rotation createRotation(Rotation rotation){
		ArrayList<Integer> ports = new ArrayList<Integer>();
		for(Node i : rotation.getRotationNodes()){
			if(i.isDeparture()){
				ports.add(i.getId());
			}
		}
		Rotation newRotation = createRotationFromPorts(ports, rotation.getVesselClass());
		createFeederEdges(rotation);
		return newRotation;
	}

	private void createFeederEdges(Rotation rotation){
		ArrayList<Route> orgRoutes = new ArrayList<Route>();
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				for(Route r : e.getRoutes()){
					if(!orgRoutes.contains(r)){
						orgRoutes.add(r);
					}
				}
			}
		}
		for(Route r : orgRoutes){
			Node fromNode = null;
			Node toNode = null;
			for(Edge e : r.getRoute()){
				if(e.isTransshipment()){
					if(e.getToNode().getRotation().equals(rotation)){
						toNode = e.getToNode();
						createFeederEdge(fromNode, toNode, rotation);
						fromNode = null;
						toNode = null;
					} else if(e.getFromNode().getRotation().equals(rotation)){
						fromNode = e.getFromNode();
					}

				} else if(e.isLoadUnload()){
					if(e.getToNode().isToCentroid()){
						toNode = e.getToNode();
						createFeederEdge(fromNode, toNode, rotation);
					} else if(e.getFromNode().isFromCentroid()){
						fromNode = e.getFromNode();
					}
				}
			}
		}
	}

	private Edge createFeederEdge(Node fromNode, Node toNode, Rotation rotation){
		int cost = computeFeederCost(fromNode, toNode, rotation);
		Edge newEdge = new Edge(fromNode, toNode, cost, Integer.MAX_VALUE, false, true, null, -1, null);
		edges.add(newEdge);
		return newEdge;
	}
	

	private int computeFeederCost(Node fromNode, Node toNode, Rotation rotation){
		VesselClass v = rotation.getVesselClass();
		DistanceElement distance = Data.getBestDistanceElement(fromNode.getPort(), toNode.getPort(), v);
		int panamaCost = 0;
		if(distance.isPanama()){
			panamaCost = v.getPanamaFee();
		}
		int suezCost = 0;
		if(distance.isSuez()){
			suezCost = v.getSuezFee();
		}
		double sailTimeDays = (distance.getDistance() / v.getDesignSpeed()) / 24.0;
		double fuelConsumptionSail = sailTimeDays * v.getFuelConsumptionDesign();
		double fuelConsumptionPort = v.getFuelConsumptionIdle();
		int fuelCost = (int) (600 * (fuelConsumptionSail + fuelConsumptionPort));
		int portCostFrom = fromNode.getPort().getFixedCallCost() + fromNode.getPort().getVarCallCost() * v.getCapacity();
		int portCostTo = toNode.getPort().getFixedCallCost() + toNode.getPort().getVarCallCost() * v.getCapacity();
		int TCCost = (int) (v.getTCRate() * sailTimeDays);
		int totalCost = panamaCost + suezCost + fuelCost + portCostFrom + portCostTo + TCCost;
		int transferCost = 0;
		if(fromNode.isArrival()){
			transferCost += fromNode.getPort().getTransshipCost();
		}
		if(toNode.isDeparture()){
			transferCost += toNode.getPort().getTransshipCost();
		}
		int avgCost = totalCost / v.getCapacity() + transferCost;

		return avgCost;
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
			distances.add(Data.getBestDistanceElement(port1, port2, vesselClass));
		}
		int lastPort = ports.get(ports.size()-1);
		int firstPort = ports.get(0);
		distances.add(Data.getBestDistanceElement(lastPort, firstPort, vesselClass));
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

	//TODO: ERROR? Method does not support transshipment within rotation.
	private void createTransshipmentEdges(ArrayList<Node> rotationNodes, Rotation rotation){
		for(Node i : rotationNodes){
			Port p = i.getPort();
			if(i.isDeparture()){
				for(Node j : p.getArrivalNodes()){
					if(!j.getRotation().equals(rotation)){
//					if(!j.getNextEdge().equals(i.getPrevEdge())){
						createTransshipmentEdge(j, i);
					}
				}
			} else {
				for(Node j : p.getDepartureNodes()){
					if(!j.getRotation().equals(rotation)){
//					if(!i.getNextEdge().equals(j.getPrevEdge())){
						createTransshipmentEdge(i, j);
					}
				}
			}
		}
	}

	private void createTransshipmentEdge(Node fromNode, Node toNode){
		int transshipCost = fromNode.getPort().getTransshipCost();
		Edge newEdge = new Edge(fromNode, toNode, transshipCost, Integer.MAX_VALUE, false, false, null, -1, null);
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
		Edge newEdge = new Edge(fromNode, toNode, cost, capacity, true, false, rotation, noInRotation, distance);
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
		DistanceElement newIngoing = Data.getBestDistanceElement(fromNode.getPort(), newArrNode.getPort(), r.getVesselClass());
		DistanceElement newOutgoing = Data.getBestDistanceElement(newDepNode.getPort(), toNode.getPort(), r.getVesselClass());
		createRotationEdge(r, fromNode, newArrNode, 0, r.getVesselClass().getCapacity(), e.getNoInRotation(), newIngoing);
		Edge dwell = createRotationEdge(r, newArrNode, newDepNode, 0, r.getVesselClass().getCapacity(), -1, null);
		createRotationEdge(r, newDepNode, toNode, 0, r.getVesselClass().getCapacity(), e.getNoInRotation()+1, newOutgoing);
		createTransshipmentEdges(dwell);
		createLoadUnloadEdges(dwell);
		r.calcOptimalSpeed();
	}

	public Edge removePort(Edge dwell){
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
		Port prevPort = ingoingEdge.getFromNode().getPort();
		Port nextPort = outgoingEdge.getToNode().getPort();
		if(prevPort.equals(nextPort) && r.getNoOfPortStays() > 1){
			Edge newDwell = null;
			for(Edge e : outgoingEdge.getToNode().getOutgoingEdges()){
				if(e.isDwell()){
					newDwell = e;
					break;
				}
			}
			outgoingEdge = newDwell.getNextEdge();
			r.decrementNoInRotation(outgoingEdge.getNoInRotation());
			deleteNode(newDwell.getFromNode());
			deleteNode(newDwell.getToNode());
			nextPort = outgoingEdge.getToNode().getPort();
		}
		deleteNode(fromNode);
		deleteNode(toNode);
		fromNode = ingoingEdge.getFromNode();
		toNode = outgoingEdge.getToNode();
		Edge newSailEdge = null;
		if(fromNode.getPort().equals(toNode.getPort())){
			System.err.println("Rotation dying");
			deleteNode(fromNode);
			deleteNode(toNode);
			r.delete();
			result.removeRotation(r);

		} else {
			DistanceElement distance = Data.getBestDistanceElement(fromNode.getPort(), toNode.getPort(), r.getVesselClass());
			newSailEdge = createRotationEdge(r, fromNode, toNode, 0, r.getVesselClass().getCapacity(), ingoingEdge.getNoInRotation(), distance);
			r.calcOptimalSpeed();
		}
		return newSailEdge;
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
		decrementNodeIds(i.getId());
		nodes.remove(i);
	}

	public void deleteRotation(Rotation rotation){
		for(int i = rotation.getRotationEdges().size()-1; i >= 0; i--){
			deleteNode(rotation.getRotationNodes().get(i));
		}
		rotation.delete();
	}

	private void decrementNodeIds(int id){
		for(Node i : nodes){
			if(i.getId() > id){
				i.decrementId();
			}
		}
		Node.decrementIdCounter();
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
		Edge newEdge = new Edge(fromNode, toNode, loadUnloadCost, Integer.MAX_VALUE, false, false, null, -1, null);
		edges.add(newEdge);
	}

	public void saveOPLData(String fileName){
		try {
			int noOfNodes = nodes.size();
			int noOfDemands = getDemands().size();
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
				Demand d = getDemands().get(i);
				demandFrom[i] = d.getOrigin().getFromCentroidNode().getId();
				demandTo[i] = d.getDestination().getToCentroidNode().getId();
				demand[i] = d.getDemand();

			}
			File fileOut = new File(fileName);
			File fileOutLegend = new File("legendOPLdata.csv");
			BufferedWriter out;
			BufferedWriter outLegend;

			out = new BufferedWriter(new FileWriter(fileOut));
			outLegend = new BufferedWriter(new FileWriter(fileOutLegend));

			outLegend.write("NodeId;Port;RotationId;Centroid");
			for(Node i : nodes){
				outLegend.newLine();
				outLegend.write(i.getId()+";"+i.getPort().getUNLocode()+";");
				if(i.isFromCentroid() || i.isToCentroid()){
					outLegend.write("-1;1");
				} else {
					outLegend.write(i.getRotation().getId()+";0");
				}
			}
			outLegend.close();

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

	/**
	 * @return the fromCentroids
	 */
	public ArrayList<Node> getFromCentroids() {
		return fromCentroids;
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public Port getPort(int portId){
		return Data.getPort(portId);
	}

	public Result getResult(){
		return result;
	}

	public ArrayList<Demand> getDemands() {
		return demandsList;
	}

	/**
	 * @return the demandsMatrix
	 */
	public Demand[][] getDemandsMatrix() {
		return demandsMatrix;
	}

	public Demand[] getFromDemandArray(int fromPortId){
		return demandsMatrix[fromPortId];
	}

	public Demand getDemand(int fromPortId, int toPortId){
		return demandsMatrix[fromPortId][toPortId];
	}

	public Demand getDemand(Port fromPort, Port toPort){
		int fromPortId = fromPort.getPortId();
		int toPortId = toPort.getPortId();
		return getDemand(fromPortId, toPortId);
	}

	/**
	 * @return the mcf
	 */
	public MulticommodityFlowThreads getMcf() {
		return mcf;
	}
	
	public ArrayList<Edge> tryRemovePort(Edge dwellEdge, Rotation r){
		if(!dwellEdge.isDwell()){
			throw new RuntimeException("Input mismatch.");
		}
		ArrayList<Edge> handledEdges = new ArrayList<Edge>();
		handledEdges.add(dwellEdge);
		handledEdges.add(dwellEdge.getPrevEdge());
		handledEdges.add(dwellEdge.getNextEdge());
		Node prevNode = dwellEdge.getPrevEdge().getFromNode();
		Node nextNode = dwellEdge.getNextEdge().getToNode();
		if(prevNode.getPort().equals(nextNode.getPort())){
			Edge nextDwell = nextNode.getNextEdge();
			handledEdges.add(nextDwell);
			Edge nextSail = nextDwell.getNextEdge();
			handledEdges.add(nextSail);
			nextNode = nextSail.getToNode();
		}
		Edge newEdge = createRotationEdge(r, prevNode, nextNode, 0, dwellEdge.getCapacity(), -1, Data.getBestDistanceElement(prevNode.getPortId(), nextNode.getPortId(), r.getVesselClass()));
		for(Edge e : handledEdges){
			e.setInactive();
		}
		handledEdges.add(0, newEdge);
		r.calcOptimalSpeed();
		return handledEdges;
	}
	
	public void undoTryRemovePort(ArrayList<Edge> handledEdges, Rotation r) {
		for(int i = 1; i < handledEdges.size(); i++){
			handledEdges.get(i).setActive();
		}
		handledEdges.get(0).delete();
		r.calcOptimalSpeed();
	}
	
}
