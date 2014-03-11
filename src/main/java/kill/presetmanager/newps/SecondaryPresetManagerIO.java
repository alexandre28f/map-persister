package kill.presetmanager.newps;

import java.io.IOException;

import kill.interf.newps.ISecondaryPresetManager;
import kill.interf.newps.ISecondaryPresetManagerIO;
import kill.sys.XManifest;
import kill.util.IFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondaryPresetManagerIO extends GenericPresetManagerIO<ISecondaryPresetManager>
									  implements ISecondaryPresetManagerIO
{
	private static Logger theLogger = LoggerFactory.getLogger(SecondaryPresetManagerIO.class);

	public SecondaryPresetManagerIO(IFileUtils fileUtils, String placeholder) {
		super(fileUtils, placeholder, XManifest.NEW_SECONDARY_EXTENSION);
	}

	/**	Part of the new structuring. We no longer need complicated linkage between presets if they're
	 	being loaded centrally, so this is a bare-bones loader.

		@param fileStem the stem for finding the secondary preset bank
		@return a bank of secondary presets
		@throws IOException
		@throws ClassNotFoundException
	 */

	@Override
	public ISecondaryPresetManager bareBonesRead(String fileStem)
		throws IOException, ClassNotFoundException
	{
		return read(fileStem);
	}
}
