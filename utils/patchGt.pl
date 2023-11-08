#!/bin/perl
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
use Env qw($GTID);

defined($GTID) or $GTID="UA-9060947-1";
my $SCRIPT = <<FOO;
<body>
<script type="text/javascript">
    var gaJsHost = (("https:" == document.location.protocol) ?
	"https://ssl." : "http://www.");
    document.write(unescape("%3Cscript src=\'" + gaJsHost +
	"google-analytics.com/ga.js\' type=\'text/javascript\'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try{
    var pageTracker = _gat._getTracker("'$GTID'");
    pageTracker._trackPageview();
} catch(err) {}
</script>
FOO
		
while (scalar @ARGV)
{
    my $f=shift @ARGV;
    if (-f $f) {
	if (system("grep","-v","-q",'"'.$GTID.'"',$f)) {
	    if (open OF,">$f.".$GTID) {
		if (open IF,"$f") {
		while (<IF>) {
		    if ($_=~m!<body>!) {
			$_=~s!<body>!$SCRIPT!;
		    } else {
#			$_=~s!href="([~"]+)"!href="$1" onclick="
		    }
		    print OF $_;
		}
		close(IF);
		close(OF);
		rename($f,$f.".old.".$GTID) or die("Couldn't rename $f to $f".".old.".$GTID,$@);
		rename($f.".$GTID",$f) or die("Couldn't rename $f.".$GTID." to $f",$@);
		unlink($f.".old.".$GTID) or die("Couldn't delete ".$f.".old.".$GTID,$@);
		} else {
		    warn("Can't open $f for reading.",$@);
		}
	    } else {
		warn("Couldn't open new edited file $f.$GTID",$@);
	    }
	}
    }
}

