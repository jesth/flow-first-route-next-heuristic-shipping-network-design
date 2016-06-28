package Data;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class VesselClass implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
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
	private int noAvailable;
	private int noUsed;
	private static AtomicInteger idCounter = new AtomicInteger();
	
	public VesselClass(){
	}

	/**
	 * @param name
	 * @param capacity
	 * @param TCRate
	 * @param draft
	 * @param minSpeed
	 * @param maxSpeed
	 * @param designSpeed
	 * @param fuelConsumptionDesign
	 * @param fuelConsumptionIdle
	 * @param panamaFee
	 * @param suezFee
	 */
	public VesselClass(String name, int capacity, int TCRate, double draft, double minSpeed, double maxSpeed,
			double designSpeed, double fuelConsumptionDesign, double fuelConsumptionIdle, int panamaFee, int suezFee) {
		super();
		this.id = idCounter.getAndIncrement();
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
		this.noAvailable = 0;
		this.noUsed = 0;
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
	
	public double getFuelConsumption(double speed){
		double fuelConsumption = Math.pow(speed/designSpeed, 3) * fuelConsumptionDesign;
//		System.out.println("at speed " + speed + " fuel consumpotion is " + fuelConsumption);
		return fuelConsumption;
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

	public int getNoAvailable() {
		return noAvailable;
	}
//	
//	public int getNetNoAvailable(){
//		return (this.noAvailable - this.noUsed);
//	}
//
	public void setNoAvailable(int noAvailable) {
		this.noAvailable = noAvailable;
	}

//	public int getNoUsed() {
//		return noUsed;
//	}
	
//	public void addNoUsed(int noOfVessels){
//		noUsed += noOfVessels;
//		if(noUsed > noAvailable){
//			throw new RuntimeException("Using more vessels " + noUsed + ">" + noAvailable + " than are available for vessel class " + this);
//		}
//	}
//
//	public void removeNoUsed(int noOfVessels) {
//		noUsed -= noOfVessels;
//		if(noUsed < 0){
//			throw new RuntimeException("Negative number of vessels used for vessel class " + this);
//		}
//	}

	@Override
	public String toString() {
		return "VesselClass [name=" + name + ", noAvailable=" + noAvailable + ", capacity=" + capacity + ", TCRate="
				+ TCRate + ", draft=" + draft + ", minSpeed=" + minSpeed + ", maxSpeed=" + maxSpeed + ", designSpeed="
				+ designSpeed + ", fuelConsumptionDesign=" + fuelConsumptionDesign + ", fuelConsumptionIdle="
				+ fuelConsumptionIdle + ", panamaFee=" + panamaFee + ", suezFee=" + suezFee + "]";
	}

	public int getId() {
		return id;
	}

	
}
