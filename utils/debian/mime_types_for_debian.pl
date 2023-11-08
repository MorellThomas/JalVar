#!/usr/bin/env perl

use strict;

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
  #jvl => "application/x-jalview-jvl+text",
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
  #jvl => {shortname=>"jvl",name=>"Jalview Launch",extensions=>["jvl"],iconfile=>"jalview-launch"},
  jnet => {shortname=>"jnet",name=>"JnetFile",extensions=>["concise","jnet"]},
  scorematrix => {shortname=>"scorematrix",name=>"Substitution Matrix",extensions=>["mat"]},
};
my $add_extensions = {
  blc => ["blc"],
};
my @put_first = qw(jalview jvl);

my @non_primary = qw(mmcif mmcif2 pdb);

my $mailcaptemplatefile = "file_associations_template-mailcap.txt";
my $mailcaptemplate;
my $sharedmimeinfotemplatefile = "file_associations_template-shared-mime-info.xml";
my $sharedmimeinfotemplate;

open(SMT,"<$sharedmimeinfotemplatefile") or die("Could not open '$sharedmimeinfotemplatefile' for reading");
while(<SMT>){
  $sharedmimeinfotemplate .= $_;
}
close(SMT);
open(MCT,"<$mailcaptemplatefile") or die("Could not open '$mailcaptemplatefile' for reading");
while(<MCT>){
  $mailcaptemplate .= $_;
}
close(MCT);
my $sharedmimeinfoauto;
my $mailcapauto;

# this file should go in /usr/share/mime/packages
my $sharedmimeinfoautofile = "debian/jalview-mime.xml";

# this file should go in /usr/lib/mime/packages
my $mailcapautofile = "debian/jalview-mailcap";

# this should be part of the jalview.desktop file that goes in /usr/shares/applications
my $desktopfile = "debian/jalview.desktop";

my $MimeType = "";

for my $key (sort keys %$add_associations) {
  my $a = $add_associations->{$key};
  warn("Known file association for $a->{shortname} (".join(",",@{$a->{extensions}}).")\n");
}

open(SMI,">$sharedmimeinfoautofile") or die ("Could not open '$sharedmimeinfoautofile' for writing");

open(MCA,">$mailcapautofile") or die ("Could not open '$mailcapautofile' for writing");

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
print SMI qq(<?xml version="1.0" encoding="UTF-8"?>\n<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">\n\n);

my $mailcapcount = 0;
for my $shortname (@ordered) {
  my $a = $all_associations{$shortname};
  next if (ref($a) ne "HASH");

  my $name = $a->{name};
  my $extensions = $a->{extensions};
  my $mimetype = $mimetypes->{$shortname};
  $mimetype = "application/x-$shortname+txt" unless $mimetype;

  $MimeType .= $MimeType?";":"";
  $MimeType .= $mimetype;

  my $iconfile = $a->{iconfile};
  $iconfile = "x-jalview-file" unless $iconfile;

  my $primary = (! grep($_ eq $shortname, @non_primary));
  my $primarystring = $primary?"true":"false";
  my $role = $primary?"Editor":"Viewer";
  my $priority = $primary?9:4;
  $priority = 10 if $mimetype =~ m/\bjalview\b/;

  my @extensions = @$extensions;
  my $extension0 = $extensions[0];

  my $xname = xml_escape($name);
  my $xmimetype = xml_escape($mimetype);
  my $xshortname = xml_escape($shortname);
  my $xiconfile = xml_escape($iconfile);
  my $xrole = xml_escape($role);
  my $xROLE = xml_escape(uc($role));
  my $xprimarystring = xml_escape($primarystring);

  my $sharedmimeinfoentry = $sharedmimeinfotemplate;
  $sharedmimeinfoentry =~ s/\$\$NAME\$\$/$xname/g;
  $sharedmimeinfoentry =~ s/\$\$SHORTNAME\$\$/$xshortname/g;
  $sharedmimeinfoentry =~ s/\$\$MIMETYPE\$\$/$xmimetype/g;
  $sharedmimeinfoentry =~ s/\$\$ICONFILE\$\$/$xiconfile/g;
  $sharedmimeinfoentry =~ s/\$\$ROLE\$\$/$xrole/g;
  $sharedmimeinfoentry =~ s/\$\$PRIMARY\$\$/$xprimarystring/g;
  $sharedmimeinfoentry =~ s/\$\$PRIORITY\$\$/$priority/g;
  while ($sharedmimeinfoentry =~ m/\$\$([^\$]*)EXTENSIONS([^\$]*)\$\$/) {
    my $pre = $1;
    my $post = $2;
    my $sharedmimeinfoextensions;
    for my $ext (@extensions) {
      my $xext = xml_escape($ext);
      $sharedmimeinfoextensions .= $pre.$xext.$post;
    }
    my $prere = $pre;
    $prere =~ s/([\*\.])/\\\1/g;
    my $postre = $post;
    $postre =~ s/([\*\.])/\\\1/g;
    $sharedmimeinfoentry =~ s/\$\$${prere}EXTENSIONS${postre}\$\$/$sharedmimeinfoextensions/gs;
  }
  print SMI $sharedmimeinfoentry;

  my $mailcapentry = $mailcaptemplate;
  $mailcapentry =~ s/\$\$NAME\$\$/$xname/g;
  $mailcapentry =~ s/\$\$SHORTNAME\$\$/$xshortname/g;
  $mailcapentry =~ s/\$\$MIMETYPE\$\$/$xmimetype/g;
  $mailcapentry =~ s/\$\$ICONFILE\$\$/$xiconfile/g;
  $mailcapentry =~ s/\$\$PRIMARY\$\$/$xprimarystring/g;
  $mailcapentry =~ s/\$\$MACASSOCIATIONROLE\$\$/$xROLE/g;
  $mailcapentry =~ s/\$\$EXTENSION\$\$/$extension0/g;
  $mailcapentry =~ s/\$\$PRIORITY\$\$/$priority/g;

  my $ext = join(",",sort(@extensions));
  my $xdisplayext = xml_escape(join(", ", map(".$_",sort(@extensions))));
  my $progresspercent = int(($mailcapcount/$num)*100);
  $progresspercent = 100 if $progresspercent > 100;
  $mailcapcount++;
  my $xext = xml_escape($ext);
  my $addunixextension = "true";

  $mailcapentry =~ s/\$\$ADDUNIXEXTENSION\$\$/$addunixextension/g;
  $mailcapentry =~ s/\$\$EXTENSION\$\$/$xext/g;
  $mailcapentry =~ s/\$\$DISPLAYEXTENSION\$\$/$xdisplayext/g;
  $mailcapentry =~ s/\$\$PROGRESSPERCENT\$\$/$progresspercent/g;
  $mailcapentry =~ s/\$\$ID\$\$/$id/g;
  $id++;
  $mailcapentry =~ s/\$\$ID1\$\$/$id/g;
  $id++;
  $mailcapentry =~ s/\$\$ID2\$\$/$id/g;
  $id++;

  print MCA $mailcapentry;

  delete $all_associations{$shortname};
  warn("Writing entry for $name (".join(",",@$extensions).": $mimetype)\n");
}

print SMI "</mime-info>\n";

close(MCA);
close(SMI);

open(D,">$desktopfile") or die ("Could not open '$desktopfile' for writing");
print D qq([Desktop Entry]
Version=1.1
Type=Application
Name=Jalview
Comment=Multiple Sequence Alignment Editor
Icon=jalview-icon
Type=Application
TryExec=jalview
Exec=jalview %u
Terminal=false
Categories=Science;Biology;
Keywords=alignment;sequence;
MimeType=${MimeType}
);
close(D);

sub xml_escape {
  my $x = shift;
  # stolen from Pod::Simple::XMLOutStream in base distro
  $x =~ s/([^-\n\t !\#\$\%\(\)\*\+,\.\~\/\:\;=\?\@\[\\\]\^_\`\{\|\}a-zA-Z0-9])/'&#'.(ord($1)).';'/eg;
  return $x;
}  
