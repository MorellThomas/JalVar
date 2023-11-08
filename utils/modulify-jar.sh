#!/usr/bin/env bash

CMD=$(basename $0)

usage() {
  echo "Usage: $CMD /path/to/jarfile" >&2
}

usagexit() {
  usage
  exit 1
}

error() {
  echo $1 >&2
  usagexit
}

JARFILE=$1
[ -z $JARFILE ] && usagexit
[ -f $JARFILE ] || error "No file $JARFILE"
[ -r $JARFILE ] || error "$JARFILE not readable"

EXT=.jar
SUFFIX=-MODULE
FILENAME=$(basename $JARFILE)
BASE=$(basename -s $EXT $JARFILE)
DIR=$(dirname $JARFILE)

# set absolute path to $JARFILE if not specified
[ x${DIR#/} = x$DIR ] && DIR=$(cd "$DIR" && pwd)

ABSJARFILE=$DIR/$FILENAME

TMPDIR=/tmp/$USER-$CMD-$BASE-$$

[ x$FILENAME = x$BASE ] && error "Should be $EXT file"

mkdir -p $TMPDIR/jar || error "Could not create tmp dir $TMPDIR/jar"
mkdir -p $TMPDIR/info || error "Could not create tmp dir $TMPDIR/info"
cd $TMPDIR/jar
jar -xvf $ABSJARFILE > /dev/null
jdeps --module-path="$DIR" --generate-module-info $TMPDIR/info $ABSJARFILE
# next line assuming only one module-info.java file created, I think this is always true...? It'll just use the last one if not.
find $TMPDIR/info -name "module-info.java" -exec /bin/mv {} . \;
[ -e ./module-info.java ] || error "No module-info.java file found in $TMPDIR/info"
javac -d $TMPDIR/jar ./module-info.java
jar -cvf $DIR/${BASE}${SUFFIX}${EXT} -C $TMPDIR/jar . > /dev/null
rm -rf $TMPDIR


