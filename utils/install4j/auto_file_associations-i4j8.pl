#!/usr/bin/env perl

use strict;

my $i4jversion = 8;
if ($ARGV[0] eq "-v") {
  shift @ARGV;
  $i4jversion = shift @ARGV;
  die("-v i4jversion must be an integer [probably 7 or 8]") unless $i4jversion =~ m/^\d+$/;
}

my $fileformats = $ARGV[0];
$fileformats = "../../src/jalview/io/FileFormat.java" unless $fileformats;

# default mimetype will be text/x-$shortname
# TODO: find an actual extension for mat, see JAL-Xxxxx for outstanding issues too
# TODO: look up standard mime type used for BLASTfmt matrices, etc
my $mimetypes = {
  rnaml => "application/rnaml+xml",
  biojson => "application/x-jalview-biojson+json",
  jnet => "application/x-jalview-jnet+text",
  features => "application/x-jalview-features+text",
  scorematrix => "application/x-jalview-scorematrix+text",
  pdb => "chemical/x-pdb",
  mmcif => "chemical/x-cif",
  mmcif2 => "chemical/x-mmcif",
  jalview => "application/x-jalview+xml+zip",
  jvl => "application/x-jalview-jvl+text",
  annotations => "application/x-jalview-annotations+text",
};

my @dontaddshortname = qw(features json);
my @dontaddextension = qw(html xml json jar mfa fastq);
my $add_associations = {
  biojson => {shortname=>"biojson",name=>"BioJSON",extensions=>["biojson"]},
  gff2 => {shortname=>"gff2",name=>"Generic Features Format v2",extensions=>["gff2"]},
  gff3 => {shortname=>"gff3",name=>"Generic Features Format v3",extensions=>["gff3"]},
  features => {shortname=>"features",name=>"Jalview Features",extensions=>["features","jvfeatures"]},
  annotations => {shortname=>"annotations",name=>"Jalview Annotations",extensions=>["annotations","jvannotations"]},
  mmcif => {shortname=>"mmcif",name=>"CIF",extensions=>["cif"]},
  mmcif2 => {shortname=>"mmcif2",name=>"mmCIF",extensions=>["mcif","mmcif"]},
  jvl => {shortname=>"jvl",name=>"Jalview Launch",extensions=>["jvl"],iconfile=>"jvl_file"},
  jnet => {shortname=>"jnet",name=>"JnetFile",extensions=>["concise","jnet"]},
  scorematrix => {shortname=>"scorematrix",name=>"Substitution Matrix",extensions=>["mat"]},
};
my $add_extensions = {
  blc => ["blc"],
};
my @put_first = qw(jalview jvl);

my @non_primary = qw(mmcif mmcif2 pdb);

my $v = ($i4jversion >= 8)?$i4jversion:"";
my $i4jtemplatefile = "file_associations_template-install4j${v}.xml";
my $i4jtemplate;
my $mactemplatefile = "file_associations_template-Info_plist.xml";
my $mactemplate;

open(MT,"<$mactemplatefile") or die("Could not open '$mactemplatefile' for reading");
while(<MT>){
  $mactemplate .= $_;
}
close(MT);
open(IT,"<$i4jtemplatefile") or die("Could not open '$i4jtemplatefile' for reading");
while(<IT>){
  $i4jtemplate .= $_;
}
close(IT);
my $macauto;
my $i4jauto;

my $macautofile = $mactemplatefile;
$macautofile =~ s/template/auto$1/;

my $i4jautofile = $i4jtemplatefile;
$i4jautofile =~ s/template/auto$1/;

for my $key (sort keys %$add_associations) {
  my $a = $add_associations->{$key};
  warn("Known file association for $a->{shortname} (".join(",",@{$a->{extensions}}).")\n");
}

open(MA,">$macautofile") or die ("Could not open '$macautofile' for writing");
print MA "<key>CFBundleDocumentTypes</key>\n<array>\n\n";

open(IA,">$i4jautofile") or die ("Could not open '$i4jautofile' for writing");

open(IN, "<$fileformats") or die ("Could not open '$fileformats' for reading");
my $id = 10000;
my $file_associations = {};
while(my $line = <IN>) {
  $line =~ s/\s+/ /g;
  $line =~ s/(^ | $)//g;
  if ($line =~ m/^(\w+) ?\( ?"([^"]*)" ?, ?"([^"]*)" ?, ?(true|false) ?, ?(true|false) ?\)$/i) {
    my $shortname = lc($1);
    next if (grep($_ eq $shortname, @dontaddshortname));
    my $name = $2;
    my $extensions = $3;
    $extensions =~ s/\s+//g;
    my @possextensions = map(lc($_),split(m/,/,$extensions));
    my @extensions;
    my $addext = $add_extensions->{$shortname};
    if (ref($addext) eq "ARRAY") {
      push(@possextensions, @$addext);
    }
    for my $possext (@possextensions) {
      next if grep($_ eq $possext, @extensions);
      next if grep($_ eq $possext, @dontaddextension);
      push(@extensions,$possext);
    }
    next unless scalar(@extensions);
    $file_associations->{$shortname} = {
      shortname => $shortname,
      name => $name,
      extensions => \@extensions
    };
    warn("Reading file association for $shortname (".join(",",@extensions).")\n");
  }
}
close(IN);

my %all_associations = (%$file_associations, %$add_associations);

my @ordered = (@put_first, @non_primary);
for my $key (sort keys %all_associations) {
  next if grep($_ eq $key, @ordered);
  push(@ordered, $key);
}
my $num = $#ordered + 1;

warn("--\n");

my $i4jcount = 0;
for my $shortname (@ordered) {
  my $a = $all_associations{$shortname};
  next if (ref($a) ne "HASH");

  my $name = $a->{name};
  my $extensions = $a->{extensions};
  my $mimetype = $mimetypes->{$shortname};
  $mimetype = "application/x-$shortname+txt" unless $mimetype;

  my $iconfile = $a->{iconfile};
  $iconfile = "Jalview-File" unless $iconfile;

  my $primary = (! grep($_ eq $shortname, @non_primary));
  my $primarystring = $primary?"true":"false";
  my $role = $primary?"Editor":"Viewer";

  my @extensions = @$extensions;

  my $xname = xml_escape($name);
  my $xmimetype = xml_escape($mimetype);
  my $xshortname = xml_escape($shortname);
  my $xiconfile = xml_escape($iconfile);
  my $xrole = xml_escape($role);
  my $xROLE = xml_escape(uc($role));
  my $xprimarystring = xml_escape($primarystring);

  my $macentry = $mactemplate;
  $macentry =~ s/\$\$NAME\$\$/$xname/g;
  $macentry =~ s/\$\$SHORTNAME\$\$/$xshortname/g;
  $macentry =~ s/\$\$MIMETYPE\$\$/$xmimetype/g;
  $macentry =~ s/\$\$ICONFILE\$\$/$xiconfile/g;
  $macentry =~ s/\$\$ROLE\$\$/$xrole/g;
  $macentry =~ s/\$\$PRIMARY\$\$/$xprimarystring/g;
  while ($macentry =~ m/\$\$([^\$]*)EXTENSIONS([^\$]*)\$\$/) {
    my $pre = $1;
    my $post = $2;
    my $macextensions;
    for my $ext (@extensions) {
      my $xext = xml_escape($ext);
      $macextensions .= $pre.$xext.$post;
    }
    $macentry =~ s/\$\$${pre}EXTENSIONS${post}\$\$/$macextensions/g;
  }
  print MA $macentry;

  my $i4jentry = $i4jtemplate;
  $i4jentry =~ s/\$\$NAME\$\$/$xname/g;
  $i4jentry =~ s/\$\$SHORTNAME\$\$/$xshortname/g;
  $i4jentry =~ s/\$\$MIMETYPE\$\$/$xmimetype/g;
  $i4jentry =~ s/\$\$ICONFILE\$\$/$xiconfile/g;
  $i4jentry =~ s/\$\$PRIMARY\$\$/$xprimarystring/g;
  $i4jentry =~ s/\$\$MACASSOCIATIONROLE\$\$/$xROLE/g;

  my $ext = join(",",sort(@extensions));
  my $xdisplayext = xml_escape(join(", ", map(".$_",sort(@extensions))));
  my $progresspercent = int(($i4jcount/$num)*100);
  $progresspercent = 100 if $progresspercent > 100;
  $i4jcount++;
  my $xext = xml_escape($ext);
  my $addunixextension = "true";

  $i4jentry =~ s/\$\$ADDUNIXEXTENSION\$\$/$addunixextension/g;
  $i4jentry =~ s/\$\$EXTENSION\$\$/$xext/g;
  $i4jentry =~ s/\$\$DISPLAYEXTENSION\$\$/$xdisplayext/g;
  $i4jentry =~ s/\$\$PROGRESSPERCENT\$\$/$progresspercent/g;
  $i4jentry =~ s/\$\$ID\$\$/$id/g;
  $id++;
  $i4jentry =~ s/\$\$ID1\$\$/$id/g;
  $id++;
  $i4jentry =~ s/\$\$ID2\$\$/$id/g;
  $id++;

  print IA $i4jentry;

  delete $all_associations{$shortname};
  warn("Writing entry for $name (".join(",",@$extensions).": $mimetype)\n");
}

close(IA);
print MA "</array>\n";
close(MA);

sub xml_escape {
  my $x = shift;
  # stolen from Pod::Simple::XMLOutStream in base distro
  $x =~ s/([^-\n\t !\#\$\%\(\)\*\+,\.\~\/\:\;=\?\@\[\\\]\^_\`\{\|\}a-zA-Z0-9])/'&#'.(ord($1)).';'/eg;
  return $x;
}  
