/*
 * INonZoneableCollector.java
 *
 * Sep 16, 2012 
 */
package mi.m.dispatch;

import java.util.List;

/**
 * An interface for an object that will collect non-zoneable parameter
 * information from all of a module's submodules.
 *
 * @author Roth Michaels (<i><a href="mailto:roth@rothmichaels.us">roth@rothmichaels.us</a></i>)
 *
 */
public interface INonZoneableCollector {

	/**
	 * Query all zoneable submodules for their non-zoneable parameters.
	 * 
	 * @return a list containing an array of scalar enumerated scalar
	 * parameter labels for each submodule for hte parent whose context
	 * this {@code INonZoneableCollector} was created in.
	 */
	List<Enum<?>[]> getNonZoneableScalarParametersForAllPanels();
	
	/**
	 * Query all zoneable submodules for their non-zoneable parameters.
	 * 
	 * @return a list containing an array of vector enumerated scalar
	 * parameter labels for each submodule for hte parent whose context
	 * this {@code INonZoneableCollector} was created in.
	 */
	List<Enum<?>[]> getNonZoneableVectorParametersForAllPanels();
}
