package RotationFlow;

import Data.Demand;

public class RotationDemand {
	private Demand orgDemand;
	private int demand;
	
	public RotationDemand(Demand orgDemand, int demand){
		this.orgDemand = orgDemand;
		this.demand = demand;
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

}
