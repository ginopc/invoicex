/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class SystemUtils {

    private static final String REGQUERY_UTIL = "reg query ";
    private static final String REGSTR_TOKEN = "REG_SZ";
    private static final String REGDWORD_TOKEN = "REG_DWORD";
    private static final String PERSONAL_FOLDER_CMD = REGQUERY_UTIL + "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Personal";

    public static void main(String[] args) throws Exception {
//        List<String> processes = listRunningProcesses();
//        String result = "";
//        String resultok = "";
//        Iterator<String> it = processes.iterator();
//        int i = 0;
//        String pid = "";
//        while (it.hasNext()) {
//            result = it.next();
//            if (result.startsWith("mysqld-nt.exe")) {
//                resultok += result + "\n";
//                pid = StringUtils.split(resultok, "|")[2];
//            }
//        }
//        System.out.println(resultok);
//        System.out.println(pid);

//        killProcess(pid);
//        System.out.println(regQuery("HKEY_CLASSES_ROOT\\Applications\\PSPad.exe\\shell\\open\\command"));
//        System.out.println(regQuery("\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Personal"));
//
//        String get = getUserDocumentsFolder();
//        System.out.println("get = [" + get + "]");
//        File f = new File(get);
//        System.out.println("f = " + f);
        //inutile, problemi di permessi con reg delete
//        togliInvoicexExeRunAsAdmin("\"HKCU\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Layers\" /reg:32");
//        togliInvoicexExeRunAsAdmin("\"HKCU\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Layers\" /reg:64");
//        togliInvoicexExeRunAsAdmin("\"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Layers\" /reg:32");
//        togliInvoicexExeRunAsAdmin("\"HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\AppCompatFlags\\Layers\" /reg:64");
    }

    public static List<String> listRunningProcesses() {
        List<String> processList = new ArrayList<String>();
        try {
            //File file = File.createTempFile("commons-tnx-listRunningProcesses", ".vbs");
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "commons-tnx-listRunningProcesses.vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "";
            InputStreamReader isr = new InputStreamReader(SystemUtils.class.getClass().getResourceAsStream("/it/tnx/commons/ps.vbs"));
            BufferedReader br = new BufferedReader(isr);
            String lineb = null;
            while ((lineb = br.readLine()) != null) {
                vbs += lineb + "\n";
            }
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                processList.add(line);
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    public static void killProcess(String pid) throws Exception {
        if (pid == null || pid.length() == 0) {
            return;
        }
        List<String> killList = new ArrayList<String>();
        //File file = File.createTempFile("commons-tnx-killProcess", ".vbs");
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "commons-tnx-killProcess.vbs");
        file.deleteOnExit();
        FileWriter fw = new java.io.FileWriter(file);
        String vbs = "";
        InputStreamReader isr = new InputStreamReader(SystemUtils.class.getClass().getResourceAsStream("/it/tnx/commons/kill.vbs"));
        BufferedReader br = new BufferedReader(isr);
        String lineb = null;
        while ((lineb = br.readLine()) != null) {
            vbs += lineb + "\n";
        }
        vbs = StringUtils.replace(vbs, "%PID%", pid);
        fw.write(vbs);
        fw.close();
        Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
        BufferedReader input
                = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = input.readLine();
        input.close();
        file.delete();
        if (line.equals("0")) {
            return;
        }
        throw new Exception("Errore in killProcess:" + line);
    }

    static public String regQuery(String chiave) {
        try {
            //prima provo a leggere il codepage del cmd
            String cp = null;
            try {
                Process process0 = Runtime.getRuntime().exec("cmd /C chcp");
                StreamReader reader0 = new StreamReader(process0.getInputStream());
                reader0.start();
                process0.waitFor();
                reader0.join();
                String result0 = reader0.getResult();
                if (result0 != null && result0.length() > 0) {
                    cp = result0.substring(result0.indexOf(":") + 2, result0.length() - 2);
                }
//                System.out.println("debug regquer cp:" + cp);
            } catch (Exception ex0) {
                ex0.printStackTrace();
            }

//            System.out.println("debug regquery:" + REGQUERY_UTIL + chiave);
            Process process = Runtime.getRuntime().exec(REGQUERY_UTIL + chiave);
            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String result = reader.getResult();
            int p = result.indexOf(REGSTR_TOKEN);
            if (p == -1) {
                return null;
            }
            if (cp == null) {
                return result.substring(p + REGSTR_TOKEN.length()).trim();
            } else {
                try {
                    byte[] ba = reader.getResultBytes();
//                    int start = p + REGSTR_TOKEN.length() + 4;  //invii
//                    int end = ba.length - 4;    //invii
//                    byte[] ba2 = new byte[end - start];
//                    for (int i = start; i < end; i++) {
//                        ba2[i - start] = ba[i];
//                    }
//                    return new String(ba2, cp);

                    result = new String(ba, cp);
                    result = StringUtils.substringAfterLast(result, REGSTR_TOKEN);
                    result = result.trim();
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return result.substring(p + REGSTR_TOKEN.length()).trim();
                }
            }
        } catch (Exception e) {
            return null;
        }

    }

    static public Map regQueryList(String chiave) {
        try {
            //prima provo a leggere il codepage del cmd
            String cp = null;
            try {
                Process process0 = Runtime.getRuntime().exec("cmd /C chcp");
                StreamReader reader0 = new StreamReader(process0.getInputStream());
                reader0.start();
                process0.waitFor();
                reader0.join();
                String result0 = reader0.getResult();
                if (result0 != null && result0.length() > 0) {
                    cp = result0.substring(result0.indexOf(":") + 2, result0.length() - 2);
                }
//                System.out.println("debug regquer cp:" + cp);
            } catch (Exception ex0) {
                ex0.printStackTrace();
            }

            Process process = Runtime.getRuntime().exec(REGQUERY_UTIL + chiave);
            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String result = reader.getResult();

            int p = result.indexOf(REGSTR_TOKEN);
            if (p == -1) {
                return null;
            }
            if (cp == null) {
                return null;
            } else {
                try {
                    byte[] ba = reader.getResultBytes();
//                    int start = p + REGSTR_TOKEN.length() + 4;  //invii
//                    int end = ba.length - 4;    //invii
//                    byte[] ba2 = new byte[end - start];
//                    for (int i = start; i < end; i++) {
//                        ba2[i - start] = ba[i];
//                    }
//                    return new String(ba2, cp);

                    result = new String(ba, cp);

                    String[] lines = StringUtils.split(result, "\r\n");
                    Map ret = new LinkedHashMap();
                    if (lines.length > 1) {
                        //salto prima riga che sarà la chiave cercata
                        for (int i = 1; i < lines.length; i++) {
                            String line = lines[i];
                            String k = StringUtils.trim(StringUtils.substringBefore(line, REGSTR_TOKEN));
                            String v = StringUtils.trim(StringUtils.substringAfter(line, REGSTR_TOKEN));
                            ret.put(k, v);
                        }
                        return ret;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }

    }

    static public String getHostname() {
        try {
            if (PlatformUtilsCommon.isMac()) {
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec("hostname");
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                line = br.readLine();
                int exitval = proc.waitFor();
                System.out.println("SystemUtils getHostname osx:" + line + " exitval:" + exitval);
                br.close();
                isr.close();
                stdin.close();
                return line;
            } else {
                return InetAddress.getLocalHost().getHostName();
            }
        } catch (Exception err) {
            err.printStackTrace();
            return "nohostname(" + err.getMessage() + ")";
        }
    }

    static public String getHostaddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception err) {
            err.printStackTrace();
            return "nohostaddress(" + err.getMessage() + ")";
        }
    }

    private static boolean containInvoicexExeRunAsAdmin(Map ret) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String regDelete(String chiave) throws IOException, InterruptedException {
        String cmd = "reg delete " + chiave;
        System.out.println("cmd = " + cmd);
        Process process = Runtime.getRuntime().exec(cmd);
        StreamReader reader = new StreamReader(process.getInputStream());
        reader.start();
        process.waitFor();
        reader.join();
        String result = reader.getResult();
        return result;
    }

    //inutile, problemi di permessi con reg delete
//    private static void togliInvoicexExeRunAsAdmin(String k) throws IOException, InterruptedException {
//        Map ret = regQueryList(k);
//        if (ret == null) return;
//        for (Object key : ret.keySet()) {
//            if (cu.s(key).endsWith("Invoicex.exe")) {
//                System.out.println("trovata chiave di esecuzione come amministratore: " + key);
//                String v = cu.s(ret.get(key));
//                System.out.println(v);
//                if (v.equalsIgnoreCase("~ RUNASADMIN")) {
//                    System.out.println("provo eliminazione");
//                    String splits[] = StringUtils.splitByWholeSeparator(k, "/reg:");
//                    String kdel = splits[0].substring(0, splits[0].length() - 3) + "\\" + key + "\" /f /reg:" + splits[1];
//                    System.out.println("kdel = " + kdel);
//                    String outdel = regDelete(kdel);
//                    System.out.println("out del: " + outdel);
//                } else {
//                    System.out.println("non provo eliminazione perchè diversa da '~ RUNASADMIN'");
//                }
//            }
//        }
//    }
    static class StreamReader extends Thread {

        private InputStream is;
        private StringWriter sw;
        private ByteArrayOutputStream bo;

        StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
            bo = new ByteArrayOutputStream();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1) {
                    sw.write(c);
                    bo.write(c);
                }
            } catch (IOException e) {
            }
        }

        String getResult() {
            return sw.toString();
        }

        byte[] getResultBytes() {
            return bo.toByteArray();
        }
    }

    public static String getUserDocumentsFolder() {
        if (PlatformUtilsCommon.isWindows()) {
            return regQuery("\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v Personal");
        } else if (PlatformUtilsCommon.isMac()) {
            return System.getProperty("user.home") + File.separator + "Documents";
        } else {
            return System.getProperty("user.home");
        }
    }
}
