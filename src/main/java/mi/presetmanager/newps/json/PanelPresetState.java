package mi.presetmanager.newps.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PanelPresetState {
	private EditBufferState itsSingleStateZoneInfo00;
	private MultiState itsMultiStateZoneInfo00;
	
	@JsonCreator
	public PanelPresetState(@JsonProperty("singleStateZoneInfo00") EditBufferState singleStateZoneInfo00,
							@JsonProperty("multiStateZoneInfo00") MultiState multiStateZoneInfo00
						   ) {
		this.itsSingleStateZoneInfo00 = singleStateZoneInfo00;
		this.itsMultiStateZoneInfo00 = multiStateZoneInfo00;
	}

	public EditBufferState getSingleStateZoneInfo00() {
		return itsSingleStateZoneInfo00;
	}

	public MultiState getMultiStateZoneInfo00() {
		return itsMultiStateZoneInfo00;
	}
}
