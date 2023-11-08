---
channel: release
version: 2.9
date: 2015-09-10
---

## New Features



### General
- Linked visualisation and analysis of DNA and Protein alignments:
  - Translated cDNA alignments shown as split protein and DNA alignment views
  - Codon consensus annotation for linked protein and cDNA alignment views
  - Link cDNA or Protein product sequences by loading them onto Protein or cDNA alignments
  - Reconstruct linked cDNA alignment from aligned protein sequences
- Jmol integration updated to Jmol v14.2.14
- Import and export of Jalview alignment views as [BioJSON](features/bioJsonFormat.html)
- New alignment annotation file statements for reference sequences and marking hidden columns
- Reference sequence based alignment shading to highlight variation
- Select or hide columns according to alignment annotation
- Find option for locating sequences by description
- Conserved physicochemical properties shown in amino acid conservation row
- Alignments can be sorted by number of RNA helices


### Application
- New cDNA/Protein analysis capabilities
  - Get Cross-References should open a Split Frame view with cDNA/Protein
  - Detect when nucleotide sequences and protein sequences are placed in the same alignment
  - Split cDNA/Protein views are saved in Jalview projects
- Use REST API to talk to Chimera
- Selected regions in Chimera are highlighted in linked Jalview windows
- VARNA RNA viewer updated to v3.93
- VARNA views are saved in Jalview Projects
- Pseudoknots displayed as Jalview RNA annotation can be shown in VARNA
- Make groups for selection uses marked columns as well as the active selected region
- Calculate UPGMA and NJ trees using sequence feature similarity
- New Export options
  - New Export Settings dialog to control hidden region export in flat file generation
  - Export alignment views for display with the [BioJS MSAViewer](http://msa.biojs.net/)
  - Export scrollable SVG in HTML page
  - Optional embedding of BioJSON data when exporting alignment figures to HTML
- 3D structure retrieval and display
  - Free text and structured queries with the PDBe Search API
  - PDBe Search API based discovery and selection of PDB structures for a sequence set
- JPred4 employed for protein secondary structure predictions
- Hide Insertions menu option to hide unaligned columns for one or a group of sequences
- Automatically hide insertions in alignments imported from the JPred4 web server
- (Nearly) Native 'Quaqua' dialogs for browsing file system on OSX<br/>
LGPL libraries courtesy of [http://www.randelshofer.ch/quaqua/](http://www.randelshofer.ch/quaqua/)
- changed 'View nucleotide structure' submenu to 'View VARNA 2D Structure'
- change "View protein structure" menu option to "3D Structure ..."


### Applet
- New layout for applet example pages
- New parameters to enable SplitFrame view (file2,enableSplitFrame, scaleProteinAsCdna)
- New example demonstrating linked viewing of cDNA and Protein alignments


### Development and deployment
- Java 1.7 minimum requirement for Jalview 2.9
- Include installation type and git revision in build properties and console log output
- Jalview Github organisation, and new github site for storing BioJsMSA Templates
- Jalview's unit tests now managed with TestNG


## Issues Resolved



### Application
- Escape should close any open find dialogs
- Typo in select-by-features status report
- Consensus RNA secondary secondary structure predictions are not highlighted in amber
- Missing gap character in v2.7 example file means alignment appears unaligned when pad-gaps is not enabled
- First switch to RNA Helices colouring doesn't colour associated structure views
- ID width preference option is greyed out when auto width checkbox not enabled
- Stopped a warning dialog from being shown when creating user defined colours
- 'View Mapping' in structure viewer shows sequence mappings for just that viewer's sequences
- Workaround for superposing PDB files containing multiple models in Chimera
- Report sequence position in status bar when hovering over Jmol structure
- Cannot output gaps as '.' symbols with Selection -> output to text box
- Flat file exports of alignments with hidden columns have incorrect sequence start/end
- 'Aligning' a second chain to a Chimera structure from Jalview fails
- Colour schemes applied to structure viewers don't work for nucleotide
- Loading/cut'n'pasting an empty or invalid file leads to a grey/invisible alignment window
- Exported Jpred annotation from a sequence region imports to different position
- Space at beginning of sequence feature tooltips shown on some platforms
- Chimera viewer 'View | Show Chain' menu is not populated
- 'New View' fails with a Null Pointer Exception in console if Chimera has been opened
- Mouseover to Chimera not working
- Miscellaneous ENA XML feature qualifiers not retrieved
- NPE in annotation renderer after 'Extract Scores'
- If two structures in one Chimera window, mouseover of either sequence shows on first structure
- 'Show annotations' options should not make non-positional annotations visible
- Subsequence secondary structure annotation not shown in right place after 'view flanking regions'
- File Save As type unset when current file format is unknown
- Save as '.jar' option removed for saving Jalview projects
- Colour by Sequence colouring in Chimera more responsive
- Cannot 'add reference annotation' for a sequence in several views on same alignment
- Cannot show linked products for EMBL / ENA records
- Jalview's tooltip wraps long texts containing no spaces


### Applet
- Jmol to JalviewLite mouseover/link not working
- JalviewLite can't import sequences with ID descriptions containing angle brackets


### General
- Cannot export and reimport RNA secondary structure via jalview annotation file
- Random helix colour palette for colour by annotation with RNA secondary structure
- Mouseover to cDNA from STOP residue in protein translation doesn't work.
- hints when using the select by annotation dialog box
- Jmol alignment incorrect if PDB file has alternate CA positions
- FontChooser message dialog appears to hang after choosing 1pt font
- Peptide secondary structure incorrectly imported from annotation file when annotation display text includes 'e' or 'h'
- Cannot set colour of new feature type whilst creating new feature
- cDNA translation alignment should not be sequence order dependent
- 'Show unconserved' doesn't work for lower case sequences
- Nucleotide ambiguity codes involving R not recognised


### Deployment and Documentation
- Applet example pages appear different to the rest of www.jalview.org


### Application Known issues
- Incomplete sequence extracted from PDB entry 3a6s
- Misleading message appears after trying to delete solid column.
- Jalview icon not shown in dock after InstallAnywhere version launches
- Fetching EMBL reference for an RNA sequence results fails with a sequence mismatch
- Corrupted or unreadable alignment display when scrolling alignment to right
- ArrayIndexOutOfBoundsException thrown when remove empty columns called on alignment with ragged gapped ends
- auto calculated alignment annotation rows do not get placed above or below non-autocalculated rows
- Jalview dekstop becomes sluggish at full screen in ultra-high resolution
- Cannot disable consensus calculation independently of quality and conservation
- Mouseover highlighting between cDNA and protein can become sluggish with more than one splitframe shown


### Applet Known Issues
- Core PDB parsing code requires Jmol
- Sequence canvas panel goes white when alignment window is being resized
