package RotationFlow;

import java.util.ArrayList;

import Data.Data;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Results.Rotation;
import Results.Route;

public class RotationGraph {
	private RotationMulticommodityFlow multicommodityFlow;
	private RotationNode[] rotationNodes;
	private ArrayList<RotationEdge> rotationEdges;
	private ArrayList<RotationDemand> rotationDemands;
	private ArrayList<Route> orgRoutes;
	private Rotation rotation;
	private static int noOfCentroids;
	private static Graph graph;

	public RotationGraph(Rotation rotation){
		this.rotation = rotation;
		this.rotationNodes = new RotationNode[noOfCentroids];
		this.rotationEdges = new ArrayList<RotationEdge>();
		this.rotationDemands = new ArrayList<RotationDemand>();
		this.orgRoutes = new ArrayList<Route>();
		this.multicommodityFlow = new RotationMulticommodityFlow(this);
		this.createGraph();
	}
	
	public static void initialize(Graph newGraph){
		noOfCentroids = Data.getPortsMap().size();
		RotationNode.setNoOfCentroids(noOfCentroids);
		graph = newGraph;
	}

	public void findFlow(){
		multicommodityFlow.run();
		multicommodityFlow.saveODSol("ODSolRotation.csv", rotationDemands);
	}
	
	
	public boolean removeWorstPort(){
//		for(RotationEdge e : rotationEdges){
//			if(e.isFeeder()){
//				System.out.println(e.getFromPortUNLo() + "-" + e.getToPortUNLo() + " cost: " + e.getCost());
//			}
//		}
		boolean madeChange = false;
		int maxIndex = -1;
		for(RotationEdge e : rotationEdges){
			if(e.isSail() && e.isActive()){
				maxIndex = Math.max(maxIndex, e.getNoInRotation());
			}
		}
		int flowCost = getFlowCost();
		int rotationCost = getRotationCost();
		int bestRotationObj = flowCost + rotationCost;
		System.out.println("Org flowCost: " + flowCost + " & rotationCost " + rotationCost);
		RotationEdge worstInto = rotationEdges.get(maxIndex);
		RotationEdge worstOut = rotationEdges.get(0);
		ArrayList<RotationEdge> handledEdges = tryRemovePort(worstInto, worstOut);
		int rotationObj = getFlowCost() + getRotationCost();
		if(rotationObj < bestRotationObj){
			bestRotationObj = rotationObj;
			madeChange = true;
		}
		undoTryRemovePort(handledEdges);
		for(int i=0; i<maxIndex; i++){
			RotationEdge into = rotationEdges.get(i);
			RotationEdge out = rotationEdges.get(i+1);
			handledEdges = tryRemovePort(into, out);
			flowCost = getFlowCost();
			rotationCost = getRotationCost();
			rotationObj = flowCost + rotationCost;
			System.out.println("Removing port " + into.getToPortUNLo() + " with flowCost " + flowCost + " & rotationCost " + rotationCost);
			if(rotationObj < bestRotationObj){
				worstInto = into;
				worstOut = out;
				bestRotationObj = rotationObj;
				madeChange = true;
			}
			undoTryRemovePort(handledEdges);
		}
		if(madeChange){
			System.out.println("Best obj: " + bestRotationObj + " by removing port " + worstInto.getToPortUNLo() + " noInRotation from " + worstInto.getNoInRotation());
			implementRemovePort(worstInto, worstOut);
		}
//		for(RotationEdge e : rotationEdges){
//			if(e.isFeeder()){
//				System.out.println(e.getFromPortUNLo() + "-" + e.getToPortUNLo() + " cost: " + e.getCost());
//			}
//		}
		return madeChange;
	}
	
	public ArrayList<RotationEdge> tryRemovePort(RotationEdge ingoingEdge, RotationEdge outgoingEdge){
		if(!ingoingEdge.getToNode().equals(outgoingEdge.getFromNode()) || !ingoingEdge.isSail() || !outgoingEdge.isSail()){
			throw new RuntimeException("Input mismatch.");
		}
		ArrayList<RotationEdge> handledEdges = new ArrayList<RotationEdge>();
		handledEdges.add(ingoingEdge);
		handledEdges.add(outgoingEdge);
		RotationNode prevNode = ingoingEdge.getFromNode();
		RotationNode nextNode = outgoingEdge.getToNode();
		if(prevNode.equals(nextNode)){
			RotationEdge nextEdge = nextNode.getOutgoingSailEdge(outgoingEdge.getNoInRotation() + 1);
			handledEdges.add(nextEdge);
			nextNode = nextEdge.getToNode();
		}
		RotationEdge newEdge = createSailEdge(prevNode, nextNode, ingoingEdge.getCapacity(), ingoingEdge.getNoInRotation());
		for(RotationEdge e : handledEdges){
			e.setInactive();
		}
		handledEdges.add(0, newEdge);
		return handledEdges;
	}

	private void undoTryRemovePort(ArrayList<RotationEdge> handledEdges) {
		for(int i = 1; i < handledEdges.size(); i++){
			handledEdges.get(i).setActive();
		}
		handledEdges.get(0).delete();
	}

	public boolean insertBestPort(){
		boolean madeChange = false;
		int bestRotationObj = getFlowCost() + getRotationCost();
		System.out.println("Original objective " + bestRotationObj);
		RotationEdge bestEdge = null;
		for(int i = rotationEdges.size()-1; i >= 0; i--){
			RotationEdge e = rotationEdges.get(i);
			if(e.isFeeder()){
				ArrayList<RotationEdge> handledEdges = tryInsertPort(e);
				int rotationObj = getFlowCost() + getRotationCost();
				System.out.println("Looking at replacing feeder edge from: " + e.getFromPortUNLo() + " to: " + e.getToPortUNLo() + " yielding objective = " + rotationObj);
				if(rotationObj < bestRotationObj){
					bestRotationObj = rotationObj;
					bestEdge = e;
					madeChange = true;
				}
				undoTryInsertPort(e, handledEdges);
			}
		}
		if(madeChange){
			System.out.println("Implementing insertBestPort()");
			implementInsertPort(bestEdge);
		}
		
		return madeChange;
	}
	
	public ArrayList<RotationEdge> tryInsertPort(RotationEdge affectedEdge){
		int capacity = -1;
		for(RotationEdge e : rotationEdges){
			if(e.isSail()){
				capacity = e.getCapacity();
			}
			if(capacity > 0){
				break;
			}
		}
		ArrayList<RotationEdge> handledEdges = new ArrayList<RotationEdge>();
		RotationNode fromNode = affectedEdge.getFromNode();
		RotationNode toNode = affectedEdge.getToNode();
		handledEdges.add(createSailEdge(fromNode, toNode, capacity, 0));
		handledEdges.add(createSailEdge(toNode, fromNode, capacity, 1));
		affectedEdge.setInactive();
		return handledEdges;
	}
	
	public void undoTryInsertPort(RotationEdge feederEdge, ArrayList<RotationEdge> newSailEdges){
		feederEdge.setActive();
		for(int i = newSailEdges.size()-1; i >= 0; i--){
			newSailEdges.get(i).delete();
		}
	}
	
	public void implementInsertPort(RotationEdge feeder){
		RotationNode from = feeder.getFromNode();
		RotationNode to = feeder.getToNode();
		int noInRotation = -1;
		int capacity = -1;
		for(RotationEdge e : from.getOutgoingEdges()){
			if(e.isSail()){
				if(e.getNoInRotation() > noInRotation){
					noInRotation = e.getNoInRotation();
				}
				capacity = e.getCapacity();
			}
		}
		incrementNoInRotation(noInRotation);
		incrementNoInRotation(noInRotation+1);
		createSailEdge(from, to, capacity, noInRotation);
		createSailEdge(to, from, capacity, noInRotation+1);
		feeder.delete();
	}
	
	public int getFlowCost(){
		multicommodityFlow.run();
		int flowCost = 0;
		for (RotationDemand d : rotationDemands){
			flowCost += d.getTotalCost();
		}
		return flowCost;
	}
	
	public int getRotationCost(){
		int rotationCost = 0;
		VesselClass v = rotation.getVesselClass();
		int ports = 0;
		int portCost = 0;
		int suezCost = 0;
		int panamaCost = 0;
		int distance = 0;
		for (RotationEdge e : rotationEdges){
			if(e.isSail() && e.isActive()){
				int fromPortId = e.getFromNode().getPort().getPortId();
				int toPortId = e.getToNode().getPort().getPortId();
				DistanceElement d = Data.getBestDistanceElement(fromPortId, toPortId, v);
				distance += d.getDistance();
				Port p = e.getToNode().getPort();
				portCost += p.getFixedCallCost() + p.getVarCallCost() * v.getCapacity();
				if(d.isSuez()){
					suezCost += v.getSuezFee();
				}
				if(d.isPanama()){
					panamaCost += v.getPanamaFee();
				}
				ports++;
			}
		}
		int bestSpeedCost = Integer.MAX_VALUE;
		int noVessels = 0;
		double bestSpeed = 0;
		int lbNoVessels = calculateMinNoVessels(distance);
		int ubNoVessels = calculateMaxNoVessels(distance);
		if(lbNoVessels > ubNoVessels){
			bestSpeed = v.getMinSpeed();
			noVessels = lbNoVessels;
		} else {
			for(int i = lbNoVessels; i <= ubNoVessels; i++){
				double speed = calculateSpeed(distance, i);
				int bunkerCost = calcSailingBunkerCost(distance, speed, i);
				int TCRate = i * v.getTCRate();
				int speedCost = bunkerCost + TCRate;
				if(speedCost < bestSpeedCost){
					bestSpeedCost = speedCost;
					bestSpeed = speed;
					noVessels = i;
				}
			}
		}
		
		//TODO USD per metric tons fuel = 600
		
		int idleTime = ports * 24;
		int sailingBunkerCost = calcSailingBunkerCost(distance, bestSpeed, noVessels);
		double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * 600;

		int rotationDays = (int) Math.ceil(((distance/bestSpeed)+idleTime)/24.0);
		int TCCost = rotationDays * v.getTCRate();

		rotationCost += sailingBunkerCost + idleBunkerCost + portCost + suezCost + panamaCost + TCCost;

		return rotationCost;
	}

	public double calculateSpeed(int distance, int noOfVessels){
		double availableTime = 168 * noOfVessels - 24 * rotationEdges.size();
		return distance / availableTime;
	}

	public int calculateMinNoVessels(int distance){
		double rotationTime = (24 * rotationEdges.size()+ (distance / rotation.getVesselClass().getMaxSpeed())) / 168.0;
		int noVessels = (int) Math.ceil(rotationTime);
		return noVessels;
	}

	public int calculateMaxNoVessels(int distance){
		double rotationTime = (24 * rotationEdges.size() + (distance / rotation.getVesselClass().getMinSpeed())) / 168.0;
		int noVessels = (int) Math.floor(rotationTime);
		return noVessels;
	}
	
	public int calcSailingBunkerCost(int distance, double speed, int noOfVessels){
		double fuelConsumption = rotation.getVesselClass().getFuelConsumption(speed);
		double sailTimeDays = (distance / speed) / 24.0;
		double bunkerConsumption = sailTimeDays * fuelConsumption;
		//TODO Fuel cost!!!
		return (int) (bunkerConsumption * 600.0);
	}
	
	public void createGraph(){
		this.createDemands();
		this.createNodes();
		this.createFeederEdges();
	}

	private void createDemands(){
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
			RotationDemand rd = getRotationDemand(d);
			if(rd == null){
				rd = new RotationDemand(d, r.getFFE());
				rotationDemands.add(rd);
			} else {
				rd.addDemand(r.getFFE());
			}
		}
	}

	private void createNodes(){
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				Port fromPort = e.getFromNode().getPort();
				RotationNode fromNode = getRotationNode(fromPort);
				Port toPort = e.getToNode().getPort();
				RotationNode toNode = getRotationNode(toPort);
				createSailEdge(fromNode, toNode, e.getCapacity(), e.getNoInRotation());
			}
		}

		for(RotationDemand d : rotationDemands){
			Port fromPort = d.getOrgDemand().getOrigin();
			RotationNode fromNode = getRotationNode(fromPort);
			fromNode.setFromCentroid();
			d.setOrigin(fromNode);
			Port toPort = d.getOrgDemand().getDestination();
			RotationNode toNode = getRotationNode(toPort);
			d.setDestination(toNode);
			createOmissionEdge(d, fromNode, toNode);
		}
	}

	private void createFeederEdges(){
		for(Route r : orgRoutes){
			Port fromPort = r.getRoute().get(0).getFromNode().getPort();
			Port toPort = null;
			for(Edge e : r.getRoute()){
				if(e.getRotation() != null && !e.getRotation().equals(rotation)){
					toPort = e.getToNode().getPort();
				} else if(e.getRotation() != null) {
					if(fromPort != null && toPort != null && !fromPort.equals(toPort)){
						RotationNode fromNode = getRotationNode(fromPort);
						RotationNode toNode = getRotationNode(toPort);
						createFeederEdge(fromNode, toNode);
					}
					fromPort = e.getToNode().getPort();
					toPort = null;
				}
			}
			if(fromPort != null && toPort != null && !fromPort.equals(toPort)){
				RotationNode fromNode = getRotationNode(fromPort);
				RotationNode toNode = getRotationNode(toPort);
				createFeederEdge(fromNode, toNode);
			}
		}
	}

	private RotationNode getRotationNode(Port port){
		RotationNode n = rotationNodes[port.getPortId()];
		if(n == null){
			n = new RotationNode(this, port);
			rotationNodes[port.getPortId()] = n;
		}
		return n;
	}

	public RotationNode[] getRotationNodes(){
		return rotationNodes;
	}

	private RotationDemand getRotationDemand(Demand d){
		for(RotationDemand rd : rotationDemands){
			if(rd.getOrgDemand().equals(d)){
				return rd;
			}
		}
		return null;
	}

	private void createOmissionEdge(RotationDemand demand, RotationNode fromNode, RotationNode toNode){
		int cost = 1000 + demand.getOrgDemand().getRate();
		RotationEdge omission = new RotationEdge(this, fromNode, toNode, Integer.MAX_VALUE, cost, false, false, true, -1);
		rotationEdges.add(omission);
	}

	private RotationEdge createSailEdge(RotationNode fromNode, RotationNode toNode, int capacity, int noInRotation){
		RotationEdge sail = new RotationEdge(this, fromNode, toNode, capacity, 1, true, false, false, noInRotation);
		rotationEdges.add(noInRotation, sail);
		fromNode.setRotation();
		toNode.setRotation();
		return sail;
	}

	private void createFeederEdge(RotationNode fromNode, RotationNode toNode){
		int cost = computeFeederCost(fromNode, toNode);
		RotationEdge feeder = new RotationEdge(this, fromNode, toNode, Integer.MAX_VALUE, cost, false, true, false, -1);
		rotationEdges.add(feeder);
	}

	private int computeFeederCost(RotationNode fromNode, RotationNode toNode){
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
		if(fromNode.isRotation()){
			transferCost += fromNode.getPort().getTransshipCost();
		}
		if(toNode.isRotation()){
			transferCost += toNode.getPort().getTransshipCost();
		}
		int avgCost = totalCost / v.getCapacity() + transferCost;

		return avgCost;
	}

	public void testRemovePort(){
		printRotation();
		RotationEdge ingoingEdge = rotationEdges.get(2);
		RotationEdge outgoingEdge = rotationEdges.get(3);
		implementRemovePort(ingoingEdge, outgoingEdge);
		printRotation();
	}

	public void testAddPort(){
		printRotation();
		RotationEdge affectedEdge = rotationEdges.get(4);
		Port newPort = graph.getPort(198);
		insertPort(affectedEdge, newPort);
		printRotation();
	}

	public void implementRemovePort(RotationEdge ingoingEdge, RotationEdge outgoingEdge){
		if(!ingoingEdge.getToNode().equals(outgoingEdge.getFromNode()) || !ingoingEdge.isSail() || !outgoingEdge.isSail()){
			throw new RuntimeException("Input mismatch.");
		}
		rotation.removePort(ingoingEdge.getNoInRotation(), outgoingEdge.getNoInRotation());
		ArrayList<RotationEdge> deleteEdges = new ArrayList<RotationEdge>();
		deleteEdges.add(ingoingEdge);
		deleteEdges.add(outgoingEdge);
		RotationNode prevNode = ingoingEdge.getFromNode();
		RotationNode nextNode = outgoingEdge.getToNode();
		if(prevNode.equals(nextNode)){
			RotationEdge nextEdge = nextNode.getOutgoingSailEdge(outgoingEdge.getNoInRotation() + 1);
			deleteEdges.add(nextEdge);
			nextNode = nextEdge.getToNode();
			decrementNoInRotation(outgoingEdge.getNoInRotation());
		}
		createSailEdge(prevNode, nextNode, ingoingEdge.getCapacity(), ingoingEdge.getNoInRotation());
		decrementNoInRotation(ingoingEdge.getNoInRotation());
		deleteEdges(deleteEdges);
		
	}
	
	public void insertPort(RotationEdge affectedEdge, Port newPort){
		rotation.insertPort(affectedEdge.getNoInRotation(), newPort);
		int noInRotation = affectedEdge.getNoInRotation();
		RotationNode fromNode = affectedEdge.getFromNode();
		RotationNode toNode = affectedEdge.getToNode();
		RotationNode newNode = getRotationNode(newPort);
		if(newNode.equals(fromNode) || newNode.equals(toNode)){
			throw new RuntimeException("Adding port which is equal to either from or to port on passed edge.");
		}
		if(!affectedEdge.isSail()){
			throw new RuntimeException("Adding port to a non-sail edge.");
		}
		incrementNoInRotation(noInRotation);
		createSailEdge(fromNode, newNode, affectedEdge.getCapacity(), noInRotation);
		createSailEdge(newNode, toNode, affectedEdge.getCapacity(), noInRotation+1);
		affectedEdge.delete();
	}

	public void decrementNoInRotation(int noFrom){
		for(RotationEdge e : rotationEdges){
			if(e.isSail() && e.getNoInRotation() > noFrom){
				e.decrementNoInRotation();
			}
		}
	}

	public void incrementNoInRotation(int noFrom){
		for(RotationEdge e : rotationEdges){
			if(e.isSail() && e.getNoInRotation() > noFrom){
				e.incrementNoInRotation();
			}
		}
	}

	public void deleteEdges(ArrayList<RotationEdge> edges){
		for(RotationEdge e : edges){
			e.delete();
		}
	}
	
	public ArrayList<RotationDemand> getRotationDemands(){
		return rotationDemands;
	}

	public ArrayList<RotationEdge> getRotationEdges() {
		return rotationEdges;
	}

	public void addUnprocessedNode(RotationNode node){
		multicommodityFlow.addUnprocessedNode(node);
	}

	public void addUnprocessedNodeRep(RotationNode node){
		multicommodityFlow.addUnprocessedNodeRep(node);
	}

	public void removeRotationNode(RotationNode rotationNode) {
		int portId = rotationNode.getPort().getPortId();
		rotationNodes[portId] = null;
	}

	public void removeRotationEdge(RotationEdge rotationEdge) {
		rotationEdges.remove(rotationEdge);
	}

	private void printRotation() {
		System.out.println("Rotation ID " + rotation.getId());
		for(RotationEdge e : rotationEdges){
			if(e.isSail()){
				String str = "NoInRotation: " + e.getNoInRotation() + " " + e.getFromPortUNLo() + "-" + e.getToPortUNLo();
//				str += " " + e.getFromNode().getPort().getPortId() + "-" + e.getToNode().getPort().getPortId();
				System.out.println(str);
			}
		}

	}
}
