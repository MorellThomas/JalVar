---
channel: release
version: 2.11.1.5
date: 2021-12-20
---

## New Features



### Security
- <!-- JAL-3933 -->  Update library dependency: Log4j 2.16.0 (was log4j 1.2.x).


### Development
- Updated building instructions


## Issues Resolved

- <!-- JAL-3840 -->  Occupancy calculation is incorrect for alignment columns with over -1+2^32 gaps (breaking filtering and display)
- <!-- JAL-3833 -->  Caps on Hi-DPI scaling to prevent crazy scale factors being set with buggy window-managers (linux only)


### Development
- Fixed non-fatal gradle errors during build
