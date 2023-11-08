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
package jalview.bin;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

public class MemorySettingTest
{

  @Test(groups = "Functional")
  public void testGetMemorySetting()
  {
    long KB = 1024;
    long MB = KB * KB;
    long GB = MB * KB;
    // long TB = GB * KB;

    /* some of these tests assume a host machine with RAM somewhere between 1GB and 1TB */

    // should return 100% of physical memory available (or 1TB whichever is
    // smaller)
    long mem1 = MemorySetting.getMemorySetting("1T", "100");
    long fullmem = mem1 + 512 * MB; // mem1 gets 512MB removed for the OS
    long mem1b = MemorySetting.getMemorySetting("1t", "100");
    assertTrue(mem1 > 1 * GB);
    assertEquals(mem1, mem1b);

    // test 10% memory. Note 512MB is set as minimum, so adjust to 50% if less
    // than
    // 5GB RAM.
    String pc;
    Float pcf;
    if (mem1 > 5 * GB)
    {
      pc = "10";
      pcf = 0.1f;
    }
    else
    {
      pc = "50";
      pcf = 0.5f;
    }
    long mem1c = MemorySetting.getMemorySetting("1T", pc);
    assertTrue(mem1c > (pcf - 0.01) * fullmem
            && mem1c < (pcf + 0.01) * fullmem); // allowing for floating point
                                                // errors

    // should return 1GB (assuming host machine has more than 1GB RAM)
    long mem2 = MemorySetting.getMemorySetting("1G", "100");
    long mem2b = MemorySetting.getMemorySetting("1g", "100");
    assertEquals(mem2, 1 * GB);
    assertEquals(mem2, mem2b);

    long mem3 = MemorySetting.getMemorySetting("1024M", "100");
    long mem3b = MemorySetting.getMemorySetting("1024m", "100");
    assertEquals(mem3, 1024 * MB);
    assertEquals(mem3, mem3b);

    long mem4 = MemorySetting.getMemorySetting("1048576K", "100");
    long mem4b = MemorySetting.getMemorySetting("1048576k", "100");
    assertEquals(mem4, 1048576 * KB);
    assertEquals(mem4, mem4b);

    long mem5 = MemorySetting.getMemorySetting("1073741824B", "100");
    long mem5b = MemorySetting.getMemorySetting("1073741824b", "100");
    long mem5c = MemorySetting.getMemorySetting("1073741824", "100");
    assertEquals(mem5, 1073741824L);
    assertEquals(mem5, mem5b);
    assertEquals(mem5, mem5c);

    // check g, m, k, b, "" acting as they should
    assertEquals(mem2, mem3);
    assertEquals(mem2, mem4);
    assertEquals(mem2, mem5);

    // default should not be more than 90% memory or 32GB
    long mem6 = MemorySetting.getMemorySetting();
    assertTrue(mem6 <= (long) (0.905 * fullmem));
    assertTrue(mem6 <= 32 * GB);

    // ensure enough memory for application
    long mem7 = MemorySetting.getMemorySetting("1B", "0.000000001");
    assertEquals(mem7, 512 * MB);

    // ensure enough memory for OS
    long mem8 = MemorySetting.getMemorySetting("2T", "100"); // this should be
                                                             // short of 512MB
    long mem8b = MemorySetting.getMemorySetting("2T", "50");
    // allow 10k leeway
    long diff = mem8b * 2 - mem8;
    assertTrue(512 * MB - 10 * KB < diff && diff < 512 * MB + 10 * KB);
    // assertEquals(mem8b * 2 - mem8, 512 * MB);
  }

}
