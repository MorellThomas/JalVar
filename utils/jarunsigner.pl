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

# perverse script to get rid of unwanted jar signatures
use Cwd qw(abs_path);
use File::Temp qw(tempdir);

my $tempdir = tempdir( CLEANUP => 1);

my $jarfile;

my @jarfiles;

while (scalar @ARGV) {
    my $jarfile = shift @ARGV;
    ((-f $jarfile) and $jarfile=~/.+\.jar/) 
        and push @jarfiles, abs_path($jarfile);
}
my $pwdir = `pwd`;
chdir($tempdir);

while (scalar @jarfiles) {
    $jarfile = shift @jarfiles;
    system("rm -Rf *");
    system("jar xf $jarfile");
    system("mv $jarfile $jarfile.bak");
    system("find META-INF \\( -name \"*.SF\" \\) -exec rm -f \\{\\} \\;");
    system("find META-INF \\( -name \"*.RSA\" \\) -exec rm -f \\{\\} \\;");
    system("find META-INF \\( -name \"*.DSA\" \\) -exec rm -f \\{\\} \\;");
    system("jar cf $jarfile *");
}

chdir($pwdir);
