---
channel: release
version: 2.11.1.1
date: 2020-09-17
---

## New Features

- <!-- JAL-3638 -->  Shift+arrow keys navigate to next gap or residue in cursor mode
- <!-- JAL-3695 -->  Support import of VCF 4.3 by updating HTSJDK from 2.12 to 2.23
- <!-- JAL-3621 -->  IntervalStore library updated to v.1.1: optimisations and improvements suggested by Bob Hanson and improved compatibility with JalviewJS
- <!-- JAL-3615 -->  Retrieve GZipped stockholm formatted alignments from Pfam and Rfam
- <!-- JAL-2656 -->  Recognise GZipped content for URLs and File import (no longer based on .gz extension)
- <!-- JAL-3570 -->  Updated Spanish Translation for 2.11.1
- <!-- JAL-3692 -->  Migrate EMBL record retrieval to use latest ENA Browser (https://www.ebi.ac.uk/ena/browser/home) and EMBL flat file
- <!-- JAL-3667 -->  Improved warning messages, debug logging and fixed Retry action when Jalview encounters errors when saving or making backup files.
- <!-- JAL-3676 -->  Enhanced Jalview Java Console:
  - Jalview's logging level can be configured
  - Copy to Clipboard Buttion
- <!-- JAL-3541 -->  Improved support for Hi-DPI (4K) screens when running on Linux (Requires Java 11+)
- <!-- JAL-1842 JAL-3509 -->  RESNUM sequence features (the green ones) are not automatically displayed when associated structures are displayed or for sequences retrieved from the PDB.


### Launching Jalview
- <!-- JAL-3608 -->  Configure Jalview Desktop's look and feel through a system property
- <!-- JAL-3477 -->  Improved built-in documentation and command line help for configuring Jalview's memory


## Issues Resolved

- <!-- JAL-3691 -->  Conservation and Quality tracks are shown but not calculated and no protein or DNA score models are available for tree/PCA calculation when launched with Turkish language locale
- <!-- JAL-3493 -->  Escape does not clear highlights on the alignment (Since Jalview 2.10.3)
- <!--  JAL-3680 -->  Alt+Left or Right arrow in cursor mode doesn't slide selected sequences, just sequence under cursor
- <!-- JAL-3732 -->  Alt+Up/Down in cursor mode doesn't move sequence under the cursor
- <!-- JAL-3613 -->  Peptide-to-CDS tracking broken when multiple EMBL gene products shown for a single contig
- <!-- JAL-3696 -->  Errors encountered when processing variants from VCF files yield "Error processing VCF: Format specifier '%s'" on the console
- <!-- JAL-3697 -->  Count of features not shown can be wrong when there are both local and complementary features mapped to the position under the cursor
- <!-- JAL-3673 -->  Sequence ID for reference sequence is clipped when Right align Sequence IDs enabled
- <!-- JAL-2983 -->  Slider with negative range values not rendered correctly in VAqua4 (Since 2.10.4)
- <!-- JAL-3685 -->  Single quotes not displayed correctly in internationalised text for some messages and log output
- <!-- JAL-3490 -->  Find doesn't report matches that span hidden gapped columns
- <!-- JAL-3597 -->  Resolved memory leaks in Tree and PCA panels, Alignment viewport and annotation renderer.
- <!-- JAL-3561 -->  Jalview ignores file format parameter specifying output format when exporting an alignment via the command line
- <!-- JAL-3667 -->  Windows 10: For a minority of users, if backups are not enabled, Jalview sometimes fails to overwrite an existing file and raises a warning dialog. (in 2.11.0, and 2.11.1.0, the workaround is to try to save the file again, and if that fails, delete the original file and save in place.)
- <!-- JAL-3509 -->  Dragging a PDB file onto an alignment with sequence features displayed causes displayed features to be hidden.
- <!-- JAL-3750 -->  Cannot process alignments from HTTPS urls via command line
- <!-- JAL-3741 -->  References to http://www.jalview.org in program and documentation


### Launching Jalview
- <!-- JAL-3718 -->  Jalview application fails when launched the first time for a version that has different jars to the previous launched version.


### Developing Jalview
- <!-- JAL-3541 -->  Fixed issue with cleaning up old coverage data, causing cloverReport gradle task to fail with an OutOfMemory error.
- <!-- JAL-3280 -->  Migrated the Jalview Version Checker to monitor the release channel


### New Known defects
- <!-- JAL-3748 -->  CDS shown in result of submitting proteins in a CDS/Protein alignment to a web service is wrong when proteins share a common transcript sequence (e.g. genome of RNA viruses)
- <!-- JAL-3576 -->  Co-located features exported and re-imported are ordered differently when shown on alignment and in tooltips. (Also affects v2.11.1.0)
- <!-- JAL-3702 -->  Drag and drop of alignment file onto alignment window when in a HiDPI scaled mode in Linux only works for the top left quadrant of the alignment window
- <!-- JAL-3701 -->  Stale build data in jalview standalone jar builds (only affects 2.11.1.1 branch)
- <!-- JAL-3127 -->  Sequence ID colourscheme not re-applied when alignment view restored from project (since Jalview 2.11.0)
- <!-- JAL-3749 -->  Duplicate CDS sequences are generated when protein products for certain ENA records are repeatedly shown via Calculate->Show Cross Refs
