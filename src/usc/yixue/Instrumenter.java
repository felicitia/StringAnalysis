package usc.yixue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class Instrumenter {

	private static int tmpCount = 0;
	
	static String newAppDir = null; //arg0
	static String androidJar = null; //arg1
	static String appfolder = null; //arg2

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		newAppDir = args[0];
		androidJar = args[1];
		appfolder = args[2];
		
		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(newAppDir);
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
		stringlist.add(args[2]);
		Options.v().set_process_dir(stringlist);

		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

		Scene.v().addBasicClass(ProxyHelper.ProxyClass);
		SootClass scc = Scene.v().loadClassAndSupport(ProxyHelper.ProxyClass);
		scc.setApplicationClass();

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

					@Override
					protected void internalTransform(final Body body,
							String phaseName,
							@SuppressWarnings("rawtypes") Map options) {
//						instrumentAll(body);
						 instrumentTimestamp(body, ProxyHelper.getInputStreamOriginal);
						body.validate();
					}

				}));

		soot.Main.main(args);
	}

	/**
	 * instrument timestamps before "sig" and after "sig"
	 * @param body
	 * @param sig
	 */
	private static void instrumentTimestamp(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				InvokeExpr invoke = stmt.getInvokeExpr();
				if (invoke
						.getMethod()
						.getSignature()
						.equals(sig)) {
					SootMethod printTimeStamp = ProxyHelper
							.findMethod(ProxyHelper.printTimeStamp);
					System.out.println("@@@@@@@@@@ sm = " + printTimeStamp.getSignature());
					Stmt newinvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(printTimeStamp.makeRef()));
					Stmt newinvoke2 = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(printTimeStamp.makeRef()));
					units.insertBefore(newinvoke, stmt);
					units.insertAfter(newinvoke2, stmt);
				}
			}
		}

	}

	// private static Local addTmpRef(Body body) {
	// Local tmpRef = Jimple.v().newLocal("tmpRef",
	// RefType.v("java.io.PrintStream"));
	// body.getLocals().add(tmpRef);
	// return tmpRef;
	// }
	//
	private static Local addTmpString2Local(Body body) {
		Local tmpString = Jimple.v().newLocal("tmpString" + (tmpCount++),
				RefType.v("java.lang.String"));
		body.getLocals().add(tmpString);
		return tmpString;
	}

	private static Local addTmpInt2Local(Body body) {
		Local tmpInt = Jimple.v().newLocal("tmpInt" + (tmpCount++),
				RefType.v("java.lang.Integer"));
		body.getLocals().add(tmpInt);
		return tmpInt;
	}

	public static void instrumentWithBody(Body body) {
		// find the body that needs to be instrumented (replace
		// methods)
		if (ProxyHelper.instrumentMap.containsKey(body.getMethod()
				.getSignature())) {
			final PatchingChain<Unit> units = body.getUnits();
			// important to use snapshotIterator here
			for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
				final Stmt stmt = (Stmt) iter.next();
				if (stmt.containsInvokeExpr()) {
					InvokeExpr invoke = stmt.getInvokeExpr();
					// instrumentTimestamp(invoke, stmt, units);
					SootMethod replaceMethod = ProxyHelper
							.queryReplaceMethodWithBody(invoke.getMethod()
									.getSignature());
					// System.out.println("replacement@@@@@@@@@@"+replaceMethod);
					if (replaceMethod != null) {
						// System.out.println("replacement ============ "+replaceMethod.getSignature());
						List<Value> arglist = new LinkedList<Value>();
						for (ValueBox vb : invoke.getUseBoxes()) {
							arglist.add(vb.getValue());
						}
						// Jimple.v().newStaticInvokeExpr(agent.makeRef())
						if (stmt instanceof AssignStmt) {
							Value assivalue = ((AssignStmt) stmt).getLeftOp();
							Stmt newassign = Jimple.v().newAssignStmt(
									assivalue,
									Jimple.v().newStaticInvokeExpr(
											replaceMethod.makeRef(), arglist));
							units.insertBefore(newassign, stmt);
							units.remove(stmt);

						} else if (stmt instanceof InvokeStmt) {
							Stmt newinvoke = Jimple.v().newInvokeStmt(
									Jimple.v().newStaticInvokeExpr(
											replaceMethod.makeRef(), arglist));
							units.insertBefore(newinvoke, stmt);
							units.remove(stmt);

						}
					}
				}
			}
		}
	}

	public static void instrumentPrefetch(Body body) {
		String bodySig = body.getMethod().getSignature();
		String triggerMethodWithId = getSigWithId(bodySig,
				AnalysisHelper.getTriggerMethods(appfolder));
		if (triggerMethodWithId != null) {
			// nodeIds example: 303#299#307
			String nodeIds = triggerMethodWithId.replace(bodySig + "#", "");
			final PatchingChain<Unit> units = body.getUnits();
			// important to use snapshotIterator here
			for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
				final Stmt stmt = (Stmt) iter.next();
				if (stmt instanceof ReturnStmt
						|| stmt instanceof ReturnVoidStmt) {
					SootClass ProxyClass = Scene.v().loadClassAndSupport(
							ProxyHelper.ProxyClass);
					SootMethod triggerPrefetch = ProxyClass
							.getMethod(ProxyHelper.triggerPrefetch);
					if (triggerPrefetch == null) {
						System.out
								.println(" @@@@@@@@@@@ triggerPrefetch method is null @@@@@@@@!!!");
					} else {
						LinkedList<Value> arglist = new LinkedList<Value>();
						arglist.add(StringConstant.v(nodeIds));
						Stmt triggerPrefetchInvoke = Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(
										triggerPrefetch.makeRef(), arglist));
						units.insertBefore(triggerPrefetchInvoke, stmt);
					}
				}
			}
		}
	}

	private static String getSigWithId(String bodySig,
			Set<String> triggerMethods) {
		for (String triggerMethod : triggerMethods) {
			if (triggerMethod.contains(bodySig)) {
				return triggerMethod;
			}
		}
		return null;
	}

	public static void instrumentSendDef(Body body) {
		if (ProxyHelper.defSpotMap.containsKey(body.getMethod().getSignature())) {
			final PatchingChain<Unit> units = body.getUnits();
			final DefSpot defSpot = ProxyHelper.defSpotMap.get(body.getMethod()
					.getSignature());
			// important to use snapshotIterator here
			for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
				final Stmt stmt = (Stmt) iter.next();
				if (stmt.toString().equals(defSpot.getJimple())) {
					SootClass ProxyClass = Scene.v().loadClassAndSupport(
							ProxyHelper.ProxyClass);
					SootMethod sendDefMethod = ProxyClass
							.getMethod(ProxyHelper.sendDef);
					if (sendDefMethod == null) {
						System.out
								.println(" !!!!!!!!!!!!!!!!!!!!!!!!!!!send def method is null!!!");
					} else {
						if (!(stmt instanceof AssignStmt)) {
							System.out
									.println("@@@@@@@@@@ stmt is not AssignStmt");
						}
						AssignStmt targetAssign = (AssignStmt) stmt;
						Value value = targetAssign.getRightOp();
						// Local index = addTmpInt2Local(body);
						// Local nodeId = addTmpInt2Local(body);
						// AssignStmt assignIndex =
						// Jimple.v().newAssignStmt(index, IntConstant.v(1));
						// AssignStmt assignNodeId =
						// Jimple.v().newAssignStmt(nodeId, IntConstant.v(307));
						LinkedList<Value> arglist = new LinkedList<Value>();
						arglist.add(value);
						arglist.add(IntConstant.v(307));
						arglist.add(IntConstant.v(1));
						arglist.add(StringConstant
								.v("edu.usc.yixue.weatherapp"));
						Stmt sendDefInvoke = Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(
										sendDefMethod.makeRef(), arglist));
						// units.insertBefore(assignIndex, targetAssign);
						// units.insertBefore(assignNodeId, targetAssign);
						units.insertBefore(sendDefInvoke, targetAssign);
					}
				}
			}
		}
	}

	/**
	 * this method will do the following 
	 * 1. insert sendDef(...) 
	 * 2. insert triggerPrefetch(...) before each return statement in each trigger method
	 * 3. add timestamp before and after each getInputStream
	 * 4. replace all the getInputStream()
	 * @param body
	 */
	public static void instrumentAll(Body body) {
		instrumentSendDef(body);
		instrumentPrefetch(body);
		instrumentTimestamp(body, ProxyHelper.getInputStreamOriginal);
		final PatchingChain<Unit> units = body.getUnits();
		// important to use snapshotIterator here
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				InvokeExpr invoke = stmt.getInvokeExpr();
				// instrumentTimestamp(invoke, stmt, units);
				SootMethod replaceMethod = ProxyHelper
						.queryReplaceMethod(invoke.getMethod().getSignature());
				// System.out.println("replacement@@@@@@@@@@" + replaceMethod);
				if (replaceMethod != null) {
					System.out.println("replacement:\n "
							+ replaceMethod.getSignature());
					List<Value> arglist = new LinkedList<Value>();
					for (ValueBox vb : invoke.getUseBoxes()) {
						arglist.add(vb.getValue());
					}
					// Jimple.v().newStaticInvokeExpr(agent.makeRef())
					if (stmt instanceof AssignStmt) {
						Value assivalue = ((AssignStmt) stmt).getLeftOp();
						Stmt newassign = Jimple.v().newAssignStmt(
								assivalue,
								Jimple.v().newStaticInvokeExpr(
										replaceMethod.makeRef(), arglist));
						units.insertBefore(newassign, stmt);
						units.remove(stmt);

					} else if (stmt instanceof InvokeStmt) {
						Stmt newinvoke = Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(
										replaceMethod.makeRef(), arglist));
						units.insertBefore(newinvoke, stmt);
						units.remove(stmt);

					}
				}
			}
		}
	}

}
