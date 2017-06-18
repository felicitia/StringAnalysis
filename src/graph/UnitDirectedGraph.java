package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

public class UnitDirectedGraph implements DirectedGraph<Unit> {

	Map<Unit, HashSet<Unit>> edgeMap = null;
	
	public UnitDirectedGraph(){
		edgeMap = new HashMap<Unit, HashSet<Unit>>();
	}
	
	public void addEdge(Unit parent, Unit child){
		if(edgeMap.containsKey(parent)){
			edgeMap.get(parent).add(child);
		}else{
			HashSet<Unit> children = new HashSet<Unit>();
			children.add(child);
			edgeMap.put(parent, children);
		}
	}
	
	@Override
	public List<Unit> getHeads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Unit> getPredsOf(Unit unit) {
		// TODO Auto-generated method stub
		List<Unit> preds = new ArrayList<Unit>();
		Iterator it = edgeMap.entrySet().iterator();
		while(it.hasNext()){
			Entry pair = (Entry) it.next();
			HashSet<Unit> valueSet = (HashSet<Unit>) pair.getValue();
			if(valueSet.contains(unit) && !preds.contains(pair.getKey())){
				preds.add((Unit) pair.getKey());
			}
		}
		return preds;
	}

	@Override
	public List<Unit> getSuccsOf(Unit unit) {
		// TODO Auto-generated method stub
		if(edgeMap.containsKey(unit)){
			List<Unit> succs = new ArrayList<Unit>(edgeMap.get(unit));
			return succs;
		}
		return null;
	}

	@Override
	public List<Unit> getTails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Unit> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
