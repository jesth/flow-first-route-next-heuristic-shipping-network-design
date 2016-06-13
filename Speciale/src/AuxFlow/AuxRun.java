package AuxFlow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import Data.Data;
import Data.Demand;
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

	public void run(){
		for(int i = 0; i < iterations; i++){
//			System.out.println("Running iteration " + i);
			graph.runDijkstra(iterations, rand);
			System.out.println("AuxDijkstra iteration " + i + " done");
		}
		for(AuxEdge ae : graph.getSortedAuxEdges()){
			System.out.print(ae.getFromNode().getUNLocode()+"-"+ae.getToNode().getUNLocode() + " with load " + ae.getAvgLoad());
			if(ae.isRotation()){
				System.out.println(" is rotation.");
			} else {
				System.out.println(" is not rotation.");
			}
		}
		graph.serialize();
	}
}
