# Links with `DS_Store`
The DMG background image should be 500x461 pixels with DPI set at 72.  The spacing for the Jalview application icon and the symbolic link to the Applications folder tie in with the information in the `DS_Store` file, so both files should be changed if you want to move the icons.  Instructions on how to create/adapt the `DS_Store` file can be found in file `README-DMG_creation.md`.

# Creation of `jalview_dmg_background.png`
The most recent `jalview_dmg_background.png` (with blurry faded tiled Jalview logos, should be released with Jalview 2.11.1) is created from the `jalview_dmg_background.svg` file which can be edited in Inkscape.  It is exported as `jalview_dmg_background.png` at 500x461 pixels but Inkscape currently makes it difficult to choose an arbitrary dpi in the PNG metadata, with 96 DPI the default.

The exported PNG can have the 96 DPI metadata changed using GIMP or ImageMagick.  Here is how to do it on the command line with ImageMagick:
```
convert -units PixelsPerInch jalview_dmg_background.png -density 72 jalview_dmg_background-72dpi.png
```
The final image should be saved in `utils/channel/<CHANNEL>/images/` as `jalview_dmg_background-72dpi.png` (which the above `convert` command will do) as this is the filename set as `install4j_dmg_background` in `utils/channel/<CHANNEL>/channel_gradle.properties` file to include in the final DMG.

# Alternative backgrounds

There are now specific background images with associated DS_Stores for Release, Develop and Test.  Anything else should use the non-release DS_Store and background image.

## Non-release background `jalview_dmg_background-NON-RELEASE.png`
This is created in much the same way with `jalview_dmg_background-NON-RELEASE.svg` and is used as a more generic background for non-release versions where the "Jalview Build" (or "Jalview Local" etc) application icon is placed in the first available slot in the Finder window.  We cannot position these unless we create a DS_Store specifically for each possible application name.  The Applications folder link can be (and is) positioned as this names is fixed.
