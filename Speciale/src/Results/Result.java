package Results;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Sortables.SortableAuxEdge;

public class Result {
	private Graph graph;
	private ArrayList<Rotation> rotations;
	
	public Result(Graph inputGraph){
		graph = inputGraph;
		rotations = new ArrayList<Rotation>();
	}
	
	public void addRotation(Rotation r){
		rotations.add(r);
	}
	
	public void removeRotation(Rotation r){
		rotations.remove(r);
	}
	
	/**
	 * @return the rotations
	 */
	public ArrayList<Rotation> getRotations() {
		return rotations;
	}

	public int getObjective(){
		int obj = 0;
		obj = getFlowProfit(false);
		for(Rotation r : rotations){
			if(r.isActive()){
				obj -= r.calcCost();
			}
		}
		
		return obj;
	}
	
	public int getFlowProfit(boolean repair){
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
		for (Demand d : graph.getDemands()){
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
	
	public Demand getLargestODLoss(){
		Demand OD = null;
		int largestODLoss = Integer.MAX_VALUE;
		for(Demand d : graph.getDemands()){
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
