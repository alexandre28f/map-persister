package mi.sys;

public class XManifest {
	//  XXX edit_buffers not used anymore...
	public static final int NUM_EDIT_BUFFERS = 1;
	public static final float EPSILON = 0.00001f;
	public static final String PRESETS_PLACEHOLDER = "presets";
	public static final String UNTITLED = "...";
	public static final String NEW_PRINCIPAL_EXTENSION = ".p_presets";
	public static final String NEW_SECONDARY_EXTENSION = ".s_presets";
	
	/** Scripting name of the {@code bpatcher} containing the sequencer controls */
	public static final String SEQUENCER__CONTROL_PATH = "SEQCONTROL";
	
	/**	We should be able to determine the number of secondaries from the appropriate enum. */
	@Deprecated
	public static final int MAX_SECONDARY_PRESETTERS = 10;
	public static final int NUM_PRESET_ZONES = 4;
	public static final int HIDDEN_PANEL = MAX_SECONDARY_PRESETTERS - 1;
	
	public static final double LOG32 = Math.log(32) ;  // 32 = five octaves
	public static final double INVLOG2 = 1 / Math.log(2) ;
	
	public static final float LOG2 = (float) Math.log(2) ;
	
	
	public static final float LOWEST_FREQ = 12 ;  // Lowest playable frequency in hertz
	
	public static final float MUSIC_RANGE = 9.5f ;  // Nb of playable octaves from the Lowest note
	public static final float HIGHEST_FREQ = LOWEST_FREQ * (float)Math.pow(2, MUSIC_RANGE) ;
	
	public static final String EMPTY_MODULE_PATCHER = "empty_patch_for_unloaded_bp";
	
	private XManifest() {
		throw new AssertionError();
	}
}
