import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Data {
	private HashMap<String, Port> ports;
	private ArrayList<Distance> distances;
	private ArrayList<Demand> demands;
	private ArrayList<VesselClass> vesselClasses;
	
	public Data(String demandFileName, String vesselNoFileName) throws FileNotFoundException{
		ports = ReadData.readPorts();
		distances = ReadData.readDistances(ports);
		demands = ReadData.readDemands(demandFileName, ports);
		vesselClasses = ReadData.readVesselClass(vesselNoFileName);
	}
	
	public HashMap<String, Port> getPorts() {
		return ports;
	}

	public ArrayList<Distance> getDistances() {
		return distances;
	}

	public ArrayList<Demand> getDemands() {
		return demands;
	}

	public ArrayList<VesselClass> getVesselClasses() {
		return vesselClasses;
	}
}
