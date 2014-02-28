package mi.util;

import java.util.Arrays;

public class Utils {
	@Deprecated
	static public int hash(int i1, int i2, int i3, int i4) {
		return Arrays.hashCode(new int[] { i1, i2, i3, i4 });
	}
	
	static public int softRound(float f) {
		return (int) (f + 0.5);
	}
	
	static public int o(Enum<?> p) {
		return p.ordinal();
	}
}
