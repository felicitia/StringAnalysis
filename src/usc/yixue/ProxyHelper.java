package usc.yixue;

import java.util.HashMap;
import java.util.HashSet;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class ProxyHelper {

	// key is the body that needs to be instrumented
	public final static HashMap<String, HashSet<InstrumentStmt>> instrumentMap = new HashMap<String, HashSet<InstrumentStmt>>();

	public final static HashMap<String, String> jimpleReplaceMap = new HashMap<String, String>();
	
	/**
	 * change this to list? Body shouldn't be the key, cause it might be duplicated, e.g., one method contains multiple def spots
	 */
//	public final static HashMap<String, DefSpot> defSpotMap = new HashMap<String, DefSpot>(); //body is the key

	final static String ProxyClass = "usc.yixue.Proxy";

//	final static String getContentOriginal = "<java.net.URLConnection: java.lang.Object getContent()>";
//	final static String getContentNew = "java.lang.Object getContent(java.net.URLConnection)";

	final static String getInputStreamOriginal = "<java.net.URLConnection: java.io.InputStream getInputStream()>";
	final static String getInputStreamNew = "java.io.InputStream getInputStream(java.net.URLConnection)";

//	final static String openStreamOriginal = "<java.net.URL: java.io.InputStream openStream()>";
//	final static String openStreamNew = "java.io.InputStream openStream(java.net.URL)";
	
	final static String sendDef = "void sendDef(java.lang.String,java.lang.String,int,java.lang.String)";

	final static String printTimeStamp = "void printTimeStamp(java.lang.String)";
	
	final static String triggerPrefetch = "void triggerPrefetch(java.lang.String)";
	
	static {
		jimpleReplaceMap.put(getInputStreamOriginal, getInputStreamNew);
//		DefSpot defSpot = new DefSpot();
//		defSpot.setJimple("$r0.<edu.usc.yixue.weatherapp.MainActivity: java.lang.String favCityId> = $r9");
//		defSpot.setNodeId("307");
//		defSpot.setPkgName("edu.usc.yixue.weatherapp");
//		defSpot.setSubStrPos(1);
//		defSpot.setBody("<edu.usc.yixue.weatherapp.MainActivity: void onCreate(android.os.Bundle)>");
//		defSpotMap.put(defSpot.getBody(), defSpot);
		
		// instrument "saveUserImage" body
//		InstrumentStmt getContentInstr = new InstrumentStmt();
//		getContentInstr.jimpleReplacement = getContentNew;
//		getContentInstr.jimpleOriginal = getContentOriginal;
//		getContentInstr.body = "<com.newsblur.util.PrefsUtils: void saveUserImage(android.content.Context,java.lang.String)>";
//		HashSet<InstrumentStmt> set1 = new HashSet<InstrumentStmt>();
//		set1.add(getContentInstr);
//
//		InstrumentStmt openStreamInstr = new InstrumentStmt();
//		openStreamInstr.body = "<com.android.buttonwidget.FetchFromAPI: java.lang.String fetchAddFromAPI(java.lang.String)>";
//		openStreamInstr.jimpleOriginal = openStreamOriginal;
//		openStreamInstr.jimpleReplacement = openStreamNew;
//		HashSet<InstrumentStmt> set2 = new HashSet<InstrumentStmt>();
//		set2.add(openStreamInstr);

		InstrumentStmt getInputStreamInstr = new InstrumentStmt();
		getInputStreamInstr.body = "";
		getInputStreamInstr.jimpleOriginal = getInputStreamOriginal;
		getInputStreamInstr.jimpleReplacement = getInputStreamNew;
		HashSet<InstrumentStmt> set3 = new HashSet<InstrumentStmt>();
		set3.add(getInputStreamInstr);

//		instrumentMap
//				.put("<com.newsblur.util.PrefsUtils: void saveUserImage(android.content.Context,java.lang.String)>",
//						set1);
//		instrumentMap
//				.put("<com.android.buttonwidget.FetchFromAPI: java.lang.String fetchAddFromAPI(java.lang.String)>",
//						set2);

	}

	/**
	 * if the body is known, then query "instrumentMap"
	 * 
	 * @param original
	 * @return
	 */
	public static SootMethod queryReplaceMethodWithBody(String original) {
		for (String key : instrumentMap.keySet()) {
			for (InstrumentStmt stmt : instrumentMap.get(key)) {
				if (stmt.jimpleOriginal.equals(original)) {
					SootClass ProxyClass = Scene.v().loadClassAndSupport(
							ProxyHelper.ProxyClass);
					SootMethod replaceMethod = ProxyClass
							.getMethod(stmt.jimpleReplacement);
					return replaceMethod;
				}
			}
		}
		return null;
	}

	/**
	 * if the body is not known, then query "jimpleReplaceMap", which will replace
	 * every method no matter what body it belongs
	 * 
	 * @param original
	 * @return
	 */
	public static SootMethod queryReplaceMethod(String original) {

		if (jimpleReplaceMap.containsKey(original)) {
			SootClass ProxyClass = Scene.v().loadClassAndSupport(
					ProxyHelper.ProxyClass);
			SootMethod replaceMethod = ProxyClass
					.getMethod(jimpleReplaceMap.get(original));
			return replaceMethod;
		}

		return null;
	}

	public static SootMethod findMethod(String methodName) {
		SootClass ProxyClass = Scene.v().loadClassAndSupport(ProxyHelper.ProxyClass);
		SootMethod helpMethod = ProxyClass.getMethod(methodName);
		System.out.println("===========helpMethod = "
				+ helpMethod.getSignature());
		return helpMethod;
	}
}
