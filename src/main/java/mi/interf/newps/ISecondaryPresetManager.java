package mi.interf.newps;

import org.alexandrehd.presetter.legacy.VectorParam;

import mi.interf.IEditState;
import mi.interf.flow.ISequencerListener;
import mi.util.IPrettyPrintable;

public interface ISecondaryPresetManager extends IPresetManager,
												 IPresetFrame<IEditState>,
												 IEditChangeNotifiable,
												 ISequencerListener
{
	void setNumScalarParams(int newSize);
	void setNumVectorParams(int newSize);
	
	VectorParam examineVectorParameter00(int paramId);
	
	void setTransientParameter(int i, float f);
	void setTransientParameter(int i, float[] fs);

	@Deprecated
    void handleScalarLocally(int paramId, float value);
	@Deprecated
    void handleVectorLocally(int paramId, float[] values);

	
	/**	Examine an edit buffer. Used in some instruments, but to be
	 	superceded by the morphing machinery.
	 	
		@return the current edit buffer
	 */
	
	@Override
	@Deprecated
	IEditState examineEditBuffer();
	
	IEditChangeNotifiable getTrigListener00();
	ISequencerListener getSeqListener00();
	void setTrigListener(IEditChangeNotifiable trigListener00);
	void reconstitute(IPrincipalPresetManager principal,
					  int panel,
					  IEditChangeNotifiable triggeredListener
					 );
}