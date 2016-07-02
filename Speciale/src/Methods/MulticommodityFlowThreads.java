package Methods;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Data.Data;
import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Results.Route;

public class MulticommodityFlowThreads implements Serializable{
	private static final long serialVersionUID = 1L;
	
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
		setBFsActive();
	}

	public void reset(){
		for(Edge e : graph.getEdges().values()){
			if(e.isSail()){
				e.setLagrangeStep(5000);
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
		for(Edge e : graph.getEdges().values()){
			if(e.isSail()){
				sailEdges.add(e);
			}
		}
		reset();
		bestFlowProfit = -Integer.MAX_VALUE;
		bestRoutes = new ArrayList<Route>();
		int iteration = 0;
		int repairCounter = 0;
		int improvementCounter = 0;
		//				startLagrange();
		//TODO hardcoded 100 iterations...
		long startTime = System.currentTimeMillis();

		while (improvementCounter < 20 && iteration < 100){
//						System.out.println("Now running BellmanFord in iteration " + iteration);
			for(Edge e : graph.getEdges().values()){
				e.clearRoutes();
			}
			if(!graph.isSubGraph()){
				runBF(true, false);
			} else {
				runBF(false, false);
			}
			boolean validFlow = false;
			double overflow = getOverflow();
			if(overflow < 0.3 || improvementCounter == 19 || iteration == 99){
				improvementCounter++;
				validFlow = findRepairFlow(improvementCounter, iteration);
				repairCounter++;
			}
			int flowProfit = graph.getResult().getFlowProfit(false);
			if(validFlow && flowProfit > bestFlowProfit){
//				System.out.println("Found better flow without repair: " + flowProfit + " > " + bestFlowProfit);
				updateBestFlow(flowProfit);
				repairCounter = 0;
				improvementCounter = 0;
			}

			for(Edge e : sailEdges){
				if(repairCounter >= 5){
					e.setLagrange(Math.max(e.getLagrange() / 2,1));
					e.saveValues(iteration);
				} else {
					e.lagrangeAdjustment(iteration);
				}
			}
			if(repairCounter >= 5){
				repairCounter = 0;
			}
			iteration++;
		}
		implementBestFlow();
		long endTime = System.currentTimeMillis();
//		saveLagranges("lagranges.csv", iteration);
//		saveLoads("loads.csv", iteration);
	}

	//TODO: ALWAYS runs threaded BF!
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
		for(Edge e : graph.getEdges().values()){
			if(e.isSail() && e.isActive()){
				sailEdges++;
				overflow += Math.max(((double) e.getLoad()/ (double) e.getCapacity())-1,0);
			}
		}
		overflow = overflow/(double) sailEdges;
		if(sailEdges == 0){
			return 0;
		}
		return overflow;
	}

	private boolean findRepairFlow(int improvementCounter, int iteration) throws InterruptedException{
		while(iteration >= 100){
			iteration -= 100;
		}
		boolean validFlow = true;
		ArrayList<Route> overflowRoutes = new ArrayList<Route>();
		if(!graph.isSubGraph()){
			runBF(true, true);
		} else {
			runBF(false, true);
		}
		int length = graph.getEdges().size();
		int counter = 0;
		ArrayList<Edge> edges = new ArrayList<Edge>(graph.getEdges().values());
		while(length > 0){
			if(counter >= 1000){
				counter -= 1000;
			}
			int i = (int) (length * Data.getRandomNumber(iteration, counter));
			Edge e = edges.remove(i);
			length--;
			counter++;
			if(e.isSail() || e.isFeeder()){
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
			updateBestFlow(flowProfit);
			for(Edge e : graph.getEdges().values()){
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
		for(Edge e : graph.getEdges().values()){
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

		for(Edge e : graph.getEdges().values()){
			if(e.getCapacity() < e.getLoad()){
				throw new RuntimeException("Capacity limit not respected on edge from " + e.getFromPortUNLo() + " to " + e.getToPortUNLo() + " with load: " + e.getLoad() + " and capacity: " + e.getCapacity());
			}
		}
//		graph.getResult().saveAllEdgesSol("AllEdgesSolErr.csv");
//		graph.getResult().saveODSol("ODSolErr.csv");
//		graph.getResult().saveRotationSol("RotationSolErr.csv");
//		graph.getResult().saveDemands("DemandSolErr.csv");
		for(Demand d : graph.getDemands()){
			d.checkDemand();
		}
	}

	public void setBFActive(int fromPortId) {
		for(BellmanFord bf : bellmanFords){
			if(bf.getCentroidNode().getPortId() == fromPortId){
				bf.setActive();
			}
		}
	}

	
	public void setBFsActive(){
		boolean[] activeCentroids = new boolean[Data.getPortsMap().size()];
		for(Demand d : graph.getDemands()){
			int portId = d.getOrigin().getPortId();
			activeCentroids[portId] = true;
		}
		for(BellmanFord bf : bellmanFords){
			int portId = bf.getCentroidNode().getPortId();
			if(activeCentroids[portId]){
				bf.setActive();
			} else {
				bf.setInactive();
			}
		}
	}
}
