---
channel: release
version: 2.6
date: 2010-09-26
---

## New Features



### Application
- Support for **Ja**va **b**ioinformatics **a**nalysis **w**eb **s**ervices (JABAWS)
- Web Services preference tab
- Analysis parameters dialog box and user defined preferences
- Improved speed and layout of Envision2 service menu
- Superpose structures using associated sequence alignment
- Export coordinates and projection as CSV from PCA viewer


### Applet
- enable javascript: execution by the applet via the link out mechanism


### Other
- Updated the Jmol Jalview interface to work with Jmol series 12
- The Jalview Desktop and JalviewLite applet now require Java 1.5
- Allow Jalview feature colour specification for GFF sequence annotation files
- New 'colour by label' keword in Jalview feature file type colour specification
- New Jalview Desktop Groovy API method that allows a script to check if it being run in an interactive session or in a batch operation from the Jalview command line


## Issues Resolved

- clustalx colourscheme colours Ds preferentially when both D+E are present in over 50% of the column


### Application
- typo in AlignmentFrame->View->Hide->all but selected Regions menu item
- sequence fetcher replaces ',' for ';' when the ',' is part of a valid accession ID
- fatal OOM if object retrieved by sequence fetcher runs out of memory
- unhandled Out of Memory Error when viewing pca analysis results
- InstallAnywhere builds fail to launch on OS X java 10.5 update 4 (due to apple Java 1.6 update)
- Installanywhere Jalview silently fails to launch


### Applet
- Jalview.getFeatureGroups() raises an ArrayIndexOutOfBoundsException if no feature groups are defined.
