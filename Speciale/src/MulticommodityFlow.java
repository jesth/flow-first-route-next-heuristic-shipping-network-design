import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulticommodityFlow {
	private static Graph graph;
	//	private static int[] bestLagranges;
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
		bestFlowProfit = Integer.MIN_VALUE;
		//		bestLagranges = new int[graph.getEdges().size()];
		bestRoutes = new ArrayList<Route>();
		int iteration = 1;
		boolean invalidFlow = true;

		//TODO hardcoded 100 iterations...
		while (iteration < 101){
			System.out.println("Now running BellmanFord in iteration " + iteration);
			System.out.println();
			BellmanFord.run();
			if(iteration == 1){
				startLagrange();
			}
			findRepairFlow();
			System.out.println("Has found repair flow");
			invalidFlow = false;
			//TODO maybe stupid??
			for (Edge e : graph.getEdges()){
				if(!e.isSail())
					continue;
				if(e.getCapacity() < e.getLoad()){
					// if we initially didn't use the edge we set the lagrange to -1,
					// but since edge is now used in a shortest route for a OD-pair we have to update the lagrange.
					if(e.getLagrange() < 0 && !e.getRoutes().isEmpty()){
						e.resetLagrange();
					}
					int wasCost = e.getCost();
					e.adjustLagrange(iteration, true);
					BellmanFord.relaxEdge(e);
					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
					System.out.println();
				} else if(e.getCapacity() > e.getLoad()){
					int wasCost = e.getCost();
					e.adjustLagrange(iteration, false);
					BellmanFord.relaxEdge(e);
					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
					System.out.println();
				} else {
					System.out.println("Nothing to adjust");
				}
			}
			int flowProfit = Result.getFlowProfit(false);
			if(!invalidFlow && flowProfit > bestFlowProfit){
				System.out.println("Found better flow without repair: " + flowProfit + " > " + bestFlowProfit);
				updateBestFlow(flowProfit);
			}
			System.out.println();
			iteration++;
		}
		implementBestFlow();
		System.out.println("Exiting while loop after iteration " + iteration);
	}

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
				lowestProfit = -1001;
			}
			e.addLagrange(lowestProfit+1000);
		}
	}
	
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
	private static int findRepairFlow(){
		boolean invalidFlow = true;
		while(invalidFlow){
			invalidFlow = false;
			for(Edge e : graph.getEdges()){
				int overflow = e.getRepLoad() - e.getCapacity();
				if(e.isSail() && overflow > 0){
					System.out.println("Overflow = " + e.getRepLoad() + " - " + e.getCapacity() + " for edge " + e.simplePrint());
					invalidFlow = true;
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
					lowestProfitRoute.adjustFFErep(-FFErep);
					repDemand.createRepRoute(lowestProfitRoute, e, FFErep);
				}
			}
		}
		int flowProfit = Result.getFlowProfit(true);
		if(flowProfit > bestFlowProfit){
			System.out.println("Found better flow: " + flowProfit + " > " + bestFlowProfit);
			updateBestFlow(flowProfit);
		}
		return flowProfit;
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
}
