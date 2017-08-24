/**
 * Invoicex Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza GNU
 * General Public License, Version 2. La licenza accompagna il software o potete
 * trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the GNU General
 * Public License, Version 2. The license should have accompanied the software
 * or you may obtain a copy of the license from the Free Software Foundation at
 * http://www.fsf.org .
 *
 * -- Marco Ceccarelli (m.ceccarelli@tnx.it) Tnx snc (http://www.tnx.it)
 *
 */
/*
 * DumpThread.java
 *
 * Created on 23-giu-2007, 14.37.57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gestioneFatture;

import it.tnx.Db;
import it.tnx.JDialogMessage;
import it.tnx.JFrameMessage;
import it.tnx.commons.ReflectUtils;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;
import javax.swing.JButton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author marco
 */
public class DumpThread extends java.lang.Thread {

    javax.swing.JTextArea text;
    javax.swing.JButton chiudi;
    javax.swing.JProgressBar bar;
    javax.swing.JScrollPane scroll;
    public String nomeFileDump;
    public String nomeFileDump2;
    boolean sendOnline = false;
    JFrameMessage frame = null;
    JDialogMessage dialog = null;

    public DumpThread(it.tnx.JFrameMessage frame) {
        this.sendOnline = false;
        this.text = frame.getTextArea();
        this.chiudi = frame.getPulsanteChiudi();
        this.bar = frame.getAvanzamento();
        this.scroll = frame.getTextAreaScrollPane();
        frame.setTitle("Creazione copia di sicurezza dei dati");
        this.frame = frame;
    }

    public DumpThread(it.tnx.JDialogMessage dialog) {
        this.sendOnline = false;
        this.text = dialog.getTextArea();
        this.chiudi = dialog.getPulsanteChiudi();
        this.bar = dialog.getAvanzamento();
        this.scroll = dialog.getTextAreaScrollPane();
        dialog.setTitle("Creazione copia di sicurezza dei dati");
        this.dialog = dialog;
    }

    synchronized public void run() {
        Exception error1 = null;

        chiudi.setEnabled(false);
        bar.setIndeterminate(true);
        text.setAutoscrolls(true);
        text.append("Inizio backup database\n");

        //creo se non esiste la cartella backup
        text.append("Controllo cartella 'backup'");

        File dirBackup = new File(main.wd + "backup");

        if (!dirBackup.exists()) {
            dirBackup.mkdir();
        }

        text.append("...ok\n");

        //test con mysqldump
        //it.tnx.shell.Exec exec = new it.tnx.shell.Exec();
        //exec.execute("/usr/bin/mysqldump", "");
        //exec.execute("/usr/bin/mysqldump", Db.dbNameDB + " --add-drop-table");
        //backup con funzione senza eseguire comandi esterni
        try {
            nomeFileDump = it.tnx.shell.CurrentDir.getCurrentDir() + "/backup/dump";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmm");
            Date d = new Date();
            nomeFileDump += "_" + sdf.format(d) + ".txt";

            if (nomeFileDump2 != null) {
                nomeFileDump = it.tnx.shell.CurrentDir.getCurrentDir() + "/backup/" + nomeFileDump2;
                File test = new File(nomeFileDump);
                if (test.exists()) {
                    test.delete();
                }
            }

            java.io.FileOutputStream fos = new FileOutputStream(nomeFileDump, false);

            //portato invoicex a utf8
//            PrintStream o = new PrintStream(fos, true, "ISO-8859-1");
            PrintStream o = new PrintStream(fos, true, "UTF8");

            o.println("SET foreign_key_checks = 0;");
            o.println("SET storage_engine = MYISAM;");

            Vector tables = new Vector();

            Statement s = Db.getConn().createStatement();
            java.sql.ResultSet r = s.executeQuery("show full tables");

            String errori = "";
            while (r.next()) {
                String tab = r.getString(1);
                String tipo = r.getString(2);
                text.append("backup " + tipo + " '" + tab + "'");
                if (!it.tnx.Util.dumpTable(tab, Db.getConn(), fos, tipo)) {
                    errori += "\n" + tab;
                    System.out.println(tab + " : ERRORE");
                    text.append("...KO\n");
                    text.setCaretPosition(text.getText().length());                    
                } else {
                    System.out.println(tab + " : dumped");
                    text.append("...ok\n");
                    text.setCaretPosition(text.getText().length());
                }
            }

            if (StringUtils.isNotBlank(errori) && !main.isBatch) {
                SwingUtils.showErrorMessage(text.getTopLevelAncestor(), "Errore sul backup delle seguenti tabelle:" + errori, true);
            }
            
            o.println("SET foreign_key_checks = 1;");

            s.close();
            r.close();

            File fnomeFileDump = new File(nomeFileDump);

            fos.flush();
            fos.close();

            try {
                /*
                 System.out.println("SystemUtils.getUserDocumentsFolder():" + SystemUtils.getUserDocumentsFolder());
                 final File fnomeFileDumpDocumentiDir = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "backup");
                 boolean retdir = fnomeFileDumpDocumentiDir.mkdirs();
                 System.out.println("creazione cartella backup in documenti:" + retdir);
                 final File fnomeFileDumpDocumenti = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "backup" + File.separator + "dump_" + sdf.format(d) + ".txt");
                 FileUtils.copyFile(fnomeFileDump, fnomeFileDumpDocumenti);
                 text.append("\nIl backup si trova nel file\n'" + fnomeFileDumpDocumenti.getAbsolutePath() + "'\n\n");
                 */

                String copia = null;
                try {
                    URL[] url = new URL[1];
                    url[0] = new URL("file:plugins/InvoicexPluginInvoicex.jar");
                    URLClassLoader classloader = new URLClassLoader(url);
                    Object obj = classloader.loadClass("invoicexplugininvoicex.backup.MainBackup").newInstance();
                    Method m = obj.getClass().getDeclaredMethod("getNomeFileDump");
                    Object ret = m.invoke(obj);
                    System.out.println("ret = " + ret);
                    if (ret instanceof String) {
                        copia = (String) ret;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (copia == null) {
                    //utilizzo il nome di file standard
                    copia = SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "backup" + File.separator;
                    File nomeFileDumpFile = new File(nomeFileDump);
                    copia += nomeFileDumpFile.getName();
                }
                File fileCopia = new File(copia);
                boolean errore_cartella = false;
                if (!fileCopia.getParentFile().isDirectory()) {
                    File cartellaCopia = fileCopia.getParentFile();
                    boolean retdir = cartellaCopia.mkdirs();
                    System.out.println("creazione cartella backup in documenti:" + retdir);
                    if (!retdir) {
                        error1 = new Exception("Errore nella creazione della cartella '" + cartellaCopia.getAbsolutePath() + "'");
                        text.append("\nErrore nella creazione della cartella per il backup\n\n");
                        text.append("Cartella: '" + cartellaCopia.getAbsolutePath() + "'\n\n");
                        errore_cartella = true;
                        SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore nella creazione della cartella:\n" + cartellaCopia.getAbsolutePath());
                    }
                }
                final File ffileCopia = fileCopia;
                if (!errore_cartella) {
                    if (!fnomeFileDump.getAbsolutePath().equals(ffileCopia.getAbsolutePath())) {
                        FileUtils.copyFile(fnomeFileDump, ffileCopia);
                    }
                    text.append("\nIl backup si trova nel file\n'" + ffileCopia.getAbsolutePath() + "'\n\n");
                }

                if (frame != null) {
                    JButton bopen = new JButton("Apri cartella backup");
                    bopen.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Util.start2(ffileCopia.getParentFile().getAbsolutePath());
                        }
                    });
                    frame.panbasso.add(bopen);
                    frame.validate();
                }
                if (dialog != null) {
                    JButton bopen = new JButton("Apri cartella backup");
                    bopen.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Util.start2(ffileCopia.getParentFile().getAbsolutePath());
                        }
                    });
                    dialog.panbasso.add(bopen);
                    dialog.validate();
                }
            } catch (Exception e) {
                error1 = e;
                text.append("\nErrore:\n'" + e.toString() + "'\n\n");
                text.append("Debug, User Folder = '" + SystemUtils.getUserDocumentsFolder() + "'\n\n");
                SwingUtils.showErrorMessage(main.getPadreFrame(), e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception err) {
            err.printStackTrace();
        }

        Comparator filecomp = new Comparator() {
            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                if (f1.lastModified() > f2.lastModified()) {
                    return -1;
                } else if (f1.lastModified() < f2.lastModified()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        //pulisco da eventuali vecchi bakcup, diciamo che ne lascio gli ultimi 20
        File dir = new File(it.tnx.shell.CurrentDir.getCurrentDir() + "/backup");
        File[] lista = dir.listFiles();
        Vector listav = it.tnx.Util.getVectorFromArray(lista);
        java.util.Collections.sort(listav, filecomp);
        for (int i = 0; i < listav.size(); i++) {
            System.out.println("files[" + i + "]:" + ((File) listav.get(i)).getName() + "\t\t" + ((File) listav.get(i)).lastModified());
            if (i > 20) {
                try {
                    System.out.println("delete file:" + ((File) listav.get(i)).getCanonicalFile() + "\t deleteed:" + ((File) listav.get(i)).delete());
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
        //anche nella cartella documents pulisco da eventuali vecchi bakcup, diciamo che ne lascio gli ultimi 10
        try {
            dir = new File(SystemUtils.getUserDocumentsFolder() + File.separator + "Invoicex" + File.separator + "backup");
            lista = dir.listFiles();
            listav = it.tnx.Util.getVectorFromArray(lista);
            java.util.Collections.sort(listav, filecomp);
            for (int i = 0; i < listav.size(); i++) {
                System.out.println("files[" + i + "]:" + ((File) listav.get(i)).getName() + "\t\t" + ((File) listav.get(i)).lastModified());
                if (i > 20) {
                    try {
                        System.out.println("delete file:" + ((File) listav.get(i)).getCanonicalFile() + "\t deleteed:" + ((File) listav.get(i)).delete());
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bar.setIndeterminate(false);
        bar.setStringPainted(true);
        bar.setValue(100);
        if (error1 == null) {
            text.append("\nBackup completato.\n\n");
        }
        text.setCaretPosition(text.getText().length());

        chiudi.setEnabled(true);

        notify();

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement s : trace) {
            System.out.println("s: " + s);
            if (s.getMethodName().equalsIgnoreCase("exitMain")) {
                System.out.println("!!!!");
            }
        }
    }
}
