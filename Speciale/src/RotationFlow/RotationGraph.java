package RotationFlow;

import java.util.ArrayList;

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
		noOfCentroids = newGraph.getData().getPortsMap().size();
		RotationNode.setNoOfCentroids(noOfCentroids);
		graph = newGraph;
	}

	public void findFlow(){
		multicommodityFlow.run();
		multicommodityFlow.saveODSol("ODSolRotation.csv", rotationDemands);
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

	private void createSailEdge(RotationNode fromNode, RotationNode toNode, int capacity, int noInRotation){
		RotationEdge sail = new RotationEdge(this, fromNode, toNode, capacity, 1, true, false, false, noInRotation);
		rotationEdges.add(noInRotation, sail);
		fromNode.setRotation();
		toNode.setRotation();
	}

	private void createFeederEdge(RotationNode fromNode, RotationNode toNode){
		int cost = computeFeederCost(fromNode, toNode);
		RotationEdge feeder = new RotationEdge(this, fromNode, toNode, Integer.MAX_VALUE, cost, false, true, false, -1);
		rotationEdges.add(feeder);
	}

	private int computeFeederCost(RotationNode fromNode, RotationNode toNode){
		VesselClass v = rotation.getVesselClass();
		DistanceElement distance = graph.getData().getBestDistanceElement(fromNode.getPort(), toNode.getPort(), v);
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
		RotationEdge ingoingEdge = rotationEdges.get(7);
		RotationEdge outgoingEdge = rotationEdges.get(8);
		removePort(ingoingEdge, outgoingEdge);
		printRotation();
	}

	public void removePort(RotationEdge ingoingEdge, RotationEdge outgoingEdge){
		if(!ingoingEdge.getToNode().equals(outgoingEdge.getFromNode()) || !ingoingEdge.isSail() || !outgoingEdge.isSail()){
			throw new RuntimeException("Input mismatch.");
		}
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

	public void decrementNoInRotation(int noFrom){
		for(RotationEdge e : rotationEdges){
			if(e.isSail() && e.getNoInRotation() > noFrom){
				e.decrementNoInRotation();
			}
		}
	}

	public void deleteEdges(ArrayList<RotationEdge> edges){
		ArrayList<RotationNode> affectedNodes = new ArrayList<RotationNode>();
		for(RotationEdge e : edges){
			RotationNode fromNode = e.getFromNode();
			RotationNode toNode = e.getToNode();
			if(!affectedNodes.contains(fromNode)){
				affectedNodes.add(fromNode);
			}
			if(!affectedNodes.contains(toNode)){
				affectedNodes.add(toNode);
			}
			e.delete();
		}
		for(RotationNode n : affectedNodes){
			if(n.noSailEdges()){
				n.delete();
			}
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
				System.out.println(str);
			}
		}

	}
}
