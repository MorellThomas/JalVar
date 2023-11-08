---
channel: release
version: 2.8.2
date: 2014-12-03
---

## New Features



### General
- Updated Java code signing certificate donated by Certum.PL.
- Features and annotation preserved when performing pairwise alignment
- RNA pseudoknot annotation can be imported/exported/displayed
- 'colour by annotation' can colour by RNA and protein secondary structure
- Warn user if 'Find' regular expression is invalid (*mentioned post-hoc with 2.9 release*)


### Application
- Extract and display secondary structure for sequences with 3D structures
- Support for parsing RNAML
- Annotations menu for layout
  - sort sequence annotation rows by alignment
  - place sequence annotation above/below alignment annotation
- Output in Stockholm format
- Internationalisation: improved Spanish (es) translation
- Structure viewer preferences tab
- Disorder and Secondary Structure annotation tracks shared between alignments
- UCSF Chimera launch and linked highlighting from Jalview
- Show/hide all sequence associated annotation rows for all or current selection
- disorder and secondary structure predictions available as dataset annotation
- Per-sequence rna helices colouring
- Sequence database accessions imported when fetching alignments from Rfam
- update VARNA version to 3.91
- New groovy scripts for exporting aligned positions, conservation values, and calculating sum of pairs scores.
- Command line argument to set default JABAWS server
- include installation type in build properties and console log output
- Updated Jalview project format to preserve dataset annotation


## Issues Resolved



### Application
- Distinguish alignment and sequence associated RNA structure in structure->view->VARNA
- Raise dialog box if user deletes all sequences in an alignment
- Pressing F1 results in documentation opening twice
- Sequence feature tooltip is wrapped
- Double click on sequence associated annotation selects only first column
- Redundancy removal doesn't result in unlinked leaves shown in tree
- Undos after several redundancy removals don't undo properly
- Hide sequence doesn't hide associated annotation
- User defined colours dialog box too big to fit on screen and buttons not visible
- author list isn't updated if already written to Jalview properties
- Popup menu won't open after retrieving sequence from database
- File open window for associate PDB doesn't open
- Left-then-right click on a sequence id opens a browser search window
- Cannot open sequence feature shading/sort popup menu in feature settings dialog
- better tooltip placement for some areas of Jalview desktop
- Allow addition of JABAWS Server which doesn't pass validation
- Web services parameters dialog box is too large to fit on screen
- Muscle nucleotide alignment preset obscured by tooltip
- JABAWS preset submenus don't contain newly defined user preset
- MSA web services warns user if they were launched with invalid input
- Jalview cannot contact DAS Registy when running on Java 8
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1273'>JAL-1273</a>] -->   'Superpose with' submenu not shown when new view created


### Deployment and Documentation
- 2G and 1G options in launchApp have no effect on memory allocation
- launchApp service doesn't automatically open www.jalview.org/examples/exampleFile.jar if no file is given
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1511'>JAL-1511</a>] -->   InstallAnywhere reports cannot find valid JVM when Java 1.7_055 is available


### Application Known issues
- <!-- [<a href='http://issues.jalview.org/browse/JAL-830'>JAL-830</a>] -->   corrupted or unreadable alignment display when scrolling alignment to right
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1329'>JAL-1329</a>] -->   retrieval fails but progress bar continues for DAS retrieval with large number of ID
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1486'>JAL-1486</a>] -->   flatfile output of visible region has incorrect sequence start/end
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1487'>JAL-1487</a>] -->   rna structure consensus doesn't update when secondary structure tracks are rearranged
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1591'>JAL-1591</a>] -->   invalid rna structure positional highlighting does not highlight position of invalid base pairs
- <!-- <a href='http://issues.jalview.org/browse/JAL-1539'>JAL-1539</a>] -->   out of memory errors are not raised when saving Jalview project from alignment window file menu
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1576'>JAL-1576</a>] -->   Switching to RNA Helices colouring doesn't propagate to structures
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1577'>JAL-1577</a>] -->   colour by RNA Helices not enabled when user created annotation added to alignment
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1439'>JAL-1439</a>] -->   Jalview icon not shown on dock in Mountain Lion/Webstart


### Applet Known Issues
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1394'>JAL-1394</a>] -->   JalviewLite needs JmolApplet and VARNA-3.91 jar dependencies
- <!-- [<a href='http://issues.jalview.org/browse/JAL-1510'>JAL-1510</a>] -->   Jalview and Jmol example not compatible with IE9
- Sort by annotation score doesn't reverse order when selected
