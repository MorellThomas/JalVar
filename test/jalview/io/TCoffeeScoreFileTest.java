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

import jalview.gui.JvOptionPane;
import jalview.io.TCoffeeScoreFile.Block;
import jalview.io.TCoffeeScoreFile.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TCoffeeScoreFileTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  final static File SCORE_FILE = new File(
          "test/jalview/io/tcoffee.score_ascii");

  final static File ALIGN_FILE = new File(
          "test/jalview/io/tcoffee.fasta_aln");

  @Test(groups = { "Functional" })
  public void testReadHeader() throws IOException
  {

    TCoffeeScoreFile scoreFile = new TCoffeeScoreFile(SCORE_FILE.getPath(),
            DataSourceType.FILE);
    AssertJUnit.assertTrue(scoreFile.getWarningMessage(),
            scoreFile.isValid());

    Header header = scoreFile.header;
    AssertJUnit.assertNotNull(header);
    AssertJUnit.assertEquals(
            "T-COFFEE, Version_9.02.r1228 (2012-02-16 18:15:12 - Revision 1228 - Build 336)",
            header.head);
    AssertJUnit.assertEquals(90, header.score);
    AssertJUnit.assertEquals(89, header.getScoreFor("1PHT"));
    AssertJUnit.assertEquals(90, header.getScoreFor("1BB9"));
    AssertJUnit.assertEquals(94, header.getScoreFor("1UHC"));
    AssertJUnit.assertEquals(94, header.getScoreFor("1YCS"));
    AssertJUnit.assertEquals(93, header.getScoreFor("1OOT"));
    AssertJUnit.assertEquals(94, header.getScoreFor("1ABO"));
    AssertJUnit.assertEquals(94, header.getScoreFor("1FYN"));
    AssertJUnit.assertEquals(94, header.getScoreFor("1QCF"));
    AssertJUnit.assertEquals(90, header.getScoreFor("cons"));
  }

  @Test(groups = { "Functional" })
  public void testWrongFile()
  {
    try
    {
      TCoffeeScoreFile result = new TCoffeeScoreFile(ALIGN_FILE.getPath(),
              DataSourceType.FILE);
      AssertJUnit.assertFalse(result.isValid());
    } catch (IOException x)
    {
      AssertJUnit.assertTrue("File not found exception thrown",
              x instanceof FileNotFoundException);
    }
  }

  @Test(groups = { "Functional" })
  public void testHeightAndWidth() throws IOException
  {
    TCoffeeScoreFile result = new TCoffeeScoreFile(SCORE_FILE.getPath(),
            DataSourceType.FILE);
    AssertJUnit.assertTrue(result.isValid());
    AssertJUnit.assertEquals(8, result.getHeight());
    AssertJUnit.assertEquals(83, result.getWidth());
  }

  @Test(groups = { "Functional" })
  public void testReadBlock() throws IOException
  {

    String BLOCK = "\n" + "\n" + "\n"
            + "1PHT   999999999999999999999999998762112222543211112134\n"
            + "1BB9   99999999999999999999999999987-------4322----2234  \n"
            + "1UHC   99999999999999999999999999987-------5321----2246\n"
            + "1YCS   99999999999999999999999999986-------4321----1-35\n"
            + "1OOT   999999999999999999999999999861-------3------1135  \n"
            + "1ABO   99999999999999999999999999986-------422-------34\n"
            + "1FYN   99999999999999999999999999985-------32--------35\n"
            + "1QCF   99999999999999999999999999974-------2---------24\n"
            + "cons   999999999999999999999999999851000110321100001134\n"
            + "\n" + "\n";
    FileParse source = new FileParse(BLOCK, DataSourceType.PASTE);
    Block block = TCoffeeScoreFile.readBlock(source, 0);

    AssertJUnit.assertNotNull(block);
    AssertJUnit.assertEquals(
            "999999999999999999999999998762112222543211112134",
            block.getScoresFor("1PHT"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------4322----2234",
            block.getScoresFor("1BB9"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------5321----2246",
            block.getScoresFor("1UHC"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------4321----1-35",
            block.getScoresFor("1YCS"));
    AssertJUnit.assertEquals(
            "999999999999999999999999999861-------3------1135",
            block.getScoresFor("1OOT"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------422-------34",
            block.getScoresFor("1ABO"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999985-------32--------35",
            block.getScoresFor("1FYN"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999974-------2---------24",
            block.getScoresFor("1QCF"));
    AssertJUnit.assertEquals(
            "999999999999999999999999999851000110321100001134",
            block.getConsensus());
  }

  @Test(groups = { "Functional" })
  public void testParse() throws IOException
  {

    TCoffeeScoreFile parser = new TCoffeeScoreFile(SCORE_FILE.getPath(),
            DataSourceType.FILE);

    AssertJUnit.assertEquals(
            "999999999999999999999999998762112222543211112134----------5666642367889999999999889",
            parser.getScoresFor("1PHT"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------4322----22341111111111676653-355679999999999889",
            parser.getScoresFor("1BB9"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------5321----2246----------788774--66789999999999889",
            parser.getScoresFor("1UHC"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------4321----1-35----------78777--356789999999999889",
            parser.getScoresFor("1YCS"));
    AssertJUnit.assertEquals(
            "999999999999999999999999999861-------3------1135----------78877--356789999999997-67",
            parser.getScoresFor("1OOT"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------422-------34----------687774--56779999999999889",
            parser.getScoresFor("1ABO"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999985-------32--------35----------6888842356789999999999889",
            parser.getScoresFor("1FYN"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999974-------2---------24----------6878742356789999999999889",
            parser.getScoresFor("1QCF"));
    AssertJUnit.assertEquals(
            "99999999999999999999999999985100011032110000113400100000006877641356789999999999889",
            parser.getScoresFor("cons"));
  }

  @Test(groups = { "Functional" })
  public void testGetAsList() throws IOException
  {

    TCoffeeScoreFile parser = new TCoffeeScoreFile(SCORE_FILE.getPath(),
            DataSourceType.FILE);
    AssertJUnit.assertTrue(parser.getWarningMessage(), parser.isValid());
    List<String> scores = parser.getScoresList();
    AssertJUnit.assertEquals(
            "999999999999999999999999998762112222543211112134----------5666642367889999999999889",
            scores.get(0));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------4322----22341111111111676653-355679999999999889",
            scores.get(1));
    AssertJUnit.assertEquals(
            "99999999999999999999999999987-------5321----2246----------788774--66789999999999889",
            scores.get(2));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------4321----1-35----------78777--356789999999999889",
            scores.get(3));
    AssertJUnit.assertEquals(
            "999999999999999999999999999861-------3------1135----------78877--356789999999997-67",
            scores.get(4));
    AssertJUnit.assertEquals(
            "99999999999999999999999999986-------422-------34----------687774--56779999999999889",
            scores.get(5));
    AssertJUnit.assertEquals(
            "99999999999999999999999999985-------32--------35----------6888842356789999999999889",
            scores.get(6));
    AssertJUnit.assertEquals(
            "99999999999999999999999999974-------2---------24----------6878742356789999999999889",
            scores.get(7));
    AssertJUnit.assertEquals(
            "99999999999999999999999999985100011032110000113400100000006877641356789999999999889",
            scores.get(8));

  }

  @Test(groups = { "Functional" })
  public void testGetAsArray() throws IOException
  {

    TCoffeeScoreFile parser = new TCoffeeScoreFile(SCORE_FILE.getPath(),
            DataSourceType.FILE);
    AssertJUnit.assertTrue(parser.getWarningMessage(), parser.isValid());
    byte[][] scores = parser.getScoresArray();

    AssertJUnit.assertEquals(9, scores[0][0]);
    AssertJUnit.assertEquals(9, scores[1][0]);
    AssertJUnit.assertEquals(9, scores[2][0]);
    AssertJUnit.assertEquals(9, scores[3][0]);
    AssertJUnit.assertEquals(9, scores[4][0]);
    AssertJUnit.assertEquals(9, scores[5][0]);
    AssertJUnit.assertEquals(9, scores[6][0]);
    AssertJUnit.assertEquals(9, scores[7][0]);
    AssertJUnit.assertEquals(9, scores[8][0]);

    AssertJUnit.assertEquals(5, scores[0][36]);
    AssertJUnit.assertEquals(4, scores[1][36]);
    AssertJUnit.assertEquals(5, scores[2][36]);
    AssertJUnit.assertEquals(4, scores[3][36]);
    AssertJUnit.assertEquals(-1, scores[4][36]);
    AssertJUnit.assertEquals(4, scores[5][36]);
    AssertJUnit.assertEquals(3, scores[6][36]);
    AssertJUnit.assertEquals(2, scores[7][36]);
    AssertJUnit.assertEquals(3, scores[8][36]);

  }

  @Test(groups = { "Functional" })
  public void testHeightAndWidthWithResidueNumbers() throws Exception
  {
    String file = "test/jalview/io/tcoffee.score_ascii_with_residue_numbers";
    TCoffeeScoreFile result = new TCoffeeScoreFile(file,
            DataSourceType.FILE);
    AssertJUnit.assertTrue(result.isValid());
    AssertJUnit.assertEquals(5, result.getHeight());
    AssertJUnit.assertEquals(84, result.getWidth());
  }

}
