package kill.presetmanager;

import java.io.Serializable;

import org.alexandrehd.persister.legacy.ScalarParam;
import org.alexandrehd.persister.legacy.VectorParam;

/** The encapsulated state of a single preset (by which we also mean any live
 	edit buffer).
 	
 	Presets in the storage bank are versions (versioning is a property of the preset
 	slot).

 	@author nick
 */

abstract public class PresetState implements Serializable {
	private static final long serialVersionUID = -8525568832832329864L;
	public ScalarParam scalars[] = new ScalarParam[0];
	public VectorParam vectors[] = new VectorParam[0];
	
	public PresetState(ScalarParam[] scalars,
					   VectorParam[] vectors) {
		this.scalars = scalars;
		this.vectors = vectors;
	}
	
	public PresetState() { }
	
	public int getNumScalarParams() {
		return scalars.length;
	}

	public int getNumVectorParams() {
		return vectors.length;
	}
	
	public void setNumScalarParams(int newSize) {
		ScalarParam[] old = scalars;
		scalars = new ScalarParam[newSize];
		for (int i = 0; i < newSize; i++) {
			if (i < old.length) {
				scalars[i] = old[i];
			} else {
				scalars[i] = new ScalarParam(0f);
			}
		}
	}
	
	public void allowScalarParam(int i) {
		if (i >= scalars.length) {
			setNumScalarParams(i + 1);
		}
	}
	
	public void setNumVectorParams(int newSize) {
		VectorParam[] old = vectors;
		vectors = new VectorParam[newSize];
		for (int i = 0; i < newSize; i++) {
			if (i < old.length) {
				vectors[i] = old[i];
			} else {
				vectors[i] = new VectorParam();
			}
		}
	}
	
	public void allowVectorParam(int i) {
		if (i >= vectors.length) {
			setNumVectorParams(i + 1);
		}
	}
}
