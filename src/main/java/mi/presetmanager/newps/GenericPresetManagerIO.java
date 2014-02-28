package mi.presetmanager.newps;

import java.io.IOException;
import java.io.Serializable;

import mi.interf.newps.IGenericPresetManagerIO;
import mi.presetmanager.MaxPathSerializer;
import mi.util.IFileUtils;

/**	We separated the preset bank from its I/O machinery in order to be able to
 	mock for unit testing.
 	
 	@author nick
 */

public class GenericPresetManagerIO<T extends Serializable> implements IGenericPresetManagerIO<T> {
	private IFileUtils itsFileUtils;
	private String itsPlaceholder;
	private String itsExtension;

	/** For mock testing: */
	public GenericPresetManagerIO(IFileUtils fileUtils, String placeholder, String extension) {
		itsFileUtils = fileUtils;
		itsPlaceholder = placeholder;
		itsExtension = extension;
	}
	
	@Override
	public void write(T bank, String fileStem) throws IOException {
		MaxPathSerializer serializer =
			new MaxPathSerializer(itsFileUtils, itsPlaceholder, itsExtension);
		
		serializer.serializeOut(fileStem, bank);
	}

	@SuppressWarnings("unchecked")
	protected T read(String fileStem)
		throws IOException, ClassNotFoundException
	{
		MaxPathSerializer serializer =
			new MaxPathSerializer(itsFileUtils, itsPlaceholder, itsExtension);

		return (T) serializer.serializeIn(fileStem);
	}
	
	protected IFileUtils getFileUtils() {
		return itsFileUtils;
	}
	
	protected String getPlaceholder() {
		return itsPlaceholder;
	}
}
