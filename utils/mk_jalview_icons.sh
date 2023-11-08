#!/usr/bin/env bash

FILENAME512=$1
BASENAME=${FILENAME512%-512.png}

usage () {
  echo "$(basename "$0") <LOGO_NAME>-512.png"
}

[ "$1" = "-h" ] && usage && exit 0
[ -r "$FILENAME512" ] || ( usage && echo "'${FILENAME512}' must exist and be readable" && exit 1 )
[ "$BASENAME" = "$FILENAME512" ] && usage && echo "Must have '-512.png' at end of filename" && exit 2

set -e

SIZES="16 32 38 48 64 128 256"
declare -a ICOFILES=()
declare -a ICNSFILES=()
for n in $SIZES
do
  NEWFILE="${BASENAME}-${n}.png"
  [ -e "{$NEWFILE}" ] && continue
  convert -geometry "${n}x${n}" "${BASENAME}-512.png" "${NEWFILE}"
  [ "${n}" != 38 ] && ICOFILES=( "${ICOFILES[@]}" "${NEWFILE}" )
  [ "${n}" != 64 ] && [ "${n}" != 38 ] && ICNSFILES=( "${ICNSFILES[@]}" "${NEWFILE}" )
done

# make the .ico
ICOFILE="${BASENAME}.ico"
echo "Creating ${ICOFILE}"
convert "${ICOFILES[@]}" "${ICOFILE}"

# make the .icns
ICNSFILE="${BASENAME}.icns"
echo "Creating ${ICNSFILE}"
png2icns "${ICNSFILE}" "${ICNSFILES[@]}"

# make the rotatable icon
ROTATABLEFILE="rotatable_${BASENAME}-38.png"
convert "${BASENAME}-38.png" -gravity center -background white -extent 60x60 "${ROTATABLEFILE}"

exit 0
