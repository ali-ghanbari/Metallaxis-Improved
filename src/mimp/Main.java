package mimp;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.ejml.simple.SimpleMatrix;

import edu.CallgraphAnalysis.CallgraphConstructor;
import edu.CallgraphAnalysis.JCallGraph;

public class Main {
//	private static Set<String> buggyMethodNames = new HashSet<>();
	private static List<Method> coveredMethods = new ArrayList<>();

	public static void main(String[] args) {		
		System.out.println("loading...");
		MethodsPool.v();
		TestsPool.v();
		loadMutants();
		//TestsPool.v().pool.stream().forEach(Test::computeInfluencers);
		TestsPool.v().computeInfluencers();
		//loadBuggyMethods();
		loadCoveredMethods();
		System.out.println("doing pagerank...");
		constructMethodToMethodMatrices();
		
//		constructTestToMethodAndMethodToTestMatrices(TestsPool.v().getFailingTestNames(), true);
//		constructTestToMethodAndMethodToTestMatrices(TestsPool.v().getPassingTestNames(), false);
		doPageRank();
		
		
//		if(Config.PROG_VER() == 75) {
//			Method bm = MethodsPool.v().getMethodByFullSignature("org.apache.commons.math.stat.Frequency:getPct(Ljava/lang/Object;)D");
//			System.out.println("***************************************************");
//			System.out.println("METHOD NAME: " + bm.fullSignature);
//			System.out.println("# MUTANTS: " + bm.mutants.size());
//			int mutantNo = 1;
//			for(Mutant m : bm.mutants) {
//				System.out.println("MUTANT #" + mutantNo);
//				System.out.println("\t|T_f_e| = " + m.failingImpacts.size());
//				System.out.println("\t|T_p_e| = " + m.passingImpacts.size());
//				mutantNo++;
//			}
//			System.out.println("TOTAL NUMBER OF FAILING TESTS: " + TestsPool.v().failingTestsCount);
//			System.out.println("SUSPICIOUSNESS VALUE: " + bm.oldSusp());
//			System.out.println("***************************************************");
//		}
		
		System.out.println("ranking...");
		final String outOldFilePath = Util.joinPath(args[0],
				Config.PROG_ID(),
				"old" + Config.PROG_VER() + ".txt");
		try(PrintWriter pwOld = new PrintWriter(new FileOutputStream(outOldFilePath))) {
			final String outNewFilePath = Util.joinPath(args[0],
					Config.PROG_ID(),
					"new" + Config.PROG_VER() + ".txt");
			try(PrintWriter pwNew = new PrintWriter(new FileOutputStream(outNewFilePath))) {
				for(Method meth : coveredMethods) {
					double sOld = meth.oldSusp();
					Double newSuspO = newSusp.get(meth.fullSignature);
					double sNew = newSuspO == null ? meth.newSusp() : newSuspO.doubleValue(); ////meth.newSusp();// 
					final String oldStr = meth.fullSignature + " " + sOld;
					final String newStr = meth.fullSignature + " " + sNew;
					pwOld.println(oldStr);
					pwNew.println(newStr);
//					if(isBuggy(meth)) {
//						pwOld.println(oldStr);
//						pwNew.println(newStr);
//					} else {
//						if(sOld > 0.) {
//							pwOld.println(oldStr);
//						}
//						if(sNew > 0.) {
//							pwNew.println(newStr);
//						}
//					}
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
//	private static boolean isBuggy(Method method) {
//		return buggyMethodNames.contains(method.fullSignature);
//	}
	
	private static String constructMethodFullSignature(String description) {
		String[] parts = description.split(",\\s");
		for(int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
		}
		return parts[0] + Config.SIGNATURE_SPLITTER + parts[1] + parts[2];
	}
	
//	public static Mutant forDescription(String[] description) {		
//
//	}

	private static void loadMutants() {
		if(Config.PROG_ID().equals("Time")) {
			loadMutantsTime();
		} else if (Config.PROG_ID().equals("Closure")) {
			loadMutantsClosure();
		} else {
			loadMutantsOthers();
		}
	}
	
	private static void loadMutantsOthers() {
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID(),
				"mutation-test",
				Config.PROG_VER() + ".txt");
		Util.openTextFile(filePath);
		String line;
		String[] description = new String[0];
		while((line = Util.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("MutationDetails")) {
				if(description.length > 0) {
					/*****************************************************************/
					processMutation(description);
					description = new String[0];
				}
			}
			String[] description_ext = new String[description.length + 1];
			System.arraycopy(description, 0, description_ext, 0, description.length);
			description_ext[description.length] = line;
			description = description_ext;
		}
		Util.closeFile();
	}
	
	private static void processMutation(String[] description) {
		/*******************************processing mutation details**************************************/
		final String mutationDescription = description[0];
		if(!mutationDescription.startsWith("MutationDetails")) {
			return;
		}
		assert(mutationDescription.startsWith("MutationDetails"));
		
		final int indexOfMethodDescription = mutationDescription.indexOf("clazz=");
		assert(indexOfMethodDescription >= 0);
		final String methodDescription = mutationDescription.substring(indexOfMethodDescription,
				mutationDescription.indexOf(']', indexOfMethodDescription));
		final String mutatedMethodFullSignature = constructMethodFullSignature(methodDescription);
		final Method mutatedMethod = MethodsPool.v().getMethodByFullSignature(mutatedMethodFullSignature);

		assert(mutatedMethod != null);

		final int indexOfCoverageDescription = mutationDescription.indexOf("testsInOrder=");
		assert(indexOfCoverageDescription >= 0);
		final List<Test> coveringTests = new ArrayList<>();
		for(String testName : mutationDescription
				.substring(mutationDescription.indexOf('[', indexOfCoverageDescription) + 1,
						mutationDescription.indexOf(']', indexOfCoverageDescription))
				.split(",\\s")) {
			int index = testName.indexOf('(');
			if(index >= 0) {
				testName = testName.substring(0, index);
				Test test = TestsPool.v().getTestByName(testName);
				assert(test != null);
				coveringTests.add(test);
			}
		}
		coveringTests.sort(Test::compareTo);
		/***********************************processing exceptions****************************************/		
		Map<String, FailureDescriptor> failingTestDetails = new HashMap<>();
		final List<Test> failingTests = new ArrayList<>();
		for(int i = 1; i < description.length; i++) {
			final String testFailureDescription = description[i];
			assert(testFailureDescription.startsWith("[EXCEPTION]"));
			int index = testFailureDescription.indexOf(']') + 1;
			/*ignoring the set of white spaces*/
			while(Character.isWhitespace(testFailureDescription.charAt(index))) {
				index++;
			}
			int lastIndex = testFailureDescription.indexOf('(');
			int indexOfFalse = testFailureDescription.indexOf(" false ");
			if(lastIndex >= 0 && lastIndex < indexOfFalse) {
				final String failedTestName = testFailureDescription.substring(index,
						lastIndex);
				int startOfFailureDescription = testFailureDescription.indexOf(')') 
						+ 1 /*ignore the right parenthesis itself*/;
				/*ignoring the first set of white spaces*/
				while(Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
					startOfFailureDescription++;
				}
				/*ignore the substring "false" whose length is 5*/
				startOfFailureDescription += 5;
				/*ignoring the second set of white spaces*/
				while(Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
					startOfFailureDescription++;
				}
				final String failureDescription = testFailureDescription.substring(startOfFailureDescription);
				final FailureDescriptor failureDescriptor = FailureDescriptorFactory
						.v()
						.create(failureDescription);
				Test failedTest = TestsPool.binarySearchTestByName(coveringTests, failedTestName);
				//	assert(failedTest != null);
				if(failedTest != null) {
					failingTests.add(failedTest);
					failingTestDetails.put(failedTestName, failureDescriptor);
				}
			}
		}
		/************************************************************************************************/
		final Mutant p = new Mutant(mutatedMethod, failingTestDetails);
		//return mutant;

		/*****************************************************************/
		mutatedMethod.mutants.add(p);
		for(Test test : failingTests) {
			test.addFailurePoint(p);
		}
		for(Test test : coveringTests) {
			test.addCover(p);
		}
	}
	
	private static void loadMutantsTime() {
		List<String> gzFileNames = new ArrayList<>();
		final String path = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID(),
				Integer.toString(Config.PROG_VER()),
				"mutation-test");
		Util.listFiles(path, gzFileNames);
		try {
			for(String fileName : gzFileNames) {				
				processMutation(splitWhole(unzip(fileName)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadMutantsClosure() {
		List<String> gzFileNames = new ArrayList<>();
		final String path = Util.joinPath(Config.CLOSURE_DYN_INF_BASE_PATH,
				Integer.toString(Config.PROG_VER()),
				"mutation-test");
		Util.listFiles(path, gzFileNames);
		try {
			for(String fileName : gzFileNames) {				
				processMutation(splitWhole(unzip(fileName)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String unzip(String gzFileName) throws Exception {
		InputStream in = new GZIPInputStream(new FileInputStream(gzFileName));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String result = "";
	    String line;
	    try {
	    	while((line = br.readLine()) != null) {
	    		result += line;
	    	}
	    } catch (EOFException e) {
	    	
	    }
	    br.close();
	    if(!result.startsWith("MutationDetails")) {
	    	System.out.println(gzFileName);
	    }
	    return result;
	}
	
	private static String[] splitWhole(String whole) {
		final int offset = "[EXCEPTION] ".length();
		String[] result = new String[0];
		if(!whole.startsWith("MutationDetails [")) {
			System.out.println(whole);
		}
		int remIndex = 0;
		while(remIndex >= 0) {
			whole = whole.substring(remIndex);
			remIndex = whole.indexOf("[EXCEPTION] ", offset);
			String line;
			if(remIndex < 0) {
				line = whole.substring(0);
			} else {
				line = whole.substring(0, remIndex);
			}
			line = line.trim();
			String[] result_ext = new String[result.length + 1];
			System.arraycopy(result, 0, result_ext, 0, result.length);
			if(result.length == 0 && !line.startsWith("MutationDetails")) {
				System.out.println(line);
			}
			result_ext[result.length] = line;
			result = result_ext;
		}		
		return result;
	}
	
//	private static void loadBuggyMethods() {
//		final String filePath = Util.joinPath(Config.BUG_METH_BASE_PATH,
//				Config.PROG_ID(),
//				Config.PROG_VER() + ".txt");
//		final String prefix = "^^^^^^";
//		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//			String line;
//			System.out.println("Buggy Methods:");
//			while((line = br.readLine()) != null) {
//				line = line.trim();
//				if(line.startsWith(prefix)) {
//					final String bugMethodFullSignature = line.substring(prefix.length());
//					buggyMethodNames.add(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
//					System.out.println(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
		
	private static void loadCoveredMethods() {
		final String filePath = Util.joinPath("/home/Ali/metallaxis/xias-susp-vals/",
				Config.PROG_ID(),
				"" + Config.PROG_VER(),
				"Ochiaitype1.txt");
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while((line = br.readLine()) != null) {
				line = line.trim();
				String[] parts = line.split("\\s");
				String[] rawParts = parts[0].split("\\.");
				String methodName = rawParts[0];
				for(int i = 1; i < rawParts.length - 1; i ++) {
					methodName += "." + rawParts[i];
				}
				methodName += ":" + rawParts[rawParts.length - 1];
				
				Method meth = MethodsPool.v().getMethodByFullSignature(methodName);
				if(meth == null) {
					System.out.println(Config.PROG_VER() + " " + methodName);
				} else {
					coveredMethods.add(meth);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Map<String, Set<String>> inverseCallgraph(Map<String, Set<String>> cg) {
		final Map<String, Set<String>> inverse = new HashMap<>();
		for(Entry<String, Set<String>> calls : cg.entrySet()) {
			final String caller = calls.getKey();
			for(String callee : calls.getValue()) {
				Set<String> callers = inverse.get(callee);
				if(callers == null) {
					callers = new HashSet<>();
					inverse.put(callee, callers);
				}
				callers.add(caller);
			}
		}
		return inverse;
	}
	
	private static Map<String, Integer> methodName2Index = new HashMap<>();
	private static List<String> index2MethodName = new ArrayList<>();
	private static SimpleMatrix methodsMatrix_Callers = null;
	private static SimpleMatrix methodsMatrix_Callees = null;
	private static int methodsCount;
	
	private static void constructMethodToMethodMatrices() {
		final String classPath = Util.joinPath(Config.CLASSES_BASE_PATH,
				Config.PROG_ID(),
				"" + Config.PROG_VER(),
				"target",
				"classes");
		final CallgraphConstructor cgc = JCallGraph.init(classPath);
		final Map<String, Set<String>> cg = cgc.getCallGraph();
		final Map<String, Set<String>> cg_inv = inverseCallgraph(cg);
		if(cg == null) {
			throw new RuntimeException("invalid class path: " + classPath);
		}
		int count = 0;
		for(final String mn : cgc.getAllMethods()) {
			if(MethodsPool.v().getMethodIndexByFullSignature(mn) >= 0) {
				methodName2Index.put(mn, count++);
				index2MethodName.add(mn);
			}
		}
		final int dim = count;
		methodsCount = count;
		methodsMatrix_Callers = new SimpleMatrix(dim, dim);
		methodsMatrix_Callees = new SimpleMatrix(dim, dim);
		/*building forward links: flow from callers to the (subject) callee*/
		for(int row = 0; row < dim; row++) {
			final String subjectMethodName = index2MethodName.get(row);
			final Method subjectMethod = MethodsPool.v().getMethodByFullSignature(subjectMethodName);
			final Set<String> callers = cg_inv.get(subjectMethodName);
			if(callers == null) {
				continue; /*we rely on the initialization of SimpleMatrix's constructor*/
			}
			final int numberOfCallers = callers.size();
			final int subjectSize = subjectMethod.numberOfMutants();
			final double edgeWeight;
			if(numberOfCallers > 0) {
				edgeWeight = Math.log(1 + subjectSize) / (double) numberOfCallers;
			} else {
				edgeWeight = 0; /*this won't be used anyway, and we rely on the initialization of SimpleMatrix's constructor*/
			}
			for(final String caller : callers) {
				final Integer col = methodName2Index.get(caller);
				assert(col != null || MethodsPool.v().getMethodIndexByFullSignature(caller) < 0);
				if(col != null) {
					methodsMatrix_Callers.set(row, col.intValue(), edgeWeight);
				}
			}
		}
		/*building backward links: flow from the (subject) caller to callees*/
		for(int col = 0; col < dim; col++) {
			final String subjectMethodName = index2MethodName.get(col);
			final Method subjectMethod = MethodsPool.v().getMethodByFullSignature(subjectMethodName);
			final Set<String> callees = cg.get(subjectMethodName);
			if(callees == null) {
				continue; /*we rely on the initialization of SimpleMatrix's constructor*/
			}
			final int numberOfCallees = callees.size();
			final int subjectSize = subjectMethod.numberOfMutants();
			final double edgeWeight;
			if(numberOfCallees > 0) {
				edgeWeight = Math.log(1 + subjectSize + numberOfCallees);
			} else {
				edgeWeight = 0; /*this won't be used anyway, and we rely on the initialization of SimpleMatrix's constructor*/
			}
			for(final String callee : callees) {
				final Integer row = methodName2Index.get(callee);
				assert(row != null || MethodsPool.v().getMethodIndexByFullSignature(callee) < 0);
				if(row != null) {
					methodsMatrix_Callees.set(row.intValue(), col, edgeWeight);
				}
			}
		}
	}
	
//	for(int col = 0; col < dim; col++) {
//		final String methodName = methodNamesInMatrix.get(col);
//		final Set<String> rawCallees = cg.get(methodName);
//		if(rawCallees != null) {
//			Set<String> callees = rawCallees.stream()
//					.filter(mn -> MethodsPool.v().getMethodIndexByFullSignature(mn) >= 0)
//					.collect(Collectors.toSet());
//			final long calleesCount = callees.size();
//			final double transitionProb;
//			if(calleesCount > 0) {
//				transitionProb = 1.D / (double) calleesCount;
//			} else {
//				transitionProb = 0.; //isolated, and will be handled by teleportation vector if we want
//			}
//			final int callerIndex = col;
//			callees.forEach(callee -> {
//				final int calleeIndex = methodNamesInMatrixIndexMap.get(callee);
//				methodsMatrix.set(calleeIndex, callerIndex, transitionProb);
////				final Set<String> callers = cg_inv.get(callee);
////				final int callersCount = callers == null ? 0 : callers.size();
////				final double invTransitionProb;
////				if(callersCount > 0) {
////					invTransitionProb = 1.D / (double) callersCount;
////				} else {
////					assert(false);
////					invTransitionProb = 0.;
////				}
////				methodsMatrix.set(callerIndex, calleeIndex, invTransitionProb);
//			});
//		}
//	}
	
//	private static SimpleMatrix testToMethodMatrix = null;
//	private static SimpleMatrix methodToTestMatrix = null;
//	private static Map<String, Integer> testNameIndexMap = new HashMap<>();
//	
//	private static void constructTestToMethodAndMethodToTestMatrices(Set<String> testNames, boolean failing) {
//		int testNamesCount = 0;
//		for(String tn : testNames) {
//			if(TestsPool.v().getTestByName(tn) != null) {
//				testNameIndexMap.put(tn, testNamesCount++);
//			}
//		}
//		testToMethodMatrix = new SimpleMatrix(methodToMethodMatrix.numRows(), testNamesCount);
//		methodToTestMatrix = new SimpleMatrix(testNamesCount, methodToMethodMatrix.numCols());
//		for(String tn : testNames) {
//			Set<Method> influencerMethods = TestsPool.v().getTestByName(tn).influencerMethods;
//			final double fromTestTransitionProb;
//			final int influencerMethodsCount = influencerMethods.size();
//			if(influencerMethodsCount > 0) {
//				fromTestTransitionProb = 1.D / (double) influencerMethodsCount;
//			} else {
//				fromTestTransitionProb = 0.; //isolated tests contribute nothing
//			}
//			final int testIndex = testNameIndexMap.get(tn);
//			influencerMethods.stream().forEach(method -> {
////				for(String s : methodNamesInMatrixMap.keySet())
////					System.out.println(s + "\t" + method.fullSignature);
//				final int methodIndex = methodNamesInMatrixMap.get(method.fullSignature);
//				testToMethodMatrix.set(methodIndex, testIndex, fromTestTransitionProb);
//			});
//		}
//		methodNamesInMatrix.stream().map(mn -> MethodsPool.v().getMethodByFullSignature(mn)).forEach(method -> {
//			final Set<String> influencedTestNames = new HashSet<>();
//			for(Mutant mutant : method.mutants) {
//				List<Test> influencedTests = failing ? mutant.failingImpacts : mutant.passingImpacts;
//				influencedTests.stream().forEach(test -> {
//					influencedTestNames.add(test.name);
//				});
//			}
//			final int influencedTestsCount = influencedTestNames.size();
//			final double fromMethodTransitionProb;
//			if(influencedTestsCount > 0) {
//				fromMethodTransitionProb = 1.D / (double) influencedTestsCount;
//			} else {
//				fromMethodTransitionProb = 0.; //isolated, and will be handled by teleportation vector if we want
//			}
//			final int methodIndex = methodNamesInMatrixMap.get(method.fullSignature);
//			influencedTestNames.stream().mapToInt(itn -> testNameIndexMap.get(itn)).forEach(testIndex -> {
//				methodToTestMatrix.set(testIndex, methodIndex, fromMethodTransitionProb);
//			});
//		});
//	}
//	
	
	private static Map<String, Double> newSusp = new HashMap<>();
	
	private static SimpleMatrix doPageRank(SimpleMatrix p, SimpleMatrix v, double dampingFactor, double alpha) {
		p = p.scale(alpha);
		SimpleMatrix x = v.copy();
		for(int __ = 0; __ < 25; __++) {
			x = p.mult(x).scale(dampingFactor).plus(v.scale(1 - dampingFactor));
		}
		return x;
	}
	
	private static void doPageRank() {
		final SimpleMatrix v = new SimpleMatrix(methodsCount, 1);
		for(int row = 0; row < methodsCount; row++) {
			final String methodName = index2MethodName.get(row);
			final Method method = MethodsPool.v().getMethodByFullSignature(methodName);
			v.set(row, 0, method.newSusp());
		}
		SimpleMatrix forwardSusp = doPageRank(methodsMatrix_Callers, v, 0.85, 0.1);
		SimpleMatrix backwardSusp = doPageRank(methodsMatrix_Callees, v, 0.85, 0.1);
		SimpleMatrix overalSusp = forwardSusp.plus(backwardSusp);//.minus(v);
		for(int row = 0; row < methodsCount; row++) {
			final String methodName = index2MethodName.get(row);
			newSusp.put(methodName, overalSusp.get(row, 0));
		}
	}
}
