package mimp;

public class Config {
	public static String PROG_ID() {
		return System.getProperty("prog.id", null);
	}
	public static int PROG_VER() {
		return Integer.parseInt(System.getProperty("prog.ver", "-1"));
	}
	public static final String ALL_METH_BASE_PATH = System.getProperty("all.meth.base.path", null);
	public static final String BUG_METH_BASE_PATH = System.getProperty("bug.meth.base.path", null);
	public static final String FAILING_TESTS_BASE_PATH = System.getProperty("failing.test.base.path", null);
	public static final String SIGNATURE_SPLITTER = System.getProperty("sign.splitter", ":");
	public static final String DYNAMIC_INFO_BASE_PATH = System.getProperty("dynamic.base.path", null);
	/*group id example: org.jfree*/
	public static final String GROUP_ID = System.getProperty("group.id", null);
}
