/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.smb.ACE;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SID;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Marco
 */
public class TestSmb {
    public static void main(String[] args) throws UnknownHostException, SmbException, MalformedURLException, IOException {
        
//        System.out.println("\n\n anonym");
//        SmbFile f = new SmbFile("smb://linux/", NtlmPasswordAuthentication.ANONYMOUS);
//        SmbFile[] listFiles = f.listFiles();
//        for (SmbFile smbf : listFiles) {
//            System.out.println("smbf = " + smbf);
//        }
//        try {
//            SmbFile f2 = new SmbFile("smb://linux/tnx/immagini/ita_s.gif", NtlmPasswordAuthentication.ANONYMOUS);
//            InputStream fileInputStream = f2.getInputStream();
//            FileOutputStream fileOutputStream = new FileOutputStream("c:\\temp\\ita_s.gif");
//            IOUtils.copy(f2.getInputStream(), fileOutputStream);
//            fileInputStream.close();
//            fileOutputStream.close();            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("\n\n con pass");
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", "tutti", "2dicotone");
        
        
        
    }
}
