package org.alexandrehd.presetter;

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
}
