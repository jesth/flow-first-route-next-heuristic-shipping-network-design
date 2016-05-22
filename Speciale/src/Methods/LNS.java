package Methods;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Data.Data;
import Graph.Graph;
import Results.Rotation;

public class LNS {
	Graph graph;
	int bestObj;
//	Graph bestGraph;
//	int timeToRun;
	
	public LNS(){	
	}
	
	public LNS(Graph inputGraph){
		this.graph = inputGraph;
		bestObj = -Integer.MAX_VALUE;
//		this.bestGraph = inputGraph;
//		this.timeToRun = timeToRun;
	}
	
	public void run(int timeToRunSeconds) throws InterruptedException, IOException{
		BufferedWriter progressWriter = graph.getResult().openProgressWriter("ProgressSol.csv");
		
		long timeToRun = (long) timeToRunSeconds * 1000;
		long startTime = System.currentTimeMillis();
		ArrayList<Rotation> remove = new ArrayList<Rotation>();
		
		int iteration = 3;
		while(System.currentTimeMillis() < startTime + timeToRun){
			double rand = Data.getRandomNumber(iteration);
			ArrayList<Rotation> rotations = findRotationsToNS(rand);
			for(Rotation r : remove){
				r.createRotationGraph();
				r.removeWorstPort();
			}
			remove.clear();
			if(rand < 0.33333){
				for(Rotation r : rotations){
					if(r.insertBestPort(1.1, 0.1)){
						remove.add(r);
					}
//					r.createRotationGraph();
				}
				
			} else if (rand < 0.66666){
				for(Rotation r : rotations){
					r.removeWorstPort();
//					r.createRotationGraph();
				}
			} else {
				for(Rotation r : graph.getResult().getRotations()){
					r.createRotationGraph();
				}
				graph.serviceBiggestOmissionDemand();
			}
			
			graph.runMcf();
			int obj = graph.getResult().getObjective();
			if(bestObj < obj){
				long currentTime = System.currentTimeMillis() - startTime;
				bestObj = obj;
				saveSol(progressWriter, currentTime, obj);
				
			}
			iteration++;
		}
		progressWriter.close();
	}
	
	public ArrayList<Rotation> findRotationsToNS(double rand){
		ArrayList<Rotation> rotationsList = new ArrayList<Rotation>(graph.getResult().getRotations());
		ArrayList<Integer> portIds = new ArrayList<Integer>();
		int noOfRotations = 5;
		ArrayList<Rotation> rotations = new ArrayList<Rotation>(noOfRotations);
		while(!rotationsList.isEmpty() && rotations.size()<5){
			int arraySize = rotationsList.size();
			int pos = (int) (arraySize * rand);
			Rotation rotation = rotationsList.remove(pos);
			if(!rotation.calls(portIds)){
				portIds = rotation.addCallsToList(portIds);
				rotations.add(rotation);
			}
		}
		for(Rotation r : rotations){
			r.createRotationGraph();
		}
		
		return rotations;
	}
	
	private void saveSol(BufferedWriter progressWriter, long currentTime, int objective){
		graph.getResult().saveAllEdgesSol("AllEdgesSol.csv");
		graph.getResult().saveODSol("ODSol.csv");
		graph.getResult().saveRotationSol("RotationSol.csv");
		graph.getResult().saveTransferSol("TransferSol.csv");
		graph.getResult().saveProgress(progressWriter, currentTime, objective);
	}
}
