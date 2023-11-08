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
package jalview.ws.jws2;

import jalview.ws.AWsJob;

import compbio.metadata.JobStatus;

/**
 * job status processing for JWS2 jobs.
 * 
 * @author JimP
 * 
 */
public abstract class JWs2Job extends AWsJob
{
  JobStatus status = null;

  public void setjobStatus(JobStatus jobStatus)
  {
    status = jobStatus;
    // update flags
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#clearResponse()
   */
  @Override
  public void clearResponse()
  {
    status = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#getState()
   */
  @Override
  public String getState()
  {
    return status == null ? ("Unknown") : status.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#hasResponse()
   */
  @Override
  public boolean hasResponse()
  {
    // TODO Auto-generated method stub
    return status != null;
  }

  /*
   * StringBuffer statusBuffer = null; (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#getStatus()
   * 
   * @Override public String getStatus() { return statusBuffer.toString(); }
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#hasStatus()
   * 
   * @Override public boolean hasStatus() { return statusBuffer!=null; }
   */

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isBroken()
   */
  @Override
  public boolean isBroken()
  {
    return status == null ? false : status.equals(status.UNDEFINED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isFailed()
   */
  @Override
  public boolean isFailed()
  {
    return status == null ? false : status.equals(status.FAILED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isFinished()
   */
  @Override
  public boolean isFinished()
  {
    return status == null ? false : status.equals(status.FINISHED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isQueued()
   */
  @Override
  public boolean isQueued()
  {
    return status == null ? false
            : status.equals(status.SUBMITTED)
                    || status.equals(status.PENDING);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isRunning()
   */
  @Override
  public boolean isRunning()
  {
    // TODO Auto-generated method stub
    return status != null && (status.equals(status.RUNNING)
            || status.equals(status.STARTED));
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.ws.AWsJob#isServerError()
   */
  @Override
  public boolean isServerError()
  {
    // server errors are raised as exceptions on the service method calls.
    return status == null ? false : false; // status.equals(status.FAILED);
  }

}
