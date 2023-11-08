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
package jalview.gui;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentGenerator;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceGroup;
import jalview.io.DataSourceType;
import jalview.io.FileLoader;
import junit.extensions.PA;

/**
 * Provides a simple test that memory is released when all windows are closed.
 * <ul>
 * <li>generates a reasonably large alignment and loads it</li>
 * <li>performs various operations on the alignment</li>
 * <li>closes all windows</li>
 * <li>requests garbage collection</li>
 * <li>asserts that the remaining memory footprint (heap usage) is 'not large'
 * </li>
 * </ul>
 * If the test fails, this means that reference(s) to large object(s) have
 * failed to be garbage collected. In this case:
 * <ul>
 * <li>set a breakpoint just before the test assertion in
 * {@code checkUsedMemory}</li>
 * <li>if the test fails intermittently, make this breakpoint conditional on
 * {@code usedMemory > expectedMax}</li>
 * <li>run the test to this point (and check that it is about to fail i.e.
 * {@code usedMemory > expectedMax})</li>
 * <li>use <a href="https://visualvm.github.io/">visualvm</a> to obtain a heap
 * dump from the suspended process (and kill the test or let it fail)</li>
 * <li>inspect the heap dump using visualvm for large objects and their
 * referers</li>
 * <li>Tips:</li>
 * <ul>
 * <li>Perform GC from the Monitor view in visualvm before requesting the heap
 * dump - test failure might be simply a delay to GC</li>
 * <li>View 'Objects' and filter classes to {@code jalview}. Sort columns by
 * Count, or Size, and look for anything suspicious. For example, if the object
 * count for {@code Sequence} is non-zero (it shouldn't be), pick any instance,
 * and follow the chain of {@code references} to find which class(es) still hold
 * references to sequence objects</li>
 * <li>If this chain is impracticably long, re-run the test with a smaller
 * alignment (set width=100, height=10 in {@code generateAlignment()}), to
 * capture a heap which is qualitatively the same, but much smaller, so easier
 * to analyse; note this requires an unconditional breakpoint</li>
 * </ul>
 * </ul>
 * <p>
 * <h2>Fixing memory leaks</h2>
 * <p>
 * Experience shows that often a reference is retained (directly or indirectly)
 * by a Swing (or related) component (for example a {@code MouseListener} or
 * {@code ActionListener}). There are two possible approaches to fixing:
 * <ul>
 * <li>Purist: ensure that all listeners and similar objects are removed when no
 * longer needed. May be difficult, to achieve and to maintain as code
 * changes.</li>
 * <li>Pragmatic: null references to potentially large objects from Jalview
 * application classes when no longer needed, typically when a panel is closed.
 * This ensures that even if the JVM keeps a reference to a panel or viewport,
 * it does not retain a large heap footprint. This is the approach taken in, for
 * example, {@code AlignmentPanel.closePanel()} and
 * {@code AnnotationPanel.dispose()}.</li>
 * <li>Adjust code if necessary; for example an {@code ActionListener} should
 * act on {@code av.getAlignment()} and not directly on {@code alignment}, as
 * the latter pattern could leave persistent references to the alignment</li>
 * </ul>
 * Add code to 'null unused large object references' until the test passes. For
 * a final sanity check, capture the heap dump for a passing test, and satisfy
 * yourself that only 'small' or 'harmless' {@code jalview} object instances
 * (such as enums or singletons) are left in the heap.
 */
public class FreeUpMemoryTest
{
  private static final int ONE_MB = 1000 * 1000;

  /*
   * maximum retained heap usage (in MB) for a passing test
   */
  private static int MAX_RESIDUAL_HEAP = 45;

  /**
   * Configure (read-only) Jalview property settings for test
   */
  @BeforeClass(alwaysRun = true)
  public void setUp()
  {
    Jalview.main(
            new String[]
            { "-nonews", "-props", "test/jalview/testProps.jvprops" });
    String True = Boolean.TRUE.toString();
    Cache.applicationProperties.setProperty("SHOW_ANNOTATIONS", True);
    Cache.applicationProperties.setProperty("SHOW_QUALITY", True);
    Cache.applicationProperties.setProperty("SHOW_CONSERVATION", True);
    Cache.applicationProperties.setProperty("SHOW_OCCUPANCY", True);
    Cache.applicationProperties.setProperty("SHOW_IDENTITY", True);
  }

  @Test(groups = "Memory")
  public void testFreeMemoryOnClose() throws IOException
  {
    File f = generateAlignment();
    f.deleteOnExit();

    doStuffInJalview(f);

    if (Desktop.instance != null)
      Desktop.instance.closeAll_actionPerformed(null);

    checkUsedMemory(MAX_RESIDUAL_HEAP);
  }

  /**
   * Returns the current total used memory (available memory - free memory),
   * rounded down to the nearest MB
   * 
   * @return
   */
  private static int getUsedMemory()
  {
    long availableMemory = Runtime.getRuntime().totalMemory();
    long freeMemory = Runtime.getRuntime().freeMemory();
    long usedMemory = availableMemory - freeMemory;

    return (int) (usedMemory / ONE_MB);
  }

  /**
   * Requests garbage collection and then checks whether remaining memory in use
   * is less than the expected value (in Megabytes)
   * 
   * @param expectedMax
   */
  protected void checkUsedMemory(int expectedMax)
  {
    /*
     * request garbage collection and wait for it to run (up to 3 times);
     * NB there is no guarantee when, or whether, it will do so
     */
    long usedMemory = 0L;
    Long minUsedMemory = null;
    int gcCount = 0;
    while (gcCount < 3)
    {
      gcCount++;
      System.gc();
      waitFor(1500);
      usedMemory = getUsedMemory();
      if (minUsedMemory == null || usedMemory < minUsedMemory)
      {
        minUsedMemory = usedMemory;
      }
      if (usedMemory < expectedMax)
      {
        break;
      }
    }

    /*
     * if this assertion fails (reproducibly!)
     * - set a breakpoint here, conditional on (usedMemory > expectedMax)
     * - run VisualVM to inspect the heap usage, and run GC from VisualVM to check 
     *   it is not simply delayed garbage collection causing the test failure 
     * - take a heap dump and identify large objects in the heap and their referers
     * - fix code as necessary to null the references on close
     */
    System.out.println("(Minimum) Used memory after " + gcCount
            + " call(s) to gc() = " + minUsedMemory + "MB (should be <="
            + expectedMax + ")");
    assertTrue(usedMemory <= expectedMax, String.format(
            "Used memory %d should be less than %d (Recommend running test manually to verify)",
            usedMemory, expectedMax));
  }

  /**
   * Loads an alignment from file and exercises various operations in Jalview
   * 
   * @param f
   */
  protected void doStuffInJalview(File f)
  {
    /*
     * load alignment, wait for consensus and other threads to complete
     */
    AlignFrame af = new FileLoader().LoadFileWaitTillLoaded(f.getPath(),
            DataSourceType.FILE);
    while (af.getViewport().isCalcInProgress())
    {
      waitFor(200);
    }

    /*
     * open an Overview window
     */
    af.overviewMenuItem_actionPerformed(null);
    assertNotNull(af.alignPanel.overviewPanel);

    /*
     * exercise the pop-up menu in the Overview Panel (JAL-2864)
     */
    Object[] args = new Object[] {
        new MouseEvent(af, 0, 0, 0, 0, 0, 1, true) };
    PA.invokeMethod(af.alignPanel.overviewPanel,
            "showPopupMenu(java.awt.event.MouseEvent)", args);

    /*
     * set a selection group - potential memory leak if it retains
     * a reference to the alignment
     */
    SequenceGroup sg = new SequenceGroup();
    sg.setStartRes(0);
    sg.setEndRes(100);
    AlignmentI al = af.viewport.getAlignment();
    for (int i = 0; i < al.getHeight(); i++)
    {
      sg.addSequence(al.getSequenceAt(i), false);
    }
    af.viewport.setSelectionGroup(sg);

    /*
     * compute Tree and PCA (on all sequences, 100 columns)
     */
    af.openTreePcaDialog();
    CalculationChooser dialog = af.alignPanel.getCalculationDialog();
    dialog.openPcaPanel("BLOSUM62", dialog.getSimilarityParameters(true));
    dialog.openTreePanel("BLOSUM62", dialog.getSimilarityParameters(false));

    /*
     * wait until Tree and PCA have been computed
     */
    while (af.viewport.getCurrentTree() == null
            || dialog.getPcaPanel().isWorking())
    {
      waitFor(10);
    }

    /*
     * give Swing time to add the PCA panel (?!?)
     */
    waitFor(100);
  }

  /**
   * Wait for waitMs miliseconds
   * 
   * @param waitMs
   */
  protected void waitFor(int waitMs)
  {
    try
    {
      Thread.sleep(waitMs);
    } catch (InterruptedException e)
    {
    }
  }

  /**
   * Generates an alignment and saves it in a temporary file, to be loaded by
   * Jalview. We use a peptide alignment (so Conservation and Quality are
   * calculated), which is wide enough to ensure Consensus, Conservation and
   * Occupancy have a significant memory footprint (if not removed from the
   * heap).
   * 
   * @return
   * @throws IOException
   */
  private File generateAlignment() throws IOException
  {
    File f = File.createTempFile("MemoryTest", "fa");
    PrintStream ps = new PrintStream(f);
    AlignmentGenerator ag = new AlignmentGenerator(false, ps);
    int width = 100000;
    int height = 100;
    ag.generate(width, height, 0, 10, 15);
    ps.close();
    return f;
  }
}
