/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

import gestioneFatture.Db;
import gestioneFatture.frmTestOrdine;
import gestioneFatture.logic.clienti.Cliente;
import gestioneFatture.main;
import it.tnx.commons.FormatUtils;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.NumberFormat;

/**
 *
 * @author mceccarelli
 */
public class DataModelFoglioOrdine extends javax.swing.table.DefaultTableModel {

    frmTestOrdine form;
    int currentRow = -1;

    public DataModelFoglioOrdine(int rowCount, int columnCount, frmTestOrdine form) {
        super(rowCount, columnCount);
        this.form = form;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return super.isCellEditable(row, column);
    }

    @Override
    public void setValueAt(Object obj, int row, int col) {
        super.setValueAt(obj, row, col);
    }

    @Override
    public void removeRow(int param) {
        String sql;

        //cancello la riga
        if (form.texNumeOrdine.getText().length() > 0 && form.texAnno.getText().length() > 0 && getValueAt(param, 0).toString().length() > 0) {
            sql = "delete from righ_ordi where ";
            sql += "serie = " + Db.pc(form.texSeri.getText(), Types.VARCHAR);
            sql += " and numero = " + Db.pc(form.texNumeOrdine.getText(), Types.INTEGER);
            sql += " and anno = " + form.texAnno.getText();
            sql += " and riga = " + Db.pc(getValueAt(param, 0), Types.INTEGER);

            if (Db.executeSql(sql) == true) {
                System.out.println("row count:" + getRowCount() + " row to del:" + param);
                super.removeRow(param);
            }

            form.dbAssociaGrigliaRighe();
        }
    }

    public void recuperaDatiArticolo(String codArt, int row) {
        String codicelistino = "0";

        if (codArt.length() > 0) {
            ResultSet temp;
            String sql = "select * from articoli where codice = " + Db.pc(codArt, "VARCHAR");
            temp = Db.openResultSet(sql);

            try {
                if (temp.next() == true) {
                    boolean eng = false;

                    if (form.texClie.getText().length() > 0) {
                        Cliente cliente = new Cliente(Integer.parseInt(form.texClie.getText()));
                        codicelistino = cliente.getListinoCliente(false);
                        if (cliente.isItalian() == true) {
                            eng = false;
                        } else {
                            eng = true;
                        }
                    }

                    if (eng) {
                        setValueAt(Db.nz(temp.getString("descrizione_en"), ""), row, 2);
                    } else {
                        setValueAt(Db.nz(temp.getString("descrizione"), ""), row, 2);
                    }

                    if (form.tipoSNJ != null && form.tipoSNJ.equals("A")) {
                        sql = "select prezzo, sconto1, sconto2 from articoli_prezzi";
                        sql += " where articolo = " + Db.pc(codArt, "VARCHAR");
                        sql += " and listino = " + Db.pc(codicelistino, java.sql.Types.VARCHAR);

                        ResultSet prezzi = Db.openResultSet(sql);

                        if (prezzi.next() == true) {
                            setValueAt(Db.formatDecimal5(prezzi.getDouble(1)), row, 5);
                        } else {
                            sql = "select prezzo, sconto1, sconto2 from articoli_prezzi";
                            sql += " where articolo = " + Db.pc(codArt, "VARCHAR");
                            sql += " and listino = " + Db.pc(main.getListinoBase(), java.sql.Types.VARCHAR);
                            prezzi = Db.openResultSet(sql);

                            if (prezzi.next() == true) {
                                setValueAt(Db.formatDecimal5(prezzi.getDouble(1)), row, 5);
                            }
                        }
                    }
                } else {
                    form.labStatus.setText("Non trovo l'articolo:" + codArt);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    public String getDouble(Object valore) {
        if (valore == null) {
            return "0";
        }
        NumberFormat numFormat = NumberFormat.getInstance();
        try {
            return Db.pc(numFormat.parse(valore.toString()), Types.DOUBLE);
        } catch (Exception err) {
            return "0";
        }
    }

    public String formatDouble(double number) {
        return FormatUtils.formatEuroIta(number);
    }
}
