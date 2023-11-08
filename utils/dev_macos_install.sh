#!/usr/bin/env bash

# perform a dev build and install on local macOS machine
INSTALLERVOL="Jalview Non-Release Installer"
APP="Jalview Local.app"

APPLICATIONS=/Applications
CHANNEL=NOCHANNEL
DMG=build/install4j/11/Jalview_Local-TEST-macos-java_11.dmg


if [ x$1 != "xnogradle" ]; then
  gradle installers -PCHANNEL=LOCAL -Pinstall4j_media_types=macosArchive
else
  echo "Not running gradle installers"
fi

if [ $? = 0 ]; then
  umount "/Volumes/$INSTALLERVOL"
  if [ -e "$DMG" ]; then
    open $DMG
  else
    echo "No DMG file '$DMG'" 1>&2
    exit 1
  fi
  echo "Mounting '$DMG' at /Volumes"
  N=0
  while [ \! -e "/Volumes/$INSTALLERVOL/$APP" ]; do
    if [ $(( N%1000 )) = 0 ]; then
      echo -n "."
    fi
    N=$(( N+1 ))
  done
  echo ""
fi
if [ -e "/Volumes/$INSTALLERVOL/$APP" ]; then
  echo "Removing '$APPLICATIONS/$APP'"
  /bin/rm -r "$APPLICATIONS/$APP"
  echo "Syncing '/Volumes/$INSTALLERVOL/$APP' to '$APPLICATIONS/'"
  rsync -avh "/Volumes/$INSTALLERVOL/$APP" "$APPLICATIONS/"
  echo "Unmounting '/Volumes/$INSTALLERVOL'"
  umount "/Volumes/$INSTALLERVOL"
fi
