---
channel: release
version: 2.11.1.3
date: 2020-10-29
---

## Issues Resolved

- <!-- JAL-3765 -->  Find doesn't always highlight all matching positions in a sequence (bug introduced in 2.11.1.2)
- <!-- JAL-3760 -->  Alignments containing one or more protein sequences can be classed as nucleotide
- <!-- JAL-3748 -->  CDS alignment doesn't match original CDS sequences after alignment of protein products (known defect first reported for 2.11.1.0)
- <!-- JAL-3725 -->  No tooltip or popup menu for genomic features outwith CDS shown overlaid on protein
- <!-- JAL-3751 -->  Overlapping CDS in ENA accessions are not correctly mapped by Jalview (e.g. affects viral CDS with ribosomal slippage, since 2.9.0)
- <!-- JAL-3763 -->  Spliced transcript CDS sequences don't show CDS features
- <!-- JAL-3700 -->  Selections in CDS sequence panel don't always select corresponding protein sequences
- <!-- JAL-3759 -->   *Make groups from selection* for a column selection doesn't always ignore hidden columns


### Installer
- <!-- JAL-3611 -->  Space character in Jalview install path on Windows prevents install4j launching getdown


### Development
- <!-- JAL-3248 -->  Fixed typos and specified compatible gradle version numbers in doc/building.md
