package Methods;

import java.io.BufferedWriter;
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
		bestObj = graph.getResult().getObjective();
//		this.bestGraph = inputGraph;
//		this.timeToRun = timeToRun;
	}
	
	public void run(int timeToRunSeconds) throws InterruptedException{
		BufferedWriter progressWriter = graph.getResult().openProgressWriter("ProgressSol.csv");
		
		long timeToRun = (long) timeToRunSeconds * 1000;
		long targetTime = System.currentTimeMillis() + timeToRun;
		
		int iteration = 0;
		while(System.currentTimeMillis() < targetTime){
			double rand = Data.getRandomNumber(iteration);
			ArrayList<Rotation> rotations = findRotationsToNS(rand);
			
			if(rand < 0.5){
				for(Rotation r : rotations){
					r.insertBestPort(1.1, 0.1);
					r.createRotationGraph();
				}
				
			} else {
				for(Rotation r : rotations){
					r.removeWorstPort();
					r.createRotationGraph();
				}
			}
			
			graph.runMcf();
			int obj = graph.getResult().getObjective();
			if(bestObj < obj){
				bestObj = obj;
				saveSol(progressWriter, obj);
				
			}
			iteration++;
		}
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
	
	private void saveSol(BufferedWriter progressWriter, int objective){
		graph.getResult().saveAllEdgesSol("AllEdgesSol.csv");
		graph.getResult().saveODSol("ODSol.csv");
		graph.getResult().saveRotationSol("RotationSol.csv");
		graph.getResult().saveTransferSol("TransferSol.csv");
		graph.getResult().saveProgress(progressWriter, objective);
	}
}
