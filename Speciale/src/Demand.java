import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Demand {
	private int id;
	private Port origin;
	private Port destination;
	private int demand;
	private int rate;
	private int lagrangeProfit;
	private int realProfit;
	//	private int repOmissionFFE;
	private ArrayList<Route> routes;
	private int maxTransitTime;
	private static AtomicInteger idCounter = new AtomicInteger();

	public Demand(){
	}

	/**
	 * @param origin
	 * @param destination
	 * @param demand
	 * @param rate
	 * @param maxTransitTime
	 */
	public Demand(Port origin, Port destination, int demand, int rate, int maxTransitTime) {
		super();
		this.id = idCounter.getAndIncrement();
		this.origin = origin;
		this.destination = destination;
		this.demand = demand;
		this.rate = rate;
		//		this.lagrangeProfit = 0;
		//		this.realProfit = 0;
		//		this.repOmissionFFE = 0;
		this.routes = new ArrayList<Route>();
		this.maxTransitTime = maxTransitTime;
	}

	/**
	 * @return the origin
	 */
	public Port getOrigin() {
		return origin;
	}

	/**
	 * @return the destination
	 */
	public Port getDestination() {
		return destination;
	}

	/**
	 * @return the demand
	 */
	public int getDemand() {
		return demand;
	}

	/**
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}

	/**
	 * @return the maxTransitTime
	 */
	public int getMaxTransitTime() {
		return maxTransitTime;
	}

	public int getId(){
		return this.id;
	}

	public int getOmissionProfit(){
		return -rate - 1000;
	}

	public Route createMainRoute(){
		Route newRoute = new Route(this, false);
		routes.add(newRoute);
		newRoute.setFFE(demand);
		newRoute.setFFErep(demand);
		return newRoute;
	}

	public Route createRepRoute(Route prevRoute, Edge prohibitedEdge, int FFErep){
		Route repRoute = new Route(this, true);
		routes.add(repRoute);
		repRoute.addProhibitedEdge(prohibitedEdge);
		if(prevRoute.isRepair()){
			for(Edge e : prevRoute.getProhibitedEdges()){
				repRoute.addProhibitedEdge(e);
			}
		}
		repRoute.setFFErep(FFErep);
		repRoute.findRoute();
		return repRoute;
	}

	public void clearRoutes(){
		routes.clear();
	}
	
	public ArrayList<Route> getRoutes(){
		return routes;
	}
	
	public void addRoute(Route addRoute){
		routes.add(addRoute);
	}
	
	public void removeRoute(Route removeRoute){
		routes.remove(removeRoute);
	}

	/*
	public int getRepOmissionFFE() {
		return repOmissionFFE;
	}

	public void setRepOmissionFFE(int repOmissionFFE) {
		this.repOmissionFFE = repOmissionFFE;
	}

	public void resetRepOmissionFFE(){
		this.repOmissionFFE = 0;
	}
	 */

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Demand [origin=" + origin.getName() + ", destination=" + destination.getName() + ", demand=" + demand + ", rate=" + rate
				+ ", maxTransitTime=" + maxTransitTime + "]";
	}


}
