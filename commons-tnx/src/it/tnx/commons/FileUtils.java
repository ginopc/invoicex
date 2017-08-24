/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author test1
 */
public class FileUtils {

    //toglie caratteri strani dai nomi file
    static public String normalizeFileName(String fileName) {
        String file = StringUtils.substringBeforeLast(fileName, ".");
        String ext = StringUtils.substringAfterLast(fileName, ".");
        file = file.replaceAll("[^a-zA-Z0-9-]", "_");
        if (ext.length() == 3) {
            return file + "." + ext;
        } else {
            return fileName.replaceAll("[^a-zA-Z0-9-]", "_");
        }
    }

    static public String normalizeFileNameDir(String fileName) {
        String file = StringUtils.substringBeforeLast(fileName, ".");
        String ext = StringUtils.substringAfterLast(fileName, ".");
        file = file.replaceAll("[^a-zA-Z0-9-\\\\/]", "_");
        if (ext.length() == 3) {
            return file + "." + ext;
        } else {
            return fileName.replaceAll("[^a-zA-Z0-9-\\\\/]", "_");
        }
    }

    public static void copyFile(File source, File dest) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(source);
        FileOutputStream fout = new FileOutputStream(dest);
        byte[] buff = new byte[1024];
        int read = 0;
        while ((read = fin.read(buff)) > 0) {
            fout.write(buff, 0, read);
        }
        fin.close();
        fout.flush();
        fout.close();
    }

    public static String readContent(InputStream source) throws FileNotFoundException, IOException {
        byte[] buff = new byte[1024];
        int read = 0;
        String out = "";
        while ((read = source.read(buff)) > 0) {
            out += new String(buff, 0, read);
        }
        source.close();
        return out;
    }

    public static String readContent(File source) throws FileNotFoundException, IOException {
        return readContent(new FileInputStream(source));
    }

    public static String readContentCp1252(File source) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), "Cp1252"));
        String str = "";
        String line = "";
        while ((line = in.readLine()) != null) {
            str += line + "\n";
        }
        return str;
    }

    public static String readContentUtf8(File source) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF8"));
        String str = "";
        String line = "";
        while ((line = in.readLine()) != null) {
            str += line + "\n";
        }
        return str;
    }

    static public String getExt(File file) {
        if (file == null) {
            return "";
        }
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    static public String getExt(String filename) {
        if (filename == null) {
            return "";
        }
        File file = new File(filename);
        return getExt(file);
    }

    static public String getName(File file) {
        if (file == null) {
            return "";
        }
        return file.getName().substring(0, file.getName().length() - getExt(file).length() - 1);
    }

    static public String getName(String filename) {
        if (filename == null) {
            return "";
        }
        File file = new File(filename);
        return getName(file);
    }

    static public String addSuffix(File file, String suffix) {
        if (file == null) {
            return "";
        }
        String pre = "";
        if (file.getParent() != null) {
            pre = file.getParent() + File.separator;
        }
        return pre + FileUtils.getName(file) + suffix + "." + FileUtils.getExt(file);
    }

    static public String addSuffix(String filename, String suffix) {
        if (filename == null) {
            return "";
        }
        File file = new File(filename);
        return addSuffix(file, suffix);
    }

    public static void main(String[] args) {
//        String file = "aAbc\\d123%$£&/()ds***%dsds.pdf";
        String file = "aAbc\\d123%$..£&/()ds*.%dsds.pdf";
        System.out.println(file);
        System.out.println(normalizeFileName(file));
        System.out.println(normalizeFileNameDir(file));

        String test = null;
        try {
            test = readContentUtf8(new File("c:\\test2"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("test: " + test);

        File f = new File("c:\\windows\\system.ini");
        System.out.println(f);
        System.out.println("name:" + getName(f));
        System.out.println("ext: " + getExt(f));
        System.out.println("suffix:" + addSuffix(f, "_suffisso"));

        f = new File("build.xml");
        System.out.println(f);
        System.out.println("name:" + getName(f));
        System.out.println("ext: " + getExt(f));
        System.out.println("suffix:" + addSuffix(f, "_suffisso"));
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
