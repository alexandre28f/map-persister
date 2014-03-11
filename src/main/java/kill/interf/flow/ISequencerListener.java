package kill.interf.flow;

import java.util.UUID;

import kill.interf.IEditState;

/**	Something which listens to parameter messages (including entire edit states) from the sequencer.

		@author nick
 */

public interface ISequencerListener {
		/**	Process a scalar parameter change.

				@param paramId the parameter ID
				@param value the scalar value
		 */

		void handleSeqScalar(int paramId, float value);

		/**	Process a vector parameter change.

				@param paramId the parameter ID
				@param values the vector of values
		 */

		void handleSeqVector(int paramId, float[] values);

		/**	New edit buffer state. */

		void handleSeqEditState(IEditState editState);

		/**	When a sequence is loaded, it attempts to establish presets by UUID. These
				calls aren't made during normal playback.

				@param id the unique ID of the preset to load (if present)
		 */

		void handleSeqRecallByUUID(UUID id);

		/**	Flash (well, flag indefinitely) a preset change. This has no
				operational effect, but provides visual feedback when a sequence
				is playing. This will almost certainly be ignored by any "real"
				instruments or controllers (and the [FIXME mi.datamanager.DataManager]
				doesn't send to them),
				but is here because the proxy used for sequence recording has to
				capture these events.

				@param id
		 */

		void handleSeqPresetFlash(UUID id);
}
