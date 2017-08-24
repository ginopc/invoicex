/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author mceccarelli
 */
public class DelayedExecutor {
    Runnable runnable = null;
    long lastupdate = 0;
//    int delay = 250;   //default ms
    int delay = 350;   //default ms
    Timer t = null;

    public DelayedExecutor(Runnable runnable, long delay) {
        this.runnable = runnable;
        this.delay = (int)delay;
    }

    public void update() {
        update(null);
    }

    public void update(Object sender) {
        if (sender == null) {            
            lastupdate = System.currentTimeMillis();
            if (t != null) t.stop();
            t = new Timer(delay+150, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update(this);
                }
            });
            t.setRepeats(false);
            t.setInitialDelay(delay+150);
            t.start();
        } else if (sender instanceof Integer && (Integer)sender == 0) {
            runnable.run();
        } else {
            //qui arriva dal timer impostato sopra
            long currentupdate = System.currentTimeMillis();
            if ((currentupdate - lastupdate) >= delay) {                
                System.out.println("DelayedExecutor update !=null RUN currentupdate:" + currentupdate + " (currentupdate - lastupdate):" + (currentupdate - lastupdate) + " delay:" + delay);
                runnable.run();
            } else {
                System.out.println("DelayedExecutor update !=null NORUN currentupdate:" + currentupdate + " (currentupdate - lastupdate):" + (currentupdate - lastupdate) + " delay:" + delay);
            }
        }
    }

}
