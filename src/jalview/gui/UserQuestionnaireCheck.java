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
package jalview.gui;

import jalview.bin.Cache;
import jalview.bin.Console;
import jalview.util.MessageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;

public class UserQuestionnaireCheck implements Runnable
{
  /**
   * Implements the client side machinery for detecting a new questionnaire,
   * checking if the user has responded to an existing one, and prompting the
   * user for responding to a questionnaire. This is intended to work with the
   * perl CGI scripts checkresponder.pl and questionnaire.pl
   */
  String url = null;

  UserQuestionnaireCheck(String url)
  {
    if (url.indexOf("questionnaire.pl") == -1)
    {
      Console.error("'" + url
              + "' is an Invalid URL for the checkForQuestionnaire() method.\n"
              + "This argument is only for questionnaires derived from jalview's questionnaire.pl cgi interface.");
    }
    else
    {
      this.url = url;
    }
  }

  String qid = null, rid = null;

  private boolean checkresponse(URL qurl) throws Exception
  {
    Console.debug("Checking Response for : " + qurl);
    boolean prompt = false;
    // see if we have already responsed to this questionnaire or get a new
    // qid/rid pair
    BufferedReader br = new BufferedReader(
            new InputStreamReader(qurl.openStream()));
    String qresp;
    while ((qresp = br.readLine()) != null)
    {
      if (qresp.indexOf("NOTYET:") == 0)
      {
        prompt = true; // not yet responded under that ID
      }
      else
      {
        if (qresp.indexOf("QUESTIONNAIRE:") == 0)
        {
          // QUESTIONNAIRE:qid:rid for the latest questionnaire.
          int p = qresp.indexOf(':', 14);
          if (p > -1)
          {
            rid = null;
            qid = qresp.substring(14, p);
            if (p < (qresp.length() - 1))
            {
              rid = qresp.substring(p + 1);
              prompt = true; // this is a new qid/rid pair
            }
          }
        }
      }
    }
    return prompt;
  }

  public void run()
  {
    if (url == null)
    {
      return;
    }
    boolean prompt = false;
    try
    {
      // First - check to see if wee have an old questionnaire/response id pair.
      String lastq = Cache.getProperty("QUESTIONNAIRE");
      if (lastq == null)
      {
        prompt = checkresponse(new URL(url
                + (url.indexOf('?') > -1 ? "&" : "?") + "checkresponse=1"));
      }
      else
      {
        String qurl = url + (url.indexOf('?') > -1 ? "&" : "?")
                + "checkresponse=1";
        // query the server with the old qid/id pair
        String qqid = lastq.indexOf(':') > -1
                ? lastq.substring(0, lastq.indexOf(':'))
                : null;
        if (qqid != null && qqid != "null" && qqid.length() > 0)
        {
          qurl += "&qid=" + qqid;
          qid = qqid;
          String qrid = lastq.substring(lastq.indexOf(':') + 1); // retrieve
          // old rid
          if (qrid != null && !qrid.equals("null"))
          {
            rid = qrid;
            qurl += "&rid=" + qrid;
          }
        }
        // see if we have already responsed to this questionnaire.
        prompt = checkresponse(new URL(qurl));
      }
      if (qid != null && rid != null)
      {
        // Update our local property cache with latest qid and rid
        Cache.setProperty("QUESTIONNAIRE", qid + ":" + rid);
      }
      if (prompt)
      {
        String qurl = url + (url.indexOf('?') > -1 ? "&" : "?") + "qid="
                + qid + "&rid=" + rid;
        Console.info("Prompting user for questionnaire at " + qurl);
        int reply = JvOptionPane.showInternalConfirmDialog(Desktop.desktop,
                MessageManager.getString("label.jalview_new_questionnaire"),
                MessageManager.getString("label.jalview_user_survey"),
                JvOptionPane.YES_NO_OPTION, JvOptionPane.QUESTION_MESSAGE);

        if (reply == JvOptionPane.YES_OPTION)
        {
          Console.debug("Opening " + qurl);
          jalview.util.BrowserLauncher.openURL(qurl);
        }
      }
    } catch (Exception e)
    {
      Console.warn("When trying to access questionnaire URL " + url, e);
    }
  }

}
