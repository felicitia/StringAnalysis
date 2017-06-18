package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import graph.DOTEdge;
import graph.DOTGraph;
import graph.DOTNode;

public class DOTUtil {

	public static void DOT2File(DOTGraph graph, String output){
		PrintWriter writer = null;
		List<DOTNode> nodeList = graph.getNodeList();
		List<DOTEdge> edgeList = graph.getEdgeList();
		
		try {
			File outputFile = new File(output);
			writer = new PrintWriter(outputFile, "UTF-8");
			writer.println("digraph "+graph.getName()+"{");
			//print nodes
			for(DOTNode node: nodeList){
				//assumption: no such a case that both shape and name == NULL
				if(node.getName()==null){
					writer.println("node [shape = "+node.getShape()+"];");
				}else if(node.getShape()==null){
					System.out.println("only node shape == null, don't know the format...");
				}else{
					writer.println("node [shape = "+node.getShape()+"]; "+node.getName()+";");
				}
			}
			//print edges
			for(DOTEdge edge: edgeList){
				if(edge.getLabel()==null){
					writer.println(edge.getParent()+" -> "+edge.getChild()+";");
				}else{
					writer.println(edge.getParent()+" -> "+edge.getChild()+" [label = \""+edge.getLabel()+"\"];");
				}
			}
			writer.println("}");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
