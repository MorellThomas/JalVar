#!/usr/bin/env pwsh

# save args and first parameter
$myArgs = $args.Clone()
$myArg1 = $args[0]

# setup for powershell version < 6.0
[bool] $myIsWindows = 0
[bool] $myIsMacOS = 0
if ( $IsWindows -eq $null ) {
  # for powershell version < 6.0 let's assume Windows
  $myIsWindows = 1
  $myIsMacOS = 0
} else {
  $myIsWindows = $IsWindows
  $myIsMacOS = $IsMacOS
}

# parent dir of this actual script (which should be the getdown appdir/bin). Follow all symlinks.  Like GNU readlink -f
function Readlink-f {
  Param($Link)
  $Return = $null
  $c = 0
  $max = 100 # just in case we end up in a loop
  [bool] $found = 0
  $file = Get-Item -Path $Link
  $prevfile = $null
  While ( $c -lt $max -and "${file}" -ne "${prevfile}" -and -not $found ) {
    $prevfile = $file
    [string] $target = ( $file ).Target
    If ( $target -eq $null -or ( $file ).LinkType -ne "SymbolicLink" ) {
      $Return = $file
      $found = 1
    } Else {
      If ( $( Split-Path -Path $target -IsAbsolute ) ) {
        $file = Get-Item -Path $target
      } Else {
# symbolic link is relative: combine previous link parent dir with the link target and resolve
        $file = Get-Item -Path ( Join-Path -Path ( Split-Path -Path $prevfile -Parent ) -ChildPath $target -Resolve )
      }
    }
    $c++
  }
  if ( -not $found ) {
    throw "Could not determine path to actual file $( Split-Path -Path $Link -Leaf )"
  }
  $Return
}

# Avert problem with unix version of powershell and tell user the reason (Windows must always have .ps1 extension)
if ( $MyInvocation.MyCommand.Path -eq $null ) {
  throw "Script or link to script must have extension .ps1"
}


$CMDPATH = ( Get-Item $MyInvocation.MyCommand.Path )
$SCRIPTPATH = Readlink-f -Link $CMDPATH
$DIR = Split-Path -Path $SCRIPTPATH -Parent

$APPDIR = If ( ( Split-Path -Path $DIR -Leaf ) -eq "bin" ) { Split-Path -Path $DIR -Parent } Else { $DIR }
$JAVAEXE = If ( $myIsWindows ) { "java.exe" } Else { "java" }
$JAVA = Join-Path -Path $APPDIR -ChildPath ( "jre/" + $( If ( $myIsMacOS ) { "Contents/Home/" } Else { "" } ) + "bin/${JAVAEXE}" )
$GETDOWNTXT = Join-Path -Path $APPDIR -ChildPath "getdown.txt"

# look for getdown.txt -- needed to create classpath
if ( -not ( Test-Path -Path "${GETDOWNTXT}" ) ) {
  throw "Cannot find ${GETDOWNTXT}"
}

# look for bundled JRE. Might not be there if unix installer used in which case just invoke "java"
if ( -not ( Test-Path -Path "${JAVA}" ) ) {
  Write-Host "Cannot find bundled ${JAVAEXE}. Using system ${JAVAEXE} and hoping for the best!"
  $JAVA = $JAVAEXE
}

$CLASSPATH = ( Select-String -Path "${GETDOWNTXT}" -AllMatches -Pattern "code\s*=\s*(.*)$" | foreach { Join-Path -Path $APPDIR -ChildPath $($_.Matches.Groups[1].Value ) } ) -join $( If ( $myIsWindows ) { ";" } Else { ":" } )

# get console width
$CONSOLEWIDTH = $Host.UI.RawUI.WindowSize.Width

# quote the args and the command (in case of spaces) with escape chars (`) and precede with & to indicate command not string
if ( $myArgs.count -eq 0 ) {
  Invoke-Expression -Command "& `"${JAVA}`" `"-DCONSOLEWIDTH=${CONSOLEWIDTH}`" `"-Dgetdownappdir=${APPDIR}`" -cp `"${CLASSPATH}`" jalview.bin.Launcher"
} else {
  $myArgsString = '"' + $($myArgs -join '" "') + '"'
  Invoke-Expression -Command "& `"${JAVA}`" `"-DCONSOLEWIDTH=${CONSOLEWIDTH}`" `"-Dgetdownappdir=${APPDIR}`" -cp `"${CLASSPATH}`" jalview.bin.Launcher ${myArgsString}"
}

