package org.alexandrehd.persister;

import java.io.File;

/**	Simple parent class to handle root filenames for loading/saving.

	@author nick
 */

public class MapIO {
	private final File itsRoot;
	private final File itsFlatName;
	
	protected MapIO(File root) {
		itsRoot = root;
		itsFlatName = new File(root.getParentFile(), root.getName() + ".ser");
	}

	protected File getRootPath() {
		return itsRoot;
	}

	protected File getFlatFile() {
		return itsFlatName;
	}
}
