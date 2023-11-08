---
channel: release
version: 2.10.5
date: 2018-09-10
---

## New Features



  - <!-- JAL-3101 -->  Default memory for Jalview webstart and InstallAnywhere increased to 1G.
  - <!-- JAL-247 -->  Hidden sequence markers and representative sequence bolding included when exporting alignment as EPS, SVG, PNG or HTML. *Display is configured via the Format menu, or for command-line use via a Jalview properties file.*
  - <!-- JAL-3076 -->  Ensembl client updated to Version 7 REST API and sequence data now imported as JSON.
  - <!-- JAL-3065 -->  Change in recommended way of starting Jalview via a Java command line: add jars in lib directory to CLASSPATH, rather than via the deprecated java.ext.dirs property.


### Development
  - <!-- JAL-3047 -->  Support added to execute test suite instrumented with [Open Clover](http://openclover.org/)


## Issues Resolved



  - <!-- JAL-3104 -->  Poorly scaled bar in quality annotation row shown in Feredoxin Structure alignment view of example alignment.
  - <!-- JAL-2854 -->  Annotation obscures sequences if lots of annotation displayed.
  - <!-- JAL-3107 -->  Group conservation/consensus not shown for newly created group when 'Apply to all groups' selected
  - <!-- JAL-3087 -->  Corrupted display when switching to wrapped mode when sequence panel's vertical scrollbar is visible.
  - <!-- JAL-3003 -->  Alignment is black in exported EPS file when sequences are selected in exported view.
  - <!-- JAL-3059 -->  Groups with different coloured borders aren't rendered with correct colour.
  - <!-- JAL-3092 -->  Jalview could hang when importing certain types of knotted RNA secondary structure.
  - <!-- JAL-3095 -->  Sequence highlight and selection in trimmed VARNA 2D structure is incorrect for sequences that do not start at 1.
  - <!-- JAL-3061 -->  '.' inserted into RNA secondary structure annotation when columns are inserted into an alignment, and when exporting as Stockholm flatfile.
  - <!-- JAL-3053 -->  Jalview annotation rows containing upper and lower-case 'E' and 'H' do not automatically get treated as RNA secondary structure.
  - <!-- JAL-3106 -->  .jvp should be used as default extension (not .jar) when saving a Jalview project file.
  - <!-- JAL-3105 -->  Mac Users: closing a window correctly transfers focus to previous window on OSX


### Java 10 Issues Resolved
  - <!-- JAL-2988 -->  OSX - Can't save new files via the File or export menus by typing in a name into the Save dialog box.
  - <!-- JAL-2988 JAL-2968 -->  Jalview now uses patched version of the [VAqua5](https://violetlib.org/vaqua/overview.html) 'look and feel' which has improved compatibility with the latest version of OSX.
