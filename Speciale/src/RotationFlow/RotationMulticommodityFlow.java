package RotationFlow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Data.Demand;
import Graph.Edge;
import Results.Route;

public class RotationMulticommodityFlow {
	private RotationBellmanFord bellmanFord;
	private RotationGraph graph;

	public RotationMulticommodityFlow(RotationGraph graph){
		this.graph = graph;
		this.bellmanFord = new RotationBellmanFord(graph);
	}

	public void run(){
		reset();
		bellmanFord.run();
		boolean validFlow = false;
		while(!validFlow){
			bellmanFord.runRep();
			validFlow = true;
			for(RotationEdge e : graph.getRotationEdges()){
				int overflow = e.getOverflow();
				if(overflow > 0){
					validFlow = false;
					e.removeLoad(bellmanFord, overflow);
				}
			}
		}
	}

	public void reset(){
		for(RotationEdge e : graph.getRotationEdges()){
			e.clearServicedRoutes();
		}
		for(RotationDemand d : graph.getRotationDemands()){
//						d.resetTransportedDemand();
			d.clearRoutes();
		}
	}

	public void addUnprocessedNode(RotationNode node){
		bellmanFord.addUnprocessedNode(node);
	}

	public void addUnprocessedNodeRep(RotationNode node){
		bellmanFord.addUnprocessedNodeRep(node);
	}

	public void saveODSol(String fileName, ArrayList<RotationDemand> demands){
		try {
			File fileOut = new File(fileName);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileOut));
			out.write("NoInRotation;ODId;ODFrom;ODTo;LegFrom;LegTo;#FFE;Feeder;Omission"); 
			out.newLine();
			for(RotationDemand d : demands){
				for(RotationRoute r : d.getRoutes()){
					for(RotationEdge e : r.getRoute()){
						if(e.isOmission()){
							out.write(";");
						} else {
							out.write(e.getNoInRotation()+";");
						}
						out.write(d.getOrgDemand().getId()+";");
						out.write(d.getOrigin().getUNLocode()+";"+d.getDestination().getUNLocode()+";");
						out.write(e.getFromPortUNLo()+";"+e.getToPortUNLo()+";");
						out.write(r.getFFE()+";");
						if(e.isFeeder()){
							out.write("1;");
						} else {
							out.write("0;");
						}
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
