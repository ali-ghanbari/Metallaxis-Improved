package mimp;

public class KindFailureDescriptor extends FailureDescriptor {

	private KindFailureDescriptor() {
		super("");
	}
	
	public static KindFailureDescriptor forDescription(String description) {
		return new KindFailureDescriptor();
	}
}
