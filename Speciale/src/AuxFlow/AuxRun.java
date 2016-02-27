package AuxFlow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import Data.Data;

public class AuxRun {
	private static AuxGraph graph;
	private static int iterations;

	public static void initialize(Data data, int iterationsIn){
		graph = new AuxGraph(data);
		iterations = iterationsIn;
//		graph.addFirstRotationManual();
//		graph.addSecondRotationManual();
//		graph.addThirdRotationManual();
//		graph.addFourthRotationManual();
//		graph.addFifthRotationManual();
		run();
		graph.serialize();
	}

	public static void run(){
		for(int i = 0; i < iterations; i++){
//			System.out.println("Running iteration " + i);
			AuxDijkstra.run();
			AuxDijkstra.convert(iterations);
		}
//		for(AuxEdge e : graph.getEdges()){
//			if(e.getSumLoad() > 1 || e.isRotation()){
//			if(e.getFromNode().getPort().getUNLocode().equals("EGPSD")){
//				System.out.println("Edge from " + e.getFromNode().getPort().getUNLocode() + " to " + e.getToNode().getPort().getUNLocode() + " has expected load " + e.getAvgLoad() + " and cost " + e.getCost());
//			}
//		}
	}
}
