import java.io.FileNotFoundException;
import java.util.ArrayList;

import AuxFlow.AuxDijkstra;
import AuxFlow.AuxEdge;
import AuxFlow.AuxGraph;
import AuxFlow.AuxRun;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Methods.ComputeRotations;
import Methods.MulticommodityFlow;
import Results.Result;
import Results.Rotation;

public class RunModel {
	public static void main(String[] args) throws FileNotFoundException {
		
//		testBaltic();
//		testBaltic();
//		testAutomatic();
//		testBalticManual();
//		testMedManual();
//		testMed();
//		saveAux();
//		testAux();
		testWorldSmallAuto();
//		testMedManual2();
		
	}
	
	public static void saveAux() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_WorldSmall.csv", "fleet_WorldSmall.csv");
		AuxRun.initialize(testGraph.getData(), 10);
	}
	
	public static void testAux() throws FileNotFoundException{
		Graph graph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		initialize(graph);
		ArrayList<AuxEdge> sortedEdges = AuxGraph.getSortedAuxEdges();
		VesselClass feeder450 = graph.getData().getVesselClasses().get(0);
		VesselClass feeder800 = graph.getData().getVesselClasses().get(1);
		VesselClass panamax1200 = graph.getData().getVesselClasses().get(2);
		Rotation r = ComputeRotations.createAuxFlowRotation(4, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(3, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder450);
		ComputeRotations.createAuxFlowRotation(3, sortedEdges, feeder450);
		
//		ArrayList<String> rotationPorts = new ArrayList<String>(r.getPorts().size());
//		for(Port p : r.getPorts()){
//			rotationPorts.add(p.getName());
//		}
//		RuneVisualization.makeVisualization(rotationPorts, "beforeOptimization");
		/*
		Rotation optRotation = graph.getResult().getRotations().get(0);
		Edge remove = optRotation.getRotationEdges().get(14);
		graph.removePort(optRotation, remove);
		*/
		ComputeRotations.addPorts();
		MulticommodityFlow.run();
//		ComputeRotations.removePorts();
//		MulticommodityFlow.run();
		ComputeRotations.removePorts();
		MulticommodityFlow.run();	
		ComputeRotations.addPorts();
		
//		rotationPorts = new ArrayList<String>(r.getPorts().size());
//		for(Port p : r.getPorts()){
//			rotationPorts.add(p.getName());
//		}
//		RuneVisualization.makeVisualization(rotationPorts, "afterOptimization");
		
		MulticommodityFlow.run();
		
		MulticommodityFlow.saveODSol("ODSol.csv", graph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", graph.getResult().getRotations());
		System.out.println();
		System.out.println("Objective " + graph.getResult().getObjective());
		System.out.println("Flow profit " + graph.getResult().getFlowProfit(false));
		
		graph.saveOPLData("OPLData.dat");
	}
	
	/*
	public static void testAutomatic() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		initialize(testGraph);
		MulticommodityFlow.run();
		Rotation r1 = ComputeRotations.createLargestLossRotation();
		System.out.println("Rotation created");
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		int counter = 0;
		while(r1.getNoOfVessels() < 6){
			ComputeRotations.insertBestPort(r1);
			MulticommodityFlow.run();
			counter++;
		}
		System.out.println("Did " + counter + " port inserts.");
		Rotation r2 = ComputeRotations.createLargestLossRotation();
//		MulticommodityFlow.run();
//		ComputeRotations.insertBestPort(r1);
//		MulticommodityFlow.run();
//		ComputeRotations.insertBestPort(r1);
//		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
		System.out.println(r1);
		System.out.println(r2);
	}
	*/
	
	public static void initialize(Graph graph) throws FileNotFoundException{
		ComputeRotations.intialize(graph);
		MulticommodityFlow.initialize(graph);
	}
	
	public static void testBalticManual() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Baltic.csv", "fleet_Baltic.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getBestDistanceElement("DEBRV", "RULED", vesselClass);
		DistanceElement leg2 = testGraph.getData().getBestDistanceElement("RULED", "DEBRV", vesselClass);
		DistanceElement leg3 = testGraph.getData().getBestDistanceElement("DEBRV", "NOSVG", vesselClass);
		DistanceElement leg4 = testGraph.getData().getBestDistanceElement("NOSVG", "SEGOT", vesselClass);
		DistanceElement leg5 = testGraph.getData().getBestDistanceElement("SEGOT", "DEBRV", vesselClass);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getBestDistanceElement("DEBRV", "DKAAR", vesselClass);
		DistanceElement leg11 = testGraph.getData().getBestDistanceElement("DKAAR", "DEBRV", vesselClass);
		DistanceElement leg12 = testGraph.getData().getBestDistanceElement("DEBRV", "RUKGD", vesselClass);
		DistanceElement leg13 = testGraph.getData().getBestDistanceElement("RUKGD", "FIKTK", vesselClass);
		DistanceElement leg14 = testGraph.getData().getBestDistanceElement("FIKTK", "DEBRV", vesselClass);
		DistanceElement leg15 = testGraph.getData().getBestDistanceElement("DEBRV", "RULED", vesselClass);
		DistanceElement leg16 = testGraph.getData().getBestDistanceElement("RULED", "PLGDY", vesselClass);
		DistanceElement leg17 = testGraph.getData().getBestDistanceElement("PLGDY", "DEBRV", vesselClass);
		distances2.add(leg10);
		distances2.add(leg11);
		distances2.add(leg12);
		distances2.add(leg13);
		distances2.add(leg14);
		distances2.add(leg15);
		distances2.add(leg16);
		distances2.add(leg17);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
//		vesselClass = testGraph.getData().getVesselClasses().get(0);
//		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
//		DistanceElement leg20 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", vesselClass);
//		DistanceElement leg21 = testGraph.getData().getDistanceElement("DKAAR", "DEBRV", vesselClass);
//		distances3.add(leg20);
//		distances3.add(leg21);
//		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		MulticommodityFlow.initialize(testGraph);
//		Result.addRotation(r3);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
		System.out.println();
		System.out.println("Objective " + testGraph.getResult().getObjective());
		System.out.println("Flow profit " + testGraph.getResult().getFlowProfit(false));
		
		testGraph.saveOPLData("OPLData.dat");
	}
	
	public static void testBaltic() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Baltic.csv", "fleet_Baltic.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getBestDistanceElement("RULED", "FIKTK", vesselClass);
		DistanceElement leg2 = testGraph.getData().getBestDistanceElement("FIKTK", "DEBRV", vesselClass);
		DistanceElement leg3 = testGraph.getData().getBestDistanceElement("DEBRV", "RUKGD", vesselClass);
		DistanceElement leg4 = testGraph.getData().getBestDistanceElement("RUKGD", "PLGDY", vesselClass);
		DistanceElement leg5 = testGraph.getData().getBestDistanceElement("PLGDY", "DEBRV", vesselClass);
		DistanceElement leg6 = testGraph.getData().getBestDistanceElement("DEBRV", "RULED", vesselClass);
		
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getBestDistanceElement("RULED", "DEBRV", vesselClass);
		DistanceElement leg11 = testGraph.getData().getBestDistanceElement("DEBRV", "NOSVG", vesselClass);
		DistanceElement leg12 = testGraph.getData().getBestDistanceElement("NOSVG", "SEGOT", vesselClass);
		DistanceElement leg13 = testGraph.getData().getBestDistanceElement("SEGOT", "DEBRV", vesselClass);
		DistanceElement leg14 = testGraph.getData().getBestDistanceElement("DEBRV", "RULED", vesselClass);
		distances2.add(leg10);
		distances2.add(leg11);
		distances2.add(leg12);
		distances2.add(leg13);
		distances2.add(leg14);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg20 = testGraph.getData().getBestDistanceElement("DEBRV", "DKAAR", vesselClass);
		DistanceElement leg21 = testGraph.getData().getBestDistanceElement("DKAAR", "DEBRV", vesselClass);
		distances3.add(leg20);
		distances3.add(leg21);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		MulticommodityFlow.initialize(testGraph);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("test.csv", testGraph.getData().getDemands());
		System.out.println();
		System.out.println("Objective " + testGraph.getResult().getObjective());
		System.out.println("Flow profit " + testGraph.getResult().getFlowProfit(false));
		
		testGraph.saveOPLData("OPLData.dat");
	}
	
	public static void testMedManual() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(2);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getBestDistanceElement("TRAMB", "ESALG", vesselClass);
		DistanceElement leg2 = testGraph.getData().getBestDistanceElement("ESALG", "MAPTM", vesselClass);
		DistanceElement leg4 = testGraph.getData().getBestDistanceElement("MAPTM", "EGPSD", vesselClass);
		DistanceElement leg5 = testGraph.getData().getBestDistanceElement("EGPSD", "ITGIT", vesselClass);
		DistanceElement leg6 = testGraph.getData().getBestDistanceElement("ITGIT", "EGALY", vesselClass);
		DistanceElement leg7 = testGraph.getData().getBestDistanceElement("EGALY", "TRAMB", vesselClass);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		distances.add(leg7);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getBestDistanceElement("EGPSD", "ILASH", vesselClass);
		DistanceElement leg11 = testGraph.getData().getBestDistanceElement("ILASH", "LBBEY", vesselClass);
		DistanceElement leg12 = testGraph.getData().getBestDistanceElement("LBBEY", "TRMER", vesselClass);
		DistanceElement leg52 = testGraph.getData().getBestDistanceElement("TRMER", "ITGIT", vesselClass);
		DistanceElement leg53 = testGraph.getData().getBestDistanceElement("ITGIT", "ITSAL", vesselClass);
		DistanceElement leg54 = testGraph.getData().getBestDistanceElement("ITSAL", "ESBCN", vesselClass);
		DistanceElement leg55 = testGraph.getData().getBestDistanceElement("ESBCN", "ESAGP", vesselClass);
		DistanceElement leg56 = testGraph.getData().getBestDistanceElement("ESAGP", "ESALG", vesselClass);
		DistanceElement leg57 = testGraph.getData().getBestDistanceElement("ESALG", "GRSKG", vesselClass);
		DistanceElement leg58 = testGraph.getData().getBestDistanceElement("GRSKG", "TRAMB", vesselClass);
		DistanceElement leg59 = testGraph.getData().getBestDistanceElement("TRAMB", "TRMER", vesselClass);
		DistanceElement leg13 = testGraph.getData().getBestDistanceElement("TRMER", "ILHFA", vesselClass);
//		DistanceElement leg14 = testGraph.getData().getDistanceElement("SYLTK", "ILHFA", vesselClass);
		DistanceElement leg15 = testGraph.getData().getBestDistanceElement("ILHFA", "EGPSD", vesselClass);
		distances2.add(leg10);
		distances2.add(leg11);
		distances2.add(leg12);
		distances2.add(leg52);
		distances2.add(leg53);
		distances2.add(leg54);
		distances2.add(leg55);
		distances2.add(leg56);
		distances2.add(leg57);
		distances2.add(leg58);
		distances2.add(leg59);
		distances2.add(leg13);
//		distances2.add(leg14);
		distances2.add(leg15);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg20 = testGraph.getData().getBestDistanceElement("TRAMB", "GEPTI", vesselClass);
		DistanceElement leg21 = testGraph.getData().getBestDistanceElement("GEPTI", "UAODS", vesselClass);
		DistanceElement leg22 = testGraph.getData().getBestDistanceElement("UAODS", "BGVAR", vesselClass);
		DistanceElement leg23 = testGraph.getData().getBestDistanceElement("BGVAR", "TRAMB", vesselClass);
		DistanceElement leg24 = testGraph.getData().getBestDistanceElement("TRAMB", "TRIZM", vesselClass);
		DistanceElement leg25 = testGraph.getData().getBestDistanceElement("TRIZM", "TRAMB", vesselClass);
		distances3.add(leg20);
		distances3.add(leg21);
		distances3.add(leg22);
		distances3.add(leg23);
		distances3.add(leg24);
		distances3.add(leg25);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances4 = new ArrayList<DistanceElement>();
		DistanceElement leg30 = testGraph.getData().getBestDistanceElement("ESALG", "MACAS", vesselClass);
		DistanceElement leg32 = testGraph.getData().getBestDistanceElement("MACAS", "MAPTM", vesselClass);
		DistanceElement leg33 = testGraph.getData().getBestDistanceElement("MAPTM", "ESALG", vesselClass);
		DistanceElement leg34 = testGraph.getData().getBestDistanceElement("ESALG", "ITGOA", vesselClass);
		DistanceElement leg35 = testGraph.getData().getBestDistanceElement("ITGOA", "ITGIT", vesselClass);
		DistanceElement leg36 = testGraph.getData().getBestDistanceElement("ITGIT", "ESALG", vesselClass);
		distances4.add(leg30);
		distances4.add(leg32);
		distances4.add(leg33);
		distances4.add(leg34);
		distances4.add(leg35);
		distances4.add(leg36);
		Rotation r4 = testGraph.createRotation(distances4, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
				ArrayList<DistanceElement> distances5 = new ArrayList<DistanceElement>();
		DistanceElement leg40 = testGraph.getData().getBestDistanceElement("MAPTM", "MAAGA", vesselClass);
		DistanceElement leg41 = testGraph.getData().getBestDistanceElement("MAAGA", "PTLEI", vesselClass);
		DistanceElement leg42 = testGraph.getData().getBestDistanceElement("PTLEI", "ESALG", vesselClass);
		DistanceElement leg43 = testGraph.getData().getBestDistanceElement("ESALG", "TNTUN", vesselClass);
		DistanceElement leg44 = testGraph.getData().getBestDistanceElement("TNTUN", "ITGIT", vesselClass);
		DistanceElement leg45 = testGraph.getData().getBestDistanceElement("ITGIT", "EGPSD", vesselClass);
		DistanceElement leg46 = testGraph.getData().getBestDistanceElement("EGPSD", "DZALG", vesselClass);
		DistanceElement leg47 = testGraph.getData().getBestDistanceElement("DZALG", "MAPTM", vesselClass);
		distances5.add(leg40);
		distances5.add(leg41);
		distances5.add(leg42);
		distances5.add(leg43);
		distances5.add(leg44);
		distances5.add(leg45);
		distances5.add(leg46);
		distances5.add(leg47);
		Rotation r5 = testGraph.createRotation(distances5, vesselClass);

		
		MulticommodityFlow.initialize(testGraph);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", testGraph.getResult().getRotations());
		System.out.println();
		System.out.println("Objective " + testGraph.getResult().getObjective());
		System.out.println("Flow profit " + testGraph.getResult().getFlowProfit(false));
		
		testGraph.saveOPLData("OPLData.dat");
	}
	
	public static void testMedManual2() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(2);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getBestDistanceElement("EGPSD", "ESALG", vesselClass);
		DistanceElement leg8 = testGraph.getData().getBestDistanceElement("ESALG", "MACAS", vesselClass);
		DistanceElement leg2 = testGraph.getData().getBestDistanceElement("MACAS", "MAAGA", vesselClass);
		DistanceElement leg7 = testGraph.getData().getBestDistanceElement("MAAGA", "PTLEI", vesselClass);
		DistanceElement leg3 = testGraph.getData().getBestDistanceElement("PTLEI", "MAPTM", vesselClass);
		DistanceElement leg4 = testGraph.getData().getBestDistanceElement("MAPTM", "ESALG", vesselClass);
		DistanceElement leg5 = testGraph.getData().getBestDistanceElement("ESALG", "EGALY", vesselClass);
		DistanceElement leg6 = testGraph.getData().getBestDistanceElement("EGALY", "EGPSD", vesselClass);
		distances.add(leg1);
		distances.add(leg8);
		distances.add(leg2);
		distances.add(leg7);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getBestDistanceElement("ESALG", "MAPTM", vesselClass);
		DistanceElement leg19 = testGraph.getData().getBestDistanceElement("MAPTM", "DZALG", vesselClass);
		DistanceElement leg11 = testGraph.getData().getBestDistanceElement("DZALG", "TNTUN", vesselClass);
		DistanceElement leg12 = testGraph.getData().getBestDistanceElement("TNTUN", "ITGIT", vesselClass);
		DistanceElement leg13 = testGraph.getData().getBestDistanceElement("ITGIT", "EGPSD", vesselClass);
		DistanceElement leg14 = testGraph.getData().getBestDistanceElement("EGPSD", "EGALY", vesselClass);
		DistanceElement leg15 = testGraph.getData().getBestDistanceElement("EGALY", "GRPIR", vesselClass);
		DistanceElement leg16 = testGraph.getData().getBestDistanceElement("GRPIR", "ITGIT", vesselClass);
//		DistanceElement leg17 = testGraph.getData().getDistanceElement("ITGIT", "ITGOA", vesselClass);
		DistanceElement leg18 = testGraph.getData().getBestDistanceElement("ITGIT", "ESBCN", vesselClass);
		DistanceElement leg20 = testGraph.getData().getBestDistanceElement("ESBCN", "ESALG", vesselClass);
		distances2.add(leg10);
		distances2.add(leg19);
		distances2.add(leg11);
		distances2.add(leg12);
		distances2.add(leg13);
		distances2.add(leg14);
		distances2.add(leg15);
		distances2.add(leg16);
//		distances2.add(leg17);
		distances2.add(leg18);
		distances2.add(leg20);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg30 = testGraph.getData().getBestDistanceElement("TRAMB", "EGPSD", vesselClass);
		DistanceElement leg32 = testGraph.getData().getBestDistanceElement("EGPSD", "ILASH", vesselClass);
		DistanceElement leg33 = testGraph.getData().getBestDistanceElement("ILASH", "ILHFA", vesselClass);
		DistanceElement leg34 = testGraph.getData().getBestDistanceElement("ILHFA", "EGPSD", vesselClass);
		DistanceElement leg35 = testGraph.getData().getBestDistanceElement("EGPSD", "TRAMB", vesselClass);
		distances3.add(leg30);
		distances3.add(leg32);
		distances3.add(leg33);
		distances3.add(leg34);
		distances3.add(leg35);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances4 = new ArrayList<DistanceElement>();
		DistanceElement leg40 = testGraph.getData().getBestDistanceElement("ESALG", "TNTUN", vesselClass);
		DistanceElement leg41 = testGraph.getData().getBestDistanceElement("TNTUN", "ITGIT", vesselClass);
		DistanceElement leg42 = testGraph.getData().getBestDistanceElement("ITGIT", "ITSAL", vesselClass);
		DistanceElement leg45 = testGraph.getData().getBestDistanceElement("ITSAL", "ESVLC", vesselClass);
		DistanceElement leg43 = testGraph.getData().getBestDistanceElement("ESVLC", "ESAGP", vesselClass);
		DistanceElement leg44 = testGraph.getData().getBestDistanceElement("ESAGP", "ESALG", vesselClass);
		distances4.add(leg40);
		distances4.add(leg41);
		distances4.add(leg42);
		distances4.add(leg45);
		distances4.add(leg43);
		distances4.add(leg44);
		Rotation r4 = testGraph.createRotation(distances4, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
				ArrayList<DistanceElement> distances5 = new ArrayList<DistanceElement>();
		DistanceElement leg50 = testGraph.getData().getBestDistanceElement("ESALG", "ESAGP", vesselClass);
		DistanceElement leg51 = testGraph.getData().getBestDistanceElement("ESAGP", "DZORN", vesselClass);
		DistanceElement leg53 = testGraph.getData().getBestDistanceElement("DZORN", "ITGIT", vesselClass);
		DistanceElement leg54 = testGraph.getData().getBestDistanceElement("ITGIT", "EGPSD", vesselClass);
		DistanceElement leg55 = testGraph.getData().getBestDistanceElement("EGPSD", "LBBEY", vesselClass);
		DistanceElement leg56 = testGraph.getData().getBestDistanceElement("LBBEY", "TRMER", vesselClass);
		DistanceElement leg57 = testGraph.getData().getBestDistanceElement("TRMER", "EGPSD", vesselClass);
		DistanceElement leg59 = testGraph.getData().getBestDistanceElement("EGPSD", "ESALG", vesselClass);
		distances5.add(leg50);
		distances5.add(leg51);
		distances5.add(leg53);
		distances5.add(leg54);
		distances5.add(leg55);
		distances5.add(leg56);
		distances5.add(leg57);
		distances5.add(leg59);
		Rotation r5 = testGraph.createRotation(distances5, vesselClass);
		
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances6 = new ArrayList<DistanceElement>();
		DistanceElement leg60 = testGraph.getData().getBestDistanceElement("TRAMB", "GEPTI", vesselClass);
		DistanceElement leg61 = testGraph.getData().getBestDistanceElement("GEPTI", "UAODS", vesselClass);
		DistanceElement leg62 = testGraph.getData().getBestDistanceElement("UAODS", "BGVAR", vesselClass);
		DistanceElement leg63 = testGraph.getData().getBestDistanceElement("BGVAR", "TRAMB", vesselClass);
		DistanceElement leg64 = testGraph.getData().getBestDistanceElement("TRAMB", "TRIZM", vesselClass);
		DistanceElement leg65 = testGraph.getData().getBestDistanceElement("TRIZM", "TRAMB", vesselClass);
		distances6.add(leg60);
		distances6.add(leg61);
		distances6.add(leg62);
		distances6.add(leg63);
		distances6.add(leg64);
		distances6.add(leg65);
		Rotation r6 = testGraph.createRotation(distances6, vesselClass);
		
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances7 = new ArrayList<DistanceElement>();
		DistanceElement leg70 = testGraph.getData().getBestDistanceElement("ESALG", "ESTAR", vesselClass);
		DistanceElement leg71 = testGraph.getData().getBestDistanceElement("ESTAR", "ITGOA", vesselClass);
		DistanceElement leg72 = testGraph.getData().getBestDistanceElement("ITGOA", "ITGIT", vesselClass);
		DistanceElement leg73 = testGraph.getData().getBestDistanceElement("ITGIT", "MAPTM", vesselClass);
		DistanceElement leg74 = testGraph.getData().getBestDistanceElement("MAPTM", "ESALG", vesselClass);
		distances7.add(leg70);
		distances7.add(leg71);
		distances7.add(leg72);
		distances7.add(leg73);
		distances7.add(leg74);
		Rotation r7 = testGraph.createRotation(distances7, vesselClass);
		
		MulticommodityFlow.initialize(testGraph);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", testGraph.getResult().getRotations());
		System.out.println();
		System.out.println("Objective " + testGraph.getResult().getObjective());
		System.out.println("Flow profit " + testGraph.getResult().getFlowProfit(false));
		
		testGraph.saveOPLData("OPLData.dat");
	}

	
	public static void testMed() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getBestDistanceElement("MACAS", "MAAGA", vesselClass);
		DistanceElement leg2 = testGraph.getData().getBestDistanceElement("MAAGA", "ESVGO", vesselClass);
		DistanceElement leg3 = testGraph.getData().getBestDistanceElement("ESVGO", "PTLEI", vesselClass);
		DistanceElement leg4 = testGraph.getData().getBestDistanceElement("PTLEI", "MAPTM", vesselClass);
		DistanceElement leg5 = testGraph.getData().getBestDistanceElement("MAPTM", "ESALG", vesselClass);
		DistanceElement leg6 = testGraph.getData().getBestDistanceElement("ESALG", "ESAGP", vesselClass);
		DistanceElement leg7 = testGraph.getData().getBestDistanceElement("ESAGP", "DZBJA", vesselClass);
		DistanceElement leg8 = testGraph.getData().getBestDistanceElement("DZBJA", "TNTUN", vesselClass);
		DistanceElement leg9 = testGraph.getData().getBestDistanceElement("TNTUN", "ITGOA", vesselClass);
		DistanceElement leg10 = testGraph.getData().getBestDistanceElement("ITGOA", "ESTAR", vesselClass);
		DistanceElement leg11 = testGraph.getData().getBestDistanceElement("ESTAR", "ESAGP", vesselClass);
		DistanceElement leg12 = testGraph.getData().getBestDistanceElement("ESAGP", "MACAS", vesselClass);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		distances.add(leg7);
		distances.add(leg8);
		distances.add(leg9);
		distances.add(leg10);
		distances.add(leg11);
		distances.add(leg12);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg20 = testGraph.getData().getBestDistanceElement("SYLTK", "ILHFA", vesselClass);
		DistanceElement leg21 = testGraph.getData().getBestDistanceElement("ILHFA", "EGDAM", vesselClass);
		DistanceElement leg22 = testGraph.getData().getBestDistanceElement("EGDAM", "EGPSD", vesselClass);
		DistanceElement leg23 = testGraph.getData().getBestDistanceElement("EGPSD", "EGALY", vesselClass);
		DistanceElement leg24 = testGraph.getData().getBestDistanceElement("EGALY", "EGPSD", vesselClass);
		DistanceElement leg25 = testGraph.getData().getBestDistanceElement("EGPSD", "ILASH", vesselClass);
		DistanceElement leg26 = testGraph.getData().getBestDistanceElement("ILASH", "TRMER", vesselClass);
		DistanceElement leg27 = testGraph.getData().getBestDistanceElement("TRMER", "SYLTK", vesselClass);
		distances2.add(leg20);
		distances2.add(leg21);
		distances2.add(leg22);
		distances2.add(leg23);
		distances2.add(leg24);
		distances2.add(leg25);
		distances2.add(leg26);
		distances2.add(leg27);
		Rotation r2 = testGraph.createRotation(distances2, vesselClass);
		
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances3 = new ArrayList<DistanceElement>();
		DistanceElement leg30 = testGraph.getData().getBestDistanceElement("ESAGP", "DZALG", vesselClass);
		DistanceElement leg31 = testGraph.getData().getBestDistanceElement("DZALG", "DZSKI", vesselClass);
		DistanceElement leg32 = testGraph.getData().getBestDistanceElement("DZSKI", "TRIZM", vesselClass);
		DistanceElement leg33 = testGraph.getData().getBestDistanceElement("TRIZM", "ITSAL", vesselClass);
		DistanceElement leg34 = testGraph.getData().getBestDistanceElement("ITSAL", "ITGOA", vesselClass);
		DistanceElement leg35 = testGraph.getData().getBestDistanceElement("ITGOA", "ESALG", vesselClass);
		DistanceElement leg36 = testGraph.getData().getBestDistanceElement("ESALG", "ESAGP", vesselClass);
		distances3.add(leg30);
		distances3.add(leg31);
		distances3.add(leg32);
		distances3.add(leg33);
		distances3.add(leg34);
		distances3.add(leg35);
		distances3.add(leg36);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances4 = new ArrayList<DistanceElement>();
		DistanceElement leg40 = testGraph.getData().getBestDistanceElement("TNTUN", "ITGIT", vesselClass);
		DistanceElement leg41 = testGraph.getData().getBestDistanceElement("ITGIT", "ITTRS", vesselClass);
		DistanceElement leg42 = testGraph.getData().getBestDistanceElement("ITTRS", "GRPIR", vesselClass);
		DistanceElement leg43 = testGraph.getData().getBestDistanceElement("GRPIR", "TRAMB", vesselClass);
		DistanceElement leg44 = testGraph.getData().getBestDistanceElement("TRAMB", "CYLMS", vesselClass);
		DistanceElement leg45 = testGraph.getData().getBestDistanceElement("CYLMS", "EGPSD", vesselClass);
		DistanceElement leg46 = testGraph.getData().getBestDistanceElement("EGPSD", "TNTUN", vesselClass);
		distances4.add(leg40);
		distances4.add(leg41);
		distances4.add(leg42);
		distances4.add(leg43);
		distances4.add(leg44);
		distances4.add(leg45);
		distances4.add(leg46);
		Rotation r4 = testGraph.createRotation(distances4, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(2);
		ArrayList<DistanceElement> distances5 = new ArrayList<DistanceElement>();
		DistanceElement leg50 = testGraph.getData().getBestDistanceElement("LBBEY", "EGPSD", vesselClass);
		DistanceElement leg51 = testGraph.getData().getBestDistanceElement("EGPSD", "ITGIT", vesselClass);
		DistanceElement leg52 = testGraph.getData().getBestDistanceElement("ITGIT", "ESVLC", vesselClass);
		DistanceElement leg53 = testGraph.getData().getBestDistanceElement("ESVLC", "ESALG", vesselClass);
		DistanceElement leg54 = testGraph.getData().getBestDistanceElement("ESALG", "ESAGP", vesselClass);
		DistanceElement leg55 = testGraph.getData().getBestDistanceElement("ESAGP", "MAPTM", vesselClass);
		DistanceElement leg56 = testGraph.getData().getBestDistanceElement("MAPTM", "ESLPA", vesselClass);
		DistanceElement leg57 = testGraph.getData().getBestDistanceElement("ESLPA", "ESBCN", vesselClass);
		DistanceElement leg58 = testGraph.getData().getBestDistanceElement("ESBCN", "ITGOA", vesselClass);
		DistanceElement leg59 = testGraph.getData().getBestDistanceElement("ITGOA", "LBBEY", vesselClass);
		distances5.add(leg50);
		distances5.add(leg51);
		distances5.add(leg52);
		distances5.add(leg53);
		distances5.add(leg54);
		distances5.add(leg55);
		distances5.add(leg56);
		distances5.add(leg57);
		distances5.add(leg58);
		distances5.add(leg59);
		Rotation r5 = testGraph.createRotation(distances5, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances6 = new ArrayList<DistanceElement>();
		DistanceElement leg60 = testGraph.getData().getBestDistanceElement("MACAS", "ESALG", vesselClass);
		DistanceElement leg61 = testGraph.getData().getBestDistanceElement("ESALG", "DZORN", vesselClass);
		DistanceElement leg62 = testGraph.getData().getBestDistanceElement("DZORN", "MACAS", vesselClass);
		distances6.add(leg60);
		distances6.add(leg61);
		distances6.add(leg62);
		Rotation r6 = testGraph.createRotation(distances6, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances7 = new ArrayList<DistanceElement>();
		DistanceElement leg70 = testGraph.getData().getBestDistanceElement("EGPSD", "ILASH", vesselClass);
		DistanceElement leg71 = testGraph.getData().getBestDistanceElement("ILASH", "ITGIT", vesselClass);
		DistanceElement leg72 = testGraph.getData().getBestDistanceElement("ITGIT", "ITGOA", vesselClass);
		DistanceElement leg73 = testGraph.getData().getBestDistanceElement("ITGOA", "GEPTI", vesselClass);
		DistanceElement leg74 = testGraph.getData().getBestDistanceElement("GEPTI", "UAODS", vesselClass);
		DistanceElement leg75 = testGraph.getData().getBestDistanceElement("UAODS", "BGVAR", vesselClass);
		DistanceElement leg76 = testGraph.getData().getBestDistanceElement("BGVAR", "EGPSD", vesselClass);
		distances7.add(leg70);
		distances7.add(leg71);
		distances7.add(leg72);
		distances7.add(leg73);
		distances7.add(leg74);
		distances7.add(leg75);
		distances7.add(leg76);
		Rotation r7 = testGraph.createRotation(distances7, vesselClass);
		
		
		MulticommodityFlow.initialize(testGraph);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", testGraph.getResult().getRotations());
		System.out.println();
		System.out.println("Objective " + testGraph.getResult().getObjective());
		System.out.println("Flow profit " + testGraph.getResult().getFlowProfit(false));
		
		testGraph.saveOPLData("OPLData.dat");
	}
	
	public static void testWorldSmallAuto()throws FileNotFoundException{
		Graph graph = new Graph("Demand_WorldSmall.csv", "fleet_WorldSmall.csv");
		initialize(graph);
		ArrayList<AuxEdge> sortedEdges = AuxGraph.getSortedAuxEdges();
		VesselClass feeder450 = graph.getData().getVesselClasses().get(0);
		VesselClass feeder800 = graph.getData().getVesselClasses().get(1);
		VesselClass panamax1200 = graph.getData().getVesselClasses().get(2);
		VesselClass panamax2400 = graph.getData().getVesselClasses().get(3);
		VesselClass postPanamax = graph.getData().getVesselClasses().get(4);
		VesselClass superPanamax = graph.getData().getVesselClasses().get(5);
		
		ComputeRotations.createAuxFlowRotation(10, sortedEdges, superPanamax);
		
		/*//WorldSmall1.
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(6, sortedEdges, postPanamax);
		*/
		//WorldSmall2 & 3.
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, postPanamax);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, postPanamax);
		
		//WorldSmall1 & 3.
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(6, sortedEdges, panamax2400);
		
		/*//WorldSmall2.
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(11, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(10, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax2400);
		ComputeRotations.createAuxFlowRotation(6, sortedEdges, panamax2400);
		*/
		/*//WorldSmall1.
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(9, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, panamax1200);
		*/
		//WorldSmall2 & 3.
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(8, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(7, sortedEdges, panamax1200);
		ComputeRotations.createAuxFlowRotation(6, sortedEdges, panamax1200);
		
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder800);
		ComputeRotations.createAuxFlowRotation(4, sortedEdges, feeder800);
		
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder450);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder450);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder450);
		ComputeRotations.createAuxFlowRotation(5, sortedEdges, feeder450);
		ComputeRotations.createAuxFlowRotation(4, sortedEdges, feeder450);
		
		System.out.println("Rotations generated.");
		
//		ComputeRotations.addPorts();
		MulticommodityFlow.run();
//		ComputeRotations.addPorts();
//		ComputeRotations.removePorts();
//		MulticommodityFlow.run();
		System.out.println("Multicommodity run.");
		MulticommodityFlow.saveODSol("ODSol.csv", graph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", graph.getResult().getRotations());
		System.out.println();
		System.out.println("Objective " + graph.getResult().getObjective());
		System.out.println("Flow profit " + graph.getResult().getFlowProfit(false));
		
		graph.saveOPLData("OPLData.dat");
		
	}

}
