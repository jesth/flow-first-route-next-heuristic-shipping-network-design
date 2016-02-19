import java.io.FileNotFoundException;
import java.util.ArrayList;

import AuxFlow.AuxDijkstra;
import AuxFlow.AuxGraph;
import AuxFlow.AuxRun;
import Data.DistanceElement;
import Data.VesselClass;
import Graph.Graph;
import Methods.ComputeRotations;
import Methods.MulticommodityFlow;
import Results.Result;
import Results.Rotation;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		
		testBaltic();
//		testBaltic();
//		testAutomatic();
//		testBalticManual();
//		testMedManual();
//		testMed();
//		testAux();
//		testMedManual2();
		
	}
	
	public static void testAux() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		AuxRun.initialize(testGraph.getData(), 10);
	}
	
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
	
	public static void initialize(Graph graph) throws FileNotFoundException{
		ComputeRotations.intialize(graph);
		MulticommodityFlow.initialize(graph);
		Result.initialize(graph);
	}
	
	public static void testBalticManual() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Baltic.csv", "fleet_Baltic.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("DEBRV", "RULED", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("RULED", "DEBRV", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("DEBRV", "NOSVG", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("NOSVG", "SEGOT", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("SEGOT", "DEBRV", false, false);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg3);
		distances.add(leg4);
		distances.add(leg5);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("DKAAR", "DEBRV", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("DEBRV", "RUKGD", false, false);
		DistanceElement leg13 = testGraph.getData().getDistanceElement("RUKGD", "FIKTK", false, false);
		DistanceElement leg14 = testGraph.getData().getDistanceElement("FIKTK", "DEBRV", false, false);
		DistanceElement leg15 = testGraph.getData().getDistanceElement("DEBRV", "RULED", false, false);
		DistanceElement leg16 = testGraph.getData().getDistanceElement("RULED", "PLGDY", false, false);
		DistanceElement leg17 = testGraph.getData().getDistanceElement("PLGDY", "DEBRV", false, false);
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
//		DistanceElement leg20 = testGraph.getData().getDistanceElement("DEBRV", "DKAAR", false, false);
//		DistanceElement leg21 = testGraph.getData().getDistanceElement("DKAAR", "DEBRV", false, false);
//		distances3.add(leg20);
//		distances3.add(leg21);
//		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
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
	
	public static void testMedManual() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(2);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("TRAMB", "ESALG", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("ESALG", "MAPTM", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("MAPTM", "EGPSD", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("EGPSD", "ITGIT", false, false);
		DistanceElement leg6 = testGraph.getData().getDistanceElement("ITGIT", "EGALY", false, false);
		DistanceElement leg7 = testGraph.getData().getDistanceElement("EGALY", "TRAMB", false, false);
		distances.add(leg1);
		distances.add(leg2);
		distances.add(leg4);
		distances.add(leg5);
		distances.add(leg6);
		distances.add(leg7);
		Rotation r = testGraph.createRotation(distances, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances2 = new ArrayList<DistanceElement>();
		DistanceElement leg10 = testGraph.getData().getDistanceElement("EGPSD", "ILASH", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("ILASH", "LBBEY", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("LBBEY", "TRMER", false, false);
		DistanceElement leg52 = testGraph.getData().getDistanceElement("TRMER", "ITGIT", false, false);
		DistanceElement leg53 = testGraph.getData().getDistanceElement("ITGIT", "ITSAL", false, false);
		DistanceElement leg54 = testGraph.getData().getDistanceElement("ITSAL", "ESBCN", false, false);
		DistanceElement leg55 = testGraph.getData().getDistanceElement("ESBCN", "ESAGP", false, false);
		DistanceElement leg56 = testGraph.getData().getDistanceElement("ESAGP", "ESALG", false, false);
		DistanceElement leg57 = testGraph.getData().getDistanceElement("ESALG", "GRSKG", false, false);
		DistanceElement leg58 = testGraph.getData().getDistanceElement("GRSKG", "TRAMB", false, false);
		DistanceElement leg59 = testGraph.getData().getDistanceElement("TRAMB", "TRMER", false, false);
		DistanceElement leg13 = testGraph.getData().getDistanceElement("TRMER", "ILHFA", false, false);
//		DistanceElement leg14 = testGraph.getData().getDistanceElement("SYLTK", "ILHFA", false, false);
		DistanceElement leg15 = testGraph.getData().getDistanceElement("ILHFA", "EGPSD", false, false);
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
		DistanceElement leg20 = testGraph.getData().getDistanceElement("TRAMB", "GEPTI", false, false);
		DistanceElement leg21 = testGraph.getData().getDistanceElement("GEPTI", "UAODS", false, false);
		DistanceElement leg22 = testGraph.getData().getDistanceElement("UAODS", "BGVAR", false, false);
		DistanceElement leg23 = testGraph.getData().getDistanceElement("BGVAR", "TRAMB", false, false);
		DistanceElement leg24 = testGraph.getData().getDistanceElement("TRAMB", "TRIZM", false, false);
		DistanceElement leg25 = testGraph.getData().getDistanceElement("TRIZM", "TRAMB", false, false);
		distances3.add(leg20);
		distances3.add(leg21);
		distances3.add(leg22);
		distances3.add(leg23);
		distances3.add(leg24);
		distances3.add(leg25);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances4 = new ArrayList<DistanceElement>();
		DistanceElement leg30 = testGraph.getData().getDistanceElement("ESALG", "MACAS", false, false);
		DistanceElement leg32 = testGraph.getData().getDistanceElement("MACAS", "MAPTM", false, false);
		DistanceElement leg33 = testGraph.getData().getDistanceElement("MAPTM", "ESALG", false, false);
		DistanceElement leg34 = testGraph.getData().getDistanceElement("ESALG", "ITGOA", false, false);
		DistanceElement leg35 = testGraph.getData().getDistanceElement("ITGOA", "ITGIT", false, false);
		DistanceElement leg36 = testGraph.getData().getDistanceElement("ITGIT", "ESALG", false, false);
		distances4.add(leg30);
		distances4.add(leg32);
		distances4.add(leg33);
		distances4.add(leg34);
		distances4.add(leg35);
		distances4.add(leg36);
		Rotation r4 = testGraph.createRotation(distances4, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
				ArrayList<DistanceElement> distances5 = new ArrayList<DistanceElement>();
		DistanceElement leg40 = testGraph.getData().getDistanceElement("MAPTM", "MAAGA", false, false);
		DistanceElement leg41 = testGraph.getData().getDistanceElement("MAAGA", "PTLEI", false, false);
		DistanceElement leg42 = testGraph.getData().getDistanceElement("PTLEI", "ESALG", false, false);
		DistanceElement leg43 = testGraph.getData().getDistanceElement("ESALG", "TNTUN", false, false);
		DistanceElement leg44 = testGraph.getData().getDistanceElement("TNTUN", "ITGIT", false, false);
		DistanceElement leg45 = testGraph.getData().getDistanceElement("ITGIT", "EGPSD", false, false);
		DistanceElement leg46 = testGraph.getData().getDistanceElement("EGPSD", "DZALG", false, false);
		DistanceElement leg47 = testGraph.getData().getDistanceElement("DZALG", "MAPTM", false, false);
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
		Result.initialize(testGraph);
		Result.addRotation(r);
		Result.addRotation(r2);
		Result.addRotation(r3);
		Result.addRotation(r4);
		Result.addRotation(r5);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", Result.getRotations());
		System.out.println();
		System.out.println("Objective " + Result.getObjective());
		System.out.println("Flow profit " + Result.getFlowProfit(false));
	}
	
	public static void testMedManual2() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(2);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("EGPSD", "ESALG", false, false);
		DistanceElement leg8 = testGraph.getData().getDistanceElement("ESALG", "MACAS", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("MACAS", "MAAGA", false, false);
		DistanceElement leg7 = testGraph.getData().getDistanceElement("MAAGA", "PTLEI", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("PTLEI", "MAPTM", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("MAPTM", "ESALG", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("ESALG", "EGALY", false, false);
		DistanceElement leg6 = testGraph.getData().getDistanceElement("EGALY", "EGPSD", false, false);
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
		DistanceElement leg10 = testGraph.getData().getDistanceElement("ESALG", "MAPTM", false, false);
		DistanceElement leg19 = testGraph.getData().getDistanceElement("MAPTM", "DZALG", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("DZALG", "TNTUN", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("TNTUN", "ITGIT", false, false);
		DistanceElement leg13 = testGraph.getData().getDistanceElement("ITGIT", "EGPSD", false, false);
		DistanceElement leg14 = testGraph.getData().getDistanceElement("EGPSD", "EGALY", false, false);
		DistanceElement leg15 = testGraph.getData().getDistanceElement("EGALY", "GRPIR", false, false);
		DistanceElement leg16 = testGraph.getData().getDistanceElement("GRPIR", "ITGIT", false, false);
//		DistanceElement leg17 = testGraph.getData().getDistanceElement("ITGIT", "ITGOA", false, false);
		DistanceElement leg18 = testGraph.getData().getDistanceElement("ITGIT", "ESBCN", false, false);
		DistanceElement leg20 = testGraph.getData().getDistanceElement("ESBCN", "ESALG", false, false);
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
		DistanceElement leg30 = testGraph.getData().getDistanceElement("TRAMB", "EGPSD", false, false);
		DistanceElement leg32 = testGraph.getData().getDistanceElement("EGPSD", "ILASH", false, false);
		DistanceElement leg33 = testGraph.getData().getDistanceElement("ILASH", "ILHFA", false, false);
		DistanceElement leg34 = testGraph.getData().getDistanceElement("ILHFA", "EGPSD", false, false);
		DistanceElement leg35 = testGraph.getData().getDistanceElement("EGPSD", "TRAMB", false, false);
		distances3.add(leg30);
		distances3.add(leg32);
		distances3.add(leg33);
		distances3.add(leg34);
		distances3.add(leg35);
		Rotation r3 = testGraph.createRotation(distances3, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances4 = new ArrayList<DistanceElement>();
		DistanceElement leg40 = testGraph.getData().getDistanceElement("ESALG", "TNTUN", false, false);
		DistanceElement leg41 = testGraph.getData().getDistanceElement("TNTUN", "ITGIT", false, false);
		DistanceElement leg42 = testGraph.getData().getDistanceElement("ITGIT", "ITSAL", false, false);
		DistanceElement leg45 = testGraph.getData().getDistanceElement("ITSAL", "ESVLC", false, false);
		DistanceElement leg43 = testGraph.getData().getDistanceElement("ESVLC", "ESAGP", false, false);
		DistanceElement leg44 = testGraph.getData().getDistanceElement("ESAGP", "ESALG", false, false);
		distances4.add(leg40);
		distances4.add(leg41);
		distances4.add(leg42);
		distances4.add(leg45);
		distances4.add(leg43);
		distances4.add(leg44);
		Rotation r4 = testGraph.createRotation(distances4, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
				ArrayList<DistanceElement> distances5 = new ArrayList<DistanceElement>();
		DistanceElement leg50 = testGraph.getData().getDistanceElement("ESALG", "ESAGP", false, false);
		DistanceElement leg51 = testGraph.getData().getDistanceElement("ESAGP", "DZORN", false, false);
		DistanceElement leg53 = testGraph.getData().getDistanceElement("DZORN", "ITGIT", false, false);
		DistanceElement leg54 = testGraph.getData().getDistanceElement("ITGIT", "EGPSD", false, false);
		DistanceElement leg55 = testGraph.getData().getDistanceElement("EGPSD", "LBBEY", false, false);
		DistanceElement leg56 = testGraph.getData().getDistanceElement("LBBEY", "TRMER", false, false);
		DistanceElement leg57 = testGraph.getData().getDistanceElement("TRMER", "EGPSD", false, false);
		DistanceElement leg59 = testGraph.getData().getDistanceElement("EGPSD", "ESALG", false, false);
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
		DistanceElement leg60 = testGraph.getData().getDistanceElement("TRAMB", "GEPTI", false, false);
		DistanceElement leg61 = testGraph.getData().getDistanceElement("GEPTI", "UAODS", false, false);
		DistanceElement leg62 = testGraph.getData().getDistanceElement("UAODS", "BGVAR", false, false);
		DistanceElement leg63 = testGraph.getData().getDistanceElement("BGVAR", "TRAMB", false, false);
		DistanceElement leg64 = testGraph.getData().getDistanceElement("TRAMB", "TRIZM", false, false);
		DistanceElement leg65 = testGraph.getData().getDistanceElement("TRIZM", "TRAMB", false, false);
		distances6.add(leg60);
		distances6.add(leg61);
		distances6.add(leg62);
		distances6.add(leg63);
		distances6.add(leg64);
		distances6.add(leg65);
		Rotation r6 = testGraph.createRotation(distances6, vesselClass);
		
		
		vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances7 = new ArrayList<DistanceElement>();
		DistanceElement leg70 = testGraph.getData().getDistanceElement("ESALG", "ESTAR", false, false);
		DistanceElement leg71 = testGraph.getData().getDistanceElement("ESTAR", "ITGOA", false, false);
		DistanceElement leg72 = testGraph.getData().getDistanceElement("ITGOA", "ITGIT", false, false);
		DistanceElement leg73 = testGraph.getData().getDistanceElement("ITGIT", "MAPTM", false, false);
		DistanceElement leg74 = testGraph.getData().getDistanceElement("MAPTM", "ESALG", false, false);
		distances7.add(leg70);
		distances7.add(leg71);
		distances7.add(leg72);
		distances7.add(leg73);
		distances7.add(leg74);
		Rotation r7 = testGraph.createRotation(distances7, vesselClass);
		
		MulticommodityFlow.initialize(testGraph);
		Result.initialize(testGraph);
		Result.addRotation(r);
		Result.addRotation(r2);
		Result.addRotation(r3);
		Result.addRotation(r4);
		Result.addRotation(r5);
		Result.addRotation(r6);
		Result.addRotation(r7);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", Result.getRotations());
		System.out.println();
		System.out.println("Objective " + Result.getObjective());
		System.out.println("Flow profit " + Result.getFlowProfit(false));
	}

	
	public static void testMed() throws FileNotFoundException{
		Graph testGraph = new Graph("Demand_Mediterranean.csv", "fleet_Mediterranean.csv");
		VesselClass vesselClass = testGraph.getData().getVesselClasses().get(1);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		DistanceElement leg1 = testGraph.getData().getDistanceElement("MACAS", "MAAGA", false, false);
		DistanceElement leg2 = testGraph.getData().getDistanceElement("MAAGA", "ESVGO", false, false);
		DistanceElement leg3 = testGraph.getData().getDistanceElement("ESVGO", "PTLEI", false, false);
		DistanceElement leg4 = testGraph.getData().getDistanceElement("PTLEI", "MAPTM", false, false);
		DistanceElement leg5 = testGraph.getData().getDistanceElement("MAPTM", "ESALG", false, false);
		DistanceElement leg6 = testGraph.getData().getDistanceElement("ESALG", "ESAGP", false, false);
		DistanceElement leg7 = testGraph.getData().getDistanceElement("ESAGP", "DZBJA", false, false);
		DistanceElement leg8 = testGraph.getData().getDistanceElement("DZBJA", "TNTUN", false, false);
		DistanceElement leg9 = testGraph.getData().getDistanceElement("TNTUN", "ITGOA", false, false);
		DistanceElement leg10 = testGraph.getData().getDistanceElement("ITGOA", "ESTAR", false, false);
		DistanceElement leg11 = testGraph.getData().getDistanceElement("ESTAR", "ESAGP", false, false);
		DistanceElement leg12 = testGraph.getData().getDistanceElement("ESAGP", "MACAS", false, false);
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
		DistanceElement leg20 = testGraph.getData().getDistanceElement("SYLTK", "ILHFA", false, false);
		DistanceElement leg21 = testGraph.getData().getDistanceElement("ILHFA", "EGDAM", false, false);
		DistanceElement leg22 = testGraph.getData().getDistanceElement("EGDAM", "EGPSD", false, false);
		DistanceElement leg23 = testGraph.getData().getDistanceElement("EGPSD", "EGALY", false, false);
		DistanceElement leg24 = testGraph.getData().getDistanceElement("EGALY", "EGPSD", false, false);
		DistanceElement leg25 = testGraph.getData().getDistanceElement("EGPSD", "ILASH", false, false);
		DistanceElement leg26 = testGraph.getData().getDistanceElement("ILASH", "TRMER", false, false);
		DistanceElement leg27 = testGraph.getData().getDistanceElement("TRMER", "SYLTK", false, false);
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
		DistanceElement leg30 = testGraph.getData().getDistanceElement("ESAGP", "DZALG", false, false);
		DistanceElement leg31 = testGraph.getData().getDistanceElement("DZALG", "DZSKI", false, false);
		DistanceElement leg32 = testGraph.getData().getDistanceElement("DZSKI", "TRIZM", false, false);
		DistanceElement leg33 = testGraph.getData().getDistanceElement("TRIZM", "ITSAL", false, false);
		DistanceElement leg34 = testGraph.getData().getDistanceElement("ITSAL", "ITGOA", false, false);
		DistanceElement leg35 = testGraph.getData().getDistanceElement("ITGOA", "ESALG", false, false);
		DistanceElement leg36 = testGraph.getData().getDistanceElement("ESALG", "ESAGP", false, false);
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
		DistanceElement leg40 = testGraph.getData().getDistanceElement("TNTUN", "ITGIT", false, false);
		DistanceElement leg41 = testGraph.getData().getDistanceElement("ITGIT", "ITTRS", false, false);
		DistanceElement leg42 = testGraph.getData().getDistanceElement("ITTRS", "GRPIR", false, false);
		DistanceElement leg43 = testGraph.getData().getDistanceElement("GRPIR", "TRAMB", false, false);
		DistanceElement leg44 = testGraph.getData().getDistanceElement("TRAMB", "CYLMS", false, false);
		DistanceElement leg45 = testGraph.getData().getDistanceElement("CYLMS", "EGPSD", false, false);
		DistanceElement leg46 = testGraph.getData().getDistanceElement("EGPSD", "TNTUN", false, false);
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
		DistanceElement leg50 = testGraph.getData().getDistanceElement("LBBEY", "EGPSD", false, false);
		DistanceElement leg51 = testGraph.getData().getDistanceElement("EGPSD", "ITGIT", false, false);
		DistanceElement leg52 = testGraph.getData().getDistanceElement("ITGIT", "ESVLC", false, false);
		DistanceElement leg53 = testGraph.getData().getDistanceElement("ESVLC", "ESALG", false, false);
		DistanceElement leg54 = testGraph.getData().getDistanceElement("ESALG", "ESAGP", false, false);
		DistanceElement leg55 = testGraph.getData().getDistanceElement("ESAGP", "MAPTM", false, false);
		DistanceElement leg56 = testGraph.getData().getDistanceElement("MAPTM", "ESLPA", false, false);
		DistanceElement leg57 = testGraph.getData().getDistanceElement("ESLPA", "ESBCN", false, false);
		DistanceElement leg58 = testGraph.getData().getDistanceElement("ESBCN", "ITGOA", false, false);
		DistanceElement leg59 = testGraph.getData().getDistanceElement("ITGOA", "LBBEY", false, false);
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
		DistanceElement leg60 = testGraph.getData().getDistanceElement("MACAS", "ESALG", false, false);
		DistanceElement leg61 = testGraph.getData().getDistanceElement("ESALG", "DZORN", false, false);
		DistanceElement leg62 = testGraph.getData().getDistanceElement("DZORN", "MACAS", false, false);
		distances6.add(leg60);
		distances6.add(leg61);
		distances6.add(leg62);
		Rotation r6 = testGraph.createRotation(distances6, vesselClass);
		
		vesselClass = testGraph.getData().getVesselClasses().get(0);
		ArrayList<DistanceElement> distances7 = new ArrayList<DistanceElement>();
		DistanceElement leg70 = testGraph.getData().getDistanceElement("EGPSD", "ILASH", false, false);
		DistanceElement leg71 = testGraph.getData().getDistanceElement("ILASH", "ITGIT", false, false);
		DistanceElement leg72 = testGraph.getData().getDistanceElement("ITGIT", "ITGOA", false, false);
		DistanceElement leg73 = testGraph.getData().getDistanceElement("ITGOA", "GEPTI", false, false);
		DistanceElement leg74 = testGraph.getData().getDistanceElement("GEPTI", "UAODS", false, false);
		DistanceElement leg75 = testGraph.getData().getDistanceElement("UAODS", "BGVAR", false, false);
		DistanceElement leg76 = testGraph.getData().getDistanceElement("BGVAR", "EGPSD", false, false);
		distances7.add(leg70);
		distances7.add(leg71);
		distances7.add(leg72);
		distances7.add(leg73);
		distances7.add(leg74);
		distances7.add(leg75);
		distances7.add(leg76);
		Rotation r7 = testGraph.createRotation(distances7, vesselClass);
		
		
		MulticommodityFlow.initialize(testGraph);
		Result.initialize(testGraph);
		Result.addRotation(r);
		Result.addRotation(r2);
		Result.addRotation(r3);
		Result.addRotation(r4);
		Result.addRotation(r5);
		Result.addRotation(r6);
		Result.addRotation(r7);
		long time = System.currentTimeMillis();
		MulticommodityFlow.run();
		long timeUse = System.currentTimeMillis() - time;
		System.out.println("Running for " + timeUse + " ms");
		MulticommodityFlow.saveODSol("ODSol.csv", testGraph.getData().getDemands());
		MulticommodityFlow.saveRotationSol("RotationSol.csv", Result.getRotations());
		System.out.println();
		System.out.println("Objective " + Result.getObjective());
		System.out.println("Flow profit " + Result.getFlowProfit(false));
	}

}
