package mimp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		String[] test = "hello:sdsd:thi,\t\\:is a test?:x".split(",\\s");
		for(String s : test)
			System.out.println(s);
		//System.out.println(Util.joinPath("d:\\", "hello", "world\\", "this.txt"));
		String str = "ali ila";
		System.out.println(str.indexOf('l', 2));
	}
	
	private static void MutantsLoader() {
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID,
				"mutation-test",
				Config.PROG_VER + ".txt");
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			String[] description = new String[0];
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("MutationDetails")) {
					if(description.length > 0) {
						ImmutablePair<Mutant, Method> p = Mutant.forDescription(description);
						p.snd.mutants.add(p.fst);
						for(Test test : p.fst.impacts) {
							test.addInfluencer(p.fst);
						}
						description = new String[0];
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
