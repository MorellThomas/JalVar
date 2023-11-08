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
package vamsas;

import vamsas.objects.simple.Alignment;
import vamsas.objects.simple.MsaResult;
import vamsas.objects.simple.SequenceSet;
import vamsas.objects.simple.WsJobId;

public interface IMsaWS
{
  public WsJobId align(SequenceSet s) throws Exception;

  public Alignment getalign(String job_id) throws Exception;

  public MsaResult getResult(String job_id) throws Exception;

  public WsJobId cancel(String jobId) throws Exception;
}
