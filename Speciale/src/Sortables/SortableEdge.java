package Sortables;

import Graph.Edge;

public class SortableEdge implements Comparable<SortableEdge>{
		private int load;
		private Edge edge;
		
		public SortableEdge(int load, Edge edge){
			this.load = load;
			this.edge = edge;
		}
		
		public Edge getEdge(){
			return edge;
		}
		
		public double getLoad(){
			return load;
		}
		
		@Override
		public int compareTo(SortableEdge o) {
			return o.load-load;
		}
}
