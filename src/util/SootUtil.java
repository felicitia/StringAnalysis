package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import soot.Body;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;

public class SootUtil {

	static String className = "usc.yixue.Proxy";
	static String outputFile = className+".jimple";
	static String sootClassPath = "/Users/felicitia/Documents/workspaces/Eclipse/NewStringAnalysis/src";
	
	static SootClass sootClass = null;
	static SootMethod method = null;

	static String LINE_TAG = "LineNumberTag";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		sootSetting();
		// only analyze the main method
		method = sootClass.getMethodByName("getContent");
		Body body = method.retrieveActiveBody();
		java2jimple(sootClass, body);
	}

	public static void java2jimple(SootClass sootClass, Body body){
		try {
			String fileName = SourceLocator.v().getFileNameFor(sootClass,
					Options.output_format_jimple);
			System.out
					.println("you can find the jimple file in -> " + fileName);
			OutputStream streamOut = new FileOutputStream(fileName);
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(
					streamOut));
			Printer.v().printTo(body, writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void sootSetting() {
		sootClassPath = Scene.v().getSootClassPath() + File.pathSeparator + sootClassPath;
		Scene.v().setSootClassPath(sootClassPath);
		Options.v().set_keep_line_number(true);
		sootClass = Scene.v().loadClassAndSupport(className);
		Scene.v().loadNecessaryClasses();
		sootClass.setApplicationClass();
	}
}
