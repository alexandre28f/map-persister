package mi.presetmanager.newps.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleZoneState {
	@JsonCreator
	public SingleZoneState(@JsonProperty("keyState00") EditBufferState keyState00,
						   @JsonProperty("zonePosition") float zonePosition,
						   @JsonProperty("zoneEnabled") boolean zoneEnabled
						  ) {
		this.itsKeyState00 = keyState00;
		this.itsZonePosition = zonePosition;
		this.itsZoneEnabled = zoneEnabled;
	}

	private EditBufferState itsKeyState00;
	private float itsZonePosition;
	private boolean itsZoneEnabled;

	public EditBufferState getKeyState00() {
		return itsKeyState00;
	}

	public float getZonePosition() {
		return itsZonePosition;
	}

	public boolean getZoneEnabled() {
		return itsZoneEnabled;
	}
}
