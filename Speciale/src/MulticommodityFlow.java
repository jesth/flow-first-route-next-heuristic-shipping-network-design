import java.util.ArrayList;

public class MulticommodityFlow {
	private static Graph graph;
	
	
	public static void run(ArrayList<Demand> demands){
		BellmanFord.run();
		ArrayList<Edge> rotationRoute = new ArrayList<Edge>();
		
		for (Demand d : demands){
			rotationRoute = BellmanFord.getRoute(d);
			for (Edge e : rotationRoute){
				e.addLoad(d.getDemand());
			}
		}
		int lagrange;
		int iteration = 1;
		boolean invalidFlow = true;
		while (invalidFlow){
			invalidFlow = false;
			for (Edge e : graph.getEdges()){
				if(e.getCapacity() < e.getLoad()){
					invalidFlow = true;
					lagrange = (e.getLoad() - e.getCapacity()) * 1/iteration;
					e.addLagrange(lagrange);
				}
			}
			iteration++;
			BellmanFord.run();
			for (Demand d : demands){
				rotationRoute = BellmanFord.getRoute(d);
				for (Edge e : rotationRoute){
					e.addLoad(d.getDemand());
				}
			}
		}
		
	}
	
	
	public static void saveODSol(){
		
	}
}
