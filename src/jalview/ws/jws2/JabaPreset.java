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

import jalview.util.MessageManager;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.WsParamSetI;

import java.util.List;

import compbio.metadata.Preset;

public class JabaPreset implements WsParamSetI
{
  Preset p = null;

  Jws2Instance service;

  public JabaPreset(Jws2Instance svc, Preset preset)
  {
    service = svc;
    p = preset;
  }

  @Override
  public String getName()
  {
    return p.getName();
  }

  @Override
  public String getDescription()
  {
    return p.getDescription();
  }

  @Override
  public String[] getApplicableUrls()
  {
    return new String[] { service.getUri() };
  }

  @Override
  public String getSourceFile()
  {
    return null;
  }

  @Override
  public boolean isModifiable()
  {
    return false;
  }

  @Override
  public void setSourceFile(String newfile)
  {
    throw new Error(MessageManager
            .formatMessage("error.cannot_set_source_file_for", new String[]
            { getClass().toString() }));
  }

  @Override
  public List<ArgumentI> getArguments()
  {
    try
    {
      return JabaParamStore.getJwsArgsfromJaba(
              p.getArguments(service.getRunnerConfig()));
    } catch (Exception e)
    {
      e.printStackTrace();
      throw new Error(MessageManager
              .getString("error.mismatch_service_instance_preset"));
    }
  }

  @Override
  public void setArguments(List<ArgumentI> args)
  {
    throw new Error(MessageManager
            .getString("error.cannot_set_params_for_ws_preset"));
  }
}
