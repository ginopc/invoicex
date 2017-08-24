/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

import java.text.DecimalFormat;

/**
 *
 * @author test1
 */
public class MicroBench {
    long t1;
    long t2;
    String current;
    DecimalFormat f1 = new DecimalFormat("0.000");

    public MicroBench() {
    }

    public MicroBench(boolean start) {
        if (start) start();
    }
    
    public void start() {
        t1 = System.currentTimeMillis();
    }
    
    public String getDiff(String memo) {
        t2 = System.currentTimeMillis();
        current = memo;
        String ret = current + " - " + f1.format((double)(t2 - t1) / 1000d) + " sec";
        t1 = t2;
        return ret;
    }

    public void out(String memo) {
        System.out.println(getDiff(memo));
    }
}
