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
	private int rand;
	
	public AuxRun(Graph orgGraph, int iterations, int rand){
		graph = new AuxGraph(orgGraph);
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
		graph.serialize();
	}
}
