/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx;

import java.sql.Connection;

/**
 *
 * @author mceccarelli
 */
public interface DbI {

    public Connection getDbConn();  //se disponibile ritorna la connessione già in uso
    public Connection getDbNewConnection() throws Exception; //ritorna una connessione nuova

}
