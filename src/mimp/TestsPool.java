package mimp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TestsPool {
	private static TestsPool instance = null;
	public final List<Test> pool;
	
	private TestsPool() {
		pool = new ArrayList<>();
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID,
				"coverage-test",
				Config.PROG_VER + ".txt");
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String description;
			while((description = br.readLine()) != null) {
				description = description.trim();
				if(!description.startsWith("^^^")) {
					pool.add(TestFactory.v().create(description));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		pool.sort(Test::compareTo);
	}
	
	public static TestsPool v() {
		if(instance == null) {
			instance = new TestsPool();
		}
		return instance;
	}
	
	public static Test binarySearchTestByName(List<Test> pool, String name) {
		int min = 0;
		int max = pool.size() - 1;
		while(min <= max) {
			int mid = (min + max) / 2;
			Test midItem = pool.get(mid);
			int compResult = midItem.name.compareTo(name);
			if(compResult < 0) { /*midItem.name < name*/
				min = mid + 1;
			} else if (compResult > 0) { /*midItem.name > name*/
				max = mid - 1;
			} else { /*midItem.name = name*/
				return midItem;
			}
		}
		return null;
	}
	
	public Test getTestByName(String name) {
		return binarySearchTestByName(pool, name);
	}
}
