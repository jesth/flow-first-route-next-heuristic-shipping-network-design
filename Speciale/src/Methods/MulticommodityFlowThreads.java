package Methods;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Data.Data;
import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Results.Result;
import Results.Rotation;
import Results.Route;

public class MulticommodityFlowThreads {
	private Graph graph;
	private int bestFlowProfit;
	private ArrayList<Route> bestRoutes;
	private ArrayList<BellmanFord> bellmanFords;


	/** Initializes the multicommodity flow by saving the graph and initializing Bellman Ford.
	 * @param inputGraph - the graph to be searched through to find the multicommodity flow.
	 */
	//	public static void initialize(Graph inputGraph){
	//		graph = inputGraph;
	//		bellmanFords = new ArrayList<BellmanFord>(Data.getPortsMap().size());
	//		BellmanFord bellmanFord = null;
	//		System.out.println("Number of acvtive ports: " + graph.getFromCentroids().size());
	//		for(Node n : graph.getFromCentroids()){
	//			bellmanFord = new BellmanFord(inputGraph, n);
	//			bellmanFords.add(bellmanFord);
	//		}
	//	}

	public MulticommodityFlowThreads(Graph inputGraph){
		graph = inputGraph;
		bellmanFords = new ArrayList<BellmanFord>(Data.getPortsMap().size());
		BellmanFord bellmanFord = null;
		for(Node n : graph.getFromCentroids()){
			bellmanFord = new BellmanFord(inputGraph, n);
			bellmanFords.add(bellmanFord);
		}
	}

	public void reset(){
		for(Edge e : graph.getEdges()){
			if(e.isSail()){
				e.setLagrangeStep(50);
				e.setLagrange(1);
			}
		}
	}

	/** Runs the multicommodity flow algorithm by:
	 * <br>1) Running the Bellman Ford-algorithm.
	 * <br>2) If the flow is illegal: 
	 * <br>&nbsp&nbsp&nbsp a) Lagrange values are computed.
	 * <br>&nbsp&nbsp&nbsp b) The illegal edges are relaxed by BellmanFord.relaxEdge(e).
	 * <br>&nbsp&nbsp&nbsp c) The flow is repaired by findRepairFlow().
	 * <br>&nbsp&nbsp&nbsp d) The process is repeated from step 1).
	 * <br>3) If the flow is legal, the best found flow is implemented.
	 * @throws InterruptedException 
	 */
	public void run() throws InterruptedException{

		ArrayList<Edge> sailEdges = new ArrayList<Edge>();
		for(Edge e : graph.getEdges()){
			if(e.isSail()){
				sailEdges.add(e);
			}
		}
		reset();
		bestFlowProfit = Integer.MIN_VALUE;
		bestRoutes = new ArrayList<Route>();
		int iteration = 0;
		int repairCounter = 0;
		int improvementCounter = 0;
		//				startLagrange();
		//TODO hardcoded 100 iterations...
		long startTime = System.currentTimeMillis();

		while (improvementCounter < 20 && iteration < 100){
			//			System.out.println("Now running BellmanFord in iteration " + iteration);
			//			System.out.println();
			for(Edge e : graph.getEdges()){
				e.clearRoutes();
			}
			if(!graph.isSubGraph()){
				runBF(true, false);
			} else {
				runBF(false, false);
			}
			boolean validFlow = false;
			double overflow = getOverflow();
			if(overflow < 0.3){
				improvementCounter++;
				//				System.out.println("Finding repair flow.");
				validFlow = findRepairFlow(improvementCounter, iteration);
				repairCounter++;

			}
			int flowProfit = graph.getResult().getFlowProfit(false);
			if(validFlow && flowProfit > bestFlowProfit){
				System.out.println("Found better flow without repair: " + flowProfit + " > " + bestFlowProfit);
				updateBestFlow(flowProfit);
				repairCounter = 0;
				improvementCounter = 0;
			}

			//			for (Edge e : graph.getEdges()){
			//				if(e.isSail()){
			for(Edge e : sailEdges){
				if(repairCounter >= 5){
					e.setLagrange(Math.max(e.getLagrange() / 2,1));
					e.saveValues(iteration);
				} else {
					e.lagrangeAdjustment(iteration);
				}
				//				}
			}
			//			for(Demand d : graph.getDemands()){
			//				for(Route r : d.getRoutes()){
			//					r.updateLagrangeProfit();
			//				}
			//			}
			if(repairCounter >= 5){
				//				System.out.println("Halving Lagranges.");
				repairCounter = 0;
			}
			iteration++;
		}
		implementBestFlow();
		long endTime = System.currentTimeMillis();
		saveLagranges("lagranges.csv", iteration);
		saveLoads("loads.csv", iteration);
		if(graph.isSubGraph()){
			saveODSol("ODSolRotation.csv", graph.getDemands());
			saveRotationSol("RotationSolRotation.csv", graph.getResult().getRotations());
			//			saveAllEdgesSol("AllEdgesSolRotation.csv");
		} else {
			saveODSol("ODSol.csv", graph.getDemands());
			saveRotationSol("RotationSol.csv", graph.getResult().getRotations());
			//			saveAllEdgesSol("AllEdgesSol.csv");
		}
			System.out.println("RunningTime " + (endTime-startTime));
		if(!graph.isSubGraph()){
			System.out.println("Exiting while loop after iteration " + iteration);
		}
	}

	private void runBF(boolean threads, boolean rep){
		threads = true;
		if(threads){
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for(BellmanFord b : bellmanFords){
				if(rep){
					b.setRep();
				} else {
					b.setMain();
				}
				executor.execute(b);
			}
			executor.shutdown();
			while(!executor.isTerminated()){
			}
		} else {
			for(BellmanFord b : bellmanFords){
				if(rep){
					b.setRep();
				} else {
					b.setMain();
				}
				b.run();
			}
		}
	}

	private double getOverflow() {
		double overflow = 0;
		int sailEdges = 0;
		for(Edge e : graph.getEdges()){
			if(e.isSail() && e.isActive()){
				sailEdges++;
				overflow += Math.max(((double) e.getLoad()/ (double) e.getCapacity())-1,0);
			}
		}
		overflow = overflow/(double) sailEdges;

		return overflow;
	}

	private boolean findRepairFlow(int improvementCounter, int iteration) throws InterruptedException{
		while(iteration >= 100){
			iteration -= 100;
		}
		boolean validFlow = true;
		ArrayList<Route> overflowRoutes = new ArrayList<Route>();
		//		for(Edge e : graph.getEdges()){
		if(!graph.isSubGraph()){
			runBF(true, true);
		} else {
			runBF(false, true);
		}
		int length = graph.getEdges().size();
		int counter = 0;
		ArrayList<Edge> edges = new ArrayList<Edge>(graph.getEdges());
		while(length > 0){
			if(counter >= 1000){
				counter -= 1000;
			}
			int i = (int) (length * Data.getRandomNumber(iteration, counter));
			Edge e = edges.remove(i);
//					for(int i = graph.getEdges().size()-1; i>= 0; i--){
//						Edge e = graph.getEdges().get(i);
			length--;
			counter++;
			if(e.isSail()){
				int overflow = e.getRepAndRemoveLoad() - e.getCapacity();
				if(overflow > 0){
					validFlow = false;
					findOverflowRoutes(e, overflow, overflowRoutes);
				}
			}
		}

		for(Route r : overflowRoutes){
			//			System.out.println("Route from " + r.getDemand().getOrigin().getUNLocode() + " to " + r.getDemand().getDestination().getUNLocode() + " with FFEforRemoval " + r.getFFEforRemoval());
			Demand d = r.getDemand();
			d.createRepRoute(r, r.getFFEforRemoval());
		}
		for(Demand d : graph.getDemands()){
			d.rerouteOmissionFFEs();
		}
		if(!validFlow){
			findRepairFlow(improvementCounter, iteration+1);
		}
		int flowProfit = graph.getResult().getFlowProfit(true);
		if(flowProfit > bestFlowProfit){
			improvementCounter = 0;
			if(!graph.isSubGraph()){
				System.out.println("Found better flow: " + flowProfit + " > " + bestFlowProfit);
			}
			updateBestFlow(flowProfit);
			for(Edge e : graph.getEdges()){
				if(e.isSail()){
					e.decreaseLagrangeStep();
				}
			}
		}
		return validFlow;
	}

	private void findOverflowRoutes(Edge e, int overflow, ArrayList<Route> overflowRoutes){
		Route r = null;
		if(graph.isSubGraph()){
			r = e.findLeastProfitableRoute2();
		} else {
			r = e.findLeastProfitableRoute();
		}
		int FFEforRemoval = Math.min(overflow, (r.getFFErep()-r.getFFEforRemoval()));
		r.addFFEforRemoval(FFEforRemoval);
		//		System.err.println("Route from " + r.getDemand().getOrigin().getUNLocode() + " to " + r.getDemand().getDestination().getUNLocode() + " with FFEforRemoval " + r.getFFEforRemoval());
		overflowRoutes.add(r);
		overflow -= FFEforRemoval;
		if(overflow > 0){
			findOverflowRoutes(e, overflow, overflowRoutes);
		}
	}

	/** Updates the best flow to the current flow. Saves the best routes and objective value of the current flow. 
	 * @param bestFlowProfitIn - the profit of the current flow that is to be saved as the best flow.
	 */
	private void updateBestFlow(int bestFlowProfitIn){
		bestRoutes.clear();
		for(Demand d : graph.getDemands()){
			for(Route r : d.getRoutes()){
				bestRoutes.add(r);
			}
		}
		bestFlowProfit = bestFlowProfitIn;
	}

	/** Implements the saved best flow by:  
	 * <br>1) Clearing all routes from all demand and edge elements.
	 * <br>2) Adding the best routes to all demand and edge elements.
	 */
	public void implementBestFlow(){
		for(Demand d : graph.getDemands()){
			d.clearRoutes();
		}
		for(Edge e : graph.getEdges()){
			e.clearRoutes();
		}
		for(Route r : bestRoutes){
			if(r.getFFErep()!=0){
				r.setFFE(r.getFFErep());
				r.getDemand().addRoute(r);
				for(Edge e : r.getRoute()){
					e.addRoute(r);
				}
			}
		}
		for(Edge e : graph.getEdges()){
			if(e.isSail()){
				//				System.out.println(e.simplePrint() + " with load " + e.getLoad());
			}
		}


		for(Edge e : graph.getEdges()){
			//			System.out.println(e.simplePrint() + " with load " + e.getLoad());
			if(e.isSail()){
				for(Route r : e.getRoutes()){
					//					System.out.println("origin: " +r.getDemand().getOrigin().getUNLocode()+" destination: "+r.getDemand().getDestination().getUNLocode()+ " load on route " + r.getFFE());
				}
			}
			if(e.getCapacity() < e.getLoad()){
				throw new RuntimeException("Capacity limit not respected on edge from " + e.getFromPortUNLo() + " to " + e.getToPortUNLo() + " with load: " + e.getLoad() + " and capacity: " + e.getCapacity());
			}
		}
		for(Demand d : graph.getDemands()){
			d.checkDemand();
		}

	}

	/** Saves the routes of all demand pairs in csv-format for easy handling in Excel.
	 * @param fileName - the complete path or name of the file to be saved. End on .csv.
	 * @param demands - the list of demands to be saved.
	 */
	public void saveODSol(String fileName, ArrayList<Demand> demands){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Omission;Feeder"); 
			out.newLine();
			for(Demand d : demands){
				for(Route r : d.getRoutes()){
					for(Edge e : r.getRoute()){
						if(e.isSail() || e.isOmission() || e.isFeeder()){
							if(e.isOmission() || e.isFeeder()){
								out.write(";;");
							} else {
								out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
							}
							out.write(d.getId()+";");
							out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
							out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
							out.write(r.getFFE()+";");
							if(e.isOmission()){
								out.write("1;0");
							} else if(e.isFeeder()){
								out.write("0;1");
							} else {
								out.write("0;0");
							}
							out.newLine();
						}
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveRotationSol(String fileName, ArrayList<Rotation> rotations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;LegFrom;LegTo;#FFE;LoadingFrom;UnloadingTo"); 
			out.newLine();
			for(Rotation r : rotations){
				for(Edge e : r.getRotationEdges()){
					if(e.isSail() && e.isActive()){
						out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
						out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
						out.write(e.getLoad()+";");
						int loading = 0;
						for(Edge l : e.getFromNode().getIngoingEdges()){
							if(!l.isDwell()){
								loading += l.getLoad();
							}
						}
						int unloading = 0;
						for(Edge l : e.getToNode().getOutgoingEdges()){
							if(!l.isDwell()){
								unloading += l.getLoad();
							}
						}
						out.write(loading+";"+unloading);
						out.newLine();
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveTransferSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("Port;RotationFrom;RotationTo;ODId;ODFrom;ODTo;#FFE");
			out.newLine();
			for(Edge e : graph.getEdges()){
				if(e.isTransshipment()){
					for(Route r : e.getRoutes()){
						Demand d = r.getDemand();
						out.write(e.getFromPortUNLo() + ";");
						out.write(e.getPrevEdge().getRotation().getId() + ";" + e.getNextEdge().getRotation().getId() + ";");
						out.write(d.getId() + ";" + d.getOrigin().getUNLocode() + ";" + d.getDestination().getUNLocode() + ";");
						out.write(r.getFFE());
						out.newLine();
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAllEdgesSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("PortFrom;PortTo;Cost;Capacity;Load;Omission;Load;Unload;Transshipment;Sail/Dwell;Feeder;RotationIdFrom;RotationIdTo;NoInRotationFrom;NoInRotationTo");
			out.newLine();
			for(Edge e : graph.getEdges()){
				if(e.isActive()){
					out.write(e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";");
					out.write(e.getCost() + ";" + e.getCapacity() + ";" + e.getLoad() + ";");
					if(e.isOmission()){
						out.write("1;0;0;0;0;0;;;;");
					} else if(e.isLoadUnload()){
						if(e.getFromNode().isFromCentroid()){
							out.write("0;1;0;0;0;0;;" + e.getToNode().getNextEdge().getRotation().getId() + ";;" + e.getToNode().getNextEdge().getNoInRotation());
						} else {
							out.write("0;0;1;0;0;0;" + e.getFromNode().getPrevEdge().getRotation().getId() + ";;" + e.getFromNode().getPrevEdge().getNoInRotation() + ";");
						}
					} else if(e.isTransshipment()){
						out.write("0;0;0;1;0;0;" + e.getFromNode().getPrevEdge().getRotation().getId() + ";" + e.getToNode().getNextEdge().getRotation().getId() + 
								";" + e.getFromNode().getPrevEdge().getNoInRotation() + ";" + e.getToNode().getNextEdge().getNoInRotation());
					} else if(e.isSail() || e.isDwell()){
						out.write("0;0;0;0;1;0;" + e.getRotation().getId() + ";" + e.getRotation().getId() + ";" + e.getNoInRotation() + ";" + e.getNoInRotation());
					} else if(e.isFeeder()) {
						if(e.getFromNode().isArrival() && e.getToNode().isDeparture()){
							out.write("0;0;0;0;0;1;" + + e.getFromNode().getPrevEdge().getRotation().getId() + ";" + e.getToNode().getNextEdge().getRotation().getId() +
									";" + e.getFromNode().getPrevEdge().getNoInRotation() + ";" + e.getToNode().getNextEdge().getNoInRotation());
						} else if(e.getFromNode().isArrival()){
							out.write("0;0;0;0;0;1;" + + e.getFromNode().getPrevEdge().getRotation().getId() + ";;" + e.getFromNode().getPrevEdge().getNoInRotation() + ";");
						} else{
							out.write("0;0;0;0;0;1;;" + e.getToNode().getNextEdge().getRotation().getId() + ";;" + e.getToNode().getNextEdge().getNoInRotation());
						}
					} else {
						throw new RuntimeException("Edge does not fit any description.");
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveLagranges(String fileName, int iterations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			String str = "EdgeId;From;To;Capacity";
			for(int i=0; i<iterations; i++){
				str += ";" + i;
			}
			out.write(str); 
			out.newLine();
			for(Edge e : graph.getEdges()){
				if(e.isSail()){
					out.write(e.getId() + ";" + e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";" + e.getCapacity());
					for(int i : e.getLagrangeValues()){
						out.write(";" + i);
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveLoads(String fileName, int iterations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			String str = "EdgeId;From;To;Capacity";
			for(int i=0; i<iterations; i++){
				str += ";" + i;
			}
			out.write(str); 
			out.newLine();
			for(Edge e : graph.getEdges()){
				if(e.isSail()){
					out.write(e.getId() + ";" + e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";" + e.getCapacity());
					for(int i : e.getLoadValues()){
						out.write(";" + i);
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
