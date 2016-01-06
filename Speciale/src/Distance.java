
public class Distance {
	private Port origin;
	private Port destination;
	private double draft;
	private int distance;
	private boolean suez;
	private boolean panama;
	
	public Distance(){
		
	}

	/**
	 * @param origin
	 * @param destination
	 * @param draft
	 * @param distance
	 * @param suez
	 * @param panama
	 */
	public Distance(Port origin, Port destination, double draft, int distance, boolean suez, boolean panama) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.draft = draft;
		this.distance = distance;
		this.suez = suez;
		this.panama = panama;
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
	 * @return the draft
	 */
	public double getDraft() {
		return draft;
	}

	/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * @return the suez
	 */
	public boolean isSuez() {
		return suez;
	}

	/**
	 * @return the panama
	 */
	public boolean isPanama() {
		return panama;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Distance [origin=" + origin + ", destination=" + destination + ", draft=" + draft + ", distance="
				+ distance + ", suez=" + suez + ", panama=" + panama + "]";
	}
	
	
}
