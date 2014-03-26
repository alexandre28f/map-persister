`-*- mode: markdown; mode: visual-line; -*-`

# `map-persister`

- `cassiel` [![Build Status](https://secure.travis-ci.org/cassiel/map-persister.png)](http://travis-ci.org/cassiel/map-persister)
- `alexandre28f` [![Build Status](https://secure.travis-ci.org/alexandre28f/map-persister.png)](http://travis-ci.org/alexandre28f/map-persister)

## Thoughts

A file path on disk might point to a flat serialised object (`foo.ser`), or the root directory of a layered serialisation (`foo/`). (This is assuming `.ser` as extension for flat files and no extension for top-level directories.) What happens if we try to save one format when the other exists? And what happens when we attempt to persist to a directory which already exists?

Here's how the unit tests are currently shaking down:

