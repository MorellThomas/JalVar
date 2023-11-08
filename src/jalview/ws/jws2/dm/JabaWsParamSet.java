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
package jalview.ws.jws2.dm;

import jalview.util.MessageManager;
import jalview.ws.jws2.JabaParamStore;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.WsParamSetI;

import java.util.ArrayList;
import java.util.List;

import compbio.metadata.Option;

public class JabaWsParamSet implements WsParamSetI
{
  /**
   * raw jaba arguments.
   */
  List<Option> jabaArguments;

  String name, description, applicableUrls[], sourceFile;

  public JabaWsParamSet(String storeSetName, String descr, List jobParams)
  {
    if (jobParams.size() > 0)
    {
      if (jobParams.get(0) instanceof Option)
      {
        jabaArguments = new ArrayList<Option>();
        // if classCastExceptions are raised then there has been an
        // implementation error.
        jabaArguments.addAll((List<Option>) jobParams);
      }
      else
      {
        if (!allJaba(jobParams))
        {
          throw new Error(MessageManager
                  .getString("error.cannot_create_jabaws_param_set"));
        }
        else
        {
          jabaArguments = JabaParamStore.getJabafromJwsArgs(jobParams);
        }
      }
    }
    name = storeSetName;
    description = descr;
    sourceFile = null;
    applicableUrls = null;
  }

  public JabaWsParamSet()
  {

  }

  private boolean allJaba(List jobParams)
  {

    boolean allJaba = true;
    for (Object jp : jobParams)
    {
      if (jp instanceof JabaParameter || jp instanceof JabaOption)
      {
        allJaba &= true;
      }
      else
      {
        allJaba = false;
        break;
      }
    }
    return allJaba;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * @param applicableUrls
   *          the applicableUrls to set
   */
  public void setApplicableUrls(String[] applicableUrls)
  {
    this.applicableUrls = applicableUrls;
  }

  /**
   * @param modifiable
   *          the modifiable to set
   */
  public void setModifiable(boolean modifiable)
  {
    this.modifiable = modifiable;
  }

  @Override
  public String[] getApplicableUrls()
  {
    return applicableUrls;
  }

  @Override
  public String getSourceFile()
  {
    return sourceFile;
  }

  @Override
  public void setSourceFile(String newfile)
  {
    sourceFile = newfile;
  }

  boolean modifiable = true;

  @Override
  public boolean isModifiable()
  {
    return modifiable;
  }

  @Override
  public List<ArgumentI> getArguments()
  {
    return JabaParamStore.getJwsArgsfromJaba(jabaArguments);
  }

  @Override
  public void setArguments(List<ArgumentI> args)
  {
    if (!allJaba(args))
    {
      throw new Error(MessageManager
              .getString("error.cannot_set_arguments_to_jabaws_param_set"));
    }
    jabaArguments = new ArrayList<Option>();
    for (ArgumentI rg : args)
    {
      jabaArguments.add(((JabaOption) rg).opt);
    }
  }

  public List<Option> getjabaArguments()
  {
    return jabaArguments;
  }

  public void setjabaArguments(List<Option> args)
  {
    jabaArguments = args;
  }

}
