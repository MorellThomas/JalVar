# Information for making the DMG

## How to make the DS_Store file needed for the DMG prettification

> ALSO SEE https://www.ej-technologies.com/resources/install4j/help/doc/concepts/dmgStyling.html

First create a dmg with no DS_Store (or an existing one if you just want to edit one)

```
cp ../../build/install4j/11/Jalview-OFFLINE_macos-app_DEVELOPMENT-j11.dmg ./temp.dmg
hdiutil convert temp.dmg -format UDRW -o temp_rw.dmg
```

If you are creating a new dmg, or want to add more files than remaining space, then you will need to add some capacity to the dmg.  If you're just doing small edits then you might get away without the following block of commands used to resize the dmg.

```
CURSIZE=`hdiutil resize temp_rw.dmg | perl -n -e 'm/^\s*\d+\s+(\d+)\s+\d+$/ && print "$1\n";'`
NEWSIZE=$(( CURSIZE + 20000))
hdiutil resize -sectors $NEWSIZE temp_rw.dmg
```
Continue by opening the dmg in Finder.  The `/Volumes/...` folder will depend on the dmg you're editing.
```
open temp_rw.dmg
open /Volumes/Jalview\ Installer
```

Then manually position/resize icons, extend size of Finder window, etc so that the Finder window looks how you want it.
You can use the

```
cp /Volumes/Jalview\ Installer/.DS_Store ./DS_Store_N

umount /Volumes/Jalview\ Installer
```

## Background image

See file `README-DMG_background_image_creation.md` on how to create the background image.

## Adding the background image

See https://www.ej-technologies.com/resources/install4j/help/doc/concepts/dmgStyling.html which describes how to add the background image to your DMG file
using macOS Finder.

Worth exploring further:
* https://github.com/create-dmg/create-dmg
* and the AppleScript on https://stackoverflow.com/questions/96882/how-do-i-create-a-nice-looking-dmg-for-mac-os-x-using-command-line-tools
to script precise placement of icons over background image.
