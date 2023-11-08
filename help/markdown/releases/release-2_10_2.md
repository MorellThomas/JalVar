---
channel: release
version: 2.10.2
date: 2017-08-17
---

## New Features



### Calculations
  - <!-- JAL-1933 -->  Occupancy annotation row shows number of ungapped positions in each column of the alignment.
  - <!-- JAL-1632 -->  Tree/PCA calculation menu items merged to a calculation dialog box
  - <!-- JAL-2379 -->  Revised implementation of PCA for speed and memory efficiency (~30x faster)
  - <!-- JAL-2403 -->  Revised implementation of sequence similarity scores as used by Tree, PCA, Shading Consensus and other calculations
  - <!-- JAL-2416 -->  Score matrices are stored as resource files within the Jalview codebase
  - <!-- JAL-2500 -->  Trees computed on Sequence Feature Similarity may have different topology due to increased precision


### Rendering
  - <!-- JAL-2360,JAL-2371, -->  More robust colours and shader model for alignments and groups
  - <!--  JAL-384 -->  Custom shading schemes created via groovy scripts


### Overview
  - <!--  JAL-2526 -->  Efficiency improvements for interacting with alignment and overview windows
  - <!-- JAL-2514 -->  Scrolling of wrapped alignment views via overview
  - <!-- JAL-2388 -->  Hidden columns and sequences can be omitted in Overview
  - <!-- JAL-2611 -->  Click-drag in visible area allows fine adjustment of visible position


### Data import/export
  - <!-- JAL-2535 -->  Posterior probability annotation from Stockholm files imported as sequence associated annotation
  - <!-- JAL-2507 -->  More robust per-sequence positional annotation input/output via stockholm flatfile
  - <!-- JAL-2533 -->  Sequence names don't include file extension when importing structure files without embedded names or PDB accessions
  - <!-- JAL-2416 -->  Drag and drop load of AAIndex and NCBI format sequence substitution matrices


### User Interface
  - <!-- JAL-2447 -->   Experimental Features Checkbox in Desktop's Tools menu to hide or show untested features in the application.
  - <!--  JAL-2491 -->  Linked scrolling of CDS/Protein views via Overview or sequence motif search operations
  - <!-- JAL-2547 -->  Amend sequence features dialog box can be opened by double clicking gaps within sequence feature extent
  - <!-- JAL-1476 -->  Status bar message shown when not enough aligned positions were available to create a 3D structure superposition.


### 3D Structure
  - <!-- JAL-2430 -->  Hidden regions in alignment views are not coloured in linked structure views
  - <!-- JAL-1596 -->  Faster Chimera/Jalview communication by file-based command exchange
  - <!-- JAL-2375 -->  Structure chooser automatically shows Cached Structures rather than querying the PDBe if structures are already available for sequences
  - <!-- JAL-2520 -->  Structures imported via URL are cached in the Jalview project rather than downloaded again when the project is reopened.
  - <!-- JAL-2295, JAL-2296 -->  New entries in the Chimera menu to transfer Chimera's structure attributes as Jalview features, and vice-versa (**Experimental Feature**)


### Web Services
  - <!-- JAL-2549 -->  Updated JABAWS client to v2.2
  - <!-- JAL-2335 -->  Filter non-standard amino acids and nucleotides when submitting to AACon and other MSA Analysis services
  - <!-- JAL-2316, -->  URLs for viewing database cross-references provided by identifiers.org and the EMBL-EBI's MIRIAM DB


### Scripting
  - <!-- JAL-2344 -->  FileFormatI interface for describing and identifying file formats (instead of String constants)
  - <!-- JAL-2228 -->  FeatureCounter script refactored for efficiency when counting all displayed features (not backwards compatible with 2.10.1)


### Example files
  - <!-- JAL-2631 -->  Graduated feature colour style example included in the example feature file


### Documentation
  - <!-- JAL-2339 -->  Release notes reformatted for readability with the built-in Java help viewer
  - <!-- JAL-1644 -->  Find documentation updated with 'search sequence description' option


### Test Suite
  - <!-- JAL-2485, -->  External service integration tests for Uniprot REST Free Text Search Client
  - <!--  JAL-2474 -->  Added PrivilegedAccessor to test suite
  - <!-- JAL-2326 -->  Prevent or clear modal dialogs raised during tests


## Issues Resolved



### Calculations
  - <!-- JAL-2398, -->  Fixed incorrect value in BLOSUM 62 score matrix - C->R should be '-3'<br/>
Old matrix restored with this one-line groovy script:<br/>
jalview.analysis.scoremodels.ScoreModels.instance.BLOSUM62.@matrix[4][1]=3
  - <!-- JAL-2397 --> []() Fixed Jalview's treatment of gaps in PCA and substitution matrix based Tree calculations.<br/>
 <br/>
In earlier versions of Jalview, gaps matching gaps were penalised, and gaps matching non-gaps penalised even more. In the PCA calculation, gaps were actually treated as non-gaps - so different costs were applied, which meant Jalview's PCAs were different to those produced by SeqSpace.<br/>
Jalview now treats gaps in the same way as SeqSpace (ie it scores them as 0). <br/>
 <br/>
Enter the following in the Groovy console to restore pre-2.10.2 behaviour:<br/>
 jalview.analysis.scoremodels.ScoreMatrix.scoreGapAsAny=true // for 2.10.1 mode <br/>
 jalview.analysis.scoremodels.ScoreMatrix.scoreGapAsAny=false // to restore 2.10.2 mode <br/>
 <br/>
 *Note: these settings will affect all subsequent tree and PCA calculations (not recommended)*
  - <!-- JAL-2424 -->  Fixed off-by-one bug that affected scaling of branch lengths for trees computed using Sequence Feature Similarity.
  - <!-- JAL-2377 -->  PCA calculation could hang when generating output report when working with highly redundant alignments
  - <!-- JAL-2544 -->   Sort by features includes features to right of selected region when gaps present on right-hand boundary


### User Interface
  - <!-- JAL-2346 -->  Reopening Colour by annotation dialog doesn't reselect a specific sequence's associated annotation after it was used for colouring a view
  - <!-- JAL-2419 -->  Current selection lost if popup menu opened on a region of alignment without groups
  - <!-- JAL-2374 -->  Popup menu not always shown for regions of an alignment with overlapping groups
  - <!-- JAL-2310 -->  Finder double counts if both a sequence's name and description match
  - <!-- JAL-2370 -->  Hiding column selection containing two hidden regions results in incorrect hidden regions
  - <!-- JAL-2386 -->  'Apply to all groups' setting when changing colour does not apply Conservation slider value to all groups
  - <!-- JAL-2373 -->  Percentage identity and conservation menu items do not show a tick or allow shading to be disabled
  - <!-- JAL-2385 -->  Conservation shading or PID threshold lost when base colourscheme changed if slider not visible
  - <!-- JAL-2547 -->  Sequence features shown in tooltip for gaps before start of features
  - <!-- JAL-2623 -->  Graduated feature colour threshold not restored to UI when feature colour is edited
  - <!-- JAL-147 -->  Vertical scrollbar jumps one page-width at a time when scrolling vertically in wrapped mode.
  - <!-- JAL-2630 -->  Structure and alignment overview update as graduate feature colour settings are modified via the dialog box
  - <!-- JAL-2034 -->  Overview window doesn't always update when a group defined on the alignment is resized
  - <!-- JAL-2605 -->  Mouseovers on left/right scale region in wrapped view result in positional status updates
  - <!-- JAL-2563 -->  Status bar doesn't show position for ambiguous amino acid and nucleotide symbols
  - <!-- JAL-2602 -->  Copy consensus sequence failed if alignment included gapped columns
  - <!-- JAL-2473 -->  Minimum size set for Jalview windows so widgets don't permanently disappear
  - <!-- JAL-2503 -->  Cannot select or filter quantitative annotation that are shown only as column labels (e.g. T-Coffee column reliability scores)
  - <!-- JAL-2594 -->  Exception thrown if trying to create a sequence feature on gaps only
  - <!-- JAL-2504 -->  Features created with 'New feature' button from a Find inherit previously defined feature type rather than the Find query string
  - <!-- JAL-2423  -->  incorrect title in output window when exporting tree calculated in Jalview
  - <!-- JAL-2437 -->  Hiding sequences at bottom of alignment and then revealing them reorders sequences on the alignment
  - <!-- JAL-964 -->  Group panel in sequence feature settings doesn't update to reflect available set of groups after interactively adding or modifying features
  - <!-- JAL-2225 -->  Sequence Database chooser unusable on Linux
  - <!-- JAL-2291 -->  Hide insertions in PopUp->Selection menu only excluded gaps in current sequence and ignored selection.


### Rendering
  - <!-- JAL-2421 -->  Overview window visible region moves erratically when hidden rows or columns are present
  - <!-- JAL-2362 -->  Per-residue colourschemes applied via the Structure Viewer's colour menu don't correspond to sequence colouring
  - <!-- JAL-2405 -->  Protein specific colours only offered in colour and group colour menu for protein alignments
  - <!-- JAL-2385 -->  Colour threshold slider doesn't update to reflect currently selected view or group's shading thresholds
  - <!-- JAL-2624 -->  Feature colour thresholds not respected when rendered on overview and structures when opacity at 100%
  - <!-- JAL-2589 -->  User defined gap colour not shown in overview when features overlaid on alignment
  - <!-- JAL-2567 -->  Feature settings for different views not recovered correctly from Jalview project file
  - <!-- JAL-2256 -->  Feature colours in overview when first opened (automatically via preferences) are different to the main alignment panel


### Data import/export
  - <!-- JAL-2576 -->  Very large alignments take a long time to load
  - <!-- JAL-2507 -->  Per-sequence RNA secondary structures added after a sequence was imported are not written to Stockholm File
  - <!-- JAL-2509 -->  WUSS notation for simple pseudoknots lost when importing RNA secondary structure via Stockholm
  - <!-- JAL-2509 -->  Secondary structure arrows for [] and {} not shown in correct direction for simple pseudoknots
  - <!-- JAL-2365,JAL-2642 -->  Cannot configure feature colours with lightGray or darkGray via features file (but can specify lightgray)
  - <!--  JAL-2383 -->  Above PID colour threshold not recovered when alignment view imported from project
  - <!-- JAL-2520,JAL-2465 -->  No mappings generated between structure and sequences extracted from structure files imported via URL and viewed in Jmol
  - <!-- JAL-2520 -->  Structures loaded via URL are saved in Jalview Projects rather than fetched via URL again when the project is loaded and the structure viewed


### Web Services
  - <!-- JAL-2519 -->  EnsemblGenomes example failing after release of Ensembl v.88
  - <!--  JAL-2366 -->  Proxy server address and port always appear enabled in Preferences->Connections
  - <!--  JAL-2461 -->  DAS registry not found exceptions removed from console output
  - <!-- JAL-2582 -->  Cannot retrieve protein products from Ensembl by Peptide ID
  - <!-- JAL-2482, JAL-2487 -->  Incorrect PDB-Uniprot mappings created from SIFTs, and spurious 'Couldn't open structure in Chimera' errors raised after April 2017 update (problem due to 'null' string rather than empty string used for residues with no corresponding PDB mapping).


### Application UI
  - <!-- JAL-2361 -->  User Defined Colours not added to Colour menu
  - <!-- JAL-2401 -->  Easier creation of colours for all 'Lower case' residues (button in colourscheme editor debugged and new documentation and tooltips added)
  - <!-- JAL-2399-->  Text colour threshold's 'Cancel' button doesn't restore group-specific text colour thresholds
  - <!-- JAL-2243 -->  Feature settings panel does not update as new features are added to alignment
  - <!-- JAL-2532 -->  Cancel in feature settings reverts changes to feature colours via the Amend features dialog
  - <!-- JAL-2506 -->  Null pointer exception when attempting to edit graduated feature colour via amend features dialog box
  - <!-- JAL-2436 -->  Structure viewer's View -> Colour By view selection menu changes colours of alignment views
  - <!-- JAL-2426 -->  Spurious exceptions in console raised from alignment calculation workers after alignment has been closed
  - <!-- JAL-1608 -->  Typo in selection popup menu - Create groups now 'Create Group'
  - <!-- JAL-1608 -->  CMD/CTRL and G or Shift G for Create/Undefine group doesn't always work
  - <!-- JAL-2464 -->  Tree Viewer's Print Dialog doesn't get shown again after pressing 'Cancel'
  - <!-- JAL-1256 -->  Trackpad horizontal scroll gesture adjusts start position in wrap mode
  - <!-- JAL-2563 -->  Status bar doesn't show positions for ambiguous amino acids
  - <!-- JAL-2431 -->  cDNA Consensus annotation not shown in CDS/Protein view after CDS sequences added for aligned proteins
  - <!-- JAL-2592 -->  User defined colourschemes called 'User Defined' don't appear in Colours menu


### Applet
  - <!-- JAL-2468 -->  Switching between Nucleotide and Protein score models doesn't always result in an updated PCA plot
  - <!-- JAL-2442 -->  Features not rendered as transparent on overview or linked structure view
  - <!--  JAL-2372 -->  Colour group by conservation doesn't work (since 2.8)
  - <!-- JAL-2517 -->  Hitting Cancel after applying user-defined colourscheme doesn't restore original colourscheme


### Test Suite
  - <!-- JAL-2314 -->  Unit test failure: jalview.ws.jabaws.RNAStructExportImport setup fails
  - <!-- JAL-2307 -->  Unit test failure: jalview.ws.sifts.SiftsClientTest due to compatibility problems with deep array comparison equality asserts in successive versions of TestNG
  - <!-- JAL-2479 -->  Relocated StructureChooserTest and ParameterUtilsTest Unit tests to Network suite


### New Known Issues
  - <!--  JAL-2566 -->  Protein/CDS view scrolling not always in phase after a sequence motif find operation
  - <!-- JAL-2550 -->  Importing annotation file with rows containing just upper and lower case letters are interpreted as WUSS RNA secondary structure symbols
  - <!-- JAL-2590 -->  Cannot load and display Newick trees reliably from eggnog Ortholog database
  - <!-- JAL-2468 -->  Status bar shows 'Marked x columns containing features of type Highlight' when 'B' is pressed to mark columns containing highlighted regions.
  - <!-- JAL-2321 -->  Dropping a PDB file onto a sequence doesn't always add secondary structure annotation.
