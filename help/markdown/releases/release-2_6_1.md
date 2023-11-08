---
channel: release
version: 2.6.1
date: 2010-11-15
---

## New Features



### Application
- New warning dialog when the Jalview Desktop cannot contact web services
- JABA service parameters for a preset are shown in service job window
- JABA Service menu entries reworded


## Issues Resolved

- Modeller PIR IO broken - cannot correctly import a pir file emitted by Jalview
- Existing feature settings transferred to new alignment view created from cut'n'paste
- Improved test for mixed amino/nucleotide chains when parsing PDB files
- Consensus and conservation annotation rows occasionally become blank for all new windows
- Exception raised when right clicking above sequences in wrapped view mode


### Application
- multiple multiply aligned structure views cause cpu usage to hit 100% and computer to hang
- Web Service parameter layout breaks for long user parameter names
- Jaba service discovery hangs desktop if Jaba server is down
