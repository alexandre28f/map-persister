package mi.presetmanager.newps.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EditBufferState {
	private float[] itsScalarParams;
	private float[][] itsVectorParams;
	
	@JsonCreator
	public EditBufferState(@JsonProperty("scalarParams") float[] scalarParams,
						   @JsonProperty("vectorParams") float[][] vectorParams
						  ) {
		this.itsScalarParams = scalarParams;
		this.itsVectorParams = vectorParams;
	}

	public float[] getScalarParams() {
		return itsScalarParams;
	}

	public float[][] getVectorParams() {
		return itsVectorParams;
	}
}
