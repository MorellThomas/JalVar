---
channel: release
version: 2.5
date: 2010-04-30
---

## New Features



### New Capabilities
- URL links generated from description line for regular-expression based URL links (applet and application)
- Non-positional feature URL links are shown in link menu
- Linked viewing of nucleic acid sequences and structures
- Automatic Scrolling option in View menu to display the currently highlighted region of an alignment.
- Order an alignment by sequence length, or using the average score or total feature count for each sequence.
- Shading features by score or associated description
- Subdivide alignment and groups based on identity of selected subsequence (Make Groups from Selection).
- New hide/show options including Shift+Control+H to hide everything but the currently selected region.


### Application
- Fetch DB References capabilities and UI expanded to support retrieval from DAS sequence sources
- Local DAS Sequence sources can be added via the command line or via the Add local source dialog box.
- DAS Dbref and DbxRef feature types are parsed as database references and protein_name is parsed as description line (BioSapiens terms).
- Enable or disable non-positional feature and database references in sequence ID tooltip from View menu in application.
- Group-associated consensus, sequence logos and conservation plots
- Symbol distributions for each column can be exported and visualized as sequence logos
- <!-- todo for applet --> Optionally scale multi-character column labels to fit within each column of annotation row
- Optional automatic sort of associated alignment view when a new tree is opened.
- Jalview Java Console
- Better placement of desktop window when moving between different screens.
- New preference items for sequence ID tooltip and consensus annotation
- Client to submit sequences and IDs to Envision2 Workflows
- *Vamsas Capabilities*
  - Improved VAMSAS synchronization (Jalview archive used to preserve views, structures, and tree display settings)
  - Import of vamsas documents from disk or URL via command line
  - Sharing of selected regions between views and with other VAMSAS applications (Experimental feature!)
  - Updated API to VAMSAS version 0.2


### Applet
- Middle button resizes annotation row height
- New Parameters
  - sortByTree (true/false) - automatically sort the associated alignment view by the tree when a new tree is opened.
  - showTreeBootstraps (true/false) - show or hide branch bootstraps (default is to show them if available)
  - showTreeDistances (true/false) - show or hide branch lengths (default is to show them if available)
  - showUnlinkedTreeNodes (true/false) - indicate if unassociated nodes should be highlighted in the tree view
  - heightScale and widthScale (1.0 or more) - increase the height or width of a cell in the alignment grid relative to the current font size.
- Non-positional features displayed in sequence ID tooltip


### Other
- Features format: graduated colour definitions and specification of feature scores
- Alignment Annotations format: new keywords for group associated annotation (GROUP_REF) and annotation row display properties (ROW_PROPERTIES)
- XML formats extended to support graduated feature colourschemes, group associated annotation, and profile visualization settings.


## Issues Resolved

- Source field in GFF files parsed as feature source rather than description
- Non-positional features are now included in sequence feature and gff files (controlled via non-positional feature visibility in tooltip).
- URL links generated for all feature links (bugfix)
- Added URL embedding instructions to features file documentation.
- Codons containing ambiguous nucleotides translated as 'X' in peptide product
- Match case switch in find dialog box works for both sequence ID and sequence string and query strings do not have to be in upper case to match case-insensitively.
- AMSA files only contain first column of multi-character column annotation labels
- Jalview Annotation File generation/parsing consistent with documentation (e.g. Stockholm annotation can be exported and re-imported)
- PDB files without embedded PDB IDs given a friendly name
- Find incrementally searches ID string matches as well as subsequence matches, and correctly reports total number of both.
- Application:
  - Better handling of exceptions during sequence retrieval
  - Dasobert generated non-positional feature URL link text excludes the start_end suffix
  - DAS feature and source retrieval buttons disabled when fetch or registry operations in progress.
  - PDB files retrieved from URLs are cached properly
  - Sequence description lines properly shared via VAMSAS
  - Sequence fetcher fetches multiple records for all data sources
  - Ensured that command line das feature retrieval completes before alignment figures are generated.
  - Reduced time taken when opening file browser for first time.
  - isAligned check prior to calculating tree, PCA or submitting an MSA to JNet now excludes hidden sequences.
  - User defined group colours properly recovered from Jalview projects.
