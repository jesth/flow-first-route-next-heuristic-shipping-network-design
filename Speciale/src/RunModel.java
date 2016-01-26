import java.io.FileNotFoundException;
import java.util.ArrayList;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		Graph testGraph = new Graph();
//		for(Node i : testGraph.getNodes()){
//			System.out.println(i.getDistances().length);
//		}
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("DKAAR", "RULED", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("RULED", "DEBRV", false, false);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		
		Rotation r = testGraph.createRotation(distances, vesselClass);
//		BellmanFord.initialize(testGraph);
//		System.out.println("Intialized");
//		BellmanFord.run();
//		System.out.println("Done");
//		for(Demand i : testGraph.getData().getDemands()){
//			BellmanFord.printRoute(i);
//		}
		MulticommodityFlow.initialize(testGraph);
		Result.initialize(testGraph);
		Result.addRotation(r);
		MulticommodityFlow.run();
		for(Demand i : testGraph.getData().getDemands()){
//			BellmanFord.printRoute(i);
		}
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
	}

}
