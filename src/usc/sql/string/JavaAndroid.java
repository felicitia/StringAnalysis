package usc.sql.string;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import soot.ResolutionFailedException;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.ImmediateBox;
import usc.sql.ir.ConstantString;
import usc.sql.ir.Expression;
import usc.sql.ir.ExternalPara;
import usc.sql.ir.InternalVar;
import usc.sql.ir.T;
import usc.sql.ir.Variable;
import usc.yixue.ViolistAnalysisHelper;
import util.DOTUtil;
import CallGraph.NewNode;
import CallGraph.StringCallGraph;
import SootEvironment.AndroidApp;
import SootEvironment.JavaApp;
import edu.usc.sql.graphs.Node;
import edu.usc.sql.graphs.NodeInterface;
import edu.usc.sql.graphs.cfg.CFGInterface;
import graph.DOTEdge;
import graph.DOTGraph;

public class JavaAndroid {
	
	private Map<String,List<Integer>> targetSignature;
	private int maxloop;
	AndroidApp App = null;
	Set<String> targetscanList = new HashSet<String>();

	public JavaAndroid(String rtjar,String appfolder,String classlist,String apk,Map<String,List<Integer>> targetSignature, int maxloop)
	{		
		this.targetSignature = targetSignature;
		this.maxloop = maxloop;
		targetscanList.add("<java.net.URL: void <init>(java.lang.String)>");
		InterpretCheckerAndroid(rtjar,appfolder+apk,appfolder+classlist,
					appfolder+"/MethodSummary/",appfolder+"/Output/", appfolder);
		
	}
	public JavaAndroid(String rtjar,String appFolder,String classlist,Map<String,List<Integer>> targetSignature, int maxloop)
	{
		this.targetSignature = targetSignature;
		this.maxloop = maxloop;
		targetscanList.add("<java.net.URL: void <init>(java.lang.String)>");
		InterpretCheckerJava(rtjar,appFolder,appFolder+classlist,
				appFolder+"/MethodSummary/",appFolder+"/Output/", appFolder);
	}


	private void InterpretCheckerAndroid(String rtjar,String apkpath,String classlistPath,String summaryFolder,String wfolder, String appFolder)
	{
		/*
		Set<String> potentialAPI = new HashSet<>();
		potentialAPI.add("<android.database.sqlite.SQLiteDatabase: android.database.sqlite.SQLiteStatement compileStatement(java.lang.String)>");
		potentialAPI.add("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String)>");
		potentialAPI.add("<android.database.sqlite.SQLiteDatabase: void execSQL(java.lang.String,java.lang.Object[])>");
		potentialAPI.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[])>");
		potentialAPI.add("<android.database.sqlite.SQLiteDatabase: android.database.Cursor rawQuery(java.lang.String,java.lang.String[],android.os.CancellationSignal)>");
	    */

		
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"
		App=new AndroidApp(rtjar,apkpath,classlistPath, appFolder);
		
		ViolistAnalysisHelper.outputRequestMap(ViolistAnalysisHelper.getTargetStmtList(appFolder), appFolder+"/"+App.getPkgname()+".json");
		
		Map<String,Map<String,Set<Variable>>> targetMap = new HashMap<>();
    	Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
    	Map<String,Set<String>> fieldMap = new HashMap<>();
		Map<String,Translator> tMap = new HashMap<>();
		long totalTranslate = 0,totalInterpret = 0;
		
		

		 File sFolder = new File(summaryFolder);
		 File wFolder = new File(wfolder);
		 // if the directory does not exist, create it
		 if (!sFolder.exists()) {
		     System.out.println("creating directory: " + sFolder);
		     boolean result = false;
		     try{
		    	 sFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"MethodSummary\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }
		 else
		 {
			 final File[] files = sFolder.listFiles();
			 if(files!=null) { //some JVMs return null for empty dirs
			        for(File f: files) {
			                f.delete();
			        }
			    }
		 }
		 if (!wFolder.exists()) {
		     System.out.println("creating directory: " + wFolder);
		     boolean result = false;
		     try{
		    	 wFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"Output\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }
		 else
		 {
			 final File[] files = wFolder.listFiles();
			 if(files!=null) { //some JVMs return null for empty dirs
			        for(File f: files) {
			                f.delete();
			        }
			    }
		 }

		 
		 System.out.println("Target Signatures and parameters: "+targetSignature);
		
		long t1,t2;
		Set<String> targetSigSet = identifyRelevant(targetscanList);
		//for(CFGInterface cfg:App.getCallgraph().getPartialRTOInterface(potentialAPI))
		
		//toDot() is written here
		String dot = App.getCallgraph().toDot();
		File outputFile = new File(wfolder+"callgraph.dot");
		PrintWriter writer;
		try {
			writer = new PrintWriter(outputFile, "UTF-8");
			writer.println(dot);
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//print Target Callgraph
		StringCallGraph strCg = App.getCallgraph();
        DOTGraph cgDot = new DOTGraph("target_call_graph");
		printTargetCallGraph(strCg, cgDot, appFolder);
//		printTargetMethods(cgDot);
		
    	for(CFGInterface cfg:App.getCallgraph().getRTOInterface())
    		
    	{
//    		System.out.println("cfg:   "+cfg.getSignature());
    		if(!targetSigSet.contains(cfg.getSignature())){
    			continue;
    		}
    		String signature=cfg.getSignature();
    		//if(!signature.startsWith("<net.aljazeera.util.MisLog$RequestSender: java.lang.Void doInBackground(java.lang.String[])>"))   		
        	//	continue;
    		
    		//System.out.println(cfg.toDot());
    		
    		if(signature.contains("<org.jsoup.nodes.Node: java.lang.String attr(java.lang.String)>")||signature.contains("<org.jsoup.nodes.Node: java.lang.String absUrl(java.lang.String)>"))
    			continue;
    		//System.out.println(signature);
    		
    		if(signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    		continue;
    		
    		//field																	def missing						

    		
 
    		//if(cfg.getAllNodes().size()>3000)
    			//continue;
      		
    		
    		//for(int i=1;i<=loopCount;i++)
    		//{  		
    		
    		
    		t1 = System.currentTimeMillis();
    		
    		LayerRegion lll = new LayerRegion(null);
    		
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		//System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder,targetSignature);
    	
    		tMap.put(signature, t);
    		paraMap.putAll(t.getParaMap());
    		
    		for(Entry<String,Set<String>> en: t.getFieldMap().entrySet())
    		{
    			if(fieldMap.containsKey(en.getKey()))
    				fieldMap.get(en.getKey()).addAll(en.getValue());
    			else
    				fieldMap.put(en.getKey(), en.getValue());
    		}
    		
    		
    		 		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		//Set<String> value = new HashSet<>();
    	
    	
    		
    		//Interpreter intp = new Interpreter(t,loopCount);
    		
    		//label set<IR>
    		Map<String,Set<Variable>> labelIR = new HashMap<>();
    		
    		for(String labelwithnum:t.getTargetLines().keySet())
    		{
    			
    			Set<Variable> targetIR = new HashSet<>();
    			for(String line: t.getTargetLines().get(labelwithnum))
    			{
    				//if target IR is a constant string
    				if(line.equals("-1"))
    				{

    		    		//add const label
    		    		
    		    		if(tMap.get(signature).getLabelConstant().get(labelwithnum)!=null)
    		    		{
    		    			String value = tMap.get(signature).getLabelConstant().get(labelwithnum);
    		    			
    		    			targetIR.add(new ConstantString(value.substring(1,value.length()-1)));
    		    		}
    					
    				}
    				if(t.getTranslatedIR(line)!=null)
    					targetIR.addAll(t.getTranslatedIR(line));
    			}
    			labelIR.put(labelwithnum, targetIR);
    		}
    		
    		
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, labelIR);
    	 		

    		t2 = System.currentTimeMillis();
    		totalTranslate += t2-t1;
    	}
    	int count = 0;
    	
    	List<String> statistic = new ArrayList<>();
    	for(Entry<String,Map<String,Set<Variable>>> enout: targetMap.entrySet())
    	{
    		String signature = enout.getKey();
    		int i1 = signature.indexOf("<"),i2 = signature.indexOf(":");
    		
    		
    		//System.out.println("\n"+signature);
    		

    		
    		for(Entry<String,Set<Variable>> en:enout.getValue().entrySet())
    		{
	    		t1 = System.currentTimeMillis();
	    		Set<Variable> newIR = replaceExternal(en.getValue(),signature,paraMap,tMap,App);
	    		
	    		//statistic.add(en.getKey()+":"+getWidth(newIR)+" "+getHeight(newIR)+" "+getLoopDepth(newIR)+" "+getLoopCount(newIR)+" "+getExternalCount(newIR));
	    		
	    		t2 = System.currentTimeMillis();
	    		totalTranslate += t2-t1;
	    
	    		
	    		t1 = System.currentTimeMillis();

				Interpreter intp = new Interpreter(newIR,fieldMap,maxloop, appFolder);
				Set<String> value = new HashSet<>();
				value.addAll(intp.getValueForIR());
	    		

	    		
    			t2 = System.currentTimeMillis();
    			
    			totalInterpret += t2-t1;
	    	//	System.out.println("Label: "+en.getKey());
	    	//	System.out.println("Output: "+value);
    			
    			//if(!value.isEmpty())
    			if(value != null)
	    		//if(!emptyOrContainUnknown(value))  			
	    		{
	    			
	    			String[] hotspot = en.getKey().split("@");
	    			/*
					System.out.println("Method Name: "+ hotspot[0]);
					System.out.println("Source Line Number: "+ hotspot[1]);
					System.out.println("Bytecode Offset: "+hotspot[2]);
					System.out.println("Nth String Parameter: " + hotspot[3]);
					System.out.println("Jimple: "+ hotspot[4]);
					System.out.println("IR: "+newIR);
					System.out.println("Value: "+ value);
					System.out.println();
	    			*/
		    		try
		    		{
		    			
		    			
		    			PrintWriter bw = new PrintWriter(new FileWriter(wfolder+"statistic.txt",true));
		    			//PrintWriter bw = new PrintWriter(new FileWriter(wfolder+en.getKey().replaceAll("\"", "")+".txt",true));
						bw.println("Method Name: "+ hotspot[0]);
						bw.println("Source Line Number: "+ hotspot[1]);
						bw.println("Bytecode Offset: "+hotspot[2]);
						bw.println("Nth String Parameter: " + hotspot[3]);
						bw.println("Jimple: "+ hotspot[4]);
						bw.println("Node ID: "+hotspot[5]);
						bw.println("IR: "+newIR);
						bw.println("Value: "+ value);
						bw.println();
		    			
		    			bw.flush();
		    			bw.close();
		    		}
		    		catch(IOException e)
		    		{
		    			e.printStackTrace();
		    		}
	    				    			
	    		/*
    			try
	    		{

	    			
	    			 BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+en.getKey().replaceAll("\"", "")+".txt",true));


	    				for(String s:value)
	    				{	    				
	    					bw.write(s);
	    					bw.newLine();
	    				}
	    			
	    			
	    			bw.flush();
	    			bw.close();
	    		}
	    		
	    		catch(IOException e)
	    		{
	    			e.printStackTrace();
	    		}
		    	*/	
		    		
		    		
	    		}

	    	}
    	}
    	/*
    	try {
			BufferedWriter sta = new BufferedWriter(new FileWriter(wfolder+"statistic.txt",true));
			for(String s: statistic)
			{
				sta.write(s);
				sta.newLine();
			}
			sta.flush();
			sta.close();
    	}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		*/
    	System.out.println("Total Trans: "+ totalTranslate);
    	System.out.println("Total Interp: "+ totalInterpret);
	}
	
	
	private void InterpretCheckerJava(String arg0,String arg1,String arg2,String summaryFolder,String wfolder, String appFolder)
	{
		//"/home/yingjun/Documents/StringAnalysis/MethodSummary/"
		//"Usage: rt.jar app_folder classlist.txt"

		JavaApp App;
		if(arg1.contains("bookstore"))
		{

			App=new JavaApp(arg0,arg1,arg2,"void _jspService(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)");
		}
		else{
			App=new JavaApp(arg0,arg1,arg2,"void main(java.lang.String[])");
		}
		Map<String,Map<String,Set<Variable>>> targetMap = new HashMap<>();
    	Map<String,Set<NodeInterface>> paraMap = new HashMap<>();
    	Map<String,Set<String>> fieldMap = new HashMap<>();
		Map<String,Translator> tMap = new HashMap<>();
		long totalTranslate = 0,totalInterpret = 0;
		
		
		long t1,t2;
		

		 File sFolder = new File(summaryFolder);
		 File wFolder = new File(wfolder);
		 // if the directory does not exist, create it
		 if (!sFolder.exists()) {
		     System.out.println("creating directory: " + sFolder);
		     boolean result = false;
		     try{
		    	 sFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"MethodSummary\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }
		 if (!wFolder.exists()) {
		     System.out.println("creating directory: " + wFolder);
		     boolean result = false;
		     try{
		    	 wFolder.mkdir();
		         result = true;
		     } 
		     catch(SecurityException se){
		    	 System.out.println("Create a folder named : \"Output\" under the app folder");
		     }        
		     if(result) {    
		         System.out.println("DIR created");  
		     }
		 }


    	for(CFGInterface cfg:App.getCallgraph().getRTOInterface())
    	{
    		//System.out.println(cfg.getSignature());
    		String signature=cfg.getSignature();
    		
    	//	if(!signature.contains("SubstringOfNull"))
    	//		continue;
    		
    		
    		if(signature.equals("<LoggerLib.Logger: void <clinit>()>")||signature.equals("<LoggerLib.Logger: void reportString(java.lang.String,java.lang.String)>"))
    		continue;
    		
    		//field																	def missing						

    		

    		
    		
      		//for(int i=1;i<=loopCount;i++)
    		//{  		
    		
    		

    		t1 = 	System.currentTimeMillis();
    		LayerRegion lll = new LayerRegion(null);
    		ReachingDefinition rd = new ReachingDefinition(cfg.getAllNodes(), cfg.getAllEdges(),lll.identifyBackEdges(cfg.getAllNodes(),cfg.getAllEdges(), cfg.getEntryNode()));	   		
    
    		
    		LayerRegion lr = new LayerRegion(cfg);
    	
    		//System.out.println(signature);
    		Translator t = new Translator(rd, lr,signature,summaryFolder,targetSignature);
    	
    		tMap.put(signature, t);
    		paraMap.putAll(t.getParaMap());
    		
    		for(Entry<String,Set<String>> en: t.getFieldMap().entrySet())
    		{
    			if(fieldMap.containsKey(en.getKey()))
    				fieldMap.get(en.getKey()).addAll(en.getValue());
    			else
    				fieldMap.put(en.getKey(), en.getValue());
    		}
    		//fieldMap.putAll(t.getFieldMap());
    		 		
    		if(t.getTargetLines().isEmpty())
    			continue;
    		
    		//Set<String> value = new HashSet<>();
    	
    		
    		//label set<IR>
    		Map<String,Set<Variable>> labelIR = new HashMap<>();
    		
    		for(String labelwithnum:t.getTargetLines().keySet())
    		{
    			Set<Variable> targetIR = new HashSet<>();
    			for(String line: t.getTargetLines().get(labelwithnum))
    				if(t.getTranslatedIR(line)!=null)
    					targetIR.addAll(t.getTranslatedIR(line));
    			
    	
    			//if(targetIR.isEmpty())
    			//	targetIR.add(new ExternalPara("Unknown"));
    			labelIR.put(labelwithnum, targetIR);
    		}
    		

    	
    		
    		if(!targetMap.containsKey(signature))
    			targetMap.put(signature, labelIR);
    	 		
    		t2 = 	System.currentTimeMillis();
    		totalTranslate+=t2-t1;
    	
    	}
    	
	
    	
		
    	
   
		
		
    	for(Entry<String,Map<String,Set<Variable>>> enout: targetMap.entrySet())
    	{
    		String signature = enout.getKey();
    		
    		//System.out.println("\n"+signature);
    		

    		
    		for(Entry<String,Set<Variable>> en:enout.getValue().entrySet())
    		{
    		
    			
    				
	    		t1 = System.currentTimeMillis();
	    		Set<Variable> newIR = replaceExternal(en.getValue(),signature,paraMap,tMap,App);
	    		t2 = System.currentTimeMillis();
	    		totalTranslate += t2-t1;
	    		
	    		
	    		int loopCount = 3;	    			    		
	    		String tempSig = signature.replaceAll("TestCases.", "");
	    		int dot = tempSig.indexOf(".");
	    		String tt = tempSig.substring(dot+1);
	    		
	    		if(tt.contains("Mix")||tt.contains("NestedLoop"))
	    			loopCount = 2;
	    		else
	    			loopCount = 3;
	    		
	    		Set<String> value = new HashSet<>();
	    		
	    		t1 = System.currentTimeMillis();
    			Interpreter intp = new Interpreter(newIR,fieldMap,loopCount, appFolder);
    			
    			value.addAll(intp.getValueForIR());

	    		
	    		//add const label
	    		
	    		if(tMap.get(signature).getLabelConstant().get(en.getKey())!=null)
	    		{
	    		//	value.add(tMap.get(signature).getLabelConstant().get(en.getKey()).replaceAll("\"", ""));
	    		}
	    		
    			t2 = System.currentTimeMillis();
    			
    			totalInterpret += t2-t1;
	    		
	    		//System.out.println("Label: "+en.getKey());
	    		//System.out.println("Output: "+value);
	    		
	    		if(!emptyOrContainUnknown(value))
	    		{
	    			//System.out.println(en.getKey()+":"+value+";"+value.isEmpty()+value.iterator().next().equals(""));
	    			
		    		try
		    		{
		    			BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+en.getKey().replaceAll("\"", "")+".txt",true));
		    			//BufferedWriter bw = new BufferedWriter(new FileWriter(wfolder+"output.txt",true));
		    			//bw.write(en.getKey().replaceAll("\"", ""));
		    			//bw.newLine();
		    			for(String s:value)
		    			{
		    				bw.write(s);
		    				bw.newLine();
		    			}
		    			
		    			bw.flush();
		    			bw.close();
		    		}
		    		catch(IOException e)
		    		{
		    			e.printStackTrace();
		    		}
	    		}

	    
	    	}
    		
    	    

    	}
    	
    	System.out.println("Total Trans: "+ totalTranslate);
    	System.out.println("Total Interp: "+ totalInterpret);
	}
	
	boolean notEmptyAndContainNotUnknown(Set<String> value)
	{
		if(value.isEmpty())
			return false;
		else
		{
			for(String s:value)
				if(!s.contains("(.*)"))
						return true;
			
			return false;
		}
	}
	boolean emptyOrContainUnknown(Set<String> value)
	{
		if(value.isEmpty())
			return true;
		else
		{
			for(String s:value)
				if(s.contains("Unknown"))
					return true;
			return false;
		}
	}
	private Variable copyVar(Variable v)
	{
		
		if(v instanceof InternalVar)
			return new InternalVar(((InternalVar) v).getName(),((InternalVar) v).getK(),((InternalVar) v).getSigma(),((InternalVar) v).getRegionNum(),((InternalVar) v).getLine());
		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
			List<Variable> tempOp = new ArrayList<>();
			for(Variable operand:operandList)
			{
				if(operand instanceof InternalVar)
					tempOp.add(new InternalVar(((InternalVar) operand).getName(),((InternalVar) operand).getK(),((InternalVar) operand).getSigma(),((InternalVar) operand).getRegionNum(),((InternalVar) operand).getLine()));
				else if(operand instanceof Expression)
				{
					
					//System.out.println(operand.getSize());
					tempOp.add(copyVar(operand));
				}
				else if(operand instanceof T)
				{
					
					tempOp.add(new T(copyVar(((T) operand).getVariable()),((T) operand).getTVarName(),((T) operand).getRegionNumber(),((T) operand).getK(),((T) operand).isFi(),((T) operand).getLine()));
				}
				else
					tempOp.add(operand);
			}
			newOperandList.add(tempOp);
			}
			return new Expression(newOperandList,((Expression) v).getOperation());
		}
		else if(v instanceof T)
		{
			return new T(copyVar(((T) v).getVariable()),((T) v).getTVarName(),((T) v).getRegionNumber(),((T) v).getK(),((T) v).isFi(),((T) v).getLine());
		}

		else
			return v;
	}
	private Set<Variable> replaceExternal(Set<Variable> IRs,String signature,Map<String,Set<NodeInterface>> paraMap,Map<String,Translator> tMap,JavaApp App)
	{
		
		boolean existPara = false;
		for(Variable v:IRs)
		{
			if(containPara(v))
				existPara = true;
		}
		if(!existPara)
			return IRs;
		else
		{
			Set<Variable> vSet = new HashSet<>();
			for(Variable v: IRs)
			{
				if(paraMap.get(signature)==null)
					vSet.add(v);
				else
				{
					if(App.getCallgraph().getParents(signature).isEmpty())
						vSet.add(copyVar(v));
					else
					{
						for(String parentSig:App.getCallgraph().getParents(signature))
						{
							Set<Variable> newIR = new HashSet<>();
							for(NodeInterface n:tMap.get(parentSig).getParaMap().get(signature))
								newIR.addAll(replaceExternal(copyVar(v),n,tMap.get(parentSig)));
							
							Set<Variable> copy = new HashSet<>();
							for(Variable vv:newIR)
								copy.add(copyVar(vv));
							vSet.addAll(replaceExternal(copy,parentSig, paraMap, tMap, App));
						}					
					}
					
				}			
			}
			return vSet;
		}

/*		Set<Variable> vSet = new HashSet<>();
		for(Variable v: IRs)
		{
			if(paraMap.get(signature)==null)
				vSet.add(v);
			else
			{
				for(NodeInterface n:paraMap.get(signature))
				{
	    			Set<Variable> newIR = new HashSet<>();
	    				if(App.getCallgraph().getParents(signature).isEmpty())
	    					newIR.add(copyVar(v));
	    				else
	    				{
	    				String parentSig = App.getCallgraph().getParents(signature).iterator().next();
	    				newIR.addAll(replaceExternal(copyVar(v),n,tMap.get(parentSig)));
	    				}
	    			vSet.addAll(newIR);
	    		}				
			}			
		}
		boolean existPara = false;
		for(Variable v:vSet)
		{
			if(containPara(v))
				existPara = true;
		}
		if(!existPara)
			return vSet;
		else
		{
			if(App.getCallgraph().getParents(signature).isEmpty())
				return vSet;
			else
			{
				String parentSig = App.getCallgraph().getParents(signature).iterator().next();
				if(paraMap.get(parentSig)==null)
					return vSet;
				else
				{
					Set<Variable> copy = new HashSet<>();
					for(Variable vv:vSet)
						copy.add(copyVar(vv));
					Set<Variable> newIR = new HashSet<>();
					newIR.addAll(replaceExternal(copy,parentSig, paraMap, tMap, App));
					return newIR;
				}
			}
		}*/
		
	}

	private Set<Variable> replaceExternal(Set<Variable> IRs,String signature,Map<String,Set<NodeInterface>> paraMap,Map<String,Translator> tMap,AndroidApp App)
	{

		boolean existPara = false;
		for(Variable v:IRs)
		{
			if(containPara(v))
				existPara = true;
		}
		if(!existPara)
		{	
			return IRs;
		}
		else
		{
			Set<Variable> vSet = new HashSet<>();
			for(Variable v: IRs)
			{
				if(paraMap.get(signature)==null)
					vSet.add(v);
				else
				{
					if(App.getCallgraph().getParents(signature).isEmpty())
						vSet.add(copyVar(v));
					else
					{
						for(String parentSig:App.getCallgraph().getParents(signature))
						{
							Set<Variable> newIR = new HashSet<>();
							
							if(tMap.get(parentSig)!=null&&tMap.get(parentSig).getParaMap()!=null)
								if(tMap.get(parentSig).getParaMap().get(signature)!=null)
								{
									for(NodeInterface n:tMap.get(parentSig).getParaMap().get(signature))
										newIR.addAll(replaceExternal(copyVar(v),n,tMap.get(parentSig)));
									
									Set<Variable> copy = new HashSet<>();
									for(Variable vv:newIR)
										copy.add(copyVar(vv));
									
								//	System.out.println(signature);
									vSet.addAll(replaceExternal(copy,parentSig, paraMap, tMap, App));
									
								}

						}					
					}
					
				}			
			}
			return vSet;
		}

	}
	
	boolean containPara(Variable v)
	{

			if(v instanceof ExternalPara)
			{
				if(((ExternalPara) v).getName().contains("@parameter"))
					return true;
				else
					return false;
			}
			else if(v instanceof Expression)
			{
				for(List<Variable> operandList:((Expression) v).getOperands())
				{
					for(Variable operand: operandList)
					if(containPara(operand))
						return true;
				}
				return false;
			}
			else if(v instanceof T)
			{
			
				return containPara(((T) v).getVariable());
			}
			else
			return false;
	}
	
	
	private Set<Variable> replaceExternal(Variable v,NodeInterface n,Translator t)
	{
		Set<Variable> returnSet = new HashSet<>();
		if(v instanceof ExternalPara)
		{
			if(((ExternalPara) v).getName().contains("@parameter"))
			{
				String tmp = ((ExternalPara) v).getName().split(":")[0].replaceAll("@parameter", "");
				int index = Integer.parseInt(tmp);
				
			//	System.out.println(index +" "+valueBox);
			
				List<ValueBox> valueBox = ((Unit)((Node)n).getActualNode()).getUseBoxes();

				if(!(valueBox.get(0) instanceof ImmediateBox))
					index = index+1;
				
			//	System.out.println(index+"->index-> "+valueBox);
				
				if(index >= valueBox.size())
					returnSet.add(v);
				else 
				{
					String para = valueBox.get(index).getValue().toString();
			
				
				
					
					if(para.contains("\""))
						returnSet.add( new ConstantString(para));
					else if(valueBox.get(index).getValue().getType().toString().equals("int"))
					{
					
						if(!para.contains("i")&&!para.contains("b"))
						returnSet.add( new ConstantString(""+(char)Integer.parseInt(para)));
					}
	
					else
					{
						
						Set<Variable> newIR = new HashSet<>();
	
						//System.out.println(valueBox.toString()+para+t.getRD().getAllDef());
						for(String line:t.getRD().getLineNumForUse(n, para))
						{
							if(t.getTranslatedIR(line)!=null)
							newIR.addAll(t.getTranslatedIR(line));
						}
						
						
						
						returnSet.addAll(newIR);
					}
					
				}
			}
			else
				returnSet.add(v);
			
		}

		else if(v instanceof Expression)
		{
			List<List<Variable>> newOperandList = new ArrayList<>();
			for(List<Variable> operandList:((Expression) v).getOperands())
			{
				List<Variable> tempOperand = new ArrayList<>();
				for(Variable operand:operandList)
				{
					tempOperand.addAll( replaceExternal(operand,n,t));
				}
				newOperandList.add(tempOperand);
			}
 			((Expression) v).setOperands(newOperandList);
 			returnSet.add(v);
		}
		else if(v instanceof T)
		{
			((T) v).setVariable(replaceExternal(((T) v).getVariable(),n,t).iterator().next());
			returnSet.add(v);
		}
		else
			returnSet.add(v);
		
		return returnSet;
	}

	public int getWidth(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int width = getWidth(v);
			if(width > max)
				max = width;
		}
		return max;
			
	}
	public int getWidth(Variable v)
	{
		if(v instanceof T)
			return getWidth(((T)v).getVariable());
		else if(v instanceof Expression)
		{
			int sumWidth = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				int max = 0;
				for(Variable var: varList)
				{
					int temp = getWidth(var);
					if(temp > max)
						max = temp;		
				}
				sumWidth+=max;
			}
			return sumWidth;
		}
		else return 1;
	}
	
	public int getHeight(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int height = getHeight(v);
			if(height > max)
				max = height;
		}
		return max;
			
	}
	public int getHeight(Variable v)
	{
		if(v instanceof T)
			return getHeight(((T)v).getVariable());
		else if(v instanceof Expression)
		{
			int maxHeight = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					int temp = getHeight(var);
					if(temp > maxHeight)
						maxHeight = temp;		
				}
			}
			return maxHeight+1;
		}
		else
			return 1;
	}
	
	public int getLoopDepth(Set<Variable> vars)
	{
		int max = 0;
		for(Variable v: vars)
		{
			int depth = getLoopDepth(v);
			if(depth > max)
				max = depth;
		}
		return max;
	}
	public int getLoopDepth(Variable v)
	{
		if(v instanceof T)
			return getLoopDepth(((T)v).getVariable())+1;
		else if(v instanceof Expression)
		{
			int maxLoopDepth = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					int temp = getLoopDepth(var);
					if(temp > maxLoopDepth)
						maxLoopDepth = temp;		
				}
			}
			return maxLoopDepth;
		}
		else 
			return 0;
	}
	public int getLoopCount(Set<Variable> vars)
	{
		int count = 0;
		for(Variable v: vars)
			count+= getLoopCount(v);
		return count;
	}
	public int getLoopCount(Variable v)
	{
		if(v instanceof T)
			return getLoopCount(((T)v).getVariable())+1;
		else if(v instanceof Expression)
		{
			int loopCount = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					loopCount+=getLoopCount(var);		
				}
			}
			return loopCount;
		}
		else 
			return 0;
	}
	public int getExternalCount(Set<Variable> vars)
	{
		int count = 0;
		for(Variable v: vars)
			count+= getExternalCount(v);
		return count;
	}
	public int getExternalCount(Variable v)
	{
		if(v instanceof ExternalPara)
			return 1;
		else if(v instanceof Expression)
		{
			int externalCount = 0;
			for(List<Variable> varList: ((Expression) v).getOperands())
			{
				for(Variable var: varList)
				{
					externalCount+=getExternalCount(var);		
				}
			}
			return externalCount;
		}
		else 
			return 0;
	}
	
	
	public Set<String> identifyRelevant(Set<String> targetScanList)
    {
//		System.out.println(targetScanList); only one
        Set<String> targetMethod = new HashSet<>();
         
        List<NewNode> rto = App.getCallgraph().getRTOdering();
//        System.out.println("Yixue:---");
//        for(String targetSig: targetScanList){
//        	System.out.println("target sig = "+targetSig);
//        	for(String parent: App.getCallgraph().getParents(targetSig)){
//        		System.out.println("parent: "+parent);
//        	}
//        }
//        System.out.println("Yixue:---");
        Map<String,NewNode> rtoMap = App.getCallgraph().getRTOMap();
//        App.getCallgraph().getParents()
        for(NewNode n: rto)
        {
            if(n.getMethod().isConcrete()&&!n.getMethod().getDeclaringClass().isAbstract())
            {

                boolean isRelevant = false;
//                System.out.println("Yixue n = "+n.toString());
                for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
                {
                    
                    if(((Stmt)actualNode).containsInvokeExpr())
                    {
                        SootMethod sm = null;
                        try{
                            sm =((Stmt)actualNode).getInvokeExpr().getMethod();
                        }
                        catch(ResolutionFailedException ex)
                        {
                            System.out.println("Soot fails to get the method:"+((Stmt)actualNode).getInvokeExpr());
                        }    
                        if(sm==null)
                            continue;
                        
                        String sig= sm.getSignature();
                        if(targetScanList.contains(sig)||targetMethod.contains(sig))
                        {
                            isRelevant = true;
//                            addParentEdgesRecursive(n.getMethod().getSignature(), cgDot, strCg);
                        }
                        
                    }
                }
                
                if(isRelevant)
                {
                    targetMethod.add(n.getMethod().getSignature());
                    
                }


            }
            
        }
        System.out.println("Methods contain target APIs: "+ targetMethod.size());
        
        Stack<String> processMethod = new Stack<>();
        processMethod.addAll(targetMethod);
        //topo
        while(!processMethod.isEmpty())
        {
            String currentMethod = processMethod.pop();
            NewNode n = rtoMap.get(currentMethod);
            SootMethod sm = n.getMethod();
//            System.out.println("Yixue: target method = "+sm.getSignature());
            if(sm.isConcrete()&&!sm.getDeclaringClass().isAbstract())
            {
                
                for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
                {
                    
                    if(((Stmt)actualNode).containsInvokeExpr())
                    {
                        
                        SootMethod method = ((Stmt)actualNode).getInvokeExpr().getMethod();
                        String type = method.getReturnType().toString();
                        
                        if(type.equals("java.lang.String")||type.equals("java.lang.StringBuilder")
                                ||type.equals("java.lang.StringBuffer"))
                        {
                            if(rto.contains(method.getSignature()))
                            {
                                if(!targetMethod.contains(method.getSignature()))
                                {
                                    processMethod.push(method.getSignature());
                                    targetMethod.add(method.getSignature());
                                }
                            }
                        }
    
                        
                    }
                }
            }

        }
        System.out.println("Target methods and callees: "+ targetMethod.size());
        
        for(NewNode n: rto)
        {

            if(n.getMethod().isConcrete()&&!n.getMethod().getDeclaringClass().isAbstract())
            {

                for(Unit actualNode:n.getMethod().retrieveActiveBody().getUnits())
                {
                    
                    if(((Stmt)actualNode).containsInvokeExpr())
                    {
                        SootMethod sm = null;
                        try{
                            sm =((Stmt)actualNode).getInvokeExpr().getMethod();
                        }
                        catch(ResolutionFailedException ex)
                        {
                            System.out.println("Soot fails to get the method:"+((Stmt)actualNode).getInvokeExpr());
                        }    
                        if(sm==null)
                            continue;
                        
                        String sig= sm.getSignature();
                        
                        if(targetMethod.contains(sig))
                        {
                            targetMethod.add(n.getMethod().getSignature());
                        }
                        
                    }
                }
                

            }
            
        }
        System.out.println("Target methods and callers and callees: "+ targetMethod.size());
        Set<String> targetClass = new HashSet<>();
        for(String sig: targetMethod)
        {
            targetClass.add(sig.substring(0,sig.indexOf(":")+1));
        }
        for(NewNode n: rto)
        {
            String sig = n.getMethod().getSignature();
            String className = sig.substring(0,sig.indexOf(":")+1);
            if(sig.contains("<init>")||sig.contains("<clinit>"))
            {
                if(targetClass.contains(className))
                {
                    targetMethod.add(sig);
                }
                
            }
                
        }
        System.out.println("Total Target Method including constructor: "+ targetMethod.size());
        return targetMethod;
    }
	
	public void addParentEdgesRecursive(String child, DOTGraph graph, StringCallGraph cg, Map<String, String> targetMethodIdMap){
		if(cg.getParents(child) == null || cg.getParents(child).isEmpty()){
			return;
		}
		for(String parent: cg.getParents(child)){
            	DOTEdge edge = new DOTEdge();
            	String right;
            	String left;
            	if(targetMethodIdMap.containsKey(child)){
            		String nodeIds = targetMethodIdMap.get(child);
            		 right = child + nodeIds;
            		 left = parent + nodeIds;
            		 targetMethodIdMap.put(parent, nodeIds);
            	}else{
            		System.out.println("in ViolistAnalysisHelper.addParentEdgesRecursive(): can't find node ids in the map! this shouldn't happen! :(");
            		right = child;
            		left = parent;
            	}
            	edge.setChild(right);
            	edge.setParent(left);
            	graph.addEdge(edge);
            	addParentEdgesRecursive(parent, graph, cg, targetMethodIdMap);
		}
	}
	
	public void printTargetCallGraph(StringCallGraph cg, DOTGraph graph, String appfolder){
		Map<String, String> targetMethodIdMap = ViolistAnalysisHelper.getTargetMethodIdMap(appfolder);
		for(String method: ViolistAnalysisHelper.getTargetMethodSet(appfolder)){
			addParentEdgesRecursive(method, graph, cg, targetMethodIdMap);
		}
		DOTUtil.DOT2File(graph, appfolder+"/Output/targetCG.dot");
	}
	
	
}
