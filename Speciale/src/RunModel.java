import java.io.FileNotFoundException;
import java.util.ArrayList;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		Graph testGraph = new Graph();
//		for(Node i : testGraph.getNodes()){
//			System.out.println(i.getDistances().length);
//		}
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<Port> ports = new ArrayList<Port>();
		ports.add(testGraph.getData().getPorts().get("DEBRV"));
		ports.add(testGraph.getData().getPorts().get("DKAAR"));
		ports.add(testGraph.getData().getPorts().get("RULED"));
		
		testGraph.createRotation(ports, vesselClass, false, false);
		
		
//		BellmanFord.initialize(testGraph);
//		System.out.println("Intialized");
//		BellmanFord.run();
//		System.out.println("Done");
//		for(Demand i : testGraph.getData().getDemands()){
//			BellmanFord.printRoute(i);
//		}
		MulticommodityFlow.initialize(testGraph);
		MulticommodityFlow.run();
		for(Demand i : testGraph.getData().getDemands()){
			BellmanFord.printRoute(i);
		}
		MulticommodityFlow.saveODSol("C:\\Users\\Jesper T\\Documents\\test.csv", testGraph.getData().getDemands());
	}

}
