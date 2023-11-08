#!/usr/bin/env bash

# see https://api.adoptopenjdk.net/swagger-ui/#/Binary/get_v3_binary_latest__feature_version___release_type___os___arch___image_type___jvm_impl___heap_size___vendor_


### bs 2020-01-22
### This is a script to download and update the JREs used in the windows, mac (and maybe linux) installations, and update channel
### It creates a structure with
### ./jre-VERSION-OS-ARCH/jre/...
### as used by getdown
### and
### ./tgz/jre-VERSION-OS-ARCH.tgz
### which is an archive of the _contents_ of ./jre-VERSION-OS-ARCH/jre/ and used by install4j for the installer
### bs 2021-10-26
### Edited to use adoptium domain to gain access to Java 17 (LTS) versions.

BASE=https://api.adoptium.net/v3/binary/latest
ZULU_BASE=https://cdn.azul.com/zulu/bin
RELEASE_TYPE=ga
JVM_IMPL=hotspot
HEAP_SIZE=normal
VENDOR=eclipse
IMAGE_TYPE=jdk
TAR=tar
ZIP=zip
UNZIP=unzip

STRIP_MAC_APP_BUNDLING=false
# archives not needed for JDKs
CREATE_ARCHIVES=""
# need zip with top-level jre dir for getdown updates. need tgz without top-level jre dir for install4j bundling

RM=/bin/rm

# unzip-strip from https://superuser.com/questions/518347/equivalent-to-tars-strip-components-1-in-unzip
unzip-strip() {
  local zip=$1
  local dest=${2:-.}
  local temp=$(mktemp -d) && $UNZIP -qq -d "$temp" "$zip" && mkdir -p "$dest" &&
  shopt -s dotglob && local f=("$temp"/*) &&
  if (( ${#f[@]} == 1 )) && [[ -d "${f[0]}" ]] ; then
    mv "$temp"/*/* "$dest"
  else
    mv "$temp"/* "$dest"
  fi && rmdir "$temp"/* "$temp"
}

dl_zulu() {
  local OS="$1"
  local ARCH="$2"
  local VERSION="$3"
  local TARFILE="$4"
  declare -A osmap
  osmap[mac]=macosx
  osmap[windows]=win
  osmap[linux]=linux
  ZOS="${osmap[$OS]}"
  echo "- Looking for download from Azul"
  LATEST_DL_URL_FILE=$(wget -q -O - "${ZULU_BASE}/" | perl -n -e 'm/<a\b[^>]*href="(([^"]*\/)?zulu[^"]*-ca-'"${IMAGE_TYPE}""${VERSION}"'\.[^"]*-'"${ZOS}"'_'"${ARCH}"'.tar.gz)"[^"]*>/ && print "$1\n";' | tail -1)
  local URL="${ZULU_BASE}/${LATEST_DL_URL_FILE}"
  if [ -z "${LATEST_DL_URL_FILE}" ]; then
    echo "- No ${IMAGE_TYPE}-${FEATURE_VERSION} download for ${OS}-${ARCH} '${URL}' found at Azul"
    return 1
  fi
  echo "- Found at Azul. Downloading '${URL}'"
  wget -q -O "${TARFILE}" "${URL}" "${TARFILE}"
  echo RETURN=$?
  if [ "$?" != 0 ]; then
    echo "- Download from Azul failed"
    return 1
  fi
  return 0
}

declare -A DOWNLOAD_SUMMARY

for FEATURE_VERSION in 8 11 17; do
  for OS_ARCH in mac:x64 mac:aarch64 windows:x64 linux:x64 linux:arm linux:aarch64; do
    OS=${OS_ARCH%:*}
    ARCH=${OS_ARCH#*:}
    NAME="${IMAGE_TYPE}-${FEATURE_VERSION}-${OS}-${ARCH}"
    TARFILE="${NAME}.tgz"
    DOWNLOAD_SUMMARY["${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}"]="None"
    STRIP_COMPONENTS=1
    MAC_STRIP_COMPONENTS=3
    echo "* Downloading ${TARFILE}"
    URL="${BASE}/${FEATURE_VERSION}/${RELEASE_TYPE}/${OS}/${ARCH}/${IMAGE_TYPE}/${JVM_IMPL}/${HEAP_SIZE}/${VENDOR}"
    wget -q -O "${TARFILE}" "${URL}"
    if [ "$?" != 0 ]; then
      echo "- No ${IMAGE_TYPE}-${FEATURE_VERSION} download for ${OS}-${ARCH} '${URL}' at Adoptium"
      $RM -f "${TARFILE}"

      # Try Azul Zulu (not an API, a bit messier, but has Java 8 JRE for mac:aarch64
      dl_zulu "${OS}" "${ARCH}" "${FEATURE_VERSION}" "${TARFILE}"

      if [ "$?" != 0 ]; then
        DOWNLOAD_SUMMARY["${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}"]="None"
        continue;
      fi
      STRIP_COMPONENTS=2
      MAC_STRIP_COMPONENTS=4
      DOWNLOAD_SUMMARY["${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}"]="Azul"
      echo "Set ${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}=Azul"
    else
      DOWNLOAD_SUMMARY["${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}"]="Adoptium"
      echo "Set ${OS_ARCH}-${IMAGE_TYPE}-${FEATURE_VERSION}=Adoptium"
    fi
    echo "Unpacking ${TARFILE}"
    JREDIR="${NAME}/${IMAGE_TYPE}"
    [ x$NAME != x -a -e "${JREDIR}" ] && $RM -rf "${JREDIR}"
    mkdir -p "${JREDIR}"
    if [ x$OS = xwindows ]; then
      echo "using unzip"
      unzip-strip "${TARFILE}" "${JREDIR}"
      RET=$?
    else
      echo "using tar"
      if [ x$OS = xmac -a x$STRIP_MAC_APP_BUNDLING = xtrue ]; then
        echo "Running $TAR --strip-components=\"${MAC_STRIP_COMPONENTS}\" -C \"${JREDIR}\" -zxf \"${TARFILE}\" \"*/Contents/Home\""
        $TAR --strip-components="${MAC_STRIP_COMPONENTS}" -C "${JREDIR}" -zxf "${TARFILE}" "*/Contents/Home"
        RET=$?
      else
        $TAR --strip-components="${STRIP_COMPONENTS}" -C "${JREDIR}" -zxf "${TARFILE}"
        RET=$?
      fi
    fi
    if [ "$RET" != 0 ]; then
      echo "Error unpacking ${TARFILE}"
      exit 1
    fi
    $RM "${TARFILE}"
    if [ \! -z "$CREATE_ARCHIVES" ]; then
      for CREATEARCHIVE in ${CREATE_ARCHIVES}; do
        ARCHIVEDIR=$CREATEARCHIVE
        case $CREATEARCHIVE in
          zip)
            EXT=${CREATEARCHIVE}
            echo "Creating ${NAME}.${EXT} for getdown updates"
            [ \! -d ${ARCHIVEDIR} ] && mkdir -p "${ARCHIVEDIR}"
            ABSARCHIVEDIR="${PWD}/$ARCHIVEDIR"
            ZIPFILE="${ABSARCHIVEDIR}/${NAME}.${CREATEARCHIVE}"
            [ -e "${ZIPFILE}" ] && $RM "${ZIPFILE}"
            cd ${NAME}
            $ZIP -X -r "${ZIPFILE}" "${IMAGE_TYPE}"
            cd -
            ;;
          tgz)
            EXT=tar.gz
            echo "Creating ${NAME}.${EXT} for install4j bundling"
            [ \! -d ${ARCHIVEDIR} ] && mkdir -p "${ARCHIVEDIR}"
            $TAR -C "${JREDIR}" -zcf "${ARCHIVEDIR}/${NAME}.${EXT}" .
            # make symbolic link with _ instead of - for install4j9
            NEWNAME=${NAME//-/_}
            echo "Linking from ${NEWNAME}.${EXT} for install4j9"
            [ -e "${ARCHIVEDIR}/${NEWNAME}.${EXT}" ] && $RM "${ARCHIVEDIR}/${NEWNAME}.${EXT}"
            ln -s "${NAME}.${EXT}" "${ARCHIVEDIR}/${NEWNAME}.${EXT}"
            ;;
          *)
            echo "Archiving as '${CREATEARCHIVE}' file not supported"
            ;;
        esac
      done
    fi
  done
done

echo ""
echo "Download Summary"
for OA in "${!DOWNLOAD_SUMMARY[@]}"; do
  echo "$OA: ${DOWNLOAD_SUMMARY[$OA]}"
done

