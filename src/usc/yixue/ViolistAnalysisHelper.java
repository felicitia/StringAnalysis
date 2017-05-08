package usc.yixue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.JSONUtil;

public class ViolistAnalysisHelper {

	static Set<String> targetMethods = null;

	static List<TargetStmt> targetStmtList = null;
	
	static Set<String> triggerMethods = null;
	
	static Map<String, String> targetMethodIdMap = null;
	
	static final String METHOD_NODEID_SEP = "#"; 
	
//	static  String appfolder = null; //arg0
//	static  String pkgname = null; //arg1
//	static  String apkname = null; //arg2

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		appfolder = args[0];
//		pkgname = args[1];
//		apkname = args[2];
		
//		outputRequestMap(getTargetStmtList(""), "");
//		System.out.println(getTriggerMethods());
	}
	
	public static Map<String, String> getTargetMethodIdMap(String appfolder){
		if(targetMethodIdMap == null){
			targetMethodIdMap = new HashMap<String, String>();
			for(TargetStmt stmt: getTargetStmtList(appfolder)){
				if(targetMethodIdMap.containsKey(stmt.methodName)){
					//already have a nodeId with the same method, then append the current nodeId
					StringBuilder value = new StringBuilder(targetMethodIdMap.get(stmt.methodName));
					value.append(METHOD_NODEID_SEP);
					value.append(stmt.nodeId);
					targetMethodIdMap.put(stmt.methodName, value.toString());
				}else{
					targetMethodIdMap.put(stmt.methodName, ""+stmt.nodeId);
				}
			}
		}
		return targetMethodIdMap;
	}

	public static Set<String> getTriggerMethods(String appfolder){
		if(triggerMethods == null){
			triggerMethods = new HashSet<String>();
			JSONParser parser = new JSONParser();
           
			try {
				JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(appfolder+"/ccfg.json"));
				for(Object key: jsonObject.keySet()){
					String keyStr = key.toString();
					JSONArray valueArray = (JSONArray)jsonObject.get(keyStr);
					StringBuilder triggerMethod = new StringBuilder(keyStr);
					for(int i=0; i<valueArray.size(); i++){
						//value example: "<edu.usc.yixue.weatherapp.MainActivity$1: void onClick(android.view.View)>#303#299#307"
						String value = (String) valueArray.get(i);
						String[] tokens = value.split(METHOD_NODEID_SEP);
						for(int j=1; j<tokens.length; j++){
							triggerMethod.append(METHOD_NODEID_SEP+tokens[j]);
						}
						triggerMethods.add(triggerMethod.toString());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
		}
		return triggerMethods;
	}
	
	public static List<TargetStmt> getTargetStmtList(String appfolder) {
		if (targetStmtList == null) {
			targetStmtList = new ArrayList<TargetStmt>();
			try (BufferedReader br = new BufferedReader(new FileReader(
					appfolder + "/statistic.txt"))) {
				String line;
				TargetStmt target = new TargetStmt();
				while ((line = br.readLine()) != null) {
					if(line.equals("")){
						targetStmtList.add(target);
						target = new TargetStmt();
					}
					if (line.startsWith("Method Name: ")) {
						target.methodName = line.replace("Method Name: ", "");
					}
					if (line.startsWith("Source Line Number: ")) {
						target.srcLine = Integer.parseInt(line.replace("Source Line Number: ",
								""));
					}
					if(line.startsWith("Bytecode Offset: ")){
						target.byteOffset = Integer.parseInt(line.replace("Bytecode Offset: ", ""));
					}
					if(line.startsWith("Jimple: ")){
						target.jimple = line.replace("Jimple: ", "");
					}
					if(line.startsWith("Node ID: ")){
						target.nodeId = Integer.parseInt(line.replace("Node ID: ", ""));
					}
					if(line.startsWith("Value: ")){
						target.value = line.replace("Value: ", "");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
		return targetStmtList;
	}

	//no nodeId information
	public static Set<String> getTargetMethodSet(String appfolder) {
		if (targetMethods == null) {
			targetMethods = new HashSet<String>();
			try (BufferedReader br = new BufferedReader(new FileReader(
					appfolder + "/statistic.txt"))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("Method Name: ")) {
						targetMethods.add(line.replace("Method Name: ", ""));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return targetMethods;
	}
	
	private static JSONObject createStmtJSON(String value){
		JSONObject stmtJSON = new JSONObject();
		if(value.contains("Unknown")){
			String[] substrs = value.split("Unknown");
			JSONArray substrArray = new JSONArray();
			if(substrs.length == 0){ //the whole thing is [Unknown]
				substrArray.add("null");
				stmtJSON.put("unknownCount", 1);
				stmtJSON.put("subStrings", substrArray);
			}else{
				for(String substr: substrs){
					substrArray.add(substr);
					substrArray.add("null");
				}
				substrArray.remove(substrArray.size()-1);
				stmtJSON.put("unknownCount", substrs.length-1);
				stmtJSON.put("subStrings", substrArray);
			}
		}else{
			//constant string
			stmtJSON.put("unknownCount", 0);
			JSONArray substrArray = new JSONArray();
			substrArray.add(value);
			stmtJSON.put("subStrings", substrArray);
		}
		return stmtJSON;
	}
	
	public static void outputRequestMap(List<TargetStmt> targetList, String outputPath){
		JSONObject result = new JSONObject();
		for(TargetStmt stmt: targetList){
			String value = stmt.value;
			JSONObject stmtJSON = new JSONObject();
			value = value.substring(1, value.length()-1);
			String[] values = value.split(", ");
			//values == null means  <=1 possible value
			if(values == null){
				//only one value
				stmtJSON = createStmtJSON(value);
			}else{
				// there are multiple possible values, only choose the "not-null" one
				//assume there's only one possible "not-null" value for now
				//otherwise, there will be duplicated keys...
				for(String possibleValue: values){
					if(!possibleValue.equals("null")){
						stmtJSON = createStmtJSON(possibleValue);
					}
				}
			}
			result.put(stmt.nodeId, stmtJSON);
		}
		System.out.println("json result is: "+result.toJSONString());
		JSONUtil.writeJSON2File(result, outputPath);
		System.out.println("done writting to file! :)");
	}
}
