package org.alexandrehd.persister;

import java.io.File;

/**	Simple parent class to handle root filenames for loading/saving.

	@author nick
 */

public class MapIO {
	private File itsRoot;
	private final File itsFlatName;
	
	protected MapIO(File root) {
		itsRoot = root;
		itsFlatName = new File(root.getParent(), root.getName() + ".ser");
	}

	protected File getRootName() {
		return itsRoot;
	}

	protected File getFlatName() {
		return itsFlatName;
	}
}
