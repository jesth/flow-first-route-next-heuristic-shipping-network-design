
public class VesselClass {
	private String name;
	private int capacity;
	private int TCRate;
	private double draft;
	private double minSpeed;
	private double maxSpeed;
	private double designSpeed;
	private double fuelConsumptionDesign;
	private double fuelConsumptionIdle;
	private int panamaFee;
	private int suezFee;
	
	public VesselClass(){
	}

	public VesselClass(String name, int capacity, int TCRate, double draft, double minSpeed, double maxSpeed,
			double designSpeed, double fuelConsumptionDesign, double fuelConsumptionIdle, int panamaFee, int suezFee) {
		super();
		this.name = name;
		this.capacity = capacity;
		this.TCRate = TCRate;
		this.draft = draft;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		this.designSpeed = designSpeed;
		this.fuelConsumptionDesign = fuelConsumptionDesign;
		this.fuelConsumptionIdle = fuelConsumptionIdle;
		this.panamaFee = panamaFee;
		this.suezFee = suezFee;
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getTCRate() {
		return TCRate;
	}

	public double getDraft() {
		return draft;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public double getDesignSpeed() {
		return designSpeed;
	}

	public double getFuelConsumptionDesign() {
		return fuelConsumptionDesign;
	}

	public double getFuelConsumptionIdle() {
		return fuelConsumptionIdle;
	}

	public int getPanamaFee() {
		return panamaFee;
	}

	public int getSuezFee() {
		return suezFee;
	}

	@Override
	public String toString() {
		return "VesselClass [name=" + name + ", capacity=" + capacity + ", TCRate=" + TCRate + ", draft=" + draft
				+ ", minSpeed=" + minSpeed + ", maxSpeed=" + maxSpeed + ", designSpeed=" + designSpeed
				+ ", fuelConsumptionDesign=" + fuelConsumptionDesign + ", fuelConsumptionIdle=" + fuelConsumptionIdle
				+ ", panamaFee=" + panamaFee + ", suezFee=" + suezFee + "]";
	}
	
	
}
