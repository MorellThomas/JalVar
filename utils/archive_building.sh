#!/usr/bin/env bash

J8HOME=/path/to/java8/jdk
J11HOME=/path/to/java11/jdk
OLDBUILDDIR=/path/to/old/build/root
JALVIEWDIR=/path/to/recent/jalview
LOCALARCHIVEDIR=/path/to/where/to/store/archive/locally
WEBSITEDOCROOTMOUNT=/path/to/mounted/website/docroot


cd OLDBUILDDIR
mkdir tar
cd tar
wget http://www.jalview.org/source/jalview_2_08b.tar.gz  http://www.jalview.org/source/jalview_2_2_1.tar.gz  http://www.jalview.org/source/jalview_2_3_buildfix.tar.gz http://www.jalview.org/source/jalview_2_4_0b2.tar.gz http://www.jalview.org/source/jalview_2_5_1.tar.gz http://www.jalview.org/source/jalview_2_6_1.tar.gz http://www.jalview.org/source/jalview_2_7.tar.gz http://www.jalview.org/source/jalview_2_8_2b1.tar.gz http://www.jalview.org/source/jalview_2_9_0b1.tar.gz http://www.jalview.org/source/jalview_2_10_5.tar.gz
cd -

export JAVA_HOME=J8HOME
export PATH=$JAVA_HOME/bin:$PATH

for x in tar/jalview_*.tar.gz
do
	V=${x#*jalview_}
	V=${V%.tar.gz}
	echo $V
	tar --one-top-level -xvf $x
	cd jalview_$V/jalview
	ant makedist -DJALVIEW_VERSION="$V"
	cd -
done

export JAVA_HOME=J11HOME
export PATH=$JAVA_HOME/bin:$PATH

cd $JALVIEWDIR
for x in $OLDBUILDDIR/jalview_*/jalview
do
	V=${x##*jalview_}
	V=${V%/jalview}
	echo $V
	[ -e getdown/website ] && /bin/rm -r getdown/website
	[ -e getdown/files ] && /bin/rm -r getdown/website
	gradle getdown -PCHANNEL=ARCHIVE -PJALVIEW_VERSION="$V" -PJAVA_VERSION=1.8 -PARCHIVEDIR=$x -Pgetdown_rsync_dest=$LOCALARCHIVEDIR -PRUNRSYNC=true
done


cd $LOCALARCHIVEDIR
rsync -avh --delete $LOCALARCHIVEDIR/archive/ $WEBSITEDOCROOTMOUNT/getdown/archive/

