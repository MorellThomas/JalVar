---
channel: release
version: 2.10.2b1
date: 2017-09-07
---

## New Features



  - <!-- JAL-2588 -->  Show gaps in overview window by colouring in grey (sequences used to be coloured grey, and gaps were white)
  - <!-- JAL-2588,JAL-2527 -->  Overview tab in Jalview Desktop Preferences
  - <!-- JAL-2587 -->  Overview updates immediately on increase in size and progress bar shown as higher resolution overview is recalculated


## Issues Resolved



  - <!-- JAL-2664 -->  Overview window redraws every hidden column region row by row
  - <!-- JAL-2681 -->  duplicate protein sequences shown after retrieving Ensembl crossrefs for sequences from Uniprot
  - <!-- JAL-2603 -->  Overview window throws NPE if show boxes format setting is unticked
  - <!-- JAL-2610 -->  Groups are coloured wrongly in overview if group has show boxes format setting unticked
  - <!-- JAL-2672,JAL-2665 -->  Redraw problems when autoscrolling whilst dragging current selection group to include sequences and columns not currently displayed
  - <!-- JAL-2691 -->  Not all chains are mapped when multimeric assemblies are imported via CIF file
  - <!-- JAL-2704 -->  Gap colour in custom colourscheme is not displayed when threshold or conservation colouring is also enabled.
  - <!-- JAL-2549 -->  JABAWS 2.2 services report wrong JABAWS server version
  - <!-- JAL-2673 -->  Jalview continues to scroll after dragging a selected region off the visible region of the alignment
  - <!-- JAL-2724 -->  Cannot apply annotation based colourscheme to all groups in a view
  - <!-- JAL-2511 -->  IDs don't line up with sequences initially after font size change using the Font chooser or middle-mouse zoom
