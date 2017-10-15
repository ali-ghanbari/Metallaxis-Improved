package mimp;

public class PassingTest extends Test {
	public PassingTest(String name) {
		super(name);
	}

	@Override
	public void computeInfluencers() {
		for(Mutant failurePoint : failurePoints) {
			addFailurePoint(failurePoint);
			addInfluencer(failurePoint);
			failurePoint.passingImpacts.add(this);
		}
	}
}
