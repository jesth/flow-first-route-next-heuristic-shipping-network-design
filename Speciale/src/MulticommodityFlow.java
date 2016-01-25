import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulticommodityFlow {
	private static Graph graph;
	private static int[] bestLagranges;
	private static int bestFlowProfit;

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
		bestLagranges = new int[graph.getEdges().size()];
		int iteration = 1;
		boolean invalidFlow = true;
		while (invalidFlow){
			findRepairFlow();
			System.out.println("Now running BellmanFord in iteration " + iteration);
			System.out.println();
			BellmanFord.run();
			invalidFlow = false;
			for (Edge e : graph.getEdges()){
				if(e.getCapacity() < e.getLoad()){
					System.out.println("Invalid flow on " + e.simplePrint());
					invalidFlow = true;
					int lowestProfit = Integer.MAX_VALUE;
					for(Demand d : e.getShortestPathOD()){
						if(d.getLagrangeProfit() < lowestProfit){
							lowestProfit = d.getLagrangeProfit();
						}
					}
					int wasCost = e.getCost();
					e.addLagrange(lowestProfit+1000, iteration);
					BellmanFord.relaxEdge(e);
					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
					System.out.println();
				}
			}
			int flowProfit = Result.getFlowProfit();
			if(!invalidFlow && flowProfit > bestFlowProfit){
				updateBestFlow(flowProfit);
			}
			System.out.println();
			iteration++;
		}
		implementBestFlow();
		System.out.println("Exiting while loop after iteration " + iteration);
	}

	/** Based on a flow obtained by the Bellman Ford-algorithm, a legal flow is computed by:
	 * <br>1) Running through all edges in unprioritized order.
	 * <br>2) Only sail edges are considered, as these are the only ones that can 
	 * have a restricting capacity (dwell edges can never be restricting).
	 * <br>3) If the capacity is violated, the profit per container of each of the serviced OD pairs is considered.
	 * <br>4) The OD pair with the lowest profit is chosen for removal to omission edges.
	 * <br>5) Containers are removed down to the capacity limit, or until all containers of the OD pair have been removed.
	 * <br>6) If a capacity violation was found on any edge in step 3), the process is repeated from 1).
	 * @return The profit of the repaired flow.
	 */
	private static int findRepairFlow(){
		for(Demand d : graph.getData().getDemands()){
			d.resetRepOmissionFFE();
		}
		int flowProfitPrev = Result.getFlowProfit();
		int flowProfit = flowProfitPrev;
		boolean invalidFlow = true;
		while(invalidFlow){
			invalidFlow = false;
			for(Edge e : graph.getEdges()){
				int overflow = e.getRepLoad() - e.getCapacity();
				if(e.isSail() && overflow > 0){
					invalidFlow = true;
					int lowestProfit = Integer.MAX_VALUE;
					Demand lowestProfitOD = null;
					for(Demand d : e.getShortestPathOD()){
						if(d.getLagrangeProfit() < lowestProfit){
							lowestProfit = d.getLagrangeProfit();
							lowestProfitOD = d;
						}
					}
					int repOmissionFFE = Math.min(lowestProfitOD.getDemand(), overflow);
					lowestProfitOD.setRepOmissionFFE(repOmissionFFE);
					flowProfit -= lowestProfitOD.getRealProfit() * repOmissionFFE;
					flowProfit += lowestProfitOD.getOmissionProfit() * repOmissionFFE;
				}
			}
		}
		if(flowProfit > flowProfitPrev){
			throw new RuntimeException("Repair flow result invalid.");
		}
		if(flowProfit > bestFlowProfit){
			updateBestFlow(flowProfit);
		}
		return flowProfit;
	}

	/** Updates the best flow to the current flow. Saves the Lagranges and objective value of the current flow. 
	 * @param bestFlowProfitIn - the profit of the current flow that is to be saved as the best flow.
	 */
	private static void updateBestFlow(int bestFlowProfitIn){
		for(Edge e : graph.getEdges()){
			bestLagranges[e.getId()] = e.getLagrange();
		}
		bestFlowProfit = bestFlowProfitIn;
	}

	/** Implements the saved best flow by:  
	 * <br>1) Resetting the labels saved by previous runs of the Bellman-Ford algorithm.
	 * <br>2) Setting the Lagrange values to the saved best values.
	 * <br>3) Running Bellman-Ford again to obtain the corresponding flow.
	 * <br>4) Running the findRepairFlow()-function to find the number of excess containers on all edges.
	 * <br>5) Setting the load on all edges to the feasible load found by the findRepairFlow()-function.
	 */
	public static void implementBestFlow(){
		for(Edge e : graph.getEdges()){
			int bestLagrange = bestLagranges[e.getId()];
			e.setLagrange(bestLagrange);
		}
		BellmanFord.reset();
		BellmanFord.run();
		findRepairFlow();
		for(Edge e : graph.getEdges()){
			e.setLoad(e.getRepLoad());
		}
		for(Demand d : graph.getData().getDemands()){
			Node fromNode = d.getOrigin().getCentroidNode();
			Node toNode = d.getDestination().getCentroidNode();
			for(Edge e : fromNode.getOutgoingEdges()){
				if(e.getToNode().equals(toNode) && e.isOmission()){
					e.setLoad(d.getRepOmissionFFE());
					break;
				}
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
				ArrayList<Edge> route = BellmanFord.getRoute(d);
				if(d.getDemand() > d.getRepOmissionFFE()){
					for(Edge e : route){
						if(e.isSail() || e.isOmission()){
							if(e.isOmission()){
								out.write(";;");
							} else {
								out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
							}
							out.write(d.getId()+";");
							out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
							out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
							out.write(d.getDemand()-d.getRepOmissionFFE()+";");
							if(e.isOmission()){
								out.write("1");
							} else {
								out.write("0");
							}
							out.newLine();
						}
					}
				} if(d.getRepOmissionFFE() > 0){
					out.write(";;");
					out.write(d.getId()+";");
					out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
					out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
					out.write(d.getRepOmissionFFE()+";");
					out.write("1");
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
