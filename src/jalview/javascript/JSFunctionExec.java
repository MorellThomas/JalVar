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

import jalview.bin.JalviewLite;

import java.net.URL;
import java.util.Vector;

import netscape.javascript.JSObject;

public class JSFunctionExec implements Runnable
{
  public JalviewLite jvlite;

  public JSFunctionExec(JalviewLite applet)
  {
    jvlite = applet;

    jsExecQueue = jvlite.getJsExecQueue();
    jvlite.setExecutor(this);
  }

  private Vector jsExecQueue;

  private Thread executor = null;

  public void stopQueue()
  {
    if (jsExecQueue != null)
    {
      Vector<JSFunctionExec> q = null;
      synchronized (jsExecQueue)
      {
        q = jsExecQueue;
        jsExecQueue = null;
      }
      if (q != null)
      {
        for (JSFunctionExec jx : q)
        {
          jx.jvlite = null;

        }
        q.removeAllElements();
        synchronized (q)
        {
          q.notifyAll();
        }
      }
    }
    jvlite = null;
    executor = null;
  }

  @Override
  public void run()
  {
    while (jsExecQueue != null)
    {
      if (jsExecQueue.size() > 0)
      {
        Runnable r = (Runnable) jsExecQueue.elementAt(0);
        jsExecQueue.removeElementAt(0);
        try
        {
          r.run();
        } catch (Exception ex)
        {
          ex.printStackTrace();
        } catch (Error ex)
        {
          ex.printStackTrace();
        }
      }
      else
      {
        try
        {
          synchronized (jsExecQueue)
          {
            jsExecQueue.wait(1000);
          }
        } catch (Exception ex)
        {
        }
        ;
      }
    }

  }

  /**
   * execute a javascript callback synchronously
   * 
   * @param _listener
   * @param objects
   * @throws Exception
   */
  public void executeJavascriptFunction(final String _listener,
          final Object[] objects) throws Exception
  {
    executeJavascriptFunction(false, _listener, objects);
  }

  /**
   * execute a javascript callback synchronously or asynchronously
   * 
   * @param async
   *          - true to execute asynchronously (do this for gui events)
   * @param _listener
   *          - javascript function
   * @param objects
   *          - arguments
   * @throws Exception
   *           - only if call is synchronous
   */
  public void executeJavascriptFunction(final boolean async,
          final String _listener, Object[] arguments) throws Exception
  {

    executeJavascriptFunction(async, _listener, arguments, null);

  }

  public void executeJavascriptFunction(final boolean async,
          final String _listener, Object[] arguments, final String dbgMsg)
          throws Exception
  {
    final Object[] objects = new Object[arguments != null ? arguments.length
            : 0];
    if (arguments != null)
    {
      System.arraycopy(arguments, 0, objects, 0, arguments.length);
    }
    final Exception[] jsex = new Exception[1];
    Runnable exec = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          JSObject scriptObject = null;
          //try
          //{
            //scriptObject = JSObject.getWindow(jvlite);
          //} catch (Exception ex)
          //{
          //}
          //;
          if (scriptObject != null)
          {
            if (jvlite.debug && dbgMsg != null)
            {
              System.err.println(dbgMsg);
            }
            scriptObject.call(_listener, objects);
          }
        } catch (Exception jex)
        {
          // squash any malformedURLExceptions thrown by windows/safari
          if (!(jex instanceof java.net.MalformedURLException))
          {
            if (jvlite.debug)
            {
              System.err.println(jex);
            }
            if (jex instanceof netscape.javascript.JSException
                    && jvlite.jsfallbackEnabled)
            {
              jsex[0] = jex;
              if (jvlite.debug)
              {
                System.err.println("Falling back to javascript: url call");
              }
              StringBuffer sb = new StringBuffer(
                      "javascript:" + _listener + "(");
              for (int i = 0; objects != null && i < objects.length; i++)
              {
                if (i > 0)
                {
                  sb.append(",");
                }
                sb.append("\"");
                // strip out nulls and complex objects that we can't pass this
                // way.
                if (objects[i] != null && !(objects[i].getClass().getName()
                        .indexOf("jalview") == 0))
                {
                  sb.append(objects[i].toString());
                }
                sb.append("\"");
              }
              sb.append(")");
              if (jvlite.debug)
              {
                System.err.println(sb.toString());
              }
              // alternate
              URL url = null;
              try
              {
                url = new URL(sb.toString());
                jvlite.getAppletContext().showDocument(url);
                jex = null;
              } catch (Exception uex)
              {
                jex = uex;
              }
            }
            if (jex != null)
            {
              if (async)
              {
                jex.printStackTrace();
              }
              else
              {
                jsex[0] = jex;
              }
            }
            ;
          }

        }
      }
    };
    if (async)
    {
      if (executor == null)
      {
        executor = new Thread(new JSFunctionExec(jvlite));
        executor.start();
      }
      synchronized (jsExecQueue)
      {
        jsExecQueue.addElement(exec);
        jsExecQueue.notify();
      }
    }
    else
    {
      // wat for executor to notify us if it's running.
      exec.run();
      if (jsex[0] != null)
      {
        throw (jsex[0]);
      }
    }
  }

}
