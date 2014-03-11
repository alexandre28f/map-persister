package mi.util;

import java.util.HashSet;
import java.util.Set;

public class VariousUtils {
	static public <T> Set<T> makeSet(T  ...items) {
		Set<T> s = new HashSet<T>();
		for (T t: items) { s.add(t); }
		return s;
	}
}
