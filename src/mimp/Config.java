package mimp;

public class Config {
	public static final String PROG_ID = System.getProperty("prog.id", null);
	public static final int PROG_VER = Integer.parseInt(System.getProperty("prog.ver", "-1"));
	public static final String ALL_METH_BASE_PATH = System.getProperty("all.meth.base.path", null);
	public static final String SIGNATURE_SPLITTER = System.getProperty("sign.splitter", ":");
	public static final String DYNAMIC_INFO_BASE_PATH = System.getProperty("dynamic.base.path", null);
}
