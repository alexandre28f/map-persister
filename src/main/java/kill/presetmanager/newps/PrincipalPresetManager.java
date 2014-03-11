package mi.presetmanager.newps;

import java.util.Set;
import java.util.UUID;
import java.util.jar.Manifest;

import mi.interf.IEditState;
import mi.interf.newps.IEditChangeNotifiable;
import mi.interf.newps.IPrincipalPreset;
import mi.interf.newps.IPrincipalPresetManager;
import mi.interf.newps.ISecondaryPresetManager;
import mi.m.dispatch.INonZoneableCollector;
import mi.sys.XManifest;
import mi.util.IMultiLinePrinter;
import mi.util.PrettyPrintBuffer;
import mi.util.PrintStamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A principal preset manager handles presets on behalf of a collection of edit
    states; there may be multiple client modules (each with its own secondary
    preset manager for single-edit-buffer "templates"), and any one of these
    client modules might want to "morph" across multiple edit states.

    <P>There is a dedicated secondary manager for each principal manager: it's the only
    way that the principal manager itself can have parameters. (Simple instruments only
    have a principal manager, so it must be able to deal with parameters. More complex
    instruments have a principal and one or more secondaries in different panels.)
    The dedicated manager is at panel index {@link Manifest#HIDDEN_PANEL}.

    <P>Complex instruments have zoning machinery to morph between edit states; the actual
    zone parameters comprise (part of) the parameter set for the principal (with hard-wired
    parameter IDs).

    @author nick
 */

public class PrincipalPresetManager extends PresetFrame<IPrincipalPreset>
									implements IPrincipalPresetManager
{
	private static final long serialVersionUID = 1L;

	transient private Set<IEditChangeNotifiable> itsNotifiables; 
	private IPrincipalPreset itsCurrentPreset;

	private static Logger theLogger = LoggerFactory.getLogger(PrincipalPresetManager.class);

	/** This is now nullable for hysterical reasons: I suspect we saved some preset files where
        it wasn't set (or even declared). So, be defensive.
	 */

	private PrintStamp itsPrintStamp00;

	public PrincipalPresetManager(int numSlots, int numSecondaries) {
		super(numSlots);
		itsCurrentPreset = new PrincipalPreset(numSecondaries, false);
	}

	@Override
	public IPrincipalPreset getCurrentPreset() {
		return itsCurrentPreset;
	}

	/** Convert from (legacy) secondary bank into a principal. The old secondary bank's edit
        buffers are copied into the HIDDEN_PANEL of the principal, and cosmetic attributes
        like date, name and colour are also copied.

        @param sman the legacy preset bank we're converting
        @param numPanels the number of secondaries for this principal (actually
            it's always {@link Manifest#MAX_SECONDARY_PRESETTERS}
	 */

	public PrincipalPresetManager(ISecondaryPresetManager sman, int numPanels) {
		super(sman.getNumPresetSlots());

		for (int slot = 0; slot < sman.getNumPresetSlots(); slot++) {
			for (int ver = 0; ver < sman.getNumVersions(slot); ver++) {
				IEditState state = sman.retrievePresetObject(slot, ver);
				itsCurrentPreset = new PrincipalPreset(numPanels, false);
				itsCurrentPreset.setSelectedUnzonedEditStateForSecondary(XManifest.HIDDEN_PANEL, state);
				/*ignore*/ storeNewPresetVersion(slot);
				setDateStored(slot, ver, sman.getDateStored(slot, ver));
				setName(slot, ver, sman.getName(slot, ver));
			}

			setColour(slot, sman.getColour(slot));
		}

		itsCurrentPreset = new PrincipalPreset(numPanels, false);
		int lastSlot = sman.getLastSlotAccessed();
		if (lastSlot >= 0) {
			int lastVersion = sman.getLastVersionAccessed(lastSlot);
			if (lastVersion >= 0) {
				IEditState state = sman.retrievePresetObject(lastSlot, lastVersion);
				itsCurrentPreset.setSelectedUnzonedEditStateForSecondary(XManifest.HIDDEN_PANEL, state);
			}
		}
	}

	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		if (itsPrintStamp00 == null) { itsPrintStamp00 = new PrintStamp(this); }
		PrettyPrintBuffer buff = new PrettyPrintBuffer(itsPrintStamp00);
		new PrettyPrintBuffer("current preset", itsCurrentPreset).prettyprint(buff);
		prettyprintSlots(buff);
		buff.prettyprint(printer);
	}

	@Override
	public UUID storeNewPresetVersion(int presetSlot) {
		return storeNewPreset(itsCurrentPreset, presetSlot);
	}

	@Override
	public IEditState examineMorphState(int panel, float xfade) {
		return itsCurrentPreset.getMorphingVector(panel).generateMorphedState(xfade);
	}

	@Override
	public void replacePreset(int presetSlot, int version) {
		replacePreset(itsCurrentPreset, presetSlot, version);
	}

	@Override
	public void pushEditState() {
		for (IEditChangeNotifiable n: itsNotifiables) {
			n.editBufferHasChanged();
		}
		
	}

	/**	@see mi.interf.newps.IPrincipalPresetManager#injectPreset(mi.interf.newps.IPrincipalPreset) */
	@Override
	public void injectPreset(IPrincipalPreset preset) {
		itsCurrentPreset = preset;
		pushEditState();
	}

	@Override
	public void doRetrievePreset(int presetSlot, int version) {
		injectPreset(retrievePresetObject(presetSlot, version));
	}

	@Override
	public IEditState getCurrentEditBuffer(int panel) {
		return itsCurrentPreset.getSelectedEditStateForSecondary(panel);
	}

	/** @see mi.interf.newps.IPrincipalPresetManager#switchToBufferForZone(int) */
	@Override
	public void switchToBufferForZone(int zone) {
		itsCurrentPreset.selectDiscreteBufferForZoneAcrossAllPanels(zone);
		pushEditState();
	}

	@Override
	public void setNotifiables(Set<IEditChangeNotifiable> notifiables) {
		itsNotifiables = notifiables;
	}

	@Override
	public void cloneSelectedEditStatesToZonePosition(int pos) {
		itsCurrentPreset.cloneSelectedEditStatesForSecondary(pos);
	}

	@Override
	public IPrincipalPreset examineEditBuffer() {
		return itsCurrentPreset;
	}

	/** @see mi.interf.newps.IPrincipalPresetManager#distributeNonZoneableParams(INonZoneableCollector) */
	@Override
	public void distributeNonZoneableParams(INonZoneableCollector nonZoneables) {
		itsCurrentPreset.distributeNonZoneableParams(nonZoneables);
	}

	/** @see mi.interf.newps.IPrincipalPresetManager#copyFromCurrent(int) */
	@Override
	public void copyFromCurrent(int target) {
		itsCurrentPreset.copyFromCurrent(target);
	}
}
