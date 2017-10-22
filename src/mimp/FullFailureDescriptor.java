package mimp;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FullFailureDescriptor extends FailureDescriptor {
	private FullFailureDescriptor(String info) {
		super(info);
	}
	
	public static int afterLastChar(String description) {
		return description.length();
	}
	
	public static String getInfo(String description) {
		String rawStackTrace = description.substring(TypeMessageFailureDescriptor.afterLastChar(description));
		return Pattern.compile("\\s")
			.splitAsStream(rawStackTrace)
			.filter(mn -> mn.startsWith(Config.GROUP_ID))
			.collect(Collectors.joining(" "));
	}
	
	public static FullFailureDescriptor forDescription(String description) {
		return new FullFailureDescriptor(TypeFailureDescriptor.getInfo(description)
				+ " "
				+ TypeMessageFailureDescriptor.getInfo(description)
				+ " "
				+ getInfo(description));
	}
}
