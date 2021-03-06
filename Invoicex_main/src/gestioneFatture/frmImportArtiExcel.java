/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * frmImportArtiExcel2.java
 *
 * Created on 11-feb-2010, 11.16.36
 */
package gestioneFatture;

import it.tnx.Db;
import it.tnx.SwingWorker;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.cu;
import it.tnx.invoicex.ImportArticoli;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.filechooser.FileFilter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author mceccarelli
 */
public class frmImportArtiExcel extends javax.swing.JInternalFrame {

    JInternalFrame padre;

    /**
     * Creates new form frmImportArtiExcel
     */
    public frmImportArtiExcel(JInternalFrame padre) {
        initComponents();

        labImportareGiacenza.setVisible(false);
        comImportareGiacenza.setVisible(false);

        this.padre = padre;
        comListino.dbOpenList(Db.getConn(), "select descrizione, codice from tipi_listino where ricarico_flag is null or ricarico_flag != 'S'");

        try {
            if (Magazzino.isMultiDeposito()) {
                comDeposito.dbOpenList(Db.getConn(), "select nome, id from depositi");
            } else {
                comDeposito.setVisible(false);
            }
        } catch (Exception ex) {
            SwingUtils.showExceptionMessage(padre, ex);
        }

        InvoicexEvent e = new InvoicexEvent(this, InvoicexEvent.TYPE_GENERIC_PostInitComps);
        main.events.fireInvoicexEvent(e);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        comListino = new tnxbeans.tnxComboField();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cheGiacenza = new javax.swing.JCheckBox();
        labGiacenzaNote = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        labDeposito = new javax.swing.JLabel();
        comDeposito = new tnxbeans.tnxComboField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        butNuovoListino = new javax.swing.JButton();
        comImportareGiacenza = new javax.swing.JComboBox();
        labImportareGiacenza = new javax.swing.JLabel();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(true);
        setTitle("Import articoli da file Excel");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Listino");

        comListino.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("<html>Selezionare un file Excel (.xls) con le seguenti colonne in ordine:<br>\nA - Codice articolo<br>\nB - Descrizione<br>\nC - Prezzo<br>\nD - Codice IVA<br>\nE - Unità di misura<br>\nF - Codice a barre<br>\nG - Codice articolo Fornitore<br>\nH - Descrizione in inglese<br>\nI - Unità di misura in inglese<br>\nJ - Gestione lotti (S/N)<br>\nK - Gestione matricole (S/N)<br>\nL - Codice Fornitore abituale<br>\nM - Codice Categoria<br>\nN - Codice Sottocategoria<br>\nO - Peso in kg<br>\nP - Giacenza (* vedi sotto)<br>\n<br>\nSe alcuni campi non sono riempiti non verranno azzerati\n</html>");

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/go-jump.png"))); // NOI18N
        jButton2.setText("Scegli il file da importare");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jLabel3.setText("Se avvalori la colonna del prezzo scegli su quale listino assegnarli");

        cheGiacenza.setText("* Importare la giacenza ?");
        cheGiacenza.setEnabled(false);
        cheGiacenza.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        labGiacenzaNote.setText("Se importi la giacenza di un articolo già presente verranno generati i movimenti di magazzino per arrivare alla giacenza indicata");
        labGiacenzaNote.setEnabled(false);

        labDeposito.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labDeposito.setText("Deposito");
        labDeposito.setEnabled(false);

        comDeposito.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comDeposito.setEnabled(false);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/it/tnx/invoicex/res/ico_pro.png"))); // NOI18N
        jLabel6.setToolTipText("Solo per versioni PRO o superiori");

        butNuovoListino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/tango-icon-theme-080/16x16/actions/document-new.png"))); // NOI18N
        butNuovoListino.setText("Nuovo Listino");
        butNuovoListino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNuovoListinoActionPerformed(evt);
            }
        });

        comImportareGiacenza.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NON Importare Giacenza", "Importa giacenza singolo deposito", "Importa giacenza generica" }));
        comImportareGiacenza.setEnabled(false);

        labImportareGiacenza.setText("* Importare la giacenza ?");
        labImportareGiacenza.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(jSeparator1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator3)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cheGiacenza)
                                .add(6, 6, 6)
                                .add(labImportareGiacenza)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comImportareGiacenza, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(labDeposito)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comDeposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comListino, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(butNuovoListino))
                            .add(labGiacenzaNote)
                            .add(jLabel3))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(comListino, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(butNuovoListino))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cheGiacenza)
                        .add(labDeposito)
                        .add(comDeposito, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(comImportareGiacenza, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(labImportareGiacenza)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labGiacenzaNote)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(new java.awt.Component[] {cheGiacenza, jLabel6}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void butNuovoListinoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNuovoListinoActionPerformed
        frmListini frm = new frmListini(this);
        main.getPadre().openFrame(frm, 600, 400);
}//GEN-LAST:event_butNuovoListinoActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (comImportareGiacenza.isVisible()
                && comImportareGiacenza.getSelectedIndex() != 0
                && comDeposito.getSelectedItem() == null) {
            SwingUtils.showErrorMessage(this, "Se scegli di importare la giacenza devi specificare il deposito da movimentare");
            return;
        }

        if (comImportareGiacenza.isVisible()
                && comImportareGiacenza.getSelectedIndex() == 1) {
            if (!SwingUtils.showYesNoMessage(this, "*** ATTENZIONE ***\n"
                    + "Con la funzione 'importa giacenza singolo deposito' verranno creati i movimenti di magazzino sul deposito inserito\n"
                    + "per arrivare alle giacenze indicate come differenze dalle giacenze presenti sul DEPOSITO SPECIFICO.", "Attenzione")) {
                return;
            }
        }

        if (comImportareGiacenza.isVisible()
                && comImportareGiacenza.getSelectedIndex() == 2) {
            if (!SwingUtils.showYesNoMessage(this, "*** ATTENZIONE ***\n"
                    + "Con la funzione 'importa giacenza generica' verranno creati i movimenti di magazzino sul deposito inserito\n"
                    + "per arrivare alle giacenze indicate come differenze dalle GIACENZE GENERALI.", "Attenzione")) {
                return;
            }
        }

        File dirListini = new File(System.getProperty("user.home") + File.separator + ".invoicex" + File.separator + "Listini" + File.separator);
        if (!dirListini.exists()) {
            dirListini.mkdir();
        }
        if (!main.fileIni.getValue("import_articoli", "path", "").equals("")) {
            File ftest = new File(main.fileIni.getValue("import_articoli", "path", ""));
            if (ftest.exists()) {
                dirListini = ftest;
            }
        }

        final JFileChooser fileChoose = new JFileChooser(dirListini);
        FileFilter filter1 = new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.getAbsolutePath().endsWith(".xls")) {
                    return true;
                } else if (pathname.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }

            public String getDescription() {
                return "File XLS (*.xls)";
            }
        };

        fileChoose.addChoosableFileFilter(filter1);
        fileChoose.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int ret = fileChoose.showOpenDialog(this);

        if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                main.fileIni.setValue("import_articoli", "path", fileChoose.getSelectedFile().getParent());

                InputStream inp = new FileInputStream(fileChoose.getSelectedFile());

                HSSFWorkbook wb = new HSSFWorkbook(inp);
                final HSSFSheet sheet = wb.getSheetAt(0);
                final int lastRow = sheet.getLastRowNum() + 1;
                final JDialogInsert dialog = new JDialogInsert(main.getPadre(), false, "", lastRow);
                final String listino = String.valueOf(comListino.getSelectedKey());

                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                try {
                    SwingWorker sw = new SwingWorker() {

                        public Object construct() {
                            try {
                                ImportArticoli.TipoImport tipoImport = ImportArticoli.TipoImport.NO;
                                if (Magazzino.isMultiDeposito()) {
                                    if (comImportareGiacenza.getSelectedIndex() == 1) {
                                        tipoImport = ImportArticoli.TipoImport.GIACENZA_DEPOSITO;
                                    } else if (comImportareGiacenza.getSelectedIndex() == 2) {
                                        tipoImport = ImportArticoli.TipoImport.GIACENZA_TOTALE;
                                    }
                                } else if (cheGiacenza.isSelected()) {
                                    tipoImport = ImportArticoli.TipoImport.GIACENZA_TOTALE;
                                }
                                ImportArticoli loader = new ImportArticoli(fileChoose.getSelectedFile().getAbsolutePath(), listino, lastRow, tipoImport, cu.i(comDeposito.getSelectedKey()));
                                loader.insert = dialog;

                                loader.process();

                                dialog.lblMessaggio.setText("aggiornamento listini");

                                try {
                                    InvoicexUtil.aggiornaListini();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                dialog.lblMessaggio.setText("completamento import");

                                ((frmArtiConListino) padre).dati.dbRefresh();
                                ((frmArtiConListino) padre).griglia.dbRefresh();
                                ((frmArtiConListino) padre).showListino();

                                dialog.dispose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            SwingUtils.showInfoMessage(padre, "Importazione completata");
                            return null;
                        }
                    };

                    sw.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (org.apache.poi.hssf.OldExcelFormatException old) {
                SwingUtils.showErrorMessage(padre, "Il formato del file Excel deve essere 'Excel versions 97/2000/XP/2003'\n" + old.getMessage(), "Formato Excel non supportato");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton butNuovoListino;
    public javax.swing.JCheckBox cheGiacenza;
    public tnxbeans.tnxComboField comDeposito;
    public javax.swing.JComboBox comImportareGiacenza;
    public tnxbeans.tnxComboField comListino;
    public javax.swing.JButton jButton2;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSeparator jSeparator2;
    public javax.swing.JSeparator jSeparator3;
    public javax.swing.JLabel labDeposito;
    public javax.swing.JLabel labGiacenzaNote;
    public javax.swing.JLabel labImportareGiacenza;
    // End of variables declaration//GEN-END:variables
}
