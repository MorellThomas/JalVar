Checkstyle for Jalview
----------------------

See
https://issues.jalview.org/browse/JAL-1854
http://checkstyle.sourceforge.net/
GNU LGPL

To get the Eclipse Checkstyle plugin
------------------------------------
	- Help | Eclipse Marketplace
	- search for checkstyle
	- install eclipse-cs checkstyle plugin

Change Log
----------
See http://checkstyle.sourceforge.net/releasenotes.html
Aug 2016	Initial version used is 6.19.1
Dec 2018	Updated to 8.12.0 (latest on Eclipse Marketplace, 8.15 is latest release)
			SuppressionCommentFilter relocated (changed in 8.1)
			FileContentsHolder removed (changed in 8.2)
			Updates to import-control.xml for code changes (htsjdk, stackoverflowusers)
			
Config
------

	File Jalview/.checkstyle holds configuration for the "JalviewCheckstyle" ruleset.
	This includes confining its scope to src/*.java and resources/*.properties.
	This can be modified interactively through the checkstyle properties editor.
	
	Checkstyle config files in utils/checkstyle:
		checkstyle.xml          : main configuration file with selected checkstyle modules
		checkstyle-suppress.xml : rules to exclude certain checks / files
		import-control.xml      : package import rules
	
	Checkstyle error messages can be customised. See TypeName for an example.

How to use checkstyle
---------------------

	Option 1: enable it for the Jalview project
		- right-click on project | Checkstyle | Activate Checkstyle
		- notice CheckstyleNature gets added to the .project file
		- don't commit this file unless we all agree to!
		- Checkstyle will run as you recompile changed code

	Option 2: on demand on selected code
		- right-click on a class or package and Checkstyle | Check code with checkstyle
		- (or Clear Checkstyle violations to remove checkstyle warnings)
		- recommended to use this as a QA step when changing or reviewing code

Checkstyle rules
----------------
	Documented at http://checkstyle.sourceforge.net/checks.html
	Should be self-documenting in checkstyle.xml
	Open for discussion:
	- which rules to use
	- what naming and layout standards to apply
	- settings for complexity metrics
	- whether any rules should report an error instead of a warning  
	
Suppressing findings
--------------------
	If there are warnings you judge it ok to suppress (false positives), 
	your options are (from most global to most local impact):
	- remove the rule entirely
	- adjust its properties
	- add an entry in checkstyle-suppress.xml to skip the file for the rule
	- add comments around the reported source lines
		// CHECKSTYLE.OFF: RuleName 'a comment to justify suppression'
		source code here
		// CHECKSTYLE.ON: RuleName
	The suppression should be as localised as possible, to avoid false negatives.
	
Tips
----
	Sometimes checkstyle needs a kick before it will refresh its findings.
	Click the 'refresh' icon at top right in Eclipse | Preferences | Checkstyle.
	
	Invalid configuration files may result in checkstyle failing with an error reported
	in the Eclipse log file. 
	Eclipse | About | Installation Details | Configuration takes you to a screen with a 
	'View Error Log' button.
	
	Sometimes checkstyle can fail silently. Try 'touching' (editing) config files, failing
	that, carefully check / back out / redo any recent changes to its config.
	
	Putting <!-- XML comments --> inside a checkstyle <module> causes it to be ignored!
	
	If a rule doesn't behave as you expected, read its documentation carefully, including
	the use and default value of any properties.
	
	To highlight a single rule's findings, you can 'Configure Contents' of the Problems view
	and filter on Text Contains <rule name> (case-sensitive). 
	Here you should select 'Use item limits' with a	value of, say, 500,	or Eclipse may 
	labour to display all warnings.
