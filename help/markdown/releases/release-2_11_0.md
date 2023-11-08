---
channel: release
version: 2.11.0
date: 2019-07-04
---

## New Features

- <!-- JAL-1059, JAL-3196,JAL-3007,JAL-3236 -->  Jalview Native Application and Installers built with [install4j](https://www.ej-technologies.com/products/install4j/overview.html) (licensed to the Jalview open source project) rather than InstallAnywhere
- <!-- JAL-1929 -->  Jalview Launcher System to auto-configure memory settings, receive over the air updates and launch specific versions via ([Three Rings' GetDown](https://github.com/threerings/getdown))
- <!-- JAL-1839,JAL-3254,JAL-3260 -->  File type associations for formats supported by Jalview (including .jvp project files)
- <!-- JAL-3260 -->  Jalview launch files (.jvl) to pass command line arguments and switch between different getdown channels
- <!-- JAL-3141 -->  Backup files created when saving Jalview project or alignment files
- <!-- JAL-1793 -->  Annotate nucleotide alignments from VCF data files
- <!-- JAL-2753 -->  Version of HTSJDK shipped with Jalview updated to version 2.12.0
- <!-- JAL-2620 -->  Alternative genetic code tables for 'Translate as cDNA'
- <!-- JAL-3018 -->  Update of Ensembl Rest Client to API v10.0
- **Enhanced visualisation and analysis of Sequence Features**
  - <!-- JAL-3140 JAL-2446 -->  IntervalStoreJ (NCList implementation that allows updates) used for Sequence Feature collections
  - <!-- JAL-2744, JAL-2808,JAL-2069,JAL-2820 -->  Sequence features can be filtered and shaded according to any associated attributes (e.g. variant attributes from VCF file, or key-value pairs imported from column 9 of GFF file)
  - <!-- JAL-2879 -->  Feature Attributes and shading schemes stored and restored from Jalview Projects
  - <!-- JAL-3334 -->  Use full Sequence Ontology (via BioJava) to recognise variant features
  - <!-- JAL-2897,JAL-3330 -->  Show synonymous codon variants on peptide sequences (also coloured red by default)
  - <!-- JAL-2792 -->  Popup window to show full report for a selected sequence feature's details
  - <!-- JAL-3139,JAL-2816,JAL-1117 -->  More efficient sequence feature render algorithm (Z-sort/transparency and filter aware)
  - <!-- JAL-3049,JAL-3054 -->  Improved tooltips in Feature Settings dialog
- <!-- JAL-3205 -->  Symmetric score matrices for faster tree and PCA calculations
- **Principal Components Analysis Viewer**
  - <!-- JAL-1767,JAL-2647 -->  Principal Components Analysis results and Viewer state saved in Jalview Project
  - <!-- JAL-2962 -->  'Change parameters' option removed from viewer's drop-down menus
  - <!-- JAL-2975 -->  Can use shift + arrow keys to rotate PCA image incrementally
  - <!-- JAL-2965, JAL-1285 -->  PCA plot is depth cued
- <!-- JAL-3127 -->  New 'Colour by Sequence ID' option
- **Speed and Efficiency**
  - <!-- JAL-2185,JAL-3198 -->  More efficient creation of selections and multiple groups when working with large alignments
  - <!-- JAL-3200 -->  Speedier import of annotation rows when parsing Stockholm files
- **User Interface**
  - <!-- JAL-2933 -->  Finder panel remembers last position in each view
  - <!-- JAL-2527 JAL-3203 -->  Alignment Overview now WYSIWIS (What you see is what is shown)<br/>
Only visible regions of alignment are shown by default (can be changed in user preferences)
  - <!-- JAL-3169 -->  File Chooser stays open after responding Cancel to the Overwrite Dialog
  - <!-- JAL-2420,JAL-3166 -->  Better popup menu behaviour when all sequences are hidden
  - <!-- JAL-1244 -->  Status bar shows bounds when dragging a selection region, and gap count when inserting or deleting gaps
  - <!-- JAL-3132 -->  Status bar updates over sequence and annotation labels
  - <!-- JAL-3093 -->  Annotation tooltips and popup menus are shown when in wrapped mode
  - <!-- JAL-3073 -->  Can select columns by dragging left/right in a graph or histogram annotation
  - <!-- JAL-2814,JAL-437 -->  Help button on Uniprot and PDB search panels
  - <!-- JAL-2621 -->  Cursor changes over draggable box in Overview panel
  - <!-- JAL-3181 -->  Consistent ordering of links in sequence id popup menu
  - <!-- JAL-3080 -->  Red line indicating tree-cut position not shown if no subgroups are created
  - <!-- JAL-3042 -->  Removed ability to configure length of search history by right-clicking search box
- <!-- JAL-3232 -->  Jalview Groovy Scripting Console updated to Groovy v2.5
- **Java 11 Support (not yet on general release)**
  - <!--  -->  OSX GUI integrations for App menu's 'About' entry and trapping CMD-Q


### Deprecations
- <!-- JAL-3035 -->  DAS sequence retrieval and annotation capabilities removed from the Jalview Desktop
- <!-- JAL-3063,JAL-3116 -->  Castor library for XML marshalling and unmarshalling has been replaced by JAXB for Jalview projects and XML based data retrieval clients
- <!-- JAL-3311 -->  Disable VAMSAS menu in preparation for removal
- <!--  -->  Jalview Desktop no longer distributed via Java Web Start


### Documentation
- <!-- JAL-3003 -->  Added remarks about transparent rendering effects not supported in EPS figure export
- <!-- JAL-2903 -->  Typos in documentation for Preferences dialog


### Development and Release Processes
- <!-- JAL-3196,JAL-3179.JAL-2671 -->  Build system migrated from Ant to Gradle
- <!-- JAL-1424 -->  Enhanced checks for missing and duplicated keys in Message bundles
- <!-- JAL-3225 -->  Eclipse project configuration managed with gradle-eclipse
- <!-- JAL-3174,JAL-2886,JAL-2729,JAL-1889 -->  Atlassian Bamboo continuous integration for unattended Test Suite execution
- <!-- JAL-2864 -->  Memory test suite to detect leaks in common operations
- <!-- JAL-2360,JAL-2416 -->  More unit test coverage, and minor issues resolved
- <!-- JAL-3248 -->  Developer documentation migrated to markdown (with HTML rendering)
- <!-- JAL-3287 -->  HelpLinksChecker runs on Windows
- <!-- JAL-3289 -->  New URLs for publishing development versions of Jalview


## Issues Resolved

- <!-- JAL-3143 -->  Timeouts when retrieving data from Ensembl
- <!-- JAL-3244 -->  'View [Structure] Mappings' and structure superposition in Jmol fail on Windows
- <!-- JAL-3286 -->  Blank error dialog is displayed when discovering structures for sequences with lots of PDB structures
- <!-- JAL-3239 -->  Text misaligned in EPS or SVG image export with monospaced font
- <!-- JAL-3171 -->  Warning of 'Duplicate entry' when saving Jalview project involving multiple views
- <!-- JAL-3164 -->  Overview for complementary view in a linked CDS/Protein alignment is not updated when Hide Columns by Annotation dialog hides columns
- <!-- JAL-3158 -->  Selection highlighting in the complement of a CDS/Protein alignment stops working after making a selection in one view, then making another selection in the other view
- <!-- JAL-3161 -->  Annotations tooltip changes beyond visible columns
- <!-- JAL-3154 -->  Table Columns could be re-ordered in Feature Settings and Jalview Preferences panels
- <!-- JAL-2865 -->  Jalview hangs when closing windows, or redrawing the overview with large alignments
- <!-- JAL-2750 -->  Tree and PCA calculation fails for selected region if columns were selected by dragging right-to-left and the mouse moved to the left of the first column
- <!-- JAL-3218 -->  Couldn't hide selected columns adjacent to a hidden column marker via scale popup menu
- <!-- JAL-2846 -->  Error message for trying to load in invalid URLs doesn't tell users the invalid URL
- <!-- JAL-2816 -->  Tooltips displayed for features filtered by score from view
- <!-- JAL-3330 -->  Sequence Variants retrieved from Ensembl during show cross references or Fetch Database References are shown in red in original view
- <!-- JAL-2898,JAL-2207 -->  stop_gained variants not shown correctly on peptide sequence (computed variant shown as p.Res.null)
- <!-- JAL-2060 -->  'Graduated colour' option not offered for manually created features (where feature score is Float.NaN)
- <!-- JAL-3097,JAL-3099 -->  Blank extra columns drawn or printed when columns are hidden
- <!-- JAL-3082 -->  Regular expression error for '(' in Select Columns by Annotation description
- <!-- JAL-3072 -->  Scroll doesn't stop on mouse up after dragging out of Scale or Annotation Panel
- <!-- JAL-3075 -->  Column selection incorrect after scrolling out of scale panel
- <!-- JAL-3074 -->  Left/right drag in annotation can scroll alignment down
- <!-- JAL-3108 -->  Error if mouse moved before clicking Reveal in scale panel
- <!-- JAL-3002 -->  Column display is out by one after Page Down, Page Up in wrapped mode
- <!-- JAL-2839,JAL-781 -->  Finder doesn't skip hidden regions
- <!-- JAL-2932 -->  Finder searches in minimised alignments
- <!-- JAL-2250 -->  'Apply Colour to All Groups' not always selected on opening an alignment
- <!-- JAL-3180 -->  'Colour by Annotation' not marked selected in Colour menu
- <!-- JAL-3201 -->  Per-group Clustal colour scheme changes when different groups in the alignment are selected
- <!-- JAL-2717 -->  Internationalised colour scheme names not shown correctly in menu
- <!-- JAL-3206 -->  Colour by Annotation can go black at min/max threshold limit
- <!-- JAL-3125 -->  Value input for graduated feature colour threshold gets 'unrounded'
- <!-- JAL-2982 -->  PCA image export doesn't respect background colour
- <!-- JAL-2963 -->  PCA points don't dim when rotated about y axis
- <!-- JAL-2959 -->  PCA Print dialog continues after Cancel
- <!-- JAL-3078 -->  Cancel in Tree Font dialog resets alignment, not Tree font
- <!-- JAL-2964 -->  Associate Tree with All Views not restored from project file
- <!-- JAL-2915 -->  Scrolling of split frame is sluggish if Overview shown in complementary view
- <!-- JAL-3313 -->  Codon consensus incorrectly scaled when shown without normalisation
- <!-- JAL-3021 -->  Sequence Details report should open positioned at top of report
- <!-- JAL-914 -->  Help page can be opened twice
- <!-- JAL-3333 -->  Fuzzy text in web service status menu on OSX Mojave


### Editing
- <!-- JAL-2822 -->  Start and End should be updated when sequence data at beginning or end of alignment added/removed via 'Edit' sequence
- <!-- JAL-2541,JAL-2684 (tests) -->  Delete/Cut selection doesn't relocate sequence features correctly when start of sequence is removed (Known defect since 2.10)
- <!-- JAL-2830 -->  Inserting gap sequence via the Edit Sequence dialog corrupts dataset sequence
- <!-- JAL-868 -->  Structure colours not updated when associated tree repartitions the alignment view (Regression in 2.10.5)


### Datamodel
- <!-- JAL-2986 -->  Sequence.findIndex returns wrong value when sequence's End is greater than its length


### Bugs fixed for Java 11 Support (not yet on general release)
- <!-- JAL-3288 -->  Menus work properly in split-screen


### New Known Defects
- <!-- JAL-3340 -->  Select columns containing feature by double clicking ignores bounds of an existing selected region
- <!-- JAL-3313 -->  Codon consensus logo incorrectly scaled in gapped regions of protein alignment.
- <!-- JAL-2647 -->  Input Data menu entry is greyed out when PCA View is restored from a Jalview 2.11 project
- <!-- JAL-3213 -->  Alignment panel height can be too small after 'New View'
- <!-- JAL-3240 -->  Display is incorrect after removing gapped columns within hidden columns
- <!-- JAL-3314 -->  Rightmost selection is lost when mouse re-enters window after dragging left to select columns to left of visible region
- <!-- JAL-2876 -->  Features coloured according to their description string and thresholded by score in earlier versions of Jalview are not shown as thresholded features in 2.11. To workaround please create a Score filter instead.
- <!-- JAL-3184 -->  Cancel on Feature Settings dialog doesn't reset group visibility
- <!-- JAL-3338 -->  F2 doesn't enable/disable keyboard mode in linked CDS/Protein view
- <!-- JAL-797 -->  Closing tree windows with CMD/CTRL-W for alignments with multiple views can close views unexpectedly


### Java 11 Specific defects
- <!-- JAL-3235 -->  Jalview Properties file is not sorted alphabetically when saved
