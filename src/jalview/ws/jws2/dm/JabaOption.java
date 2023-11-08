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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import compbio.metadata.Option;
import jalview.util.MessageManager;
import jalview.ws.jws2.ParameterUtils;
import jalview.ws.params.OptionI;

public class JabaOption implements jalview.ws.params.OptionI
{
  public JabaOption(Option rg)
  {
    opt = rg;
  }

  Option opt;

  @Override
  public String getValue()
  {
    return opt.getDefaultValue();
  }

  @Override
  public String getName()
  {
    return opt.getName();
  }

  @Override
  public URL getFurtherDetails()
  {
    try
    {
      return new URL(opt.getBasicURL().toExternalForm() + "/"
              + opt.getFurtherDetails());
    } catch (NullPointerException q)
    {
    } catch (MalformedURLException q)
    {

    }
    return null;
  }

  @Override
  public boolean isRequired()
  {
    return opt.isRequired();
  }

  @Override
  public String getDescription()
  {
    return opt.getDescription();
  }

  @Override
  public List<String> getPossibleValues()
  {
    // TODO: assert typesafety
    return opt.getPossibleValues();
  }

  @Override
  public void setValue(String selectedItem)
  {
    try
    {
      opt.setDefaultValue(selectedItem);
    } catch (Exception e)
    {
      throw new IllegalArgumentException(MessageManager
              .formatMessage("error.invalid_value_for_option", new String[]
              { selectedItem, opt.getName() }));
    }
  }

  @Override
  public OptionI copy()
  {
    return new JabaOption(ParameterUtils.copyOption(opt));
  }

  /**
   * get the underlying Jaba option or parameter object. Note - use copy first
   * if you want to modify the value of the option.
   * 
   * @return
   */
  public Option getOption()
  {
    return opt;
  }

}
