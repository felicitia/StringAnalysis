package usc.yixue;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.options.Options;


public class ResourceAnalysis {

	static String libPath = "/Users/felicitia/Documents/Research/Prefetch/Develop/Ding_Sample/libs";
	
	static String appFolder; //args0
	static String pkgName;
	static String jimpleStrPath; // = "/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/ClasslistGenerator/sootOutput/edu.usc.yixue.weatherapp.R$string.jimple"; //full path
	static String xmlStrPath; // = "/Users/felicitia/Documents/workspaces/AndroidStudio/WeatherApp/app/src/main/res/values/strings.xml"; //full path
	static String strXmlClassName; // = "edu.usc.yixue.weatherapp.R$string";
	static Map<String, String> stringRMap = null;
	
	public static void main(String args[]){
//		System.out.println();
		appFolder = args[0];
		pkgName = getPkgName(appFolder);
		jimpleStrPath = appFolder+"/sootOutput/"+pkgName+".R$string.jimple";
		xmlStrPath = appFolder+"/decompile/res/values/strings.xml";
		strXmlClassName = pkgName+".R$string";
		sootSettingForJimple();
//		System.out.println(getStringRMap());
	}
	public static Map<String, String> getStringRMap(String appFolderParam){
		if(stringRMap != null) {
			return stringRMap;
		}
		appFolder = appFolderParam;
		pkgName = getPkgName(appFolder);
		jimpleStrPath = appFolder+"/sootOutput/"+pkgName+".R$string.jimple";
		xmlStrPath = appFolder+"/decompile/res/values/strings.xml";
		strXmlClassName = pkgName+".R$string";
		sootSettingForJimple();
		stringRMap = new HashMap<String, String>();
		Map<String, String> stringXml = buildStringXml();
		Map<String, String> stringJimple = buildStringJimple();
//		stringRMap.put("2131099672", "APPID=f46f62442611cdc087b629f6e87c7374");
		Iterator it = stringJimple.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        String id = pair.getKey().toString();
	        String stringName = pair.getValue().toString();
	        if(stringXml.containsKey(stringName)){
	        	stringRMap.put(id, stringXml.get(stringName));
	        }
	    }
		return stringRMap;
	}

	// e.g., 2131099672: weather_api_key
	public static Map<String, String> buildStringJimple(){
		Map<String, String> stringJimple = new HashMap<String, String>();
		File jimpleFile = new File(jimpleStrPath);
		SootClass stringRClass = Scene.v().loadClassAndSupport(strXmlClassName);
		System.out.println("strXmlClassName = "+strXmlClassName);
		for(SootField field: stringRClass.getFields()){
			System.out.println("field = "+field);
			System.out.println("field tags = "+field.getTags());
			String tag = field.getTag("IntegerConstantValueTag").toString();
			String id = tag.substring("ConstantValue: ".length());
			stringJimple.put(id, field.getName());
		}
		return stringJimple;
	}
	// e.g., app_name: WeatherApp
	public static Map<String, String> buildStringXml(){
		Map<String,String> stringXml = new HashMap<String, String>(); 
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new File(xmlStrPath));
					doc.getDocumentElement().normalize();
					NodeList stringList = doc.getElementsByTagName("string");
					for(int i=0; i<stringList.getLength(); i++){
						Node node = stringList.item(i);
						Element element = (Element) node;
						stringXml.put(element.getAttribute("name"), element.getTextContent());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return stringXml;
	}
	
	public static String getJimpleStrPath() {
		return jimpleStrPath;
	}

	public static String getPkgName(String appfolder){
		String manifestPath = appfolder+"/decompile/AndroidManifest.xml";
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new File(manifestPath));
					doc.getDocumentElement().normalize();
					NodeList elements =  doc.getElementsByTagName("manifest");
					Node node = elements.item(0); //only one node with tag "manifest"
					Element element = (Element) node;
					return element.getAttribute("package");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
	}
	
	public static String getXmlStrPath() {
		return xmlStrPath;
	}
	
	public static void sootSettingForJimple(){
		Options.v().set_soot_classpath(libPath+"/rt.jar:"+libPath+"/android--1/android.jar"+":"+appFolder+"/sootOutput");
        Options.v().set_whole_program(true);
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_src_prec(Options.src_prec_jimple);
        Options.v().set_keep_offset(true);
        Options.v().set_allow_phantom_refs(true);
      	Options.v().set_keep_offset(true);
      	Scene.v().loadNecessaryClasses();
	}
	
}
