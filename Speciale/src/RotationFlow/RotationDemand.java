package RotationFlow;

import java.util.ArrayList;

import Data.Demand;
import Data.Port;
import Graph.Edge;
import Methods.BellmanFord;
import Results.Route;

public class RotationDemand {
	private Demand orgDemand;
	private RotationNode origin;
	private RotationNode destination;
	private int demand;
	private ArrayList<RotationRoute> routes;

	public RotationDemand(Demand orgDemand, int demand){
		this.orgDemand = orgDemand;
		this.demand = demand;
		this.routes = new ArrayList<RotationRoute>();
	}

	public void createRepRoute(int FFE, ArrayList<RotationEdge> route){
		RotationRoute r = new RotationRoute(this);
		r.setRoute(route);
		r.setFFE(FFE);
		routes.add(r);
	}

	public Demand getOrgDemand() {
		return orgDemand;
	}

	public int getDemand() {
		return demand;
	}

	public void addDemand(int addDemand){
		demand += addDemand;
	}

	public void setOrigin(RotationNode origin) {
		this.origin = origin;
	}

	public void setDestination(RotationNode destination) {
		this.destination = destination;
	}

	public RotationNode getOrigin() {
		return origin;
	}

	public RotationNode getDestination() {
		return destination;
	}

	//	public int getTransported() {
	//		return transported;
	//	}
	//
	//	public int getOmission() {
	//		return omission;
	//	}

	public ArrayList<RotationRoute> getRoutes() {
		return routes;
	}

	/*
	public void setRoute(ArrayList<RotationEdge> route){
		this.route = route;
		setTransported(demand);
		for(RotationEdge e : route){
			e.addServicedDemand(this);
		}
	}
	 */

	//	public void setTransported(int transported){
	//		this.transported = transported;
	//		this.omission = this.demand - this.transported;
	//		if(omission < 0){
	//			throw new RuntimeException("Negative number of omission containers.");
	//		}
	//	}
	//
	//	public void addOmission(int removedFFE) {
	//		omission += removedFFE;
	//		transported -= removedFFE;
	//		if(omission + transported != demand || transported < 0){
	//			throw new RuntimeException("Something is completely wrong with the number of transported/number of omission containers.");
	//		}
	//	}
	//
	//	public void resetTransportedDemand(){
	//		this.transported = 0;
	//		this.omission = 0;
	//	}

	public int getTotalCost(){
		int cost = 0;
		for(RotationRoute r : routes){
			cost += r.getTotalCost();
//			if(r.getDemand().getOrigin().getPort().getUNLocode().equals("MYPKG") && r.getDemand().getDestination().getPort().getUNLocode().equals("DEHAM")){
//				System.out.println("Route cost MYPKG-DEHAM: " + r.getTotalCost() + " for " + r.getFFE() + " FFE");
//			}
		}
		return cost;
	}

	public void clearRoutes(){
		this.routes.clear();
	}

	public void createRoute(ArrayList<RotationEdge> route) {
		RotationRoute r = new RotationRoute(this);
		r.setRoute(route);
		r.setFFE(demand);
		routes.add(r);	
	}

	public void removeRoute(RotationRoute r) {
		routes.remove(r);		
	}
	
	public void rerouteOmissionFFEs() {
		ArrayList<RotationRoute> omissionRoutes = new ArrayList<RotationRoute>();
		int omissionDemand = 0;
		for(RotationRoute r : routes){
			boolean omission = false;
			for(RotationEdge e : r.getRoute()){
				if(e.isOmission()){
					omission = true;
				}
			}
			if(omission){
				omissionRoutes.add(r);
				omissionDemand += r.getFFE();
			}
		}
		if(omissionDemand > 0){
			ArrayList<RotationEdge> altRoute = RotationBellmanFord.getRouteRep(this);
			boolean omission = altRoute.get(0).isOmission();
			if(!omission){
				int altCap = Integer.MAX_VALUE;
				for(RotationEdge e : altRoute){
					int underflow = e.getCapacity() - e.getLoad();
					if(underflow < altCap){
						altCap = underflow;
					}
				}
				if(altCap > 0){
					int rerouted = 0;
					for(RotationRoute r : omissionRoutes){
						int reroute = Math.min(altCap, r.getFFE());
						altCap -= reroute;
						rerouted += reroute;
						r.removeFFE(reroute);
					}
					RotationRoute repRoute = new RotationRoute(this);
					routes.add(repRoute);
					repRoute.setRoute(altRoute);
					repRoute.setFFE(rerouted);
				}
			}
		}
	}
}
