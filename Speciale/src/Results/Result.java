package Results;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import Data.Demand;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Sortables.SortableAuxEdge;

public class Result {
	private Graph graph;
	private ArrayList<Rotation> rotations;

	public Result(Graph inputGraph){
		graph = inputGraph;
		rotations = new ArrayList<Rotation>();
	}

	public void addRotation(Rotation r){
		rotations.add(r);
	}

	public void removeRotation(Rotation r){
		rotations.remove(r);
	}

	/**
	 * @return the rotations
	 */
	public ArrayList<Rotation> getRotations() {
		return rotations;
	}

	public int getObjective(){
		int obj = 0;
		obj = getFlowProfit(false);
		for(Rotation r : rotations){
			if(r.isActive()){
				obj -= r.calcCost();
			}
		}

		return obj;
	}

	public int getFlowProfit(boolean repair){
		int flowProfit = 0;
		int omissionCost = 0;
		int flowCost = 0;
		for (Edge e : graph.getEdges().values()){
			if(e.isActive()){
				if(repair){
					if(e.isOmission()){
						omissionCost += 1000 * e.getRepLoad();
					} else {
						flowCost += e.getRealCost() * e.getRepLoad();
					}
				} else {
					if(e.isOmission()){
						omissionCost += 1000 * e.getLoad();
					} else {
						flowCost += e.getRealCost() * e.getLoad();
					}
				}
			}
		}
		int flowRevenue = 0;
		for (Demand d : graph.getDemands()){
			for(Route r : d.getRoutes()){
				if(r.getRoute().size() > 1 && repair){
					flowRevenue += r.getFFErep() * d.getRate();
				} else if(r.getRoute().size() > 1 && !repair){
					flowRevenue += r.getFFE() * d.getRate();
				}
			}
		}
		//		System.out.println("flowRevenue " + flowRevenue + ". flowCost " + flowCost + ". omissionCost " + omissionCost);
		flowProfit = flowRevenue - flowCost - omissionCost;

		return flowProfit;
	}

	public Demand getLargestODLoss(){
		Demand OD = null;
		int largestODLoss = Integer.MAX_VALUE;
		for(Demand d : graph.getDemands()){
			int odLoss = 0;
			for(Route r : d.getRoutes()){
				odLoss += r.getRealProfit() *  r.getFFE();
			}
			//			System.out.println("From " + d.getOrigin().getUNLocode() + " to " + d.getDestination().getUNLocode() + " loss of profit = " + odLoss);
			if(odLoss < largestODLoss){
				largestODLoss = odLoss;
				OD = d;
			}
		}

		return OD;
	}
	
	public Rotation getRotation(int id){
		for(Rotation r : rotations){
			if(r.getId() == id){
				return r;
			}
		}
		return null;
	}

	public void copyRotations(Result copyResult, Graph newGraph) {
		for(Rotation r : copyResult.getRotations()){
			new Rotation(r, newGraph);
		}
	}
	
	public void saveRotationCost(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;VesselSize;Distance;NoVessels;TotalCost;PortCallCost;SailingFuelCost;IdleFuelCost;CanalCost;TCCost"); 
			out.newLine();
				for(Rotation r : rotations){
					out.write(r.getId() + ";" + r.getVesselClass().getCapacity() + ";" + r.getDistance() + ";" + r.getNoOfVessels() + ";" + r.calcCost() + ";");
					out.write(r.getPortCallCost() + ";" + r.getSailingBunkerCost() + ";" + r.getIdleBunkerCost() + ";");
					out.write(r.getCanalCost() + ";" + r.getTCCost());
					out.newLine();
			}
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveFlowCost(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("ODId;ODFrom;ODTo;Omission;#FFE;TotalRev;TotalCost"); 
			out.newLine();
			for(Demand d : graph.getDemands()){
				for(Route r : d.getRoutes()){
					int FFE = r.getFFE();
					out.write(d.getId() + ";" + d.getOrigin().getUNLocode() + ";" + d.getDestination().getUNLocode() + ";"); 
					if(r.isOmission()){
						out.write(1 + ";");
					} else {
						out.write(0 + ";");
					}
					int revenue = d.getRate() * FFE;
					int cost = r.getCost() * FFE;
					out.write(FFE + ";" + revenue + ";" + cost);
					out.newLine();
				}
			}
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveDemands(String fileName){
		ArrayList<Demand> demands = graph.getDemands();
		File fileOut = new File(fileName);
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(fileOut));

			out.write("ODId;ODFrom;ODTo;#FFE"); 
			out.newLine();
			for(Demand d : demands){
				out.write(d.getId() + ";" + d.getOrigin().getUNLocode() + ";" + 
						d.getDestination().getUNLocode() + ";" + d.getDemand());
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Saves the routes of all demand pairs in csv-format for easy handling in Excel.
	 * @param fileName - the complete path or name of the file to be saved. End on .csv.
	 * @param demands - the list of demands to be saved.
	 */
	public void saveODSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Omission;Feeder"); 
			out.newLine();
			for(Demand d : graph.getDemands()){
				for(Route r : d.getRoutes()){
					for(Edge e : r.getRoute()){
						if(e.isSail() || e.isOmission() || e.isFeeder()){
							if(e.isOmission() || e.isFeeder()){
								out.write(";;");
							} else {
								out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
							}
							out.write(d.getId()+";");
							out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
							out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
							out.write(r.getFFE()+";");
							if(e.isOmission()){
								out.write("1;0");
							} else if(e.isFeeder()){
								out.write("0;1");
							} else {
								out.write("0;0");
							}
							out.newLine();
						}
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveRotationSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;LegFrom;LegTo;#FFE;LoadingFrom;UnloadingTo"); 
			out.newLine();
			for(Rotation r : rotations){
				for(Edge e : r.getRotationEdges()){
					if(e.isSail() && e.isActive()){
						out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
						out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
						out.write(e.getLoad()+";");
						int loading = 0;
						for(Edge l : e.getFromNode().getIngoingEdges()){
							if(!l.isDwell()){
								loading += l.getLoad();
							}
						}
						int unloading = 0;
						for(Edge l : e.getToNode().getOutgoingEdges()){
							if(!l.isDwell()){
								unloading += l.getLoad();
							}
						}
						out.write(loading+";"+unloading);
						out.newLine();
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveTransferSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("Port;RotationFrom;RotationTo;ODId;ODFrom;ODTo;#FFE");
			out.newLine();
			for(Edge e : graph.getEdges().values()){
				if(e.isTransshipment()){
					for(Route r : e.getRoutes()){
						Demand d = r.getDemand();
						out.write(e.getFromPortUNLo() + ";");
						out.write(e.getPrevEdge().getRotation().getId() + ";" + e.getNextEdge().getRotation().getId() + ";");
						out.write(d.getId() + ";" + d.getOrigin().getUNLocode() + ";" + d.getDestination().getUNLocode() + ";");
						out.write(r.getFFE());
						out.newLine();
					}
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAllEdgesSol(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("Id;FromNodeId;ToNodeId;PortFrom;PortTo;Cost;Capacity;Load;TravelTimeRounded;Omission;Load;Unload;Transshipment;Sail/Dwell;Feeder;RotationIdFrom;RotationIdTo;NoInRotationFrom;NoInRotationTo");
			out.newLine();
			for(Edge e : graph.getEdges().values()){
				if(e.isActive()){
					out.write(e.getId() + ";" + e.getFromNode().getId() + ";" + e.getToNode().getId() + ";");
					out.write(e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";");
					out.write(e.getCost() + ";" + e.getCapacity() + ";" + e.getLoad() + ";" + (int) e.getTravelTime() + ";");
					if(e.isOmission()){
						out.write("1;0;0;0;0;0;;;;");
					} else if(e.isLoadUnload()){
						if(e.getFromNode().isFromCentroid()){
							out.write("0;1;0;0;0;0;;" + e.getToNode().getNextEdge().getRotation().getId() + ";;" + e.getToNode().getNextEdge().getNoInRotation());
						} else {
							out.write("0;0;1;0;0;0;" + e.getFromNode().getPrevEdge().getRotation().getId() + ";;" + e.getFromNode().getPrevEdge().getNoInRotation() + ";");
						}
					} else if(e.isTransshipment()){
						out.write("0;0;0;1;0;0;" + e.getFromNode().getPrevEdge().getRotation().getId() + ";" + e.getToNode().getNextEdge().getRotation().getId() + 
								";" + e.getFromNode().getPrevEdge().getNoInRotation() + ";" + e.getToNode().getNextEdge().getNoInRotation());
					} else if(e.isSail() || e.isDwell()){
						out.write("0;0;0;0;1;0;" + e.getRotation().getId() + ";" + e.getRotation().getId() + ";" + e.getNoInRotation() + ";" + e.getNoInRotation());
					} else if(e.isFeeder()) {
						if(e.getFromNode().isArrival() && e.getToNode().isDeparture()){
							out.write("0;0;0;0;0;1;" + + e.getFromNode().getPrevEdge().getRotation().getId() + ";" + e.getToNode().getNextEdge().getRotation().getId() +
									";" + e.getFromNode().getPrevEdge().getNoInRotation() + ";" + e.getToNode().getNextEdge().getNoInRotation());
						} else if(e.getFromNode().isArrival()){
							out.write("0;0;0;0;0;1;" + + e.getFromNode().getPrevEdge().getRotation().getId() + ";;" + e.getFromNode().getPrevEdge().getNoInRotation() + ";");
						} else{
							out.write("0;0;0;0;0;1;;" + e.getToNode().getNextEdge().getRotation().getId() + ";;" + e.getToNode().getNextEdge().getNoInRotation());
						}
					} else {
						throw new RuntimeException("Edge does not fit any description.");
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveLagranges(String fileName, int iterations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			String str = "EdgeId;From;To;Capacity";
			for(int i=0; i<iterations; i++){
				str += ";" + i;
			}
			out.write(str); 
			out.newLine();
			for(Edge e : graph.getEdges().values()){
				if(e.isSail()){
					out.write(e.getId() + ";" + e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";" + e.getCapacity());
					for(int i : e.getLagrangeValues()){
						out.write(";" + i);
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveLoads(String fileName, int iterations){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			String str = "EdgeId;From;To;Capacity";
			for(int i=0; i<iterations; i++){
				str += ";" + i;
			}
			out.write(str); 
			out.newLine();
			for(Edge e : graph.getEdges().values()){
				if(e.isSail()){
					out.write(e.getId() + ";" + e.getFromPortUNLo() + ";" + e.getToPortUNLo() + ";" + e.getCapacity());
					for(int i : e.getLoadValues()){
						out.write(";" + i);
					}
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BufferedWriter openProgressWriter(String fileName){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter(fileOut));
			String str = "Time;objective";
			out.write(str); 
			out.newLine();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void saveProgress(BufferedWriter out, long currentTime, int objective){
		try {
		String str = currentTime + ";" + objective;
		out.write(str); 
		out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void saveOPLData(String fileName){
		try {
			graph.updateNodeSequenceIds();
			int noOfNodes = graph.getNodes().size();
			int noOfDemands = graph.getDemands().size();
			int[][] capacity = new int[noOfNodes][noOfNodes];
			int[][] cost = new int[noOfNodes][noOfNodes];
			int[] demandFrom = new int[noOfDemands];
			int[] demandTo = new int[noOfDemands];
			int[] demand = new int[noOfDemands];

			for(Edge e : graph.getEdges().values()){
				int fromNode = e.getFromNode().getSequenceId();
				int toNode = e.getToNode().getSequenceId();
				capacity[fromNode][toNode] = e.getCapacity();
				cost[fromNode][toNode] = e.getRealCost();
			}
			for(int i = 0; i < noOfDemands; i++){
				Demand d = graph.getDemands().get(i);
				demandFrom[i] = d.getOrigin().getFromCentroidNode().getSequenceId();
				demandTo[i] = d.getDestination().getToCentroidNode().getSequenceId();
				demand[i] = d.getDemand();

			}
			File fileOut = new File(fileName);
			File fileOutLegend = new File("legendOPLdata.csv");
			BufferedWriter out;
			BufferedWriter outLegend;

			out = new BufferedWriter(new FileWriter(fileOut));
			outLegend = new BufferedWriter(new FileWriter(fileOutLegend));

			outLegend.write("NodeSequenceId;Port;RotationId;Centroid");
			for(Node i : graph.getNodes().values()){
				outLegend.newLine();
				outLegend.write(i.getSequenceId()+";"+i.getPort().getUNLocode()+";");
				if(i.isFromCentroid() || i.isToCentroid()){
					outLegend.write("-1;1");
				} else {
					outLegend.write(i.getRotation().getId()+";0");
				}
			}
			outLegend.close();

			out.write("n = " + noOfNodes + ";");
			out.newLine();
			out.write("d = " + noOfDemands + ";");
			out.newLine();
			out.newLine();

			out.write("u = [");
			writeDouble(out, capacity, noOfNodes);
			out.newLine();
			out.newLine();

			out.write("c = [");
			writeDouble(out, cost, noOfNodes);
			out.newLine();
			out.newLine();

			out.write("dFrom = [");
			writeSingle(out, demandFrom, noOfDemands);
			out.newLine();
			out.newLine();

			out.write("dTo = [");
			writeSingle(out, demandTo, noOfDemands);
			out.newLine();
			out.newLine();

			out.write("D = [");
			writeSingle(out, demand, noOfDemands);
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	

	private void writeSingle(BufferedWriter out, int[] array, int number) throws IOException{
		for(int i = 0; i < number; i++){
			out.write(array[i] + " ");				
		}
		out.write("];");
	}

	private void writeDouble(BufferedWriter out, int[][] array, int number) throws IOException{
		for(int i = 0; i < number; i++){
			out.write("[");
			for(int j = 0; j < number; j++){
				out.write(array[i][j] + " ");				
			}
			out.write("]");
			if(i < number-1){
				out.newLine();
			}
		}
		out.write("];");
	}

	public int getHighestRotationId() {
		int highestId = 0;
		for(Rotation r : rotations){
			if(r.getId() > highestId){
				highestId = r.getId();
			}
		}
		return highestId;
	}

}
