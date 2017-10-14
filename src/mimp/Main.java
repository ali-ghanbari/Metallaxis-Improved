package mimp;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Main {
	private static Set<String> buggyMethodNames = new HashSet<>();

	public static void main(String[] args) {
		System.out.println("loading...");
		MethodsPool.v();
		TestsPool.v();
		loadMutants();
		TestsPool.v().pool.stream().forEach(test -> test.computeInfluencers());
		loadBuggyMethods();
		System.out.println("ranking...");
		final String outFilePath = Util.joinPath(args[0],
				Config.PROG_ID(),
				Config.PROG_VER() + ".txt");
		try(PrintWriter pw = new PrintWriter(new FileOutputStream(outFilePath))) {
			for(Method meth : MethodsPool.v().pool) {
				if(isBuggy(meth)) {
					double s = meth.susp();
					pw.println(meth.fullSignature + " " + s);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	private static boolean isBuggy(Method method) {
		return buggyMethodNames.contains(method.fullSignature);
	}
	
	private static void loadMutants() {
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID(),
				"mutation-test",
				Config.PROG_VER() + ".txt");
		Util.openFile(filePath);
		String line;
		String[] description = new String[0];
		while((line = Util.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("MutationDetails")) {
				if(description.length > 0) {
					Mutant p = Mutant.forDescription(description);
					p.mutatedMethod.mutants.add(p);
					for(Test test : p.failingTests) {
						test.addFailurePoint(p);
					}
					for(Test test : p.coveringTests) {
						test.addCover(p);
					}
					description = new String[0];
				}
			}
			String[] description_ext = new String[description.length + 1];
			System.arraycopy(description, 0, description_ext, 0, description.length);
			description_ext[description.length] = line;
			description = description_ext;
		}
		Util.closeFile();
//		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private static void loadBuggyMethods() {
		final String filePath = Util.joinPath(Config.BUG_METH_BASE_PATH,
				Config.PROG_ID(),
				Config.PROG_VER() + ".txt");
		final String prefix = "^^^^^^";
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			System.out.println("Buggy Methods:");
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith(prefix)) {
					final String bugMethodFullSignature = line.substring(prefix.length());
					buggyMethodNames.add(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
					System.out.println(bugMethodFullSignature.replace(":", Config.SIGNATURE_SPLITTER));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
