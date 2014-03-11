package kill.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Cloner<T> {
	@SuppressWarnings("unchecked")
	public T deepCopy(T oldObj) {
		ObjectOutputStream oos = null;	
		ObjectInputStream ois = null;
	      
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();
			ByteArrayInputStream bin =	new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			
	         // return the new object
			return (T) ois.readObject();
		} catch (Exception exn) {
			exn.printStackTrace();
			return null;
		} finally {
			try { oos.close(); } catch (IOException _) { }
			try { ois.close(); } catch (IOException _) { }
		}
	}
}
