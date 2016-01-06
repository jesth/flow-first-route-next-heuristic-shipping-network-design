
public class ODpair {
	private Port origin;
	private Port destination;
	private int demand;
	private int rate;
	private int maxTransitTime;
	private double maxDraft;
	private int distance;
	private boolean suez;
	private boolean panama;
	
	public ODpair(){
	}

	public ODpair(Port origin, Port destination, int demand, int rate, int maxTransitTime, double maxDraft,
			int distance, boolean suez, boolean panama) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.demand = demand;
		this.rate = rate;
		this.maxTransitTime = maxTransitTime;
		this.maxDraft = maxDraft;
		this.distance = distance;
		this.suez = suez;
		this.panama = panama;
	}

	public Port getOrigin() {
		return origin;
	}

	public Port getDestination() {
		return destination;
	}

	public int getDemand() {
		return demand;
	}

	public int getRate() {
		return rate;
	}

	public int getMaxTransitTime() {
		return maxTransitTime;
	}

	public double getMaxDraft() {
		return maxDraft;
	}

	public int getDistance() {
		return distance;
	}

	public boolean isSuez() {
		return suez;
	}

	public boolean isPanama() {
		return panama;
	}

	@Override
	public String toString() {
		return "ODpair [demand=" + demand + ", rate=" + rate + ", maxTransitTime=" + maxTransitTime + ", maxDraft="
				+ maxDraft + ", distance=" + distance + ", suez=" + suez + ", panama=" + panama + "]";
	}
}
