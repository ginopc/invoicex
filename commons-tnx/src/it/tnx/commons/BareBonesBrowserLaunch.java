/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 3.1 (June 6, 2010)                         //
//  By Dem Pilafian                                    //
//  Supports:                                          //
//     Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7   //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////
import javax.swing.JOptionPane;
import java.util.Arrays;

public class BareBonesBrowserLaunch {

    static final String[] browsers = {"google-chrome", "firefox", "opera",
        "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla"};
    static final String errMsg = "Error attempting to launch web browser";

    public static void openURL(String url) {
        System.out.println("BareBonesBrowserLaunch url = " + url);
//        try {  //attempt to use Desktop library from JDK 1.6+
//            Class<?> d = Class.forName("java.awt.Desktop");
//            d.getDeclaredMethod("browse", new Class[]{java.net.URI.class}).invoke(
//                    d.getDeclaredMethod("getDesktop").invoke(null),
//                    new Object[]{java.net.URI.create(url)});
//            //above code mimicks:  java.awt.Desktop.getDesktop().browse()
//        } catch (Exception ignore) {  //library not available or failed
//            System.out.println("ignore = " + ignore);
//            ignore.printStackTrace();
            String osName = System.getProperty("os.name");
            System.out.println("osName = " + osName);
            try {
                if (osName.startsWith("Mac OS")) {
                    try {
                        Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                                "openURL", new Class[]{String.class}).invoke(null,
                                        new Object[]{url});
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } else if (osName.startsWith("Windows")) {
                    try {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } else { //assume Unix or Linux
                    String browser = null;
                    try {
                        for (String b : browsers) {
                            if (browser == null && Runtime.getRuntime().exec(new String[]{"which", b}).getInputStream().read() != -1) {
                                Runtime.getRuntime().exec(new String[]{browser = b, url});
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    if (browser == null) {
                        throw new Exception(Arrays.toString(browsers));
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
            }
//        }
    }

}
