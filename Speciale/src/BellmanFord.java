import java.util.ArrayList;

import javax.swing.plaf.synth.SynthSpinnerUI;

public class BellmanFord {
	private static ArrayList<Node> unprocessedNodes = new ArrayList<Node>();
	private static Graph graph;

	/** Initializes all nodes to distance Integer.MAX_VALUE and predecessor to null. Exception: Centroids.
	 * @param graph
	 */
	public static void initialize(Graph inputGraph){
		graph = inputGraph;
		reset();
	}

	/** Resets the graph, i.e. setting all labels to Integer.MAX_VALUE and null as predecessor.
	 * Sets centroids to unprocessed.
	 */
	public static void reset(){
		for(Node i : graph.getNodes()){
			for(int j = 0; j < i.getDistances().length-1; j++){
				i.setLabels(j, Integer.MAX_VALUE, null);
			}
			if(i.isCentroid()){
				i.setLabels(i.getPortId(), 0, null);
				i.setUnprocessed(i.getPortId());
			}
		}
	}

	/** Runs the BellmanFord algorithm.
	 * Also adds the demand load, shortest path OD relations, lagrange profit and real profit to relevant edges.
	 */
	public static void run(){
		for(Edge e : graph.getEdges()){
			e.clearRoutes();
		}
		while(!unprocessedNodes.isEmpty()){
			Node u = unprocessedNodes.remove(0);
			relaxAll(u);
		}
		ArrayList<Demand> demands = graph.getData().getDemands();
		for(Demand d : demands){
			d.clearRoutes();
			//TODO: Incorporate lines 2 and 3 below to the createMainRoute() method.
			Route r = d.createMainRoute();
			ArrayList<Edge> route = getRoute(d, false);
			r.update(route);
		}
	}

	/** Relaxes outgoing edges for Node u, with respect to all centroids/ports that Node u is unProcessed.
	 * @param u
	 */
	public static void relaxAll(Node u){
		for(int i = 0; i < u.getDistances().length-1; i++){
			if(u.isUnprocessed(i)){
				for(Edge e : u.getOutgoingEdges()){
					relax(i, e);
				}
				u.setProcessed(i);
			}
		}
	}

	/** Relax the edge with respect to a port/centroid.
	 * @param centroidId - for which centroid the edge should be relaxed.
	 * @param edge
	 */
	public static void relax(int centroidId, Edge edge){
		Node u = edge.getFromNode();
		Node v = edge.getToNode();
		if(u.getDistance(centroidId) < Integer.MAX_VALUE){
			if(v.getDistance(centroidId) > u.getDistance(centroidId) + edge.getCost()){
				v.setLabels(centroidId, u.getDistance(centroidId) + edge.getCost(), edge);
				v.setUnprocessed(centroidId);
			}
		}
	}

	/** Relax an edge and resets all relevant nodes for affected ports/centroid.
	 * @param edge
	 */
	public static void relaxEdge(Edge edge){
		Node toNode = edge.getToNode();
		ArrayList<Integer> affectedPorts = new ArrayList<Integer>();
		for(Route r : edge.getRoutes()){
			if(!r.isRepair()){
				int originPortId = r.getDemand().getOrigin().getPortId();
				if(!affectedPorts.contains(originPortId)){
					affectedPorts.add(originPortId);
				}
			}
		}
		for(int i : affectedPorts){
			resetNodeRecursive(i, toNode);
		}
	}

	/** Calls resetNode(int, Node) for this and all further nodes in the shortest path to a centroid.
	 * @param centroidId - for which centroid the node should be set to unprocessed.
	 * @param resetNode  - the node that is reset.
	 */
	public static void resetNodeRecursive(int centroidId, Node resetNode){
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
	public static void resetNode(int centroidId, Node resetNode){
		resetNode.setLabels(centroidId, Integer.MAX_VALUE, null);
		if(resetNode.isCentroid() && centroidId == resetNode.getPortId()){
			resetNode.setLabels(centroidId, 0, null);
		}
		//All nodes connected by edges to the reset node must be processed again to get the reset node relaxed correctly.
		for(Edge e : resetNode.getIngoingEdges()){
			Node fromNode = e.getFromNode();
			fromNode.setUnprocessed(centroidId);
		}
	}

	/** 
	 * @param fromNode
	 * @param toNode
	 * @return an ArrayList of edges i.e. a route that a demand uses from origin port/centroid to destination port/centroid.
	 */
	public static ArrayList<Edge> getRoute(Demand demand, boolean repRoute){
		Node fromNode = demand.getOrigin().getCentroidNode();
		Node toNode = demand.getDestination().getCentroidNode();
		ArrayList<Edge> usedEdges = new ArrayList<Edge>();
		int arrayPos;
		if(!repRoute){
			arrayPos = fromNode.getPortId();
		} else {
			arrayPos = fromNode.getDistances().length-1;
		}
		Edge predecessor = toNode.getPredecessor(arrayPos);
		usedEdges.add(predecessor);
		//		System.out.println("Getting route from " + demand.getOrigin().getUNLocode() + " to " + demand.getDestination().getUNLocode());
		if(repRoute){
			System.out.println("REP ROUTE");
		}
		while(!predecessor.getFromNode().equals(fromNode)){
			predecessor = predecessor.getFromNode().getPredecessor(arrayPos);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}

	/** Print the route that a Demand used from origin port/centroid to destination port/centroid.
	 * @param demand
	 */
	public static void printRoute(Demand demand){
		ArrayList<Edge> usedEdges = getRoute(demand, false);
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

	public static void addUnprocessedNode(Node unprocessedNode){
		if(!unprocessedNodes.contains(unprocessedNode)){
			unprocessedNodes.add(unprocessedNode);
		}
	}

	public static void runSingleRoute(Route r){
		Node origin = r.getDemand().getOrigin().getCentroidNode();
		resetSingle(origin);
		ArrayList<Node> unprocessedRepNodes = new ArrayList<Node>(unprocessedNodes);
		while(!unprocessedRepNodes.isEmpty()){
			Node u = unprocessedRepNodes.remove(0);
			relaxSingle(u, r.getProhibitedEdges());
			if(u.allCentroidsProcessed()){
				unprocessedNodes.remove(u);
			}
			adjustUnprocessedRepNodes(unprocessedRepNodes);
		}
		//TODO: Incorporate the two lines below to the createRepRoute() method.
		ArrayList<Edge> route = getRoute(r.getDemand(), true);
		r.update(route);
	}

	public static void resetSingle(Node origin){
		int arrayPos = Node.getNoOfCentroids()-1;
		for(Node i : graph.getNodes()){
			i.setLabels(arrayPos, Integer.MAX_VALUE, null);
		}
		origin.setLabels(arrayPos, 0, null);
		origin.setUnprocessed(arrayPos);
	}

	public static void relaxSingle(Node u, ArrayList<Edge> prohibitedEdges){
		int arrayPos = Node.getNoOfCentroids()-1;
		if(u.isUnprocessed(arrayPos)){
			for(Edge e : u.getOutgoingEdges()){
				if(!prohibitedEdges.contains(e)){
					relax(arrayPos, e);
				}
			}
			u.setProcessed(arrayPos);
		}
	}
	
	public static void adjustUnprocessedRepNodes(ArrayList<Node> unprocessedRepNodes){
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
