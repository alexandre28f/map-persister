package kill.presetmanager.newps.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrincipalPresetState {
	private Map<String, PanelPresetState> itsPanelMap;

	@JsonCreator
	public PrincipalPresetState(@JsonProperty("panelMap") Map<String, PanelPresetState> panelMap) {
		itsPanelMap = panelMap;
	}

	public Map<String, PanelPresetState> getPanelMap() {
		return itsPanelMap;
	}
}
