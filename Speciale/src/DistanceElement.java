
public class DistanceElement {
	private Distance parent;
	private int distance;
	private double draft;
	private boolean suez;
	private boolean panama;
	
	public DistanceElement(Distance parent, boolean suez, boolean panama) {
		super();
		this.distance = Integer.MAX_VALUE;
		this.draft = -1;
		this.parent = parent;
		this.suez = suez;
		this.panama = panama;
	}
	
	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setDraft(double draft) {
		this.draft = draft;
	}

	public void setSuez(boolean suez) {
		this.suez = suez;
	}

	public void setPanama(boolean panama) {
		this.panama = panama;
	}

	public Distance getParent(){
		return parent;
	}
	
	public int getDistance(){
		return distance;
	}
	
	public double getDraft(){
		return draft;
	}
	
	public boolean isSuez(){
		return suez;
	}
	
	public boolean isPanama(){
		return panama;
	}
	
	public Port getOrigin(){
		return parent.getOrigin();
	}
	
	public Port getDestination(){
		return parent.getDestination();
	}
}
