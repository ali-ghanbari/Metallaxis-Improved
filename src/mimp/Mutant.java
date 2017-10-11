package mimp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mutant {
	/*'impactDetails' maps test name to a failure-descriptor which describes the way the test is failed
	 * due to the mutation corresponding to this mutant*/
	public final Map<String, FailureDescriptor> impactDetails;
	/*dom(impactDetails) = map(Test":getName, impacts)*/
	public final List<Test> impacts;
	/*'coveringTests' lists the set of all tests that cover this mutant*/
	public final List<Test> coveringTests;
	/*class invariant 1: for each test name tn in dom(impactDetails) there is some test t in
	 * coveringTests such that t.name == tn*/
	/*class invariant 2: for each test t in coveringTests, t in not equal to null*/
	
	private Mutant(List<Test> coveringTests,
			List<Test> impacts,
			Map<String, FailureDescriptor> impactDetails) {
		this.impacts = impacts;
		this.impactDetails = impactDetails;
		this.coveringTests = coveringTests;
	}
	
	private static String constructMethodFullSignature(String description) {
		String[] parts = description.split(",\\s");
		for(int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].substring(parts[i].indexOf('=') + 1);
		}
		return parts[0] + Config.SIGNATURE_SPLITTER + parts[1] + parts[2];
	}
	
	public static ImmutablePair<Mutant, Method> forDescription(String[] description) {		
		/*******************************processing mutation details**************************************/
		final String mutationDescription = description[0];
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
			testName = testName.substring(0, testName.indexOf('('));
			Test test = TestsPool.v().getTestByName(testName);
			assert(test != null);
			coveringTests.add(test);
		}
		coveringTests.sort(Test::compareTo);
		/***********************************processing exceptions****************************************/		
		Map<String, FailureDescriptor> impactDetails = new HashMap<>();
		final List<Test> impacts = new ArrayList<>();
		for(int i = 1; i < description.length; i++) {
			final String testFailureDescription = description[i];
			assert(testFailureDescription.startsWith("[EXCEPTION]"));
			
			int index = testFailureDescription.indexOf(']') + 1;
			/*ignoring the set of white spaces*/
			while(Character.isWhitespace(testFailureDescription.charAt(index))) {
				index++;
			}
			final String failedTestName = testFailureDescription.substring(index,
					testFailureDescription.indexOf('(', index));
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
			assert(failedTest != null);
			impacts.add(failedTest);
			impactDetails.put(failedTestName, failureDescriptor);
		}
		/************************************************************************************************/
		return new ImmutablePair<Mutant, Method>(new Mutant(coveringTests, impacts, impactDetails), mutatedMethod);
	}
}
