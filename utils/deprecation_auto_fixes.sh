#!/usr/bin/env bash

TYPES="Boolean|Character|Double|Float|Long|Integer" find src test -type f -name "*.java" -exec perl -p -i -e 's/\bnew\s+(java\.lang\.)?($ENV{TYPES})\(/${1}${2}.valueOf(/;' {} +

find src test -type f -name "*.java" -exec perl -p -i -e 's/(InputEvent|KeyEvent)\s*\.\s*([A-Z0-9]+)_MASK\b/${1}.${2}_DOWN_MASK/g;' {} +

find src test -type f -name "*.java" -exec perl -p -i -e 's/\b(e|evt)\.getModifiers\b/${1}.getModifiersEx/g;' {} +

find src test -type f -name "*.java" -exec perl -p -i -e 's/\.getMenuShortcutKeyMask\b/\.getMenuShortcutKeyMaskEx/g;' {} +

#
find src test -type f -name "*.java" -exec perl -p -i -e 'if ( s/^\s*import\s+java\.awt\.Event\s*;\s*$/import java.awt.event.InputEvent;/ ) { $event = 1 }; if ($event == 1) { s/\b(java\.awt\.)?Event\s*\.\s*([A-Z0-9]+)_MASK\b/InputEvent.${2}_DOWN_MASK/g; s/\b(java\.awt\.)?Event\s*\.\s*(MOUSE_MOVE)/MouseEvent.MOUSE_MOVED/g }' {} +
