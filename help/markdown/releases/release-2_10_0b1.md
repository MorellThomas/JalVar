---
channel: release
version: 2.10.0b1
date: 2016-10-25
---

## New Features



### Application
- 3D Structure chooser opens with 'Cached structures' view if structures already loaded
- Progress bar reports models as they are loaded to structure views


## Issues Resolved



### General
  - Colour by conservation always enabled and no tick shown in menu when BLOSUM or PID shading applied
  - FER1_ARATH and FER2_ARATH labels were switched in example sequences/projects/trees


### Application
  - Jalview projects with views of local PDB structure files saved on Windows cannot be opened on OSX
  - Multiple structure views can be opened and superposed without timeout for structures with multiple models or multiple sequences in alignment
  - Cannot import or associated local PDB files without a PDB ID HEADER line
  - RMSD is not output in Jmol console when superposition is performed
  - Drag and drop of URL from Browser fails for Linux and OSX versions earlier than El Capitan
  - ENA client ignores invalid content from ENA server
  - Exceptions are not raised in console when ENA client attempts to fetch non-existent IDs via Fetch DB Refs UI option
  - Exceptions are not raised in console when a new view is created on the alignment
  - OSX right-click fixed for group selections: CMD-click to insert/remove gaps in groups and CTRL-click to open group pop-up menu


### Build and deployment
  - URL link checker now copes with multi-line anchor tags


### New Known Issues
  - Drag and drop from URL links in browsers do not work on Windows
