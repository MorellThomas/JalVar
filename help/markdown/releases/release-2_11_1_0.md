---
channel: release
version: 2.11.1.0
date: 2020-04-22
---

## New Features

- <!-- JAL-3187,JAL-3305,JAL-3304,JAL-3302,JAL-3567 -->  Map 'virtual' codon features shown on protein (or vice versa) for display in alignments, on structure views (including transfer to UCSF chimera), in feature reports and for export.
- <!-- JAL-3121 -->  Feature attributes from VCF files can be exported and re-imported as GFF3 files
- <!-- JAL-3376 -->  Capture VCF "fixed column" values POS, ID, QUAL, FILTER as Feature Attributes
- <!-- JAL-3375 -->  More robust VCF numeric data field validation while parsing
- <!-- JAL-3533 -->  Feature Settings dialog keeps same screen position if reopened
- <!-- JAL-3535 -->  Feature Settings dialog title includes name of associated view
- <!-- JAL-3538 -->  Font anti-aliasing in alignment views enabled by default
- <!-- JAL-3468 -->  Very long feature descriptions truncated in tooltips and menus
- <!-- JAL-3549 -->  Warn if Sort by Score or Density attempted with no feature types visible
- <!-- JAL-3574 -->  Improved support for filtering feature attributes with large integer values


### Jalview Installer
- <!-- JAL-3449 -->  Versions for install4j and getdown and installer template version reported in console (may be null when Jalview launched as executable jar or via conda)
- <!-- JAL-3393 -->  Layout improvements for OSX .dmg Finder and higher quality background images
- <!-- JAL-3394 -->  New installer/application launcher generated with install4j 8.0.4
- <!-- JAL-3420 -->  Jalview File Associations shown for Unix Platforms
- <!-- JAL-3477 -->  Improved defaults for maximum memory setting when running on large memory machines


### Release processes
- <!-- JAL-3508 -->  New point release version scheme - 2.11.1.0
- <!-- JAL-3577 -->  'Jalview Test' installers/apps for easier access to test-release channel builds


### Build System
- <!-- JAL-3510 -->  Clover updated to 4.4.1
- <!-- JAL-3513 -->  Test code included in Clover coverage report


### Groovy Scripts
- <!--  JAL-3547 -->  exportconsensus.groovy prints a FASTA file to stdout containing the consensus sequence for each alignment in a Jalview session
- <!-- JAL-3578 -->  ComputePeptideVariants.groovy to translate genomic sequence_variant annotation from CDS as missense_variant or synonymous_variant on protein products.


## Issues Resolved

- <!-- JAL-3581 -->  Hidden sequence markers still visible when 'Show hidden markers' option is not ticked
- <!-- JAL-247 -->  Hidden sequence markers not shown in EPS and PNG output when 'Automatically set ID width' is set in jalview preferences or properties file
- <!-- JAL-3571 -->  Feature Editor dialog can be opened when 'Show Sequence Features' option is not ticked
- <!-- JAL-3549 -->  Undo 'Null' operation shown after sort by buttons in Feature Settings dialog are clicked when no features are visible
- <!-- JAL-3412 -->  ID margins for CDS and Protein views not equal when split frame is first opened
- <!-- JAL-3296 -->  Sequence position numbers in status bar not correct after editing a sequence's start position
- <!-- JAL-3377 -->  Alignment is misaligned in wrapped mode with annotation and exceptions thrown when only a few columns shown in wrapped mode
- <!-- JAL-3386 -->  Sequence IDs missing in headless export of wrapped alignment figure with annotations
- <!-- JAL-3388-->  Sorting Structure Chooser table by Sequence ID fails with ClassCastException
- <!-- JAL-3389 -->  Chimera session not restored from Jalview Project
- <!-- JAL-3441 -->  Double-click on 'Show feature' checkbox in feature settings dialog also selects columns
- <!-- JAL-3473 -->  SpinnerNumberModel causes IllegalArgumentException in some circumstances
- <!-- JAL-3534 -->  Multiple feature settings dialogs can be opened for a view
- <!-- JAL-2764 -->  Feature Settings dialog is orphaned if alignment window is closed
- <!-- JAL-3406 -->  Credits missing some authors in Jalview help documentation for 2.11.0 release
- <!-- JAL-3529 -->  Export of Pfam alignment as Stockholm includes Pfam ID as sequence's accession rather than its Uniprot Accession


### Java 11 Compatibility issues
- <!-- JAL-2987 -->  OSX - Can't view some search results in PDB/Uniprot search panel


### Installer
- <!-- JAL-3447 -->  Jalview should not create file associations for 3D structure files (.pdb, .mmcif. .cif)


### Repository and Source Release
- <!-- JAL-3474 -->  removed obsolete .cvsignore files from repository
- <!-- JAL-3541 -->  Clover report generation running out of memory


### New Known Issues
- <!-- JAL-3523 -->  OSX - Current working directory not preserved when Jalview.app launched with parameters from command line
- <!--  JAL-3525 -->  Sequence IDs aligned to wrong margin and clipped in headless figure export when Right Align option enabled
- <!-- JAL-3542 -->  Jalview Installation type always reports 'Source' in console output
- <!-- JAL-3562 -->  Test Suite: Certain Functional tests fail on jalview's bamboo server but run fine locally.
