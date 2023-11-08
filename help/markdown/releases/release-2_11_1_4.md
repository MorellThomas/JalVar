---
channel: release
version: 2.11.1.4
date: 2021-03-09
---

## New Features



### Improved control of Jalview's use of network services via jalview_properties
- <!-- JAL-3814 -->  New .jalview_properties token controlling launch of the news browser (like -nonews argument)
- <!-- JAL-3813 -->  New .jalview_properties token controlling download of linkout URLs from www.jalview.org/services/identifiers
- <!-- JAL-3812 -->  New .jalview_properties token controlling download of BIOJSHTML templates
- <!-- JAL-3811 -->  New 'Discover Web Services' option to trigger a one off JABAWS discovery if autodiscovery was disabled


## Issues Resolved

- <!-- JAL-3818 -->  Intermittent deadlock opening structure in Jmol


### New Known defects
- <!-- JAL-3705 -->  Protein Cross-Refs for Gene Sequence not always restored from project (since 2.10.3)
- <!-- JAL-3806 -->  Selections from tree built from CDS aren't propagated to Protein alignment (since 2.11.1.3)
