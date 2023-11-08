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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import jalview.datamodel.SequenceI;

import java.io.IOException;

import org.testng.annotations.Test;

public class ClustalFileTest
{
  @Test(groups = "Functional")
  public void testParse_withNumbering() throws IOException
  {
    //@formatter:off
    String data = "CLUSTAL\n\n"
            + "FER_CAPAA/1-8      -----------------------------------------------------------A\t1\n"
            + "FER_CAPAN/1-55     MA------SVSATMISTSFMPRKPAVTSL-KPIPNVGE--ALFGLKS-A--NGGKVTCMA 48\n"
            + "FER1_SOLLC/1-55    MA------SISGTMISTSFLPRKPAVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA 48\n"
            + "Q93XJ9_SOLTU/1-55  MA------SISGTMISTSFLPRKPVVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA 48\n"
            + "FER1_PEA/1-60      MATT---PALYGTAVSTSFLRTQPMPMSV-TTTKAFSN--GFLGLKT-SLKRGDLAVAMA 53\n\n"
            + "FER_CAPAA/1-8      SYKVKLI 8\n"
            + "FER_CAPAN/1-55     SYKVKLI 55\n"
            + "FER1_SOLLC/1-55    SYKVKLI 55\n"
            + "Q93XJ9_SOLTU/1-55  SYKVKLI 55\n"
            + "FER1_PEA/1-60      SYKVKLV 60\n"
            + "                   .*     .:....*******..** ..........**  ********...*:::*  ...\n"
            + "\t\t.:.::.  *\n";
    //@formatter:on
    ClustalFile cf = new ClustalFile(data, DataSourceType.PASTE);
    cf.parse();
    SequenceI[] seqs = cf.getSeqsAsArray();
    assertEquals(seqs.length, 5);
    assertEquals(seqs[0].getName(), "FER_CAPAA");
    assertEquals(seqs[0].getStart(), 1);
    assertEquals(seqs[0].getEnd(), 8);
    assertTrue(seqs[0].getSequenceAsString().endsWith("ASYKVKLI"));
  }

  @Test(groups = "Functional")
  public void testParse_noNumbering() throws IOException
  {
    //@formatter:off
    String data = "CLUSTAL\n\n"
            + "FER_CAPAA/1-8      -----------------------------------------------------------A\n"
            + "FER_CAPAN/1-55     MA------SVSATMISTSFMPRKPAVTSL-KPIPNVGE--ALFGLKS-A--NGGKVTCMA\n"
            + "FER1_SOLLC/1-55    MA------SISGTMISTSFLPRKPAVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA\n"
            + "Q93XJ9_SOLTU/1-55  MA------SISGTMISTSFLPRKPVVTSL-KAISNVGE--ALFGLKS-G--RNGRITCMA\n"
            + "FER1_PEA/1-60      MATT---PALYGTAVSTSFLRTQPMPMSV-TTTKAFSN--GFLGLKT-SLKRGDLAVAMA\n\n"
            + "FER_CAPAA/1-8      SYKVKLI\n"
            + "FER_CAPAN/1-55     SYKVKLI\n"
            + "FER1_SOLLC/1-55    SYKVKLI\n"
            + "Q93XJ9_SOLTU/1-55  SYKVKLI\n"
            + "FER1_PEA/1-60      SYKVKLV\n";
    //@formatter:on
    ClustalFile cf = new ClustalFile(data, DataSourceType.PASTE);
    cf.parse();
    SequenceI[] seqs = cf.getSeqsAsArray();
    assertEquals(seqs.length, 5);
    assertEquals(seqs[0].getName(), "FER_CAPAA");
    assertEquals(seqs[0].getStart(), 1);
    assertEquals(seqs[0].getEnd(), 8);
    assertTrue(seqs[0].getSequenceAsString().endsWith("ASYKVKLI"));
  }
}
