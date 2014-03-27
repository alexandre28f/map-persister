/* -*- fill-column: 100; tab-width: 4; -*- */

package kill.presetmanager.newps;

import java.util.UUID;

import kill.interf.IEditState;
import kill.interf.flow.ISequencerListener;
import kill.interf.newps.IEditChangeNotifiable;
import kill.interf.newps.IPrincipalPresetManager;
import kill.interf.newps.ISecondaryPresetManager;
import kill.presetmanager.TransientPresetTemplate;
import kill.util.IMultiLinePrinter;

import org.alexandrehd.persister.legacy.VectorParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**	A secondary preset manager handles presets on behalf of a single edit buffer.
	It is becoming vestigial - a principal manager should be able to do everything
	a secondary manager can if run in a degenerate form - but we'll probably keep
	this code around as a template.

	@author nick
*/

public class SecondaryPresetManager extends PresetFrame<IEditState> // PresetManager
	implements ISecondaryPresetManager
{
	private static final long serialVersionUID = 1L;
	
	private static Logger theLogger = LoggerFactory.getLogger(SecondaryPresetManager.class);
	
	transient private IPrincipalPresetManager itsPrincipal;

	private TransientPresetTemplate itsTransientPresetTemplate;
	transient private IEditChangeNotifiable itsTrigListener00;
	transient private ISequencerListener itsSeqListener00;

	private int itsPanel;

	/**	Constructor.

		@param numSlots the number of preset slots
		@param manager the principal preset manager (which we hook into)

		@param trigListener00 optional listener for "trigger"-style out-of-band
		edit states

		@param seqListener00 FIXME

	*/

	public SecondaryPresetManager(int panel,
								  int numSlots,
								  IPrincipalPresetManager manager,
								  IEditChangeNotifiable trigListener00,
								  ISequencerListener seqListener00
								 ) {
		super(numSlots);
		itsPanel = panel;
		itsPrincipal = manager;
		//manager.getClub().putSecondaryManager(panel, this);
		itsTransientPresetTemplate = new TransientPresetTemplate();
		itsTrigListener00 = trigListener00;
		itsSeqListener00 = seqListener00;
	}

	@Override
	public void reconstitute(IPrincipalPresetManager principal,
							 int panel,
							 IEditChangeNotifiable triggeredListener) {
		itsPrincipal = principal;
		itsPanel = panel;
		itsTrigListener00 = triggeredListener;
	}
	
	private IEditState getEditBuffer() {
		return itsPrincipal.getCurrentEditBuffer(itsPanel);
	}

	@Override
	public UUID storeNewPresetVersion(int presetSlot) {
		return storeNewPreset(getEditBuffer(), presetSlot);
	}

	@Override
	public void replacePreset(int presetSlot, int version) {
		replacePreset(getEditBuffer(), presetSlot, version);
	}

	/**	Retrieve a preset. */

	@Override
		public void doRetrievePreset(int presetSlot, int version) {
		IEditState temp = retrievePresetObject(presetSlot, version);

		//	Extend with scalars:
		int buffSize = temp.getNumScalarParams();
		temp.allowScalarParam(itsTransientPresetTemplate.getNumScalarParams() - 1);
		for (int i = buffSize;
			 i < itsTransientPresetTemplate.getNumScalarParams();
			 i++
			 ) {
			temp.putScalar(i, itsTransientPresetTemplate.scalars[i]);
		}

		//	Extend with vectors: note: we need to clone the template.
		//	(Edit buffers are seriously mutable.)
		buffSize = temp.getNumVectorParams();
		temp.allowVectorParam(itsTransientPresetTemplate.getNumVectorParams() - 1);
		for (int i = buffSize;
			 i < itsTransientPresetTemplate.getNumVectorParams();
			 i++
			 ) {
			VectorParam source = itsTransientPresetTemplate.vectors[i];
			float[] f = new float[source.x.length];
			System.arraycopy(source.x, 0, f, 0, source.x.length);
			VectorParam vp = new VectorParam();
			vp.x = f;
			temp.putVector(i, vp);
		}

		temp.setOriginalPresetHint(presetSlot, version);
		getEditBuffer().refreshFrom(temp);

		pushEditState();
	}

	@Override
		public void setTransientParameter(int i, float f) {
		itsTransientPresetTemplate.allowScalarParam(i);
		itsTransientPresetTemplate.scalars[i].x = f;
	}

	@Override
		public void setTransientParameter(int i, float[] fs) {
		itsTransientPresetTemplate.allowVectorParam(i);
		itsTransientPresetTemplate.vectors[i].x = fs;		//	XXX: clone?
	}

	@Override
	@Deprecated
	public void handleScalarLocally(int paramId, float value) {
		IEditState eb = getEditBuffer();
		eb.allowScalarParam(paramId);
		eb.getScalar(paramId).x = value;
	}

	@Override
	@Deprecated
	public void handleVectorLocally(int paramId, float[] values) {
		IEditState eb = getEditBuffer();
		eb.allowVectorParam(paramId);
		eb.getVector(paramId).x = values;
	}

	/**	Scalar change from sequencer (here via our presetter). */

	@Override
		public void handleSeqScalar(int paramId, float value) {
		handleScalarLocally(paramId, value);
	}

	/**	Vector change from sequencer (here via our presetter). */

	@Override
		public void handleSeqVector(int paramId, float[] values) {
		if (itsSeqListener00 != null) {
			itsSeqListener00.handleSeqVector(paramId, values);
		}
	}

	@Override
		public void prettyprint(IMultiLinePrinter printer) {
		prettyprintSlots(printer);
	}

	@Override
		public IEditState examineEditBuffer() {
		return getEditBuffer();
	}

	@Override
		public VectorParam examineVectorParameter00(int paramId) {
		IEditState eb = getEditBuffer();
		int np = eb.getNumVectorParams();

		if (np > paramId) {
			return eb.getVector(paramId);
		} else {
			return null;
		}
	}

	@Override
		public void handleSeqEditState(IEditState editState) {
		getEditBuffer().refreshFrom(editState);

		if (itsSeqListener00 != null) {
			itsSeqListener00.handleSeqEditState(editState);
		}
	}

	/**	From principal preset manager. Last vestige. (Might only be used in unit tests!) */

	public void handleDirectedToEditState(IEditState editState) {
		pushEditState();
	}

	@Override
		public void handleSeqRecallByUUID(UUID id) {
		// TODO Auto-generated method stub

	}

	@Override
		public void handleSeqPresetFlash(UUID id) {
		// TODO Auto-generated method stub

	}

	@Override
		public void pushEditState() {
		if (itsTrigListener00 != null) {
			itsTrigListener00.editBufferHasChanged();
		}
	}

	@Override
		public void setNumScalarParams(int newSize) {
		getEditBuffer().setNumScalarParams(newSize);
	}

	@Override
		public void setNumVectorParams(int newSize) {
		getEditBuffer().setNumVectorParams(newSize);
	}

	@Override
		public IEditChangeNotifiable getTrigListener00() {
		return itsTrigListener00;
	}

	@Override
		public void setTrigListener(IEditChangeNotifiable trigListener00) {
		itsTrigListener00 = trigListener00;
	}

	@Override
		public ISequencerListener getSeqListener00() {
		return itsSeqListener00;
	}

//	@Override
//	public void storeScalarChange(int param, float value) {
//		handleScalarLocally(param, value);
//		
//	}
//
//	@Override
//	public void storeVectorChange(int param, float[] values) {
//		handleVectorLocally(param, values);
//	}

	@Override
	public void editBufferHasChanged() {
		pushEditState();
	}

	public void resizeVectorParam(int paramId, int newSize) {
		// XXX Need to chase this through.
		// TODO Auto-generated method stub
	}
}
