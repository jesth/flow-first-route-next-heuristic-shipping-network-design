package Data;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import Graph.Edge;
import Methods.BellmanFord;
import Results.Route;

public class Demand {
	private int id;
	private Port origin;
	private Port destination;
	private int demand;
	private int rate;
	//	private int lagrangeProfit;
	//	private int realProfit;
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
		origin.setActive();
		destination.setActive();
		origin.addDemand(demand, rate);
		destination.addDemand(demand, rate);
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


	/**
	 * @return The Id.
	 */
	public int getId(){
		return this.id;
	}

	/**
	 * @return The "profit" of using a omission edge for a container.
	 */
	public int getOmissionProfit(){
		return -rate - 1000;
	}

	public Route createMainRoute(){
		Route newRoute = new Route(this, false);
		routes.add(newRoute);
		newRoute.setFFE(demand);
		newRoute.setFFErep(demand);
		ArrayList<Edge> route = BellmanFord.getRoute(this, false);
		newRoute.update(route);
		return newRoute;
	}

	/*
	public int createRepRoute(Route prevRoute, int FFErep){
		if(FFErep == 0){
			throw new RuntimeException("Creating route without any demand for OD-pair " + origin.getUNLocode() + "-" + destination.getUNLocode());
		}
		Route repRoute = new Route(this, true);
		routes.add(repRoute);
		repRoute.findRoute();
		ArrayList<Edge> route = BellmanFord.getRoute(this, true);
		repRoute.update(route);
		int maxUnderflow = repRoute.findMaxUnderflow();
		int correctedFFErep = Math.min(FFErep,maxUnderflow);
		repRoute.setFFErep(correctedFFErep);
		return correctedFFErep;
	}
	*/
	
	public void createRepRoute(Route prevRoute, int FFEforRemoval){
		if(FFEforRemoval == 0){
			return;
		}
		prevRoute.implementFFEforRemoval();
		Route repRoute = new Route(this, true);
		routes.add(repRoute);
		ArrayList<Edge> route = BellmanFord.getRouteRep(this);
		repRoute.update(route);
		repRoute.setFFErep(FFEforRemoval);
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
	
	public void checkDemand(){
		int servicedDemand = 0;
		for(Route r : routes){
			servicedDemand += r.getFFE();
		}
		if(servicedDemand != demand){
			throw new RuntimeException("The correct number of containers is not transported from " + origin.getUNLocode() + " to " + destination.getUNLocode() + ". Transported/Demand: " + servicedDemand + "/" + demand);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Demand [origin=" + origin.getName() + ", destination=" + destination.getName() + ", demand=" + demand + ", rate=" + rate
				+ ", maxTransitTime=" + maxTransitTime + "]";
	}


}
