#/usr/bin/env bash

# Be in the jalview top level dir.
# lib -- contains usual jalview jar files
#        make sure it contains the extra jar files needed in classpath for Java 11
# j11lib -- contains java11 style modules to be put into the JRE (not needed at runtime)
#
# j11jre -- dir containing JRE environments for jalview
#
# creates file modules.new which is comma-separated list of modules needed, can be used like this in jlink argument
# and a java 11 JRE in j11jre/jre-new

( for x in lib/*.jar j11lib/*.jar dist/jalview.jar; do echo $x >&2; jdeps --list-deps --module-path j11lib $x | grep -v Warning: | grep -v "JDK removed" | sed -e 's/^ *//;s/\/.*//;s/$/,/;'; done ) | sort -u | perl -p -e 'chomp;' | perl -p -e 's/,$//;chomp;' > modules.new

if [ x$JAVA_HOME != x ]; then
  jlink --no-header-files --no-man-pages --strip-debug --module-path "$JAVA_HOME/jmods:j11lib" --add-modules `cat modules.new` --compress=2 --output j11jre/jre-new
else
  jlink --no-header-files --no-man-pages --strip-debug --module-path "j11lib" --add-modules `cat modules.new` --compress=2 --output j11jre/jre-new
fi


# or if you're in a hurry for a one-liner...
#jlink --no-header-files --no-man-pages --strip-debug --module-path "$JAVA_HOME/jmods:j11lib" --add-modules ` ( for x in lib/*.jar j11lib/*.jar dist/jalview.jar; do echo $x >&2; jdeps --list-deps --module-path j11mod $x | grep -v "Warning:" | grep -v "JDK removed" | sed -e 's/^ *//;s/\/.*//;s/$/,/;'; done ) | sort -u | perl -p -e 'chomp;' | perl -p -e 's/,$//;chomp;' ` --compress=2 --output j11jre/jre-new
