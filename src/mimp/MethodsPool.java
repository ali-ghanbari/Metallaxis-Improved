package mimp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MethodsPool {
	private static MethodsPool instance = null;
	public final List<Method> pool;
	
	private MethodsPool() {
		pool = new ArrayList<>();
		final String filePath = Util.joinPath(Config.ALL_METH_BASE_PATH,
				Config.PROG_ID,
				Config.PROG_VER + ".txt");
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String previous = null;
			String ln;
			while((ln = br.readLine()) != null) {
				String[] lnSplitted = ln.trim().split(":");
				String methodFullSignature = lnSplitted[0] 
						+ Config.SIGNATURE_SPLITTER 
						+ lnSplitted[1];
				if(!methodFullSignature.equals(previous)) {
					pool.add(new Method(methodFullSignature));
					previous = methodFullSignature;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		pool.sort(Method::compareTo);
	}

	public static MethodsPool v() {
		if(instance == null) {
			instance = new MethodsPool();
		}
		return instance;
	}
	
	public Method getMethodByFullSignature(String fullSignature) {
		int min = 0;
		int max = pool.size() - 1;
		while(min <= max) {
			int mid = (min + max) / 2;
			Method midItem = pool.get(mid);
			int compResult = midItem
					.fullSignature
					.compareTo(fullSignature);
			if(compResult < 0) { /*midItem.fullSignature < fullSignature*/
				min = mid + 1;
			} else if (compResult > 0) { /*midItem.fullSignature > fullSignature*/
				max = mid - 1;
			} else { /*midItem.fullSignature = fullSignature*/
				return midItem;
			}
		}
		return null;
	}
}
