---
channel: release
version: 2.9.0b1
date: 2015-10-08
---

## New Features



### General
- Updated Spanish translations of localized text for 2.9


### Application
- Signed OSX InstallAnywhere installer<br/>
- Support for per-sequence based annotations in BioJSON


### Applet
- Split frame example added to applet examples page


### Build and Deployment
- <!--  JAL-1888 -->  New ant target for running Jalview's test suite


## Issues Resolved



### General
  - Mapping of cDNA to protein in split frames incorrect when sequence start > 1
  - Broken images in filter column by annotation dialog documentation
  - Feature colours not parsed from features file
  - Exceptions and incomplete link URLs recovered when loading a features file containing HTML tags in feature description


### Application
  - Annotations corrupted after BioJS export and reimport
  - Incorrect sequence limits after Fetch DB References with 'trim retrieved sequences'
  - Incorrect warning about deleting all data when deleting selected columns
  - Patch to build system for shipping properly signed JNLP templates for webstart launch
  - EMBL-PDBe fetcher/viewer dialogs do not offer unreleased structures for download or viewing
  - Tab/space/return keystroke operation of EMBL-PDBe fetcher/viewer dialogs works correctly
  - Disabled 'minimise' button on Jalview windows running on OSX to workaround redraw hang bug
  - Split cDNA/Protein view position and geometry not recovered from jalview project
  - Initial enabled/disabled state of annotation menu sorter 'show autocalculated first/last' corresponds to alignment view
  - Restoring of Clustal, RNA Helices and T-Coffee color schemes from BioJSON


### Applet
  - Reorder sequences mirrored in cDNA/Protein split frame
  - Applet with Jmol examples not loading correctly
