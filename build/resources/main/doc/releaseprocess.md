# Making a Jalview release

## Objectives

1 Update the release getdown channel
2 Update single executable jar
3 Update installer
4 Update web site with release notes
5 Update Conda recipe
6 Update Homebrew
7 Update JalviewJS


## tl;dr

```OSX_SIGNING=yes KEYPASS=MYSECRETKEY gradle -POSX_KEYPASS=true -POSX_KEYSTORE=/Users/jbb/buildtools/private/sourceofpain.p12 -PJSIGN_SH="/Users/jbb/buildtools/jsign.sh" -Pinstall4j_verbose=true -PJAVA_VERSION=1.8 -PCHANNEL=RELEASE -PVERSION=W.X.Y.Z getdown installers shadowJar```
```codesign -s "MY Apple Dev Name" build/install4j/1.8/Jalview-DEVELOPMENT-macos-java_8.dmg```

