package Methods;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import AuxFlow.AuxEdge;
import AuxFlow.AuxGraph;
import Data.Data;
import Data.Demand;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Results.Rotation;
import Results.Route;

public class LNS {
	Graph graph;
	int bestObj;
	//	Graph bestGraph;
	//	int timeToRun;


//	public LNS(Graph inputGraph){
//		this.graph = inputGraph;
//		bestObj = graph.getResult().getObjective();
//	}
	
	public LNS(){
		bestObj = -Integer.MAX_VALUE;
	}

	public void run(int timeToRunSeconds) throws InterruptedException, IOException{
		long timeToRun = (long) timeToRunSeconds * 1000;
		long startTime = System.currentTimeMillis();

		graph = findInitialSolution(25);
		graph.runMcf();
		bestObj = graph.getResult().getObjective();
		System.out.println("Rotations generated.");

		BufferedWriter progressWriter = graph.getResult().openProgressWriter("ProgressSol.csv");
		saveSol(progressWriter, 0, bestObj);
		//		if(true){
		//			throw new RuntimeException("STOP");
		//		}
		ArrayList<Rotation> remove = new ArrayList<Rotation>();

		int lastImproveIter = 22;
		int iteration = 22;
		while(System.currentTimeMillis() < startTime + timeToRun){
			boolean madeChange = false;

			double rand = Data.getRandomNumber(iteration);
			//			for(int i=remove.size()-1; i>=0; i--){
			ArrayList<Rotation> newRemove = new ArrayList<Rotation>();
			for(Rotation r : remove){
				//				Rotation r = remove.remove(i);
				if(r.isActive()){
					if(r.removeWorstPort(1)){
						newRemove.add(r);
					}
				}
			}
			remove = newRemove;
			ArrayList<Rotation> rotations = findRotationsToNS(rand);

			if(iteration > lastImproveIter+5){
				System.out.println("Diversification because of lastImproveIter");
				for(int i=0; i<5; i++){
					for(Rotation r : rotations){
						if(r.isActive()){
							if(r.removeWorstPort(0.2)){
								remove.add(r);
							}
							madeChange = true;
						}
					}
					rand = Data.getRandomNumber((iteration + i)* (i+1)*13);
					rotations = findRotationsToNS(rand);
				}
				/*
				Rotation lowestLfRot = null;
				double lowestLf = 1;
				for(Rotation r : graph.getResult().getRotations()){
					if(r.getVesselClass().getCapacity() >= 1200){
						double lf = r.getLoadFactor();
						if(lf < lowestLf){
							lowestLf = lf;
							lowestLfRot = r;
						}
					}
					graph.runMcf();
					rand = Data.getRandomNumber((iteration + i) * (i+1) * 13);
					rotations = findRotationsToNS(rand);
				}
				VesselClass vessel = lowestLfRot.getVesselClass();
				graph.deleteRotation(lowestLfRot);
				
				graph.runMcf();
				int largestOmissionDemand = 0;
				Demand largestOmission = null;
				for(Demand d : graph.getDemands()){
					int omission = 0;
					for(Route r : d.getRoutes()){
						if(r.isOmission()){
							omission += r.getFFE();
						}
					}
					if(omission > largestOmissionDemand){
						largestOmissionDemand = omission;
						largestOmission = d;
					}
				}
				ArrayList<Integer> ports = new ArrayList<Integer>();
				ports.add(largestOmission.getOrigin().getPortId());
				ports.add(largestOmission.getDestination().getPortId());
				graph.createRotationFromPorts(ports, vessel, -1);
				System.out.println("Creating rotation from " + largestOmission.getOrigin().getUNLocode() + "-" + largestOmission.getDestination().getUNLocode());
				*/
				lastImproveIter = iteration+1;
			} else if(rand < 0){
				for(Rotation r : rotations){
					if(r.insertBestPort(1.1, 0.05)){
						remove.add(r);
						madeChange = true;
					}
				}
			} else if (rand < 0.3){
				for(Rotation r : rotations){
					if(r.isActive()){
						if(r.removeWorstPort(1)){
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
//			if(!rotation.calls(portIds)){
//				portIds = rotation.addCallsToList(portIds);
				rotations.add(rotation);
//			}
		}
		//		for(Rotation r : rotations){
		//			r.createRotationGraph();
		//		}
		//		System.out.println("rotations.size() = " + rotations.size());
		return rotations;
	}

	private Graph findInitialSolution(int iterations) throws FileNotFoundException, InterruptedException{
		Data.initialize("fleet_WorldSmall.csv", "randomNumbers.csv");
		ArrayList<AuxEdge> sortedEdges = AuxGraph.getSortedAuxEdges();
		ArrayList<Integer> vesselAndDuration;
		ArrayList<Integer> bestVesselAndDuration = new ArrayList<Integer>();
		int bestObj = -Integer.MAX_VALUE;

		for(int i = 0; i < iterations; i++){
			System.out.println("Activity " + i);
			Graph graph = new Graph("Demand_WorldSmall.csv");
			ComputeRotations cr = new ComputeRotations(graph);
			vesselAndDuration = findSolution(cr, graph, sortedEdges, i);
			graph.runMcf();
			int obj = graph.getResult().getObjective();
			System.out.println("Objective " + obj);
			if(obj > bestObj){
				bestObj = obj;
				bestVesselAndDuration = new ArrayList<Integer>(vesselAndDuration);
			}
			vesselAndDuration.clear();
			for(AuxEdge e : sortedEdges){
				e.setUnusedInRotation();
			}
		}
		Graph graph = new Graph("Demand_WorldSmall.csv");
		ComputeRotations cr = new ComputeRotations(graph);
		implementSol(cr, graph, sortedEdges, bestVesselAndDuration);
		return graph;
	}

	private ArrayList<Integer> findSolution(ComputeRotations cr, Graph graph, ArrayList<AuxEdge> sortedEdges, int iteration){
		ArrayList<Integer> vesselAndDuration = new ArrayList<Integer>();		
		VesselClass feeder450 = Data.getVesselClasses().get(0);
		VesselClass feeder800 = Data.getVesselClasses().get(1);
		VesselClass panamax1200 = Data.getVesselClasses().get(2);
		VesselClass panamax2400 = Data.getVesselClasses().get(3);
		VesselClass postPanamax = Data.getVesselClasses().get(4);
		VesselClass superPanamax = Data.getVesselClasses().get(5);

		VesselClass[] vesselClasses = new VesselClass[]{superPanamax, postPanamax, panamax2400, panamax1200, feeder800, feeder450};
		int[] minLengths = new int[]{10, 7, 6, 6, 4, 4};
		int[] maxLengths = new int[]{10, 14, 12, 10, 8, 8};
		int[] noAvailable = new int[]{superPanamax.getNoAvailable(), postPanamax.getNoAvailable(), panamax2400.getNoAvailable(),
				panamax1200.getNoAvailable(), feeder800.getNoAvailable(), feeder450.getNoAvailable()};

		for(int i = 0; i < 6; i++){
			vesselAndDuration = findRotations(cr, vesselAndDuration, vesselClasses[i], sortedEdges, minLengths[i], maxLengths[i], noAvailable[i], iteration * (i+1));
		}

		return vesselAndDuration;
	}

	private ArrayList<Integer> findRotations(ComputeRotations cr, ArrayList<Integer> vesselAndDuration, VesselClass vesselClass, ArrayList<AuxEdge> sortedEdges, int minLength, int maxLength, int noAvailable, int randIn){
		double rand = Data.getRandomNumber(randIn);
		int nextLength = (int) ((maxLength - minLength) * rand) + minLength;
		while(nextLength <= noAvailable){
			cr.createAuxFlowRotation(nextLength, sortedEdges, vesselClass);
			noAvailable -= nextLength;
			vesselAndDuration.add(vesselClass.getId());
			vesselAndDuration.add(nextLength);
			randIn++;
			rand = Data.getRandomNumber(randIn);
			nextLength = (int) ((maxLength - minLength) * rand) + minLength;
		}
		return vesselAndDuration;
	}

	private void implementSol(ComputeRotations cr, Graph graph, ArrayList<AuxEdge> sortedEdges, ArrayList<Integer> bestVesselAndDuration){
		VesselClass feeder450 = Data.getVesselClasses().get(0);
		VesselClass feeder800 = Data.getVesselClasses().get(1);
		VesselClass panamax1200 = Data.getVesselClasses().get(2);
		VesselClass panamax2400 = Data.getVesselClasses().get(3);
		VesselClass postPanamax = Data.getVesselClasses().get(4);
		VesselClass superPanamax = Data.getVesselClasses().get(5);

		VesselClass[] vesselClasses = new VesselClass[]{superPanamax, postPanamax, panamax2400, panamax1200, feeder800, feeder450};

		for(int i = 0; i < bestVesselAndDuration.size(); i += 2){
			VesselClass vesselClass = null;
			int vesselId = bestVesselAndDuration.get(i);
			for(VesselClass v : vesselClasses){
				if(v.getId() == vesselId){
					vesselClass = v;
				}
			}
			int length = bestVesselAndDuration.get(i+1);
			cr.createAuxFlowRotation(length, sortedEdges, vesselClass);
		}
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
