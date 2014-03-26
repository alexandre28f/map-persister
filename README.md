`-*- mode: markdown; mode: visual-line; -*-`

# `map-persister`

- `cassiel` [![Build Status](https://secure.travis-ci.org/cassiel/map-persister.png)](http://travis-ci.org/cassiel/map-persister)
- `alexandre28f` [![Build Status](https://secure.travis-ci.org/alexandre28f/map-persister.png)](http://travis-ci.org/alexandre28f/map-persister)

## Thoughts

A file path on disk might point to a flat serialised object (`foo.ser`), or the root directory of a layered serialisation (`foo/`). (This is assuming `.ser` as extension for flat files and no extension for top-level directories.) What happens if we try to save one format when the other exists? And what happens when we attempt to persist to a directory which already exists?

Here's how the unit tests are currently shaking down:

- At top-level: creating a layered state (`foo/`) deletes an unlayered one (`foo.ser`), and vice versa. This rule is recursive, whenever a state is saved at a different depth than the previously saved one.

- A layered state may be saved to a new directory, or to one which already holds a saved state. It may not be saved to an arbitrary directory which isn't a saved state. (This is to prevent accidental erasure.) At the moment we're marking the top level of saved states with a place-holder file, but we should probably use a dedicated directory name extension instead (`foo.saved.d`?).

- At lower levels (below the top level), we aren't doing this sanity check, although we still need to do the layered vs. flat checks all the way down.

- Items in a layered tree are added or removed as required, depending on the keys present in the map at that level. We have to remove bogus keys that happen to be in a directory on save; those entries will have a read attempt on them on `unpersist`, and in any case they might correspond to keys which have been explicitly removed. It's only safe to manually add things after a save and before a read.

- We'll allow arbitrary characters in (string) keys - if we were to slug them, there would be no reliable mapping back into memory. We'll escape what we regard as illegal characters, URL-style.

- The jury is out regarding timestamps on files (or, put another way, not overwriting files representing entries which have not changed). The brute force method is to re-import and equality-test everything we're exporting; a slightly neater way is probably to "attach" the layered persister object to a location, and have it keep a clone of the original map, so that it can do a delta on save. (This could be the fall-back for a persister that's created on the fly - it re-reads to a notional cloned map, then saves according to delta analysis.)

## Operation

We want to write a nested hashmap `M_new`, at folder depth `D`, to a location which we assume contains a representation (to some arbitrary depth) of hashmap `M_old`.

```
function write(M_new, M_old, location, D):
        for all keys k in M_old which are not in M_new:
                erase from file system (directory or flat file) at location/k
                
        for all keys k in M_new which are not in M_old:
                create structure (directory or flat file) at location/k according to depth D
                
        for all keys k in both M_old and M_new:
                let obj_old = M_old[k] and obj_new = M_new[k]

                structurally compare obj_old and obj_new
                
                if not (obj_old equals obj_new):        // Need to replace tree here.
                        if obj_old is map and location/k is dir and obj_new is map and D > 0:
                                RECURSE(obj_new[k], obj_old[k], location/k, D-1)
                        else:
                                erase at location/k
                                create structure for obj_new at location/k depth D
```
