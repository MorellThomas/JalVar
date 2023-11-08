---
channel: release
version: 2.11.2.1
date: 2022-04-05
---

## New Features

- <!-- JAL-3973 -->  Distribution Tarball includes git commit and branch details


## Issues Resolved

- <!-- JAL-3975 -->  Keyboard mode (F2) stops working after using the "Create sequence feature" dialog
- <!-- JAL-3976 -->  3D Structure chooser fails to select structures from 3D-beacons and pops up a 'null' dialog
- <!-- JAL-3985 -->  PDB FTS query results in error dialog containing '414' [URL too long]
- <!-- JAL-3980 JAL-3981 -->  Sequence ID tooltip not shown during long running retrieval/crossref operations (affects at least 2.11.1 onwards)
- <!-- JAL-3973 -->  Cannot build Jalview 2.11.2.0 via gradle from its source tarball


### New Known Issues
- <!-- JAL-3984 -->  Keyboard mode (F2) stops working after using the "Text Colour" dialog
- <!-- JAL-3873 -->  Colour by->all views doesn't allow colouring same structure from different views (since 2.11.2.0)
- <!-- JAL-3886 -->  Pfam and Rfam alignment retrieval as gzipped stockholm doesn't work on JalviewJS build of 2.11.2
- <!-- JAL-3972 -->  Java 11 Only: Jalview 2.11.2.0 OSX install not working due to VAqua requiring sun.awt.image.MultiResolutionImage
- <!-- JAL-3981 -->  Sequence Details can take a long time to be displayed for heavily annotated sequences (all versions)
