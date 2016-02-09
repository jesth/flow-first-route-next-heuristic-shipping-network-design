import java.io.FileNotFoundException;
import java.util.ArrayList;

import Data.DistanceElement;
import Data.VesselClass;
import Graph.Graph;
import Methods.ComputeRotations;
import Methods.MulticommodityFlow;
import Results.Result;
import Results.Rotation;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		
//		testBaltic();
		testAutomatic();

	}
	
	public static void testAutomatic() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Baltic.csv", "fleet_Baltic.csv");
		initialize(testGraph);
		MulticommodityFlow.run();
		ComputeRotations.createLargestLossRotation();
		System.out.println("Rotation created");
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		ComputeRotations.createLargestLossRotation();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
	}
	
	public static void initialize(Graph graph) throws FileNotFoundException{
		ComputeRotations.intialize(graph);
		MulticommodityFlow.initialize(graph);
		Result.initialize(graph);
	}
	
	public static void testBaltic() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Baltic.csv", "fleet_Baltic.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("RULED", "FIKTK", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("FIKTK", "DEBRV", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("DEBRV", "RUKGD", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("RUKGD", "PLGDY", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("PLGDY", "DEBRV", false, false);
		DistanceElement leg6 = testGraph.getData().getDistanceElement("DEBRV", "RULED", false, false);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getDistanceElement("RULED", "DEBRV", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("DEBRV", "NOSVG", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("NOSVG", "SEGOT", false, false);
		DistanceElement leg13 = testGraph.getData().getDistanceElement("SEGOT", "DEBRV", false, false);
		DistanceElement leg14 = testGraph.getData().getDistanceElement("DEBRV", "RULED", false, false);
		distances2.add(leg10);
		distances2.add(leg11);
		distances2.add(leg12);
		distances2.add(leg13);
		distances2.add(leg14);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg20 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", false, false);
		DistanceElement leg21 = testGraph.getData().getDistanceElement("DKAAR", "DEBRV", false, false);
		distances3.add(leg20);
		distances3.add(leg21);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		MulticommodityFlow.initialize(testGraph);
		Result.initialize(testGraph);
		Result.addRotation(r);
		Result.addRotation(r2);
		Result.addRotation(r3);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
		System.out.println();
		System.out.println("Objective " + Result.getObjective());
		System.out.println("Flow profit " + Result.getFlowProfit(false));
	}
	
	public static void testMed() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("ESALG", "TRAMB", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("TRAMB", "ITGOA", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("ITGOA", "ESALG", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("ESALG", "ESLPA", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("ESLPA", "ESALG", false, false);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getDistanceElement("ESALG", "EGALY", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("EGALY", "MACAS", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("MACAS", "ESALG", false, false);
		distances2.add(leg10);
		distances2.add(leg11);
		distances2.add(leg12);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		/*
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg20 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", false, false);
		DistanceElement leg21 = testGraph.getData().getDistanceElement("DKAAR", "DEBRV", false, false);
		distances3.add(leg20);
		distances3.add(leg21);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		*/
		
		MulticommodityFlow.initialize(testGraph);
		Result.initialize(testGraph);
		Result.addRotation(r);
		Result.addRotation(r2);
//		Result.addRotation(r3);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
		System.out.println();
		System.out.println("Objective " + Result.getObjective());
		System.out.println("Flow profit " + Result.getFlowProfit(false));
	}

}
