
public class Distance {
	private Port origin;
	private Port destination;
	private DistanceElement[] distances;
//	private int distanceNone;
//	private int distanceSuez;
//	private int distancePanama;
//	private int distanceSuezPanama;
	//Draft is set to 14, which is not restricting, if no other value is supplied.
//	private double draftNone;
//	private double draftSuez;
//	private double draftPanama;
//	private double draftSuezPanama;
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
		this.distances = new DistanceElement[4];
		this.distances[0] = new DistanceElement(this, false, false);
		this.distances[1] = new DistanceElement(this, true, false);
		this.distances[2] = new DistanceElement(this, false, true);
		this.distances[3] = new DistanceElement(this, true, true);
//		this.distanceNone = Integer.MAX_VALUE;
//		this.distanceSuez = Integer.MAX_VALUE;
//		this.distancePanama = Integer.MAX_VALUE;
//		this.distanceSuezPanama = Integer.MAX_VALUE;
//		this.draftNone = -1;
//		this.draftSuez = -1;
//		this.draftPanama = -1;
//		this.draftSuezPanama = -1;
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

	public void setDistanceDraft(int distance, double draft, boolean suez, boolean panama){
		if(suez == false && panama == false){
			distances[0].setDistance(distance);
			distances[0].setDraft(draft);
		} else if(suez == true && panama == false){
			distances[1].setDistance(distance);
			distances[1].setDraft(draft);
		} else if(suez == false && panama == true){
			distances[2].setDistance(distance);
			distances[2].setDraft(draft);
		} else {
			distances[3].setDistance(distance);
			distances[3].setDraft(draft);
		}
	}
	
	public double getDraft(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distances[0].getDraft();
		} else if(suez == true && panama == false){
			return distances[1].getDraft();
		} else if(suez == false && panama == true){
			return distances[2].getDraft();
		} else {
			return distances[3].getDraft();
		}
	}
	
	public int getDistance(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distances[0].getDistance();
		} else if(suez == true && panama == false){
			return distances[1].getDistance();
		} else if(suez == false && panama == true){
			return distances[2].getDistance();
		} else {
			return distances[3].getDistance();
		}
	}
}
