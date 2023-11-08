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
package jalview.javascript;

import jalview.appletgui.AlignFrame;
import jalview.bin.JalviewLite;
import jalview.datamodel.SequenceI;
import jalview.structure.VamsasListener;
import jalview.structure.VamsasSource;

public class MouseOverListener extends JSFunctionExec
        implements VamsasListener, JsCallBack
{
  AlignFrame _af;

  String _listener;

  SequenceI last = null;

  int i = -1;

  @Override
  public void mouseOverSequence(SequenceI seq, int index,
          VamsasSource source)
  {
    if (seq != last || i != index)
    {
      // this should really be a trace message.
      // Cache.debug("Mouse over " + v.getId() + " bound to "
      // + seq + " at " + index);
      last = seq;
      i = index;
      AlignFrame src = null;
      try
      {
        if (source != null)
        {
          if (source instanceof jalview.appletgui.AlignViewport
                  && ((jalview.appletgui.AlignViewport) source).applet.currentAlignFrame.viewport == source)
          {
            // should be valid if it just generated an event!
            src = ((jalview.appletgui.AlignViewport) source).applet.currentAlignFrame;

          }
          // TODO: ensure that if '_af' is specified along with a handler
          // function, then only events from that alignFrame are sent to that
          // function
        }
        executeJavascriptFunction(_listener,
                new Object[]
                { src, seq.getDisplayId(false), "" + (1 + i),
                    "" + seq.findPosition(i) });
      } catch (Exception ex)
      {

        System.err.println(
                "JalviewLite javascript error: Couldn't send mouseOver with handler '"
                        + _listener + "'");
        if (ex instanceof netscape.javascript.JSException)
        {
          System.err.println("Javascript Exception: "
                  + ((netscape.javascript.JSException) ex).getMessage());
        }
        ex.printStackTrace();
      }
    }
  }

  public MouseOverListener(JalviewLite applet, AlignFrame af,
          String listener)
  {
    super(applet);
    _af = af;
    _listener = listener;
  }

  @Override
  public AlignFrame getAlignFrame()
  {
    return _af;
  }

  @Override
  public String getListenerFunction()
  {
    return _listener;
  }

}
