package mi.presetmanager.newps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mi.interf.newps.IDeepCloneable;
import mi.interf.newps.IPresetFrame;
import mi.sys.XManifest;
import mi.util.IMultiLinePrinter;
import mi.util.PrettyPrintBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class PresetFrame<T extends IDeepCloneable<T>> implements IPresetFrame<T> {
	private static final long serialVersionUID = 1L;
	
	static Logger itsLogger = LoggerFactory.getLogger(PresetFrame.class);

	static private class Preset<T extends IDeepCloneable<T>> implements Serializable {
		private static final long serialVersionUID = 1L;
		String itsName;
		T itsData;
		UUID itsStoredUUID;
		Date itsDateStored;

		Preset(T data) {
			itsData = data.deepClone();
			itsName = XManifest.UNTITLED;
			itsStoredUUID = UUID.randomUUID();
			itsDateStored = new Date();
		}
	}

	static public class PresetFrameSlot<T extends IDeepCloneable<T>> implements Serializable {
		private static final long serialVersionUID = 1L;
		float[] itsColour;
		private List<Preset<T>> itsPresets = new ArrayList<Preset<T>>();
		int itsLastVersionAccessed = -1;
	
		void deleteVersion(int version) {
			itsPresets.remove(version);
			
			if (itsLastVersionAccessed == version) {
				itsLastVersionAccessed = -1;
			} else if (itsLastVersionAccessed > version) {
				itsLastVersionAccessed--;
			}
		}
		
		void add(Preset<T> preset) {
			itsPresets.add(preset);
			itsLastVersionAccessed = itsPresets.size() - 1;
		}

		void replace(int pos, Preset<T> preset) {
			itsPresets.set(pos, preset);
			itsLastVersionAccessed = pos;
		}
		
		Preset<T> retrieve(int pos) {
			Preset<T> p = itsPresets.get(pos);
			itsLastVersionAccessed = pos;
			return p;
		}
		
		void clear() {
			itsPresets.clear();
			itsLastVersionAccessed = -1;
		}

		int size() {
			return itsPresets.size();
		}
	}

	private PresetFrameSlot<T>[] itsSlots;
	private int[] itsSelectionOriginalLocation = new int[] { -1, -1 };
	private int itsLastSlotAccessed = -1;

	transient private int itsDirty;

	private void locate(int presetSlot, int version) {
		itsSelectionOriginalLocation = new int[] { presetSlot, version };
	}
	
	@SuppressWarnings("unchecked")
	public PresetFrame(int numSlots) {
		itsSlots = new PresetFrameSlot[numSlots];

		for (int i = 0; i < numSlots; i++) {
			itsSlots[i] = new PresetFrameSlot<T>();
		}
	}

	protected UUID storeNewPreset(T data, int presetSlot) {
		Preset<T> p = new Preset<T>(data);

		PresetFrameSlot<T> slot = itsSlots[presetSlot];
		slot.add(p);
		int last = getNumVersions(presetSlot) - 1;
		slot.itsLastVersionAccessed = last;
		locate(presetSlot, last);
		itsLastSlotAccessed = presetSlot;
		makeDirty();
		return p.itsStoredUUID;
	}
	
	protected void setLastSlotAccessed(int slot) {
		itsLastSlotAccessed = slot;
	}	

	@Override
	public T retrievePresetObject(int presetSlot, int version) {
		PresetFrameSlot<T> slot = itsSlots[presetSlot];
		itsLastSlotAccessed = presetSlot;
		slot.itsLastVersionAccessed = version;
		return slot.retrieve(version).itsData.deepClone();
	}

	protected void replacePreset(T data, int presetSlot, int version) {
		Preset<T> p = new Preset<T>(data);
		itsSlots[presetSlot].replace(version, p);
		itsLastSlotAccessed = presetSlot;
		locate(presetSlot, version);
		makeDirty();
	}

	protected void removeAllVersions(int presetSlot) {
		itsSlots[presetSlot].clear();
		locate(-1, -1);
		makeDirty();
	}

	@Override
	public void setName(int presetSlot, int version, String name) {
		itsSlots[presetSlot].retrieve(version).itsName = name;
		makeDirty();
	}

	@Override
	public String getName(int presetSlot, int version) {
		return itsSlots[presetSlot].retrieve(version).itsName;
	}

	@Override
	public void setColour(int presetSlot, float[] rgb) {
		itsSlots[presetSlot].itsColour = rgb.clone();
		makeDirty();
	}

	@Override
	public float[] getColour(int presetSlot) {
		//	Defensive:
		if (itsSlots[presetSlot].itsColour == null) {
			return new float[] { 1f, 1f, 1f };
		} else {
			return itsSlots[presetSlot].itsColour.clone();
		}
	}

	@Override
	public Date getDateStored(int presetSlot, int version) {
		return itsSlots[presetSlot].retrieve(version).itsDateStored;
	}


	@Override
	public void setDateStored(int presetSlot, int version, Date date) {
		itsSlots[presetSlot].retrieve(version).itsDateStored = date;
		makeDirty();
	}

	@Override
	public int getNumPresetSlots() {
		return itsSlots.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setNumPresetSlots(int numSlots) {
		PresetFrameSlot<T>[] oldSlots = itsSlots;
		itsSlots = new PresetFrameSlot[numSlots];
		
		for (int i = 0; i < itsSlots.length; i++) {
			if (i >= oldSlots.length) {
				itsSlots[i] = new PresetFrameSlot<T>();
			} else {
				itsSlots[i] = oldSlots[i];
			}
		}
		
		makeDirty();
	}

	@Override
	public int getNumVersions(int presetSlot) {
		if (presetSlot >= itsSlots.length) {
			//	Protection against small preset banks in jumbo LCD panels (in our testing patchers).
			itsLogger.error("getNumVersions overflow: requested={}, length={}", presetSlot, itsSlots.length);
			return 0;
		} else {
			return itsSlots[presetSlot].size();
		}
	}

	@Override
	public int getLastSlotAccessed() {
		return itsLastSlotAccessed;
	}

	@Override
	public int getLastVersionAccessed(int presetSlot) {
		return itsSlots[presetSlot].itsLastVersionAccessed;
	}

	@Override
	public int[] findByUUID00(UUID id) {
		for (int slotNum = 0; slotNum < itsSlots.length; slotNum++) {
			PresetFrameSlot<T> slot = itsSlots[slotNum];
			for (int ver = 0; ver < slot.size(); ver++) {
				if (slot.retrieve(ver).itsStoredUUID.equals(id)) {
					return new int[] { slotNum, ver };
				}
			}
		}

		return null;
	}

	@Override
	public UUID getUUID(int presetSlot, int version) {
		return itsSlots[presetSlot].retrieve(version).itsStoredUUID;
	}

	@Override
	public int[] getSelectionOriginalLocation() {
		return itsSelectionOriginalLocation;
	}

	@Override
	public void deleteVersion(int presetSlot, int version) {
		PresetFrameSlot<T> slot = itsSlots[presetSlot];
		
		int[] orig = getSelectionOriginalLocation();
		
		slot.deleteVersion(version);
		
		if (orig[0] == presetSlot) {
			if (orig[1] == version) {		//	Wiped the version we're pointing at.
				locate(-1, -1);
			} else if (orig[1] > version) {
				locate(presetSlot, orig[1] - 1);	//	Bump down.
			}
		}
		
		makeDirty();
	}

	@Override
	public void clear(int presetSlot) {
		PresetFrameSlot<T> slot = itsSlots[presetSlot];
		slot.clear();
		makeDirty();
	}
	
	@Override
	public void clearAll() {
		for (PresetFrameSlot<T> slot: itsSlots) {
			slot.clear();
		}
		
		locate(-1, -1);
		makeDirty();
	}
	
	protected void prettyprintSlots(IMultiLinePrinter printer) {
		PrettyPrintBuffer buff = new PrettyPrintBuffer("SLOTS");

		for (int i = 0; i < getNumPresetSlots(); i++) {
			int depth = getNumVersions(i);
			
			if (depth == 0) {
				buff.printLine(String.format("slot %d: ---", i));
			} else {
				for (int j = 0; j < depth; j++) {
					buff.printLine(
						String.format("slot %d/ver %d: \"%s\" [%s %s]",
								      i, j, getName(i, j),
								      	    getUUID(i, j).toString(),
								            getDateStored(i, j).toString()
									 )
					);
				}
			}
		}
		
		buff.prettyprint(printer);
	}
	
	
	@Override
	public boolean isDirty() {
		return (itsDirty > 0);
	}
	
	private void makeDirty() {
		itsDirty++;
	}

	@Override
	public void makeClean() {
		itsDirty = 0;
	}
}
