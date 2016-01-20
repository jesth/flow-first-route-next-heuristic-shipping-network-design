import java.util.ArrayList;

public class BellmanFord {
	private static ArrayList<Node> unprocessedNodes = new ArrayList<Node>();
	private static Graph graph;

	/** Initializes all nodes to distance Integer.MAX_VALUE and predecessor to null. Exception: Centroids.
	 * @param graph
	 */
	public static void initialize(Graph inputGraph){
		graph = inputGraph;
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

	public static void run(){
		while(!unprocessedNodes.isEmpty()){
			Node u = unprocessedNodes.get(0);
			relaxAll(u);
		}
		System.out.println("All unprocessed nodes processed.");
		for(Edge e : graph.getEdges()){
			e.resetLoad();
			e.clearShortestPathOD();
		}
		System.out.println("All edges reset.");
		ArrayList<Demand> demands = graph.getData().getDemands();
		for(Demand d : demands){
			//TODO: Add 1000 to lagrangeProfit???
			int lagrangeProfit = d.getRate();
			ArrayList<Edge> route = getRoute(d);
			System.out.println("Route for demand " + d.getId() + " computed.");
			for(Edge e : route){
				e.addLoad(d.getDemand());
				e.addShortestPathOD(d);
				lagrangeProfit -= e.getCost();
			}
			d.setLagrangeProfit(lagrangeProfit);
			System.out.println("Demand " + d.getId() + " processed.");
		}
		System.out.println("Bellman Ford finished.");
	}

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

	public static void relax(int centroidId, Edge edge){
		Node u = edge.getFromNode();
		Node v = edge.getToNode();
		if(v.getDistance(centroidId) > u.getDistance(centroidId) + edge.getCost()){
			v.setLabels(centroidId, u.getDistance(centroidId) + edge.getCost(), edge);
			v.setUnprocessed(centroidId);
		}
	}

	public static void relaxEdge(Edge edge){
		//		System.out.println("Relaxing edge " + edge);
		Node toNode = edge.getToNode();
		ArrayList<Integer> affectedPorts = new ArrayList<Integer>();
		for(Demand d : edge.getShortestPathOD()){
			affectedPorts.add(d.getOrigin().getPortId());
		}
		for(int i : affectedPorts){
			resetNode(i, toNode);
		}
	}

	public static void resetNode(int centroidId, Node resetNode){
		for(Edge e : resetNode.getOutgoingEdges()){
			Node toNode = e.getToNode();
			if(toNode.getPredecessor(centroidId) != null){
				if(toNode.getPredecessor(centroidId).equals(e)){
					resetNode(centroidId, toNode);
					System.out.println("Resetting node " + toNode.getPort().getUNLocode() + " " + " from centroid " + centroidId);
				}
			}
		}
		resetNode.setLabels(centroidId, Integer.MAX_VALUE, null);
		for(Edge e : resetNode.getIngoingEdges()){
			Node fromNode = e.getFromNode();
			fromNode.setUnprocessed(centroidId);

		}
	}

	public static void addNodes(ArrayList<Node> unprocessedNodes){
		for(Node i : unprocessedNodes){
			if(!unprocessedNodes.contains(i)){
				unprocessedNodes.add(i);
			}
		}
	}

	/** Returns the route from one centroid/port to another centroid/port.
	 * @param fromNode
	 * @param toNode
	 * @return
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
//			System.out.println("Predecessor " + predecessor.getFromNode());
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}

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

	public static void removeUnprocessedNode(Node unprocessedNode){
		unprocessedNodes.remove(unprocessedNode);
	}
}
