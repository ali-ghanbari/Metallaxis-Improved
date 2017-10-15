package mimp;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
	private static Set<String> buggyMethodNames = new HashSet<>();

	public static void main(String[] args) {
		System.out.println("loading...");
		MethodsPool.v();
		TestsPool.v();
		loadMutants();
		TestsPool.v().pool.stream().forEach(Test::computeInfluencers);
		loadBuggyMethods();
		
		if(Config.PROG_VER() == 12) {
			Method bm = MethodsPool.v().getMethodByFullSignature("org.jfree.chart.plot.MultiplePiePlot:<init>(Lorg/jfree/data/category/CategoryDataset;)V");
			System.out.println("***************************************************");
			System.out.println("METHOD NAME: " + bm.fullSignature);
			System.out.println("# MUTANTS: " + bm.mutants.size());
			int mutantNo = 1;
			for(Mutant m : bm.mutants) {
				System.out.println("MUTANT #" + mutantNo);
				System.out.println("\t|T_f_e| = " + m.failingImpacts.size());
				System.out.println("\t|T_p_e| = " + m.passingImpacts.size());
				mutantNo++;
			}
			System.out.println("TOTAL NUMBER OF FAILING TESTS: " + TestsPool.v().failingTestsCount);
			System.out.println("SUSPICIOUSNESS VALUE: " + bm.susp());
			System.out.println("***************************************************");
		}
		
		System.out.println("ranking...");
		final String outFilePath = Util.joinPath(args[0],
				Config.PROG_ID(),
				Config.PROG_VER() + ".txt");
		try(PrintWriter pw = new PrintWriter(new FileOutputStream(outFilePath))) {
			for(Method meth : MethodsPool.v().pool) {
				double s = meth.susp();
				if(isBuggy(meth) || s > 0.) {
					
					pw.println(meth.fullSignature + " " + s);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	private static boolean isBuggy(Method method) {
		return buggyMethodNames.contains(method.fullSignature);
	}
	
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
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID(),
				"mutation-test",
				Config.PROG_VER() + ".txt");
		Util.openFile(filePath);
		String line;
		String[] description = new String[0];
		while((line = Util.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("MutationDetails")) {
				if(description.length > 0) {
					/*****************************************************************/
					/*******************************processing mutation details**************************************/
					final String mutationDescription = description[0];
					assert(mutationDescription.startsWith("MutationDetails"));
					
					final int indexOfMethodDescription = mutationDescription.indexOf("clazz=");
					assert(indexOfMethodDescription >= 0);
					final String methodDescription = mutationDescription.substring(indexOfMethodDescription,
							mutationDescription.indexOf(']', indexOfMethodDescription));
					final String mutatedMethodFullSignature = constructMethodFullSignature(methodDescription);
					final Method mutatedMethod = MethodsPool.v().getMethodByFullSignature(mutatedMethodFullSignature);
					if(mutatedMethod == null) {
						System.out.println(mutatedMethodFullSignature);
					}
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
						if(lastIndex >= 0) {
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
					final Mutant p = new Mutant(failingTestDetails);
					//return mutant;
					
					/*****************************************************************/
					mutatedMethod.mutants.add(p);
					for(Test test : failingTests) {
						test.addFailurePoint(p);
					}
					for(Test test : coveringTests) {
						test.addCover(p);
					}
					description = new String[0];
				}
			}
			String[] description_ext = new String[description.length + 1];
			System.arraycopy(description, 0, description_ext, 0, description.length);
			description_ext[description.length] = line;
			description = description_ext;
		}
		Util.closeFile();
//		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private static void loadBuggyMethods() {
		final String filePath = Util.joinPath(Config.BUG_METH_BASE_PATH,
				Config.PROG_ID(),
				Config.PROG_VER() + ".txt");
		final String prefix = "^^^^^^";
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			System.out.println("Buggy Methods:");
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith(prefix)) {
					final String bugMethodFullSignature = line.substring(prefix.length());
					buggyMethodNames.add(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
					System.out.println(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
