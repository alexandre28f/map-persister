package kill.interf.newps;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**	Instrument presets and principal presets have a common interface: a bank of
 	version-controlled entries, named and colour-coded.
 	
 	@author nick

 	@param <T> the type of object held in the preset frame
 */

public interface IPresetFrame<T> extends Serializable {
//	UUID storeNewPreset(T data, int presetSlot);
//	T retrievePreset(int presetSlot, int version);
//	void replacePreset(T data, int presetSlot, int version);
//	void removeAllVersions(int presetSlot);

	void setName(int presetSlot, int version, String name);
	String getName(int presetSlot, int version);
	
	void setColour(int presetSlot, float[] rgb);
	float[] getColour(int presetSlot);
	
	Date getDateStored(int presetSlot, int version);
	void setDateStored(int presetSlot, int version, Date date);
	
	int getNumPresetSlots();
	void setNumPresetSlots(int numSlots);

	int getNumVersions(int presetSlot);
	int getLastVersionAccessed(int presetSlot);

	/**	Look for a preset by UUID: return an array of two integers, preset number
	 	(from 0) and version (from 0).
	 	
		@param id the UUID
		@return an array of two integers, or null if the UUID is not found
	 */
	
	int[] findByUUID00(UUID id);
	
	UUID storeNewPresetVersion(int presetSlot);
	
	/**	Push an edit state (read from file, perhaps) to something which
	 	responds to new, out-of-band edit states.
	 	
		@param presetSlot
		@param version
	 */

	void replacePreset(int presetSlot, int version);
	
	/**	Get the UUID of a preset.
	
		@param slot the preset slot
		@param version the preset version (0 = oldest)
		@return the UUID
	 */
	
	UUID getUUID(int slot, int version);
	
	T retrievePresetObject(int presetSlot, int version);
	void doRetrievePreset(int presetSlot, int version);
	void pushEditState();

	/**	Last preset slot accessed (for store, retrieve, replace)
	 	or -1 if none. NOTE: no attention paid to delete.
	 	
		@return the last slot accessed, or -1
	 */
	
	int getLastSlotAccessed();
	int[] getSelectionOriginalLocation();
	void deleteVersion(int presetSlot, int version);
	void clear(int presetSlot);
	void clearAll();
	
	//	FIXME !!!
	T examineEditBuffer();
	
	boolean isDirty();
	void makeClean();
}
