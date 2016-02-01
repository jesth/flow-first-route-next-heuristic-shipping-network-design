import java.util.ArrayList;
import java.util.Collections;

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
	public ArrayList<Rotation> getRotations() {
		return rotations;
	}

	public static int getObjective(){
		int obj = 0;
		
		obj = getFlowProfit(false);
		for(Rotation r : rotations){
			if(r.isActive()){
				VesselClass v = r.getVesselClass();
				ArrayList<Edge> rotationEdges = r.getRotationEdges();
				double sailingTime = 0;
				double idleTime = 0;
				int portCost = 0;
				for (Edge e : rotationEdges){
					if(e.isSail()){
						sailingTime += e.getTravelTime();
						if(e.isSuez()){
							obj += v.getSuezFee();
						}
						if(e.isPanama()){
							obj += v.getPanamaFee();
						}
					}
					if(e.isDwell()){
						idleTime += e.getTravelTime();
						portCost = e.getFromNode().getPort().getFixedCallCost();
					}
				}
				//TODO USD per metric tons fuel = 600
				double sailingBunkerCost = (int) Math.ceil(sailingTime/24.0) * v.getFuelConsumptionDesign() * 600;
				double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * 600;
						
				int rotationDays = (int) Math.ceil((sailingTime+idleTime)/24.0);
				obj += rotationDays * v.getTCRate() * r.calculateNoVessels();
				obj += sailingBunkerCost + idleBunkerCost + portCost;
			}
		}
		return obj;
	}
	
	public static int getFlowProfit(boolean repair){
		int flowProfit = 0;
		
		int flowCost = 0;
		for (Edge e : graph.getEdges()){
			if(repair){
				flowCost += e.getRealCost() * e.getRepLoad();
			} else {
				flowCost += e.getRealCost() * e.getLoad();
			}
		}
		int flowRevenue = 0;
		for (Demand d : graph.getData().getDemands()){
			flowRevenue += d.getDemand() * d.getRate();
		}
		flowProfit = flowRevenue - flowCost;
		
		return flowProfit;
	}
	
//	public static Demand[] getNlargestDemandLosses(int n){
//		if(n < 1){
//			throw new RuntimeException("Cannot find less than 1 Demand");
//		}
//		if(n > graph.getData().getDemands().size()){
//			throw new RuntimeException("Cannot find more than graph.getDemands().size() Demands");
//		}
//		
//		SortableDemand[] sortableDemands = new SortableDemand[n];
//		for(SortableDemand d : sortableDemands){
//			d = new SortableDemand(Integer.MAX_VALUE, new Demand());
//		}
//		
//		int largestODLoss = Integer.MAX_VALUE;
//		for(Demand d : graph.getData().getDemands()){
//			int odLoss = 0;
//			for(Route r : d.getRoutes()){
//				odLoss += r.getRealProfit() *  r.getFFE();
//			}
//			if(odLoss < sortableDemands[n-1]){
//				SortableDemand newSortableDemand = new SortableDemand(odLoss, d);
//				sortableDemands[n-1] = newSortableDemand;
//				Collections.sort(sortableDemands);
//			}
//		}
//		
//		Demand[] demands = new Demand[n];
//		for(int i=0; i<demands.length-1; i++){
//			demands[i] = sortableDemands[i].getDemand();
//		}
//		
//		return demands;
//	}
	
	
	public static Demand getLargestODLoss(){
		Demand OD = new Demand();
		int largestODLoss = Integer.MAX_VALUE;
		for(Demand d : graph.getData().getDemands()){
			int odLoss = 0;
			for(Route r : d.getRoutes()){
				odLoss += r.getRealProfit() *  r.getFFE();
			}
			if(odLoss < largestODLoss){
				largestODLoss = odLoss;
				OD = d;
			}
		}
		
		return OD;
	}
	
}
