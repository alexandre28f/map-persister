package mi.presetmanager.newps;

import java.io.IOException;

import mi.interf.newps.IPrincipalPresetManager;
import mi.interf.newps.IPrincipalPresetManagerIO;
import mi.interf.newps.ISecondaryPresetManager;
import mi.interf.newps.ISecondaryPresetManagerIO;
import mi.sys.XManifest;
import mi.util.IFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrincipalPresetManagerIO extends GenericPresetManagerIO<IPrincipalPresetManager>
									  implements IPrincipalPresetManagerIO
{
	private static Logger theLogger = LoggerFactory.getLogger(PrincipalPresetManagerIO.class);

	public PrincipalPresetManagerIO(IFileUtils fileUtils, String placeholder) {
		super(fileUtils, placeholder, XManifest.NEW_PRINCIPAL_EXTENSION);
	}

	private IPrincipalPresetManager readNewFormat(String fileStem)
		throws IOException, ClassNotFoundException
	{
		return read(fileStem);
	}

	/**	Read a secondary (or legacy) preset file and turn it into a principal.
		The secondary still needs some assembly (see {@link SecondaryPresetManager#reconstitute},
		but that's done in the sub-module.
		
		@param fileStem
		@return FIXME
		@throws IOException
		@throws ClassNotFoundException
	 */

	private IPrincipalPresetManager readAndBuildFromSecondary(String fileStem)
		throws IOException, ClassNotFoundException
	{
		ISecondaryPresetManagerIO io = new SecondaryPresetManagerIO(getFileUtils(), getPlaceholder());

		ISecondaryPresetManager sman = io.bareBonesRead(fileStem);

		//	The conversion is done here:
		return new PrincipalPresetManager(sman, XManifest.MAX_SECONDARY_PRESETTERS);
	}

	/**	Part of the new structuring. We no longer need complicated linkage between presets if they're
		being loaded centrally, so this is a bare-bones loader. It does, however, try to read legacy
		presets.
	
		@param fileStem the stem of the principal preset file
		@return a new bunch of principal presets
	 
		@throws ClassNotFoundException 
		@throws IOException 
	 */

	@Override
	public IPrincipalPresetManager bareBonesRead(String fileStem)
		throws IOException, ClassNotFoundException
	{
		try {
			return readNewFormat(fileStem);
		} catch (IOException exn) {
			theLogger.warn("cannot find new format {}; trying old format", fileStem);
			return readAndBuildFromSecondary(fileStem);
		}
	}
}
