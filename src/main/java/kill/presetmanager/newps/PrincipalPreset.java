package kill.presetmanager.newps;

import java.util.List;
import java.util.Map;

import kill.interf.IEditState;
import kill.interf.newps.IPrincipalPreset;
import kill.interf.newps.IZone;
import kill.m.dispatch.INonZoneableCollector;
import kill.presetmanager.EditBuffer;
import kill.presetmanager.newps.json.PanelPresetState;
import kill.sys.XManifest;
import kill.util.IMultiLinePrinter;
import kill.util.PrettyPrintBuffer;
import kill.util.PrintStamp;

import org.alexandrehd.persister.util.Cloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrincipalPreset implements IPrincipalPreset {
	private static final long serialVersionUID = 1L;

	private IZone[] itsPanelZones;
	private int itsCurrentZoneSelection;
	private PrintStamp itsPrintStamp00;
	
	static final Logger theLogger = LoggerFactory.getLogger(PrincipalPreset.class);

	public PrincipalPreset(int numPanels, boolean multiZone) {
		itsPanelZones = new IZone[numPanels];

		for (int i = 0; i < numPanels; i++) {
			if (multiZone) {
				itsPanelZones[i] = new MultiStateZone(XManifest.NUM_PRESET_ZONES);
				//	We always have the zero'th edit buffer for each secondary.
				itsPanelZones[i].setKeyState(0, 0, new EditBuffer());
			} else {
				itsPanelZones[i] = new SingleStateZone();		//	Edit buffer created implicitly.
			}
		}

		itsCurrentZoneSelection = 0;
	}
	
	/**	This constructor for Clojure scripting. */
	
	public PrincipalPreset(List<IZone> zones, int currentZoneSelection) {
		itsPanelZones = zones.toArray(new IZone[] { });
		itsCurrentZoneSelection = currentZoneSelection;
	}

	/**	Create a principal preset by reconstituting a JSON state (slightly munged:
	 	here we work with an array of zone states, not a map from panel names).
	 	
		@param panelMap
	 */

	public PrincipalPreset(PanelPresetState[] panelStates) {
		this(panelStates.length, false);
		
		for (int i = 0; i < panelStates.length; i++) {
			PanelPresetState state = panelStates[i];

			if (state.getMultiStateZoneInfo00() != null) {
				itsPanelZones[i] = new MultiStateZone(state.getMultiStateZoneInfo00());
			} else {
				itsPanelZones[i] = new SingleStateZone(new EditBuffer(state.getSingleStateZoneInfo00()));
			}
		}
	}

	private PrintStamp getPrintStamp() {
		if (itsPrintStamp00 == null) { itsPrintStamp00 = new PrintStamp(this); }
		return itsPrintStamp00;
	}

	/** @see kill.interf.newps.IPrincipalPreset#selectDiscreteBufferForZoneAcrossAllPanels(int)
	 */

	@Override
	public void selectDiscreteBufferForZoneAcrossAllPanels(int selection) {
		itsCurrentZoneSelection = selection;
	}

	@Override
	public IZone getMorphingVector(int pos) {
		return itsPanelZones[pos];
	}

	@Override
	public IPrincipalPreset deepClone() {
		return new Cloner<PrincipalPreset>().deepCopy(this);
	}

	@Override
	public IEditState getSelectedEditStateForSecondary(int panel) {
		ensurePosition(panel);
		ensureEditBuffer(panel, itsCurrentZoneSelection);
		return itsPanelZones[panel].getDiscreteEditState00(itsCurrentZoneSelection);
	}

	/** Dynamically extend according to panel index. (I'm not sure why we bother to do this.)

		@param panel
	 */

	private void ensurePosition(int panel) {
		if (panel >= itsPanelZones.length) {
			IZone newVectors[] = new MultiStateZone[panel + 1];

			for (int i = 0; i < itsPanelZones.length; i++) {
				newVectors[i] = itsPanelZones[i];
			}

			for (int i = itsPanelZones.length; i < newVectors.length; i++) {
				newVectors[i] = new MultiStateZone(XManifest.NUM_PRESET_ZONES);
				//	Always create a new buffer, leftmost.
				newVectors[i].setKeyState(0, 0, new EditBuffer());
			}

			itsPanelZones = newVectors;
		}
	}

	/** Make sure there's an edit buffer for a specific zone, cloning the first
		edit buffer if necessary.

		@param panel
		@param zone
	 */

	private void ensureEditBuffer(int panel, int zone) {
		IZone z = itsPanelZones[panel];
		if (z.getDiscreteEditState00(zone) == null) {
			z.setEditBuffer(zone, z.getDiscreteEditState00(0).deepClone());
		}
	}

	@Override
	public void cloneSelectedEditStatesForSecondary(int pos) {
		for (IZone v: itsPanelZones) {
			v.setKeyState(pos, 0, v.getDiscreteEditState00(itsCurrentZoneSelection).deepClone());
		}
	}

	@Override
	public int getOriginalPresetSlot() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOriginalPresetVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		PrettyPrintBuffer b = new PrettyPrintBuffer(getPrintStamp());

		for (int i = 0; i < itsPanelZones.length; i++) {
			PrettyPrintBuffer zp = new PrettyPrintBuffer("panel #" + i);
			IZone zone = itsPanelZones[i];
			zone.prettyprint(zp);
			zp.prettyprint(printer);
		}

		b.prettyprint(printer);
	}

	/** Called when we're converting old preset files. Assume we don't want zoning.

		@see kill.interf.newps.IPrincipalPreset#setSelectedUnzonedEditStateForSecondary(int, kill.interf.IEditState)
	 */
	@Override
	public void setSelectedUnzonedEditStateForSecondary(int panel, IEditState state) {
		itsPanelZones[panel] = new SingleStateZone(state);
	}

	@Override
	public void setZoneBarrierPosition(int zone, float position) {
		for (IZone z: itsPanelZones) {
			z.setZoneBarrier(zone, position);
		}
	}

	@Override
	public void setZoningEnableForPanel(int panel, boolean how) {
		IZone z = itsPanelZones[panel];

		if (how && (!z.zoningSupported())) {
			theLogger.info("promoting panel {}[/1] to zoning support", panel + 1);
			MultiStateZone mz = new MultiStateZone(1);
			mz.setKeyState(0, 0f, z.getDiscreteEditState00(0));
			itsPanelZones[panel] = mz;
		} else if (!how && z.zoningSupported()) {
			theLogger.info("demoting panel {}[/1] to single support", panel + 1);
			SingleStateZone sz = new SingleStateZone(z.getDiscreteEditState00(0));
			itsPanelZones[panel] = sz;
		} else {
			theLogger.info("no zoning support changes for panel {}[/1]", panel + 1);
		}
	}

	@Override
	public void setZoneBarrierEnable(int zone, boolean how) {
		for (IZone z: itsPanelZones) {
			if (how) {
				z.enableKeyState(zone);
			} else {
				z.disableKeyState(zone);
			}
		}
	}

	/** @see kill.interf.newps.IPrincipalPreset#distributeNonZoneableParams(INonZoneableCollector) */
	@Override
	public void distributeNonZoneableParams(INonZoneableCollector nonZoneables) {
		List<Enum<?>[]> nzScalars = nonZoneables.getNonZoneableScalarParametersForAllPanels();
		List<Enum<?>[]> nzVectors = nonZoneables.getNonZoneableVectorParametersForAllPanels();
		if (nzScalars.size() != nzVectors.size()) {
			throw new AssertionError(nzScalars.toString()+" must have same size as "+nzVectors.toString());
		}
	
		for (int i = 0; i < nzScalars.size(); ++i) {
			itsPanelZones[i].distributeSpecifiedState(itsCurrentZoneSelection, nzScalars.get(i), nzVectors.get(i));
		}

	}

	/** @see kill.interf.newps.IPrincipalPreset#copyFromCurrent(int) */
	@Override
	public void copyFromCurrent(int target) {
		for (int p = 0; p < itsPanelZones.length; p++) {
			itsPanelZones[p].copyEntireState(itsCurrentZoneSelection, target);
		}
	}
}
