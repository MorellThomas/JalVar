/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
import jalview.gui.*;
import jalview.io.*;

def class JvLoadTest {
    FileLoader fl = null;
    def String safename = null;
    JvLoadTest(String sname) { 
	if (!new File(sname).exists() || new File(sname).canWrite())
	    {
		safename = sname;
	    } else {
		System.err.println("Warning : "+sname+" isn't being used to store temporary files.");
	    }	
    }
    def public boolean doTest (file) {
	fl = new FileLoader(false);
	System.gc();
	AlignFrame af = fl.LoadFileWaitTillLoaded(file
						  ,FormatAdapter.FILE);
	return doTest(af);
    }
    def public boolean doSequentialReadTest (file) {
	return doSequentialReadTest(file, 0);
    }
    // Return true if there is more data to read.
    def public boolean peekFp(FileParse fp) {
	try { fp.mark(); }  catch (Exception ex) { System.err.println("FAILED mark."+ex); return false; };
	try {
	  def String nl;
	  for (i in 1..3) { 
	   nl = fp.nextLine();
	   if (nl==null) { return false; }
	   System.out.println(nl +"\\n");
          }
	} catch (Exception e) { // end of file.
		return false; };
	try { fp.reset(); } catch (Exception ex) { System.err.println("FAILED rewind."+ex); return false; };
	return true;
    }
    /*
      Halt after loading the mx'th entry in the filestream
    */
    def public boolean doSequentialReadTest (file, int mx) {
	// first properly open the file
	//	if (!doTest(file)) { return };
	def FileParse fp = null;
	try {
		fp = new FileParse(file, AppletFormatAdapter.FILE);
	} catch (Exception e) { System.err.println("Couldn't open "+file+"\\n"); e.printStackTrace(); return false;};
	Desktop.instance.closeAll_actionPerformed(null)
	    System.gc();
	while (fp!=null && fp.isValid() && (mx==0 || mx!=fp.index)) {
	    if (!peekFp(fp)) return false;
	    fl = new FileLoader(false);
	    AlignFrame af = fl.LoadFileWaitTillLoaded(fp, null);
	    System.out.println("FileParse index: "+fp.index);	
	    if (af!=null && (mx==0 || mx!=fp.index))
		{	def boolean res = doTest(af);
		if (!res)
		    {
			// return false;
		    }
		} else {
		    // return false;
		}
	}
	return true;
    }
    def public void waitTillSettled(AlignFrame af)
    {
	if (af==null) { return; }
	Thread.sleep(10);
	while (af.getViewport().updatingConsensus || af.getViewport().updatingConservation) {
	    Thread.sleep(150); // wait until things settle down
	}
    }
    def public boolean doTest(AlignFrame af) {
	Object pr = af.getViewport().getAlignment().getProperty("AC");
	if (pr!=null) { System.out.println("Accession = "+(String) pr); }
	af.selectAllSequenceMenuItem_actionPerformed(null)
	    def boolean done = false;
	// Just try to save - don\'t mess around with clipboard
	/*while (!done) {
	  try {
	  af.copy_actionPerformed(null)
	  done = true;
	  } catch (Exception e) {
	  Thread.sleep(100); // wait until clipboard might be available again
	  }
	  }*/
	if (af==null) { return false; }
	waitTillSettled(af);
	// Try and save as a jalview project and reload
	try {
	    //	    af.saveAlignment(safename, "Jalview")
	    new Jalview2XML().SaveState(new java.io.File(safename));
	    Thread.sleep(100);
		} catch (Exception ex) { 
		    System.out.println("Couldn\'t save.");
		    ex.printStackTrace(System.err);
		    return false;
		}
	waitTillSettled(af);
	try {
	    Desktop.instance.closeAll_actionPerformed(null);
	} catch (Exception ex) {}
	System.gc();
	try {
	    af = new FileLoader(false).LoadFileWaitTillLoaded(safename, FormatAdapter.FILE);	
	} 
	catch (Exception ex) {
	    System.out.println("Couldn't reload saved file.");
            System.gc();
	    return false;
	}
	waitTillSettled(af);

	Desktop.instance.closeAll_actionPerformed(null);

	// af.paste(true)
	// af.newView_actionPerformed(null)
	// af.newView_actionPerformed(null)

	return true;
    }
    def public boolean TestForAll(String dir) {
	println "For directory or file : "+dir;
	File fd = new File(dir);
	if (!fd.isDirectory()) { return doSequentialReadTest(dir); }
	fd.eachFile() { file -> TestForAll(file.getAbsolutePath()) };
    }
}
def JvLoadTest newJvLoadTest(String tempFile) {
	jalview.gui.Desktop.instance.closeAll_actionPerformed(null);
	System.gc();
	jalview.gui.Desktop.instance.desktop.showMemoryUsage(true);
	return new JvLoadTest(tempFile)
}