---
channel: release
version: 2.5.1
date: 2010-06-14
---

## Issues Resolved

- Alignment prettyprinter doesn't cope with long sequence IDs
- clustalx colourscheme colours Ds preferentially when both D+E are present in over 50% of the column
- nucleic acid structures retrieved from PDB do not import correctly
- More columns get selected than were clicked on when a number of columns are hidden
- annotation label popup menu not providing correct add/hide/show options when rows are hidden or none are present
- Stockholm format shown in list of readable formats, and parser copes better with alignments from RFAM.
- CSV output of consensus only includes the percentage of all symbols if sequence logo display is enabled


### Applet
- annotation panel disappears when annotation is hidden/removed


### Application
- Alignment view not redrawn properly when new alignment opened where annotation panel is visible but no annotations are present on alignment
- pasted region containing hidden columns is incorrectly displayed in new alignment window
- Jalview slow to complete operations when stdout is flooded (fix is to close the Jalview console)
- typo in AlignmentFrame->View->Hide->all but selected Rregions menu item.
- inconsistent group submenu and Format submenu entry 'Un' or 'Non'conserved
- Sequence feature settings are being shared by multiple distinct alignments
- group annotation not recreated when tree partition is changed
- double click on group annotation to select sequences does not propagate to associated trees
- Mac OSX specific issues:
  - exception raised when mouse clicked on desktop window background
  - Desktop menu placed on menu bar and application name set correctly
  - sequence feature settings not wide enough for the save feature colourscheme button
