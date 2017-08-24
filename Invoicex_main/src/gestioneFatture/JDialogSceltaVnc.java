/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogSceltaVnc.java
 *
 * Created on 24-nov-2010, 14.44.33
 */
package gestioneFatture;

/**
 *
 * @author mceccarelli
 */
public class JDialogSceltaVnc extends javax.swing.JDialog {

    static public final int MARCO_CECCARELLI = 1;
    static public final int ALESSIO_TOCE = 2;
    static public final int CARLO_CAMPINOTI = 3;
    static public final int ANDREA_PROVVEDI = 4;
    public int scelta = 0;
    public int porta = 5500;

    /** Creates new form JDialogSceltaVnc */
    public JDialogSceltaVnc(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        b1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
        b2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
        b3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
        b4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/contact-new.png"))); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        b1 = new javax.swing.JButton();
        b2 = new javax.swing.JButton();
        b4 = new javax.swing.JButton();
        b3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Seleziona l'interlocutore");

        b1.setFont(b1.getFont().deriveFont(b1.getFont().getSize()+3f));
        b1.setText("Marco Ceccarelli");
        b1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b1ActionPerformed(evt);
            }
        });

        b2.setFont(b2.getFont().deriveFont(b2.getFont().getSize()+3f));
        b2.setText("Alessio Toce");
        b2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b2ActionPerformed(evt);
            }
        });

        b4.setFont(b4.getFont().deriveFont(b4.getFont().getSize()+3f));
        b4.setText("Andrea Provvedi");
        b4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b4ActionPerformed(evt);
            }
        });

        b3.setFont(b3.getFont().deriveFont(b3.getFont().getSize()+3f));
        b3.setText("Carlo Campinoti");
        b3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(b1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                    .add(b2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, b3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                    .add(b4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(b1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(b2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(b3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(b4)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void b1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b1ActionPerformed
        scelta = MARCO_CECCARELLI;
        impostaPorta();
        setVisible(false);
    }//GEN-LAST:event_b1ActionPerformed

    private void b2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b2ActionPerformed
        scelta = ALESSIO_TOCE;
        impostaPorta();
        setVisible(false);
    }//GEN-LAST:event_b2ActionPerformed

    private void b3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b3ActionPerformed
        scelta = CARLO_CAMPINOTI;
        impostaPorta();
        setVisible(false);
    }//GEN-LAST:event_b3ActionPerformed

    private void b4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b4ActionPerformed
        scelta = ANDREA_PROVVEDI;
        impostaPorta();
        setVisible(false);
    }//GEN-LAST:event_b4ActionPerformed

    private void impostaPorta() {
        if (scelta == MARCO_CECCARELLI) {
            porta = 5500;
        } else if (scelta == ALESSIO_TOCE) {
            porta = 5504;
        } else if (scelta == ANDREA_PROVVEDI) {
            porta = 5503;
        } else if (scelta == CARLO_CAMPINOTI) {
            porta = 5501;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JDialogSceltaVnc dialog = new JDialogSceltaVnc(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b1;
    private javax.swing.JButton b2;
    private javax.swing.JButton b3;
    private javax.swing.JButton b4;
    // End of variables declaration//GEN-END:variables
}
