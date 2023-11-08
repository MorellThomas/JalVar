---
version: 2.11.2.3
date: 2022-07-06
channel: "release"
---

## New Features


- <!-- JAL-1988,JAL-3416 --> "Do you really want to Quit ?" prompt on OSX/Java 8
- <!-- JAL-4004 --> Release notes and what's new documentation pages now generated from individual markdown files
- <!-- JAL-3989 --> Release process generates Hugo friendly JSON metadata to use when publishing releases on www.jalview.org
- <!-- JAL-3553 --> New gradle tasks for publishing to the Jalview version archive
- <!-- JAL-4023 --> Tree branch labels shown using Scientific notation for very small or large lengths
- <!-- JAL-4036 --> Uniprot Free Text Search now uses legacy.uniprot.org rather than main Uniprot query service


## Issues Resolved

- <!-- JAL-4036 --> Uniprot Free Text Search in Jalview 2.11.2.2 and earlier stopped working on 29th June 2022
- <!-- JAL-4008 --> Validation fails when trying to configure custom JABAWS server
- <!-- JAL-4020 --> Jalview doesn't call PymolWIN.exe correctly - improved recognition of binaries on Windows
- <!-- JAL-4024 --> Jumping from left to far right via rapid drag of scroll bar or clicking the overview window can cause Jalview to temporarily hang when working with alignments with more than 10 thousand columns
- <!-- JAL-4023 --> Labels for tree branches smaller than 0.005 are shown as '0' in the tree viewer
- <!-- JAL-1544 --> Fixed typos in documentation
- <!-- JAL-4026 --> defensive patches in code and improvements for failing tests 
