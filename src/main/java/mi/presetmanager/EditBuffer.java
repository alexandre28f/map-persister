package mi.presetmanager;

import org.alexandrehd.presetter.legacy.ScalarParam;
import org.alexandrehd.presetter.legacy.VectorParam;

import mi.interf.IEditState;
import mi.presetmanager.newps.json.EditBufferState;
import mi.util.IMultiLinePrinter;

public class EditBuffer extends PresetState implements IEditBuffer
{
	/**
	 * @param scalars
	 * @param vectors
	 */
	public EditBuffer(ScalarParam[] scalars, VectorParam[] vectors) {
		super(scalars, vectors);
	}
	
	public EditBuffer() { }

	public EditBuffer(EditBufferState singleStateZoneInfo) {
		super(genScalars(singleStateZoneInfo.getScalarParams()),
			  genVectors(singleStateZoneInfo.getVectorParams()));
	}

	private static ScalarParam[] genScalars(float[] scalarParams) {
		ScalarParam[] params = new ScalarParam[scalarParams.length];
		
		for (int i = 0; i < params.length; i++) {
			params[i] = new ScalarParam(scalarParams[i]);
		}
		
		return params;
	}
	
	private static VectorParam[] genVectors(float[][] vectorParams) {
		VectorParam[] params = new VectorParam[vectorParams.length];
		
		for (int i = 0; i < params.length; i++) {
			params[i] = new VectorParam(vectorParams[i]);
		}
		
		return params;
	}

	private static final long serialVersionUID = 5153700129841855359L;
	
	private int itsOriginalPresetSlot = -1;
	private int itsOriginalPresetVersion = -1;

	@Override
	public ScalarParam getScalar(int param) {
		return scalars[param];
	}

	@Override
	public VectorParam getVector(int param) {
		return vectors[param];
	}

	@Override
	public void putScalar(int param, ScalarParam value) {
		allowScalarParam(param);
		scalars[param] = value;
	}

	@Override
	public void putVector(int param, VectorParam value) {
		allowVectorParam(param);
		vectors[param] = value;
	}

	@Override
	public int getOriginalPresetSlot() {
		return itsOriginalPresetSlot;
	}

	@Override
	public int getOriginalPresetVersion() {
		return itsOriginalPresetVersion;
	}

	@Override
	public void setOriginalPresetHint(int presetSlot, int version) {
		itsOriginalPresetSlot = presetSlot;
		itsOriginalPresetVersion = version;
	}

	@Override
	public void refreshFrom(IEditState editState) {
		int ns = editState.getNumScalarParams();
		ScalarParam[] newScalars = new ScalarParam[ns];
		for (int i = 0; i < ns; i++) {
			newScalars[i] = new ScalarParam(editState.getScalar(i).x);
		}
		
		scalars = newScalars;

		int nv = editState.getNumVectorParams();
		VectorParam[] newVectors = new VectorParam[nv];
		for (int i = 0; i < nv; i++) {
			VectorParam vp1 = editState.getVector(i);
			newVectors[i] = new VectorParam(vp1.resize(vp1.x.length));
		}
		
		vectors = newVectors;
	}

	@Override
	public EditBuffer deepClone() {
		EditBuffer b = new EditBuffer();
		b.refreshFrom(this);
		return b;
	}

	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		printer.printLine(String.format("EBuff #scalars=%d #vectors=%d", scalars.length, vectors.length));
	}

	@Override
	public EditBufferState exportState() {
		float[] s = new float[scalars.length];
		float[][] v = new float[vectors.length][];
		
		for (int i = 0; i < s.length; i++) {
			s[i] = scalars[i].x;
		}
	
		for (int i = 0; i < v.length; i++) {
			v[i] = vectors[i].x;
		}
	
		return new EditBufferState(s, v);
	}
}
