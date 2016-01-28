import java.util.ArrayList;

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
			for(int j = 0; j < i.getDistances().length; j++){
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
			e.resetLoad();
			e.clearShortestPathOD();
		}
		while(!unprocessedNodes.isEmpty()){
			Node u = unprocessedNodes.remove(0);
			relaxAll(u);
		}
		ArrayList<Demand> demands = graph.getData().getDemands();
		for(Demand d : demands){
			//TODO: Add 1000 to lagrangeProfit???
			int lagrangeProfit = d.getRate();
			int realProfit = d.getRate();
			ArrayList<Edge> route = getRoute(d);
			for(Edge e : route){
				e.addLoad(d.getDemand());
				e.addShortestPathOD(d);
				lagrangeProfit -= e.getCost();
				realProfit -= e.getRealCost();
			}
			d.setLagrangeProfit(lagrangeProfit);
			d.setRealProfit(realProfit);
		}
	}

	/** Relaxes outgoing edges for Node u, with respect to all centroids/ports that is unProcessed for this node.
	 * @param u - The node to relax outgoing edges from.
	 */
	public static void relaxAll(Node u){
		for(int i = 0; i < u.getDistances().length; i++){
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
		for(Demand d : edge.getShortestPathOD()){
			if(!affectedPorts.contains(d.getOrigin().getPortId())){
				affectedPorts.add(d.getOrigin().getPortId());
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
	public static ArrayList<Edge> getRoute(Demand demand){
		Node fromNode = demand.getOrigin().getCentroidNode();
		Node toNode = demand.getDestination().getCentroidNode();
		ArrayList<Edge> usedEdges = new ArrayList<Edge>();
		int fromNodeId = fromNode.getPortId();
		Edge predecessor = toNode.getPredecessor(fromNodeId);
		usedEdges.add(predecessor);
		while(!predecessor.getFromNode().equals(fromNode)){
			predecessor = predecessor.getFromNode().getPredecessor(fromNodeId);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}

	/** Print the route that a Demand used from origin port/centroid to destination port/centroid.
	 * @param demand
	 */
	public static void printRoute(Demand demand){
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

	public static void addUnprocessedNode(Node unprocessedNode){
		if(!unprocessedNodes.contains(unprocessedNode)){
			unprocessedNodes.add(unprocessedNode);
		}
	}
}
