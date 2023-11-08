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
 
# Splits a concatenated set of Stockholm Files into several individual files.

use strict;
use FileHandle;
my $ac;
my $lns="";
my $fh;
while (<>) {
    if ($_=~m!^//!) {
	$fh->print("//\n");
	$fh->close();
	$ac = undef;
	$lns = "";
    } else {
	if ($_=~/GF\s+AC\s+([0-9.RPF]+)/) { 
	    $ac=$1; 
	    ($fh=new FileHandle)->open(">$ac.stk") or die("Couldn't open file '$ac.stk'"); 
	    $lns=~/^. STOCKHOLM 1.0/ or $fh->print("# STOCKHOLM 1.0\n");
	};
	if (defined($fh)) {
	    if (defined $lns) { 
		$fh->print($lns); $lns=undef; }
	    
	    $fh->print($_);
	} else {
	    $lns .= $_;
	}
    }
}
