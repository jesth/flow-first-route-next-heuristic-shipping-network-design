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
		double totalSail = 0;
		double totalIdle = 0;
		int totalPort = 0;
		int totalVessel = 0;
		obj = getFlowProfit(false);
		int rotationNumber = 0;
		for(Rotation r : rotations){
			if(r.isActive()){
				VesselClass v = r.getVesselClass();
				ArrayList<Edge> rotationEdges = r.getRotationEdges();
				double sailingTime = 0;
				double idleTime = 0;
				int portCost = 0;
				double distance = 0;
				int suezCost = 0;
				int panamaCost = 0;
				for (Edge e : rotationEdges){
					if(e.isSail()){
						sailingTime += e.getTravelTime();
						distance += e.getDistance().getDistance();
						Port p = e.getToNode().getPort();
						portCost += p.getFixedCallCost() + p.getVarCallCost() * v.getCapacity();
						if(e.isSuez()){
							suezCost += v.getSuezFee();
						}
						if(e.isPanama()){
							panamaCost += v.getPanamaFee();
						}
					}
					if(e.isDwell()){
						idleTime += e.getTravelTime();
					}
				}
				//TODO USD per metric tons fuel = 600
				double sailingBunkerCost = (int) Math.ceil(sailingTime/24.0) * v.getFuelConsumptionDesign() * 600;
				double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * 600;
				
				int rotationDays = (int) Math.ceil((sailingTime+idleTime)/24.0);
				int TCCost = rotationDays * v.getTCRate();
				
				System.out.println("Rotation number "+ rotationNumber);
				System.out.println("Voyage duration in nautical miles " + distance);
				System.out.println(r.calculateNoVessels() + " ships needed for rotationTime of " + r.getRotationTime());
				System.out.println("Port call cost " + portCost);
				System.out.println("Bunker idle burn in Ton " + idleBunkerCost/600.0);
				System.out.println("Bunker fuel burn in Ton " + sailingBunkerCost/600.0);
				System.out.println("Total TC cost " + TCCost);
				System.out.println();
				
				totalSail += sailingBunkerCost;
				totalIdle += idleBunkerCost;
				totalPort += portCost;
				totalVessel += rotationDays * v.getTCRate();
				obj -= sailingBunkerCost + idleBunkerCost + portCost + suezCost + panamaCost + TCCost;
			}
			rotationNumber++;
		}
		System.out.println("Vessel Class cost: " + totalVessel);
		System.out.println("Bunker idle burn cost: " + totalIdle);
		System.out.println("fuel_burn: " + totalSail);
		System.out.println("Port Call Cost: " + totalPort);
		
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
		System.out.println("flowRevenue " + flowRevenue + ". flowCost " + flowCost + ". omissionCost " + omissionCost);
		flowProfit = flowRevenue - flowCost - omissionCost;
		
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
