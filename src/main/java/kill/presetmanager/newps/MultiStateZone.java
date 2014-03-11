package kill.presetmanager.newps;

import kill.interf.IEditState;
import kill.interf.newps.IZone;
import kill.interf.newps.IZoneableParameter;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.json.EditBufferState;
import kill.presetmanager.newps.json.MultiState;
import kill.presetmanager.newps.json.PanelPresetState;
import kill.presetmanager.newps.json.SingleZoneState;
import kill.util.IMultiLinePrinter;
import kill.util.PrettyPrintBuffer;
import kill.util.PrintStamp;

public class MultiStateZone implements IZone {
	private static final long serialVersionUID = 1L;
	private IEditState itsKeyStates00[];			//	Array always present; edit states can be missing.
	private float itsZonePositions00[];				//	Entire array might be missing (old serialisations!);
													//	if the array is present, entries will obviously be there.
	private boolean itsZoneEnabledFlags00[];		//	Ditto.
	
	private PrintStamp itsPrintStamp00;

	private PrintStamp getPrintStamp() {
		if (itsPrintStamp00 == null) { itsPrintStamp00 = new PrintStamp(this); }
		return itsPrintStamp00;
	}

	private float[] getZonePositions() {
		if (itsZonePositions00 == null) {
			itsZonePositions00 = new float[itsKeyStates00.length];
		}
		
		return itsZonePositions00;
	}
	
	private boolean[] getZoneEnabledFlags() {
		if (itsZoneEnabledFlags00 == null) {
			itsZoneEnabledFlags00 = new boolean[itsKeyStates00.length];

			for (int i = 0; i < itsZoneEnabledFlags00.length; i++) {
				itsZoneEnabledFlags00[i] = true;		//	Initialise as set.
			}
		}
		
		return itsZoneEnabledFlags00;
	}
	
	public MultiStateZone(int numKeyStates) {
		itsKeyStates00 = new IEditState[numKeyStates];
		//itsKeyStates00[0] = new EditBuffer();
	}
	
	private boolean active(int zone) {
		boolean[] enabled = getZoneEnabledFlags();
		return enabled[zone] && (itsKeyStates00[zone] != null);
	}

	public MultiStateZone(int numKeyStates, SingleStateZone single) {
		this(numKeyStates);
		itsKeyStates00[0] = single.getDiscreteEditState00(0);
	}
	
	public MultiStateZone(MultiState multiStateZoneInfo) {
		this(multiStateZoneInfo.getStates().length);
		
		SingleZoneState[] states = multiStateZoneInfo.getStates();
		
		for (int i = 0; i < states.length; i++) {
			itsKeyStates00[i] = (states[i].getKeyState00() == null ? null : new EditBuffer(states[i].getKeyState00()));
			getZonePositions()[i] = states[i].getZonePosition();
			getZoneEnabledFlags()[i] = states[i].getZoneEnabled();
		}
	}

	/**	Represent an interpolation position; {@code lIndex} and {@code rIndex} are
		the zone indices (-1 means past one end), while {@code pos} is the position
		along from one zone to the next.
	 */
	
	class InterpolationInformation {
		int lIndex, rIndex;		//	Which edit buffer position; -1 if past one end or the other.
		float pos;
		
		InterpolationInformation(int lIndex, int rIndex, float pos) {
			this.lIndex = lIndex;
			this.rIndex = rIndex;
			this.pos = pos;
		}
	}
	
	private InterpolationInformation interpolationPos(float zonePos) {
		int lIndex = -1;
		int rIndex = -1;
		float[] positions = getZonePositions();
		
		int len = itsKeyStates00.length;
		for (int i = 0; i < len; i++) {
			if (active(i) && zonePos >= positions[i]) {
				lIndex = i;
			}
		}
		
		for (int i = len - 1; i >= 0; i--) {
			if (active(i) && zonePos <= positions[i]) {
				rIndex = i;
			}
		}
		
		if (lIndex == -1) {
			if (rIndex == -1) {
				throw new IllegalArgumentException("no key states for crossfade");
			} else {
				return new InterpolationInformation(-1, rIndex, 1f);
			}
		} else if (rIndex == -1) {
			return new InterpolationInformation(lIndex, -1, 0f);
		} else {
			float lPos = positions[lIndex];
			float rPos = positions[rIndex];
			
			float interpPos = (lPos == rPos) ? 0f : ((zonePos - lPos) / (rPos - lPos));
			return new InterpolationInformation(lIndex, rIndex, interpPos);
		}
		
	}

	/**	Interpolate a single parameter. Deprecated: we want to generate an
	 	entire edit state on demand.

		@see kill.interf.newps.IZone#examineScalarMorphState(int, float)
	 */

	@Deprecated
	@Override
	public float examineScalarMorphState(int param, float pos) {
		InterpolationInformation info = interpolationPos(pos);
		if (info.lIndex == -1) {
			return itsKeyStates00[info.rIndex].getScalar(param).x;
		} else if (info.rIndex == -1) {
			return itsKeyStates00[info.lIndex].getScalar(param).x;
		} else {
			IEditState s0 = itsKeyStates00[info.lIndex];
			IEditState s1 = itsKeyStates00[info.rIndex];
			float f0 = s0.getScalar(param).x;
			float f1 = s1.getScalar(param).x;
			return f0 * (1.0f - pos) + f1 * pos;
		}
	}

	@Deprecated
	@Override
	public float[] examineVectorMorphState(int param, float pos) {
		InterpolationInformation info = interpolationPos(pos);
		if (info.lIndex == -1) {
			return itsKeyStates00[info.rIndex].getVector(param).x;
		} else if (info.rIndex == -1) {
			return itsKeyStates00[info.lIndex].getVector(param).x;
		} else {
			IEditState s0 = itsKeyStates00[info.lIndex];
			IEditState s1 = itsKeyStates00[info.rIndex];

			float[] f0 = s0.getVector(param).x;
			float[] f1 = s1.getVector(param).x;
			float[] res = new float[f0.length];
			
			for (int j = 0; j < res.length; j++) {
				res[j] = f0[j] * (1.0f - pos) + f1[j] * pos;
			}
			
			return res;
		}
	}

	/**	Interpolator. At a particular point, look for left and right neighbour
		points, and interpolate between them.
		
		@see kill.interf.newps.IZone#generateMorphedState(float)
	 */
	
	@Override
	public IEditState generateMorphedState(Class<? extends IZoneableParameter> scalarEnumClass,
										   Class<? extends IZoneableParameter> vectorEnumClass,
										   float xfade,
										   int defaultEditStateForNonZoned
										  ) {
		InterpolationInformation info = interpolationPos(xfade);
		
		if (info.lIndex == -1) {
			return itsKeyStates00[info.rIndex].deepClone();
		} else if (info.rIndex == -1) {
			return itsKeyStates00[info.lIndex].deepClone();
		} else {
			return interp(itsKeyStates00[info.lIndex],
						  itsKeyStates00[info.rIndex],
						  scalarEnumClass,
						  vectorEnumClass,
						  info.pos,
						  defaultEditStateForNonZoned
						 );
		}
	}

	enum Empty implements IZoneableParameter {
		/* nothing */ ;
		@Override public boolean isZoneable() { return false; }
	}
	
	@Override
	public IEditState generateMorphedState(float xfade) {
		return generateMorphedState(Empty.class, Empty.class, xfade, 0);
	}
	
	/**	Check whether this parameter ID is marked as zoneable by its enum type

		@param paramID the parameter ID
		@param params an array enumerating the parameter type

		@return true if this parameter is marked as zoneable, false if not
	 */

	private boolean markedAsZoneable(int paramID, IZoneableParameter[] params) {
		//	For test cases, return true if we run past the end of the enum.
		if (paramID >= params.length) {
			return true;
		} else {
			return params[paramID].isZoneable();
		}
	}

	private IEditState interp(IEditState s0,
							  IEditState s1,
							  Class<? extends IZoneableParameter> scalarEnumClass,
							  Class<? extends IZoneableParameter> vectorEnumClass,
							  float pos,
							  int defaultEditStateForNonZoned
							 ) {
		IZoneableParameter[] scalarParams = scalarEnumClass.getEnumConstants();
		IZoneableParameter[] vectorParams = vectorEnumClass.getEnumConstants();
		int ns = s0.getNumScalarParams();
		int nv = s0.getNumVectorParams();
		EditBuffer result = new EditBuffer();
		
		if (s0 != null) {
			if (s0.getNumScalarParams() != s1.getNumScalarParams()) {
				throw new IllegalArgumentException("different number of scalar parameters");
			} else if (s0.getNumVectorParams() != s1.getNumVectorParams()) {
				throw new IllegalArgumentException("different number of vector parameters");
			}
			
			for (int i = 0; i < s1.getNumVectorParams(); i++) {
				if (s0.getVector(i).x.length != s1.getVector(i).x.length) {
					throw new IllegalArgumentException("mismatch in vector lengths for param[" + i + "]");
				}
			}
		}
		
		result.setNumScalarParams(ns);
		
		for (int i = 0; i < s0.getNumScalarParams(); i++) {
			if (markedAsZoneable(i, scalarParams)) {
				float f0 = s0.getScalar(i).x;
				float f1 = s1.getScalar(i).x;
				result.scalars[i].x = f0 * (1.0f - pos) + f1 * pos;
			} else {
				result.scalars[i].x = itsKeyStates00[defaultEditStateForNonZoned].getScalar(i).x;
			}
		}

		result.setNumVectorParams(nv);

		for (int i = 0; i < s0.getNumVectorParams(); i++) {
			if (markedAsZoneable(i, vectorParams)) {
				float[] f0 = s0.getVector(i).x;
				float[] f1 = s1.getVector(i).x;
				float[] res = new float[f0.length];
			
				for (int j = 0; j < res.length; j++) {
					res[j] = f0[j] * (1.0f - pos) + f1[j] * pos;
				}
			
				result.vectors[i].x = res;
			} else {
				float[] sourceVec = itsKeyStates00[defaultEditStateForNonZoned].getVector(i).x;
				result.vectors[i].x = new float[sourceVec.length];
				System.arraycopy(sourceVec, 0, result.vectors[i].x, 0, sourceVec.length);
			}
		}
		
		return result;
	}

	@Override
	public IEditState getDiscreteEditState00(int zone) {
		ensureZone(zone);
		return itsKeyStates00[zone];
	}
	
	@Override
	public void setKeyState(int zone, float position, IEditState state) {
		ensureZone(zone);

		setEditBuffer(zone, state.deepClone());
		float[] positions = getZonePositions();
		positions[zone] = position;
		
		for (int i = 0; i < zone; i++) {
			positions[i] = Math.min(positions[i], position);
		}
		
		for (int i = zone + 1; i < itsKeyStates00.length; i++) {
			positions[i] = Math.max(positions[i], position);
		}
	}
	
	@Override
	public void setEditBuffer(int zone, IEditState state) {
		ensureZone(zone);
		itsKeyStates00[zone] = state;
	}

	private void ensureZone(int zone) {
		if (zone >= itsKeyStates00.length) {
			IEditState newKeyStates00[] = new IEditState[zone + 1];
			System.arraycopy(itsKeyStates00, 0, newKeyStates00, 0, itsKeyStates00.length);
			itsKeyStates00 = newKeyStates00;
			
			float[] newZonePositions = new float[zone + 1];
			System.arraycopy(getZonePositions(), 0, newZonePositions, 0, getZonePositions().length);
			itsZonePositions00 = newZonePositions;
			
			boolean[] newZoneEnabledFlags = new boolean[zone + 1];
			System.arraycopy(getZoneEnabledFlags(), 0, newZoneEnabledFlags, 0, getZoneEnabledFlags().length);
			itsZoneEnabledFlags00 = newZoneEnabledFlags;
		}
	}

	@Override
	public void disableKeyState(int i) {
		ensureZone(i);
		getZoneEnabledFlags()[i] = false;
	}

	@Override
	public void enableKeyState(int i) {
		ensureZone(i);
		getZoneEnabledFlags()[i] = true;
	}
	
	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		PrettyPrintBuffer b = new PrettyPrintBuffer(getPrintStamp());
		
		for (int i = 0; i < itsKeyStates00.length; i++) {
			PrettyPrintBuffer bx = new PrettyPrintBuffer("zone pos " + i);
			if (itsKeyStates00[i] == null) {
				bx.printLine("---");
			} else {
				itsKeyStates00[i].prettyprint(bx);
			}
			
			bx.prettyprint(b);
		}
		
		b.prettyprint(printer);
	}

	@Override
	public void setZoneBarrier(int zone, float position) {
		ensureZone(zone);
		getZonePositions()[zone] = position;
	}

	@Override
	public boolean zoningSupported() { return true; }

	@Override
	public boolean keyStateIsEnabled(int i) {
		ensureZone(i);
		return getZoneEnabledFlags()[i];
	}

	@Override
	public int getNumKeyStates() {
		return itsKeyStates00.length;
	}

	/**	@see kill.interf.newps.IZone#distributeSpecifiedState(int, Enum[], Enum[]) */
	
	@Override
	public void distributeSpecifiedState(int sourceZone,
										 Enum<?>[] nonZoneableScalars,
										 Enum<?>[] nonZoneableVectors) {
		IEditState src = itsKeyStates00[sourceZone];

		for (int i = 0; i < itsKeyStates00.length; i++) {
			if (i != sourceZone) {
				IEditState dest00 = itsKeyStates00[i];
				if (dest00 != null) {
					for (Enum<?> pNum: nonZoneableScalars) {
						int p = pNum.ordinal();
						dest00.allowScalarParam(p);
						dest00.getScalar(p).x = src.getScalar(p).x;
					}

					for (Enum<?> pNum: nonZoneableVectors) {
						int p = pNum.ordinal();
						dest00.allowVectorParam(p);
						float[] from = src.getVector(p).x;
						dest00.getVector(p).x = new float[from.length];
						System.arraycopy(from, 0, dest00.getVector(p).x, 0, from.length);
					}
				}
			}
		}
	}

	/**	@see kill.interf.newps.IZone#copyEntireState(int, int) */
	@Override
	public void copyEntireState(int sourceZone, int targetZone) {
		itsKeyStates00[targetZone] = itsKeyStates00[sourceZone].deepClone();
	}

	/**	@see kill.interf.newps.IZone#exportState() */
	@Override
	public PanelPresetState exportState() {
		SingleZoneState[] zones = new SingleZoneState[itsKeyStates00.length];
		
		for (int i = 0; i < zones.length; i++) {
			EditBufferState exportState00 = (itsKeyStates00[i] == null ? null : itsKeyStates00[i].exportState());
			float zonePosition = (itsZonePositions00 == null ? 0f : itsZonePositions00[i]);
			boolean zoneEnabled = (itsZoneEnabledFlags00 == null ? false : itsZoneEnabledFlags00[i]);
			zones[i] = new SingleZoneState(exportState00, zonePosition, zoneEnabled);
		}
		
		return new PanelPresetState(null, new MultiState(zones));
	}
}
