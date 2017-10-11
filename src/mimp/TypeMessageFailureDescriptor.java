package mimp;

public class TypeMessageFailureDescriptor extends FailureDescriptor {
	private TypeMessageFailureDescriptor(String info) {
		super(info);
	}
	
	public static int afterLastChar(String description) {
		final int indexOfLastChar = description.indexOf("[STACKTRACE]");
		assert(indexOfLastChar >= 0);
		return indexOfLastChar;
	}
	
	public static String getInfo(String description) {
		final int indexOfFirstChar = TypeFailureDescriptor.afterLastChar(description) 
				+ 1 /*ignore last char of type which is either a clone or a space*/;
		final String msg = description.substring(indexOfFirstChar, afterLastChar(description)).trim();
		return msg;
	}
	
	public static TypeMessageFailureDescriptor forDescription(String description) {
		return new TypeMessageFailureDescriptor(TypeFailureDescriptor.getInfo(description) 
				+ " " 
				+ getInfo(description));
	}
}
