package mi.presetmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import mi.util.IFileUtils;

public class MaxPathSerializer {
	private IFileUtils itsFileUtils;
	private String itsPlaceholderStem;
	private String itsExtension;

	public MaxPathSerializer(IFileUtils fileUtils, String placeholderStem, String extension) {
		itsFileUtils = fileUtils;
		itsPlaceholderStem = placeholderStem;
		itsExtension = extension;
	}

	public void serializeOut(String stem, Serializable obj) throws IOException {
		File f00 = itsFileUtils.locateFromStem00(itsPlaceholderStem, stem, itsExtension);
		
		if (f00 == null) {
			throw new IOException(String.format("Cannot locate %s%s via place-holder %s",
												stem, itsExtension, itsPlaceholderStem
											   )
								 );
		} else {
			FileOutputStream fos = new FileOutputStream(f00);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		}
	}
	
	public Serializable serializeIn(String stem) throws IOException, ClassNotFoundException {
		File f00 = itsFileUtils.locateFromStem00(itsPlaceholderStem, stem, itsExtension);

		if (f00 == null) {
			throw new IOException(String.format("Cannot locate %s%s via place-holder %s",
												stem, itsExtension, itsPlaceholderStem
											   )
								 );
		} else {
			FileInputStream fis = new FileInputStream(f00);
			ObjectInputStream in = new ObjectInputStream(fis);
		
			try {
				return (Serializable) in.readObject();
			} finally {
				in.close();
			}
		}
	}
}
