package mi.util;

import java.io.Serializable;

public class PrintStamp implements Serializable {
	private static final long serialVersionUID = 1L;
	static int theCounter = 0;
	private int itsCounter;
	private String itsPrintClass;

	public PrintStamp(Object obj) {
		itsPrintClass = obj.getClass().toString();
		itsCounter = theCounter++;
	}
	
	@Override
	public String toString() {
		return String.format("[%s(%d)]", itsPrintClass, itsCounter);
	}
}
