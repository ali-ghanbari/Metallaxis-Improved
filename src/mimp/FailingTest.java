package mimp;

public class FailingTest extends Test {
	public final FailureDescriptor descriptor;
	
	public FailingTest(String name, FailureDescriptor descriptor) {
		super(name);
		this.descriptor = descriptor;
	}
}
