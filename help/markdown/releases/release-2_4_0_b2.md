---
channel: release
version: 2.4.0.b2
date: 2009-10-28
---

## New Features

- Experimental support for google analytics usage tracking.
- Jalview privacy settings (user preferences and docs).


## Issues Resolved

- Race condition in applet preventing startup in jre1.6.0u12+.
- Exception when feature created from selection beyond length of sequence.
- Allow synthetic PDB files to be imported gracefully
- Sequence associated annotation rows associate with all sequences with a given id
- Find function matches case-insensitively for sequence ID string searches
- Non-standard characters do not cause pairwise alignment to fail with exception


### Application Issues
- Sequences are now validated against EMBL database
- Sequence fetcher fetches multiple records for all data sources


### InstallAnywhere Issues
- Dock icon works for Mac OS X java (Mac 1.6 update issue with installAnywhere mechanism)
- Command line launching of JARs from InstallAnywhere version (java class versioning error fixed)
