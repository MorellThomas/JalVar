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
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.SequenceGroup;
import jalview.structure.SelectionSource;

public class JsSelectionSender extends JSFunctionExec
        implements jalview.structure.SelectionListener, JsCallBack
{

  AlignFrame _af;

  String _listener;

  public JsSelectionSender(JalviewLite jvlite, AlignFrame af,
          String listener)
  {
    super(jvlite);
    _af = af;
    _listener = listener;
  }

  @Override
  public void selection(SequenceGroup seqsel, ColumnSelection colsel,
          HiddenColumns hidden, SelectionSource source)
  {
    // System.err.println("Testing selection event relay to
    // jsfunction:"+_listener);
    try
    {
      String setid = "";
      AlignFrame src = _af;
      if (source != null)
      {
        if (source instanceof jalview.appletgui.AlignViewport
                && ((jalview.appletgui.AlignViewport) source).applet.currentAlignFrame.viewport == source)
        {
          // should be valid if it just generated an event!
          src = ((jalview.appletgui.AlignViewport) source).applet.currentAlignFrame;

        }
      }
      String[] seqs = new String[] {};
      String[] cols = new String[] {};
      int strt = 0, end = (src == null) ? -1
              : src.alignPanel.av.getAlignment().getWidth();
      if (seqsel != null && seqsel.getSize() > 0)
      {
        seqs = new String[seqsel.getSize()];
        for (int i = 0; i < seqs.length; i++)
        {
          seqs[i] = seqsel.getSequenceAt(i).getName();
        }
        if (strt < seqsel.getStartRes())
        {
          strt = seqsel.getStartRes();
        }
        if (end == -1 || end > seqsel.getEndRes())
        {
          end = seqsel.getEndRes();
        }
      }
      if (colsel != null && !colsel.isEmpty())
      {
        if (end == -1)
        {
          end = colsel.getMax() + 1;
        }
        cols = new String[colsel.getSelected().size()];
        for (int i = 0; i < cols.length; i++)
        {
          cols[i] = "" + (1 + colsel.getSelected().get(i).intValue());
        }
      }
      else
      {
        if (seqsel != null && seqsel.getSize() > 0)
        {
          // send a valid range, otherwise we send the empty selection
          cols = new String[2];
          cols[0] = "" + (1 + strt) + "-" + (1 + end);
        }
        ;

      }
      System.err.println("Relaying selection to jsfunction:" + _listener);
      executeJavascriptFunction(_listener,
              new Object[]
              { src, setid, jvlite.arrayToSeparatorList(seqs),
                  jvlite.arrayToSeparatorList(cols) });
    } catch (Exception ex)
    {
      System.err.println(
              "Jalview Javascript exec error: Couldn't send selection message using function '"
                      + _listener + "'");
      ex.printStackTrace();
      if (ex instanceof netscape.javascript.JSException)
      {
        System.err.println("Javascript Exception: "
                + ((netscape.javascript.JSException) ex).getCause()
                        .toString());
      }

    }
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
