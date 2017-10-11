package mimp;

public class FullFailureDescriptor extends FailureDescriptor {
	private FullFailureDescriptor(String info) {
		super(info);
	}
	
	public static int afterLastChar(String description) {
		return description.length();
	}
	
	public static String getInfo(String description) {
		return description.substring(TypeMessageFailureDescriptor.afterLastChar(description));
	}
	
	public static FullFailureDescriptor forDescription(String description) {
		return new FullFailureDescriptor(TypeFailureDescriptor.getInfo(description)
				+ " "
				+ TypeMessageFailureDescriptor.getInfo(description)
				+ " "
				+ getInfo(description));
	}
}
