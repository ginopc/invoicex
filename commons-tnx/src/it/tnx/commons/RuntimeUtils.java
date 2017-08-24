/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author mceccarelli
 */
public class RuntimeUtils {

    static public int run(String run) throws IOException, InterruptedException {
        return run(run, false);
    }

    static public int run(final String run, boolean showOutput) throws IOException, InterruptedException {
        return run(run, showOutput, true);
    }

    static public int run(final String run, boolean showOutput, boolean waitfor) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(run);
        JFrame frame = null;
        JScrollPane scroll = null;
        JTextArea text = null;
        if (showOutput) {
            frame = new JFrame("RuntimeUtils Output");
            text = new JTextArea();
            scroll = new JScrollPane(text);
            text.setFont(new Font("Courier New", Font.PLAIN, 11));
            text.setEditable(false);
            frame.setLayout(new BorderLayout(10, 10));
            frame.add(scroll);
//            frame.setSize(600, 400);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();
            DisplayMode dm = gs[0].getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();

            frame.setSize(screenWidth, screenHeight / 6);
            frame.setLocation(0, screenHeight - (screenHeight / 5));

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        }
        final JFrame framef = frame;
        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", text) {

            @Override
            public void output(final String line) {
                if (text != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            text.append(type + " > " + line + "\n");
                        }
                    });
                } else {
                    System.out.println(type + " > " + line);
                }
            }
        };
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", text) {

            @Override
            public void output(final String line) {
                if (text != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            text.append(type + " > " + line + "\n");
                        }
                    });
                } else {
                    System.out.println(type + " > " + line);
                }
            }
        };
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        // any error???
        if (!showOutput) {
            if (waitfor) {
                int exitVal = proc.waitFor();
                System.out.println("RuntimeUtils run:" + run + " -> ExitValue: " + exitVal);
                return exitVal;
            } else {
                return 0;
            }
        } else {
            Thread t = new Thread() {

                @Override
                public void run() {
                    int exitVal = -1;
                    try {
                        exitVal = proc.waitFor();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("RuntimeUtils run:" + run + " -> ExitValue: " + exitVal);
                    framef.dispose();
                }
            };
            t.start();
        }
        return 0;
    }

    static public String runWait(final String run) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(run);
        final StringBuilder sb = new StringBuilder();
        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR") {

            @Override
            public void output(final String line) {
                sb.append(line);
            }
        };
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT") {

            @Override
            public void output(final String line) {
                sb.append(line);
            }
        };
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        // any error???
        int exitVal = proc.waitFor();
        System.out.println("RuntimeUtils run:" + run + " -> ExitValue: " + exitVal);
        return sb.toString();
    }

    static public String runWait(final String[] run) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(run);
        final StringBuilder sb = new StringBuilder();
        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR") {

            @Override
            public void output(final String line) {
                sb.append(line + "\n");
            }
        };
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT") {

            @Override
            public void output(final String line) {
                sb.append(line + "\n");
            }
        };
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        // any error???
        int exitVal = proc.waitFor();
        System.out.println("RuntimeUtils run:" + run + " -> ExitValue: " + exitVal);
        return sb.toString();
    }

    public static List<String> getJvmOptions() {
        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        return RuntimemxBean.getInputArguments();
    }

    public static boolean isInDebug() {
        if (getJvmOptions().contains("-Xdebug")) return true;
        return false;
    }

    public static void main(String[] args) {
        List l = getJvmOptions();
        DebugUtils.dump(l);

        System.out.println(isInDebug());
    }

    static public class StreamGobbler extends Thread {

        InputStream is;
        String type;
        JTextArea text;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        StreamGobbler(InputStream is, String type, JTextArea text) {
            this.is = is;
            this.type = type;
            this.text = text;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    output(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void output(String line) {
            System.out.println(type + ">" + line);
        }
    }
}
