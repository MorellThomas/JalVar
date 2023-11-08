---
channel: release
version: 2.10.1
date: 2016-11-29
---

## New Features



### General
  - <!-- JAL-98 -->  Improved memory usage: sparse arrays used for all consensus calculations
  - <!-- JAL-2177 -->  Jmol updated to version 14.6.4 (released 3rd Oct 2016)
  - Updated Jalview's Certum code signing certificate for 2016-2017


### Application
  - <!-- JAL-1723 -->  Sequence ID tool tip presents abridged set of database cross-references, sorted alphabetically
  - <!-- JAL-2282-->  New replacement token for creating URLs *just* from database cross references. Users with custom links will receive a [warning dialog](webServices/urllinks.html#warning) asking them to update their preferences.
  - <!-- JAL-2287-->  Cancel button and escape listener on dialog warning user about disconnecting Jalview from a Chimera session
  - <!-- JAL-2320-->  Jalview's Chimera control window closes if the Chimera it is connected to is shut down
  - <!-- JAL-1738-->  New keystroke (B) and Select highlighted columns menu item to mark columns containing highlighted regions (e.g. from structure selections or results of a Find operation)
  - <!-- JAL-2284-->  Command line option for batch-generation of HTML pages rendering alignment data with the BioJS MSAviewer


## Issues Resolved



### General
  - <!-- JAL-2286 -->  Columns with more than one modal residue are not coloured or thresholded according to percent identity (first observed in Jalview 2.8.2)
  - <!-- JAL-2301 -->  Threonine incorrectly reported as not hydrophobic
  - <!-- JAL-2318 -->  Updates to documentation pages (above PID threshold, amino acid properties)
  - <!-- JAL-2292 -->  Lower case residues in sequences are not reported as mapped to residues in a structure file in the View Mapping report
  - <!--JAL-2324 -->  Identical features with non-numeric scores could be added multiple times to a sequence
  - <!--JAL-2323, JAL-2333,JAL-2335,JAL-2327 -->  Disulphide bond features shown as two highlighted residues rather than a range in linked structure views, and treated correctly when selecting and computing trees from features
  - <!-- JAL-2281-->  Custom URL links for database cross-references are matched to database name regardless of case


### Application
  - <!-- JAL-2282-->  Custom URL links for specific database names without regular expressions also offer links from Sequence ID
  - <!-- JAL-2315-->  Removing a single configured link in the URL links pane in Connections preferences doesn't actually update Jalview configuration
  - <!-- JAL-2272-->  CTRL-Click on a selected region to open the alignment area popup menu doesn't work on El-Capitan
  - <!-- JAL-2280 -->  Jalview doesn't offer to associate mmCIF files with similarly named sequences if dropped onto the alignment
  - <!-- JAL-2312 -->  Additional mappings are shown for PDB entries where more chains exist in the PDB accession than are reported in the SIFTS file
  - <!-- JAL-2317-->  Certain structures do not get mapped to the structure view when displayed with Chimera
  - <!-- JAL-2317-->  No chains shown in the Chimera view panel's View->Show Chains submenu
  - <!--JAL-2277 -->  Export as HTML with embedded SVG doesn't work for wrapped alignment views
  - <!--JAL-2197 -->  Rename UI components for running JPred predictions from 'JNet' to 'JPred'
  - <!-- JAL-2337,JAL-2277 -->  Export as PNG or SVG is corrupted when annotation panel vertical scroll is not at first annotation row
  - <!--JAL-2332 -->  Attempting to view structure for Hen lysozyme results in a PDB Client error dialog box
  - <!-- JAL-2319 -->  Structure View's mapping report switched ranges for PDB and sequence for SIFTS
