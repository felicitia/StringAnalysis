package graph;

import java.util.ArrayList;
import java.util.List;

public class DOTGraph {
	private String name;
	private List<DOTNode> nodeList = null;
	private List<DOTEdge> edgeList = null;
	public DOTGraph(String name){
		super();
		this.name = name;
		nodeList = new ArrayList<DOTNode>();
		edgeList = new ArrayList<DOTEdge>();
		
	}
	
	public List getPredsOf(String child){
		List preds = new ArrayList<String>();
		for(DOTEdge edge: edgeList){
			if (child.equals(edge.getChild())) {
				if(!preds.contains(edge.getParent())){
					preds.add(edge.getParent());
				}
			}
		}
		return preds;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DOTNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<DOTNode> nodeList) {
		this.nodeList = nodeList;
	}

	public List<DOTEdge> getEdgeList() {
		return edgeList;
	}

	public void setEdgeList(List<DOTEdge> edgeList) {
		this.edgeList = edgeList;
	}

	public void addNode(DOTNode node){
		nodeList.add(node);
	}
	
	public void addEdge(DOTEdge edge){
		if(!edgeList.contains(edge)){
			edgeList.add(edge);
		}
	}
}
