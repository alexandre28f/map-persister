package mi.util;

import java.util.ArrayList;
import java.util.List;

/**	A sequence of lines, to be indented. Pretty-printable objects can be
 	printed to one of these, and one of these can itself be pretty-printed
 	(with indentation and a label).
 	
 	@author nick
 */

public class PrettyPrintBuffer implements IMultiLinePrinter, IPrettyPrintable {
	private String itsLabel;
	private List<String> itsContents = new ArrayList<String>();

	public PrettyPrintBuffer(String label) {
		itsLabel = label;
	}

	public PrettyPrintBuffer(String label, IPrettyPrintable printable00) {
		this(label);
		if (printable00 == null) {
			printLine("(null)");
		} else {
			printable00.prettyprint(this);
		}
	}

	public PrettyPrintBuffer(PrintStamp printStamp) {
		this(printStamp.toString());
	}

	/**	Print a line into this buffer.

		@see mi.util.IMultiLinePrinter#printLine(java.lang.String)
	 */

	@Override
	public void printLine(String line) {
		itsContents.add(line);
	}

	/**	Print this buffer to an enclosing printing object. Print
	 	the label and indent the contents.

		@see mi.util.IPrettyPrintable#prettyprint(mi.util.IMultiLinePrinter)
	 */

	@Override
	public void prettyprint(IMultiLinePrinter printer) {
		printer.printLine(itsLabel);
		for (String s: itsContents) {
			printer.printLine("    " + s);
		}
	}
}
