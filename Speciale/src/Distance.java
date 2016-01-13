
public class Distance {
	private Port origin;
	private Port destination;
	private int distanceNone;
	private int distanceSuez;
	private int distancePanama;
	private int distanceSuezPanama;
	//Draft is set to 14, which is not restricting, if no other value is supplied.
	private double draftNone;
	private double draftSuez;
	private double draftPanama;
	private double draftSuezPanama;
//	private boolean suez;
//	private boolean panama;
	
	public Distance(){
		
	}

	/**
	 * @param origin
	 * @param destination
	 */
	public Distance(Port origin, Port destination) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.distanceNone = Integer.MAX_VALUE;
		this.distanceSuez = Integer.MAX_VALUE;
		this.distancePanama = Integer.MAX_VALUE;
		this.distanceSuezPanama = Integer.MAX_VALUE;
		this.draftNone = 14;
		this.draftSuez = 14;
		this.draftPanama = 14;
		this.draftSuezPanama = 14;
//		if(draft != -1){
//			this.draft = draft;
//		}
//		this.suez = suez;
//		this.panama = panama;
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
	 * @return the distanceNone
	 */
	public int getDistanceNone() {
		return distanceNone;
	}

	/**
	 * @return the distanceSuez
	 */
	public int getDistanceSuez() {
		return distanceSuez;
	}

	/**
	 * @return the distancePanama
	 */
	public int getDistancePanama() {
		return distancePanama;
	}

	/**
	 * @return the distanceSuezPanama
	 */
	public int getDistanceSuezPanama() {
		return distanceSuezPanama;
	}

	/**
	 * @return the draftNone
	 */
	public double getDraftNone() {
		return draftNone;
	}

	/**
	 * @return the draftSuez
	 */
	public double getDraftSuez() {
		return draftSuez;
	}

	/**
	 * @return the draftPanama
	 */
	public double getDraftPanama() {
		return draftPanama;
	}

	/**
	 * @return the draftSuezPanama
	 */
	public double getDraftSuezPanama() {
		return draftSuezPanama;
	}

	public void setNone(int distance, double draft){
		this.distanceNone = distance;
		this.draftNone = draft;
	}
	
	public void setSuez(int distance, double draft){
		this.distanceSuez = distance;
		this.draftSuez = draft;
	}
	
	public void setPanama(int distance, double draft){
		this.distancePanama = distance;
		this.draftPanama = draft;
	}
	
	public void setSuezPanama(int distance, double draft){
		this.distanceSuezPanama = distance;
		this.draftSuezPanama = draft;
	}
	
	public int getDistance(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distanceNone;
		} else if(suez == true && panama == false){
			return distanceSuez;
		} else if(suez == false && panama == true){
			return distancePanama;
		} else {
			return distanceSuezPanama;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Distance [origin=" + origin + ", destination=" + destination + ", distanceNone=" + distanceNone
				+ ", distanceSuez=" + distanceSuez + ", distancePanama=" + distancePanama + ", distanceSuezPanama="
				+ distanceSuezPanama + ", draftNone=" + draftNone + ", draftSuez=" + draftSuez + ", draftPanama="
				+ draftPanama + ", draftSuezPanama=" + draftSuezPanama + "]";
	}




	
	
}
