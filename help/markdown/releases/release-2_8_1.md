---
channel: release
version: 2.8.1
date: 2014-06-04
---

## New Features



### General
- Internationalisation of user interface (usually called i18n support) and translation for Spanish locale
- Define/Undefine group on current selection with Ctrl-G/Shift Ctrl-G
- Improved group creation/removal options in alignment/sequence Popup menu
- Sensible precision for symbol distribution percentages shown in logo tooltip.
- Annotation panel height set according to amount of annotation when alignment first opened


### Application
- Interactive consensus RNA secondary structure prediction VIENNA RNAAliFold JABA 2.1 service
- Select columns containing particular features from Feature Settings dialog
- View all 'representative' PDB structures for selected sequences
- Update Jalview project format:
  - New file extension for Jalview projects '.jvp'
  - Preserve sequence and annotation dataset (to store secondary structure annotation,etc)
  - Per group and alignment annotation and RNA helix colouring
- New similarity measures for PCA and Tree calculation (PAM250)
- Experimental support for retrieval and viewing of flanking regions for an alignment


## Issues Resolved



### Application
- logo keeps spinning and status remains at queued or running after job is cancelled
- cannot export features from alignments imported from Jalview/VAMSAS projects
- Buggy slider for web service parameters that take float values
- Newly created RNA secondary structure line doesn't have 'display all symbols' flag set
- T-COFFEE alignment score shading scheme and other annotation shading not saved in Jalview project
- Local file cannot be loaded in freshly downloaded Jalview
- Jalview icon not shown on dock in Mountain Lion/Webstart
- Load file from desktop file browser fails
- Occasional NPE thrown when calculating large trees
- Cannot reorder or slide sequences after dragging an alignment onto desktop
- Colour by annotation dialog throws NPE after using 'extract scores' function
- Loading/cut'n'pasting an empty file leads to a grey alignment window
- Disorder thresholds rendered incorrectly after performing IUPred disorder prediction
- Multiple group annotated consensus rows shown when changing 'normalise logo' display setting
- Find shows blank dialog after 'finished searching' if nothing matches query
- <!--  possibly JAL-599 but commit 7c7a5a297e063d3892dd7e629bc317cdde837b81 associated with JAL-971 --> Null Pointer Exceptions raised when sorting by feature with lots of groups
- <!-- JAL-1476 Work in progress - don't send junk to Jmol --> Errors in Jmol console when structures in alignment don't overlap
- Not all working JABAWS services are shown in Jalview's menu
- JAVAWS version of Jalview fails to launch with 'invalid literal/length code'
- Annotation/RNA Helix colourschemes cannot be applied to alignment with groups (actually fixed in 2.8.0b1)
- RNA Helices and T-Coffee Scores available as default colourscheme


### Applet
- Remove group option is shown even when selection is not a group
- Apply to all groups ticked but colourscheme changes don't affect groups
- Documented RNA Helices and T-Coffee Scores as valid colourscheme name
- Annotation labels drawn on sequence IDs when Annotation panel is not displayed
- Increased font size for dropdown menus on OSX and embedded windows


### Other
- Consensus sequence for alignments/groups with a single sequence were not calculated
- annotation files that contain only groups imported as annotation and junk sequences
- Fasta files with sequences containing '*' incorrectly recognised as PFAM or BLC
- conservation/PID slider apply all groups option doesn't affect background (2.8.0b1)
- redundancy highlighting is erratic at 0% and 100%
- Remove gapped columns fails for sequences with ragged trailing gaps
- AMSA annotation row with leading spaces is not registered correctly on import
- Jalview crashes when selecting PCA analysis for certain alignments
- Opening the colour by annotation dialog for an existing annotation based 'use original colours' colourscheme loses original colours setting
