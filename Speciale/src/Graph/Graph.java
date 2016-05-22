package Graph;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.plaf.synth.SynthSpinnerUI;

import Data.Data;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
import Data.PortData;
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
	private int[] noVesselsAvailable;
	private int[] noVesselsUsed;
	private Port[] ports;
	private boolean subGraph;

	public Graph(String demandFileName) throws FileNotFoundException {
		result = new Result(this);
		setNoVessels();
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.fromCentroids = new ArrayList<Node>();
		this.subGraph = false;
		//		this.toCentroids = new ArrayList<Node>();
		Node.setNoOfCentroids(Data.getPortsMap().size());
		createPorts();
		readDemands(demandFileName);
		createCentroids();
		createOmissionEdges();
		mcf = new MulticommodityFlowThreads(this);
	}

	public Graph(Rotation rotation) {
		result = new Result(this);
		setNoVessels();
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		this.fromCentroids = new ArrayList<Node>();
		this.subGraph = true;
		//		this.toCentroids = new ArrayList<Node>();
		Node.setNoOfCentroids(Data.getPortsMap().size());
		createPorts();
		createDemands(rotation);
		createCentroids();
		createOmissionEdges();
		Rotation subRotation = createRotation(rotation);
		rotation.setSubRotation(subRotation);
		mcf = new MulticommodityFlowThreads(this);
	}
	
	public Graph(Graph copyGraph){
		result = new Result(this);
		result.copyRotations(copyGraph.getResult());
		
		noVesselsAvailable = new int[copyGraph.noVesselsAvailable.length];
		for(int i=0; i<copyGraph.noVesselsAvailable.length; i++){
			noVesselsAvailable[i] = copyGraph.noVesselsAvailable[i];
		}
		noVesselsUsed = new int[copyGraph.noVesselsUsed.length];
		for(int i=0; i<copyGraph.noVesselsUsed.length; i++){
			noVesselsUsed[i] = copyGraph.noVesselsUsed[i];
		}
		
		nodes = new ArrayList<Node>();
		nodes.addAll(copyGraph.nodes);
		
		edges = new ArrayList<Edge>();
		edges.addAll(copyGraph.edges);
		
		fromCentroids = new ArrayList<Node>();
		fromCentroids.addAll(copyGraph.fromCentroids);
		subGraph = false;
		Node.setNoOfCentroids(Data.getPortsMap().size());
		
		ports = new Port[copyGraph.getPorts().length];
		for(int i=0; i<copyGraph.getPorts().length; i++){
			ports[i] = copyGraph.ports[i];
		}
		
		demandsList = new ArrayList<Demand>(copyGraph.getDemands().size());
		demandsList.addAll(copyGraph.demandsList);
		
		demandsMatrix = createDemandsMatrix();
//		createPorts();
//		readDemands(demandFileName);
//		createCentroids();
//		createOmissionEdges();
		mcf = new MulticommodityFlowThreads(this);
	}
	

	private void setNoVessels(){
		this.noVesselsAvailable = new int[Data.getVesselClasses().size()];
		this.noVesselsUsed = new int[Data.getVesselClasses().size()];
		for(int i = 0; i < noVesselsAvailable.length; i++){
			noVesselsAvailable[i] = Data.getVesselClassId(i).getNoAvailable();
			noVesselsUsed[i] = 0;
		}
	}

	private void createPorts(){
		int size = Data.getPorts().length;
		ports = new Port[size];
		for(PortData pd : Data.getPorts()){
			Port p = new Port(pd);
			ports[p.getPortId()] = p;
		}
	}

	public void runMcf() throws InterruptedException{
		mcf.run();
	}

	public void readDemands(String demandFileName) throws FileNotFoundException{
		demandsList = ReadData.readDemands(demandFileName, Data.getPortsMap(), ports);
		demandsMatrix = createDemandsMatrix();
	}

	private void createDemands(Rotation rotation){
		ArrayList<Route> orgRoutes = new ArrayList<Route>();
		ArrayList<Demand> orgDemands = new ArrayList<Demand>();
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
		int[][] demands = new int[Data.getPorts().length][Data.getPorts().length];
		for(Route r : orgRoutes){
			Demand d = r.getDemand();
			if(!orgDemands.contains(d)){
				orgDemands.add(d);
			}
			demands[d.getOrigin().getPortId()][d.getDestination().getPortId()] += r.getFFE();
		}
		for(Demand d : orgDemands){
			Port origin = ports[d.getOrigin().getPortId()];
			Port destination = ports[d.getDestination().getPortId()];
			Demand newDemand = new Demand(d, origin, destination, demands[origin.getPortId()][destination.getPortId()]);
			demandsList.add(newDemand);
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

	public void addDemand(Demand d){
		int fromPortId = d.getOrigin().getPortId();
		int toPortId = d.getDestination().getPortId();
		demandsMatrix[fromPortId][toPortId] = d;
		demandsList.add(d);
		mcf.setBFActive(fromPortId);
		createOmissionEdge(d);
	}

	public void removeDemand(Demand d){
		int fromPortId = d.getOrigin().getPortId();
		int toPortId = d.getDestination().getPortId();
		demandsMatrix[fromPortId][toPortId] = null;
		demandsList.remove(d);
		Node fromCentroid = getPort(fromPortId).getFromCentroidNode();
		for(Edge e : fromCentroid.getOutgoingEdges()){
			if(e.getToNode().getPortId() == toPortId){
				deleteEdge(e);
				break;
			}
		}
	}

	private void createCentroids(){
		for(Port p : ports){
			//			if(p.isActive()){
			Node fromCentroid = new Node(p, true);
			Node toCentroid = new Node(p, false);
			fromCentroids.add(fromCentroid);
			nodes.add(fromCentroid);
			nodes.add(toCentroid);
			//			}
		}
	}

	private void createOmissionEdges(){
		for(Demand d : getDemands()){
			Node fromCentroid = d.getOrigin().getFromCentroidNode();
			Node toCentroid = d.getDestination().getToCentroidNode();
			Edge newOmissionEdge = new Edge(fromCentroid, toCentroid, d.getRate());
			edges.add(newOmissionEdge);
		}
	}

	private void createOmissionEdge(Demand d){
		Node fromCentroid = d.getOrigin().getFromCentroidNode();
		Node toCentroid = d.getDestination().getToCentroidNode();
		for(Edge e : fromCentroid.getOutgoingEdges()){
			if(e.isOmission()){
				if(e.getToNode().equals(toCentroid)){
					return;
				}
			}
		}
		Edge newOmissionEdge = new Edge(fromCentroid, toCentroid, d.getRate());
		edges.add(newOmissionEdge);
	}

	public Rotation createRotation(ArrayList<DistanceElement> distances, VesselClass vesselClass){
		Rotation rotation = new Rotation(vesselClass, this, -1);
		createRotationEdges(distances, rotation, vesselClass);
		createLoadUnloadEdges(rotation);
		createTransshipmentEdges(rotation);
		rotation.calcOptimalSpeed();
		return rotation;
	}

	public Rotation createRotation(ArrayList<DistanceElement> distances, VesselClass vesselClass, int id){
		Rotation rotation = new Rotation(vesselClass, this, id);
		createRotationEdges(distances, rotation, vesselClass);
		createLoadUnloadEdges(rotation);
		createTransshipmentEdges(rotation);
		rotation.calcOptimalSpeed();
		return rotation;
	}

	public Rotation createRotation(Rotation rotation){
		ArrayList<Integer> ports = new ArrayList<Integer>();
		Edge firstEdge = null;
		for(Edge e : rotation.getRotationEdges()){
			if(e.getNoInRotation() == 0){
				firstEdge = e;
				break;
			}
		}
		ports.add(firstEdge.getFromNode().getPortId());
		Edge e = firstEdge.getNextEdge().getNextEdge();
		while(e.getNoInRotation() != 0){
			ports.add(e.getFromNode().getPortId());
			e = e.getNextEdge().getNextEdge();
		}
		Rotation newRotation = createRotationFromPorts(ports, rotation.getVesselClass(), rotation.getId());
		//		System.out.println("Creating feeder edges for " + newRotation.getId());
		createFeederEdges(rotation, newRotation);
		return newRotation;
	}

	private void createFeederEdges(Rotation oldRotation, Rotation newRotation){
		ArrayList<Route> orgRoutes = new ArrayList<Route>();
		for(Edge e : oldRotation.getRotationEdges()){
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
			int lowestFreeCap = Integer.MAX_VALUE-r.getFFE()-1;
			for(Edge e : r.getRoute()){
				int freeCap = e.getCapacity()-e.getLoad();
				if(e.isSail() && !e.getRotation().equals(oldRotation) && freeCap < lowestFreeCap){
					lowestFreeCap = freeCap;
				}
				if(e.isTransshipment()){
					if(e.getToNode().getRotation().equals(oldRotation)){
						toNode = e.getToNode();
						createFeederEdge(fromNode, toNode, newRotation, lowestFreeCap, r.getFFE());
						lowestFreeCap = Integer.MAX_VALUE-r.getFFE()-1;
						fromNode = null;
						toNode = null;
					} else if(e.getFromNode().getRotation().equals(oldRotation)){
						fromNode = e.getFromNode();
					}

				} else if(e.isLoadUnload()){
					if(e.getToNode().isToCentroid()){
						toNode = e.getToNode();
						if(!e.getFromNode().getRotation().equals(oldRotation)){
							createFeederEdge(fromNode, toNode, newRotation, lowestFreeCap, r.getFFE());
//							lowestFreeCap = Integer.MAX_VALUE-r.getFFE()-1;
						}
					} else if(e.getFromNode().isFromCentroid()){
						fromNode = e.getFromNode();
					}
				} 
			}
		}
	}

	private void createFeederEdge(Node oldFromNode, Node oldToNode, Rotation rotation, int freeCap, int routeFFE){
		Edge feeder = null;
		if(!oldFromNode.getPort().equals(oldToNode.getPort())){
			int fromPortId = oldFromNode.getPortId();
			int toPortId = oldToNode.getPortId();
			Port fromPort =  ports[fromPortId];
			Port toPort = ports[toPortId];
			if(!oldFromNode.isActive() || !oldToNode.isActive()){
				return;
			}
			Node fromNode = null;
			Node toNode = null;
			if(oldFromNode.isFromCentroid()){
				fromNode = fromPort.getFromCentroidNode();
			} else {
				if(oldFromNode.isArrival()){
					for(Node i : fromPort.getArrivalNodes()){
						if(i.isEqualTo(oldFromNode)){
							fromNode = i;
						}
					}
				} else {
					for(Node i : fromPort.getDepartureNodes()){
						if(i.isEqualTo(oldFromNode)){
							fromNode = i;
						}
					}
				}
			}
			if(oldToNode.isToCentroid()){
				toNode = toPort.getToCentroidNode();
			} else {
				if(oldToNode.isArrival()){
					for(Node i : toPort.getArrivalNodes()){
						if(i.isEqualTo(oldToNode)){
							toNode = i;
						}
					}
				} else {
					for(Node i : toPort.getDepartureNodes()){
						if(i.isEqualTo(oldToNode)){
							toNode = i;
						} 
					}
				}
			}
			feeder = fromNode.getFeeder(toNode);
			if(feeder == null){
				int cost = computeFeederCost(fromNode, toNode, rotation);
				feeder = new Edge(fromNode, toNode, cost, (freeCap+routeFFE), false, true, null, -1, null);
				edges.add(feeder);
				if(fromNode.isFromCentroid()){
					for(Node n : rotation.getRotationNodes()){
						if(!n.equals(toNode) && n.getPortId() == toNode.getPortId() && n.isDeparture()){
							feeder = fromNode.getFeeder(n);
							if(feeder == null){
								feeder = new Edge(fromNode, n, cost, (freeCap+routeFFE), false, true, null, -1, null);
								edges.add(feeder);
							} else {
								feeder.addCapacity(routeFFE);
							}
						}
					}
				} else if(toNode.isToCentroid()){
					for(Node n : rotation.getRotationNodes()){
						if(!n.equals(fromNode) && n.getPortId() == fromNode.getPortId() && n.isArrival()){
							feeder = n.getFeeder(toNode);
							if(feeder == null){
								feeder = new Edge(n, toNode, cost, (freeCap+routeFFE), false, true, null, -1, null);
								edges.add(feeder);
							} else {
								feeder.addCapacity(routeFFE);
							}
							
						}
					}
				}
			} else {
				feeder.addCapacity(routeFFE);
			}
		}
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
		int fuelCost = (int) (Data.getFuelPrice() * (fuelConsumptionSail + fuelConsumptionPort));
		int portCostFrom = fromNode.getPort().getFixedCallCost() + fromNode.getPort().getVarCallCost() * v.getCapacity();
		int portCostTo = toNode.getPort().getFixedCallCost() + toNode.getPort().getVarCallCost() * v.getCapacity();
		int TCCost = (int) (v.getTCRate() * sailTimeDays);
		int totalCost = panamaCost + suezCost + fuelCost + portCostFrom + portCostTo + TCCost;
		int portHandlingCost = 0;
		if(fromNode.isArrival()){
			portHandlingCost += fromNode.getPort().getTransshipCost();
		} else {
			portHandlingCost += fromNode.getPort().getMoveCost();
		}
		if(toNode.isDeparture()){
			portHandlingCost += toNode.getPort().getTransshipCost();
		} else {
			portHandlingCost += toNode.getPort().getMoveCost();
		}
		int avgCost = totalCost / v.getCapacity() + portHandlingCost;

		return avgCost;
	}
	
	public Rotation createRotationFromPorts(ArrayList<Integer> ports, VesselClass vesselClass, int id){
		ArrayList<DistanceElement> distances = findDistances(ports, vesselClass);
		Rotation rotation = createRotation(distances, vesselClass, id);
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

	private ArrayList<Edge> createTransshipmentEdges(Rotation rotation){
		ArrayList<Node> rotationNodes = rotation.getRotationNodes();
		ArrayList<Edge> newEdges = createTransshipmentEdges(rotationNodes, rotation);
		return newEdges;
	}

	public ArrayList<Edge> createTransshipmentEdges(Edge e){
		ArrayList<Node> rotationNodes = new ArrayList<Node>();
		rotationNodes.add(e.getFromNode());
		rotationNodes.add(e.getToNode());
		ArrayList<Edge> newEdges = createTransshipmentEdges(rotationNodes, e.getRotation());
		return newEdges;
	}

	//TODO: ERROR? Method does not support transshipment within rotation.
	private ArrayList<Edge> createTransshipmentEdges(ArrayList<Node> rotationNodes, Rotation rotation){
		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		Edge newEdge = null;
		for(Node i : rotationNodes){
			Port p = i.getPort();
			if(i.isDeparture()){
				for(Node j : p.getArrivalNodes()){
					if(!j.getRotation().equals(rotation)){
						newEdge = createTransshipmentEdge(j, i);
						newEdges.add(newEdge);
					}
				}
			} else {
				for(Node j : p.getDepartureNodes()){
					if(!j.getRotation().equals(rotation)){
						newEdge = createTransshipmentEdge(i, j);
						newEdges.add(newEdge);
					}
				}
			}
		}
		return newEdges;
	}

	private Edge createTransshipmentEdge(Node fromNode, Node toNode){
		int transshipCost = fromNode.getPort().getTransshipCost();
		Edge newEdge = new Edge(fromNode, toNode, transshipCost, Integer.MAX_VALUE, false, false, null, -1, null);
		edges.add(newEdge);
		return newEdge;
	}

	private void createRotationEdges(ArrayList<DistanceElement> distances, Rotation rotation, VesselClass vesselClass){
		checkDistances(distances, vesselClass);
		//Rotation opened at port 0 outside of for loop.
		DistanceElement currDist;
		PortData firstPortData = distances.get(0).getOrigin();
		Port firstPort = ports[firstPortData.getPortId()];
		Port depPort = firstPort;
		PortData arrPortData;
		Port arrPort;
		Node firstNode = createRotationNode(depPort, rotation, true);
		Node depNode = firstNode;
		Node arrNode;
		for(int i = 0; i < distances.size()-1; i++){
			currDist = distances.get(i);
			arrPortData = currDist.getDestination();
			arrPort = ports[arrPortData.getPortId()];
			arrNode = createRotationNode(arrPort, rotation, false);
			createRotationEdge(rotation, depNode, arrNode, 0, vesselClass.getCapacity(), i, currDist);
			depPort = arrPort;
			depNode = createRotationNode(depPort, rotation, true);
			createRotationEdge(rotation, arrNode, depNode, 0, vesselClass.getCapacity(), -1, null);
		}
		//Rotation closed at port 0 outside of for loop.
		currDist = distances.get(distances.size()-1);
		arrPortData = currDist.getDestination();
		arrPort = ports[arrPortData.getPortId()];
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


	/** Insert a port by replacing an edge 
	 * @param r
	 * @param e
	 * @param p
	 */
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

	/** Insert a port by adding a detour from a port to the new port and then back again.
	 * @param r
	 */
	public void insertPort(Rotation r){

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
		if(prevPort.equals(nextPort) && r.getNoOfPortStays() > 2){
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
		Port nodePort = i.getPort();
		nodePort.getArrivalNodes().remove(i);
		nodePort.getDepartureNodes().remove(i);
		nodes.remove(i);
		i.setInactive();
	}

	public void deleteRotNode(Node i){
		//		ArrayList<Edge> ingoingEdges = i.getIngoingEdges();
		//		for(int j = ingoingEdges.size()-1; j >= 0; j--){
		//			Edge e = ingoingEdges.remove(j);
		//			deleteEdge(e);
		//		}
		//		ArrayList<Edge> outgoingEdges = i.getOutgoingEdges();
		//		for(int j = outgoingEdges.size()-1; j >= 0; j--){
		//			Edge e = outgoingEdges.remove(j);
		//			deleteEdge(e);
		//		}
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
		PortData firstPort = distances.get(0).getOrigin();
		for(int i = 1; i < distances.size(); i++){
			PortData portA = distances.get(i-1).getDestination();
			PortData portB = distances.get(i).getOrigin();
			if(portA.getPortId() != portB.getPortId()){
				throw new RuntimeException("The distances are not compatible.");
			}
			if(portA.getDraft() < vesselClass.getDraft()){
				throw new RuntimeException("The draft at " + portA.getUNLocode() + " is exceeded.");
			}
		}
		PortData lastPort = distances.get(distances.size()-1).getDestination();
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

	private ArrayList<Edge> createLoadUnloadEdges(Rotation rotation){
		ArrayList<Edge> newEdges = createLoadUnloadEdges(rotation.getRotationNodes(), rotation);
		return newEdges;
	}

	public ArrayList<Edge> createLoadUnloadEdges(Edge edge){
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(edge.getFromNode());
		nodes.add(edge.getToNode());
		ArrayList<Edge> newEdges = createLoadUnloadEdges(nodes, edge.getRotation());
		return newEdges;
	}

	private ArrayList<Edge> createLoadUnloadEdges(ArrayList<Node> rotationNodes, Rotation rotation){
		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		Edge newEdge = null;
		for(Node i : rotationNodes){
			if(i.isArrival()){
				newEdge = createLoadUnloadEdge(i, i.getPort().getToCentroidNode());
				newEdges.add(newEdge);
			} else if(i.isDeparture()){
				newEdge = createLoadUnloadEdge(i.getPort().getFromCentroidNode(), i);
				newEdges.add(newEdge);
			} else {
				throw new RuntimeException("Tried to create load/unload edge that does not match definition.");
			}
		}
		return newEdges;
	}

	public Edge createLoadUnloadEdge(Node fromNode, Node toNode){
		int loadUnloadCost = fromNode.getPort().getMoveCost();
		Edge newEdge = new Edge(fromNode, toNode, loadUnloadCost, Integer.MAX_VALUE, false, false, null, -1, null);
		edges.add(newEdge);
		return newEdge;
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
		return ports[portId];
	}

	public Port getPort(String UNLocode){
		for(Port p : ports){
			if(p.getUNLocode().equals(UNLocode)){
				return p;
			}
		}
		return null;
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

	//	public ArrayList<Edge> tryInsertPort(Rotation r, Edge nextSailEdge, Port feederPort){
	//		Port orgPort = nextSailEdge.getFromNode().getPort();
	////		System.out.println("Inserting " + feederPort.getUNLocode() + " on rotation " + r.getId() + " between " + feederPort.getFromPortUNLo() + " and " + feederPort.getToPortUNLo());
	//		Node fromNode = nextSailEdge.getFromNode();
	//		Node toNode = nextSailEdge.getToNode();
	////		deleteEdge(e);
	////		r.incrementNoInRotation(e.getNoInRotation());
	//		Node newFeederArrNode = createRotationNode(feederPort, r, false);
	//		Node newFeederDepNode = createRotationNode(feederPort, r, true);
	//		Node newOrgArrNode = createRotationNode(orgPort, r, false);
	//		Node newOrgDepNode = createRotationNode(orgPort, r, true);
	//		
	//		for(Edge e : edges){
	//			if(e.isSail()){
	//				capacity = e.getCapacity();
	//			}
	//			if(capacity > 0){
	//				break;
	//			}
	//		}
	//		ArrayList<Edge> handledEdges = new ArrayList<Edge>();
	//		Node fromNode = affectedEdge.getFromNode();
	//		Node toNode = affectedEdge.getToNode();
	//		createRotationEdge(r, prevNode, nextNode, 0, dwellEdge.getCapacity(), -1, Data.getBestDistanceElement(prevNode.getPortId(), nextNode.getPortId(), r.getVesselClass()));
	//		handledEdges.add(createSailEdge(fromNode, toNode, 0, affectedEdge.getCapacity(), 0));
	//		handledEdges.add(createSailEdge(toNode, fromNode, capacity, 1));
	//		affectedEdge.setInactive();
	//		return handledEdges;
	//	}
	public boolean isSubGraph(){
		return subGraph;
	}

	public void removeNoUsed(VesselClass vesselClass, int noUsed){
		int id = vesselClass.getId();
		noVesselsUsed[id] -= noUsed;
		if(noVesselsUsed[id] < 0){
			throw new RuntimeException("Negative number of vessels used for vessel class " + vesselClass);
		}
	}

	public void addNoUsed(VesselClass vesselClass, int noUsed){
		int id = vesselClass.getId();
		noVesselsUsed[id] += noUsed;
		if(noVesselsUsed[id] > noVesselsAvailable[id]){
			throw new RuntimeException("Using more vessels " + noVesselsUsed[id] + ">" + noVesselsAvailable[id] + " than are available for vessel class " + vesselClass);
		}
	}
	//	public ArrayList<RotationEdge> tryInsertPort(Rotation r, Edge feederEdge){
	//		Edge dwellEdge = feederEdge.getFromNode().getNextEdge();
	//		
	////		for(Edge e : edges){
	////			if(e.isSail()){
	////				capacity = e.getCapacity();
	////			}
	////			if(capacity > 0){
	////				break;
	////			}
	////		}
	//		ArrayList<RotationEdge> handledEdges = new ArrayList<RotationEdge>();
	//		Node fromNode = affectedEdge.getFromNode();
	//		Node toNode = affectedEdge.getToNode();
	//		createRotationEdge(r, prevNode, nextNode, 0, dwellEdge.getCapacity(), -1, Data.getBestDistanceElement(prevNode.getPortId(), nextNode.getPortId(), r.getVesselClass()));
	//		handledEdges.add(createSailEdge(fromNode, toNode, 0, affectedEdge.getCapacity(), 0));
	//		handledEdges.add(createSailEdge(toNode, fromNode, capacity, 1));
	//		affectedEdge.setInactive();
	//		return handledEdges;
	//	}

	//	public void undoTryInsertPort(Edge feederEdge, ArrayList<Edge> newSailEdges){
	//		feederEdge.setActive();
	//		for(int i = newSailEdges.size()-1; i >= 0; i--){
	//			newSailEdges.get(i).delete();
	//		}
	//	}
	//
	//	public void undoTryInsertPort(Edge feederEdge, ArrayList<Edge> newSailEdges){
	//		feederEdge.setActive();
	//		for(int i = newSailEdges.size()-1; i >= 0; i--){
	//			newSailEdges.get(i).delete();
	//		}
	//	}

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
		Edge newEdge = createRotationEdge(r, prevNode, nextNode, 0, dwellEdge.getCapacity(), dwellEdge.getPrevEdge().getNoInRotation(), Data.getBestDistanceElement(prevNode.getPortId(), nextNode.getPortId(), r.getVesselClass()));
		for(Edge e : handledEdges){
			e.setInactive();
		}
		System.out.println();
//		for(Edge e : r.getRotationEdges()){
//		}
		//		for(Edge e : edges){
		//			if(e.isActive() && e.isSail() && e.getRotation().getId() == 0){
		//				System.out.println(e.simplePrint());
		//			}
		//		}
		handledEdges.add(0, newEdge);
		r.calcOptimalSpeed();
		return handledEdges;
	}

	public void undoTryRemovePort(ArrayList<Edge> handledEdges, Rotation r) {
		for(int i = 1; i < handledEdges.size(); i++){
			handledEdges.get(i).setActive();
		}
		this.deleteEdge(handledEdges.get(0));
		r.calcOptimalSpeed();
	}

	public ArrayList<Node> tryInsertMakeNodes(Rotation r, Port orgPort, Port feederPort, Edge nextSailEdge) {
		ArrayList<Node> insertNodes = new ArrayList<Node>(4);
		Node newFeederArrNode = createRotationNode(feederPort, r, false);
		Node newFeederDepNode = createRotationNode(feederPort, r, true);
		Node newOrgArrNode = createRotationNode(orgPort, r, false);
		Node newOrgDepNode = createRotationNode(orgPort, r, true);
		insertNodes.add(newFeederArrNode);
		insertNodes.add(newFeederDepNode);
		insertNodes.add(newOrgArrNode);
		insertNodes.add(newOrgDepNode);
		nextSailEdge.setInactive();
		return insertNodes;
	}

	public ArrayList<Edge> tryInsertMakeEdges(Rotation r, ArrayList<Node> insertNodes, Node orgDepNode, Node orgNextArrPortNode) {
		ArrayList<Edge> insertEdges = new ArrayList<Edge>();
		Node newFeederArrNode = insertNodes.get(0);
		Node newFeederDepNode = insertNodes.get(1);
		Node newOrgArrNode = insertNodes.get(2);
		Node newOrgDepNode = insertNodes.get(3);
		DistanceElement newToFeederPortDist = Data.getBestDistanceElement(orgDepNode.getPort(), newFeederArrNode.getPort(), r.getVesselClass());
		DistanceElement newFromFeederPortDist = Data.getBestDistanceElement(newFeederDepNode.getPort(), newOrgArrNode.getPort(), r.getVesselClass());
		DistanceElement newOrgSailDist = Data.getBestDistanceElement(newOrgDepNode.getPort(), orgNextArrPortNode.getPort(), r.getVesselClass());
		Edge newToFeederPort = createRotationEdge(r, orgDepNode, newFeederArrNode, 0, r.getVesselClass().getCapacity(), 0, newToFeederPortDist);
		Edge newFromFeederPort = createRotationEdge(r, newFeederDepNode, newOrgArrNode, 0, r.getVesselClass().getCapacity(), 0, newFromFeederPortDist);
		Edge newOrgSail = createRotationEdge(r, newOrgDepNode, orgNextArrPortNode, 0, r.getVesselClass().getCapacity(), 0, newOrgSailDist);
		insertEdges.add(newToFeederPort);
		insertEdges.add(newFromFeederPort);
		insertEdges.add(newOrgSail);
		Edge newFeederDwell = createRotationEdge(r, newFeederArrNode, newFeederDepNode, 0, r.getVesselClass().getCapacity(), -1, null);
		Edge newOrgDwell = createRotationEdge(r, newOrgArrNode, newOrgDepNode, 0, r.getVesselClass().getCapacity(), -1, null);
		ArrayList<Edge> transhipmentFeederPort = createTransshipmentEdges(newFeederDwell);
		insertEdges.addAll(transhipmentFeederPort);
		ArrayList<Edge> loadUnloadFeederPort = createLoadUnloadEdges(newFeederDwell);
		insertEdges.addAll(loadUnloadFeederPort);
		ArrayList<Edge> transhipmentNewOrgPort = createTransshipmentEdges(newOrgDwell);
		insertEdges.addAll(transhipmentNewOrgPort);
		ArrayList<Edge> loadUnloadNewOrgPort = createLoadUnloadEdges(newOrgDwell);
		insertEdges.addAll(loadUnloadNewOrgPort);

		return insertEdges;
	}

	public void undoTryInsertMakeNodes(ArrayList<Node> insertNodes, Edge nextSailEdge) {
		for(int i = insertNodes.size()-1; i >= 0; i--){
			this.deleteNode(insertNodes.get(i));
		}
		nextSailEdge.setActive();
	}

	public void undoTryInsertMakeEdges(ArrayList<Edge> insertEdges) {
		for(int i = insertEdges.size()-1; i >= 0; i--){
			this.deleteEdge(insertEdges.get(i));
		}
	}

	/*
	private void addUnderservicedPort(){
		int[] unservicedDemand = new int[Data.getPortsMap().size()];
		for(Demand d : demandsList){
			int portId1 = d.getOrigin().getPortId();
			int portId2 = d.getDestination().getPortId();
			int omission = d.getOmissionFFEs();
			unservicedDemand[portId1] += omission;
			unservicedDemand[portId2] += omission;
		}
		int bestPortId = -1;
		int bestFFE = -1;
		for(int i = 0; i < Data.getPortsMap().size(); i++){
			int FFE = unservicedDemand[i];
			if(FFE > bestFFE){
				bestFFE = FFE;
				bestPortId = i;
			}
		}
		if(bestPortId == -1){
			return;
		}
		ArrayList<Demand> bestDemands = new ArrayList<Demand>();
		for(Demand d : demandsList){
			if(d.getOrigin().getPortId() == bestPortId || d.getDestination().getPortId() == bestPortId){
				bestDemands.add(d);
			}
		}
		int bestNoInRotation = -1;
		int bestObj = -1;
		Rotation bestRotation;
		for(Rotation r : result.getRotations()){
			serviceOmissionDemand(bestDemands, bestRotation, bestNoInRotation, bestObj);
		}
		implementServiceOmissionDemand(bestDemands, bestRotation, bestNoInRotation);
	}
	 */

	public Port[] getPorts() {
		return ports;
	}

	public int getNoVesselsUsed(int vesselId){
		return noVesselsUsed[vesselId];
	}

	/**
	 * @return the noVesselsAvailable
	 */
	public int getNoVesselsAvailable(int vesselId) {
		return noVesselsAvailable[vesselId];
	}

	public void serviceBiggestOmissionDemand() throws InterruptedException{
		int[] portOmission = new int[Data.getPorts().length];
		for(Demand d : demandsList){
			for(Route r : d.getRoutes()){
				if(r.isOmission()){
					int origin = d.getOrigin().getPortId();
					int destination = d.getDestination().getPortId();
					portOmission[origin] += r.getFFE();
					portOmission[destination] += r.getFFE();
				}
			}
		}
		int biggestPort = -1;
		int biggestOmission = -1;
		for(int i = 0; i < portOmission.length; i++){
			if(portOmission[i] > biggestOmission){
				biggestOmission = portOmission[i];
				biggestPort = i;
			}
		}
		Port p = getPort(biggestPort);
		serviceOmissionDemand(p);
	}

	public void serviceOmissionDemand(Port port) throws InterruptedException {
		ArrayList<Demand> oldDemands = new ArrayList<Demand>();
		for(Demand d : demandsList){
			if(d.getOrigin().equals(port) || d.getDestination().equals(port)){
				int omission = d.getOmissionFFEs();
				if(omission > 0){
					oldDemands.add(d);
				}
			}
		}
		int bestObj = -Integer.MAX_VALUE;
		Rotation bestRot = null;
		int bestNoInRot = -1;
		for(Rotation rot : result.getRotations()){
			int obj = rot.serviceOmissionDemand(oldDemands, port.getPortId());
			if(obj > bestObj){
				bestObj = obj;
				bestRot = rot;
			}
		}
		if(bestRot != null){
			System.out.println("Adding " + port.getUNLocode() + " to rotation " + bestRot.getId());
			bestRot.implementServiceOmissionDemand(oldDemands, port.getPortId());
		}
	}

}
