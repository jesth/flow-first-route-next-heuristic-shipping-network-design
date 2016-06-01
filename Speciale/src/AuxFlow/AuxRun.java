package AuxFlow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import Data.Data;
import Data.Demand;
import Graph.Graph;

public class AuxRun {
	private AuxGraph graph;
	private int iterations;
	
	public AuxRun(Graph orgGraph, int iterations){
		graph = new AuxGraph(orgGraph);
		this.iterations = iterations;
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
			graph.runDijkstra(iterations);
			System.out.println("AuxDijkstra iteration " + i + " done");
		}
		graph.serialize();
	}
}
