package Methods;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import AuxFlow.AuxEdge;
import AuxFlow.AuxGraph;
import AuxFlow.AuxRun;
import Data.Data;
import Data.Demand;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Results.Rotation;
import Results.Route;

public class LNS {
	Graph graph;
	//	Graph bestGraph;
	//	int timeToRun;


	//	public LNS(Graph inputGraph){
	//		this.graph = inputGraph;
	//		bestObj = graph.getResult().getObjective();
	//	}

	public LNS(){
		//		bestObj = -Integer.MAX_VALUE;
	}

	//	public void run(int timeToRunSeconds, int numIterToFindInit, AuxGraph auxGraph) throws InterruptedException, IOException{
	public void run(int timeToRunSeconds, int numIterToFindInit, String fleetFileName, String demandFileName) throws InterruptedException, IOException{
		Data.initialize(fleetFileName, "randomNumbers.csv");
		long timeToRun = (long) timeToRunSeconds * 1000;
		long startTime = System.currentTimeMillis();

		ArrayList<Rotation> rotationsToKeep = new ArrayList<Rotation>();
		graph = findInitialSolution(numIterToFindInit, rotationsToKeep, demandFileName);
		startTime = System.currentTimeMillis();
		graph.runMcf();
		int allTimeBestObj = graph.getResult().getObjective();
		int currBestObj = allTimeBestObj;
		Graph bestGraph = null;
//		System.out.println("Rotations generated.");

		BufferedWriter progressWriter = graph.getResult().openProgressWriter("ProgressSol.csv");
		saveSol(progressWriter, 0, allTimeBestObj);
		//		if(true){
		//			throw new RuntimeException("STOP");
		//		}
		ArrayList<Rotation> remove = new ArrayList<Rotation>();
		ArrayList<Rotation> insert = new ArrayList<Rotation>();

		int allTimeLastImproveIter = 550;
		int lastImproveIter = allTimeLastImproveIter;
		int lastDiversification = lastImproveIter;
		int iteration = lastImproveIter;
		while(System.currentTimeMillis() < startTime + timeToRun){
			boolean madeChange = false;

			double rand = Data.getRandomNumber(iteration);
			madeChange = removeAndInsert(remove, insert);
			ArrayList<Rotation> rotations = findRotationsToNS(rand);

			if((iteration % 3) == 0){
				for(int i=graph.getResult().getRotations().size()-1; i>=0; i--){
					Rotation r = graph.getResult().getRotations().get(i); 
					if(r.removeUnservingCalls(0.05)){
						madeChange = true;
					}
				}
//				if(madeChange){
//					//					System.out.println("Objective after removing unserving calls = " + graph.getResult().getObjective());
//					madeChange = false;
//				}
			}
			for(Rotation r : graph.getResult().getRotations()){
				r.removeRotationGraph();
			}
			if(iteration > lastDiversification + 5 && iteration > lastImproveIter + 5){
				diversify(insert, remove, iteration);
				madeChange = true;
				lastDiversification = iteration+1;
//<<<<<<< HEAD
//			} else if(iteration > lastImproveIter + 10) {
//				graph = bestGraph;
////				restart(AuxGraph.deserialize(), demandFileName);
////				currBestObj = -Integer.MAX_VALUE;
//				lastImproveIter = iteration+1;
//				lastDiversification = iteration+1;
//=======
////			} else if(iteration > lastImproveIter + 10) {
////				graph = bestGraph;
////				restart(AuxGraph.deserialize(), demandFileName);
////				currBestObj = -Integer.MAX_VALUE;
////				lastImproveIter = iteration+1;
////				lastDiversification = iteration+1;
//>>>>>>> branch 'master' of https://github.com/jesth/speciale.git
			} else if(rand < 0.2){
//				System.out.println("    serviceOmissionDemand() chosen");
				for(Rotation r : graph.getResult().getRotations())
					r.createRotationGraph(false);
				if(graph.serviceBiggestOmissionDemand(iteration)){
					madeChange = true;
				}
			} else if(rand < 0.3){
				if(graph.createFeederRotation()){
					madeChange = true;
				}
			} else if(rand < 0.6){
//				System.out.println("    insertBestPortEdge() chosen");
				for(Rotation r : rotations){
					r.createRotationGraph(true);
					r.includeOmissionDemands();
					if(r.getVesselClass().getCapacity() <= 800 && r.isActive() && r.insertBestPort(1.05, 0.05, false)){
						remove.add(r);
						madeChange = true;
					}
					else if(r.isActive() && r.insertBestPortEdge(1.05, 0.05, false)){
						remove.add(r);
						madeChange = true;
					}
				}
				//			} else if(rand < 0.6){
				//				System.out.println("Trying to reduce transfer load.");
				//				Rotation r = findBiggestTransferRotation();
				//				r.includeOmissionDemands();
				//				if(r.isActive() && r.insertBestPortEdge(1.05, 0.05, false)){
				//					remove.add(r);
				//					madeChange = true;
				//				}
			} else {
//				System.out.println("    removeWorstPort() chosen");
				for(Rotation r : rotations){
					//					if(r.isActive() && r.getLoadFactor() < 0.7 && r.removeWorstPort(1, false)){
					if(r.isActive() && r.removeWorstPort(1, false)){
						madeChange = true;
					}
				}
			}
			graph.runMcf();
			if(lastDiversification == iteration+1){
				currBestObj = graph.getResult().getObjective();
			}
			if(madeChange){
				int obj = graph.getResult().getObjective();
				if(currBestObj < obj){
					currBestObj = obj;
					lastImproveIter = iteration + 1;
				}
				if(allTimeBestObj < obj){
					long currentTime = System.currentTimeMillis() - startTime;
					allTimeBestObj = obj;
					bestGraph = new Graph(graph);
					saveSol(progressWriter, currentTime, obj);
					allTimeLastImproveIter = iteration+1;
					System.out.println("New best solution: " + allTimeBestObj);
				}
				System.out.println("#"+ iteration +" Iteration objective: " + obj);
			}
			iteration++;
		}
		progressWriter.close();
//		graph.serialize();
	}

	private void diversify(ArrayList<Rotation> insert, ArrayList<Rotation> remove, int iteration) throws InterruptedException{
		System.out.println("Diversification because of lastImproveIter");
		for(int i=0; i<7; i++){
			double rand = Data.getRandomNumber((iteration + i)* (i+1)*13);
			ArrayList<Rotation> rotations = findRotationsToNS(rand);
			for(Rotation r : rotations){
				if(r.isActive()){
					if(r.removeWorstPort(0.5, false)){
						remove.add(r);
					}
				}
			}
		}

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
		}
		if(lowestLf < 1){
			graph.deleteRotation(lowestLfRot);
			System.out.println("Rotation " + lowestLfRot.getId() + " deleted.");
			graph.runMcf();
			ArrayList<Demand> noGoes = new ArrayList<Demand>();
			Rotation newR = createNewRotation(noGoes);
			insert.add(newR);
		}
	}

	public Rotation createNewRotation(ArrayList<Demand> noGoes){
		Demand demand = graph.findHighestCostDemand(noGoes);
		ArrayList<Integer> portsId = new ArrayList<Integer>();
		ArrayList<Port> ports = new ArrayList<Port>();
		ports.add(demand.getOrigin());
		ports.add(demand.getDestination());
		portsId.add(demand.getOrigin().getPortId());
		portsId.add(demand.getDestination().getPortId());
		Rotation newR = null;
		for(int i = Data.getVesselClasses().size()-1; i>=2; i--){
			VesselClass v = Data.getVesselClasses().get(i);
			int reqVessels = ComputeRotations.calcNumberOfVessels(ports, v);
			int spareVessels = graph.getNetNoVesselsAvailable(v.getId());
			if(reqVessels < spareVessels){
				if(v.getDraft() <= demand.getOrigin().getDraft() && v.getDraft() <= demand.getDestination().getDraft()){
					newR = graph.createRotationFromPorts(portsId, v);
					System.out.println("Creating rotation from " + demand.getOrigin().getUNLocode() + "-" + demand.getDestination().getUNLocode());
					break;
				}
			}
		}
		if(newR == null){
			noGoes.add(demand);
			newR = createNewRotation(noGoes);
		}
		return newR;
	}

	public ArrayList<Rotation> findRotationsToNS(double rand){
		ArrayList<Rotation> rotationsList = new ArrayList<Rotation>();
		for(Rotation r : graph.getResult().getRotations()){
			if(r.isActive()){
				rotationsList.add(r);
			}
		}
		ArrayList<Integer> portIds = new ArrayList<Integer>();
		int noOfRotations = 6;
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

	private Graph findInitialSolution(int iterations, ArrayList<Rotation> rotationsToKeep, String demandFileName) throws FileNotFoundException, InterruptedException{
		AuxGraph auxGraph = AuxGraph.deserialize();
		ArrayList<AuxEdge> sortedEdges = auxGraph.getSortedAuxEdges();
		ArrayList<AuxEdge> usedEdges = new ArrayList<AuxEdge>();
		for(AuxEdge ae : sortedEdges){
			if(ae.isUsedInRotation()){
				usedEdges.add(ae);
			}
		}
		int bestObj = -Integer.MAX_VALUE;
		Graph bestGraph = null;

		for(int i = 0; i < iterations; i++){
//			System.out.println("Activity " + i);
			Graph graph = new Graph(demandFileName);
			ComputeRotations cr = new ComputeRotations(graph);
			findSolution(cr, graph, sortedEdges, rotationsToKeep, i+iterations);
			graph.runMcf();
			int obj = graph.getResult().getObjective();
//			System.out.println("Objective " + obj);
			System.out.println(obj);
			if(obj > bestObj){
				bestObj = obj;
				bestGraph = graph;
			}
			for(AuxEdge ae : sortedEdges){
				ae.setUnusedInRotation();
			}
			for(AuxEdge ae : usedEdges){
				ae.setUsedInRotation();
			}
		}
		return bestGraph;
	}

	private Graph findInitialSolution2(int iterations, ArrayList<Rotation> rotationsToKeep, String demandFileName) throws FileNotFoundException, InterruptedException{
		Graph bestGraph = null;
		int bestObj = -Integer.MAX_VALUE;
		for(int i = 0; i < 3; i++){
			graph = new Graph(demandFileName);
			System.out.println("Running outer loop at iteration " + i);
			AuxRun auxRun = new AuxRun(graph, 3, i);
			auxRun.run();
			AuxGraph auxGraph = AuxGraph.deserialize();
			ArrayList<AuxEdge> sortedEdges = auxGraph.getSortedAuxEdges();
			//			for(AuxEdge e : sortedEdges){
			//				System.out.println("AuxEdge " + e.getFromNode().getUNLocode() + "-" + e.getToNode().getUNLocode() + " with load " + e.getAvgLoad());
			//			}
			ArrayList<AuxEdge> usedEdges = new ArrayList<AuxEdge>();
			for(AuxEdge ae : sortedEdges){
				if(ae.isUsedInRotation()){
					usedEdges.add(ae);
				}
			}
			for(int j = 0; j < iterations; j++){
				System.out.println("Activity " + j);
				Graph graph = new Graph(demandFileName);
				ComputeRotations cr = new ComputeRotations(graph);
				findSolution(cr, graph, sortedEdges, rotationsToKeep, j+iterations);
				graph.runMcf();
				int obj = graph.getResult().getObjective();
				System.out.println("Objective " + obj);
				if(obj > bestObj){
					bestObj = obj;
					bestGraph = graph;
				}
				for(AuxEdge ae : sortedEdges){
					ae.setUnusedInRotation();
				}
				for(AuxEdge ae : usedEdges){
					ae.setUsedInRotation();
				}
			}
		}
		return bestGraph;
	}

	private ArrayList<Integer> findSolution(ComputeRotations cr, Graph graph, ArrayList<AuxEdge> sortedEdges, ArrayList<Rotation> rotationsToKeep, int iteration){
		graph.createRotations(rotationsToKeep);

		ArrayList<Integer> vesselAndDuration = new ArrayList<Integer>();		
		VesselClass feeder450 = Data.getVesselClasses().get(0);
		VesselClass feeder800 = Data.getVesselClasses().get(1);
		VesselClass panamax1200 = Data.getVesselClasses().get(2);
		VesselClass panamax2400 = Data.getVesselClasses().get(3);
		VesselClass postPanamax = Data.getVesselClasses().get(4);
		VesselClass superPanamax = Data.getVesselClasses().get(5);

		VesselClass[] vesselClasses = new VesselClass[]{superPanamax, postPanamax, panamax2400, panamax1200, feeder800, feeder450};
		int[] minLengths = new int[]{10, 7, 6, 6, 2, 2};
		int[] maxLengths = new int[]{10, 14, 12, 10, 8, 5};
		int[] noAvailable = new int[]{graph.getNetNoVesselsAvailable(superPanamax.getId()), graph.getNetNoVesselsAvailable(postPanamax.getId()), graph.getNetNoVesselsAvailable(panamax2400.getId()),
				graph.getNetNoVesselsAvailable(panamax1200.getId()), graph.getNetNoVesselsAvailable(feeder800.getId()), graph.getNetNoVesselsAvailable(feeder450.getId())};

		for(int i = 0; i < 6; i++){
			vesselAndDuration = findRotations(cr, vesselAndDuration, vesselClasses[i], sortedEdges, minLengths[i], maxLengths[i], noAvailable[i], iteration * (i+1));
		}

		return vesselAndDuration;
	}

	private void restart(AuxGraph auxGraph, String demandFileName) throws FileNotFoundException, InterruptedException{
		System.out.println("RESTARTING!");
		graph.runMcf();
		ArrayList<Rotation> rotationsToKeep = graph.findRotationsToKeep();
		System.out.println("Keeping " + rotationsToKeep.size() + " rotations.");
		//		auxGraph.setEdgesUsed(rotationsToKeep);
		AuxRun auxRun = new AuxRun(graph, rotationsToKeep, 5, 43);
		auxRun.run();
		graph = findInitialSolution(20, rotationsToKeep, demandFileName);
	}

	private boolean removeAndInsert(ArrayList<Rotation> remove, ArrayList<Rotation> insert) throws InterruptedException{
		boolean madeChange = false;
		ArrayList<Rotation> newRemove = new ArrayList<Rotation>();
		for(Rotation r : remove){
			//				Rotation r = remove.remove(i);
			if(r.isActive() && r.removeWorstPort(1, false)){
				madeChange = true;
				newRemove.add(r);
			}
		}
		remove = newRemove;

		ArrayList<Rotation> newInsert = new ArrayList<Rotation>();
		for(Rotation r : insert){
//			System.out.println("        insert in removeAndInsert()");
			if(r.isActive() && r.insertBestPortEdge(1.05, 0.05, false)){
				newInsert.add(r);
				madeChange = true;
			}
		}
		insert = newInsert;
		return madeChange;
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

	/*
	private Rotation findBiggestTransferRotation(){
		Edge biggestTransshipmentEdge = null;
		int biggestTransshipment = -1;
		for(Edge e : graph.getEdges().values()){
			if(e.isTransshipment()){
				int load = e.getLoad();
				if(load > biggestTransshipment){
					biggestTransshipment = load;
					biggestTransshipmentEdge = e;
				}
			}
		}
		int kmFrom = 0;
		Rotation from = biggestTransshipmentEdge.getFromNode().getRotation();
		int kmTo = 0;
		Rotation to = biggestTransshipmentEdge.getToNode().getRotation();
		for(Route r : biggestTransshipmentEdge.getRoutes()){
			for(Edge e : r.getRoute()){
				if(e.isSail() && e.getRotation().equals(from)){
					kmTo += e.getDistance().getDistance();
				} else if(e.isSail() && e.getRotation().equals(to)){
					kmFrom += e.getDistance().getDistance();
				}
			}
		}
		if(kmFrom < kmTo){
			return from;
		}
		return to;
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
	 */

	private void saveSol(BufferedWriter progressWriter, long currentTime, int objective){
		graph.getResult().saveAllEdgesSol("AllEdgesSol.csv");
		graph.getResult().saveODSol("ODSol.csv");
		graph.getResult().saveRotationSol("RotationSol.csv");
		graph.getResult().saveTransferSol("TransferSol.csv");
		graph.getResult().saveFlowCost("FlowCost.csv");
		graph.getResult().saveRotationCost("RotationCost.csv");
		graph.getResult().saveOPLData("OPLData.dat");
		graph.getResult().saveProgress(progressWriter, currentTime, objective);
	}
}
