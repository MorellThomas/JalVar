---
channel: release
version: 2.10.3
date: 2017-11-17
---

## New Features



  - <!-- JAL-2446 -->  Faster and more efficient management and rendering of sequence features
  - <!-- JAL 2523-->  More reliable Ensembl fetching with HTTP 429 rate limit request hander
  - <!-- JAL-2773 -->  Structure views don't get updated unless their colours have changed
  - <!-- JAL-2495 -->  All linked sequences are highlighted for a structure mousover (Jmol) or selection (Chimera)
  - <!-- JAL-2790 -->  'Cancel' button in progress bar for JABAWS AACon, RNAAliFold and Disorder prediction jobs
  - <!-- JAL-2617 -->  Stop codons are excluded in CDS/Protein view from Ensembl locus cross-references
  - <!-- JAL-2685 -->  Start/End limits are shown in Pairwise Alignment report
  - <!-- JAL-2810 -->  Sequence fetcher's Free text 'autosearch' feature can be disabled
  - <!-- JAL-2810 -->  Retrieve IDs tab added for UniProt and PDB easier retrieval of sequences for lists of IDs
  - <!-- JAL-2758 -->  Short names for sequences retrieved from Uniprot


### Scripting
  - Groovy interpreter updated to 2.4.12
  - Example groovy script for generating a matrix of percent identity scores for current alignment.


### Testing and Deployment
  - <!-- JAL-2727 -->  Test to catch memory leaks in Jalview UI


## Issues Resolved



### General
  - <!-- JAL-2643 -->  Pressing tab after updating the colour threshold text field doesn't trigger an update to the alignment view
  - <!-- JAL-2682 -->  Race condition when parsing sequence ID strings in parallel
  - <!-- JAL-2608 -->  Overview windows are also closed when alignment window is closed
  - <!-- JAL-2548 -->  Export of features doesn't always respect group visibility
  - <!-- JAL-2831 -->  Jumping from column 1 to column 100,000 takes a long time in Cursor mode


### Desktop
  - <!-- JAL-2777 -->  Structures with whitespace chainCode cannot be viewed in Chimera
  - <!-- JAL-2728 -->  Protein annotation panel too high in CDS/Protein view
  - <!-- JAL-2757 -->  Can't edit the query after the server error warning icon is shown in Uniprot and PDB Free Text Search Dialogs
  - <!-- JAL-2253 -->  Slow EnsemblGenome ID lookup
  - <!-- JAL-2529 -->  Revised Ensembl REST API CDNA query
  - <!-- JAL-2739 -->  Hidden column marker in last column not rendered when switching back from Wrapped to normal view
  - <!-- JAL-2768 -->  Annotation display corrupted when scrolling right in unwapped alignment view
  - <!-- JAL-2542 -->  Existing features on subsequence incorrectly relocated when full sequence retrieved from database
  - <!-- JAL-2733 -->  Last reported memory still shown when Desktop->Show Memory is unticked (OSX only)
  - <!-- JAL-2658 -->  Amend Features dialog doesn't allow features of same type and group to be selected for amending
  - <!-- JAL-2524 -->  Jalview becomes sluggish in wide alignments when hidden columns are present
  - <!-- JAL-2392 -->  Jalview freezes when loading and displaying several structures
  - <!-- JAL-2732 -->  Black outlines left after resizing or moving a window
  - <!-- JAL-1900,JAL-1625 -->  Unable to minimise windows within the Jalview desktop on OSX
  - <!-- JAL-2667 -->  Mouse wheel doesn't scroll vertically when in wrapped alignment mode
  - <!-- JAL-2636 -->  Scale mark not shown when close to right hand end of alignment
  - <!-- JAL-2684 -->  Pairwise alignment of selected regions of each selected sequence do not have correct start/end positions
  - <!-- JAL-2793 -->  Alignment ruler height set incorrectly after canceling the Alignment Window's Font dialog
  - <!-- JAL-2036 -->  Show cross-references not enabled after restoring project until a new view is created
  - <!-- JAL-2756 -->  Warning popup about use of SEQUENCE_ID in URL links appears when only default EMBL-EBI link is configured (since 2.10.2b2)
  - <!-- JAL-2775 -->  Overview redraws whole window when box position is adjusted
  - <!-- JAL-2225 -->  Structure viewer doesn't map all chains in a multi-chain structure when viewing alignment involving more than one chain (since 2.10)
  - <!-- JAL-2811 -->  Double residue highlights in cursor mode if new selection moves alignment window
  - <!-- JAL-2837,JAL-2840 -->  Alignment vanishes when using arrow key in cursor mode to pass hidden column marker
  - <!-- JAL-2679 -->  Ensembl Genomes example ID changed to one that produces correctly annotated transcripts and products
  - <!-- JAL-2776 -->  Toggling a feature group after first time doesn't update associated structure view


### Applet
  - <!-- JAL-2687 -->  Concurrent modification exception when closing alignment panel


### BioJSON
  - <!-- JAL-2546 -->  BioJSON export does not preserve non-positional features


### New Known Issues
  - <!-- JAL-2541 -->  Delete/Cut selection doesn't relocate sequence features correctly (for many previous versions of Jalview)
  - <!-- JAL-2841 -->  Cursor mode unexpectedly scrolls when using cursor in wrapped panel other than top
  - <!-- JAL-2791 -->  Select columns containing feature ignores graduated colour threshold
  - <!-- JAL-2822,JAL-2823 -->  Edit sequence operation doesn't always preserve numbering and sequence features


### Known Java 9 Issues
  - <!-- JAL-2902 -->  Groovy Console very slow to open and is not responsive when entering characters (Webstart, Java 9.01, OSX 10.10)
