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
package jalview.ws.jabaws;

import static org.testng.AssertJUnit.assertEquals;

import jalview.gui.JvOptionPane;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.data.msa.MsaWS;
import compbio.data.msa.RegistryWS;
import compbio.data.sequence.FastaSequence;
import compbio.metadata.JobStatus;
import compbio.ws.client.Jws2Client;
import compbio.ws.client.Services;

public class MinJabawsClientTests
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  /**
   * simple test for the benefit of JAL-1338
   * 
   * @throws Exception
   */
  @SuppressWarnings("rawtypes")
  @Test(groups = { "Network" })
  public void msaTest() throws Exception
  {
    String url;
    RegistryWS registry = Jws2Client.connectToRegistry(
            url = "http://www.compbio.dundee.ac.uk/jabaws");
    if (registry != null)
    {

      MsaWS msaservice = null;
      for (Services service : registry.getSupportedServices())
      {
        if (service == null)
        {
          // the 'unsupported service'
          continue;
        }
        if (service.equals(Services.ClustalOWS))
        {
          msaservice = (MsaWS) Jws2Client.connect(url, service);
          if (msaservice != null)
          {
            break;
          }
        }
      }
      if (msaservice == null)
      {
        Assert.fail(
                "couldn't find a clustalO service on the public registry");
      }
      FastaSequence fsq = new FastaSequence("seqA",
              "SESESESESESESESSESESSESESESESESESESESESEEEEEESSESESESESSSSESESESESESESE");
      List<FastaSequence> iseqs = new ArrayList<FastaSequence>();
      for (int i = 0; i < 9; i++)
      {
        iseqs.add(new FastaSequence(fsq.getId() + i, fsq.getSequence()
                + fsq.getSequence().substring(i + 3, i + 3 + i)));
      }

      String jobid = msaservice.align(iseqs);
      if (jobid != null)
      {
        JobStatus js = null;
        do
        {
          try
          {
            Thread.sleep(500);
          } catch (InterruptedException q)
          {
          }
          ;
          js = msaservice.getJobStatus(jobid);
        } while (!js.equals(JobStatus.FAILED)
                && !js.equals(JobStatus.CANCELLED)
                && !js.equals(JobStatus.FINISHED));
        assertEquals("Trial alignment failed. State was " + js.name(), js,
                JobStatus.FINISHED);
        assertEquals(
                "Mismatch in number of input and result sequences - assume alignment service wasn't interacted with correctly",
                msaservice.getResult(jobid).getSequences().size(),
                iseqs.size());
        for (FastaSequence t : msaservice.getResult(jobid).getSequences())
        {
          System.out.println(">" + t.getId());
          System.out.println(t.getFormattedFasta());
        }
        // .forEach(new Consumer<FastaSequence>() {
        // @Override
        // public void accept(FastaSequence t) {
        // System.out.println(">"+t.getId());
        // System.out.println(t.getFormattedFasta());
        // }
        // });
      }

    }
  }
}
