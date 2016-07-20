package AuxFlow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Data.Data;
import Data.PortData;
import Graph.Graph;
import Results.Rotation;

public class AuxRun {
	private AuxGraph graph;
	private int iterations;
	private int rand;
	
	public AuxRun(Graph orgGraph, int iterations, int rand){
		graph = new AuxGraph(orgGraph);
		this.iterations = iterations;
		this.rand = rand;
	}
	
	public AuxRun(Graph orgGraph, ArrayList<Rotation> rotationsToKeep, int iterations, int rand){
		graph = new AuxGraph(orgGraph);
		for(Rotation r : rotationsToKeep){
			graph.addRotation(r);
		}
		this.iterations = iterations;
		this.rand = rand;
	}

//	public void initialize(Graph originalGraph, int iterationsIn){
//		graph = new AuxGraph(originalGraph);
//		iterations = iterationsIn;
////		graph.addFirstRotationManual();
////		graph.addSecondRotationManual();
////		graph.addThirdRotationManual();
////		graph.addFourthRotationManual();
////		graph.addFifthRotationManual();
//		run();
//		graph.serialize();
//	}

	public AuxGraph run(){
		for(int i = 0; i < iterations; i++){
//			System.out.println("Running iteration " + i);
			graph.runDijkstra(iterations, rand);
			System.out.println("AuxDijkstra iteration " + i + " done");
		}
//		for(AuxEdge ae : graph.getSortedAuxEdges()){
//			System.out.print(ae.getFromNode().getUNLocode()+"-"+ae.getToNode().getUNLocode() + " with load " + ae.getAvgLoad());
//			if(ae.isRotation()){
//				System.out.println(" is rotation.");
//			} else {
//				System.out.println(" is not rotation.");
//			}
//		}
//		saveJson("AuxLoad.geoJSON", 5);
//		saveODJson("PortsWorldLarge.geoJSON");
//		graph.serialize();
		return graph;
	}
	
	private void saveJson(String fileName, int minLoad){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("{");
			out.newLine();
			out.write("\"type\": \"FeatureCollection\",");
			out.newLine();
			out.write("\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },");
			out.newLine();
			out.write("\"features\": [");
			out.newLine();
			int counter = 1;
			for(AuxEdge e : graph.getEdges()){
				if(e.getAvgLoad() >= minLoad){
					if(counter > 1){
						out.write(",");
						out.newLine();
					}
					PortData fromPort = e.getFromNode().getPort();
					PortData toPort = e.getToNode().getPort();
					out.write("{ \"type\": \"Feature\", \"properties\": { \"value\": ");
					out.write(String.valueOf(e.getAvgLoad())); 
					out.write(", \"label\": \"polygon" + counter + "\" }, \"geometry\": { \"type\": \"LineString\", \"coordinates\": [ [");
					out.write(String.valueOf(fromPort.getLng()) + ", " + String.valueOf(fromPort.getLat()) + "], [");
					out.write(String.valueOf(toPort.getLng()) + ", " + String.valueOf(toPort.getLat()) + "] ] } }");
					counter++;
				}
			}
			out.newLine();
			out.write("]");
			out.newLine();
			out.write("}");
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void saveODJson(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("{");
			out.newLine();
			out.write("\"type\": \"FeatureCollection\",");
			out.newLine();
			out.write("\"crs\": { \"type\": \"name\", \"properties\": { \"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\" } },");
			out.newLine();
			out.write("\"features\": [");
			out.newLine();
			int counter = 1;
			for(PortData p : Data.getPorts()){
				int totalDemand = graph.getTotalDemand(p);
				if(totalDemand > 0){
					if(counter > 1){
						out.write(",");
						out.newLine();
					}
					out.write("{ \"type\": \"Feature\", \"properties\": { \"value\": ");
					out.write(String.valueOf(totalDemand)); 
					out.write(", \"label\": \"" + p.getUNLocode() + "\" }, \"geometry\": { \"type\": \"Point\", \"coordinates\": [");
					out.write(String.valueOf(p.getLng()) + ", " + String.valueOf(p.getLat()) + "] } }");
					counter++;
				}
			}
			out.newLine();
			out.write("]");
			out.newLine();
			out.write("}");
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
