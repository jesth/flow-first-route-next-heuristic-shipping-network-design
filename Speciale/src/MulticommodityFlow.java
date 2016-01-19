import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MulticommodityFlow {
	Graph graph;

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
