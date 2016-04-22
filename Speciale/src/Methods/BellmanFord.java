package Methods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Results.Route;


public class BellmanFord implements Runnable {
	private ArrayList<Node> unprocessedNodes = new ArrayList<Node>();
	private ArrayList<Node> unprocessedNodesRep = new ArrayList<Node>();
	private ArrayList<Node> changedRepNodes = new ArrayList<Node>();
	private Node centroidNode;
	private boolean rep;
	
	private static Graph graph;

	/** Initializes all nodes to distance Integer.MAX_VALUE and predecessor to null. Exception: Centroids.
	 * @param graph
	 */
	public BellmanFord(Graph inputGraph, Node centroidNode){
		graph = inputGraph;
		this.centroidNode = centroidNode;
		reset();
	}

	/** Resets the graph, i.e. setting all labels to Integer.MAX_VALUE and null as predecessor.
	 * Sets centroids to unprocessed.
	 */
	public void reset(){
		for(Node i : graph.getNodes()){
			i.setLabels(centroidNode.getPortId(), Integer.MAX_VALUE, null);
		}
		centroidNode.setLabels(centroidNode.getPortId(), 0, null);
		centroidNode.setUnprocessed(this, centroidNode.getPortId());
	}
	
	public void resetRep(){
		for(Node i : graph.getNodes()){
			i.setLabelsRep(centroidNode.getPortId(), Integer.MAX_VALUE, null);
		}
		centroidNode.setLabelsRep(centroidNode.getPortId(), 0, null);
		centroidNode.setUnprocessedRep(this, centroidNode.getPortId());
	}
	
	public void run(){
		if(!rep){
			runMain();
		} else {
			runRep();
		}
	}

	/** Runs the BellmanFord algorithm.
	 * Also adds the demand load, shortest path OD relations, lagrange profit and real profit to relevant edges.
	 */
	private void runMain(){
		reset();

		int counter = 0;
		while(!unprocessedNodes.isEmpty()){
			Node u = unprocessedNodes.remove(0);
			relaxAll(u);
//			System.out.println(counter++);
		}
		Demand[] demands = graph.getData().getFromDemandArray(centroidNode.getPortId());
		for(Demand d : demands){
			if(d == null)
				continue;
//			System.out.println("Running demand from " + d.getOrigin().getUNLocode() + " to " + d.getDestination().getUNLocode() + ": " + d.getDemand());
			d.clearRoutes();
			d.createMainRoute();
		}
	}
	
//	public void runForOrigin(int centroidId){
//		while(!unprocessedNodes.isEmpty()){
//			Node u = unprocessedNodes.remove(0);
//			if(u.isUnprocessed(centroidId)){
//				for(Edge e : u.getOutgoingEdges()){
//					relax(centroidId, e);
//				}
//				u.setProcessed(centroidId);
//			} else {
//				for(int i = 0; i < u.getNoOfCentroids(); i++){
//					
//				}
//			}
//		}
//	}

	private void runRep(){
		resetRep();
		while(!unprocessedNodesRep.isEmpty()){
			Node u = unprocessedNodesRep.remove(0);
			relaxAllRep(u);
		}
	}

	/** Relaxes outgoing edges for Node u, with respect to all centroids/ports that is unProcessed for this node.
	 * @param u - The node to relax outgoing edges from.
	 */
	public void relaxAll(Node u){
		if(u.isUnprocessed(centroidNode.getPortId())){
			for(Edge e : u.getOutgoingEdges()){
				relax(centroidNode.getPortId(), e);
			}
			u.setProcessed(centroidNode.getPortId());
		} else {
			System.out.println("how often does this happen???????????????????????????????");
		}
	}

	public void relaxAllRep(Node u){
		if(u.isUnprocessedRep(centroidNode.getPortId())){
			for(Edge e : u.getOutgoingEdges()){
				relaxRep(centroidNode.getPortId(), e);
			}
			u.setProcessedRep(centroidNode.getPortId());
		}
	}

	/** Relax the edge with respect to a port/centroid.
	 * @param centroidId - for which centroid the edge should be relaxed.
	 * @param edge
	 */
	public void relax(int centroidId, Edge e){
		Node u = e.getFromNode();
		Node v = e.getToNode();
		if(u.getDistance(centroidId) < Integer.MAX_VALUE){
			if(v.getDistance(centroidId) > u.getDistance(centroidId) + e.getCost()){
				v.setLabels(centroidId, u.getDistance(centroidId) + e.getCost(), e);
				v.setUnprocessed(this, centroidId);
			}
		}
	}

	public void relaxRep(int centroidId, Edge e){
		Node u = e.getFromNode();
		Node v = e.getToNode();
		if(u.getDistanceRep(centroidId) < Integer.MAX_VALUE){
			if(v.getDistanceRep(centroidId) > u.getDistanceRep(centroidId) + e.getCost()){
				if(e.getRepLoad() < e.getCapacity()){
					v.setLabelsRep(centroidId, u.getDistanceRep(centroidId) + e.getCost(), e);
					v.setUnprocessedRep(this, centroidId);
				}
			}
		}
	}

	/** Relax an edge and resets all relevant nodes for affected ports/centroid.
	 * @param edge
	 */
	
	public void relaxEdge(Edge edge){
		Node toNode = edge.getToNode();
		resetNodeRecursive(centroidNode.getPortId(), toNode);
//		ArrayList<Integer> affectedPorts = new ArrayList<Integer>();
//		for(Route r : edge.getRoutes()){
//			if(!r.isRepair()){
//				int originPortId = r.getDemand().getOrigin().getPortId();
//				if(!affectedPorts.contains(originPortId)){
//					affectedPorts.add(originPortId);
//				}
//			}
//		}
//		for(int i : affectedPorts){
//			resetNodeRecursive(i, toNode);
//		}
	}
	

	/** Calls resetNode(int, Node) for this and all further nodes in the shortest path to a centroid.
	 * @param centroidId - for which centroid the node should be set to unprocessed.
	 * @param resetNode  - the node that is reset.
	 */
	public void resetNodeRecursive(int centroidId, Node resetNode){
		//If an outgoing edge is part of a shortest path to the end node of that edge, the end node must be reset.
		for(Edge e : resetNode.getOutgoingEdges()){
			if(!e.isOmission()){ //Omission edges are not affected by cost changes for rotation edges.
				Node toNode = e.getToNode();
				if(toNode.getPredecessor(centroidId) != null){
					if(toNode.getPredecessor(centroidId).equals(e)){
						resetNodeRecursive(centroidId, toNode);
					}
				}
			}
		}
		resetNode(centroidId, resetNode);
	}

	/** Resets the labels of a node to Integer.MAX_VALUE in distance and null as predecessor.
	 * @param centroidId - for which centroid the node should be set to unprocessed.
	 * @param resetNode - the node that is reset.
	 */
	public void resetNode(int centroidId, Node resetNode){
		resetNode.setLabels(centroidId, Integer.MAX_VALUE, null);
		if(resetNode.isFromCentroid() && centroidId == resetNode.getPortId()){
			resetNode.setLabels(centroidId, 0, null);
		}
		//All nodes connected by edges to the reset node must be processed again to get the reset node relaxed correctly.
		for(Edge e : resetNode.getIngoingEdges()){
			Node fromNode = e.getFromNode();
			fromNode.setUnprocessed(this, centroidId);
		}
	}

	/**
	 * @param demand - the demand for which the route is to be returned.
	 * @param repRoute - whether the route to be returned is a repair route.
	 * @return A list of edges used in the route of the specified demand element.
	 */
	public static ArrayList<Edge> getRoute(Demand demand){
		Node fromNode = demand.getOrigin().getFromCentroidNode();
		Node toNode = demand.getDestination().getToCentroidNode();
		ArrayList<Edge> usedEdges = new ArrayList<Edge>();
		int arrayPos = fromNode.getPortId();
//		if(!repRoute){
//			arrayPos = fromNode.getPortId();
//		}
//		else {
//			arrayPos = Node.getNoOfCentroids()-1;
//		}
		Edge predecessor = toNode.getPredecessor(arrayPos);
		usedEdges.add(predecessor);
		//		System.out.println("Getting route from " + demand.getOrigin().getUNLocode() + " to " + demand.getDestination().getUNLocode());
		while(!predecessor.getFromNode().equals(fromNode)){
			//			System.out.println("Predecessor from " + predecessor.getFromPortUNLo() + " to " + predecessor.getToPortUNLo() + " with Lagrange " + predecessor.getLagrange());
			predecessor = predecessor.getFromNode().getPredecessor(arrayPos);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}
	
	public static ArrayList<Edge> getRouteRep(Demand demand){
		Node fromNode = demand.getOrigin().getFromCentroidNode();
		Node toNode = demand.getDestination().getToCentroidNode();
//		System.out.println("From centroid " + fromNode.getPort().getUNLocode() + " to centroid " + toNode.getPort().getUNLocode());
		ArrayList<Edge> usedEdges = new ArrayList<Edge>();
		int arrayPos = fromNode.getPortId();
		Edge predecessor = toNode.getPredecessorRep(arrayPos);
		usedEdges.add(predecessor);
		//		System.out.println("Getting route from " + demand.getOrigin().getUNLocode() + " to " + demand.getDestination().getUNLocode());
		while(!predecessor.getFromNode().equals(fromNode)){
//			System.out.println("Whiling from " + predecessor.getFromPortUNLo() + " to " + predecessor.getToPortUNLo());
			//			System.out.println("Predecessor from " + predecessor.getFromPortUNLo() + " to " + predecessor.getToPortUNLo() + " with Lagrange " + predecessor.getLagrange());
			predecessor = predecessor.getFromNode().getPredecessorRep(arrayPos);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}

	/** Print the route that a Demand used from origin port/centroid to destination port/centroid.
	 * @param demand
	 */
	public void printRoute(Demand demand){
		ArrayList<Edge> usedEdges = getRoute(demand);
		String str = "Demand of " + demand.getDemand() + " from " + demand.getOrigin().getUNLocode() + " to " + 
				demand.getDestination().getUNLocode() + " uses route: \n";
		int counter = 1;
		boolean wasDwell = false;
		str += "Leg " + counter + ": ";
		for(Edge e : usedEdges){
			if(e.isSail() && !wasDwell){
				str += e.getFromPortUNLo() + "-" + e.getToPortUNLo();
			} else if(e.isSail() && wasDwell){
				str += "-" + e.getToPortUNLo();
				wasDwell = false;
			} else if(e.isDwell()){
				wasDwell = true;
			} else if(e.isTransshipment()){
				wasDwell = false;
				counter++;
				str += "\nLeg " + counter + ": ";
			} else if(e.isOmission()){
				str += e.getFromPortUNLo() + "-" + e.getToPortUNLo() + " via omission edge";
			} 
			counter++;
		}
		str += "\n";
		System.out.println(str);
		//TODO: Not finished - needs rotation information for non-omission edges.
	}

	/**
	 * @param unprocessedNode - the unprocessed node to be added to the unprocessedNodes array.
	 */
	public void addUnprocessedNode(Node unprocessedNode){
//		System.out.println("unproccesedNodes size: " + unprocessedNodes.size());
		if(!unprocessedNodes.contains(unprocessedNode)){
			unprocessedNodes.add(unprocessedNode);
		}
	}
	
	public void addUnprocessedNodeRep(Node unprocessedNode){
		if(!unprocessedNodesRep.contains(unprocessedNode)){
			unprocessedNodesRep.add(unprocessedNode);
		}
	}

	
	/** Finds the shortest path for the route element given as input.
	 * @param r - the route element that the shortest path is to be determined for.
	 */
	public void runSingleRoute(Route r){
		Node origin = r.getDemand().getOrigin().getFromCentroidNode();
		resetSingle(origin);
		ArrayList<Node> unprocessedRepNodes = new ArrayList<Node>(unprocessedNodes);
		while(!unprocessedRepNodes.isEmpty()){
			Node u = unprocessedRepNodes.remove(0);
			relaxSingle(u);
			if(u.allCentroidsProcessed()){
				unprocessedNodes.remove(u);
			}
			adjustUnprocessedRepNodes(unprocessedRepNodes);
		}
	}

	/** Resets the "single route" position of the distance and predecessor arrays (the last position) to Integer.MAX_VALUE and null, respectively.
	 * @param origin - the node from which the shortest path is to be determined.
	 */
	public void resetSingle(Node origin){
		int arrayPos = Node.getNoOfCentroids()-1;
		for(Node i : changedRepNodes){
			i.setLabels(arrayPos, Integer.MAX_VALUE, null);
		}
		changedRepNodes.clear();
		origin.setLabels(arrayPos, 0, null);
		origin.setUnprocessed(this, arrayPos);
	}

	/** Relaxes a node for the "single route" position of the distance and predecessor arrays only.
	 * @param u - the node to be relaxed.
	 * @param prohibitedEdges - the edges that cannot be used in the route element for which the shortest path is determined.
	 */
	public void relaxSingle(Node u){
		int arrayPos = Node.getNoOfCentroids()-1;
		if(u.isUnprocessed(arrayPos)){
			for(Edge e : u.getOutgoingEdges()){
				if(e.getCapacity() > e.getRepLoad()){
					relax(arrayPos, e);
					changedRepNodes.add(e.getToNode());
				}
			}
			u.setProcessed(arrayPos);
		}
	}
	
	public void setRep(){
		rep = true;
	}
	
	public void setMain(){
		rep = false;
	}

	/** Bookkeeping for the separate array unprocessedRepNodes - updates this array with the relevant changes to the "official" array unprocessedNodes.
	 * @param unprocessedRepNodes - the array to be updated.
	 */
	public void adjustUnprocessedRepNodes(ArrayList<Node> unprocessedRepNodes){
		int arrayPos = Node.getNoOfCentroids()-1;
		for(Node i : unprocessedNodes){
			if(i.isUnprocessed(arrayPos)){
				if(!unprocessedRepNodes.contains(i)){
					unprocessedRepNodes.add(i);
				}
			} else {
				System.out.println("Node " + i.simplePrint() + " already processed ");
			}
		}
	}
}
