package mimp;

public class FailingTest extends Test {
	public final FailureDescriptor descriptor;
	
	public FailingTest(String name, FailureDescriptor descriptor) {
		super(name);
		this.descriptor = descriptor;
	}

	@Override
	public void computeInfluencers() {
		for(Mutant failurePoint : failurePoints) {
			assert(failurePoint.failingTestDetails.get(name) != null);
			if(!descriptor.equals(failurePoint.failingTestDetails.get(name))) {
				addInfluencer(failurePoint);
				failurePoint.failingImpacts.add(this);
			}
		}
		for(Mutant covered : cover) {
			if(covered.failingTestDetails.get(name) == null) {
				addInfluencer(covered);
				covered.failingImpacts.add(this);
			}
		}
	}
}
