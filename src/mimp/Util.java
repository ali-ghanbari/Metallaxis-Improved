package mimp;

import java.io.File;

public class Util {
	public static String joinPath(String... pathElements) {
		String path = "";
		for(int i = 0; i < pathElements.length; i++) {
			String element = pathElements[i];
			path += element;
			if(!element.endsWith(File.separator) && (i + 1) < pathElements.length) {
				path += File.separator;
			}
		}
		return path;
	}
}
