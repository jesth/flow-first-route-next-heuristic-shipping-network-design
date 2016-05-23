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
		int lastImproveIter = 3;
		while(System.currentTimeMillis() < startTime + timeToRun){
			boolean madeChange = false;

			double rand = Data.getRandomNumber(iteration);
			//			for(int i=remove.size()-1; i>=0; i--){
			ArrayList<Rotation> newRemove = new ArrayList<Rotation>();
			for(Rotation r : remove){
				//				Rotation r = remove.remove(i);
				if(r.removeWorstPort(0.8)){
					if(r.isActive()){
						newRemove.add(r);
					}
				}
			}
			remove = newRemove;
			ArrayList<Rotation> rotations = findRotationsToNS(rand);
			
			if(iteration > lastImproveIter+5){
				System.out.println("Diversification because of lastImproveIter");
				for(Rotation r : rotations){
					if(r.removeWorstPort(0.5)){
						if(r.isActive()){
							remove.add(r);
						}
						madeChange = true;
					}
				}
				lastImproveIter = iteration+1;
			} else if(rand < 0.33333){
				for(Rotation r : rotations){
					if(r.insertBestPort(1.1, 0.1)){
						remove.add(r);
						madeChange = true;
					}
				}
			} else if (rand < 0.66666){
				for(Rotation r : rotations){
					if(r.removeWorstPort(0.8)){
						if(r.isActive()){
							remove.add(r);
						}
						madeChange = true;
					}
				}
			} else {
				for(Rotation r : graph.getResult().getRotations()){
					r.createRotationGraph();
				}
				if(graph.serviceBiggestOmissionDemand()){
					madeChange = true;
				}
			}
			if(madeChange){
				graph.runMcf();	
				int obj = graph.getResult().getObjective();
				if(bestObj < obj){
					long currentTime = System.currentTimeMillis() - startTime;
					bestObj = obj;
					saveSol(progressWriter, currentTime, obj);
					lastImproveIter = iteration+1;
				}
				System.out.println("Iteration objective: " + obj);
				for(Rotation r : graph.getResult().getRotations()){
					r.removeRotationGraph();
				}
				
			}
			iteration++;
		}
		progressWriter.close();
	}

	public ArrayList<Rotation> findRotationsToNS(double rand){
		ArrayList<Rotation> rotationsList = new ArrayList<Rotation>();
		for(Rotation r : graph.getResult().getRotations()){
			if(r.isActive()){
				rotationsList.add(r);
			}
		}
		ArrayList<Integer> portIds = new ArrayList<Integer>();
		int noOfRotations = 5;
		ArrayList<Rotation> rotations = new ArrayList<Rotation>(noOfRotations);
		while(!rotationsList.isEmpty() && rotations.size()<noOfRotations){
			int arraySize = rotationsList.size();
			int pos = (int) (arraySize * rand);
			Rotation rotation = rotationsList.remove(pos);
			if(!rotation.calls(portIds)){
				portIds = rotation.addCallsToList(portIds);
				rotations.add(rotation);
			}
		}
		//		for(Rotation r : rotations){
		//			r.createRotationGraph();
		//		}
		//		System.out.println("rotations.size() = " + rotations.size());
		return rotations;
	}

	private void saveSol(BufferedWriter progressWriter, long currentTime, int objective){
		graph.getResult().saveAllEdgesSol("AllEdgesSol.csv");
		graph.getResult().saveODSol("ODSol.csv");
		graph.getResult().saveRotationSol("RotationSol.csv");
		graph.getResult().saveTransferSol("TransferSol.csv");
		graph.getResult().saveFlowCost("FlowCost.csv");
		graph.getResult().saveRotationCost("RotationCost.csv");
		graph.getResult().saveProgress(progressWriter, currentTime, objective);
	}
}
