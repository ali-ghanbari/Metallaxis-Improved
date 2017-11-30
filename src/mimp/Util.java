package mimp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class Util {
	private static String remaining;
	private static BufferedReader br;
	
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
	
	public static void openTextFile(String fileName) {
		try {
			br = new BufferedReader(new FileReader(fileName), 4096);
			
			remaining = "";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void closeFile() {
		try {
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String readLine() {
		String text = remaining;
		String result = null;
		try {
			String line = br.readLine();
			if(line == null) {
				if(text.isEmpty()) {
					return null;
				}
				if(!text.startsWith("MutationDetails [") && !text.startsWith("[EXCEPTION] ")) {
					assert(false);
					return text;
				}
			}
			if(line != null) {
				text += line.trim();
			}
			assert(text.startsWith("MutationDetails [") || text.startsWith("[EXCEPTION] "));
			while(text.indexOf("MutationDetails [", 1) < 0 && text.indexOf("[EXCEPTION] ", 1) < 0) {
				line = br.readLine();
				if(line != null) {
					text += line.trim();
				} else {
					break;
				}
			}
			int upper = text.indexOf("MutationDetails [", 1);
			if(upper < 0) {
				upper = text.indexOf("[EXCEPTION] ", 1);
			}
			if(upper < 0) {
				upper = text.length();
			}
			result = text.substring(0, upper);
			remaining = text.substring(upper);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void listFiles(String path, List<String> fileList) {
        File directory = new File(path);
        File[] fList = directory.listFiles();
        for(File file : fList) {
            if(file.isFile()) {
                fileList.add(file.getAbsolutePath());
            } else if(file.isDirectory()) {
            	listFiles(file.getAbsolutePath(), fileList);
            }
        }
    }
}
