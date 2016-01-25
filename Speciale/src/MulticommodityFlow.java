import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulticommodityFlow {
	private static Graph graph;
	private static int[] bestLagranges;
	private static int bestFlowProfit;

	public static void initialize(Graph inputGraph){
		graph = inputGraph;
		BellmanFord.initialize(graph);
	}

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
	
	private static void updateBestFlow(int bestFlowProfitIn){
		for(Edge e : graph.getEdges()){
			bestLagranges[e.getId()] = e.getLagrange();
		}
		bestFlowProfit = bestFlowProfitIn;
	}

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
					if(e.getLoad() != 0){
						throw new RuntimeException("Setting load on omission edge that already has a load.");
					}
					e.setLoad(d.getRepOmissionFFE());
					break;
				}
			}
		}
	}

	public static void saveODSol(String fileName, ArrayList<Demand> demands){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Omission"); 
			out.newLine();
			for(Demand d : demands){
				ArrayList<Edge> route = BellmanFord.getRoute(d);
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
						out.write(d.getDemand()+";");
						if(e.isOmission()){
							out.write("1");
						} else {
							out.write("0");
						}
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
}
