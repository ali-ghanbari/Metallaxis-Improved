package mimp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Mutant {
	public final Map<String, FailureDescriptor> failingTestDetails;

	public final List<Test> failingImpacts;
	
	public final List<Test> passingImpacts;
	
	public Mutant(Map<String, FailureDescriptor> failingTestDetails) {
		this.failingTestDetails = failingTestDetails;
		failingImpacts = new ArrayList<>();
		passingImpacts = new ArrayList<>();
	}
	
	public double oldSusp() {
		if(failingImpacts.isEmpty() && passingImpacts.isEmpty()) {
			return 0;
		}
		double T_f_e = failingImpacts.size();//.stream().mapToDouble(Test::rank).sum(); //
		double T_p_e = passingImpacts.size();//.stream().mapToDouble(Test::rank).sum(); //
		double T_f = TestsPool.v().failingTestsCount;//.failingTestsRank(); //
		//if(T_f > 0. && (T_f_e > 0. || T_p_e > 0.)) {
			return T_f_e / Math.sqrt((T_f_e + T_p_e) * T_f);
		//}
		//return 0;
	}
	
	public double newSusp() {
//		if(failingImpacts.isEmpty() && passingImpacts.isEmpty()) {
//			return 0;
//		}
		double T_f_e = failingImpacts.stream().mapToDouble(Test::rank).sum(); //.size();//
		double T_p_e = passingImpacts.stream().mapToDouble(Test::rank).sum(); //.size();//
		double T_f = TestsPool.v().failingTestsRank(); //.failingTestsCount;//
		if(T_f > 0. && (T_f_e > 0. || T_p_e > 0.)) {
			return T_f_e / Math.sqrt((T_f_e + T_p_e) * T_f);
		}
		return 0;
	}	
}


//public class Mutant {
//	/*'failingTestDetails' maps test name to a failure-descriptor which describes the way the test is failed
//	 * due to the mutation corresponding to this mutant*/
//	public final Map<String, FailureDescriptor> failingTestDetails;
//	/*dom(failingTestDetails) = map(Test:getName, failingTests)*/
//	public final List<Test> failingTests;
//	/*'coveringTests' lists the set of all tests that cover this mutant*/
//	public final List<Test> coveringTests;
//	/*class invariant 1: for each test name tn in dom(failingTestDetails) there is some test t in
//	 * coveringTests such that t.name == tn*/
//	/*class invariant 2: for each test t in coveringTests, t in not equal to null*/
//	public final Method mutatedMethod;
//	
//	public final List<Test> failingImpacts;
//	
//	public final List<Test> passingImpacts;
//	
//	private Mutant(Method mutatedMethod,
//			List<Test> coveringTests,
//			List<Test> failingTests,
//			Map<String, FailureDescriptor> failingTestDetails) {
//		this.mutatedMethod = mutatedMethod;
//		this.failingTests = failingTests;
//		this.failingTestDetails = failingTestDetails;
//		this.coveringTests = coveringTests;
//		failingImpacts = new ArrayList<>();
//		passingImpacts = new ArrayList<>();
//	}
//	
//	public double susp() {
//		if(failingImpacts.isEmpty() && passingImpacts.isEmpty()) {
//			return 0;
//		}
//		double T_f_e = failingImpacts.size();//.stream().mapToDouble(Test::rank).sum(); //
//		double T_p_e = passingImpacts.size();//.stream().mapToDouble(Test::rank).sum(); //
//		double T_f = TestsPool.v().failingTestsCount;//.failingTestsRank(); //
//		//if(T_f > 0. && (T_f_e > 0. || T_p_e > 0.)) {
//			return T_f_e / Math.sqrt((T_f_e + T_p_e) * T_f);
//		//}
//		//return 0;
//		
//	}
//	
//	private static String constructMethodFullSignature(String description) {
//		String[] parts = description.split(",\\s");
//		for(int i = 0; i < parts.length; i++) {
//			parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
//		}
//		return parts[0] + Config.SIGNATURE_SPLITTER + parts[1] + parts[2];
//	}
//	
//	public static Mutant forDescription(String[] description) {		
//		/*******************************processing mutation details**************************************/
//		final String mutationDescription = description[0];
//		assert(mutationDescription.startsWith("MutationDetails"));
//		
//		final int indexOfMethodDescription = mutationDescription.indexOf("clazz=");
//		assert(indexOfMethodDescription >= 0);
//		final String methodDescription = mutationDescription.substring(indexOfMethodDescription,
//				mutationDescription.indexOf(']', indexOfMethodDescription));
//		final String mutatedMethodFullSignature = constructMethodFullSignature(methodDescription);
//		final Method mutatedMethod = MethodsPool.v().getMethodByFullSignature(mutatedMethodFullSignature);
//		if(mutatedMethod == null) {
//			System.out.println(mutatedMethodFullSignature);
//		}
//		assert(mutatedMethod != null);
//		
//		final int indexOfCoverageDescription = mutationDescription.indexOf("testsInOrder=");
//		assert(indexOfCoverageDescription >= 0);
//		final List<Test> coveringTests = new ArrayList<>();
//		for(String testName : mutationDescription
//				.substring(mutationDescription.indexOf('[', indexOfCoverageDescription) + 1,
//						mutationDescription.indexOf(']', indexOfCoverageDescription))
//				.split(",\\s")) {
//			int index = testName.indexOf('(');
//			if(index >= 0) {
//				testName = testName.substring(0, index);
//				Test test = TestsPool.v().getTestByName(testName);
//				assert(test != null);
//				coveringTests.add(test);
//			}
//		}
//		coveringTests.sort(Test::compareTo);
//		/***********************************processing exceptions****************************************/		
//		Map<String, FailureDescriptor> failingTestDetails = new HashMap<>();
//		final List<Test> failingTests = new ArrayList<>();
//		for(int i = 1; i < description.length; i++) {
//			final String testFailureDescription = description[i];
//			assert(testFailureDescription.startsWith("[EXCEPTION]"));
//			
//			int index = testFailureDescription.indexOf(']') + 1;
//			/*ignoring the set of white spaces*/
//			while(Character.isWhitespace(testFailureDescription.charAt(index))) {
//				index++;
//			}
//			int lastIndex = testFailureDescription.indexOf('(');
//			if(lastIndex >= 0) {
//				final String failedTestName = testFailureDescription.substring(index,
//					lastIndex);
//				int startOfFailureDescription = testFailureDescription.indexOf(')') 
//						+ 1 /*ignore the right parenthesis itself*/;
//				/*ignoring the first set of white spaces*/
//				while(Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
//					startOfFailureDescription++;
//				}
//				/*ignore the substring "false" whose length is 5*/
//				startOfFailureDescription += 5;
//				/*ignoring the second set of white spaces*/
//				while(Character.isWhitespace(testFailureDescription.charAt(startOfFailureDescription))) {
//					startOfFailureDescription++;
//				}
//				final String failureDescription = testFailureDescription.substring(startOfFailureDescription);
//				final FailureDescriptor failureDescriptor = FailureDescriptorFactory
//						.v()
//						.create(failureDescription);
//				Test failedTest = TestsPool.binarySearchTestByName(coveringTests, failedTestName);
//			//	assert(failedTest != null);
//				if(failedTest != null) {
//					failingTests.add(failedTest);
//					failingTestDetails.put(failedTestName, failureDescriptor);
//				}
//			}
//		}
//		/************************************************************************************************/
//		final Mutant mutant = new Mutant(mutatedMethod, coveringTests, failingTests, failingTestDetails);
//		return mutant;
//	}
//}
