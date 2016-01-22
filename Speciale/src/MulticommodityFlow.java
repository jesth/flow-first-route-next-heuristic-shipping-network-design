import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulticommodityFlow {
	private static Graph graph;
	
	public static void initialize(Graph inputGraph){
		graph = inputGraph;
		BellmanFord.initialize(graph);
	}
	
	public static void run(){
		int iteration = 1;
		boolean invalidFlow = true;
		while (invalidFlow){
			System.out.println("Now running BellmanFord in iteration " + iteration);
			System.out.println();
			BellmanFord.run();
			invalidFlow = false;
			for (Edge e : graph.getEdges()){
				if(e.getCapacity() < e.getLoad()){
					System.out.println("Invalid flow on " + e.simplePrint());
					invalidFlow = true;
					int lowestProfit = Integer.MAX_VALUE;
					for(Demand d : e.getShortestPathOD()){
						if(d.getLagrangeProfit() < lowestProfit){
							lowestProfit = d.getLagrangeProfit();
						}
					}
					int wasCost = e.getCost();
					e.addLagrange(lowestProfit, iteration);
					BellmanFord.relaxEdge(e);
					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
					System.out.println();
				}
			}
			System.out.println();
			iteration++;
		}
		System.out.println("Exiting while loop after iteration " + iteration);
	}
	
	public static void saveODSol(String fileName, ArrayList<Demand> demands){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("RotationId;NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Omission"); 
			out.newLine();
			for(Demand d : demands){
				ArrayList<Edge> route = BellmanFord.getRoute(d);
				for(Edge e : route){
					if(e.isSail() || e.isOmission()){
						if(e.isOmission()){
							out.write(";;");
						} else {
							out.write(e.getRotation().getId()+";"+e.getNoInRotation()+";");
						}
						out.write(d.getId()+";");
						out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
						out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
						out.write(d.getDemand()+";");
						if(e.isOmission()){
							out.write("1");
						} else {
							out.write("0");
						}
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
}
