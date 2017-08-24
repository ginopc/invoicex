/*
 * AggWorker.java
 *
 * Created on 25 settembre 2007, 9.43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package it.tnx.commons.agg;

import it.tnx.commons.*;
import java.io.File;
import java.net.InetAddress;
import java.util.List;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author mceccarelli
 */
public class UnzipWorker extends SwingWorker<Object,String> {
    String file;
    JDialogProgress progress;
    String lanciare_dopo;
    
    /** Creates a new instance of AggWorker */
    public UnzipWorker(String file, JDialogProgress progress, String lanciare_dopo) {
        this.file = file;
        this.progress = progress;
        this.lanciare_dopo = lanciare_dopo;
    }
    
    protected Object doInBackground() throws Exception {
        
        publish("Decompressione aggiornamento");
        
        try {
            Thread.sleep(3000);
        } catch (Exception e) {e.printStackTrace();}
        
        //decomprimo
        try {
            UnZip.main(new String[] {file}, this);
        } catch (Exception ex0) {
            JOptionPane.showMessageDialog(progress, ex0);
        }
        
        publish("Aggiornamento decompresso");

        Runtime.getRuntime().exec(lanciare_dopo);
        
        return null;
    }
    
    protected void done() {
        super.done();
        System.exit(0);
    }
    
    protected void process(List<String> objects) {
        progress.labStatus.setText(objects.get(0).toString());
    }
    
    public void publicPublish(String p) {
        publish(p);
    }

}