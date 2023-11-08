#!/usr/bin/env bash

find Uninstall\ Old\ Jalview.app | perl -p -e 'chomp;$_=qq(            <file name=").$_.qq(" file="./).$_.qq(" />\n);' > uninstall_old_jalview_files.xml

# makes the file used to replace the line
# <file name="UNINSTALL_OLD_JALVIEW_APP_REPLACED_IN_GRADLE" file="./Uninstall Old Jalview.app" />
# (replacement happens in gradle)
