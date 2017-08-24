/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import it.tnx.commons.agg.JDialogProgress;
import it.tnx.commons.agg.UnzipWorker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author test1
 */
public class Aggiornamenti {

    static public JDialogProgress p = null;

    public void aggiorna(File file_zip, String lanciare_dopo) {
        aggiorna(file_zip.getAbsolutePath(), lanciare_dopo);
    }

    public void aggiorna(String file_zip_s, String lanciare_dopo) {
        File file_zip = new File(file_zip_s);
        File f = new File("lib/commons-tnx.jar");

        if (!f.exists()) {
            JOptionPane.showMessageDialog(null, "Problema durante l'aggioranmento, non e' presente lib/commons-tnx.jar", "Attenzione", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!file_zip.exists()) {
            JOptionPane.showMessageDialog(null, "Problema durante l'aggioranmento, non e' presente " + file_zip_s, "Attenzione", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //copio il commons-tnx.jar per non farlo bloccare durante l'aggiornamento dello stesso
        File fda = new File("lib/commons-tnx.jar");
        File fa = new File("lib/commons-tnx-tmp.jar");
        fa.delete();
        try {
            FileUtils.copyFile(fda, fa);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problema durante l'aggioranmento: " + ex.toString(), "Attenzione", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String run = "java -cp lib/commons-tnx-tmp.jar it.tnx.commons.Aggiornamenti \"" + file_zip_s + "\" " + lanciare_dopo;
        System.out.println("run:" + run);

        Thread t1 = new Thread() {

            public void run() {
                try {
                    Thread.sleep(1000);

//                    // Find the root thread group
//                    ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
//                    while (root.getParent() != null) {
//                        root = root.getParent();
//                    }
//                    visit(root, 0);

                    System.out.println("### exit 0");
                    System.exit(0);
                    System.out.println("### exit 1");
                } catch (Exception ex0) {
                    ex0.printStackTrace();
                }
                System.out.println("### halt 0");
                Runtime.getRuntime().halt(0);
                System.out.println("### halt 1");
            }
            
            // This method recursively visits all thread groups under `group'.
            public void visit(ThreadGroup group, int level) {
                // Get threads in `group'
                int numThreads = group.activeCount();
                Thread[] threads = new Thread[numThreads * 2];
                numThreads = group.enumerate(threads, false);

                // Enumerate each thread in `group'
                for (int i = 0; i < numThreads; i++) {
                    // Get thread
                    Thread thread = threads[i];
                    System.out.println("t:" + thread);
                    try {
                        //thread.interrupt();
                        thread.destroy();
                    } catch (Exception ex0) {
                        ex0.printStackTrace();
                    }
                }

                // Get thread subgroups of `group'
                int numGroups = group.activeGroupCount();
                ThreadGroup[] groups = new ThreadGroup[numGroups * 2];
                numGroups = group.enumerate(groups, false);

                // Recursively visit each subgroup
                for (int i = 0; i < numGroups; i++) {
                    visit(groups[i], level + 1);
                }
            }
        };
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread() {

            public void run() {
                try {
//                    Thread.sleep(100);
                    Runtime.getRuntime().exec(run);
                } catch (Exception ex0) {
                    JOptionPane.showMessageDialog(null, ex0.toString(), "Attenzione", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        t2.setDaemon(true);
        t2.start();
    }

    static public void main(final String[] argv) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                JOptionPane.showMessageDialog(null, e.toString());
            }
        });
        
        if (argv.length < 2) {
            JOptionPane.showMessageDialog(null, "Aggiornamenti.java : mancano i parametri: <filezip> <lanciare_dopo>");
            System.exit(1);
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    
//                    JOptionPane.showMessageDialog(null, "avvio ?...");

                    p = new JDialogProgress();
                    p.setSize(318, 100);
//                    p.setSize(318, 120);                    
                    p.setLocationRelativeTo(null);
                    p.setVisible(true);
                    p.setAlwaysOnTop(true);

                    p.labStatus.setText(".. attendere ..");
                    Thread.sleep(5000);

                    String args = "";
                    args = argv[1];
                    if (argv.length > 2) {
                        for (int i = 2; i < argv.length; i++) {
                            args += " " + argv[i];
                        }
                    }
                    UnzipWorker work = new UnzipWorker(argv[0], p, args);
                    work.execute();
                } catch (Exception err) {
                    err.printStackTrace();
                    JOptionPane.showMessageDialog(null, err.toString());
                }
            }
        });
    }
}
