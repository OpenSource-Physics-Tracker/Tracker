/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.cabrillo.tracker.analytics;

import org.opensourcephysics.tools.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Objects;

/**
 * Reads accumulated counts on the Tracker server.
 *
 * @author Doug Brown
 * @version 1.0
 */
public class TrackerCountReader extends JFrame {

    private final String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    private final String launchListPage = "list_"; //$NON-NLS-1$
    private final String downloadListFile = "list__list"; //$NON-NLS-1$
    private final String launchClearPage = "clear_"; //$NON-NLS-1$
    private final String downloadClearFile = "clear__clear"; //$NON-NLS-1$
    private final String launchPHPPath = "http://physlets.org/tracker/counter/counter.php?page="; //$NON-NLS-1$
    private final String downloadPHPPath = "http://physlets.org/tracker/installers/download.php?file="; //$NON-NLS-1$
    private final String[] versions = {"all", "5.0.5", "5.0.4", "5.0.3", "5.0.2",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "5.0.1", "5.0.0", "4.11.0", "4.10.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            "4.9.8", "4.97", "4.96", "4.95", "4.94",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "4.93", "4.92", "4.91", "4.90"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private final String[] OSs = {"all", "windows", "osx", "linux"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private final String[] engines = {"all", "FFMPeg", "none"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    JComboBox<String> actionDropdown;
    JComboBox<String> versionDropdown;
    JComboBox<String> osDropdown;
    JComboBox<String> engineDropdown;
    JLabel actionLabel, versionLabel, osLabel, engineLabel;

    JTextArea textArea;
    JButton sendButton;

    private TrackerCountReader() {
        super("Tracker Count Reader"); //$NON-NLS-1$
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        actionLabel = new JLabel("Action"); //$NON-NLS-1$
        versionLabel = new JLabel("Version"); //$NON-NLS-1$
        osLabel = new JLabel("OS"); //$NON-NLS-1$
        engineLabel = new JLabel("Engine"); //$NON-NLS-1$

        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String[] actions = {"read launch counts", "read downloads", "version", "list launch log failures", "list download failures",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "clear launch log failures", "clear download failures", "test launch log", "test downloads"};
        actionDropdown = new JComboBox<>(actions);
        versionDropdown = new JComboBox<>(versions);
        osDropdown = new JComboBox<>(OSs);
        engineDropdown = new JComboBox<>(engines);
        actionDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        versionDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        osDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        engineDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));

        Box actionBox = Box.createVerticalBox();
        actionBox.add(leftJustify(actionLabel));
        actionBox.add(actionDropdown);
        Box versionBox = Box.createVerticalBox();
        versionBox.add(leftJustify(versionLabel));
        versionBox.add(versionDropdown);
        Box osBox = Box.createVerticalBox();
        osBox.add(leftJustify(osLabel));
        osBox.add(osDropdown);
        Box engineBox = Box.createVerticalBox();
        engineBox.add(leftJustify(engineLabel));
        engineBox.add(engineDropdown);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(4, 7, 2, 7));
        box.add(actionBox);
        box.add(versionBox);
        box.add(osBox);
        box.add(engineBox);

        sendButton = new JButton("Send"); //$NON-NLS-1$
        sendButton.addActionListener(e -> {
            if (textArea.getForeground().equals(Color.RED.darker())) {
                String text = textArea.getText().trim();
                String result = send(launchPHPPath + text);
                textArea.setForeground(Color.BLACK);
                textArea.setText(result);
            } else {
                String[] ver, os, eng;
                String action = Objects.requireNonNull(actionDropdown.getSelectedItem()).toString();
                if (action.contains("list")) { //$NON-NLS-1$
                    String result;
                    if (action.contains("launch")) { //$NON-NLS-1$
                        result = send(launchPHPPath + launchListPage);
                        if ("".equals(result)) result = "(no launch log failures)"; //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        result = send(downloadPHPPath + downloadListFile);
                        if ("".equals(result)) result = "(no download failures)"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    textArea.setForeground(Color.BLACK);
                    textArea.setText(result);
                    return;
                } else if (action.equals("version")) { //$NON-NLS-1$
                    textArea.setForeground(Color.BLACK);
                    textArea.setText(send(launchPHPPath + "version")); //$NON-NLS-1$
                    return;
                } else if (action.contains("clear")) { //$NON-NLS-1$
                    String result;
                    if (action.contains("launch")) { //$NON-NLS-1$
                        result = send(launchPHPPath + launchClearPage);
                        if ("".equals(result)) result = "(cleared launch log failures)"; //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        result = send(downloadPHPPath + downloadClearFile);
                        if ("".equals(result)) result = "(cleared download failures)"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    textArea.setForeground(Color.BLACK);
                    textArea.setText(result);
                    return;
                } else { // action is "read..." or "test..."
                    if (Objects.equals(versionDropdown.getSelectedItem(), "all")) { //$NON-NLS-1$
                        ver = new String[versions.length - 1];
                        System.arraycopy(versions, 1, ver, 0, ver.length);
                    } else {
                        ver = new String[]{Objects.requireNonNull(versionDropdown.getSelectedItem()).toString()};
                    }
                    if (Objects.equals(osDropdown.getSelectedItem(), "all")) { //$NON-NLS-1$
                        os = new String[OSs.length - 1];
                        System.arraycopy(OSs, 1, os, 0, os.length);
                    } else {
                        os = new String[]{Objects.requireNonNull(osDropdown.getSelectedItem()).toString()};
                    }
                    if (Objects.equals(engineDropdown.getSelectedItem(), "all")) { //$NON-NLS-1$
                        eng = new String[engines.length - 1];
                        System.arraycopy(engines, 1, eng, 0, eng.length);
                    } else {
                        eng = new String[]{Objects.requireNonNull(engineDropdown.getSelectedItem()).toString()};
                    }
                }
                String result = send(actionDropdown.getSelectedItem().toString(), ver, os, eng);
                textArea.setForeground(Color.BLACK);
                String command = versionDropdown.getSelectedItem() + "_" + osDropdown.getSelectedItem(); //$NON-NLS-1$
                if (!action.contains("download")) { //$NON-NLS-1$
                    command += "_" + engineDropdown.getSelectedItem(); //$NON-NLS-1$
                }
                textArea.setText(action + " " + command + ": " + result); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(box, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        buttonPanel.add(sendButton, BorderLayout.NORTH);
        top.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(top, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                textArea.setForeground(Color.RED.darker());
            }
        });
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setPreferredSize(new Dimension(200, 400));
        contentPane.add(scroller, BorderLayout.CENTER);

        pack();
    }

    private String send(String command) {
        try {
            URL url = new URL(command);
            Resource res = new Resource(url);
            return res.getString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String send(final String action, String[] ver, String[] os, String[] eng) {
        // action is "read..." or "test..."
        int counts = 0;
        StringBuilder commands = new StringBuilder(); //$NON-NLS-1$
        for (String value : ver) {
            for (String o : os) {
                if ((action.contains("test") || action.contains("read")) && action.contains("download")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    // read or test downloads
                    String suffix = action.contains("read") ? "__read" : "_test"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    // typical Tracker-4.9.8-windows-installer.exe
                    // typical Tracker-4.9.8-osx-installer.zip
                    // typical Tracker-4.9.8-linux-32bit-installer.run
                    // typical Tracker-4.9.8-linux-64bit-installer.run
                    String osname = o;
                    String ext = osname.equals("windows") ? ".exe" : osname.equals("osx") ? ".zip" : ".run"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    if (osname.equals("linux")) { //$NON-NLS-1$
                        osname = "linux-32bit"; //$NON-NLS-1$
                    }
                    String command = "Tracker-" + value + "-" + osname + "-installer" + ext; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    String result = send(downloadPHPPath + command + suffix);
                    commands.append(NEW_LINE).append(command).append(": ").append(result); //$NON-NLS-1$
                    if (action.contains("read")) { //$NON-NLS-1$
                        try {
                            assert result != null;
                            result = result.replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
                            int n = Integer.parseInt(result);
                            counts += n;
                        } catch (NumberFormatException e) {
                            return "failed to parse " + result; //$NON-NLS-1$
                        }
                    }
                    if (osname.contains("linux")) { //$NON-NLS-1$
                        command = "Tracker-" + value + "-linux-64bit-installer" + ext; //$NON-NLS-1$ //$NON-NLS-2$
                        result = send(downloadPHPPath + command + suffix);
                        commands.append(NEW_LINE).append(command).append(": ").append(result); //$NON-NLS-1$
                        if (action.contains("read")) { //$NON-NLS-1$
                            try {
                                result = Objects.requireNonNull(result).replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                int n = Integer.parseInt(result);
                                counts += n;
                            } catch (NumberFormatException e) {
                                return "failed to parse " + result; //$NON-NLS-1$
                            }
                        }
                    }
                } else { // read or test launch counts
                    for (String s : eng) {
                        String osname = o;
                        if (osname.equals("osx")) osname = "macosx"; //$NON-NLS-1$ //$NON-NLS-2$
                        String command = "read_" + value + "_" + osname + "_" + s; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (action.contains("test")) { //$NON-NLS-1$
                            command = "log_" + value + "_" + osname + "_" + s + "test"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        }
                        String result = send(launchPHPPath + command);
                        commands.append(NEW_LINE).append(value).append("_").append(osname).append("_").append(s).append(": ").append(result); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (action.contains("read")) { //$NON-NLS-1$
                            try {
                                result = Objects.requireNonNull(result).replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                int n = Integer.parseInt(result);
                                counts += n;
                            } catch (NumberFormatException e) {
                                return "failed to parse " + result; //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        String s = String.valueOf(counts);
        if (action.contains("test")) { //$NON-NLS-1$
            if (action.contains("launch")) s = "launch log attempts"; //$NON-NLS-1$ //$NON-NLS-2$
            else s = "download attempts"; //$NON-NLS-1$
        }
        if (ver.length > 1 || os.length > 1 || eng.length > 1) {
            s += NEW_LINE + commands;
        }
        return s;
    }

    private Component leftJustify(Component c) {
        Box b = Box.createHorizontalBox();
        b.add(c);
        b.add(Box.createHorizontalGlue());
        b.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        return b;
    }

    public static void main(String[] args) {
        TrackerCountReader app = new TrackerCountReader();
        // center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - app.getBounds().width) / 2;
        int y = (dim.height - app.getBounds().height) / 2;
        app.setLocation(x, y);
        // display
        app.setVisible(true);
    }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2018  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */