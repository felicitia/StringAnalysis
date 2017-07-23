package usc.yixue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.Files;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import util.FileUtil;

public class JimpleHelper {
//	static private Set<SootClass> appclasses = new HashSet<SootClass>();
//	static private Set<String> methodsignatures = new HashSet<String>();
//	static private Set<SootMethod> allmethods = new HashSet<SootMethod>();
	final static String appFolder = "/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/SmallApps/App171/sootOutput";
//	final static String androidJar = "/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/Android";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("counter = "+urlCounter());
//		String FINALIZER_CLASS_NAME = "com.google.inject.internal.util.$Finalizer";
//		String finalizerPath = FINALIZER_CLASS_NAME.replace('.', '/') + ".class";
//		System.out.println(finalizerPath.length());
		
//		moveTargetJimple("/Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/SmallApps");
	}



	public static int urlCounter() {
		int counter = 0;
		File[] files = FileUtil.getFilesWithExtension(appFolder, ".jimple");
		//
		// CHATransformer.v().transform();
		for (File file : files) {
			if (file.isFile()) {
				System.out.println("filename = "+file.getName());
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {

					String sCurrentLine;

					while ((sCurrentLine = br.readLine()) != null) {
						if(sCurrentLine.contains("<java.net.URL: void <init>(java.lang.String)>")){
							System.out.println(sCurrentLine);
							counter++;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		return counter;
	}

	/**
	 * copy the target jimple files from sootOutput to ../targetJimple 
	 * @param dirpath
	 */
	public static void copyTargetJimple(String dirpath){
		Set<String> targetJimples = new HashSet<String>();
		File rootDir = new File(dirpath);
		String[] appDirs = rootDir.list();
		for(String appDir: appDirs){
			if(!appDir.startsWith("App")){
				continue;
			}
			String fileName = dirpath+"/"+appDir+"/statistic.txt";
			try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					if(sCurrentLine.startsWith("Method Name:")){
						String[] tokens = sCurrentLine.replace("Method Name: <", "").split(":");
						String jimpleName = tokens[0];
						targetJimples.add(jimpleName);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			File sootOutDir = new File(dirpath+"/"+appDir+"/sootOutput");
			for(File jimpleFile: sootOutDir.listFiles()){
				String jimpleName = jimpleFile.getName().replace(".jimple", "");
				if(targetJimples.contains(jimpleName)){
//					System.out.println(jimpleName);
					File targetJimpleFolder = new File(dirpath+"/"+appDir+"/targetJimple");
					if(!targetJimpleFolder.exists()){
						targetJimpleFolder.mkdir();
					}
					try {
						Files.copy(jimpleFile, new File(dirpath+"/"+appDir+"/targetJimple/"+jimpleFile.getName()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Done! :)");
	}
//	public static void sootSettingAndroid(String apkPath, String androidJar) {
//		// prefer Android APK files// -src-prec apk
//		Options.v().set_src_prec(Options.src_prec_apk);
//		// output as APK, too//-f J
//		Options.v().set_android_jars(androidJar);
//		Options.v().set_whole_program(true);
//		Options.v().set_verbose(false);
//		Options.v().set_allow_phantom_refs(true);
//
//		// sootClassPath = Scene.v().getSootClassPath() + File.pathSeparator
//		// + sootClassPath;
//		// Scene.v().setSootClassPath(Scene.v().getSootClassPath());
//		Options.v().set_keep_line_number(true);
//		Options.v().setPhaseOption("jb", "use-original-names:true");
//		// resolve the PrintStream and System soot-classes
//
//		// System.out.println("------------------------java.class.path = "+System.getProperty("java.class.path"));
//		Options.v().set_soot_classpath(System.getProperty("java.class.path"));
//		Options.v().set_prepend_classpath(true);
//
//		List<String> stringlist = new LinkedList<String>();
//		stringlist.add(apkPath);
//		Options.v().set_process_dir(stringlist);
//	}
}
