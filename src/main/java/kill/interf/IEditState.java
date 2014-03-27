package kill.interf;

import java.io.Serializable;

import kill.interf.newps.IBasicPresetState;
import kill.interf.newps.IDeepCloneable;
import kill.presetmanager.newps.json.EditBufferState;
import kill.util.IPrettyPrintable;

import org.alexandrehd.persister.legacy.ScalarParam;
import org.alexandrehd.persister.legacy.VectorParam;

/**	An IEditState is a read-only edit buffer-style snapshot. The IEditBuffer
	subclass actually allows parameters to be altered. IEditStates are the
	things which get passed around between data manager clients.

	@author nick
 */

public interface IEditState extends IDeepCloneable<IEditState>,
									IBasicPresetState,
									IPrettyPrintable,
									Serializable {
	ScalarParam getScalar(int param);		//	Does not clone, so result is mutable in place.
	VectorParam getVector(int param);		//	Does not clone, so result is mutable in place.
	
	void putScalar(int param, ScalarParam value);
	void putVector(int param, VectorParam value);
	
	int getNumScalarParams();
	int getNumVectorParams();
	
	void setNumScalarParams(int limit);
	void setNumVectorParams(int limit);

	void allowScalarParam(int i);
	void allowVectorParam(int i);
	
	void setOriginalPresetHint(int presetSlot, int version);

	void refreshFrom(IEditState other);
	
	/**	Export for JSON.

		@return the exported state
	 */

	EditBufferState exportState();
}
