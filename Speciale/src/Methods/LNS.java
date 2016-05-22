package Methods;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Random;

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
		
		Random rand = new Random();
		while(System.currentTimeMillis() < targetTime){
			
			ArrayList<Rotation> rotations = findRotationsToNS(rand);
			
			if(rand.nextBoolean()){
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
			
		}
	}
	
	public ArrayList<Rotation> findRotationsToNS(Random rand){
		

		ArrayList<Integer> rotationIdList = new ArrayList<Integer>(graph.getResult().getRotations().size());
		for(Rotation r : graph.getResult().getRotations()){
			rotationIdList.add(r.getId());
		}
		int noOfRotations = rand.nextInt(5)+1;
		ArrayList<Rotation> rotations = new ArrayList<Rotation>(noOfRotations);
		for(int i=0; i<noOfRotations; i++){
			int rotationId = rotationIdList.remove(rand.nextInt(rotationIdList.size()));
			rotations.add(graph.getResult().getRotations().get(rotationId));
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
