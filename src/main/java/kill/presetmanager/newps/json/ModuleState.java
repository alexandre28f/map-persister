package kill.presetmanager.newps.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleState {
	private String itsModuleClassName;
	private PrincipalPresetState itsPrincipalPresetState;

	@JsonCreator
	public ModuleState(@JsonProperty("moduleClassName") String moduleClassName,
					   @JsonProperty("principalPresetState") PrincipalPresetState principalPresetState) {
		this.itsModuleClassName = moduleClassName;
		this.itsPrincipalPresetState = principalPresetState;
	}

	public String getModuleClassName() {
		return itsModuleClassName;
	}
	
	public PrincipalPresetState getPrincipalPresetState() {
		return itsPrincipalPresetState;
	}
}
