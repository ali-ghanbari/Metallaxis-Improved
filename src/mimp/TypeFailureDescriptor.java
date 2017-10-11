package mimp;

public class TypeFailureDescriptor extends FailureDescriptor {
	private TypeFailureDescriptor(String info) {
		super(info);
	}
	
	public static int afterLastChar(String description) {
		int indexOfLastChar = 0;
		char ch = description.charAt(indexOfLastChar);
		while(ch != ':' && !Character.isWhitespace(ch)) {
			indexOfLastChar++;
			ch = description.charAt(indexOfLastChar);
		}
		assert(indexOfLastChar > 0); //we have to at least have exception type 
		return indexOfLastChar;
	}
	
	public static String getInfo(String description) {
		final String type = description.substring(0, afterLastChar(description));
		return type;
	}

	public static TypeFailureDescriptor forDescription(String description) {
		return new TypeFailureDescriptor(getInfo(description));
	}
}
