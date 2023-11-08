The src2 directory is on the class path, but it is generally empty.
It is for transient transpiling of external JAR file contents only. 


After transpiling:

1) move src code to srcjar
2) edit README_SWINGJS.txt in main directory of the code
3) add a zip-up target in build-libjs.xml (from site to libjs directory)
4) add an unzip target in build-site.xml (from libjs to site directory)
5) run build-libjs.xml
6) run build-site.xml

BH 2018.07.03/2019.01.30