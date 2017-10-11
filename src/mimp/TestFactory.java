package mimp;

public class TestFactory {
	private static TestFactory instance = null;
	
	private TestFactory() {
		
	}
	
	public static TestFactory v() {
		if(instance == null) {
			instance = new TestFactory();
		}
		return instance;
	}
	
	public Test create(String description) {
		final int firstLeftParenthesis = description.indexOf('(');
		assert(firstLeftParenthesis >= 0);
		final String testName = description.substring(0, firstLeftParenthesis);
		if(description.endsWith("PASS")) {
			return new PassingTest(testName);
		}
		final int firstRightParenthesis = description.indexOf(')');
		assert(firstRightParenthesis > firstLeftParenthesis);
		int startOfFailureDescription = firstRightParenthesis 
				+ 1 /*ignore the right parenthesis itself*/;
		/*ignoring the first set of white spaces*/
		while(Character.isWhitespace(description.charAt(startOfFailureDescription))) {
			startOfFailureDescription++;
		}
		/*ignore the substring "false" whose length is 5*/
		startOfFailureDescription += 5;
		/*ignoring the second set of white spaces*/
		while(Character.isWhitespace(description.charAt(startOfFailureDescription))) {
			startOfFailureDescription++;
		}
		final String failureDescription = description.substring(startOfFailureDescription);
		final FailureDescriptor failureDescriptor = FailureDescriptorFactory
				.v()
				.create(failureDescription);
		return new FailingTest(testName, failureDescriptor);
	}
}
