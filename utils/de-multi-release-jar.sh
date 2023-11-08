#!/usr/bin/env bash

CMD=$(basename $0)

usage() {
  echo "Usage: $CMD [-v N] /path/to/jarfile" >&2
  echo "             -v N   build jar with alternative classes for version N of java (optional, defaults to base jar packages which is usually 8)" >&2
}

usagexit() {
  usage
  exit 1
}

error() {
  echo $1 >&2
  usagexit
}

VERSION=""
while getopts v: opt
do
  case "${opt}"
    in
    v) VERSION=${OPTARG};;
  esac
done
shift $((OPTIND-1))

JARFILE=$1
[ -z $JARFILE ] && usagexit
[ -f $JARFILE ] || error "No file $JARFILE"
[ -r $JARFILE ] || error "$JARFILE not readable"

EXT=.jar
SUFFIX=-SINGLE_RELEASE
FILENAME=$(basename $JARFILE)
BASE=$(basename -s $EXT $JARFILE)
DIR=$(dirname $JARFILE)

# set absolute path to $JARFILE if not specified
[ x${DIR#/} = x$DIR ] && DIR=$(cd "$DIR" && pwd)

TMPDIR=/tmp/$USER-$CMD-$BASE-$$

[ x$FILENAME = x$BASE ] && error "Should be $EXT file"

mkdir -p $TMPDIR || error "Could not create tmp dir $TMPDIR"
cd $TMPDIR
jar -xvf $JARFILE > /dev/null
VDIR=$TMPDIR/META-INF/versions

[ -d $VDIR ] || error "$JARFILE doesn't look like a multi-release jar file"

if [ -z $VERSION ]; then
  # no version set... nothing to copy
  echo ""
elif [ -d $VDIR/$VERSION ]; then
  # this version has alternative classes for the version asked for, copy them into the base jar
  tar -cf - -C $VDIR/$VERSION . | tar -xf - -C $TMPDIR
else
  echo "No specific classes for version $VERSION" >&2
  echo "Available alternative versions are" >&2
  cd $VDIR
  ls >&2
fi

# remove the alternative versions
/bin/rm -r $VDIR

# alter the manifest. note sed on macos is a bit weird
if [ `uname -s` = "Darwin" ]; then
  sed -E -i '' 's/^([Mm]ulti-[Rr]elease):).*/\1 false/' $TMPDIR/META-INF/MANIFEST.MF
else
  sed -E -i 's/^([Mm]ulti-[Rr]elease):).*/\1 false/' $TMPDIR/META-INF/MANIFEST.MF
fi

jar -cvf $DIR/${BASE}${SUFFIX}${EXT} -C $TMPDIR . > /dev/null
rm -rf $TMPDIR

