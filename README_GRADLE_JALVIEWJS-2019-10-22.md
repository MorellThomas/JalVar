Last updated: 2019-10-22

# TRANSPILING USING ECLIPSE AS AN EXTERNAL CLI TOOL:


1) Download and install a clean eclipse jee-2019-09:

RECOMMEND using the eclipse installer found at https://www.eclipse.org/downloads/packages/installer
and installing in ~/buildtools/eclipse/jee-2019-09 (which is what the gradle.properties property jalviewjs_eclipse_root is set to by default).


2) Check the `jalviewjs_eclipse_root` property in `gradle.properties`:

If you have an eclipse 2019-06 or 2019-09 installed already elsewhere that you want to try and use then change the
```
jalviewjs_eclipse_root
```
property in gradle.properties to point to the folder that the application is installed in.  If you are on macOS this is the folder _above_ Eclipse.app (i.e. the folder Eclipse.app is in).
If on Windows or Linux then this can be the folder that the eclipse installer says it is installing too (usually something like `.../jee-2019-09`) (even though it then installs it to a folder `eclipse` under that) or the folder the eclipse binary and plugins folders etc are in (`build.gradle` will check both the folder stated in `jalviewjs_eclipse_root` and `${jalviewjs_eclipse_root}/eclipse`).

You can use a '~' as the first character which will get replaced with `System.getProperty("user.home")`.


3) That's it, but note the following:

i) Note that the gradle task (`jalviewjsEclipseCopyDropins`) will take care of copying the appropriate `net.sf.j2s.core.jar` and the `com.seeq.eclipse.importprojects.jar` into the `dropins` dir (tested on both unix or mac -- not tested on windows yet).

ii) Note that we have two versions of `net.sf.j2s.core.jar`, currently manifesting in two different `utils/jalviewjs/eclipse/dropins_4.1?` folders.  This is because the behaviour of eclipse changed between 2019-06 (4.12) and 2019-09 (4.13) that meant the `src` and `test` BuildContexts are separated during the Eclipse build.  This overwrites the list of classes to be transpiled with the set of classes from the last BuildContext to be compiled (which inevitablt seems to be tests).  The way the file paths are represented must also have changed so that the exclusion from `j2s.ecluded.paths` in the `.j2s` file are not honoured.
I have made a fixed version of `net.sf.j2s.core_3.2.4-FOR_4.13.jar` that ought to be backward compatible, but for unknown reason doesn't work in eclipse 4.12.
Hopefully this is temporary and Bob can incorporate a properly backward compatible fix.

iii) Note that the logs from the transpile go into `build/jalviewjs/j2s-transpile.out` which combines both stdout and stderr.


# CLI

gradle tasks possibly of interest to you:
```
gradle jalviewjs  # (should build the build/jalviewjs/site dir)

gradle jalviewjsSiteTar  # will produce build/distribution/site.tar.gz

gradle jalviewjsTranspile  # should run a new eclipse compile+transpile

gradle jalviewjsServer # will run a localhost http server to allow you to test the site in a browser.  Just use the URL given in the output.  To stop the server you have to do  gradle --stop  or you can just leave it running until the gradle daemon dies.
```

If it's working okay, you just need to to
```
gradle jalviewjs
gradle jalviewjsServer
```
and go to the localhost URL in the output of the jalviewjsServer task in your web browser (on a mac, just right clicking on the URL in terminal window gives an "Open URL" option which is nice and easy).


# IN ECLIPSE

## Setting up

If you are developing in Eclipse as IDE (which is the natural way to use Eclipse!) and have the plugin installed, then you will obviously not need to run the transpile as a gradle task -- it will be performed automatically.

Some things to remember:
i) You will need the buildship plugin (part of the JEE package).
ii) If you haven't already, remember to run on the CLI
```
gradle cleanEclipse eclipse jalviewjsCreateJ2sSettings
```
to create clean jalview `.classpath, .project, .settings/org.eclipse.jdt.core.prefs, .j2s` files.
You should only need to do this once.
Be aware that manual changes to these files will likely be overwritten during some gradle tasks.

If your Eclipse IDE doesn't already have the `net.sf.j2s.core.jar` plugin, you can run on CLI
```
gradle jalviewjsEclipseCopyDropins -Pjalviewjs_eclipse_root=/Applications
```
(replacing `/Applications` with the folder where your IDE Eclipse is installed).
You should also only need to do this once but you will have to restart Eclipse if it was already running.

## Building site

The important gradle tasks needed to build and test the site have been adapted/duplicated into the _jalview in eclipse_ group of tasks which you can find in the _Gradle Tasks_ window/tab.

To build the site, your Eclipse should be creating the transpilation continuously into `build/jalviewjs/tmp/site`.  If there are no transpiled files then you should Refresh, Gradle->Refresh Gradle Project, and if nothing has appeared you can Project->Clean which should trigger a rebuild.

You can clean the transpiled code with the gradle task `cleanJalviewjsIDESite`.

To then create the site from the transpiled code and supporting libraries, run the gradle task `jalviewjsIDEBuildSite` which will create a working (hopefully) site in `build/jalviewjs/site`

You can test the build right here in Eclipse, or in a browser of your choice, by running the `jalviewjsIDEServer` task (which is really just the `jalviewjsServer` task).  That will start a localhost server and create an access HTML file, `jalviewjsTest.html`.

You can open that in the Eclipse Internal Web Browser (you might need to Refresh the project to see it the first time you run the server task) by right clicking and Open With...->Web Browser, or you can open the file in your own web browser such as Firefox.
