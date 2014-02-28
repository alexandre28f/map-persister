package mi.basicdata;

import java.io.Serializable;

public class ScalarParam implements Serializable {
	private static final long serialVersionUID = 1162336643583770153L;
	
	public ScalarParam(float x) {
		this.x = x;
	}
	
	public float x  ;
}
