import java.io.FileNotFoundException;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		Graph testGraph = new Graph();
//		for(Node i : testGraph.getNodes()){
//			System.out.println(i.getDistances().length);
//		}
		BellmanFord.initialize(testGraph);
		System.out.println("Intialized");
		BellmanFord.run();
		System.out.println("Done");
		for(Demand i : testGraph.getData().getDemands()){
			BellmanFord.printRoute(i);
		}
	}

}
