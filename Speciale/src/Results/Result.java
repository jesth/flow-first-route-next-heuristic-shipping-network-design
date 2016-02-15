package Results;
import java.util.ArrayList;
import java.util.Arrays;
import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Sortables.SortableDemand;

public class Result {
	private static Graph graph;
	private static ArrayList<Rotation> rotations;

	public static void initialize(Graph inputGraph){
		graph = inputGraph;
		rotations = new ArrayList<Rotation>();
	}
	
	public static void addRotation(Rotation rotation){
		rotations.add(rotation);
	}
	
	/**
	 * @return the rotations
	 */
	public static ArrayList<Rotation> getRotations() {
		return rotations;
	}

	public static int getObjective(){
		int obj = 0;
		obj = getFlowProfit(false);
		for(Rotation r : rotations){
			if(r.isActive()){
				obj -= r.calcCost();
			}
		}
		
		return obj;
	}
	
	public static int getFlowProfit(boolean repair){
		int flowProfit = 0;
		int omissionCost = 0;
		int flowCost = 0;
		for (Edge e : graph.getEdges()){
			if(repair){
				if(e.isOmission()){
					omissionCost += 1000 * e.getRepLoad();
				} else {
					flowCost += e.getRealCost() * e.getRepLoad();
				}
			} else {
				if(e.isOmission()){
					omissionCost += 1000 * e.getLoad();
				} else {
					flowCost += e.getRealCost() * e.getLoad();
				}
			}
			
		}
		int flowRevenue = 0;
		for (Demand d : graph.getData().getDemands()){
			for(Route r : d.getRoutes()){
				if(r.getRoute().size() > 1 && repair){
					flowRevenue += r.getFFErep() * d.getRate();
				} else if(r.getRoute().size() > 1 && !repair){
					flowRevenue += r.getFFE() * d.getRate();
				}
			}
		}
//		System.out.println("flowRevenue " + flowRevenue + ". flowCost " + flowCost + ". omissionCost " + omissionCost);
		flowProfit = flowRevenue - flowCost - omissionCost;
		
		return flowProfit;
	}
	
	public static Demand[] getNlargestDemandLosses(int n){
		if(n < 1){
			throw new RuntimeException("Cannot find less than 1 Demand");
		}
		if(n > graph.getData().getDemands().size()){
			throw new RuntimeException("Cannot find more than graph.getDemands().size() Demands");
		}
		
		SortableDemand[] sortableDemands = new SortableDemand[n];
		for(int i=0; i<sortableDemands.length; i++){
			sortableDemands[i] = new SortableDemand(Integer.MAX_VALUE, new Demand());
		}
		
		for(Demand d : graph.getData().getDemands()){
			int odLoss = 0;
			for(Route r : d.getRoutes()){
				odLoss -= r.getRealProfit() *  r.getFFE();
			}
			if(odLoss < sortableDemands[n-1].getProfit()){
				SortableDemand newSortableDemand = new SortableDemand(odLoss, d);
				sortableDemands[n-1] = newSortableDemand;
				Arrays.sort(sortableDemands);
			}
		}
		
		Demand[] demands = new Demand[n];
		for(int i=0; i<demands.length; i++){
			demands[i] = sortableDemands[i].getDemand();
		}
		
		return demands;
	}
	
	public static Demand getLargestODLoss(){
		Demand OD = null;
		int largestODLoss = Integer.MAX_VALUE;
		for(Demand d : graph.getData().getDemands()){
			int odLoss = 0;
			for(Route r : d.getRoutes()){
				odLoss += r.getRealProfit() *  r.getFFE();
			}
//			System.out.println("From " + d.getOrigin().getUNLocode() + " to " + d.getDestination().getUNLocode() + " loss of profit = " + odLoss);
			if(odLoss < largestODLoss){
				largestODLoss = odLoss;
				OD = d;
			}
		}
		
		return OD;
	}
	
}
