---
channel: release
version: 2.8.0b1
date: 2014-01-30
---

## New Features

- Trusted certificates for JalviewLite applet and Jalview Desktop application<br/>
Certificate was donated by [Certum](https://www.certum.eu) to the Jalview open source project).
- Jalview SRS links replaced by UniProt and EBI-search
- Output in Stockholm format
- Allow import of data from gzipped files
- Export/import group and sequence associated line graph thresholds
- Nucleotide substitution matrix that supports RNA and ambiguity codes
- Allow disorder predictions to be made on the current selection (or visible selection) in the same way that JPred works
- Groovy scripting for headless Jalview operation


### Other improvements
- Upgrade desktop installer to InstallAnywhere 2013
- COMBINE statement uses current SEQUENCE_REF and GROUP_REF scope to group annotation rows
- Support '' style escaping of quotes in Newick files
- Group options for JABAWS service by command line name
- Empty tooltip shown for JABA service options with a link but no description
- Select primary source when selecting authority in database fetcher GUI
- Add .mfa to FASTA file extensions recognised by Jalview
- Annotation label tooltip text wrap


## Issues Resolved

- Slow scrolling when lots of annotation rows are displayed
- Lots of NPE (and slowness) after creating RNA secondary structure annotation line
- Sequence database accessions not imported when fetching alignments from Rfam
- Incorrect SHMR submission for sequences with identical IDs
- View all structures does not always superpose structures
- Option widgets in service parameters not updated to reflect user or preset settings
- Null pointer exceptions for some services without presets or adjustable parameters
- Discover PDB IDs entry in structure menu doesn't discover PDB xRefs
- Exception encountered while trying to retrieve features with DAS
- Lowest value in annotation row isn't coloured when colour by annotation (per sequence) is coloured
- Keyboard mode P jumps to start of gapped region when residue follows a gap
- Jalview appears to hang importing an alignment with Wrap as default or after enabling Wrap
- 'Right click to add annotations' message shown in wrap mode when no annotations present
- Disorder predictions fail with NPE if no automatic annotation already exists on alignment
- oninit javascript function should be called after initialisation completes
- Remove redundancy after disorder prediction corrupts alignment window display
- Example annotation file in documentation is invalid
- Grouped line graph annotation rows are not exported to annotation file
- Multi-harmony analysis cannot be run when only two groups created
- Cannot create multiple groups of line graphs with several 'combine' statements in annotation file
- Pressing return several times causes Number Format exceptions in keyboard mode
- Multi-harmony (SHMMR) method doesn't submit correct partitions for input data
- Translation from DNA to Amino Acids fails
- Jalview fail to load newick tree with quoted label
- --headless flag isn't understood
- ClassCastException when generating EPS in headless mode
- Adjusting sequence-associated shading threshold only changes one row's threshold
- Preferences and Feature settings panel panel doesn't open
- hide consensus histogram also hides conservation and quality histograms
