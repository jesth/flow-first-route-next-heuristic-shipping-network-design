import java.util.ArrayList;

public class BellmanFord {
	private static ArrayList<Node> unprocessedNodes = new ArrayList<Node>();
	
	/** Initializes all nodes to distance Integer.MAX_VALUE and predecessor to null. Exception: Centroids.
	 * @param graph
	 */
	public static void initialize(Graph graph){
		for(Node i : graph.getNodes()){
			for(int j = 0; j < i.getDistances().length; j++){
				i.setLabels(j, Integer.MAX_VALUE, null);
			}
			if(i.isCentroid()){
				i.setLabels(i.getCentroidId(), 0, null);
				i.setUnprocessed(i.getCentroidId());
				unprocessedNodes.add(i);
			}
		}
	}
	
	public static void run(){
		for(Node v : unprocessedNodes){
			relaxAll(v);
		}
	}
	
	public static void relaxAll(Node v){
		for(int i = 0; i < v.getDistances().length; i++){
			if(v.isUnprocessed(i)){
				for(Edge e : v.getIngoingEdges()){
					relax(i, e);
					v.setProcessed(i);
				}
			}
		}
	}
	
	public static void relax(int centroidId, Edge edge){
		Node u = edge.getFromNode();
		Node v = edge.getToNode();
		if(v.getDistance(centroidId) > u.getDistance(centroidId) + edge.getCost()){
			v.setLabels(centroidId, u.getDistance(centroidId) + edge.getCost(), edge);
		}
	}
	
	public static void addNodes(ArrayList<Node> nodes){
		unprocessedNodes.addAll(nodes);
	}
	
	public static void addNode(Node node){
		unprocessedNodes.add(node);
	}
	
}
