/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.DataTrackSupport;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.*;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * This is the default Tracker application.
 *
 * @author Douglas Brown
 */
public class Tracker {

    static {
        XML.setLoader(Preferences.class, new Preferences.Loader());
    }

    // define static constants
    /**
     * tracker version and copyright
     */
    public static final String VERSION = "5.2.0.0";
    public static final String COPYRIGHT = "Copyright (c) 2018 Douglas Brown, Nikolai Avdeev";
    /**
     * the tracker icon
     */
    public static final ImageIcon TRACKER_ICON = new ImageIcon(Tracker.class.getResource("/images/tracker_icon_32.png"));
    /**
     * a larger tracker icon
     */
    public static final ImageIcon TRACKER_ICON_256 = new ImageIcon(Tracker.class.getResource("/images/tracker_icon_256.png"));

    public static final String THETA = TeXParser.parseTeX("$\\theta");
    static final String OMEGA = TeXParser.parseTeX("$\\omega");
    static final String ALPHA = TeXParser.parseTeX("$\\alpha");
    static final String DEGREES = "°";
    static final String SQUARED = "\u00b2";
    static final String DOT = "\u00b7";
    static final Level DEFAULT_LOG_LEVEL = ConsoleLevel.OUT_CONSOLE;

    // for testing
    static boolean timeLogEnabled = false;
    static boolean testOn = false;

    // define static fields
    static String trackerHome;
    static String[] fullConfig =
            {"file.new", "file.open", "file.close",
                    "file.import", "file.export", "file.save",
                    "file.saveAs", "file.print", "file.library",
                    "edit.copyObject", "edit.copyData", "edit.copyImage",
                    "edit.paste", "edit.matSize",
                    "edit.clear", "edit.undoRedo", "video.import",
                    "video.close", "video.visible", "video.filters",
                    "pageView.edit", "notes.edit", "new.pointMass",
                    "new.cm", "new.vector", "new.vectorSum",
                    "new.lineProfile", "new.RGBRegion",
                    "new.analyticParticle", "new.clone", "new.circleFitter",
                    "new.dynamicParticle", "new.dynamicTwoBody",
                    "new.dataTrack", "new.tapeMeasure", "new.protractor",
                    "calibration.stick", "calibration.tape",
                    "calibration.points", "calibration.offsetOrigin",
                    "track.name", "track.description",
                    "track.color", "track.footprint",
                    "track.visible", "track.locked",
                    "track.delete", "track.autoAdvance",
                    "track.markByDefault", "track.autotrack",
                    "model.stamp", "help.diagnostics", "coords.locked",
                    "coords.origin", "coords.angle", "data.algorithm",
                    "coords.scale", "coords.refFrame", "button.x",
                    "button.v", "button.a", "button.trails",
                    "button.labels", "button.stretch", "button.clipSettings",
                    "button.xMass", "button.axes", "button.path", "button.drawing",
                    "number.formats", "number.units", "text.columns", "plot.compare",
                    "config.saveWithData", "data.builder", "data.tool"};
    static Set<String> defaultConfig;
    static ImageIcon trackerLogoIcon = new ImageIcon(Tracker.class.getResource("/images/tracker_logo.png"));
    static String[] mainArgs;
    static JFrame splash;
    static JProgressBar progressBar;
    static String counterPath = "http://physlets.org/tracker/counter/counter.php?";
    static String newerVersion; // new version available if non-null
    static boolean checkedForNewerVersion = false; // true if checked for new version
    static String trackerWebsite = "physlets.org/tracker";
    static String author = "Douglas Brown";
    static String osp = "Open Source Physics";
    static AbstractAction aboutFFMPegAction, aboutThreadsAction;
    static Action aboutTrackerAction, readmeAction;
    static Action aboutJavaAction, startLogAction, trackerPrefsAction;
    private static Tracker sharedTracker;
    static String readmeFileName = "tracker_README.txt";
    static JDialog readmeDialog, startLogDialog, trackerPrefsDialog;
    static JTextArea trackerPrefsTextArea;
    static String prefsPath;
    public static String rootXMLPath = ""; // path to root directory of trk files 
    static Cursor zoomInCursor, zoomOutCursor, grabCursor;
    public static boolean showHints = true;
    static boolean startupHintShown;
    static String pdfHelpPath = "/tracker_help.pdf";
    static JButton pdfHelpButton;
    static final ArrayList<String> recentFiles = new ArrayList<>();
    static int minimumMemorySize = 32;
    static int requestedMemorySize = -1, originalMemoryRequest = 0;
    static long lastMillisChecked;
    static int maxFontLevel = 6;
    protected static Locale[] locales;
    protected static Object[][] incompleteLocales;
    static Locale defaultLocale;
    static ArrayList<String> checkForUpgradeChoices;
    static Map<String, Integer> checkForUpgradeIntervals;
    static Collection<String> dataFunctionControlStrings = new HashSet<>();
    static Collection<String> initialAutoloadSearchPaths = new TreeSet<>();
    static Map<String, ArrayList<XMLControl>> dataFunctionControls = new TreeMap<>();
    static java.io.FileFilter xmlFilter;
    static Registry registry; // used for RMI communication with EJS
    static DataTrackTool dataTrackTool; // used for RMI communication with EJS

    // user-settable preferences saved/loaded by Preferences class
    static Level preferredLogLevel = DEFAULT_LOG_LEVEL;
    static boolean showHintsByDefault = true;
    static int recentFilesSize = 6;
    static int preferredMemorySize = -1;
    static String lookAndFeel, preferredLocale, preferredDecimalSeparator, additionalDecimalSeparators;
    static String preferredJRE, preferredTrackerJar, preferredPointMassFootprint;
    static int checkForUpgradeInterval = 0;
    static int preferredFontLevel = 0, preferredFontLevelPlus = 0;
    static boolean isRadians, isVideoFast;
    static boolean warnFFMPegError = true, warnNoVideoEngine = true;
    static boolean warnVariableDuration = true;
    static String[] prelaunchExecutables = new String[0];
    static Map<String, String[]> autoloadMap = new TreeMap<>();
    static String[] preferredAutoloadSearchPaths;
    static boolean markAtCurrentFrame = true;
    static boolean scrubMouseWheel, centerCalibrationStick, enableAutofill, showGaps, hideLabels;
    static int trailLengthIndex = TToolBar.trailLengths.length - 2;

    // the only instance field!
    private TFrame frame;

    static {
        defaultLocale = Locale.getDefault();
        trackerHome = System.getenv("TRACKER_HOME");
        if (trackerHome == null) {
            try {
                trackerHome = TrackerStarter.findTrackerHome(false);
            } catch (Exception ignored) {
            }
        }
        // set system properties for Mac OSX look and feel

        // get logo icons with ResourceLoader so launch jar file is identified

        // create grab cursor
        Image grab = ResourceLoader.getImage("resources/images/grab.gif");
        grabCursor = GUIUtils.createCustomCursor(grab, new Point(14, 10), "Grab", Cursor.HAND_CURSOR);

        // create static objects AFTER they are defined above
        locales = new Locale[]{
                Locale.ENGLISH,
                new Locale("ar"), // arabic 
                new Locale("cs"), // czech 
                new Locale("da"), // danish 
                Locale.GERMAN,
                new Locale("el", "GR"), // greek 
                new Locale("es"), // spanish 
                new Locale("fi"), // finnish 
                Locale.FRENCH,
                new Locale("hu", "HU"), // hungarian 
                new Locale("in"), // indonesian 
                Locale.ITALIAN,
                new Locale("iw", "IL"), // hebrew 
                new Locale("ko"), // korean 
                new Locale("ms", "MY"), // malaysian 
                new Locale("nl", "NL"), // dutch 
                new Locale("pl"), // polish 
                new Locale("pt", "BR"), // Brazil portuguese 
                new Locale("pt", "PT"), // Portugal portuguese 
                new Locale("sk"), // slovak 
                new Locale("sl"), // slovenian 
                new Locale("sv"), // swedish 
                new Locale("th", "TH"), // Thailand thai 
                new Locale("tr"), // turkish 
                new Locale("vi", "VN"), // vietnamese 
                Locale.CHINA, // simplified chinese
                Locale.TAIWAN}; // traditional chinese

        // pig last updated March 2018
        incompleteLocales = new Object[][]{
                {new Locale("cs"), "2013"}, // czech
                {new Locale("fi"), "2013"}, // finnish
                {new Locale("sk"), "2011"}, // slovak
                {new Locale("in"), "2013"}};// indonesian

        setDefaultConfig(getFullConfig());
        loadPreferences();
        // load current version after a delay to allow video engines to load
        // and every 24 hours thereafter (if program is left running)
        Timer timer = new Timer(86400000, e -> {
            Runnable runner = () -> {
                checkedForNewerVersion = false;
                loadCurrentVersion(false, true);
            };
            Thread opener = new Thread(runner);
            opener.setPriority(Thread.NORM_PRIORITY);
            opener.setDaemon(true);
            opener.start();
        });
        timer.setInitialDelay(10000);
        timer.setRepeats(true);
        timer.start();

        xmlFilter = f -> {
            if (f == null || f.isDirectory()) {
                return false;
            }
            String ext = XML.getExtension(f.getName());
            return "xml".equalsIgnoreCase(ext);
        };
        autoloadDataFunctions();

        // check for upgrade intervals
        checkForUpgradeChoices = new ArrayList<>();
        checkForUpgradeIntervals = new HashMap<>();
        String s = "PrefsDialog.Upgrades.Always";
        checkForUpgradeChoices.add(s);
        checkForUpgradeIntervals.put(s, 0);
        s = "PrefsDialog.Upgrades.Weekly";
        checkForUpgradeChoices.add(s);
        checkForUpgradeIntervals.put(s, 7);
        s = "PrefsDialog.Upgrades.Monthly";
        checkForUpgradeChoices.add(s);
        checkForUpgradeIntervals.put(s, 30);
        s = "PrefsDialog.Upgrades.Never";
        checkForUpgradeChoices.add(s);
        checkForUpgradeIntervals.put(s, 10000);


        // create splash frame
        Color darkred = new Color(153, 0, 0);
        Color darkblue = new Color(51, 51, 102);
        Color grayblue = new Color(116, 147, 179);
        Color darkgrayblue = new Color(83, 105, 128);
        Color lightblue = new Color(169, 193, 217);
        Color background = new Color(250, 250, 230);
        splash = new JFrame("Tracker"); // name shown on task bar
        splash.setIconImage(TRACKER_ICON.getImage()); // icon shown on task bar
        splash.setUndecorated(true);
        splash.setAlwaysOnTop(true);
        splash.setResizable(false);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(background);
        contentPane.setBorder(BorderFactory.createBevelBorder(
                BevelBorder.RAISED, grayblue, darkgrayblue));
        splash.setContentPane(contentPane);
        MouseInputAdapter splashMouseListener = new MouseInputAdapter() {
            Point mouseLoc;
            Point splashLoc;

            public void mousePressed(MouseEvent e) {
                splashLoc = splash.getLocation(); // original screen position of splash
                mouseLoc = e.getPoint(); // original screen position of mouse
                mouseLoc.x += splashLoc.x;
                mouseLoc.y += splashLoc.y;
            }

            public void mouseDragged(MouseEvent e) {
                Point loc = splash.getLocation();
                loc.x += e.getPoint().x;
                loc.y += e.getPoint().y;
                splash.setLocation(splashLoc.x + loc.x - mouseLoc.x, splashLoc.y + loc.y - mouseLoc.y);
            }
        };
        contentPane.addMouseListener(splashMouseListener);
        contentPane.addMouseMotionListener(splashMouseListener);

        // tracker logo north
        JLabel trackerLogoLabel = new JLabel(trackerLogoIcon);
        trackerLogoLabel.setBorder(BorderFactory.createEmptyBorder(12, 24, 4, 24));
        contentPane.add(trackerLogoLabel, BorderLayout.NORTH);

        // tip of the day and progress bar in the center
        String tip = TrackerRes.getString("Tracker.Splash.HelpMessage");
        tip += " " + TrackerRes.getString("TMenuBar.Menu.Help");
        tip += "|" + TrackerRes.getString("TMenuBar.MenuItem.GettingStarted");
        JLabel helpLabel = new JLabel(tip);
        helpLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        Font font = helpLabel.getFont().deriveFont(Font.PLAIN).deriveFont(14f);
        helpLabel.setFont(font);
        helpLabel.setForeground(darkred);
        helpLabel.setAlignmentX(0.5f);
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(12, 50, 16, 50));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.setOpaque(false);
        Box center = Box.createVerticalBox();
        center.add(helpLabel);
        center.add(progressPanel);
        contentPane.add(center, BorderLayout.CENTER);

        // version south
        String vers = author + "   " + osp + "   Ver " + VERSION;
        if (testOn) vers += " BETA";
        JLabel versionLabel = new JLabel(vers);
        versionLabel.setForeground(darkblue);
        font = font.deriveFont(Font.BOLD).deriveFont(10f);
        versionLabel.setFont(font);
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        versionLabel.setOpaque(false);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        JPanel versionPanel = new JPanel(new BorderLayout());
        versionPanel.setBackground(new Color(212, 230, 247));
        versionPanel.add(versionLabel, BorderLayout.CENTER);
        versionPanel.setBorder(BorderFactory.createLineBorder(lightblue));
        contentPane.add(versionPanel, BorderLayout.SOUTH);

        splash.pack();
        Dimension size = splash.getSize();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = dim.width / 2;
        int y = 3 * dim.height / 5;  // below center
        splash.setLocation(x - size.width / 2, y - size.height / 2);

        // set up videos extensions to extract from jars
        // this list should agree with ffmpeg video types below
        for (String ext : VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
            ResourceLoader.addExtractExtension(ext);
        }

        // add FFMPeg video types, if available, using reflection
        try {
            String ffmpegIOName = "org.opensourcephysics.media.ffmpeg.FFMPegIO";
            Class<?> ffmpegIOClass = Class.forName(ffmpegIOName);
            Method method = ffmpegIOClass.getMethod("registerWithVideoIO", (Class[]) null);
            method.invoke(null, (Object[]) null);
        } catch (Exception ignored) {
        }

        VideoIO.setDefaultXMLExtension("trk");

        // create pdf help button
        pdfHelpButton = new JButton(TrackerRes.getString("Tracker.Button.PDFHelp"));
        pdfHelpButton.addActionListener(e -> {
            try {
                URL url = new URL("https://" + trackerWebsite + pdfHelpPath);
                org.opensourcephysics.desktop.OSPDesktop.displayURL(url.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // find Java VMs in background thread so they are ready when needed
        Runnable runner = () -> JREFinder.getFinder().getJREs(32);
        new Thread(runner).start();
    }

    /**
     * Gets the shared Tracker for single-VM use.
     *
     * @return the shared Tracker
     */
    public static Tracker getTracker() {
        if (sharedTracker == null) {
            OSPLog.fine("creating shared Tracker");
            sharedTracker = new Tracker(null, false, false);
        }
        return sharedTracker;
    }

    /**
     * Constructs Tracker with a blank tab and splash.
     */
    public Tracker() {
        this(null, true, true);
    }

    /**
     * Constructs Tracker with a video.
     *
     * @param video the video
     */
    public Tracker(Video video) {
        createFrame();
        // add a tracker panel with the video
        TrackerPanel trackerPanel = new TrackerPanel(video);
        frame.addTab(trackerPanel);
    }

    /**
     * Constructs Tracker and loads the named xml files.
     *
     * @param names an array of xml, video or zip file names
     */
    private Tracker(String[] names, boolean addTabIfEmpty, boolean showSplash) {
        // set font level resize and center splash frame
        FontSizer.setFonts(splash, FontSizer.getLevel());
        splash.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - splash.getBounds().width) / 2;
        int y = (dim.height - splash.getBounds().height) / 2;
        splash.setLocation(x, y);
        splash.setVisible(showSplash);
        createFrame();
        Tracker.setProgress(5);
        if (names != null) {
            // parse file names
            for (String name : names) {
                if (name == null) continue;
                // set default root path to path of first .trk or .trz file opened
                if ((name.endsWith(".trk") || name.endsWith(".trz"))
                        && name.contains("/")
                        && rootXMLPath.equals("")) {
                    rootXMLPath = name.substring(0, name.lastIndexOf("/") + 1);
                    OSPLog.fine("Setting rootPath: " + rootXMLPath);
                }
                TrackerIO.open(name, frame);
            }
        }
        // add an empty tab if requested
        else if (addTabIfEmpty) {
            TrackerPanel trackerPanel = new TrackerPanel();
            frame.addTab(trackerPanel);
            JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
            pane.setDividerLocation(frame.defaultRightDivider);
            if (showHints) {
                startupHintShown = true;
                trackerPanel.setMessage(TrackerRes.getString("Tracker.Startup.Hint"));
            }
            Tracker.setProgress(100);
        }
    }

    /**
     * Gets the frame.
     *
     * @return the frame
     */
    public TFrame getFrame() {
        return frame;
    }

    /**
     * Creates the TFrame.
     */
    private void createFrame() {
        // create actions
        createActions();
        Tracker.setProgress(5);
        OSPRuntime.setLookAndFeel(true, lookAndFeel);
        frame = new TFrame();
        Diagnostics.setDialogOwner(frame);
        DiagnosticsForFFMPeg.setDialogOwner(frame);
        // set up the Java VM exit mechanism when used as application
        if (org.opensourcephysics.display.OSPRuntime.applet == null) {
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // save preferences, but first clean up autoloadMap
                    ArrayList<String> dirs = new ArrayList<>();
                    if (preferredAutoloadSearchPaths != null) {
                        dirs.addAll(Arrays.asList(preferredAutoloadSearchPaths));
                    } else dirs.addAll(getDefaultAutoloadSearchPaths());

                    for (Iterator<String> it = autoloadMap.keySet().iterator(); it.hasNext(); ) {
                        String filePath = it.next();
                        String parentPath = XML.getDirectoryPath(filePath);
                        boolean keep = false;
                        for (String dir : dirs) {
                            keep = keep || parentPath.equals(dir);
                        }
                        if (!keep || !new File(filePath).exists()) {
                            it.remove();
                        }
                    }
                    savePreferences();
                    if (frame.libraryBrowser != null) {
                        boolean canceled = !frame.libraryBrowser.exit();
                        if (canceled) {
                            // exiting is canceled so temporarily change close operation
                            // to DO_NOTHING and return
                            final int op = frame.getDefaultCloseOperation();
                            final boolean exit = frame.wishesToExit();
                            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            Runnable runner = () -> {
                                if (exit) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                frame.setDefaultCloseOperation(op);
                            };
                            EventQueue.invokeLater(runner);
                            return;
                        }
                    }
                    // remove all tabs
                    for (int i = frame.getTabCount() - 1; i >= 0; i--) {
                        // save/close tabs in try/catch block so always closes
                        try {
                            if (!frame.getTrackerPanel(i).save()) {
                                // exiting is canceled so temporarily change close operation
                                // to DO_NOTHING and return
                                final int op = frame.getDefaultCloseOperation();
                                final boolean exit = frame.wishesToExit();
                                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                                Runnable runner = () -> {
                                    if (exit) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                    frame.setDefaultCloseOperation(op);
                                };
                                EventQueue.invokeLater(runner);
                                return;
                            }
                            frame.removeTab(frame.getTrackerPanel(i));
                        } catch (Exception ignored) {
                        }
                    }

                    // hide the frame
                    frame.setVisible(false);

                    // unregister the DataTrackTool and inform RMI clients
                    dataTrackTool.trackerExiting();
                    unregisterRemoteTool(dataTrackTool);

                    // exit the system if frame wishes to exit
                    if (frame.wishesToExit() &&
                            frame.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE) {
                        System.exit(0);
                    }
                }
            });
        }
    }

//________________________________  static methods ____________________________

    /**
     * Compares version strings.
     *
     * @param ver1 version 1
     * @param ver2 version 2
     * @return 0 if equal, 1 if ver1>ver2, -1 if ver1<ver2
     */
    public static int compareVersions(String ver1, String ver2) {
        try {
            // deal with null values
            if (ver1 == null || ver2 == null) {
                return 0;
            }
            // typical newer semantic version "4.9.10" or 5.0.0.171230
            // typical older version "4.97"
            String[] v1 = ver1.trim().split("\\.");
            String[] v2 = ver2.trim().split("\\.");
            // beta version arrays have length 4
            // newer semantic version arrays have length 3
            // older version arrays have length 2

            // truncate beta versions to length 3
            if (v1.length == 4) {
                v1 = new String[]{v1[0], v1[1], v1[2]};
            }
            if (v2.length == 4) {
                v2 = new String[]{v2[0], v2[1], v2[2]};
            }

            if (v2.length > v1.length) {
                // v1 is older version, v2 is newer
                return -1;
            }
            if (v1.length > v2.length) {
                // v2 is older version, v1 is newer
                return 1;
            }
            // both arrays have the same length
            for (int i = 0; i < v1.length; i++) {
                if (Integer.parseInt(v1[i]) < Integer.parseInt(v2[i])) {
                    return -1;
                } else if (Integer.parseInt(v1[i]) > Integer.parseInt(v2[i])) {
                    return 1;
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * Shows the About Tracker dialog.
     */
    public static void showAboutTracker() {
        String newline = System.getProperty("line.separator", "\n");
        String vers = Tracker.VERSION;
        // typical beta version 4.10.0170514
        if (testOn) vers += " BETA";
        String date = OSPRuntime.getLaunchJarBuildDate();
        if (date != null)
            vers = vers + "   " + date;
        String aboutString = "Tracker "
                + vers + newline
                + Tracker.COPYRIGHT + newline
                + "https://" + Tracker.trackerWebsite + newline + newline
                + TrackerRes.getString("Tracker.About.ProjectOf") + newline
                + "Open Source Physics" + newline
                + "www.opensourcephysics.org" + newline;
        String translator = TrackerRes.getString("Tracker.About.Translator");
        if (!translator.equals("")) {
            aboutString += newline + TrackerRes.getString("Tracker.About.TranslationBy")
                    + " " + translator + newline;
        }
        if (Tracker.trackerHome != null) {
            aboutString += newline + TrackerRes.getString("Tracker.About.TrackerHome")
                    + newline + Tracker.trackerHome + newline;
        }
        loadCurrentVersion(true, false);
        if (newerVersion != null) {
            aboutString += newline + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1")
                    + " " + newerVersion + " "
                    + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2")
                    + newline + "https://" + trackerWebsite + newline;
        } else {
            aboutString += newline + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message");
        }
        JOptionPane.showMessageDialog(null,
                aboutString,
                TrackerRes.getString("Tracker.Dialog.AboutTracker.Title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Finds data functions in all DataBuilder XMLControl files found in a specified directory.
     * This returns a map for which the keys are names of DataBuilder xml files and the values
     * are lists of data functions as String[] {function name, expression, tracktype}
     *
     * @param dirPath the directory path
     * @return map of file name to list of data functions
     */
    public static Map<String, ArrayList<String[]>> findDataFunctions(String dirPath) {
        Map<String, ArrayList<String[]>> results = new TreeMap<>();
        if (dirPath == null) return results;

        File dir = new File(dirPath);
        if (!dir.exists()) return results;

        File[] files = dir.listFiles(xmlFilter);
        if (files != null) {
            for (File file : files) {
                XMLControl control = new XMLControlElement(file.getPath());
                if (control.failedToRead()) {
                    continue;
                }

                Class<?> type = control.getObjectClass();
                if (type != null && TrackDataBuilder.class.isAssignableFrom(type)) {
                    ArrayList<String[]> expandedFunctions = new ArrayList<>();

                    // look through XMLControl for data functions
                    for (Object next : control.getPropertyContent()) {
                        if (next instanceof XMLProperty
                                && ((XMLProperty) next).getPropertyName().equals("functions")) {
                            // found DataFunctionPanels
                            XMLControl[] panels = ((XMLProperty) next).getChildControls();
                            for (XMLControl panelControl : panels) {
                                String trackType = panelControl.getString("description");
                                @SuppressWarnings("unchecked")
                                ArrayList<String[]> functions = (ArrayList<String[]>) panelControl.getObject("functions");
                                if (trackType == null || functions == null || functions.isEmpty()) continue;

                                // add localized trackType name to function arrays
                                for (String[] f : functions) {
                                    String[] data = new String[3];
                                    System.arraycopy(f, 0, data, 0, 2);
                                    // use XML.getExtension method to get short name of track type
                                    String trackName = XML.getExtension(trackType);
                                    String localized = TrackerRes.getString(trackName + ".Name");
                                    if (!localized.startsWith("!"))
                                        trackName = localized;
                                    data[2] = trackName;
                                    expandedFunctions.add(data);
                                }
                            } // end inner loop
                        }
                    } // end outer loop

                    // add entry to the results map
                    results.put(file.getName(), expandedFunctions);
                }

            }
        }
        return results;
    }

    /**
     * Creates the actions.
     */
    protected static void createActions() {
        // about Tracker
        aboutTrackerAction = new AbstractAction(
                TrackerRes.getString("Tracker.Action.AboutTracker"), null) {
            public void actionPerformed(ActionEvent e) {
                showAboutTracker();
            }
        };
        // Tracker README
        readmeAction = new AbstractAction(
                TrackerRes.getString("Tracker.Readme") + "...", null) {
            public void actionPerformed(ActionEvent e) {
                if (readmeDialog == null && Tracker.trackerHome != null) {
                    String slash = System.getProperty("file.separator", "/");
                    String path = Tracker.trackerHome + slash + readmeFileName;
                    if (OSPRuntime.isMac()) {
                        String dir = new File(Tracker.trackerHome).getParent();
                        path = dir + slash + readmeFileName;
                    }
                    String s = ResourceLoader.getString(path);
                    if (s == null || "".equals(s)) {
                        s = TrackerRes.getString("Tracker.Readme.NotFound") + ": " + path;
                        JOptionPane.showMessageDialog(null, s,
                                TrackerRes.getString("Tracker.Readme.NotFound"),
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    readmeDialog = new JDialog((Frame) null, true);
                    readmeDialog.setTitle(TrackerRes.getString("Tracker.Readme"));
                    JTextArea textPane = new JTextArea();
                    textPane.setEditable(false);
                    textPane.setTabSize(2);
                    textPane.setLineWrap(true);
                    textPane.setWrapStyleWord(true);
                    JScrollPane scroller = new JScrollPane(textPane);
                    readmeDialog.setContentPane(scroller);
                    textPane.setText(s);
                    textPane.setCaretPosition(0);
                    readmeDialog.setSize(600, 600);
                    FontSizer.setFonts(readmeDialog, FontSizer.getLevel());
                    // center on screen
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = (dim.width - readmeDialog.getBounds().width) / 2;
                    int y = (dim.height - readmeDialog.getBounds().height) / 2;
                    readmeDialog.setLocation(x, y);
                }
                assert readmeDialog != null;
                readmeDialog.setVisible(true);
            }
        };

        // Start log
        final String startLogPath = System.getenv("START_LOG");
        if (startLogPath != null) {
            startLogAction = new AbstractAction(
                    TrackerRes.getString("Tracker.StartLog") + "...", null) {
                public void actionPerformed(ActionEvent e) {
                    if (startLogDialog == null) {
                        String s = ResourceLoader.getString(startLogPath);
                        if (s == null || "".equals(s)) {
                            s = TrackerRes.getString("Tracker.StartLog.NotFound") + ": " + startLogPath;
                            JOptionPane.showMessageDialog(null, s,
                                    TrackerRes.getString("Tracker.startLogPath.NotFound"),
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        startLogDialog = new JDialog((Frame) null, true);
                        startLogDialog.setTitle(TrackerRes.getString("Tracker.StartLog"));
                        JTextArea textPane = new JTextArea();
                        textPane.setEditable(false);
                        textPane.setTabSize(2);
                        textPane.setLineWrap(true);
                        textPane.setWrapStyleWord(true);
                        JScrollPane scroller = new JScrollPane(textPane);
                        startLogDialog.setContentPane(scroller);
                        textPane.setText(s);
                        textPane.setCaretPosition(0);
                        FontSizer.setFonts(startLogDialog, FontSizer.getLevel());
                        startLogDialog.setSize(600, 600);
                        // center on screen
                        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (dim.width - startLogDialog.getBounds().width) / 2;
                        int y = (dim.height - startLogDialog.getBounds().height) / 2;
                        startLogDialog.setLocation(x, y);
                    }
                    startLogDialog.setVisible(true);
                }
            };
        }

        if (prefsPath != null) {
            trackerPrefsAction = new AbstractAction(
                    TrackerRes.getString("Tracker.Prefs.MenuItem.Text") + "...", null) {
                public void actionPerformed(ActionEvent e) {
                    if (trackerPrefsDialog == null) {
                        String s = ResourceLoader.getString(prefsPath);
                        if (s == null || "".equals(s)) {
                            s = TrackerRes.getString("Tracker.Prefs.NotFound") + ": " + prefsPath;
                            JOptionPane.showMessageDialog(null, s,
                                    TrackerRes.getString("Tracker.Prefs.NotFound"),
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        trackerPrefsDialog = new JDialog((Frame) null, true);
                        trackerPrefsDialog.setTitle(TrackerRes.getString("ConfigInspector.Title") + ": " +
                                XML.forwardSlash(Tracker.prefsPath));
                        trackerPrefsTextArea = new JTextArea();
                        trackerPrefsTextArea.setEditable(false);
                        trackerPrefsTextArea.setTabSize(2);
                        trackerPrefsTextArea.setLineWrap(true);
                        trackerPrefsTextArea.setWrapStyleWord(true);
                        JScrollPane scroller = new JScrollPane(trackerPrefsTextArea);
                        trackerPrefsDialog.setContentPane(scroller);
                        trackerPrefsTextArea.setText(s);
                        trackerPrefsTextArea.setCaretPosition(0);
                        FontSizer.setFonts(trackerPrefsDialog, FontSizer.getLevel());
                        trackerPrefsDialog.setSize(800, 400);
                        // center on screen
                        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (dim.width - trackerPrefsDialog.getBounds().width) / 2;
                        int y = (dim.height - trackerPrefsDialog.getBounds().height) / 2;
                        trackerPrefsDialog.setLocation(x, y);
                    } else {
                        String s = ResourceLoader.getString(prefsPath);
                        trackerPrefsTextArea.setText(s);
                        trackerPrefsTextArea.setCaretPosition(0);
                    }
                    trackerPrefsDialog.setVisible(true);
                }
            };
        }

        // about Java
        aboutJavaAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutJava"), null) {
            public void actionPerformed(ActionEvent e) {
                Diagnostics.aboutJava();
            }
        };
        aboutFFMPegAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutFFMPeg"), null) {
            public void actionPerformed(ActionEvent e) {
                DiagnosticsForFFMPeg.aboutFFMPeg("Tracker");
            }
        };
        aboutThreadsAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutThreads"), null) {
            public void actionPerformed(ActionEvent e) {
                DiagnosticsForThreads.aboutThreads();
            }
        };
    }


    /**
     * Gets the full set of configuration properties.
     *
     * @return the full configuration set
     */
    protected static Set<String> getFullConfig() {
        return new TreeSet<>(Arrays.asList(fullConfig));
    }

    /**
     * Gets the default set of configuration properties.
     *
     * @return the default configuration set
     */
    protected static Set<String> getDefaultConfig() {
        if (defaultConfig == null)
            defaultConfig = getFullConfig();
        return new TreeSet<>(defaultConfig);
    }

    /**
     * Sets the default set of configuration properties.
     *
     * @param config a set of configuration properties
     */
    protected static void setDefaultConfig(Set<String> config) {
        if (defaultConfig == null)
            defaultConfig = new TreeSet<>();
        defaultConfig.clear();
        defaultConfig.addAll(config);
    }

    /**
     * Autoloads data functions found in the user home and code base directories.
     * This loads DataFunctionPanel XMLControls into a static collection that is
     * accessed when need by DataBuilder.
     */
    protected static void autoloadDataFunctions() {
        dataFunctionControls.clear();
        for (String dirPath : getInitialSearchPaths()) {
            if (dirPath == null) continue;

            File dir = new File(dirPath);
            if (!dir.exists()) continue;

            File[] files = dir.listFiles(xmlFilter);
            if (files != null) {
                for (File file : files) {
                    XMLControl control = new XMLControlElement(file.getPath());
                    if (control.failedToRead()) {
                        continue;
                    }

                    Class<?> type = control.getObjectClass();
                    if (type != null && TrackDataBuilder.class.isAssignableFrom(type)) {
                        for (Object next : control.getPropertyContent()) {
                            if (next instanceof XMLProperty
                                    && ((XMLProperty) next).getPropertyName().equals("functions")) {
                                // found DataFunctionPanels
                                ArrayList<XMLControl> controls = new ArrayList<>();
                                XMLControl[] panels = ((XMLProperty) next).getChildControls();
                                for (XMLControl panelControl : panels) {
                                    String trackType = panelControl.getString("description");
                                    @SuppressWarnings("unchecked")
                                    ArrayList<String[]> functions = (ArrayList<String[]>) panelControl.getObject("functions");
                                    if (trackType == null || functions == null || functions.isEmpty())
                                        continue;

                                    // add panel to dataFunctionControls
                                    controls.add(panelControl);
                                } // end inner loop

                                String filePath = XML.forwardSlash(file.getAbsolutePath());
                                dataFunctionControls.put(filePath, controls);
                            }
                        } // end next loop
                    }
                } // end file loop
            }
        } // end dirPath loop
    }

    /**
     * Gets the default autoload search paths.
     *
     * @return the default search paths
     */
    public static Collection<String> getDefaultAutoloadSearchPaths() {
        return OSPRuntime.getDefaultSearchPaths();
    }

    /**
     * Gets the starting autoload search paths. Search paths may be later modified by the user.
     *
     * @return the search paths
     */
    protected static Collection<String> getInitialSearchPaths() {
        if (initialAutoloadSearchPaths.isEmpty()) {
            if (preferredAutoloadSearchPaths != null) {
                initialAutoloadSearchPaths.addAll(Arrays.asList(preferredAutoloadSearchPaths));
            } else {
                initialAutoloadSearchPaths.addAll(getDefaultAutoloadSearchPaths());
            }
        }
        return initialAutoloadSearchPaths;
    }

    /**
     * Sets the preferred locale.
     *
     * @param localeName the name of the locale
     */
    protected static void setPreferredLocale(String localeName) {
        if (localeName == null) {
            Locale.setDefault(defaultLocale);
            preferredLocale = null;
        } else for (Locale locale : locales) {
            if (locale.toString().equals(localeName)) {
                Locale.setDefault(locale);
                preferredLocale = localeName;
                break;
            }
        }
        // set the default decimal separator
        OSPRuntime.setDefaultDecimalSeparator(
                new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator());
    }

    /**
     * Sets the cache path.
     *
     * @param cachePath the cache path
     */
    protected static void setCache(String cachePath) {
        File cacheDir = cachePath == null || cachePath.trim().equals("") ? ResourceLoader.getDefaultOSPCache() : new File(cachePath);
        ResourceLoader.setOSPCache(cacheDir);
    }

    /**
     * Checks and updates FFMPeg resources.
     *
     * @return true if any resources were updated
     */
    protected static boolean updateResources() {
        return false;
    }

    /**
     * Determines if two sets contain the same elements.
     *
     * @return true if the sets are equal
     */
    protected static boolean areEqual(Set<?> set1, Set<?> set2) {
        for (Object next : set1) {
            if (!set2.contains(next)) return false;
        }
        for (Object next : set2) {
            if (!set1.contains(next)) return false;
        }
        return true;
    }

    /**
     * Check for upgrades and show a dialog with upgrade info.
     * Also refresh toolbar associated with TrackerPanel, if any.
     *
     * @param trackerPanel a TrackerPanel (may be null)
     */
    protected static void showUpgradeStatus(TrackerPanel trackerPanel) {
        checkedForNewerVersion = false;
        loadCurrentVersion(true, false);
        if (trackerPanel != null) TTrackBar.getTrackbar(trackerPanel).refresh();
        String message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message");
        if (Tracker.newerVersion != null) { // new version available
            message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1")
                    + " " + Tracker.newerVersion + " "
                    + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2")
                    + XML.NEW_LINE + "https://" + Tracker.trackerWebsite;
        }
        TFrame frame = trackerPanel == null ? null : trackerPanel.getTFrame();
        JOptionPane.showMessageDialog(frame,
                message,
                TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Loads the current (latest) Tracker version number and compares it with this version.
     *
     * @param ignoreInterval true to load/compare immediately
     * @param logToFile      true to log in to the PHP counter
     */
    protected static void loadCurrentVersion(boolean ignoreInterval, boolean logToFile) {
        if (!ResourceLoader.isURLAvailable("http://www.opensourcephysics.org")) {
            return;
        }
        if (checkedForNewerVersion) return;
        checkedForNewerVersion = true;

        // check to see how much time has passed
        long millis = System.currentTimeMillis();
        double days = (millis - lastMillisChecked) / 86400000.0;

        // don't log to file more often than every 2 hours no matter what
        if (logToFile && days < 0.0833) logToFile = false;

        // send data as page name to get latest version from PHP script
        // typical pre-4.97 version: "4.90" or "4.61111227"
        // typical post-4.97 version: "4.9.8" or "4.10.0170504" or "5.0.1"
        String pageName = getPHPPageName(logToFile);
        String latestVersion = loginGetLatestVersion(pageName);

        if (!ignoreInterval) {
            // check to see if upgrade interval has passed
            double interval = checkForUpgradeInterval == 0 ? 0.0833 : checkForUpgradeInterval;
            if (days < interval) {
                return;
            }
        }

        // interval has passed or ignored, so check for upgrades
        lastMillisChecked = millis;
        int result = compareVersions(latestVersion, VERSION);
        if (result > 0) { // newer version available
            newerVersion = latestVersion;
            TFrame tFrame;
            Frame[] frames = Frame.getFrames();
            for (Frame value : frames) {
                if (value instanceof TFrame) {
                    tFrame = (TFrame) value;
                    TrackerPanel trackerPanel = tFrame.getTrackerPanel(tFrame.getSelectedTab());
                    if (trackerPanel != null) {
                        TTrackBar trackbar = TTrackBar.getTrackbar(trackerPanel);
                        trackbar.refresh();
                    }
                }
            }
        }
    }

    /**
     * Gets the "page name" to send to the PHP counter.
     *
     * @param logToFile true to assemble a page name that will be counted/logged
     * @return the page name
     */
    private static String getPHPPageName(boolean logToFile) {
        String page = "version";
        if (logToFile) {
            // assemble "page" to send to counter
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String engine = VideoIO.getEngine();
            String os = "unknownOS";
            try { // system properties may not be readable in some environments
                os = System.getProperty("os.name", "unknownOS").toLowerCase();
            } catch (SecurityException ignored) {
            }
            os = os.replace(" ", "");
            if (os.contains("windows")) {
                os = "windows";
            }
            page = "log_" + VERSION + "_" + os + "_" + engine;
            if (!"".equals(language)) {
                if (!"".equals(country)) {
                    language += "-" + country;
                }
                page += "_" + language;
            }
        }
        return page;
    }

    /**
     * Logs a specified page and returns the latest version of Tracker.
     *
     * @param page a page name
     * @return the latest available version as a string
     */
    private static String loginGetLatestVersion(String page) {
        String path = counterPath + "page=" + page;
        try {
            URL url = new URL(path);
            Resource res = new Resource(url);
            String version = res.getString().trim();
            OSPLog.finer(path + ":   " + version);
            return version;
        } catch (Exception ignored) {
        }
        return VERSION;
    }


    /**
     * Loads preferences from a preferences file, if any.
     */
    protected static void loadPreferences() {

        XMLControl prefsControl = TrackerStarter.findPreferences();
        if (prefsControl != null) {
            prefsPath = prefsControl.getString("prefsPath");
            if (prefsPath != null) {
                OSPLog.getOSPLog();
                OSPLog.info("preferences loaded from " + XML.getAbsolutePath(new File(prefsPath)));
            }
            prefsControl.loadObject(null);  // the loader itself reads the values
            return;
        }

        // unable to find prefs, so write new one(s) if possible
        StringBuilder recommendedPath = null;
        String fileName = TrackerStarter.PREFS_FILE_NAME;
        if (!OSPRuntime.isWindows()) {
            // add leading dot to hide file on OSX and Linux
            fileName = "." + fileName;
        }
        for (String path : OSPRuntime.getDefaultSearchPaths()) {
            String prefs_path = new File(path, fileName).getAbsolutePath();
            if (recommendedPath == null) recommendedPath = new StringBuilder(prefs_path);
            else recommendedPath.append(" or ").append(prefs_path);
            XMLControl control = new XMLControlElement(new Preferences());
            if (control.write(prefs_path) != null) {
                prefsPath = prefs_path;
                OSPLog.getOSPLog();
                OSPLog.info("wrote new preferences file to " + XML.getAbsolutePath(new File(prefsPath)));
            }
        }
        if (prefsPath == null) {
            // unable to read or write prefs
            OSPLog.getOSPLog();
            if (recommendedPath != null) {
                OSPLog.warning("administrator action required: unable to write preferences file to " + recommendedPath);
            } else {
                OSPLog.warning("unable to find or create preferences file " + TrackerStarter.PREFS_FILE_NAME);
            }
        }
    }

    /**
     * Saves the current preferences.
     *
     * @return the path to the saved file
     */
    protected static String savePreferences() {
        // save prefs file in current preferences path
        XMLControl control = new XMLControlElement(new Preferences());
        if (prefsPath != null) {
            control.write(prefsPath);
        }

        // save other existing prefs files
        for (int i = 0; i < 2; i++) {
            String fileName = TrackerStarter.PREFS_FILE_NAME;
            if (i == 1) {
                fileName = "." + fileName;
            }
            // update prefs files in OSPRuntime search paths, if any
            for (String path : OSPRuntime.getDefaultSearchPaths()) {
                File prefsFile = new File(path, fileName);
                if (prefsFile.getAbsolutePath().equals(prefsPath)) {
                    continue;
                }
                if (prefsFile.exists() && prefsFile.canWrite()) {
                    control.write(prefsFile.getAbsolutePath());
                }
            }
            // update prefs in current directory, if any
            File prefsFile = new File(fileName);
            if (prefsFile.getAbsolutePath().equals(prefsPath)) {
                continue;
            }
            if (prefsFile.exists() && prefsFile.canWrite()) {
                control.write(prefsFile.getAbsolutePath());
            }
        }

        // save current trackerHome and ffmpegHome in OSP preferences
        if (trackerHome != null && new File(trackerHome, "tracker.jar").exists()) {
            OSPRuntime.setPreference("TRACKER_HOME", trackerHome);
        }
        String ffmpegHome = System.getenv("FFMPEG_HOME");
        if (ffmpegHome != null) {
            OSPRuntime.setPreference("FFMPEG_HOME", ffmpegHome);
        }
        OSPRuntime.savePreferences();

        return prefsPath;
    }

    /**
     * Gets the zoomInCursor.
     *
     * @return the cursor
     */
    protected static Cursor getZoomInCursor() {
        if (zoomInCursor == null) {
            String imageFile = "/images/zoom_in.gif";
            Image zoom = ResourceLoader.getImage(imageFile);
            zoomInCursor = GUIUtils.createCustomCursor(zoom, new Point(12, 12), "Zoom In", Cursor.DEFAULT_CURSOR);
        }
        return zoomInCursor;
    }

    /**
     * Determines if a cursor is the zoomInCursor.
     *
     * @return true if the cursor is zoonIn
     */
    protected static boolean isZoomInCursor(Cursor cursor) {
        return cursor == Tracker.zoomInCursor && Tracker.zoomInCursor != Cursor.getDefaultCursor();
    }

    /**
     * Gets the zoomOutCursor.
     *
     * @return the cursor
     */
    protected static Cursor getZoomOutCursor() {
        if (zoomOutCursor == null) {
            String imageFile = "/images/zoom_out.gif";
            Image zoom = ResourceLoader.getImage(imageFile);
            zoomOutCursor = GUIUtils.createCustomCursor(zoom, new Point(12, 12), "Zoom Out", Cursor.DEFAULT_CURSOR);
        }
        return zoomOutCursor;
    }

    /**
     * Determines if a cursor is the zoomOutCursor.
     *
     * @return true if the cursor is zoomOut
     */
    protected static boolean isZoomOutCursor(Cursor cursor) {
        return cursor == Tracker.zoomOutCursor && Tracker.zoomOutCursor != Cursor.getDefaultCursor();
    }

    /**
     * Main entry point when used as application.
     *
     * @param args array of tracker or video file names
     */
    public static void main(String[] args) {

//         determine if this is tracker.jar (Tracker main class)
        boolean isTracker = false;
        JarFile jarfile = OSPRuntime.getLaunchJar();
        try {
            assert jarfile != null;
            Attributes att;
            att = Objects.requireNonNull(jarfile).getManifest().getMainAttributes();
            Object mainclass = att.getValue("Main-Class");
            isTracker = mainclass.toString().endsWith("org.opensourcephysics.cabrillo.tracker.Tracker");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // determine if relaunch is needed
        boolean isRelaunch = args != null && args.length > 0 && "relaunch".equals(args[args.length - 1]);
        if (isRelaunch) {
            String[] newargs = new String[args.length - 1];
            System.arraycopy(args, 0, newargs, 0, newargs.length);
            args = newargs;
        } else {
            // versions 4.87+ use environment variable to indicate relaunch
            String s = System.getenv(TrackerStarter.TRACKER_RELAUNCH);
            isRelaunch = "true".equals(s);
        }

        // get memory size requested in environment, if any
        String memoryEnvironment = System.getenv("MEMORY_SIZE");
        // get current memory (maximum heap) size
        java.lang.management.MemoryMXBean memory
                = java.lang.management.ManagementFactory.getMemoryMXBean();
        long currentMemory = memory.getHeapMemoryUsage().getMax() / (1024 * 1024);

        if (!isRelaunch) {
            String javaCommand = System.getProperty("java.home");
            javaCommand = XML.forwardSlash(javaCommand) + "/bin/java";
            String javaPath = preferredJRE;
            if (javaPath != null) {
                File javaFile = OSPRuntime.getJavaFile(javaPath);
                if (javaFile != null) {
                    javaPath = XML.stripExtension(XML.forwardSlash(javaFile.getPath()));
                } else javaPath = null;
            }
            boolean needsJavaVM = javaPath != null && !javaCommand.equals(javaPath);

            // update FFMPeg
            boolean updated = updateResources();

            // compare memory with requested size(s)
            if (memoryEnvironment != null) {
                originalMemoryRequest = requestedMemorySize;
                requestedMemorySize = Integer.parseInt(memoryEnvironment);
            }

            boolean needsMemory = requestedMemorySize > 10 &&
                    (currentMemory < 9L * requestedMemorySize / 10 || currentMemory > 11L * requestedMemorySize / 10);

            // check environment
            boolean needsEnvironment = false;
            try {
                String trackerDir = TrackerStarter.findTrackerHome(false);
                String trackerEnv = System.getenv("TRACKER_HOME");
                if (trackerDir != null && !trackerDir.equals(trackerEnv)) {
                    needsEnvironment = true;
                } else {
                    String ffmpegDir = TrackerStarter.findFFMPegHome(trackerDir, false);
                    String ffmpegEnv = System.getenv("FFMPEG_HOME");
                    if (ffmpegDir != null && !ffmpegDir.equals(ffmpegEnv)) {
                        needsEnvironment = true;
                    } else {
                        if (ffmpegDir != null && !OSPRuntime.isLinux()) {
                            String subdir = OSPRuntime.isWindows() ? "bin" : "lib";
                            String ffmpegPath = ffmpegDir + File.separator + subdir;
                            String pathName = OSPRuntime.isWindows() ? "Path" :
                                    OSPRuntime.isMac() ? "DYLD_LIBRARY_PATH" : "LD_LIBRARY_PATH";
                            String pathEnv = System.getenv(pathName);
                            if (pathEnv == null || !pathEnv.contains(ffmpegPath)) {
                                needsEnvironment = true;
                            }
                        }
                    }
                }

            } catch (Exception ignored) {
            }


            // attempt to relaunch if needed
            if (/*isTracker && */(needsJavaVM || needsMemory || needsEnvironment || updated)) {
                mainArgs = args;
                if (requestedMemorySize <= 10) {
                    requestedMemorySize = TrackerStarter.DEFAULT_MEMORY_SIZE;
                }
                System.setProperty(TrackerStarter.PREFERRED_MEMORY_SIZE, String.valueOf(requestedMemorySize));
                System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, OSPRuntime.getLaunchJarPath());

                TrackerStarter.relaunch(mainArgs, true);
                return;
            }
        }
        preferredMemorySize = requestedMemorySize;
        if (requestedMemorySize < 0)
            requestedMemorySize = (int) (currentMemory + 2);
        start(args);
    }

    /**
     * Starts a new Tracker.
     *
     * @param args array of tracker or video file names
     */
    private static void start(String[] args) {
        FontSizer.setLevel(preferredFontLevel + preferredFontLevelPlus);
        Dataset.maxPointsMultiplier = 6; // increase max points in dataset
        Tracker tracker;
        if (args == null || args.length == 0) tracker = new Tracker();
        else tracker = new Tracker(args, true, true);

        if (OSPRuntime.isMac()) {
            // instantiate the OSXServices class by reflection
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tracker");
            String className = "org.opensourcephysics.cabrillo.tracker.deploy.OSXServices";
            try {
                Class<?> OSXClass = Class.forName(className);
                Constructor<?> constructor = OSXClass.getConstructor(Tracker.class);
                constructor.newInstance(tracker);
            } catch (Exception | Error ignored) {
            }
        }

        final TFrame frame = tracker.getFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create and register DataTrackTool
        Runnable runner = () -> {
            try {
                dataTrackTool = new DataTrackTool(frame);
                registerRemoteTool(dataTrackTool);
            } catch (RemoteException ignored) {
            }
        };
        new Thread(runner).start();

        LaunchNode node = Launcher.activeNode;
        if (node != null) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
        TTrackBar.refreshMemoryButton();

        // inform user if memory size was reduced
        if (originalMemoryRequest > requestedMemorySize) {
            JOptionPane.showMessageDialog(frame,
                    TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message1") + " " + originalMemoryRequest + "MB\n" +
                            TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message2") + " " + requestedMemorySize + "MB.\n\n" +
                            TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message3"),
                    TrackerRes.getString("Tracker.Dialog.MemoryReduced.Title"),
                    JOptionPane.INFORMATION_MESSAGE);
        }


//    warnNoVideoEngine = false; // for PLATO
        if (warnNoVideoEngine && VideoIO.getDefaultEngine().equals(VideoIO.ENGINE_NONE)) {
            // warn user that there is no working video engine
            boolean ffmpegInstalled = DiagnosticsForFFMPeg.hasFFMPegJars();

            ArrayList<String> message = new ArrayList<>();
            boolean showRelaunchDialog = false;

            // no engine installed
            if (!ffmpegInstalled) {
                message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message1"));
                message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message2"));
                message.add(" ");
                message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message3"));
            }

            // engines installed on Windows but no 32-bit VM
            else if (OSPRuntime.isWindows() && JREFinder.getFinder().getDefaultJRE(32, trackerHome, true) == null) {
                message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message1"));
                message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message2"));
                message.add(" ");
                message.add(TrackerRes.getString("Tracker.Dialog.Install32BitVM.Message"));
                message.add(TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Message"));
            }

            // engines installed on Windows but running in 64-bit VM
            else if (OSPRuntime.isWindows() && OSPRuntime.getVMBitness() == 64) {
                message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message1"));
                message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message2"));
                message.add(" ");
                message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Question"));
                showRelaunchDialog = true;
            }

            // engines installed but not working
            else {
                message.add(TrackerRes.getString("Tracker.Dialog.EngineProblems.Message1"));
                message.add(TrackerRes.getString("Tracker.Dialog.EngineProblems.Message2"));
            }

            Box box = Box.createVerticalBox();
            for (String line : message) {
                box.add(new JLabel(line));
            }

            // add "don't show again" checkbox
            box.add(new JLabel("  "));
            final JCheckBox checkbox = new JCheckBox(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox"));
            checkbox.addActionListener(e -> warnNoVideoEngine = !checkbox.isSelected());
            box.add(checkbox);
            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            if (showRelaunchDialog) {
                // provide immediate way to change to 32-bit VM and relaunch
                Object[] options = new Object[]{
                        TrackerRes.getString("Tracker.Dialog.Button.RelaunchNow"),
                        TrackerRes.getString("Tracker.Dialog.Button.ContinueWithoutEngine")};
                int response = JOptionPane.showOptionDialog(frame, box,
                        TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (response == 0) {
                    // use prefs dialog to switch to 32-bit VM/default engine and relaunch
                    Runnable launcher = () -> {
                        PrefsDialog prefs = frame.getPrefsDialog();
                        prefs.relaunch32Bit();
                    };
                    SwingUtilities.invokeLater(launcher);

                }
            } else {
                JOptionPane.showMessageDialog(frame, box,
                        TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"),
                        JOptionPane.INFORMATION_MESSAGE);
            }

        }

        if (System.getenv("STARTER_WARNING") != null) {
            // possible cause: running VM in 64-bits even though preference is 32-bit
            // if so, change preference
            String warningString = System.getenv("STARTER_WARNING");
            String[] lines = warningString.split("\n");
            Box box = Box.createVerticalBox();
            for (String line : lines) {
                box.add(new JLabel(line));
            }

            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            JOptionPane.showMessageDialog(null,
                    box,
                    TrackerRes.getString("Tracker.Dialog.StarterWarning.Title"),
                    JOptionPane.WARNING_MESSAGE);
        }

        final String newVersionURL = System.getenv(TrackerStarter.TRACKER_NEW_VERSION);
        if (newVersionURL != null) {
            final File target = new File(trackerHome, "tracker.jar");
            Timer timer = new Timer(2000, e -> {
                ResourceLoader.download(newVersionURL, target, true);
                // check preferences: if not default tracker.jar, ask user to change to default
                if (Tracker.preferredTrackerJar != null && !"tracker.jar".equals(Tracker.preferredTrackerJar)) {
                    String prefVers = Tracker.preferredTrackerJar.substring(8, Tracker.preferredTrackerJar.lastIndexOf("."));
                    String s1 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message1") + " " + Tracker.VERSION;
                    String s2 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message2") + " " + prefVers;
                    String s3 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message3");
                    String title = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Title");
                    int response = JOptionPane.showConfirmDialog(null, s1 + XML.NEW_LINE + s2 + XML.NEW_LINE + s3,
                            title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        Tracker.preferredTrackerJar = null;
                        Tracker.savePreferences();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        Timer memoryTimer = new Timer(5000, e -> TTrackBar.refreshMemoryButton());
        memoryTimer.setRepeats(true);
        memoryTimer.start();
    }

    /**
     * Sets the progress in percent for splash display.
     *
     * @param progress a number from 0 (start) to 100 (done)
     */
    protected static void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    /**
     * Logs the current time (to milliseconds) with a message.
     */
    protected static void logTime(String message) {
        if (timeLogEnabled) {
            SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS");
            Calendar cal = Calendar.getInstance();
            OSPLog.info(sdf.format(cal.getTime()) + ": " + message);
        }
    }

    /**
     * Registers a Remote tool with the RMI registry.
     *
     * @param remoteTool the Remote
     */
    protected static void registerRemoteTool(Remote remoteTool) {
        final String toolname = remoteTool.getClass().getSimpleName();

        // register tool
        try {
            // create registry if needed
            if (registry == null) {
                registry = java.rmi.registry.LocateRegistry.createRegistry(DataTrackSupport.PORT);
            }
            registry.rebind(toolname, remoteTool);
            OSPLog.fine(toolname + " successfully registered");
        } catch (Exception ex) {
            OSPLog.warning(ex.getMessage());
        }
    }

    /**
     * Unregisters a Remote tool with the RMI registry.
     *
     * @param remoteTool the Remote
     */
    protected static void unregisterRemoteTool(Remote remoteTool) {
        if (registry == null || remoteTool == null) return;
        try {
            String name = remoteTool.getClass().getSimpleName();
            registry.unbind(name);
            OSPLog.fine(name + " successfully unregistered");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds a path to the list of recent files.
     *
     * @param filename the absolute path to a recently opened or saved file.
     * @param atEnd    true to add at end of the list
     */
    protected static void addRecent(String filename, boolean atEnd) {
        synchronized (recentFiles) {
            while (recentFiles.contains(filename))
                recentFiles.remove(filename);
            if (atEnd)
                recentFiles.add(filename);
            else
                recentFiles.add(0, filename);
            while (recentFiles.size() > recentFilesSize) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }
    }

    /**
     * Sets the maximum size of the recent files list. Limited to 12 or less.
     *
     * @param max the desired maximum size.
     */
    protected static void setRecentSize(int max) {
        max = Math.min(max, 12);
        recentFilesSize = Math.max(max, 0);
        while (recentFiles.size() > recentFilesSize) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }

    /**
     * A class to compare version strings.
     */
    public static class Version implements Comparable {
        String ver;

        /**
         * Constructor
         *
         * @param version the version string
         */
        public Version(String version) {
            ver = version;
        }

        public boolean isValid() {
            String[] v = this.ver.trim().split("\\.");
            if (v.length == 2 || v.length == 3) {
                for (String s : v) {
                    try {
                        Integer.parseInt(s.trim());
                    } catch (Exception ex) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(Object o) {
            // typical newer semantic version "4.9.10"
            // typical older version "4.97"

            // split at decimal points
            String[] v1 = this.ver.trim().split("\\.");
            String[] v2 = ((Version) o).ver.trim().split("\\.");
            // newer semantic version arrays have length 3
            // older version arrays have length 2

            if (v2.length > v1.length) {
                // v1 is older version, v2 is newer
                return -1;
            }
            if (v1.length > v2.length) {
                // v2 is older version, v1 is newer
                return 1;
            }
            // both arrays have the same length
            for (int i = 0; i < v1.length; i++) {
                if (Integer.parseInt(v1[i]) < Integer.parseInt(v2[i])) {
                    return -1;
                } else if (Integer.parseInt(v1[i]) > Integer.parseInt(v2[i])) {
                    return 1;
                }
            }
            return 0;
        }
    }


    /**
     * A class to save and load Tracker preferences. The preference data are static Tracker fields.
     */
    static class Preferences {

        /**
         * Returns an ObjectLoader to save and load data for this class.
         *
         * @return the object loader
         */
        public static XML.ObjectLoader getLoader() {
            return new Loader();
        }

        /**
         * A class to save and load data for this class.
         */
        static class Loader implements XML.ObjectLoader {

            /**
             * Saves an object's data to an XMLControl.
             *
             * @param control the control to save to
             * @param obj     the object to save
             */
            public void saveObject(XMLControl control, Object obj) {
                // save only non-default values
                if (!Tracker.preferredLogLevel.equals(Tracker.DEFAULT_LOG_LEVEL)) // true by default
                    control.setValue("log_level", Tracker.preferredLogLevel.getName());
                if (!Tracker.showHintsByDefault) // true by default
                    control.setValue("show_hints", Tracker.showHintsByDefault);
                if (Tracker.isRadians) // false by default
                    control.setValue("radians", Tracker.isRadians);
                if (Tracker.markAtCurrentFrame) // false by default
                    control.setValue("mark_current_frame", Tracker.markAtCurrentFrame);
                if (Tracker.scrubMouseWheel) // false by default
                    control.setValue("scrub_mousewheel", Tracker.scrubMouseWheel);
                if (Tracker.enableAutofill) // false by default
                    control.setValue("enable_autofill", Tracker.enableAutofill);
                if (Tracker.showGaps) // false by default
                    control.setValue("show_gaps", Tracker.showGaps);
                if (Tracker.trailLengthIndex != TToolBar.trailLengths.length - 2)
                    control.setValue("trail_length", TToolBar.trailLengthNames[Tracker.trailLengthIndex]);
                if (Tracker.centerCalibrationStick) // false by default
                    control.setValue("center_stick", Tracker.centerCalibrationStick);
                if (!Tracker.warnNoVideoEngine) // true by default
                    control.setValue("warn_no_engine", Tracker.warnNoVideoEngine);
                if (!Tracker.warnVariableDuration) // true by default
                    control.setValue("warn_variable_frame_duration", Tracker.warnVariableDuration);
                if (!Tracker.warnFFMPegError) // true by default
                    control.setValue("warn_ffmpeg_error", Tracker.warnFFMPegError);
                // always save preferred tracker.jar
                String jar = Tracker.preferredTrackerJar == null ?
                        "tracker.jar" : Tracker.preferredTrackerJar;
                control.setValue("tracker_jar", jar);
                if (Tracker.preferredJRE != null)
                    control.setValue("java_vm", Tracker.preferredJRE);
                if (Tracker.preferredPointMassFootprint != null)
                    control.setValue("pointmass_footprint", Tracker.preferredPointMassFootprint);
                if (Tracker.preferredMemorySize > -1) // -1 by default
                    control.setValue("memory_size", Tracker.preferredMemorySize);
                if (Tracker.lookAndFeel != null)
                    control.setValue("look_feel", Tracker.lookAndFeel);
                if (Tracker.prelaunchExecutables.length > 0)
                    control.setValue("run", Tracker.prelaunchExecutables);
                if (Tracker.preferredLocale != null)
                    control.setValue("locale", Tracker.preferredLocale);
                if (Tracker.preferredDecimalSeparator != null)
                    control.setValue("decimal_separator", Tracker.preferredDecimalSeparator);
                if (Tracker.additionalDecimalSeparators != null)
                    control.setValue("additional_decimal_separators", Tracker.additionalDecimalSeparators);
                if (Tracker.preferredFontLevel > 0) {
                    control.setValue("font_size", Tracker.preferredFontLevel);
                }
                if (Tracker.preferredFontLevelPlus > 0) {
                    control.setValue("font_size_plus", Tracker.preferredFontLevelPlus);
                }
                if (ResourceLoader.getOSPCache() != null) {
                    File cache = ResourceLoader.getOSPCache();
                    control.setValue("cache", cache.getPath());
                }
                control.setValue("upgrade_interval", Tracker.checkForUpgradeInterval);
                int lastChecked = (int) (Tracker.lastMillisChecked / 1000L);
                control.setValue("last_checked", lastChecked);
                JFileChooser chooser = VideoIO.getChooser();
                File file = chooser.getCurrentDirectory();
                String userDir = System.getProperty("user.dir");
                if (!file.getAbsolutePath().equals(userDir)) // user.dir by default
                    control.setValue("file_chooser_directory", XML.getAbsolutePath(file));

                // video_engine--used by version 4.75+
                if (!VideoIO.getPreferredExportExtension().equals(VideoIO.DEFAULT_PREFERRED_EXPORT_EXTENSION))
                    control.setValue("export_extension", VideoIO.getPreferredExportExtension());
                if (!ExportZipDialog.preferredExtension.equals(ExportZipDialog.DEFAULT_VIDEO_EXTENSION))
                    control.setValue("zip_export_extension", ExportZipDialog.preferredExtension);

                if (Tracker.recentFilesSize != 6) // 6 items by default
                    control.setValue("max_recent", Tracker.recentFilesSize);
                if (!Tracker.recentFiles.isEmpty()) // empty by default
                    control.setValue("recent_files", Tracker.recentFiles);
                if (Tracker.preferredAutoloadSearchPaths != null) {
                    // added Dec 2104
                    control.setValue("autoload_search_paths", preferredAutoloadSearchPaths);
                }
                if (!Tracker.autoloadMap.isEmpty()) {
                    // added Dec 2104
                    String[][] autoloadData = new String[Tracker.autoloadMap.size()][];
                    int i = 0;
                    for (String filePath : Tracker.autoloadMap.keySet()) {
                        String[] functions = Tracker.autoloadMap.get(filePath);
                        String[] fileAndFunctions = new String[functions.length + 1];
                        fileAndFunctions[0] = filePath;
                        System.arraycopy(functions, 0, fileAndFunctions, 1, functions.length);
                        autoloadData[i] = fileAndFunctions;
                        i++;
                    }
                    control.setValue("autoload_exclusions", autoloadData);
                }
                if (!Tracker.dataFunctionControlStrings.isEmpty()) {
                    // deprecated Dec 2014: this is for legacy preferences
                    control.setValue("data_functions", Tracker.dataFunctionControlStrings);
                }
                if (defaultConfig != null && !areEqual(defaultConfig, getFullConfig())) { // defaultConfig by default
                    Configuration config = new Configuration(defaultConfig);
                    control.setValue("configuration", config);
                }
            }

            /**
             * Creates a new object.
             *
             * @param control the XMLControl with the object data
             * @return the newly created object
             */
            public Object createObject(XMLControl control) {
                return new Preferences();
            }

            /**
             * Loads an object with data from an XMLControl.
             *
             * @param control the control
             * @param obj     the object
             * @return the loaded object
             */
            @SuppressWarnings("unchecked")
            public Object loadObject(XMLControl control, Object obj) {
                Level logLevel = OSPLog.parseLevel(control.getString("log_level"));
                if (logLevel != null) {
                    Tracker.preferredLogLevel = logLevel;
                    OSPLog.setLevel(logLevel);
                    if (logLevel == Level.ALL) {
                        OSPLog.showLogInvokeLater();
                    }
                }
                Tracker.isRadians = control.getBoolean("radians");
                Tracker.markAtCurrentFrame = control.getBoolean("mark_current_frame");
                Tracker.scrubMouseWheel = control.getBoolean("scrub_mousewheel");
                Tracker.enableAutofill = control.getBoolean("enable_autofill");
                Tracker.showGaps = control.getBoolean("show_gaps");
                Tracker.centerCalibrationStick = control.getBoolean("center_stick");
                Tracker.isVideoFast = control.getBoolean("ffmpeg_fast");
                if (control.getPropertyNames().contains("trail_length")) {
                    String name = control.getString("trail_length");
                    for (int i = 0; i < TToolBar.trailLengthNames.length; i++) {
                        if (TToolBar.trailLengthNames[i].equals(name)) Tracker.trailLengthIndex = i;
                    }
                }
                if (control.getPropertyNames().contains("warn_no_engine"))
                    Tracker.warnNoVideoEngine = control.getBoolean("warn_no_engine");
                if (control.getPropertyNames().contains("warn_ffmpeg_error"))
                    Tracker.warnFFMPegError = control.getBoolean("warn_ffmpeg_error");
                if (control.getPropertyNames().contains("warn_variable_frame_duration"))
                    Tracker.warnVariableDuration = control.getBoolean("warn_variable_frame_duration");
                if (control.getPropertyNames().contains("show_hints")) {
                    Tracker.showHintsByDefault = control.getBoolean("show_hints");
                    Tracker.showHints = Tracker.showHintsByDefault;
                    Tracker.startupHintShown = !Tracker.showHints;
                }
                if (control.getPropertyNames().contains("java_vm")) {
                    Tracker.preferredJRE = control.getString("java_vm");
                    if (OSPRuntime.getJavaFile(Tracker.preferredJRE) == null) {
                        Tracker.preferredJRE = null;
                    }
                }
                Tracker.preferredPointMassFootprint = control.getString("pointmass_footprint");
                if (control.getPropertyNames().contains("memory_size"))
                    Tracker.requestedMemorySize = control.getInt("memory_size");
                if (control.getPropertyNames().contains("look_feel"))
                    Tracker.lookAndFeel = control.getString("look_feel");
                if (control.getPropertyNames().contains("decimal_separator")) {
                    Tracker.preferredDecimalSeparator = control.getString("decimal_separator");
                    OSPRuntime.setPreferredDecimalSeparator(preferredDecimalSeparator);
                }
                if (control.getPropertyNames().contains("additional_decimal_separators")) {
                    Tracker.additionalDecimalSeparators = control.getString("additional_decimal_separators");
                    OSPRuntime.setAdditionalDecimalSeparators(additionalDecimalSeparators);
                }
                if (control.getPropertyNames().contains("run"))
                    Tracker.prelaunchExecutables = (String[]) control.getObject("run");
                if (control.getPropertyNames().contains("locale"))
                    setPreferredLocale(control.getString("locale"));
                if (control.getPropertyNames().contains("font_size")) {
                    Tracker.preferredFontLevel = control.getInt("font_size");
                    Tracker.preferredFontLevelPlus = control.getInt("font_size_plus");
                    if (Tracker.preferredFontLevelPlus == Integer.MIN_VALUE) {
                        Tracker.preferredFontLevelPlus = 0;
                    }
                }
                // set cache only if it has not yet been set
                if (ResourceLoader.getOSPCache() == null) {
                    setCache(control.getString("cache"));
                }
                if (control.getPropertyNames().contains("upgrade_interval")) {
                    Tracker.checkForUpgradeInterval = control.getInt("upgrade_interval");
                    Tracker.lastMillisChecked = control.getInt("last_checked") * 1000L;
                }
                if (control.getPropertyNames().contains("file_chooser_directory"))
                    OSPRuntime.chooserDir = control.getString("file_chooser_directory");

                // preferred video engine
                VideoIO.setPreferredExportExtension(control.getString("export_extension"));
                if (control.getPropertyNames().contains("zip_export_extension"))
                    ExportZipDialog.preferredExtension = control.getString("zip_export_extension");

                if (control.getPropertyNames().contains("max_recent"))
                    Tracker.recentFilesSize = control.getInt("max_recent");
                if (control.getPropertyNames().contains("recent_files")) {
                    ArrayList<?> recent = (ArrayList) control.getObject("recent_files");
                    for (Object next : recent) {
                        addRecent(next.toString(), true); // add at end
                    }
                }
                // added Dec 2014
                Tracker.preferredAutoloadSearchPaths = (String[]) control.getObject("autoload_search_paths");
                // load autoload_exclusions: added Dec 2014
                if (control.getPropertyNames().contains("autoload_exclusions")) {
                    String[][] autoloadData = (String[][]) control.getObject("autoload_exclusions");
                    for (String[] next : autoloadData) {
                        String filePath = XML.forwardSlash(next[0]);
                        String[] functions = new String[next.length - 1];
                        System.arraycopy(next, 1, functions, 0, functions.length);
                        Tracker.autoloadMap.put(filePath, functions);
                    }
                }

                // load autoloadable data function strings (deprecated Dec 2014: this is for legacy files)
                if (control.getPropertyNames().contains("data_functions")) {
                    Collection<String> autoloads = (Collection<String>) control.getObject("data_functions");
                    Tracker.dataFunctionControlStrings.addAll(autoloads);
                }

                XMLControl child = control.getChildControl("configuration");
                if (child != null) {
                    Configuration config = (Configuration) child.loadObject(null);
                    setDefaultConfig(config.enabled);
                }
                // always load "tracker_jar"
                Tracker.preferredTrackerJar = control.getString("tracker_jar");
                return obj;
            }
        }
    }
}