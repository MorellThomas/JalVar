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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Isolated class to ascertain physical memory of the system using
 * com.sun.management.OperatingSystemMXBean class's getTotalPhysicalMemorySize
 * method. This class is present in OpenJDK 8,9,10,11,12,13. It is present but
 * marked as deprecated in the early-access(30) release of OpenJDK 14. In case
 * of an alternative/unsupported JRE being used or the class/method not being
 * implemented in an exotic architecture JRE this call has been isolated into
 * this separate class.
 * 
 * @author bsoares
 *
 */
class GetMemory
{

  /**
   * Wrapper for
   * com.sun.management.OperatingSystemMXBean.getTotalPhysicalMemorySize()
   * 
   * @return Result of
   *         com.sun.management.OperatingSystemMXBean.getTotalPhysicalMemorySize()
   *         or -1 if this class is not present in the JRE.
   */
  protected static long getPhysicalMemory()
  {
    final OperatingSystemMXBean o = ManagementFactory
            .getOperatingSystemMXBean();

    try
    {
      if (o instanceof com.sun.management.OperatingSystemMXBean)
      {
        final com.sun.management.OperatingSystemMXBean osb = (com.sun.management.OperatingSystemMXBean) o;
        return osb.getTotalPhysicalMemorySize();
      }
    } catch (NoClassDefFoundError e)
    {
      // com.sun.management.OperatingSystemMXBean doesn't exist in this JVM
      System.err.println(
              "No com.sun.management.OperatingSystemMXBean: cannot get total physical memory size");
    }

    // We didn't get a com.sun.management.OperatingSystemMXBean.
    return -1;
  }

}
