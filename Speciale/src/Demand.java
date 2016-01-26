import java.util.concurrent.atomic.AtomicInteger;

public class Demand {
	private int id;
	private Port origin;
	private Port destination;
	private int demand;
	private int rate;
	private int lagrangeProfit;
	private int realProfit;
	private int repOmissionFFE;
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
		this.lagrangeProfit = 0;
		this.realProfit = 0;
		this.repOmissionFFE = 0;
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
	
	/**
	 * @return The Id.
	 */
	public int getId(){
		return this.id;
	}
	
	/**
	 * @return The lagrange profit of transporting a container given the current route.
	 */
	public int getLagrangeProfit() {
		return lagrangeProfit;
	}

	/** Set the lagrange profit of transporting a container given the current route.
	 * @param lagrangeProfit
	 */
	public void setLagrangeProfit(int lagrangeProfit) {
		this.lagrangeProfit = lagrangeProfit;
	}
	
	/**
	 * @return The real profit of transporting a container given the current route.
	 */
	public int getRealProfit() {
		return realProfit;
	}

	/** Set the real profit of transporting a container given the current route.
	 * @param realProfit
	 */
	public void setRealProfit(int realProfit) {
		this.realProfit = realProfit;
	}
	
	/**
	 * @return The "profit" of using a omission edge for a container.
	 */
	public int getOmissionProfit(){
		return -rate - 1000;
	}
	
	/**
	 * @return The number of containers to be sent on the omission edge after the <b>findRepairFlow()</b> algorithm is used.
	 */
	public int getRepOmissionFFE() {
		return repOmissionFFE;
	}

	/** Sets the number of containers to be sent on the omission edge. This is used and set in the <b>findRepairFlow()</b> algorithm.
	 * @param repOmissionFFE
	 */
	public void setRepOmissionFFE(int repOmissionFFE) {
		this.repOmissionFFE = repOmissionFFE;
	}

	/**
	 * Resets the number of containers to be sent on the omission edge, i.e. 0.
	 */
	public void resetRepOmissionFFE(){
		this.repOmissionFFE = 0;
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
