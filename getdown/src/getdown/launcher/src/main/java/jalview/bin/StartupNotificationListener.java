package jalview.bin;

import com.threerings.getdown.launcher.GetdownApp;
import com.threerings.getdown.launcher.StatusPanel;
import static com.threerings.getdown.Log.log;

public class StartupNotificationListener {

  public static void setListener() {

    
    try {
      com.install4j.api.launcher.StartupNotification.registerStartupListener(
        new com.install4j.api.launcher.StartupNotification.Listener() {
          @Override
          public void startupPerformed(String parameters) { 
            log.info("StartupNotification.Listener.startupPerformed: '"+parameters+"'");
            GetdownApp.setStartupFilesParameterString(parameters);
          }
        }
      );
    } catch (Exception e) {
      e.printStackTrace();
    } catch (NoClassDefFoundError t) {
      log.warning("Starting without install4j classes");
    }

  }

}
