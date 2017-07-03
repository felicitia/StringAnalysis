package usc.yixue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;

public class DefFinder {

	final static String appFolder = "/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/Android/WeatherApp"; 
	final static String androidJar = "/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/Android";
	static List<DefSpot> defSpotList = new ArrayList<DefSpot>();
	static List<TargetField> targetFieldList = new ArrayList<TargetField>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		sootSettingAndroid(args[2], androidJar);
		
		add2TargetFieldList(); //hardcoded function
		
		for(TargetField field: targetFieldList){
			defSpotList.addAll(createDefSpots(field));
		}
		
		for(DefSpot defSpot: defSpotList){
			System.out.println("body = " + defSpot.body);
			System.out.println("jimple " + defSpot.jimple);
			System.out.println();
		}
	}

	public static List<DefSpot> createDefSpots(TargetField field){
		List<DefSpot> defSpotsOfField = new ArrayList<DefSpot>();
		SootClass sootClass = Scene.v().loadClassAndSupport(field.classBelonged);
		Scene.v().loadNecessaryClasses();
//		SootField  field = sootClass.getFieldByName("favCityId");
		for(SootMethod method: sootClass.getMethods()){
			Body body = method.retrieveActiveBody();
			final PatchingChain<Unit> units = body.getUnits();
			for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
				final Stmt stmt = (Stmt) iter.next();
				for(ValueBox defBox: stmt.getDefBoxes()){
//					System.out.println(defBox.getValue().getClass().getName());
					if(defBox.getValue().getClass().getName().contains("Field") ){
						// ">" is to make sure it's not the other fields that start with the same name
						// example of defBox.getValue().toString is:  $r0.<edu.usc.yixue.weatherapp.MainActivity: java.lang.String cityName>
						if(defBox.getValue().toString().contains(field.fieldName+">")){
							if(!stmt.toString().endsWith(" = null")){
								DefSpot defSpot = new DefSpot();
								defSpot.jimple = stmt.toString();
								defSpot.body = method.getSignature();
								defSpot.nodeId = field.nodeId;
								defSpot.pkgName = field.pkgName;
								defSpot.subStrPos = field.substrPos;
								defSpotsOfField.add(defSpot);
							}
						}
					}
				}
			}
		}
		return defSpotsOfField;
	}
	
	/**
	 * hardcode the TargetField List now
	 */
	public static void add2TargetFieldList(){
		TargetField targetFiled = new TargetField();
		targetFiled.classBelonged = "edu.usc.yixue.weatherapp.MainActivity";
		targetFiled.fieldName = "cityName";
		targetFiled.nodeId = ""+299;
		targetFiled.pkgName = "edu.usc.yixue.weatherapp";
		targetFiled.substrPos = 1;

		TargetField targetFiled2 = new TargetField();
		targetFiled2.classBelonged = "edu.usc.yixue.weatherapp.MainActivity";
		targetFiled2.fieldName = "cityId";
		targetFiled2.nodeId = ""+303;
		targetFiled2.pkgName = "edu.usc.yixue.weatherapp";
		targetFiled2.substrPos = 1;
		
		
		targetFieldList.add(targetFiled);
		targetFieldList.add(targetFiled2);
	}

	public static void sootSettingAndroid(String apkPath, String androidJar){
		// prefer Android APK files// -src-prec apk
				Options.v().set_src_prec(Options.src_prec_apk);
				// output as APK, too//-f J
				Options.v().set_android_jars(androidJar);
				Options.v().set_whole_program(true);
				Options.v().set_verbose(false);
				Options.v().set_allow_phantom_refs(true);

				// sootClassPath = Scene.v().getSootClassPath() + File.pathSeparator
				// + sootClassPath;
				// Scene.v().setSootClassPath(Scene.v().getSootClassPath());
				Options.v().set_keep_line_number(true);
				Options.v().setPhaseOption("jb", "use-original-names:true");
				// resolve the PrintStream and System soot-classes

				// System.out.println("------------------------java.class.path = "+System.getProperty("java.class.path"));
				Options.v().set_soot_classpath(System.getProperty("java.class.path"));
				Options.v().set_prepend_classpath(true);

				List<String> stringlist = new LinkedList<String>();
				stringlist.add(apkPath);
				Options.v().set_process_dir(stringlist);
	}
}
