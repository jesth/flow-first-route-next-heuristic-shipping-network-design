package Sortables;

import Data.Demand;

public class SortableDemand implements Comparable<SortableDemand>{
	private int profit;
	private Demand demand;
	
	public SortableDemand(int profit, Demand demand){
		this.profit = profit;
		this.demand = demand;
	}
	
	public Demand getDemand(){
		return demand;
	}
	
	public int getProfit(){
		return profit;
	}
	
	@Override
	public int compareTo(SortableDemand o) {
		return profit-o.profit;
	}
}
