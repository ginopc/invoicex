/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Marco
 */
public class MyStreamGobblerMain extends Thread {
    InputStream is;
    String type;

    public MyStreamGobblerMain(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {

        try {

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                System.out.println(type + ">" + line);
                line(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void line(String line) {
    }
}
