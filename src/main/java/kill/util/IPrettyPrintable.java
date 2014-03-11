package kill.util;

public interface IPrettyPrintable {
	/**	Things which can be (multiline) pretty-printed.

		@param printer the printing object to output to.
	 */

	void prettyprint(IMultiLinePrinter printer);
}
