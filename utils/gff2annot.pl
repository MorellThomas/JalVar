#!/usr/bin/perl
##
# Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
# Copyright (C) $$Year-Rel$$ The Jalview Authors
# 
# This file is part of Jalview.
# 
# Jalview is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License 
# as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#  
# Jalview is distributed in the hope that it will be useful, but 
# WITHOUT ANY WARRANTY; without even the implied warranty 
# of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
# PURPOSE.  See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
# The Jalview Authors are detailed in the 'AUTHORS' file.
##

use strict;
use warnings;

my %annotLines;
my %featureids;
my @fields;
while (<>) {
    ($_=~/^\#/) and next;
    my @fields = split /\s+/, $_;
    if (scalar @fields) {
        (defined $annotLines{$fields[1]}) or $annotLines{$fields[1]}=[];
        # this is the tab-separated set of fields forming a jalview annotation line
        # we only use sequence IDs, not numbers
        my $line = [$fields[2],$fields[0],"-1", $fields[3], $fields[4], $fields[2]];
        $featureids{$fields[2]}="FF0000"; # red is the colour.
        my $attribs = {};
        if (scalar @fields>5) {
            $attribs->{"gff:score"}=$fields[5];
            (scalar @fields>6) and $attribs->{"gff:strand"}=$fields[6];
            (scalar @fields>7) and $attribs->{"gff:frame"}=$fields[7];
            if (scalar @fields>8) {
                for (my $i=7; ($i+1)<(scalar @fields); $i+=2) {
                    $attribs->{"gff:".$fields[$i]} = $fields[$i+1];
                }
            }
        }
        push @{$annotLines{$fields[1]}}, [$line, $attribs];
    }
}
foreach my $labels (keys %featureids) {
    print "$labels\t".$featureids{$labels}."\n"; 
}
foreach my $labels (keys %annotLines) {
    print "STARTGROUP\t".$labels."\n";
    foreach my $annot (@{$annotLines{$labels}}) {
        # bare minimum is written - no attributes/links yet.
        print "".(join "\t",@{$annot->[0]})."\n"; 
    }
    print "ENDGROUP\t".$labels."\n";
}

=pod

=head1 NAME

gff2annot.pl

=head2 SYNOPSIS


  gff2annot.pl [one or more files containing gff annotation]

Generates a nominally usable Jalview Annotation file on B<STDOUT> from arbitrary GFF annotation lines.

=head2 DESCRIPTION

This script will generate a jalview features file on standard out, from a set of GFF annotation lines input from STDIN and/or any provided filenames.

For a series of GFF annotation lines looking like :

E<lt>seqIdE<gt> E<lt>sourceE<gt> E<lt>nameE<gt> E<lt>startE<gt> E<lt>endE<gt> [E<lt>scoreE<gt> E<lt>strandE<gt> E<lt>frameE<gt> [E<lt>AttributeE<gt> E<lt>Attribute-Value<gt>]]

The script will generate a seuqence features file on B<STDOUT> where annotation with a particular B<source> string will be grouped together under that name.

=head2 Example

Passing some GFF annotation through STDIN:

  perl gff2annot.pl
  Seq1 blastx significant_hsp 1 5 0.9 + 1 link http://mylink/
  # a comment
  Seq1 blasty significant_hsp 15 25 0.9 + 1 link http://mylink/
  Seq1 blastz significant_hsp 32 43 0.9 + 1 link http://mylink/
  Seq2 blastx significant_hsp 1 5 0.9 + 1 link http://mylink/
  Seq2 blasty significant_hsp 1 5 0.9 + 1 link http://mylink/
  Seq2 blastz significant_hsp 1 5 0.9
  Seq3 blastx significant_hsp 50 70
  <Control^D/Z>

Produces

  significant_hsp	FF0000
  STARTGROUP	blasty
  significant_hsp	Seq1	-1	15	25	significant_hsp
  significant_hsp	Seq2	-1	1	5	significant_hsp
  ENDGROUP	blasty
  STARTGROUP	blastx
  significant_hsp	Seq1	-1	1	5	significant_hsp
  significant_hsp	Seq2	-1	1	5	significant_hsp
  significant_hsp	Seq3	-1	50	70	significant_hsp
  ENDGROUP	blastx
  STARTGROUP	blastz
  significant_hsp	Seq1	-1	32	43	significant_hsp
  significant_hsp	Seq2	-1	1	5	significant_hsp
  ENDGROUP	blastz

=cut
