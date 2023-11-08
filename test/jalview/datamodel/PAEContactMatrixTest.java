package jalview.datamodel;

import static org.testng.Assert.*;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jalview.analysis.AlignmentUtils;
import jalview.analysis.SeqsetUtils;
import jalview.gui.JvOptionPane;
import jalview.util.MapList;
import jalview.ws.datamodel.MappableContactMatrixI;
import jalview.ws.datamodel.alphafold.PAEContactMatrix;

public class PAEContactMatrixTest
{
  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  static float[][] PAEdata = { { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f },
      { 2.0f, 1.0f, 2.0f, 3.0f, 4.0f },
      { 3.0f, 2.0f, 1.0f, 2.0f, 3.0f },
      { 4.0f, 3.0f, 2.0f, 1.0f, 2.0f },
      { 5.0f, 4.0f, 3.0f, 2.0f, 1.0f } };

  /**
   * test associations for a PAE matrix
   */
  @Test(groups = { "Functional" })
  public void testSeqAssociatedPAEMatrix()
  {
    Sequence seq = new Sequence("Seq", "ASDQE");
    AlignmentAnnotation aa = seq
            .addContactList(new PAEContactMatrix(seq, PAEdata));
    assertNotNull(seq.getContactListFor(aa, 0));
    assertEquals(seq.getContactListFor(aa, 0).getContactAt(0), 1.0);
    assertNotNull(seq.getContactListFor(aa, 1));
    assertEquals(seq.getContactListFor(aa, 1).getContactAt(1), 1.0);
    assertNotNull(seq.getContactListFor(aa, 2));
    assertEquals(seq.getContactListFor(aa, 2).getContactAt(2), 1.0);
    assertNotNull(seq.getContactListFor(aa, 3));
    assertEquals(seq.getContactListFor(aa, 3).getContactAt(3), 1.0);
    assertNotNull(seq.getContactListFor(aa, 4));
    assertEquals(seq.getContactListFor(aa, 4).getContactAt(4), 1.0);

    assertNotNull(seq.getContactListFor(aa, seq.getEnd() - 1));
    assertNull(seq.getContactListFor(aa, seq.getEnd()));

    ContactListI cm = seq.getContactListFor(aa, seq.getStart());
    assertEquals(cm.getContactAt(seq.getStart()), 1d);
    verifyPAEmatrix(seq, aa, 0, 0, 4);

    // Now associated with sequence not starting at 1
    seq = new Sequence("Seq/5-9", "ASDQE");
    ContactMatrixI paematrix = new PAEContactMatrix(seq, PAEdata);
    aa = seq.addContactList(paematrix);
    assertNotNull(aa);
    // individual annotation elements need to be distinct for Matrix associated
    // rows
    Annotation ae5 = aa.getAnnotationForPosition(5);
    Annotation ae6 = aa.getAnnotationForPosition(6);
    assertNotNull(ae5);
    assertNotNull(ae6);
    assertTrue(ae5 != ae6);

    cm = seq.getContactListFor(aa, 0);
    assertEquals(cm.getContactAt(0), 1d);
    verifyPAEmatrix(seq, aa, 0, 0, 4);

    // test clustering
    paematrix.setGroupSet(GroupSet.makeGroups(paematrix, false,0.1f, false));

    // remap - test the MappableContactMatrix.liftOver method
    SequenceI newseq = new Sequence("Seq", "ASDQEASDQEASDQE");
    Mapping sqmap = new Mapping(seq,
            new MapList(new int[]
            { 5, 8, 10, 10 }, new int[] { 5, 9 }, 1, 1));
    assertTrue(paematrix instanceof MappableContactMatrixI);

    MappableContactMatrixI remapped = ((MappableContactMatrixI) paematrix)
            .liftOver(newseq, sqmap);
    assertTrue(remapped instanceof PAEContactMatrix);

    AlignmentAnnotation newaa = newseq.addContactList(remapped);
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(1)));
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(4)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(5)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(6)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(7)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(8)));
    // no mapping for position 9
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(9)));
    // last column
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(10)));

    // verify MappedPositions includes discontinuity
    int[] mappedCl = newseq.getContactListFor(newaa, 5)
            .getMappedPositionsFor(0, 4);
    assertEquals(4, mappedCl.length,
            "getMappedPositionsFor doesn't support discontinuous mappings to contactList");

    // make it harder.

    SequenceI alseq = newseq.getSubSequence(6, 10);
    alseq.insertCharAt(2, 2, '-');
    AlignmentI alForSeq = new Alignment(new SequenceI[] { alseq });
    newaa = AlignmentUtils.addReferenceAnnotationTo(alForSeq, alseq, newaa,
            null);
    ContactListI alcl = alForSeq.getContactListFor(newaa, 1);
    assertNotNull(alcl);
    mappedCl = alcl.getMappedPositionsFor(0, 4);
    assertNotNull(mappedCl);
    assertEquals(4, mappedCl.length,
            "getMappedPositionsFor doesn't support discontinuous mappings to contactList");

    // remap2 - test with original matrix map from 1-5 remapped to 5-9

    seq = new Sequence("Seq/1-5", "ASDQE");
    paematrix = new PAEContactMatrix(seq, PAEdata);
    assertTrue(paematrix instanceof MappableContactMatrixI);
    aa = seq.addContactList(paematrix);

    newseq = new Sequence("Seq", "ASDQEASDQEASDQE");
    sqmap = new Mapping(seq,
            new MapList(new int[]
            { 5, 9 }, new int[] { 1, 5 }, 1, 1));

    remapped = ((MappableContactMatrixI) paematrix).liftOver(newseq, sqmap);
    assertTrue(remapped instanceof PAEContactMatrix);

    newaa = newseq.addContactList(remapped);
    verify_mapping(newseq, newaa);

    // remap3 - remap2 but mapping sense in liftover is reversed

    seq = new Sequence("Seq/1-5", "ASDQE");
    paematrix = new PAEContactMatrix(seq, PAEdata);
    assertTrue(paematrix instanceof MappableContactMatrixI);
    aa = seq.addContactList(paematrix);

    newseq = new Sequence("Seq", "ASDQEASDQEASDQE");
    sqmap = new Mapping(newseq,
            new MapList(new int[]
            { 1, 5 }, new int[] { 5, 9 }, 1, 1));

    remapped = ((MappableContactMatrixI) paematrix).liftOver(newseq, sqmap);
    assertTrue(remapped instanceof PAEContactMatrix);

    newaa = newseq.addContactList(remapped);
    verify_mapping(newseq, newaa);
  }

  /**
   * checks that the PAE matrix is located at positions 1-9 in newseq, and
   * columns are not truncated.
   * 
   * @param newseq
   * @param newaa
   */
  private void verify_mapping(SequenceI newseq, AlignmentAnnotation newaa)
  {
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(1)));
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(4)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(5)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(6)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(7)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(8)));
    assertNotNull(
            newseq.getContactListFor(newaa, -1 + newseq.findIndex(9)));
    // last column should be null this time
    assertNull(newseq.getContactListFor(newaa, -1 + newseq.findIndex(10)));

    verifyPAEmatrix(newseq, newaa, 4, 4, 8);
  }

  private void verifyPAEmatrix(SequenceI seq, AlignmentAnnotation aa,
          int topl, int rowl, int rowr)
  {
    int[] mappedCl;
    for (int f = rowl; f <= rowr; f++)
    {
      ContactListI clist = seq.getContactListFor(aa, f);
      assertNotNull(clist, "No ContactListI for position " + (f));
      assertEquals(clist.getContactAt(0), (double) f - topl + 1,
              "for column " + f + " relative to " + topl);
      mappedCl = clist.getMappedPositionsFor(0, 0);
      assertNotNull(mappedCl);
      assertEquals(mappedCl[0], mappedCl[1]);
      assertEquals(mappedCl[0], seq.findIndex(seq.getStart() + topl));
      assertEquals(clist.getContactAt(f - topl), 1d,
              "for column and row " + f + " relative to " + topl);
    }
  }

  /**
   * check mapping and resolution methods work
   */
  @Test(groups= {"Functional"})
  public void testMappableContactMatrix() {
    SequenceI newseq = new Sequence("Seq", "ASDQEASDQEASDQE");
    MapList map = new MapList(new int[]
            { 5, 9 }, new int[] { 1, 5 }, 1, 1);
    AlignmentAnnotation aa = newseq
            .addContactList(new PAEContactMatrix(newseq,map, PAEdata,null));
    ContactListI clist = newseq.getContactListFor(aa, 4);
    assertNotNull(clist);
    clist = newseq.getContactListFor(aa, 3);
    assertNull(clist);
    
    ContactMatrixI cm = newseq.getContactMatrixFor(aa);
    MappableContactMatrixI mcm = (MappableContactMatrixI) cm;
    int[] pos = mcm.getMappedPositionsFor(newseq, 0);
    assertNull(pos);

    pos = mcm.getMappedPositionsFor(newseq, 1);
    assertNotNull(pos);
    assertEquals(pos[0],4+newseq.getStart());
    
    pos = mcm.getMappedPositionsFor(newseq, 6); // after end of matrix
    assertNull(pos);
    pos = mcm.getMappedPositionsFor(newseq, 5); // at end of matrix
    assertNotNull(pos);
    assertEquals(pos[0],8+newseq.getStart());
    SequenceI alseq = newseq.deriveSequence();
    alseq.insertCharAt(5,'-');
    pos = mcm.getMappedPositionsFor(alseq, 5); // at end of matrix
    assertNotNull(pos);
    assertEquals(pos[0],8+newseq.getStart());
    
    
  }
}
