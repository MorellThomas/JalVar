---
version: 2.11.3.0
date: 2023-07-19
channel: "release"
---

## New Features
- <!-- JAL-4064 --> Native M1 build for macOS using Adoptium JRE 11 macos-aarch64
- <!-- JAL-4054 --> Installers built with install4j10
- <!-- JAL-3676 --> Allow log level configuration via Jalview's Java Console, and a Copy to Clipboard button
- <!-- JAL-3416 --> FlatLAF default look and feel on Linux, OSX and everywhere else ?

- <!-- JAL-4019 --> Ambiguous Base Colourscheme
- <!-- JAL-4061 --> Find can search sequence features' type and description
- <!-- JAL-4062 --> Hold down Shift + CMD/CTRL C to copy highlighted regions as new sequences
- <!-- JAL-1556 --> Quickly enable select and/or colour by for displayed annotation row via its popup menu
- <!-- JAL-4094 --> Shift+Click+Drag to adjust height of all annotation tracks of same type
- <!-- JAL-4190 --> Pressing escape in tree panel clears any current selection

- <!-- JAL-4089 --> Use selected columns for superposition
- <!-- JAL-4086 --> Highlight aligned positions on all associated structures when mousing over a column

- <!-- JAL-4221 --> sequence descriptions are updated from database reference sources if not already defined


### Improved support for working with computationally determined models

- <!-- JAL-3895 --> Alphafold red/orange/yellow/green colourscheme for structures
- <!-- JAL-4095 --> Interactive picking of low pAE score regions
- <!-- JAL-4027 --> contact matrix datatype in Jalview
- <!-- JAL-4033 --> Selections with visual feedback via contact matrix annotation

- <!-- JAL-3855 --> Discover and import alphafold2 models and metadata from https://alphafold.ebi.ac.uk/

- <!-- JAL-4091 --> Visual indication of relationship with associated sequence to distinguish different sequence associated annotation rows
- <!-- JAL-4123 --> GUI and command line allows configuration of how temperature factor in imported 3D structure data should be interpreted
- <!-- JAL-3914 --> Import model reliability scores encoded as temperature factor annotation with their correct name and semantics
- <!-- JAL-3858 --> Import and display alphafold alignment uncertainty matrices from JSON
- <!-- JAL-4134,JAL-4158 --> Column-wise alignment groups and selections and interactive tree viewer for PAE matrices
- <!-- JAL-4124 --> Store/Restore PAE data and visualisation settings from Jalview Project
- <!-- JAL-4083 --> Multiple residue sidechain highlighting in structure viewers from PAE mouseovers


### Jalview on the command line

- <!-- JAL-4160,JAL-629 --> New command line argument framework allowing flexible batch processing, figure generation, and import of structures, pae matrices and other sequence associated data
- <!-- JAL-4121 --> Assume --headless when jalview is run with a command line argument that generates output

### Other improvements

- <!-- JAL-3119 --> Name of alignment and view included in overview window's title
- <!-- JAL-4213 --> "add reference annotation" add all positions in reference annotation tracks, not just positions in the currently highlighted columns/selection range
- <!-- JAL-4119 --> EMBL-EBI SIFTS file downloads now use split directories

- <!-- JAL-4195,JAL-4194,JAL-4193 --> sensible responses from the CLI when things go wrong during image export
Add a command line option to set Jalview properties for this session only
Add a command line option to suppress opening the startup file for this session


JAL-4187	 Powershell launcher script fails when given no arguments with the old ArgsParser

known issue ? <!-- JAL-4127	--> 'Reload' for a jalview project results in all windows being duplicated


- <!-- JAL-3830 --> Command-line wrapper script for macOS bundle, linux and Windows installations (bash, powershell and .bat wrappers)
- <!-- JAL-3820 --> In Linux desktops' task-managers, the grouped Jalview windows get a generic name

## Still in progress (delete on release)

- <!-- JAL-2382 --> Import and display sequence-associated contact predictions in CASP-RR format
- <!-- JAL-2349 --> Contact prediction visualisation
- <!-- JAL-2348 --> modularise annotation renderer

### Development and Deployment

- <!-- JAL-4167 --> Create separate gradle test task for some tests
- <!-- JAL-4111 --> Allow gradle build to create suffixed DEVELOP-... builds with channel appbase
- <!-- JAL-4243 --> Jalview bio.tools description maintained under jalview's git repo and bundled with source release

## Issues Resolved
- <!-- JAL-2961 --> Jmol view not always centred on structures when multiple structures are viewed
- <!-- JAL-3776 --> Cancelling interactive calculation leaves empty progress bar.
- <!-- JAL-3772 --> Unsaved Alignment windows close without prompting to save, individually or at application quit.
- <!-- JAL-1988 --> Can quit Jalview while 'save project' is in progress
- <!-- JAL-4126 --> 'Use original colours' option of colour by annotation not honoured when restoring view from project
- <!-- JAL-4116 --> PDB structures slow to view when Jalview Java Console is open (2.11.2.7 patch)
- <!-- JAL-3784 --> Multiple overview windows can be opened for a particular alignment view when multiple views are present
- <!-- JAL-3785 --> Overview windows opened automatically (due to preferences settings) lack title
- <!-- JAL-2353 --> Show Crossrefs fails to retrieve CDS from ENA or Ensembl for sequences retrieved from Uniprot due to version numbers in cross-reference accession
- <!-- JAL-4184 --> Stockholm export does not include sequence descriptions
- <!-- JAL-4075 --> Don't add string label version of DSSP secondary structure codes in secondary structure annotation rows
- <!-- JAL-4182 --> reference annotation not correctly transferred to alignment containing a sub-sequence when a selection is active
- <!-- JAL-4177 --> Can press 'Add' or 'New View' multiple times when manually adding and viewing a 3D structure via structure chooser
- <!-- JAL-4133 --> Jalview project does not preserve font aspect ratio when Viewport is zoomed with mouse
- <!-- JAL-4128 --> Resizing overview quickly with solid-drags enabled causes exception
- <!-- JAL-4150 --> Sequences copied to clipboard from within Jalview cannot be pasted via the desktop's popup menu to a new alignment window
- <!-- JAL-2528, JAL-1713 --> Overview window is saved in project file, and state of 'show hidden regions' is preserved.
- <!-- JAL-4153 --> JvCacheableInputBoxTest flaky on build server

## New Known defects
- <!-- JAL-4178 --> Cannot cancel structure view open action once it has been started via the structure chooser dialog
- <!-- JAL-4142 --> Example project's multiple views do not open in distinct locations when eXpand views is used to show them all separately
- <!-- JAL-4165 --> Missing last letter when copying consensus sequence from alignment if first column is hidden




