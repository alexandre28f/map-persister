package org.alexandrehd.presetter;

import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveableState {
	private Map<String, SaveableState> itsContentMap00;
	private float[] its1DParam00;
	private float[][] its2DParam00;
	
	@JsonCreator
	public SaveableState(@JsonProperty("contentMap00") Map<String, SaveableState> contentMap00,
						 @JsonProperty("oneDParam00") float[] oneDParam00,
						 @JsonProperty("twoDParam00") float[][] twoDParam00
						) {
		itsContentMap00 = contentMap00;
		its1DParam00 = oneDParam00;
		its2DParam00 = twoDParam00;
	}
	
	public Map<String, SaveableState> getContentMap00() {
		return itsContentMap00;
	}
	
	public float[] getOneDParam00() {
		return its1DParam00;
	}

	public float[][] getTwoDParam00() {
		return its2DParam00;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(its1DParam00);
		result = prime * result + Arrays.hashCode(its2DParam00);
		result = prime * result
				+ ((itsContentMap00 == null) ? 0 : itsContentMap00.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SaveableState other = (SaveableState) obj;
		if (!Arrays.equals(its1DParam00, other.its1DParam00))
			return false;
		if (!Arrays.deepEquals(its2DParam00, other.its2DParam00))
			return false;
		if (itsContentMap00 == null) {
			if (other.itsContentMap00 != null)
				return false;
		} else if (!itsContentMap00.equals(other.itsContentMap00))
			return false;
		return true;
	}
}
