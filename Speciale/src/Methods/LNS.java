package Methods;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Random;

import Graph.Graph;
import Results.Rotation;

public class LNS {
	Graph graph;
	Graph bestGraph;
//	int timeToRun;
	
	public LNS(){	
	}
	
	public LNS(Graph inputGraph){
		this.graph = inputGraph;
		this.bestGraph = inputGraph;
//		this.timeToRun = timeToRun;
	}
	
	public Graph run(int timeToRunSeconds) throws InterruptedException{
		BufferedWriter progressWriter = graph.getResult().openProgressWriter("ProgressSol.csv");
		
		long timeToRun = (long) timeToRunSeconds * 1000;
		long targetTime = System.currentTimeMillis() + timeToRun;
		
		Random rand = new Random();
		while(System.currentTimeMillis() < targetTime){
			
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
			
			int rotationNumber = rand.nextInt(graph.getResult().getRotations().size());
			Rotation currentRotation = graph.getResult().getRotations().get(rotationNumber);
			if(rand.nextBoolean()){
				currentRotation.insertBestPort(1.1, 0.1);
				currentRotation.createRotationGraph();
				currentRotation.removeWorstPort();
				currentRotation.createRotationGraph();
				
			} else {
				currentRotation.removeWorstPort();
				currentRotation.createRotationGraph();
			}
		}
		return bestGraph;
	}
	
	private void saveSol(BufferedWriter progressWriter, int objective){
		graph.getResult().saveAllEdgesSol("AllEdgesSol.csv");
		graph.getResult().saveODSol("ODSol.csv");
		graph.getResult().saveRotationSol("RotationSol.csv");
		graph.getResult().saveTransferSol("TransferSol.csv");
		graph.getResult().saveProgress(progressWriter, objective);
	}
	
}
