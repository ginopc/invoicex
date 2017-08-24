/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gestioneFatture;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import tnxbeans.tnxComboField;
import tnxbeans.tnxDbPanel;
import tnxbeans.tnxTextField;

/**
 *
 * @author mceccarelli
 */
public interface GenericFrmTest {
    public void aggiornareProvvigioni();
    public JTable getGrid();
    public tnxDbPanel getDatiPanel();
    public JTabbedPane getTab();
    public void selezionaCliente(String codice);
    public boolean isAcquisto();
    public Integer getId();
    public boolean isPrezziIvati();
    public void ricalcolaTotali();
    
    public JLabel getCampoLibero1Label();
    public tnxComboField getCampoLibero1Combo();
    
    public String getNomeTabRigheLotti();
    public String getNomeTabRigheMatricole();
}
