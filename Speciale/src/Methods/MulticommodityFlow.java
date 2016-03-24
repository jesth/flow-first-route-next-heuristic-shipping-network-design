package Methods;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Results.Result;
import Results.Rotation;
import Results.Route;

public class MulticommodityFlow {
	private static Graph graph;
	private static int bestFlowProfit;
	private static ArrayList<Route> bestRoutes;

	/** Initializes the multicommodity flow by saving the graph and initializing Bellman Ford.
	 * @param inputGraph - the graph to be searched through to find the multicommodity flow.
	 */
	public static void initialize(Graph inputGraph){
		graph = inputGraph;
		BellmanFord.initialize(graph);
	}

	/** Runs the multicommodity flow algorithm by:
	 * <br>1) Running the Bellman Ford-algorithm.
	 * <br>2) If the flow is illegal: 
	 * <br>&nbsp&nbsp&nbsp a) Lagrange values are computed.
	 * <br>&nbsp&nbsp&nbsp b) The illegal edges are relaxed by BellmanFord.relaxEdge(e).
	 * <br>&nbsp&nbsp&nbsp c) The flow is repaired by findRepairFlow().
	 * <br>&nbsp&nbsp&nbsp d) The process is repeated from step 1).
	 * <br>3) If the flow is legal, the best found flow is implemented.
	 */
	public static void run(){
		ArrayList<Edge> sailEdges = new ArrayList<Edge>();
		for(Edge e : graph.getEdges()){
			if(e.isSail()){
				sailEdges.add(e);
			}
		}
		BellmanFord.reset();
		bestFlowProfit = Integer.MIN_VALUE;
		bestRoutes = new ArrayList<Route>();
		int iteration = 0;
		int repairCounter = 0;
		int improvementCounter = 0;
		//				startLagrange();
		//TODO hardcoded 100 iterations...
		long startTime = System.currentTimeMillis();
		while (iteration < 100){
			System.out.println("Now running BellmanFord in iteration " + iteration);
			//			System.out.println();
			BellmanFord.run();
			boolean validFlow = false;
			double overflow = getOverflow();
			if(overflow < 0.1){
				System.out.println("Finding repair flow.");
				validFlow = findRepairFlow();
//				repairCounter++;
				improvementCounter++;
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
					e.setLagrange(Math.max(e.getLagrange() / 3,1));
				} else {
					e.lagrangeAdjustment(iteration);
				}
				//				}
			}
			if(repairCounter >= 5){
				System.out.println("Two/thirding Lagranges.");
				repairCounter = 0;
			}
			iteration++;
		}
		implementBestFlow();
		long endTime = System.currentTimeMillis();
		saveLagranges("lagranges.csv", iteration);
		saveLoads("loads.csv", iteration);
		System.out.println("RunningTime " + (endTime-startTime));
		System.out.println("Exiting while loop after iteration " + iteration);
	}

	private static double getOverflow() {
		double overflow = 0;
		int sailEdges = 0;
		for(Edge e : graph.getEdges()){
			if(e.isSail()){
				sailEdges++;
				overflow += Math.max(((double) e.getLoad()/ (double) e.getCapacity())-1,0);
			}
		}
		overflow = overflow/(double) sailEdges;

		return overflow;
	}

	private static void startLagrange(){
		for (Edge e : graph.getEdges()){
			if(e.isSail()){
				e.setLagrange(1000);

			}
		}
	}

	/*
	//TODO: Update description.
	private static void startLagrange() {
		for (Edge e : graph.getEdges()){
			//TODO only sail edges are relevant right?
			if(!e.isSail())
				continue;
			//TODO if lagrange is already set (from previously) we reuse it
			if(e.getLagrange() > 0)
				continue;
			int lowestProfit = Integer.MAX_VALUE;
			for(Route r : e.getRoutes()){
				if(r.getLagrangeProfit() < lowestProfit){
					lowestProfit = r.getLagrangeProfit();
				}
			}
			if(lowestProfit == Integer.MAX_VALUE){
				System.out.println("Setting lagrange to -1");
				lowestProfit = -1001;
			}
			e.addLagrange(lowestProfit+1000);
		}
	}
	 */

	/** Based on a flow obtained by the Bellman Ford-algorithm, a legal flow is computed by:
	 * <br>1) Running through all edges in unprioritized order.
	 * <br>2) Only sail edges are considered, as these are the only ones that can 
	 * have a restricting capacity (dwell edges can never be restricting).
	 * <br>3) If the capacity is violated, the profit per container of each of the serviced OD pairs is considered.
	 * <br>4) The OD pair with the lowest profit is chosen for computation of an alternative repair route.
	 * <br>5) The repair route is computed.
	 * <br>6) Containers are removed down to the capacity limit, or until all containers of the OD pair have been removed.
	 * <br>7) If a capacity violation was found on any edge in step 3), the process is repeated from 1).
	 * @return The profit of the repaired flow.
	 */

	/*
	private static boolean findRepairFlow(){
		System.out.println("Finding repair flow.");
		boolean firstValid = true;
		boolean invalidFlow = true;
		ArrayList<Edge> overflowEdges = new ArrayList<Edge>();
//		for(Edge e : graph.getEdges()){
		for(int i = graph.getEdges().size()-1; i>= 0; i--){
			Edge e = graph.getEdges().get(i);
			int overflow = e.getRepLoad() - e.getCapacity();
			if(e.isSail() && overflow > 0){
				overflowEdges.add(e);
			}
		}
		while(invalidFlow){
			//			System.out.println("Iteration: " + counter);
			invalidFlow = false;
			for(int i = overflowEdges.size()-1; i >= 0; i--){
				Edge e = overflowEdges.get(i);
				int overflow = e.getRepLoad() - e.getCapacity();
				if(overflow > 0){
					invalidFlow = true;
					firstValid = false;
					int lowestProfit = Integer.MAX_VALUE;
					Route lowestProfitRoute = null;
					for(Route r : e.getRoutes()){
						if(r.getLagrangeProfit() < lowestProfit){
							lowestProfit = r.getLagrangeProfit();
							lowestProfitRoute = r;
						}
					}
					Demand repDemand = lowestProfitRoute.getDemand();
					int FFErep = Math.min(lowestProfitRoute.getFFErep(), overflow);
					FFErep = repDemand.createRepRoute(lowestProfitRoute, FFErep);
					lowestProfitRoute.adjustFFErep(-FFErep);
				} else {
					overflowEdges.remove(i);
				}
			}
		}
		int flowProfit = graph.getResult().getFlowProfit(true);
		if(flowProfit > bestFlowProfit){
			System.out.println("Found better flow: " + flowProfit + " > " + bestFlowProfit);
			updateBestFlow(flowProfit);
			for(Edge e : graph.getEdges()){
				if(e.isSail()){
					e.decreaseLagrangeStep();
				}
			}
		}
		return firstValid;
	}
	 */

	private static boolean findRepairFlow(){
		boolean validFlow = true;
		ArrayList<Route> overflowRoutes = new ArrayList<Route>();
//		for(Edge e : graph.getEdges()){
		for(int i = graph.getEdges().size()-1; i>= 0; i--){
			Edge e = graph.getEdges().get(i);
			if(e.isSail()){
				int overflow = e.getRepAndRemoveLoad() - e.getCapacity();
				if(overflow > 0){
					validFlow = false;
					findOverflowRoutes(e, overflow, overflowRoutes);
				}
			}
		}
		BellmanFord.runRep();
		for(Route r : overflowRoutes){
			//			System.out.println("Route from " + r.getDemand().getOrigin().getUNLocode() + " to " + r.getDemand().getDestination().getUNLocode() + " with FFEforRemoval " + r.getFFEforRemoval());
			Demand d = r.getDemand();
			d.createRepRoute(r, r.getFFEforRemoval());
		}
		if(!validFlow){
			findRepairFlow();
		}
		int flowProfit = graph.getResult().getFlowProfit(true);
		if(flowProfit > bestFlowProfit){
			System.out.println("Found better flow: " + flowProfit + " > " + bestFlowProfit);
			updateBestFlow(flowProfit);
			for(Edge e : graph.getEdges()){
				if(e.isSail()){
					e.decreaseLagrangeStep();
				}
			}
		}
		return validFlow;
	}


	private static void findOverflowRoutes(Edge e, int overflow, ArrayList<Route> overflowRoutes){
		Route r = e.findLeastProfitableRoute();
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
	private static void updateBestFlow(int bestFlowProfitIn){
		bestRoutes.clear();
		for(Demand d : graph.getData().getDemands()){
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
	public static void implementBestFlow(){
		for(Demand d : graph.getData().getDemands()){
			d.clearRoutes();
		}
		for(Edge e : graph.getEdges()){
			e.clearRoutes();
		}
		for(Route r : bestRoutes){
			r.setFFE(r.getFFErep());
			r.getDemand().addRoute(r);
			for(Edge e : r.getRoute()){
				e.addRoute(r);
			}
		}
		for(Edge e : graph.getEdges()){
			if(e.getCapacity() < e.getLoad()){
				throw new RuntimeException("Capacity limit not respected on edge from " + e.getFromPortUNLo() + " to " + e.getToPortUNLo());
			}
		}
		for(Demand d : graph.getData().getDemands()){
			d.checkDemand();
		}
	}

	/** Saves the routes of all demand pairs in csv-format for easy handling in Excel.
	 * @param fileName - the complete path or name of the file to be saved. End on .csv.
	 * @param demands - the list of demands to be saved.
	 */
	public static void saveODSol(String fileName, ArrayList<Demand> demands){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Omission"); 
			out.newLine();
			for(Demand d : demands){
				for(Route r : d.getRoutes()){
					for(Edge e : r.getRoute()){
						if(e.isSail() || e.isOmission()){
							if(e.isOmission()){
								out.write(";;");
							} else {
								out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
							}
							out.write(d.getId()+";");
							out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
							out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
							out.write(r.getFFE()+";");
							if(e.isOmission()){
								out.write("1");
							} else {
								out.write("0");
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

	public static void saveRotationSol(String fileName, ArrayList<Rotation> rotations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;LegFrom;LegTo;#FFE;LoadingFrom;UnloadingTo"); 
			out.newLine();
			for(Rotation r : rotations){
				for(Edge e : r.getRotationEdges()){
					if(e.isSail()){
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

	public static void saveLagranges(String fileName, int iterations){
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

	public static void saveLoads(String fileName, int iterations){
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
