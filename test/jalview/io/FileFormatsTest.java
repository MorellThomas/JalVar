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
package jalview.io;

import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileFormatsTest
{
  @AfterMethod(alwaysRun = true)
  public void tearDown()
  {
    FileFormats.getInstance().reset();
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp()
  {
    FileFormats.getInstance().reset();
  }

  @Test(groups = "Functional")
  public void testIsIdentifiable()
  {
    FileFormats formats = FileFormats.getInstance();
    assertTrue(formats
            .isIdentifiable(formats.forName(FileFormat.Fasta.getName())));
    assertTrue(formats
            .isIdentifiable(formats.forName(FileFormat.MMCif.getName())));
    assertTrue(formats
            .isIdentifiable(formats.forName(FileFormat.Jnet.getName())));
    assertTrue(formats
            .isIdentifiable(formats.forName(FileFormat.Jalview.getName())));
    // GenBank/ENA
    assertFalse(formats.isIdentifiable(null));

    /*
     * remove and re-add a format: it is still 'identifiable'
     */
    formats.deregisterFileFormat(FileFormat.Fasta.getName());
    assertNull(formats.forName(FileFormat.Fasta.getName()));
    formats.registerFileFormat(FileFormat.Fasta);
    assertSame(FileFormat.Fasta,
            formats.forName(FileFormat.Fasta.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.Fasta));
  }

  @Test(groups = "Functional")
  public void testGetReadableFormats()
  {
    String expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GenBank Flatfile, ENA Flatfile, GFF or Jalview features, PDB, mmCIF, Jalview]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getReadableFormats().toString(), expected);
  }

  @Test(groups = "Functional")
  public void testGetWritableFormats()
  {
    String expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getWritableFormats(true).toString(), expected);
    expected = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP, Jalview]";
    assertEquals(formats.getWritableFormats(false).toString(), expected);
  }

  @Test(groups = "Functional")
  public void testDeregisterFileFormat()
  {
    String writable = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    String readable = "[Fasta, PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GenBank Flatfile, ENA Flatfile, GFF or Jalview features, PDB, mmCIF, Jalview]";
    FileFormats formats = FileFormats.getInstance();
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);

    formats.deregisterFileFormat(FileFormat.Fasta.getName());
    writable = "[PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP]";
    readable = "[PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GenBank Flatfile, ENA Flatfile, GFF or Jalview features, PDB, mmCIF, Jalview]";
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);

    /*
     * re-register the format: it gets added to the end of the list
     */
    formats.registerFileFormat(FileFormat.Fasta);
    writable = "[PFAM, Stockholm, PIR, BLC, AMSA, JSON, PileUp, MSF, Clustal, PHYLIP, Fasta]";
    readable = "[PFAM, Stockholm, PIR, BLC, AMSA, HTML, RNAML, JSON, PileUp, MSF, Clustal, PHYLIP, GenBank Flatfile, ENA Flatfile, GFF or Jalview features, PDB, mmCIF, Jalview, Fasta]";
    assertEquals(formats.getWritableFormats(true).toString(), writable);
    assertEquals(formats.getReadableFormats().toString(), readable);
  }

  @Test(groups = "Functional")
  public void testForName()
  {
    FileFormats formats = FileFormats.getInstance();
    for (FileFormatI ff : FileFormat.values())
    {
      assertSame(ff, formats.forName(ff.getName()));
      assertSame(ff,
              formats.forName(ff.getName().toUpperCase(Locale.ROOT)));
      assertSame(ff,
              formats.forName(ff.getName().toLowerCase(Locale.ROOT)));
    }
    assertNull(formats.forName(null));
    assertNull(formats.forName("rubbish"));
  }

  @Test(groups = "Functional")
  public void testRegisterFileFormat()
  {
    FileFormats formats = FileFormats.getInstance();
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.MMCif));

    /*
     * deregister mmCIF format
     */
    formats.deregisterFileFormat(FileFormat.MMCif.getName());
    assertNull(formats.forName(FileFormat.MMCif.getName()));

    /*
     * re-register mmCIF format
     * it is reinstated (still 'identifiable')
     */
    formats.registerFileFormat(FileFormat.MMCif);
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
    assertTrue(formats.isIdentifiable(FileFormat.MMCif));
    // repeating does nothing
    formats.registerFileFormat(FileFormat.MMCif);
    assertSame(FileFormat.MMCif,
            formats.forName(FileFormat.MMCif.getName()));
  }

  @Test(groups = "Functional")
  public void testGetFormats()
  {
    /*
     * verify the list of file formats registered matches the enum values
     */
    FileFormats instance = FileFormats.getInstance();
    Iterator<FileFormatI> formats = instance.getFormats().iterator();
    FileFormatI[] builtIn = FileFormat.values();

    for (FileFormatI ff : builtIn)
    {
      assertSame(ff, formats.next());
    }
    assertFalse(formats.hasNext());

    /*
     * remove the first format, check it is no longer in 
     * the list of formats
     */
    String firstFormatName = instance.getFormats().iterator().next()
            .getName();
    instance.deregisterFileFormat(firstFormatName);
    assertNotEquals(instance.getFormats().iterator().next().getName(),
            firstFormatName);
  }
}
