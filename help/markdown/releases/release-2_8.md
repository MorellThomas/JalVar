---
channel: release
version: 2.8
date: 2012-11-12
---

## New Features



### Application
- Support for JABAWS 2.0 Services (AACon alignment conservation, protein disorder and Clustal Omega)
- JABAWS server status indicator in Web Services preferences
- VARNA (http://varna.lri.fr) viewer for RNA structures in Jalview alignment window
- Updated Jalview build and deploy framework for OSX mountain lion, windows 7, and 8
- Nucleotide substitution matrix for PCA that supports RNA and ambiguity codes
- Improved sequence database retrieval GUI
- Support fetching and database reference look up against multiple DAS sources (Fetch all from in 'fetch db refs')
- Jalview project improvements
  - Store and retrieve the 'belowAlignment' flag for annotation
  - calcId attribute to group annotation rows on the alignment
  - Store AACon calculation settings for a view in Jalview project
- horizontal scrolling gesture support
- Visual progress indicator when PCA calculation is running
- Simpler JABA web services menus
- visual indication that web service results are still being retrieved from server
- Serialise the dialogs that are shown when Jalview starts up for first time
- Jalview user agent string for interacting with HTTP services
- DAS 1.6 and DAS 2.0 source support using new JDAS client library
- Examples directory and Groovy library included in InstallAnywhere distribution


### Applet
- RNA alignment and secondary structure annotation visualization applet example


### General
- Normalise option for consensus sequence logo
- Reset button in PCA window to return dimensions to defaults
- Allow seqspace or Jalview variant of alignment PCA calculation
- PCA with either nucleic acid and protein substitution matrices
- Allow windows containing HTML reports to be exported in HTML
- Interactive display and editing of RNA secondary structure contacts
- RNA Helix Alignment Colouring
- RNA base pair logo consensus
- Parse sequence associated secondary structure information in Stockholm files
- HTML Export database accessions and annotation information presented in tooltip for sequences
- Import secondary structure from LOCARNA clustalw style RNA alignment files
- import and visualise T-COFFEE quality scores for an alignment
- 'colour by annotation' per sequence option to shade each sequence according to its associated alignment annotation
- New Jalview Logo


### Documentation and Development
- documentation for score matrices used in Jalview
- New Website!


## Issues Resolved



### Application
- PDB, Unprot and EMBL (ENA) databases retrieved via wsdbfetch REST service
- Stop windows being moved outside desktop on OSX
- Filetype associations not installed for webstart launch
- Jalview does not always retrieve progress of a JABAWS job execution in full once it is complete
- revise SHMR RSBS definition to ensure alignment is uploaded via ali_file parameter
- Jalview 2.7 is incompatible with Jmol-12.2.2
- View all structures superposed fails with exception
- Jnet job queues forever if a very short sequence is submitted for prediction
- Cut and paste menu not opened when mouse clicked on desktop window
- Putting fractional value into integer text box in alignment parameter dialog causes Jalview to hang
- Structure view highlighting doesn't work on windows 7
- View all structures fails with exception shown in structure view
- Characters in filename associated with PDBEntry not escaped in a platform independent way
- Jalview desktop fails to launch with exception when using proxy
- Tree calculation reports 'you must have 2 or more sequences selected' when selection is empty
- Jalview desktop fails to launch with jar signature failure when java web start temporary file caching is disabled
- DAS Sequence retrieval with range qualification results in sequence xref which includes range qualification
- Errors during processing of command line arguments cause progress bar (JAL-898) to be removed
- Replace comma for semi-colon option not disabled for DAS sources in sequence fetcher
- Cannot close news reader when JABAWS server warning dialog is shown
- Option widgets not updated to reflect user settings
- Edited sequence not submitted to web service
- Jalview 2.7 Webstart does not launch on mountain lion
- InstallAnywhere installer doesn't unpack and run on OSX Mountain Lion
- Annotation panel not given a scroll bar when sequences with alignment annotation are pasted into the alignment
- Sequence associated annotation rows not associated when loaded from Jalview project
- Browser launch fails with NPE on java 1.7
- JABAWS alignment marked as finished when job was cancelled or job failed due to invalid input
- NPE with v2.7 example when clicking on Tree associated with all views
- Exceptions when copy/paste sequences with grouped annotation rows to new window


### Applet
- Sequence features are momentarily displayed before they are hidden using hidefeaturegroups applet parameter
- loading features via javascript API automatically enables feature display
- scrollToColumnIn javascript API method doesn't work


### General
- Redundancy removal fails for rna alignment
- PCA calculation fails when sequence has been selected and then deselected
- PCA window shows grey box when first opened on OSX
- Letters coloured pink in sequence logo when alignment coloured with clustalx
- Choosing fonts without letter symbols defined causes exceptions and redraw errors
- Initial PCA plot view is not same as manually reconfigured view
- Grouped annotation graph label has incorrect line colour
- Grouped annotation graph label display is corrupted for lots of labels
