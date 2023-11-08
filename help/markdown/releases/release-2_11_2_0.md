---
channel: release
version: 2.11.2.0
date: 2022-03-10
---

## New Features

- <!-- JAL-3616 JAL-3551 JAL-2322 -->  Support for viewing 3D structures with ChimeraX and Pymol in addition to Jmol and Chimera.
- <!-- JAL-3829 -->  Discover 3D structure data for sequences with Uniprot references via 3D-Beacons
- <!-- JAL-3391 -->  Rank and select available structures for Uniprot sequences according to number of residues in structure mapped to positions involved in the alignment
- <!-- JAL-2226 -->  Structure annotation rows for all mapped chains in 3D structures are included in the 'Reference Annotation' for a sequence
- <!-- JAL-1260 -->  Import Genbank and EMBL format flatfiles
- <!-- JAL-3821 -->  ENA record's mol_type honoured so RNA molecules imported from ENA records are shown as RNA
- <!-- JAL-3863 -->  Support for Canonical Uniprot IDs
- <!-- JAL-3503 -->  New Preferences tab for adjusting Jalview's memory settings at launch
- <!-- JAL-3881 -->  Sequence IDs split on '_' as well as other non-alphanumerics when discovering database references with 'Fetch DB Refs'
- <!-- JAL-3884 -->  Suppressed harmless exceptions output to Console whilst discovering database references for a sequence
- <!-- JAL-3204 -->  Updated Jalview bindings for Uniprot XML schema
- <!-- JAL-3926 -->  Uniprot and PDBe autosearch option is disabled by default
- <!-- JAL-3144 -->  Reverted to Jalview 'classic' drop-down menu for selecting which database to fetch from in sequence fetcher dialog.
- <!-- JAL-3018 -->  Updated Ensembl REST Client compatibility to 15.2 and revised model organism names (rat, xenopus, dmelanogaster now rattus_norvegicus, xenopus_tropicalis, drosophila_melanogaster)
- <!-- JAL-3530 -->  -nowebservicediscovery command line argument to prevent automatic discovery of analysis webservices on launch
- <!-- JAL-3618 -->  Allow 'App' directories to be opened when locating Chimera, ChimeraX or Pymol binaries via filechooser opened by double clicking the Structure Preferences' path textbox
- <!-- JAL-3632 JAL-3633 -->  support for HTTP/S access via proxies that require authentication
- <!-- JAL-3103 -->  New mechanism for opening URLs with system default browser (works on OSX and Linux as well as Windows)
- <!-- JAL-3871 JAL-3874 -->  Upgraded bundled version of Jmol to 14.31.53
- <!-- JAL-3837 -->  GPL license info on splash screen and About text


### Jalview Native App
- <!-- JAL-3830 -->  New command line launcher scripts (.sh, .ps1, .bat) usable on macOS, Linux/Unix, Windows and documentation in Help. Installer wizard has option to add this to PATH, or link to it in your PATH.<br/>
 *This is the recommended workaround for known issue about working directory preservation when running native application from command line.*
- Notarized MacOS installer for compliance with latest OSX releases (Monterey)
- <!-- JAL-3805 -->  Uninstaller application for old (InstallAnywhere based) Jalview installations removed from the OSX disk image
- <!-- JAL-3608 -->  Options to allow user to choose the (Swing) Look and Feel (LaF) used by Jalview
- <!-- JAL-3552, JAL-3609 -->  Metal LaF used to improved operation on Linux Ubuntu with HiDPI display in Java 11 (still known issues with HiDPI screens in java 8 and 11. see [JAL-3137](https://issues.jalview.org/browse/JAL-3137))
- <!-- JAL-3633 -->  Getdown launcher inherits HTTP/S proxy configuration from jalview_properties
- <!-- JAL- -->  New Jalview Develop app - making it even easier to get at Jalview's development builds
- <!-- JAL-3594 -->  New splashscreens for Jalview, Jalview Test and Jalview Develop applications.
- <!-- JAL-3728 -->  Jalview logos shown for Jalview Java Console and other window widgets in taskbar and dock rather than anonymous 'Java' icons


### JalviewJS
- <!-- JAL-3624 -->  PDB structures mapped to Uniprot Sequences with SIFTS
- <!-- JAL-3208 -->  setprop commandline argument reinstated for JalviewJS only
- <!-- JAL-3163 -->  Missing message bundle keys are only reported once per key (avoids excessive log output in js console)
- <!-- JAL-3168 -->  Feature type is included in the title of the Feature Settings' Colour Chooser dialog
- <!-- JAL-3279 -->  Build details reported in About window
- <!-- JAL-3038 JAL-3071 JAL-3263 JAL-3084 -->  Numerous minor GUI additions and improvements in sync with Java application.


### Development
- <!--   -->  First integrated JalviewJS and Jalview release
- <!-- JAL-3841,JAL-3248 -->  Updated README and doc/building.md
- <!-- JAL-3789, JAL-3679 -->  Improved JalviewJS/Jalview build process, added support for system package provided eclipse installs on linux
- Install4j 9.0.x used for installer packaging
- <!-- JAL-3930 -->  Improved use of installers for unattended installation with a customizedId of "JALVIEW" in install4j's Jalview Launcher
- <!-- JAL-3907 -->  Improved compatibility of Jalview build with Java 17 (next LTS target)


## Issues Resolved

- <!-- JAL-3674 -->  Slow structure commands can block Jalview execution
- <!-- JAL-3904 -->  Structure window's viewer-specific menu disappears when only one structure is shown (and many sequences:one chain mappings are present)
- <!-- JAL-3779 -->  Annotation file: PROPERTIES apply only to the first SEQUENCE_GROUP defined
- <!-- JAL-3700,JAL-3751,JAL-3763, JAL-3725 -->  Selections not propagated between Linked CDS - Protein alignments and their trees (known defect from 2.11.1.3)
- <!-- JAL-3761  -->  Not all codon positions highlighted for overlapping exon splice sites (e.g due to RNA slippage)
- <!-- JAL-3794 -->  X was not being recognised as the unknown base in DNA sequences
- <!-- JAL-3915 -->  Removed RNAview checkbox and logic from Structure Preferences
- <!--  JAL-3583 -->  Tooltip behaviour improved (slightly)
- <!-- JAL-3162 -->  Can edit a feature so that start > end
- <!-- JAL-2848 -->  Cancel from Amend Features doesn't reset a modified graduated colour
- <!-- JAL-3788 -->  New View with automatic 'Show Overview' preference enabled results in Null Pointer Exceptions when clustal colouring is enabled
- <!-- JAL-3275 -->  Can open multiple Preferences panels
- <!-- JAL-3633 -->  Properly configure HTTPS proxy settings from Preferences
- <!-- JAL-3949 -->  Standard out logging broken: messages only routing to stderr and appear as a raw template
- <!-- JAL-3739 -->  Entering web service parameter values in numerical field doesn't update the value of the parameter until return is pressed.
- <!-- JAL-3749 -->  Resolved known issue (from 2.11.1.1) concerning duplicate CDS sequences generated when protein products for certain ENA records are repeatedly shown via Calculate->Show Cross Refs


### JalviewJS
- <!-- JAL-3202 -->  Consensus profile may include zero (rounded down) percentage values causing a divide by zero
- <!-- JAL-3762 -->  JalviewJS doesn't honour arguments passed via Info.args when there are arguments on the URL
- <!-- JAL-3602 -->  gradle closure-compiler not using UTF-8
- <!-- JAL-3603 -->  Annotation file fails to load from URL in JalviewJS


### Development
- Gradle
  - Fixed non-fatal gradle errors during build
  - <!--  JAL-3745 -->  Updated build.gradle for use with Gradle v.6.6+


### Known Issues
- <!-- JAL-3764 -->  Display of RESNUM sequence features are not suppressed when structures associated with a sequence are viewed with an external viewer (Regression from 2.11.1 series)
