---
channel: release
version: 2.10.0
date: 2016-10-06
---

## New Features



### General
- <!-- JAL-2124 -->  Updated Spanish translations.
- <!-- JAL-2164,JAL-1919,JAL-2148 -->  Jmol now primary parser for importing structure data to Jalview. Enables mmCIF and better PDB parsing.
- <!-- JAL-192 --->  Alignment ruler shows positions relative to reference sequence
- <!-- JAL-2202 -->  Position/residue shown in status bar when mousing over sequence associated annotation
- <!-- JAL-2171 -->  Default RNA SS symbol to 'matching bracket' for manual entry
- <!-- JAL-2214 -->  RNA Structure consensus indicates wc-only '()', canonical '[]' and invalid '{}' base pair populations for each column
- <!-- JAL-2092 -->  Feature settings popup menu options for showing or hiding columns containing a feature
- <!-- JAL-1557 -->  Edit selected group by double clicking on group and sequence associated annotation labels
- <!-- JAL-2236 -->  Sequence name added to annotation label in select/hide columns by annotation and colour by annotation dialogs


### Application
- <!-- JAL-2050-->  Automatically hide introns when opening a gene/transcript view
- <!-- JAL-1563 -->  Uniprot Sequence fetcher Free Text Search dialog
- <!--  JAL-1957, JAL-1479 JAL-1491 -->  UniProt - PDB protein structure mappings with the EMBL-EBI PDBe SIFTS database
- <!-- JAL-2079 -->  Updated download sites used for Rfam and Pfam sources to xfam.org
- <!-- JAL-2084 -->  Disabled Rfam(Full) in the sequence fetcher
- <!-- JAL-2123 -->  Show residue labels in Chimera when mousing over sequences in Jalview
- <!-- JAL-2027-->  Support for reverse-complement coding regions in ENA and EMBL
- <!-- JAL-1855, JAL-2113, JAL-2114-->  Upgrade to EMBL XML 1.2 for record retrieval via ENA rest API
- <!-- JAL-2027 -->  Support for ENA CDS records with reverse complement operator
- <!--  JAL-1812 -->  Update to groovy-2.4.6-indy - for faster groovy script execution
- <!--  JAL-1812 -->  New 'execute Groovy script' option in an alignment window's Calculate menu
- <!--  JAL-1812 -->  Allow groovy scripts that call Jalview.getAlignFrames() to run in headless mode
- <!--  JAL-2068 -->  Support for creating new alignment calculation workers from groovy scripts
- <!-- JAL-1369 --->  Store/restore reference sequence in Jalview projects
- <!-- JAL-1803 -->  Chain codes for a sequence's PDB associations are now saved/restored from project
- <!-- JAL-1993 -->  Database selection dialog always shown before sequence fetcher is opened
- <!-- JAL-2183 -->  Double click on an entry in Jalview's database chooser opens a sequence fetcher
- <!-- JAL-1563 -->  Free-text search client for UniProt using the UniProt REST API
- <!-- JAL-2168 -->  -nonews command line parameter to prevent the news reader opening
- <!-- JAL-2028 -->  Displayed columns for PDBe and Uniprot querying stored in preferences
- <!-- JAL-2091 -->  Pagination for displaying PDBe and Uniprot search results
- <!-- JAL-1977-->  Tooltips shown on database chooser
- <!--  JAL-391 -->  Reverse complement function in calculate menu for nucleotide sequences
- <!-- JAL-2005, JAL-599 -->  Alignment sort by feature scores and feature counts preserves alignment ordering (and debugged for complex feature sets).
- <!-- JAL-2152-->  Chimera 1.11.1 minimum requirement for viewing structures with Jalview 2.10
- <!-- JAL-1705, JAL-1975, JAL-2050,JAL-2041,JAL-2105 -->  Retrieve genome, transcript CCDS and gene ids via the Ensembl and Ensembl Genomes REST API
- <!-- JAL-2049 -->  Protein sequence variant annotation computed for 'sequence_variant' annotation on CDS regions (Ensembl)
- <!-- JAL-2232 -->  ENA CDS 'show cross references' for Uniprot sequences
- <!-- JAL-2213,JAL-1856 -->  Improved warning messages when DB Ref Fetcher fails to match, or otherwise updates sequence data from external database records.
- <!-- JAL-2154 -->  Revised Jalview Project format for efficient recovery of sequence coding and alignment annotation relationships.


## Issues Resolved



### General
  - <!-- JAL-2077 -->  reinstate CTRL-click for opening pop-up menu on OSX
  - <!-- JAL-2018-->  Export features in Jalview format (again) includes graduated colourschemes
  - <!-- JAL-2172,JAL-1722, JAL-2001-->  More responsive when working with big alignments and lots of hidden columns
  - <!-- JAL-2053-->  Hidden column markers not always rendered at right of alignment window
  - <!-- JAL-2067 -->  Tidied up links in help file table of contents
  - <!-- JAL-2072  -->  Feature based tree calculation not shown for DNA alignments
  - <!-- JAL-2075  -->  Hidden columns ignored during feature based tree calculation
  - <!-- JAL-2065  -->  Alignment view stops updating when show unconserved enabled for group on alignment
  - <!--  JAL-2086  -->  Cannot insert gaps into sequence when set as reference
  - <!-- JAL-2146 -->  Alignment column in status incorrectly shown as "Sequence position" when mousing over annotation
  - <!--  JAL-2099 -->  Incorrect column numbers in ruler when hidden columns present
  - <!--  JAL-1577 -->  Colour by RNA Helices not enabled when user created annotation added to alignment
  - <!-- JAL-1841 -->  RNA Structure consensus only computed for '()' base pair annotation
  - <!-- JAL-2215, JAL-1841 -->  Enabling 'Ignore Gaps' results in zero scores for all base pairs in RNA Structure Consensus
  - <!-- JAL-2174-->  Extend selection with columns containing feature not working
  - <!-- JAL-2275 -->  Pfam format writer puts extra space at beginning of sequence
  - <!-- JAL-1827 -->  Incomplete sequence extracted from pdb entry 3a6s
  - <!-- JAL-2238 -->  Cannot create groups on an alignment from from a tree when t-coffee scores are shown
  - <!-- JAL-1836,1967 -->  Cannot import and view PDB structures with chains containing negative resnums (4q4h)
  - <!--  JAL-1998 -->  ArithmeticExceptions raised when parsing some structures
  - <!--  JAL-1991, JAl-1952 -->  'Empty' alignment blocks added to Clustal, PIR and PileUp output
  - <!--  JAL-2008 -->  Reordering sequence features that are not visible causes alignment window to repaint
  - <!--  JAL-2006 -->  Threshold sliders don't work in graduated colour and colour by annotation row for e-value scores associated with features and annotation rows
  - <!-- JAL-1797 -->  amino acid physicochemical conservation calculation should be case independent
  - <!-- JAL-2173 -->  Remove annotation also updates hidden columns
  - <!-- JAL-2234 -->  FER1_ARATH and FER2_ARATH mislabelled in example file (uniref50.fa, feredoxin.fa, unaligned.fa, exampleFile_2_7.jar, exampleFile.jar, exampleFile_2_3.jar)
  - <!-- JAL-2065 -->  Null pointer exceptions and redraw problems when reference sequence defined and 'show non-conserved' enabled
  - <!-- JAL-1306 -->  Quality and Conservation are now shown on load even when Consensus calculation is disabled
  - <!-- JAL-1932 -->  Remove right on penultimate column of alignment does nothing


### Application
  - <!-- JAL-1552-->  URLs and links can't be imported by drag'n'drop on OSX when launched via webstart (note - not yet fixed for El Capitan)
  - <!-- JAL-1911-->  Corrupt preferences for SVG, EPS & HTML output when running on non-gb/us i18n platforms
  - <!-- JAL-1944 -->  Error thrown when exporting a view with hidden sequences as flat-file alignment
  - <!-- JAL-2030-->  InstallAnywhere distribution fails when launching Chimera
  - <!-- JAL-2080-->  Jalview very slow to launch via webstart (also hotfix for 2.9.0b2)
  - <!--  JAL-2085  -->  Cannot save project when view has a reference sequence defined
  - <!--  JAL-1011  -->  Columns are suddenly selected in other alignments and views when revealing hidden columns
  - <!--  JAL-1989  -->  Hide columns not mirrored in complement view in a cDNA/Protein splitframe
  - <!--  JAL-1369 -->  Cannot save/restore representative sequence from project when only one sequence is represented
  - <!-- JAL-2002 -->  Disabled 'Best Uniprot Coverage' option in Structure Chooser
  - <!-- JAL-2215 -->  Modifying 'Ignore Gaps' on consensus or structure consensus didn't refresh annotation panel
  - <!-- JAL-1962 -->  View mapping in structure view shows mappings between sequence and all chains in a PDB file
  - <!-- JAL-2102, JAL-2101, JAL-2102, -->  PDB and Uniprot FTS dialogs format columns correctly, don't display array data, sort columns according to type
  - <!-- JAL-1975 -->  Export complete shown after destination file chooser is cancelled during an image export
  - <!-- JAL-2025 -->  Error when querying PDB Service with sequence name containing special characters
  - <!-- JAL-2024 -->  Manual PDB structure querying should be case insensitive
  - <!-- JAL-2104 -->  Large tooltips with broken HTML formatting don't wrap
  - <!-- JAL-1128 -->  Figures exported from wrapped view are truncated so L looks like I in consensus annotation
  - <!-- JAL-2003 -->  Export features should only export the currently displayed features for the current selection or view
  - <!-- JAL-2036 -->  Enable 'Get Cross-References' in menu after fetching cross-references, and restoring from project
  - <!-- JAL-2032 -->  Mouseover of a copy of a sequence is not followed in the structure viewer
  - <!-- JAL-2163 -->  Titles for individual alignments in splitframe not restored from project
  - <!-- JAL-2145 -->  missing autocalculated annotation at trailing end of protein alignment in transcript/product splitview when pad-gaps not enabled by default
  - <!-- JAL-1797 -->  amino acid physicochemical conservation is case dependent
  - <!-- JAL-1448 -->  RSS reader doesn't stay hidden after last article has been read (reopened issue due to internationalisation problems)
  - <!-- JAL-1960 -->  Only offer PDB structures in structure viewer based on sequence name, PDB and UniProt cross-references
  - <!-- JAL-1976 -->  No progress bar shown during export of alignment as HTML
  - <!-- JAL-2213 -->  Structures not always superimposed after multiple structures are shown for one or more sequences.
  - <!-- JAL-1370 -->  Reference sequence characters should not be replaced with '.' when 'Show unconserved' format option is enabled.
  - <!-- JAL-1823 -->  Cannot specify chain code when entering specific PDB id for sequence
  - <!-- JAL-1944 -->  File->Export->.. as doesn't work when 'Export hidden sequences' is enabled, but 'export hidden columns' is disabled.
  - <!--JAL-2026-->  Best Quality option in structure chooser selects lowest rather than highest resolution structures for each sequence
  - <!-- JAL-1887 -->  Incorrect start and end reported for PDB to sequence mapping in 'View Mappings' report
  - <!-- JAL-2284 -->  Unable to read old Jalview projects that contain non-XML data added after Jalvew wrote project.
  - <!-- JAL-2118 -->  Newly created annotation row reorders after clicking on it to create new annotation for a column.
  - <!-- JAL-1980 -->  Null Pointer Exception raised when pressing Add on an orphaned cut'n'paste window.


### Applet
  - <!-- JAL-2151 -->  Incorrect columns are selected when hidden columns present before start of sequence
  - <!-- JAL-1986 -->  Missing dependencies on applet pages (JSON jars)
  - <!-- JAL-1947 -->  Overview pixel size changes when sequences are hidden in applet
  - <!-- JAL-1996 -->  Updated instructions for applet deployment on examples pages.
