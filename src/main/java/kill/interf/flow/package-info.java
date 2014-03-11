/* -*- fill-column: 100; -*- */

/** The flow package.

    <P>This package is a collection of interfaces.

    <P>When recording, sequencing or playing back event streams, events flow through components in
    the sequencer. We use these interfaces to specify what kinds of event data various components
    will accept, and under what conditions this data might be provided. In particular, some
    components might pass messages "downstream" differently depending on their origin, to avoid
    feedback loops.
 */

package mi.interf.flow;
