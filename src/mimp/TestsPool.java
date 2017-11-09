package mimp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestsPool {
	private static TestsPool instance = null;
	//public final List<Test> pool;
	private final Map<String, FailingTest> failingTestsMap;
	private final Map<String, PassingTest> passingTestsMap;
	public final int failingTestsCount;
	
	private TestsPool() {
		//pool = new ArrayList<>();
		failingTestsMap = new HashMap<>();
		passingTestsMap = new HashMap<>();
		Map<String, String> failureInfoMap;
		if(Config.PROG_ID().equals("Closure")) {
			failureInfoMap = loadCoverageTestClosure();
		} else {
			failureInfoMap = loadCoverageTestOthers();
		}
		final String filePath = Util.joinPath(Config.FAILING_TESTS_BASE_PATH,
				Config.PROG_ID(),
				Config.PROG_VER() + ".txt");
		int failingTestsCount = 0;
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String description;
			while((description = br.readLine()) != null) {
				description = description.trim();
				if(!description.isEmpty()) { //sometimes the file is empty
					final int firstColon = description.indexOf(':');
					final String className = description.substring(0, firstColon);
					final String methodName = description.substring(firstColon + 2 /*ignores the two colons*/);
					final String testName = className + "." + methodName;
					String failureDescription = failureInfoMap.get(testName);
					if(failureDescription != null) {
						FailingTest test = (FailingTest) TestFactory.v().create(failureDescription);
						if(test != null) {
							failingTestsMap.put(testName, test);
							failingTestsCount++;
						}
					}
				}				
//				if(!description.startsWith("^^^")) {
//				Test test = TestFactory.v().create(description);
//				if(test != null) {
//					pool.add(test);
//					if(test instanceof FailingTest) {
//						failingTestsCount++;
//					}
//				}
//			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		pool.sort(Test::compareTo);
		this.failingTestsCount = failingTestsCount;
		if(this.failingTestsCount == 0)
			System.out.println("SDSDS");
	}
	
	private Map<String, String> loadCoverageTestClosure() {
		final String path = Util.joinPath(Config.CLOSURE_DYN_INF_BASE_PATH,
				Integer.toString(Config.PROG_VER()),
				"coverage-test");
		final Map<String, String> result = new HashMap<>();
		final List<String> filesList = new ArrayList<>();
		Util.listFiles(path, filesList);
		for(String fileName : filesList) {
			try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
				br.lines().filter(description -> description.indexOf("[STACKTRACE]") > 0)
					.map(description -> description.trim())
					.forEach(description -> {
						final int firstSpace = description.indexOf(' ');
						final String firstPart = description.substring(0, firstSpace);
						final int firstParenthesis = firstPart.indexOf('(');
						if(firstParenthesis >= 0) {
							final String testName = firstPart.substring(0, firstParenthesis);
							result.put(testName, description);
						}
					});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private Map<String, String> loadCoverageTestOthers() {
		final String filePath = Util.joinPath(Config.DYNAMIC_INFO_BASE_PATH,
				Config.PROG_ID(),
				"coverage-test",
				Config.PROG_VER() + ".txt");
		Map<String, String> result = new HashMap<>();
		try (BufferedReader brMD = new BufferedReader(new FileReader(filePath))) {
			String description;
			while((description = brMD.readLine()) != null) {
				description = description.trim();
				if(description.indexOf("[STACKTRACE]") > 0) {
					final int firstSpace = description.indexOf(' ');
					final String firstPart = description.substring(0, firstSpace);
					final int firstParenthesis = firstPart.indexOf('(');
					if(firstParenthesis >= 0) {
						final String testName = firstPart.substring(0, firstParenthesis);
						result.put(testName, description);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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
	
	private static Double failingTestsRank = null;
	
	public double failingTestsRank() {
		if(failingTestsRank == null) {
			failingTestsRank = failingTestsMap.values()
					.parallelStream()
					.filter(test -> test instanceof FailingTest)
					.mapToDouble(Test::rank)
					.sum();
		}
		return failingTestsRank.doubleValue();
	}
	
	public void computeInfluencers() {
		failingTestsMap.values().stream().forEach(Test::computeInfluencers);
		failingTestsMap.values().stream().forEach(Test::computeInfluencerMethods);
		passingTestsMap.values().stream().forEach(Test::computeInfluencers);
		passingTestsMap.values().stream().forEach(Test::computeInfluencerMethods);
	}
	
	public Test getTestByName(String name) {
		Test test;
		test = failingTestsMap.get(name);
		if(test != null) {
			return test;
		}
		test = passingTestsMap.get(name);
		if(test != null) {
			return test;
		}
		test = TestFactory.v().create(name + "() true PASS");
		passingTestsMap.put(name, (PassingTest) test);
		return test;
	}
	
	public Set<String> getFailingTestNames() {
		return failingTestsMap.keySet();
	}
	
	public Set<String> getPassingTestNames() {
		return passingTestsMap.keySet();
	}
	
	public static <T> List<T> interleave(final List<T> list1, final List<T> list2) {
		List<T> result = new ArrayList<>(list1.size() + list2.size());
		Iterator<T> it1 = list1.iterator();
		Iterator<T> it2 = list2.iterator();
		while(it1.hasNext() || it2.hasNext()) {
			if(it1.hasNext()) {
				result.add(it1.next());
		    }
		    if(it2.hasNext()) {
		    	result.add(it2.next());
		    }
		}
		return result;
	}
}
