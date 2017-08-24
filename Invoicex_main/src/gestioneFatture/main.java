/**
 * Invoicex Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza GNU
 * General Public License, Version 2. La licenza accompagna il software o potete
 * trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the GNU General
 * Public License, Version 2. The license should have accompanied the software
 * or you may obtain a copy of the license from the Free Software Foundation at
 * http://www.fsf.org .
 *
 * -- Marco Ceccarelli (m.ceccarelli@tnx.it) Tnx srl (http://www.tnx.it)
 *
 */
package gestioneFatture;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.lowagie.text.pdf.ByteBuffer;
import gestioneFatture.logic.provvigioni.ProvvigioniFattura;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import it.tnx.Db;
import it.tnx.MyStreamGobblerMain;
import it.tnx.PrintUtilities;
import it.tnx.SwingWorker;
import it.tnx.accessoUtenti.Utente;
import it.tnx.commons.CastUtils;
import it.tnx.commons.DateUtils;
import it.tnx.commons.DbUtils;
import it.tnx.commons.DebugFastUtils;
import it.tnx.commons.DebugUtils;
import it.tnx.commons.FileUtils;
import it.tnx.commons.HttpUtils;
import static it.tnx.commons.HttpUtils.getHttpClient;
import it.tnx.commons.MicroBench;
import it.tnx.commons.RunnableWithArgs;
import it.tnx.commons.SwingUtils;
import it.tnx.commons.SystemUtils;
import it.tnx.commons.agg.UnzipWorker;
import it.tnx.commons.cu;
import it.tnx.commons.dbu;
import it.tnx.commons.swing.EventDispatchThreadHangMonitor;
import it.tnx.invoicex.Attivazione;
import it.tnx.invoicex.DbVersionChanges;
import it.tnx.invoicex.InvoicexUtil;
import it.tnx.invoicex.Magazzino;
import it.tnx.invoicex.Main;
import it.tnx.invoicex.PlatformUtils;
import it.tnx.invoicex.Plugin;
import it.tnx.invoicex.Versione;
import it.tnx.invoicex.gui.JDialogBugMassimaleRivalsa;
import it.tnx.invoicex.gui.JDialogBugMovDepArrivo;
import it.tnx.invoicex.gui.JDialogDatiAzienda;
import it.tnx.invoicex.gui.JDialogExc;
import it.tnx.invoicex.gui.JDialogInstallaPlugins;
import it.tnx.invoicex.gui.JDialogPlugins;
import it.tnx.invoicex.gui.JDialogProxyAuth;
import it.tnx.invoicex.gui.JDialogRiattivaPlugins;
import it.tnx.invoicex.gui.JDialogUpd;
import it.tnx.invoicex.gui.JFrameIntro2;
import it.tnx.invoicex.sync.Sync;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;

import java.util.*;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.*;
import javax.swing.text.DefaultEditorKit;
import mjpf.EntryDescriptor;
import mjpf.PluginEntry;
import mjpf.PluginFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import tnxbeans.tnxDbPanel;

public class main {

    public static main INSTANCE = null;

//    static public boolean debug = true;
    static public boolean debug = false;

//    static public String build = "20081201-1";
//    static public String build = "20081211";    //gestione listini con ricarico
//    static public String build = "20081212";    //migliore su generazione movimenti e filtro in statistiche per non mettere le proforma
//    static public String build = "20081215";    //corretto problema su doppio click situazione fornitori
//    static public String build = "20081218";    //corretto problema su articoli con listini a percentuale, aggiunta possibilità di articolo di tipo servizio, corretto finestra situazione clienti che non spariva..., ricerca su articoli non faceva +
//    static public String build = "20081222";    //aggiunta stampa paese se non italia
//    static public String build = "20090107";    //problema su stampa paese, svuoto errore di stampa
//    static public String build = "20090108";    //problema su stampa fatture le scadenze veniva sommate alle scadenze delle fatture ricevute
    //aggiornata release sourceforge 08/01/2009
//    static public String build = "20090113";    //problema su tnxBeans.jar
//    static public String build = "20090115";    //problema su tnxBeans.jar, su modifica righe fatture precedenti venivano eliminate le righe, aggiunto pulsante duplica
//    static public String build = "20090121";    //aggiunto prezzo con iva per scorporo
//    static public String build = "20090128";        //font su mac osx più piccolo, tolti temi substance per problema freeze su clienti, aggiunti temi toniclf e officelf
//    static public String build = "20090204";        //risolto problema decimali su aggiorna iva, problema non stampava la serie sui documenti, ritenuta d'acconto a 2 decimali
//    static public String build = "20090212";        //inserimento plugin ricerca, aggiunta impostazione per vedere solo i documenti dell'anno in corso, aggiunti controlli anagrafica clienti
//    static public String build = "20090213";        //calcolo sconto in righe, sconti portati a 3 cifre per il 100%, salvataggio filtri situazione/clienti/fornitori
//    static public String build = "20090217";        //migliore su gestione agenti con nuovo report per agente, aggiunta generazione provvigioni in automatico su conversione ddt a fattura, aggiunto codice iva 41 e scelta automatica in inserimento righe in base al cliente
//    static public String build = "20090218";        //aggiunto parametro su stampa registro iva per usare data documento esterno
//    static public String build = "20090219";        //debug click situazione clienti
//    static public String build = "20090304";        //aggiornati Db per connessioni più sicure
//    static public String build = "20090311";        //risolto problema arrotondamento in alcuni casi con cifre a più di due decimali
//    static public String build = "20090312";        //migliorie su uscita programma (problemi di bloccaggio in uscita su Mac risolti) migliorie su gestione destinazione diversa du prev. ddt e fatture, riattivato tema jgoodies su mac, riaggiunti temi substance dopo modifica per alcuni bugs, invio segnalazione errore generico, problema su articoli con stessa descrizione
//    static public String build = "20090316";        //Risolti alcuni bug su dati vuoti, ritorno da inserimento riga a testata documento, corretto problema creazione icona su ubuntu 8.x, correzione su apertura folder e link su linux
//    static public String build = "20090327";        //Risolto problema paese diverso in destinazione diversa e caricamento del pagamento su fattura su apertura per modifica
//    static public String build = "20090331";        //Aggiunta possibilità di raggruppare i DDT in conversione a Fattura
//    static public String build = "20090403";        //Aggiunta possibilità di gestire le scadenze per gli ordini e preventivi e export di vendite e acquisti su pdf, excel e html, risolti problemi su destinazione diversa
//    static public String build = "20090406";        //Aggiunta possibilità di richiedere la password (del database) all'avvio del programma
//    static public String build = "20090407";        //Portato numero righe sul foglio righe a 1000 invece che 100, tolti N in um e 20 su iva
//    static public String build = "20090414";        //Corretta stampa articoli con calcolo prezzo per listini a percentuale
//    static public String build = "20090415";        //Corretto funzionamento da proforma a fattura per spostamento scadenze su nuova fattura e generazione movimenti, Aggiunti flag visualizzazione note di credito e proforma in scadenze
//    static public String build = "20090416";        //Portati servizi di invoicex su server tnx.it, aggiunto invio log su segnalazione, anche su creazione db per risolvere problemi all'avvio
//    static public String build = "20090417";        //Test per bug mac osx con mysqld, incongruenza settaggio tema su mac osx, risolto problema reset password su aggiornamento, rivista procedura di creazione db inziale ed agganciata in impostazioni
//    static public String build = "20090514";        //Aggiunta gestione pagamenti parziali delle scadenze
//    static public String build = "20090515";        //stampa situazione clienti come a video, aggiunti controlli su dimensioni logo e sfondo per stampa (errore heap space out of ..)
//    static public String build = "20090521";        //corretta generazione scadenze per fatture di acquisto per data documento esterno invece che data registrazione
//    static public String build = "20090522";        //aggiunta funzione di inserimento righe tra altre righe
//    static public String build = "20090525";        //correzione scadenze su fatture di acquisto, articoli separati vend e acq, prova xerces per problema su mac sax parser
//    static public String build = "20090526";        //risolto bug su get mac address win vista, nel caso di ordine a ddt a fattura non riportava il pagamento selezionato
//    static public String build = "20090608";        //risolto bug su F4 in ricerca su nuova riga e su apertura pdf in linux, aggiunta gestione serie_fatture in dati azienda
//    static public String build = "20090630";        //aggiunte stampe per ritenute d'acconto, migliorie sulla gestione degli errori e bug fix per posizionamenti delle finestre
//    static public String build = "20090706";        //Prezzi di vendita e acquisto su stampa articoli e stampa giacenze, migliorie a classe Db, controlli su ordini/ddt e fatture per perdita righe ma no totali, aggiunti messaggi di controllo e facilitata la chiusura del programma
//    static public String build = "20090709";        //Prezzi di listino su giacenze
//    static public String build = "20090714";        //cambiata gestione degli statement su tnxbeans
//    static public String build = "20090720";        //cambiata gestione elenco finestre
//    static public String build = "20090721";        //ottimizzata apertura situazione clienti fornitori
//    static public String build = "20090724";        //aggiunti tooltip in situazione clienti e data in giacenze
//    static public String build = "20090730";        //aggiunto controllo totale scadenze con totale fattura per rigenerare le scadenze
//    static public String build = "20090812";        //possibilità di usare un codice iva alfanumerico
//    static public String build = "20090813";        //kill di mysqld su uscita brutale, tolta colonna sconti se non presenti, risolto bug su cambio numero e annulla, aggiunta colonna note su situazione pagamenti, migliorie su inserimento righe per usare solo tastiera, problema di stampa in linux
//    static public String build = "20090814";        //look and feel da jgoodies
//    static public String build = "20090817";        //controllo errori su ridimensionamento griglie,
//    static public String build = "20090818";        //modifiche per gestione scontrini
//    static public String build = "20090819";        //modifiche per gestione degli aggiornamenti dei plugins
//    static public String build = "20090831";        //Corretto bug test porte su mac per avvi odatabase locale
//    static public String build = "20090901";        //Corretto bug su colorForRow null
//    static public String build = "20090915";        //Implementata gestione articoli composti
//    static public String build = "20090917";        //Implementata gestione articoli composti e controllo giacenza
//    static public String build = "20090924";        //Implementato invio personalizzazione report, Possibilità di filtrare statistiche per articolo
//    static public String build = "20091008";        //problema su stampa fatture, non prendeva mai dati intestazione. Possibilità di inserire note di credito in negativo.
//    static public String build = "20091009";        //Problema email in agenti
//    static public String build = "20091012";        //Impostata data corrente su cambio da proforma a fattura
//    static public String build = "20091015";        //Aggiunto totale riga nell'inserimento articolo e in elenco righe dei ddt e ordini, aggiunta ricerca testuale nella casella cliente in elenco prev. ddt e fatture
//    static public String build = "20091021";        //Integrazione modifiche per gestione scontrini
//    static public String build = "20091022";        //Nuova funzione di controllo plugin nel caso siano danneggiati vengono riscaricati, Aggiornamento setup per windows 7
//    static public String build = "20091027";        //Gestione di matricole integrata, Migliorata ricerca articoli (per adesso solo in fattura), Migliorata anagrafica articoli
//    static public String build = "20091028";        //Corretta generazione DDT raggruppando articoli e risolto bug su converti documenti (nuovo campo id autoinc), Corretto problema su fioglio righe
//    static public String build = "20091030";        //Aggiornamenti per gestione scontrini
//    static public String build = "20091104";        //Aggiornamenti per gestione ritenute e no pro forma in iva
//    static public String build = "20091106";        //Corretto calcolo rivalsa e ritenuta
//    static public String build = "20091110";        //Corretto calcolo iva per arrotondamento (faceva arrotondamento al 3 e poi al secondo decimale, ERRATO, deve essere direttamente alla seconda cifra decimale), coretto raggruppamento DDT in fattura
//    static public String build = "20091111";        //Correzione su conversione da fattura a ddt, nuovo report ordini per destinazione diversa, errori su export statistiche preventivi
//    static public String build = "20091113";          //Correzione su conversione da ordine a fattura
    //Correzione su selezione del logo in preventivi e fatture su scelta del fornitore
    //Aggiunto codice fornitore e codice a barre in anagrafica articoli e ricerca in inserimento riga
    //Aggiunta immagine su articoli
//    static public String build = "20091119";        //Aggiunta possibilità di esportare DDT in Excel
//    static public String build = "20091120";        //Risolto problema destinazioni diverse in DDT
//    static public String build = "20091201";        //Modifiche per Luxury Company
//    static public String build = "20091211";        //Implementato Undo e Redo + menu contestuale su caselle di testo per Copia e Incolla
//    static public String build = "20091217";        //Unite modifiche vari clienti con vecchi lavori
    //velocizzato il controllo delle modifiche al database in partenza
    //Aggiunto scrolling al desktop di Invoicex per gestire meglio più finestre
    //corretto bug di creazione tabelle tmeporanee per undo
    //abilitato all'assistenza anche Mac tramite Vine Vnc
    //Aggiunto totale sconti su report preventivi
    //Aggiunti export e import righe da CSV
//    static public String build = "20091218";        //Aggiunta attivazione e export su cvs di vendite e acquisti
//    static public String build = "20091221";        //Correzioni per mac icona ricerca e foto articoli
//    static public String build = "20100106";        //Aggiunta gestione Lotti nei movimenti di magazzino
//    static public String build = "20100107";        //Corretto bug su linux e mac in presenza di altro mysql
//    static public String build = "20100109";        //Migliorati avvisi di errore su wizard db iniziale
//    static public String build = "20100119";        //Aggiunto import Clienti da excel
//    static public String build = "20100126";        //Rsiolto problema movimenti kit e lotti
//    static public String build = "20100205";        //Risolto problema con gestione scontrini
    //Risolto problema su statistiche export e date in inglese
    //Aggiunta scelta listino su reimport righe
    //Risolto problema posizionamento finestra scelta iva
//    static public String build = "20100210";        //Aggiunta esportazione di ogni tabella in Excel
    //Corretto import articoli da file Excel, non inseriva i prezzi in alcuni casi
//    static public String build = "20100211";        //Corretto bug in duplicazione Fatture
//    static public String build = "20100212";        //Corretto bug salvataggio fatture con iva null
//    static public String build = "20100310";        //
//    static public String build = "20100318";
    /*
     * Aggiunte:
     * Nuovo sistema per posizionare e ridimensionare il logo in stampa
     * Anagrafica Porti e causali
     * Aggiunta ricerca su movimenti di magazzino
     * Milgiorie su inserimento prezzi articoli
     * Eliminazione multipla su movimenti di magazzino e articoli
     * Aggiunta possibilità di ricaricare il magazzino inserendo una nota di credito
     * Nuovo sistema per selezionare il cliente in nuova Fattura e Situazione Clienti
     * Stampa delle matricole e dei lotti nei documenti
     *
     *
     * Risolti problemi in stampa su linux
     * Corretto gestione del giorno di pagamento
     * Possibilità di ricercare clienti e fornitori in Situazione Clienti tramite il codice numerico.
     * Risolto problema decimali su preventivi
     * Controllate le scadenze per la rigenerazione delle provvigioni
     * Cap su anag. agenti che non veniva salvato
     * Risolto problema codice fornitore su fatture in acquisto
     * Risolto problema su prezzi con aumento in percentuale e servizio
     * Risolti problemi con Foglio Righe in fattura
     * Risolto problema della sparizione dell'eleimina riga
     * Risolto problema sulla funzione duplica che non generava i movimenti
     *
     */
    //static public String build = "20100330";        //debug problema attivazione
//    static public String build = "20100422";        //aggiornato plugin ricerca
//    static public String build = "20100423";        //migliorato import clienti
//      static public String build = "20100504";        //aggiornamento plugin ritenute, specifica applicazione ritenuta su riga
    //migliorato controllo Piva e Cod Fiscale su anag. cliente
//      static public String build = "20100511";        //Migliorie per utilizzo via internet
    //corretti piccoli bugs su Anagrafiche Clienti
//      static public String build = "20100512";        //Migliorie per utilizzo via internet
//                                                        //Aggiunta settimana scadenza
//      static public String build = "20100517";        //Aggiunta gestione provvigioni per scaglioni
//      static public String build = "20100520";        //Aggiunta gestione provvigioni per scaglioni
//      static public String build = "20100521";        //Problema esportazioni pdf (per main.wd)
//      static public String build = "20100531";        //Parametrizzata scritta su conversione da ordine e da ddt
//      static public String build = "20100603";        //Migliorata selezione articolo con descrizione su più righe
    //Aggiunta scelta prezzo dai listini in inserimento riga
    //Correzione generazione movimenti in conversione da Preventivo a DDT
    //nuove griglie per caricamento tabelle molto grandi
    //possibilità di scegliere il prezzo da un altro listino in inserimento riga
//    static public String build = "20100611";        //Corretto bug su plugin ricerca con nuova ricerca cliente in testata fattura
//    static public String build = "20100617";        //Corretto bug modifica fattura accompagnatoria
//    static public String build = "20100621";        //aumentato a 60 da 15 timeout resultset
//    static public String build = "20100630";        //corretto lazyresultset in lista articoli
//    static public String build = "20100702";        //Aggiunta specifica della provvigione agente su ogni riga (prima era sulla testata del documento)
    //Migliorata conversione documenti da ddt e ordine a fattura per recuperare le coordinate bancarie del cliente
    //Diviso su anagrafica articoli flag 'Servizio' da 'Applicare ricarico listino a percentuale's
    //Controllati problemi su salvataggio
    //Corretti "+ Aggiungi ... " su Form Cliente
//    static public String build = "20100709";        //
//    static public String build = "20100720";        //scontrini
//    static public String build = "20100903";        //bug fixing vari assistenza su win7 , conversione dati bancari, ssl, nome file cliente con slash
//    static public String build = "20100930";        //user name fino a 250 caratteri per problemi su tabelle temporanee
    //raggruppamento riepilogativo dei ddt in conversione a fattura (1 ddt -> 1 riga di fattura)
    //ordini di acquisto e ddt di acquisto (alfa)
    //provvigoni agenti su riga oltre che su documento e report dettagliato
//    static public String build = "20101004";        //correzione conversione ddt a fattura
//    static public String build = "20101013";        correzioni:
    /*
     colori griglie in base al tema saltavano fuori caselle nere
     generazione fatture di acquisto dal ddt di acquisto
     generazione movimenti su ddt di acquisto
     richiesta generazione movimenti su fatture di acquisto
     ritorno su documento selezionato negli elenchi
     */
//    static public String build = "20101014";        //problema newseuropa
//    static public String build = "20101015";        //aggiornmaento per non comprendere la rivalsa nell'imponibile iva (ponente wine)
//    static public String build = "20101022";        //stampa ordine acquisto corretta
    //stampa registro iva corretta
//    static public String build = "20101115";
    //evasione parziale degli Ordini
    //fatturazione parziale dei DDT
    //calcolo giacenza con Ordinato e Venduto
    //stampa ragione sociale su più righe
    //correzioni grafiche su tema standard
    //corretto ordine righe su esportazione da Ordine ad altro documento
    //corretto bug posizione immagine
    //corretto bug visualizzazione articoli
    //corretto note automatiche da cliente
    //corretto dump backup per problemi memoria
    //corretta stampa iva con fino a 4 percentuali
    //risolto problema su conversione + ddt e raggruppamento per articolo
    //aggiunta personalizzazione carburante per spese carburante invece che spese di trasporto
    //aggiunto campo data consegna su preve. e ordini
    //online con version 1.8.0
//    static public String build = "20101122";
//    static public String build = "20101213";
    /*
     - problema non rigenerava provvigioni su cambio scadenze
     - migliorato box rihiesta assistenza
     - migliorata rimozione plugin scaduti
     - migliorata stampa liquidiazione iva
     - migliorati prezzi per Clienti, tabella di editing in anagrafica Cliente
     */
//    static public String build = "20101216";
    /*
     - migliorato export Acquisti/Vendite con filtro per articolo, calcolo importi scontati e export di tutti i documenti
     - migliorato Statistiche con possibilità di stampare le quantità, sfondo del grafico bianco, correzione ordine mesi.
     */
//    static public String build = "20101222";    //perle gitane temp
//    static public String build = "20101223";    //piccoli bug fixing su conversione da ddt a fattura
//    static public String build = "20101229";    //perle gitane temp
//    static public String build = "20110103";    // protekno
    // inserimento quantità tramite numero scatole
    // conversione da ordine a fattura di acquisto
    // fino a sei codice fornitore in articoli
//    static public String build = "20110105";    // perle gitane
//    static public String build = "20110110";    // aggiunto agente su import clienti
//    static public String build = "20110111";    // aggiunto colonne aggiuntive su elenchi fatture, ddt ordini e situazione clienti
//    static public String build = "20110112";    // perle gitane web
//    static public String build = "20110114";    // unione sorgenti alessio toce
    /*
     filtro per causale ddt e colonne aggiuntive per riferimento
     gestione rivalsa per agenti di commercio (vedi rivalsa 6,75% da sotrrarre, nuova opzione in impostazioni)
     */
//    static public String build = "20110117";    // unione sorgenti alessio toce
    /*
     import ipsoa
     modifiche oman per filtraggi osu ddt
     */
//    static public String build = "20110118";    // unione sorgenti alessio toce
    //bug su inserimento listini a ricarico e visualizzazione tutti i listini su anag articoli
    //incluse modifiche ipsoa e oman
    //static public String build = "20110121";
    /*
     autenticazione proxy (distilleria fatebenefratelli)
     */
//    static public String build = "20110124";
    /*
     personalizzazioni
     */
    //static public String build = "20110131";
    /*
     aggiunto riconoscimento proxy per navigazione
     corretto form richiesta assistenza
     corretto export righe csv su ordini acquisto e ddt acquisto
     corretto modifica riga con matricole associate (id_padre null)
     */
//    static public String build = "20110218";
    /*
     ottimizzazione per uso via internet su inserimento movimenti di magazzino
     debug ibrain lentezza/blocco
     problema nomi file in caso di windows mail e file con tanti puntini
     risolto un thread lock su apertura preventivi (tnxComboField edt)
     risolto problema paese di destinazione non azzerabile
     risolto problema modifca cliente su preventivi in modifica

     risolto problema causali di trasporto
     risolto problema elenco iva in articoli
     risolto problema iva con descrizione lunga in stampa
     aggiunto minimo ordinabile perle gitane

     */

 /*
     static public String build = "20110315";

     risolto problema import righe da CSV in caso di tabella righe vuota
     nascosto checkbox 'applica ritenuta d'acconto' su inserimento righe se non c'è nè bisogno
     aggiunta possibilità di collegamento via tunnel ssh
     risolto problema accenti su ragione sociale in registrazione dati utente
     aggiunto personalizzazione 'open' per apertura file con altro comando (vedi Crea Pdf per email)
     implementato Manutenzione con controllo movimenti senza documenti
     aggiunta possibilità di includere un listino prezzo consigliato su stampa DDT
     corretta stampa liquidazione iva in caso di costi indeducibili
     aggiunto teamviewer come assistenza remota
     aggiunta scelta font in impostazioni
     aggiunta possibilità di recuperare righe fattura da una versione precedente
     corretta movimentazioni articoli con gestione matricole su ddt di acquisto
     corretto export e import csv in ddt di acquisto
     */

 /*static public String build = "20110330";
     cancellazione documenti con controllo su documenti di provenienza
     impostazione carattere interfaccia grafica
     impostazione listino consigliato standard
     aumentato log sql
     */

 /*static public String build = "20110407";
     impostazione listino consigliato standard, correzione
     corretto problema su recupero database
     corretto problema su raggr in conversione documenti e su ristampa distinta riba
     */

 /*static public String build = "20110418";
     aggiunte librerie per plugin Email
     aggiornato plugin riba da toce
     in stampa articoli adesso visualizza anche quelli senza il prezzo associato
     */

 /*static public String build = "20110419";
     corretto arrotondamento prezzi totali di riga, quindi totale imponibile = somma dei totali di riga arrotondati
     corretto bug rigenerazione scadenze su cambio numero documento
     */

 /*
     static public String build = "20110509";
     ipsoware, gaia servizi, conenna
     */

 /*static public String build = "20110520";
     corretta versione online per mac e linux
     vannuccini, conenna
     aggiunto controllo tabelle prima dei cambi di struttura
     corretta gestione note di credito in provvigioni
     passaggio a 1.8.1
     tolto personalizzazione conenna su nuovo articolo
     */

 /*static public String build = "20110607";
     pulitura sorgenti
     aggiornamento gestione plugin (plugin privati, compry, achievo e client manager)
     check table ignorare: Found row where the auto...
     centralizzato logo e sfondo su db per installazioni in rete o via internet
     aggiunto controllo windows vista program files
     correzioni stampa fattura mod4 per casella sconto su riga errato
     migliorie su visualizzazione giacenze (filtro e flag giacenza a zero)
     */

 /*static public String build = "20110701";
     corretto 'open' per percorsi con spazi su windows
     corretta generazione scadenze su fatture di acquisto in caso di ritenuta d'acconto
     corretta estrazione preventivi di vendita filtrando per cliente
     corretta stampa liquidazione iva dopo aggiornamento per deducibilità non conteggiava bene le spese accessorie, trasporto
     migliorati messaggi di errore su inserimento destinazioni diverse
     problema logo in db con ragione sociale, spostata pk su id
     parametrizzate etichette Cliente e Destinazione merce
     corretto Costi ineducibili in report iva
     */

 /*static public String build = "20110715";
     problema destinazione diversa invertita su stampa preventivi e ordini
     aggiunto controllo chiusura con attesa chiusura finestre documenti aperti
     aggiunto controllo apertura fatture
     problema ricerca articolo in export srtatistiche venduto
     corretto backup per campi binary, vedi nuovo campo logo in dati azienda

     integrazione tnxBeans e tnxUtil dentro Invoicex
     distribuzione pluginEmail
     */
//    static public String build = "20110721";
    /*
     problema logo e scritta fornitore modifica Bianconi
     corretto bug su username con apostrofo
     corrette note automatiche che sovrascrivevano note su DDT e Ordine
     migliorie su recupero database (bug USING BTREE e drop di tutte le tabelle)
     migliorato controllo merce in arrivo e in uscita considerando solo ordini e non preventivi
     Aggiunto flag Iva a 30 giorni sui tipi di pagamento per aggiungere scadenza fissa a 30gg o 30gg fine mese per addebitare l'importo IVA
     */

 /*static public String build = "20110912";
     modifiche cd garage, brignoli, wdr
     corretto campo convertito su conversione
     corretto pluginScontrini (movimenti, numeri...)
     corretto problema mac address in attivazione
     corretto mysqladmin su osx x86
     */

 /*static public String build = "20111109";
     iva 21
     correzzioni iva 21
     test ibrain
     correzione iva 20 su spese documenti precedente al 17/09/2011
     correzione calcolo otali dopo correzzione iva 20 di cui sopra
     modifica stampe per p.iva sotto intestazione
     corretto iva spese se impostato a altro (non 21) per documenti precedenti iva 21
     correzioni su prezzi articoli e form listini
     correzioni plugin Scontrini Lanzoni
     allargato campo data dopo aggiornamenti per partita iva (non aggiornava i listini a ricarico e non faceva vedere subito i cambiamenti nella form articoli)
     corretto problema non chiusura mysql (stop dei plugin, soprattutto pluginRicerca)
     aggiunto paramentro max allowed packet a 64M per ripristino loghi grandi
     corretto gestione errore su ripristino backup (anche su plugin)
     corretto restore dump fra win/mac (lettura in utf8)
     aggiornato plugin ricerca e backup
     nuovo splash screen
     corretto posizionamento finestra nuova riga
     scritta vostro riferimento corretta per mac su stampe preventivo
     aggiunti sconti su prezzi articoli e su anag cliente/fornitore
     aggiunti ordinamenti su export totali fatture
     corretto report agenti dettagliato
     nuovo splash screen :)
     */

 /*static public String build = "20111202";
     ottimizzate le dimensioni delle finestre per un minimo di 1024 x 600
     configurabilità comandi per apertura cartelle, file e pdf (tolta personalizzazione open)
     completati report fatture e fatture accompagnatorie in inglese
     duplicazione righe documenti
     possibilità di marcare una riga di un documento come 'descrizione' e quindi occupare tutte le colonne in stampa
     calcolo totale spese di trasporto e incasso su conversione multipla ddt a fattura
     migliorata scrittura movimenti in caso di archivio articoli molto grande
     migliorata ricerca articoli in caso di archivio articoli molto grande
     importi a 5 decimali su apertura prezzi di listino dall'inserimento righe
     corretto bug annullamento conversione ddt a fattura creava comunque la fattura
     nuova funzionalità di approssimazione con parametro in inserimento righe (da attivare nelle impostazioni)
     nuova opzione per togliere la scritta di Invoicex nelle stampe
     possibilità di portare la serie serie in conversione documenti
     nuova opzione stato preventivo in conversione documenti
     */

 /*static public String build = "20111207";
     bug fix del precedente update
     */
 /*static public String build = "20111216";
     bug fix del precedente update ...
     errori su import ipsoa per totali a zero, conversione falliva per campo riferimento nullo...
     aggiunto calcolo totali righe su ogni azione
     corretto duplica per informazioni da non duplicare (evaso, riferimenti documenti..)
     corretta ricerca articoli che duplicava i risultati...
     corretto input todouble foglio righe (cirri) prendeva il punto come migliaia
     corretto converti a fattura da ddt
     corretto converti a fattura da ddt per acquisto
     corretto aggiornamento stato prev. e ord. con <lascia invariato>
     problema invio plugin
     */

 /*static public String build = "20111230";
     migliorata stampa liquidazione iva con quarta colonna iva
     corretta stampa documenti per totale_imponibile di riga
     corretto parametro_arrotondamento a 0 se vuoto, problema totali di riga a zero
     */

 /*static public String build = "20120105"; 
     corretto bug ricerca cliente su ele fatt con tema jgoodies
     corretto bug cambio anno ricalcolo progressivo
     corretto bug stampa registro iva su fatture di acquisto con sconti di testata
     */

 /*static public String build = "20120119";
     * problema generazione movimenti su articoli con apostrofo nel codice articolo
     * problema omaggi totali uguali a riga originante
     * problema conversione ddt a fattura con gestione scontrini
     * aggiunte note parametri di stampa Statistiche Totale fatture
     * problema salvataggio in anagrafica listini che azzerava i prezzi caricati   
     */
 /*static public String build = "20120125";
     * migliorato e aggiunti la maggior parte dei campi disponibili per l'import clienti/fornitori da excel
     * corretto filtro su anagrafiche clienti/fornitori
     * aggiunto controllo derivazione da ddt su generazione movimenti in fatture di acquisto
     * corretto ricerca per cliente in fatture di vendita (ancora problema di focus)
     * corretta ricerca articoli con invio in descrizione
     * problema su elimina righe da grid per resultsemetadata null
     */
    //static public String build = "20120315";
    /*
     apertura pdf su ordini e fatture di acquisto
     personalizzazioni SNJ
     personalizzazioni NetUnion
     aggiunta conferma su annullamento documenti
     corretto problema logo fornitori su preventivi/ordini
     corretti accenti (urlencode con utf8) su invio mail di assistenza
     possibilità di inserire numero documento esterno alfanumerico di 50 caratteri per le fatture di acquisto
     aggiunta richiesta di conferma su pulsante Annulla in preventivi ddt e fatture
     aggiunto Claudio Romeo nell'about

     correzione movimenti da fatture di acquisto
     variazioni su apertura combo agenti e clie/forn

     correzioni layout testando su mac
     copia di backup nella cartella Documenti\Invoicex\backup
     corretta generazione scadenze su cambi odata fatture di acquisto
     gestione sconto a importo
     gestione prezzi ivati

     corretto bug JIDE linux
     aggiunta update totali pre sconto e prezzi ivati

     20120315 problema ritenuta in stampa
     */
    //static public String build = "20120329";
    /*
     modifiche net union / litri
     modifiche birra peroni (personal peroni)
     aggiunta scelta colonna importo su elenco fatture di acquisto (con plugin ritenute)
     layout osx e dbUndo
     correzione su eliminazione fattura proveniente da ddt non toglieva sempre i riferimenti sul ddt
     aggiunto import righe da excel su ordini per Proskin (personal: proskin)     
     */

 /*
     static public String build = "20120412";
    
     modifiche idrocclima
     personal no-colori-iva
     personal proskin, import da excel per ordini
     flag_rivalsa
     nuovo report certificazione imposta
     rimozione dei riferimenti su eliminazione fattura
     aggiunto controllo installazione in locale con Invoicex già aperto per sovrascrittura dati

     massimale su rivalse
     descrizione tipi pagamento su report
     modifiche per cambiare serie in conversione documenti
     correzioni SNJ
     */

 /*static public String build = "20120419";
    
     problema stampa registro iva (flag_rivalsa solo con plugin ritenute)
     problema generazione scadenze con scontrini
     implementata gestione lotti su conversione documenti
     implementati omaggi su import proskin
     */

 /*static public String build = "20120503";
     aggiunta conversione del punto in virgola nell'inserimento dei rpezzi in anagrafica articoli
     personal pagamento_stampa_codice per stmapare codice di pagamento invece che descrizione nei documenti
     problema assegnazione numero su duplica
     problema note automatiche su selezione cliente in ddt e ordini/preventivi
     */

 /*static public String build = "20120517";
     corretto estrazione export acquisti vendite con filtro su fornitore in caso di fatture di acquisto
     nuova opzione Provvigioni su data fattura (invece che date scadenze)
     corretto calcolo totale per problema con rivalsa
     gestione scadenze fino a 12 mesi
     */

 /*static public String build = "20120528";
     corretto bug in salvataggio dati articoli, azzerava prezzo se flag listino a ricarico era nullo
     download dei file mysql/data/mysql/proc* per problema sotred procedure e corretto dbchanges per problema aggiunta campo a dati azienda
     */

 /*static public String build = "20120718";
     new:
     gestione utenti
     l'attivazione della licenza adesso scarica i plugins della versione acquistata
     export teamsystem fatseq
     migliorata finestra plugins
     destinatario e dest. diversa personalizzabile da impostazioni
     migliorato spostamento finestre interne per andare fuori dai margini
     possibilità di aggiungere un menu' personale tramite param_prop.txt
     bugs:
     import csv con listino diverso corretti totali riga
     forzato engine a MyIsam per database con innodb come default
     download del createdb della giusta versione in caso di wizard nuovo db da versioni invoicex vecchie
     corretto bug su scelta immagine non jpg in impostazioni
     in Statistiche tolti i preventivi e lasciati gli ordini
     forzato charset ISO-8859-1 in backup e restore per problema accenti fra sistemi diversi
     corretto lettura cartella utente per aprire cartella Documenti in caso di utente con accenti
     */

 /*static public String build = "20120726";
     bugs:
     corretto problema pulsante 'Nuovo' su anagrafiche senza records
     corretto problema cambiamento impostazioni database in caso di problemi di apertura database
     apertura pagamento scadenze su posizione mouse
     aggiunto pulsante salva e chiudi su pagamento scadenze
     tasto destro su griglie seleziona la riga se non selezionata o selezionata solo una riga
     corretta eliminazione riferimenti documenti di acquisto
     migliorata gestione lotti in movimenti di magazzino e lotti omaggio
     correzione per aggiornare totali dopo import righe csv
     aggiunto controllo numerazione fatture e ddt di vendita in automatico e via Utilità->Manutenzione
     aggiunta eliminazione file temporanei e vecchie versioni di Invoicex e libs
     */

 /*static public String build = "20120917";
     bugs:
     problema import cc
     problema eliminazione righe dal fogli orighe cliccando su righe vuote se avevano stesso numero riga di altre
     migliorata conversione documenti con scelta di importare la riga o meno
     migliorata gestione scadenze/provvigioni, in caso di non cambiamento di data e/o importo della scadenza non vengono rigenerate le provvigioni
     */

 /*static public String build = "20120919";
     bugs:
     corretto problema generazione provvigioni in presenza di sconto su totale a importo
     migliorato resize pannello fatture di vendita per entrare bene su schermi 1024x600
     aggiunto campo email_2 da poter aggiungere nell' intestazione cliente
     */

 /*static public String build = "20121017"; 
     new:
     più veloce in stampa preventivi/ddt/fatture per uso via internet
     completate traduzioni in inglese delle stampe preventivi/ddt/fatture
     aggiunti limiti personalizzabili per pluginRicerca in Impostazioni
     possibilità di evasione parziale su articoli con lotti
     bugs:
     non riprende giorno pagamento da ddt a fattura se specificato nel cliente
     problema qta esportata anche se non si seleziona la riga
     colori situazione clienti
     backup prima di ripristino                
     */

 /*static public String build = "20121123"; 
     bugs:
     corretto in ripristino quando loghi vuoti (0x)
     corretto bug timer su ricerca e panbarr
     corretto bug su gestione utenti assegnazioni ruoli su nuovi utenti
     corretto bug di visualizzazione su finestra impostazioni
     corretto bug gestione lotti quando più di 20 lotti in inserimento righe
     ottimizzata form articoli per gestione prezzi quando abilitato prezzi per cliente e ci sono motli clienti (visualizza soltanto i prezzi dei listini non automaici)
     corretto problema di reimpostazione parametri daabase in caso di problemi da locale a rete
     righe selezionate in situazione clienti fornitori più scure
     corretto bug cartella documenti errata su windows xp su backup in locale
     corretto bug in Foglio Righe su importi mal formattati
     corretto campo quantita_evasa per avere stessi decimali di quantita
     migliorata gestione quantità evasa con approssimazione ai 5 decimali
     */
 /*static public String build = "20121207"; 
     bugs:
     * aggiunta quinta colonna su stampa iva e aggiornato il report di stampa
     * corretto import articoli per non sovrascrivere altre informazioni
     * corretto icona mail in situazione clienti/fornitori
     * aggiunta quantità fra parentesi nelle note aggiuntive dei lotti in inserimento riga
     * corretto controllo numerazione documenti ignorando le proforma
     * corretto pluginRicerca mostra/nascondi
     * controllo quantità inserita in riga con totale quantità inserita in lotti (se diversa si usa la somma dei lotti)
     * corretto stampa dell'importo di riga a 0 se sconto 100%, prima non stampava niente invece di 0
     * aggiunta anagrafica aspetto esteriore dei beni
     * aggiunto vostro riferimento se presente nell'ordine nella conversione a ddt o fattura
     * corretto inserimento dati da foglio righe in fattura
     */

 /*static public String build = "20121219";
     * new:
     * Nuova stampa etichette con codici a barre da DDT
     * bugs:
     * corretto download eseguibile e icona in base alla versione in caso di problemi di navigazione
     * parametrizzazione etichette clienti report in inglese,
     * formattazione caselle per visualizzazione inglese,
     * modifica export readytec (note di credito saldo in positivo),
     * corretto salvataggio prezzi automatico da salvataggio righe
     * 
     */
 /*static public String build = "20130108"; 
     * new:
     * Scelta guidata del tipo di numerazione da adottare dal 1/1/2013 per le fatture in base alla legge di stabilità 2013
     * bugs:
     * riabilitata ricerca automatica sui campi combo ddt e fattura accompagnatoria
     * aggiornamento dei riferimenti ai documenti convertiti quando si modifica un numero di documento (con funzione in Manutenzione per farlo rigenerare su tutti i documenti)
     */
 /*static public String build = "20130111"; 
     * migliorata stampa registro iva
     * corretto problema stampa numero fattura su documenti precedentei al 2013
     * possibilità di rimanere con il cambio numero di anno in anno senza stampare l'anno (risoluzione agenzia entrate n. 1/E del 10/1/2013)
     */

 /*static public String build = "20130204"; 
     * versione 1.8.4 compatibilità con pluginContabilità
     * tolto limite dell'anno precedente su visualizza prezzi precedenti    
     */

 /*static public String build = "20130218"; 
     * ottimizzati indici scadenze per velocizzare Situazione Clienti/Fornitori
     * allargato il campo numero su stampa fattura
     * corretto ricerca cliente/fornitore in ddt,ordini e fatture di acquisto
     * corretto problema tasto destro su piano dei conti con Mac
     * corretto problema arrotondamento decimali iva fatture di acquisto in stampa registro iva
     * corretto inserimento note automatiche su fatture di acquisto in scorriemnto fornitori
     * corretto bug cliente obsoleto su nuovo preventivo
     * aggiunta possibilità di specificare i decimali della quantità nella composizione del kit articolo
     */

 /* static public String build = "20130613"; 
     * creazione movimenti su proforma in conversione da ordinecon con personalizzazione (movimenti_su_proforma) 
     * aggiunta possibilità di elaborare i certificati e versamenti ritenute d'acconto per data pagamento oltre che per data documento
     * risolto problema stampa codici a barre da documenti di acquisto
     * risolto problema scadenze su fatture proforma, non si spsotava dalla scadenza numero 1
     * modifiche toys4you
     * corretti importi in negativo su export acquisti e vendite in caso di note di credito
     * corretto restore dati azienda con loghi grandi (tramite split hex)
     * corretta stampa iva con fatture e scontrini
     * nuova finestra Ultimi Prezzi in menù Magazzino
     * nuovo report Chi / Cosa in menù Statistiche
     * nuovo report Ordinato in menù Magazzino
     * nuova anagrafica Nazioni
     * nuova anagrafica modalità Consegna
     * nuova anagrafica modalità Scarico
     * aggiunto campo fornitore abituale in anagrafica articoli
     * aggiunti campi modalità Consegna e Scarico su anagrafica Clienti/Fornitori
     * 
     * corretto bug anno sbagliato su modifica, stampa, annulla salvataggio
     * ottimizzazioni prima nota
     * 
     * aggiunto controllo automatico parm_prop
     */

 /*static public String build = "20130621"; 
     * 
     * Corretto algoritmo di ricerca del prezzo in caso di articoli importati e listino a ricarico
     * Corretto backup per gestire le VIEW (vedi segnalazione errore su restore sulla VIEW v_righ_tutte
     * piccole correzioni su plugin contabilità
     * bug su nuovo report chi / cosa in caso di fatture di acquisto filtrate per fornitore 
     * bug su salvataggio destinazione diversa ordini
     * bug su visualizzazione fornitore in fatture di acquisto
     * 
     */

 /*static public String build = "20130704"; 
     * 
     * Possibilità di evidenziare tutti i documenti come per le Fatture, tasto destro Marca e scelta del colore
     * Aggiunta possibilità di scrolling verticale su anagrafica Articoli
     * tolta dipendenza font Tahoma sui report provvigioni per compatibilità con Linux  
     * corretto bug vista v_righ_tutte in ripristino database e controllo tabelle 
     * migliorata gestione interna delle righe dei documenti per id invece che numero/serie/anno
     */
 /*static public String build = "20130906"; 
     * 
     * Aggiunto pulsante Crea PDF in testata del documento e in plugin Ricerca
     * aggiunta scelta lingua in stampa elenco articoli
     * (nordas) richiesta dei lotti in fase di conversione ordine (prima venivano richiesti solo se qta conf minore di qta)
     * aggiunti campi codice a barre e codice fornitore nell'elenco degli articoli in anagrafica articoli e compresi nel filtraggio
     * riattivato snj
     * corretto inserimento movimenti, non permetteva di movimentare un articolo a lotti specificando un lotto vuoto
     * corretto problema iacovone prezzi di base invece che zero se import articolo e listino non di ricarico (richiamo aggiornaListini dopo import articoli)
     * aggiunto descrizione in inglese e um in inglese all'import articoli da excel
     * aggiunta la gestione dello sconto a importo sulle fatture di acquisto
     * corretto posizionamento etichetta descrizione in stampa fattura
     * corretto opzione stampa prezzi da cliente in conversione da prev/ordine a ddt
     * 
     */

 /*static public String build = "20130926"; 
     * 
     * risolto problema visualizzazione e salvataggio fatture di acquisto
     * aggiunta qta dei lotti in descrizione riga su conversione documenti
     * corretto calcolo importo scadenze con iva a 30gg e rivalsa in sottrazione
     * correzioni e aggiornamenti contabilita
     */
 /*static public String build = "20130930"; 
     * 
     * Passaggio IVA 22%
     * corretto calcolo totale documento con rivalsa + spese con iva da ripartizionare
     */

 /*static public String build = "20131004";
     * 
     * Correzioni layout versione Osx
     * corretto calcolo totale su documenti con data precedente al 1/10, più di un codice iva e spese di trasporto o incasso
     * 
     */
 /*static public String build = "20131010"; 
     * 
     * Preparazione setup base 185 20131010
     * aggiornato createdb a 185
     * debug attivazione
     * correzioni grafiche su finestra giacenze e plugins per mac
     * 
     */

 /*static public String build = "20131105"; 
     * 
     * corretto aggiornamento prezzi listino con prezzi ivati
     * corretto foglio righe fatture per controllo numero riga già usato
     * corretto modifica documento dopo stampa e relativo salvataggio (annullando le modifiche il totale non tornava con le righe inserite)
     * corretto provvigioni agenti riportare su righe con cosniderava i decimali
     * corretta stampa fatture di acquisto per totali errati
     * modifiche medcomp
     * 
     */
 /*static public String build = "20131111"; /*
     * 
     * nuova esportazione registro iva su excel
     * corretto problema iva su apertura fatture anni precedenti al 2012
     * corretto problema richiesta lotti su articoli kit
     * 
     */

 /*static public String build = "20131115"; 
     * 
     * corretto problema conversione ordine a fattura accompagnatoria e richiesta della serie di numerazione
     * 
     */
 /*static public String build = "20140205"; 
     * corretto problema creazione db esterno con mysql 5.1.68
     * aggiunto categoria cli/for in elenco documenti 
     * toysforyou - report con controllo evaso != s
     * corretto controllo matricola in rigenerazione movimenti per problema con db socis
     * corretto problema cambio riferimenti provvigioni da proforma a fattura
     * aggiunto flag fattura_al_rinnovo (per uso interno TNX)
     * aggiunta possibilità di giacenze con prezzi netti
     * aggiunto costo netto medio su Giacenze
     * modifiche colmar toys
     * modifiche erika immobiliare
     * corretta stampa iva su fatture di acquisto con sconto a importo
     * multi deposito per pro+
     * aggiunto campo Nazione in import clienti/fornitori
     * aggiunto totale da pagare su testo email solleciti e gestito le note di credito
     * aggiunta gestione note di credito in acquisti
     * aggiunti sconti di listino in stampa elenco articoli
     * risolto problema errore KEY duplicate su fatture di acquisto rinumerando fatture già inserite
     * migliorie scadenzario (ex situazione clienti/fornitori)
     * corretto chi cosa per note di credito
     * assegnazione della ritenute/rivalsa su conversione documenti
     */
 /*static public String build = "20140207"; /*
     * corretto problema gestione lotti su DDT
     * aggiornata finestra ultimi prezzi con aggiunta del documento e della quantità
     * aggiornato report chi/cosa con prezzi netti invece che lordi
     * migliorata stampa anagrafica clienti fornitori
     */
 /* static public String build = "20140210"; /*
     * corretto calcolo arrotondamento su totale documento (derivato da cambio calcolo per deducibilità)
     * corretto problema numerazione in conversione da preventivo/ddt con serie a fattura
     * corretta rigenerazione scadenze e provvigioni su fatture con sconto a importo
     * corretto filtro scadenzario su scadenze da preventivi/ordini e filtro per non fatturati
     * corretto report chi/cosa su ddt e preventivi/ordini
     */

 /*static public String build = "20140305"; 
     * aggiunta data di stampa in scadenzario
     * corretto problema generazione scadenze e provvigioni su cambi oserie da Ordine/DDT a Fattura
     * aggiunto parametro estrai incassi (acconti) su export teamsystem (premioceleste)
     * aggiunte opzioni su export fatseq e gestito fatture con ritenute d'acconto
     * correzioni frajor
     * predisposizione conversione a utf8 e nuovi setup tutto utf8
     * corretto statistiche chi cosa per fatture di acquisto
     * non movimenta gli articoli con flag servizio anche su kit
     */
 /* static public String build = "20140307"; 
     * sostituzione tab con 3 spazi in stampa
     * modifiche alchemica valgelaata (stampa destinatario da ddt/clineit, stampa privacy, azzera intestazione se 'non stampare logo')
     * modifiche safety partner (stampa contributi solo se pagati)
     */
 /*static public String build = "20140321"; 
     * aggiunta intestazione scadenza / fattura su stampa scadenze
     * corretto bug 'iconifica' finestre in mac
     * corretto bug lista articoli che si espande in alto invece che in basso su mac
     * aggiunta etichetta Fornitore in impostazioni per le stampe dei documenti di acquisto
     * corretto import righe in ddt da csv
     * correzioni e migliore su finestra dettaglio scadenze
     * corretto errore collation su scadenzario linux (utf8)
     * corretta duplicazione ordini e ddt (in alcuni casi riportava vecchie righe non eliminate)
     */
 /*static public String build = "20140521";  
     * aggiunto controllo all'avvio su windows che non sia installato su un percorso di rete
     * risolto problema generazione movimenti su righe 'orfane'
     * risolto problema eliminazione proforma non eliminava i riferimenti dal documento di origine
     * risolto problema statistiche su prezzi totali
     * ampliati i colori per marcare i documenti
     * risolto problema 'null' su export teamsystem
     * cambiata chiave destinazioni diverse a id autoinc
    
     * lc service email allegati
     * pellet consegne
     * 
     * aggiunta opzione testi personalizzati in plugin Email
     * corretta logica di invio quando non presente l'indirizzo email ma presente il cc, in caso di invio multiplo la prima email non partiva ma le successive arrivavano al cc
     * corretto in plugin Ritenute l'imponibile sulle certificazione delle fatture di vendita
     * se scelto di stampare in PDF adesso anche i tutti i report vengono creati in PDF
     * aggiunto trim a destra dei codici articoli all'avvio su articoli,righe documenti e prezzi listino
     * aggiunto trim automatico a destra in import articoli e in salvataggio dati string
     * corretto Inserisci colli in modifica riga
     * migliorate stampe versamenti e certificati ritenute d'acconto per calcolare il pagamento parziale delle ritenute
     * corretta la dimensione di alcuni campi decimal su test_ordi_acquisto
     * corretta deselezione banca su scadenzario quando si sceglie pagamento fornitori
     * corretta intestazione stampa elenco clienti e fornitori in base al filtro attivo
     */
 /*static public String build = "20140610";  
     * aggiunto controllo su param_prop vuoto per ripartire in automatico dal backup
     * aggiunto export xml pubblica amministrazione
     */

 /*static public String build = "20140613";  
     * corrette scorciatoie per copia/incolla, undo/redo su Osx
     * aggiunta possibilità di inserire i dai REA e IBAN e Istituto Finanziario su export fattura elettronica
     * correzioni su export fattura elettronica (in caso di sconti o codice articolo)
     */
 /*static public String build = "20140617";  
     * corretti controlli su elenco documenti in caso di archivi vuoti (non segnalava di dover selezionare il documento prima di eseguire l'azione ma partiva il puntatore di attesa all'infinito)
     * corretta segnalazione di errore su creazione nuova anagrafica in caso di archivio vuoto
     * correzioni su export fattura elettronica (in caso di codice contratto, nuova parte di collegamento righe fattura con righe ddt e forzatura iva su righe descrittive a iva della fattura o standard nel caso di più codici iva)
     * corretto bug per cui se in riga si calcolava lo sconto1 a partire dal prezzo netto ma si aveva inserito anche lo sconto2 questo non veniva azzerato    
     */
 /*static public String build = "20140808";  
     * aggiunto controllo numero di caratteri in descrizione riga per controllare il massimo di 100 caratteri della fattura elettronica p.a.
     * corretto ingrandimento di tutte le finestre dopo averne ingrandita una sola
     * migliroato export righe documenti in excel per avere il campo quantità in valore numerico invece che stringa
     * migliorato elenco righe documenti aggiungendo la colonna iva
     * correzioni su conversione documenti (in alcuni casi si inserivano righe 'orfane' non collegate al documento di origine)
     * per problemi con alcuni Windows XP, implementato httpclient per comunicazioni internet e possibilità di impostare il proxy manualmente da Impostazioni->Avanzate
     * corretto il campo quantità nelle tabelle per la memorizzazione dei lotti
     * aggiunto in Utilità->Manutenzione possibilità di inserire in anagrafica gli articoli inseriti nei documenti ma non registrati in anagrafica
     * corretto bug su rivalsa con massimale, nella sottrazione dal totale non considerava il massimale
     */

 /*static public String build = "20140901";  
     * aggiunta possibilità di specificare etichetta da inserire nella conversione da Preventivo a DDT/Fattura (prima era presente solo la dicitura da Ordine, adesso se il documento di origine è un Preventtivo viene presa questa nuova etichetta)
     * migliorata stampa registro iva per non stampare le colonne dei codici iva non usati    
     * aggiunti dati necessari per le ritenute e rivalse nell'export xml per la pubblica amministrazione
     */
 /*static public String build = "20141002";  
     * aggiunto flag_consegnato (punto pellet)
     * aggiunto pulsante carica e salva su inserimneto riga in ddt di vendita per dream flower (personal autocarico)
     * corretto problema su foglio righe fatture, se si inseriva il codice articolo, il prezzo contiene dei decimali e non si entra nella casella del prezzo il programma troncava i decimali
     * corretto quantità evasa e 'convertito' sul documento di origine quando si elimina il documento convertito
     * corretto problema flag fatturato su scadenzario ordini
     * aggiunto lock tabelle durante aggiornamento gaicenza reale
     * aggiunti trimestri su scelta periodo per situazione provvigioni agenti
    
     * corretto importo totale documento su export xmlpa con ritenuta d'acconto
     * corretto salvataggio dati ritenuta d'acconto nella sezione xml pa delle fatture
     * aggiornamenti al plugin di autoaggiornamento
     * 
     * aggiunt controllo modifica contemporanea dei documenti con lock
     * aggiunta gestione allegati versioni a pagamento
     */
 /* static public String build = "20141016";
     * corretto problema storage engine su ricreazione tabelle righe temp    
     * corretto problema su scadenzario con colonna persona di riferimento non faceva vedere clienti con persona di riferimento NULL
     * corretto problema locks su inserimento documenti con gestione della serie
     * corretto problema righe omaggi che non facevano scalare le righe successive
     * migliorato calcolo importo singole scadenze per bug arrotondamento e quadratura
     * migliorata eliminazione documenti per includere cancellazioni eventuali provvigioni o scadenze collegate
     * corretto chiusura form documenti su click in dettagli scadenze o stampa
     * corretto recupero database quando non si riesce a capire la codifica del file
     * risolti due problemi di memoria non rilasciata sulla creazione di documenti
     */
 /*static public String build = "20141203";  
     * corretto problema 'eol' su export readytec su mac
     * inizio implementazioni acconti (ranocchi)
     * produttori fornitori toysforyou
     * corretto pulsanti 'Aggiungi DDT' e Aggiungi Ordine' su anagrafica clienti fornitori
     * nuova gestione acconti
     * nuova estrazione elenco ddt
     * ottimizzazione creazione fattura da molti ddt con raggruppamento
     * aggiornato export xml pa per totale lordo/netto ritenuta e possibilità di specificare codice fiscale del cedente
     * aggiunto calcolo numero collin automatico in base al numero colli inserito per riga oppure alla quantità
     */
 /*static public String build = "20141219";  
     * aggiunta opzione per colli automatici
     */
 /*static public String build = "20150115";  
     * problema stampa renzo renzi
     * xmlpa codice ufficio in anagrafica cliente
     * xmlpa nuovo campo causale in dati fattura
     * possibilità di cambiare la base per il calcolo delle provvigioni agenti 
     * aggiunto filtro per data documento e dal/al su situazione agenti
     * corretto numero colli in caso di altre scritte dopo
     */
 /*static public String build = "20150126";  
     * corretto problema iva stringa su stampa ddt
     * correzione su generazione provvigioni (problema con impostazione per data fattura)
     * modifiche frigo clima (possibilità di stampare FAX e dest diversa su ordini a fornitore)
     * split payment
     * allegati xml pa
     * nuova versione export fattura xml pa
     * no acconto in scadenza
     * problema ritenute per campo imponibile vuoto
     * problema su scadenze se acconto uguale a totale da pagare
     * backup dati a riga di comando per schedulazione
     */

 /*static public String build = "20150203";
     * aggiunto N6 nei codici iva per xml pa
     */
 /*static public String build = "20150205";
     * corretto bug su salvataggio flag split payment su fatture con serie
     * corretto bug in stampa documenti per righe duplicate su database condivisi con client manager
     */

 /*static public String build = "20150209";    
     * corretto bug arrotondamenti, alcuni casi in cui se il centesimo finale era 5 veniva erroneamente arrotondato a per difetto invece che per eccesso (586.795 -> 586.79 invece di 586.80)
     * corretto bug aggiornamento totale da pagare su cambio split payment
     * aggiunto controllo su gestione kit per non inserire se stesso
     * aggiunta colonna rivalsa su stampa certificati ritenute su fatture di vendita
     */
 /*static public String build = "20150219";
     /*
     * corretto bug provvigioni per scadenza e acconti (aggiornata struttura scadenze e provvigioni per id_doc)
     * corretto bug su giacenze per lotti che non sentiva cambio deposito
     * corretto bug su primo salvataggio fattura con cliente split payment non generava la giusta scadenza
     * plugin Ritenute: aggiunto campo rivalsa su stampe certificati e versamenti
     * 
     */

 /*static public String build = "20150325"; 
     * !!! mettere online nuovo report !!! migliorata stampa fatture di acquisto con ritenute e rivalse
     * !!! controllare report fatture sembra che non ci sia la versione con lo split payment
     * !!! mettere online pluginInvoicex per xmlpa dati ddt e chiusura fattura su salva e rimani per cirri
     * !!! mettere online plugin email per nomi file cambiati
     *
     * costo medio ponderato per toys con codici assortimento e movimenti storicizzati
     * costo medio ponderato per toys su salvataggio fattura di acquisto e ricalcolo registro vendite
     * miglior antialias font
     * modifiche cirri
     * modifiche agripiù (vedi sotto)
     * aggiunto filtro fornitore abituale su giacenze
     * migliorata stampa elenco movimenti di magazzino
     * corretta traduzione partita iva e cod. fiscale su stampa ordine/preventivo in inglese
     */
 /* static public String build = "20150331"; 
     * aggiornamento giacenza su articoli (campo disponibilita_reale) ad ogni movimento
     * aggiunto prezzo e cliente/fornitore su stampa movimenti magazzino
     * corretto problema su generazione scadenze in fatture di acquisto se modificata dopo una conversione da ddt/ordine
     * migliorata codifica nome file in export pdf e allegato per email
     * corretto problema stampa iva dello split payment che non veniva ricalcolata su stampe successive alla prima
     * corretto caricamento giorno da anag. fornitore su fatture di acquisto quando pagamento con giorno del mese preimpostato
     */
 /*static public String build = "20150401"; 
     * corretto bug su lancio backup a riga di comando
     */
 /*static public String build = "20150421"; 
     * aggiunta opzione includi prezzi in nuova stampa movimenti
     * corretta stampa movimenti di magazzino se filtro per articolo con invio dentro descrizione
     * migliorata autneticazione tunnel per gestire UIKeyboardInteractive (ssh su freebsd con impostazioni predefinite)
     * corretto problema filtro giacenze
     * IMPORTANTE, corretto bug perdita codici lotti e matricole su apertura in modifica di un documento e successivo annullamento delle modifiche
     * modifica per personalizzazione gestione anticipazioni avvocati (7F)
     * corretto ordinamento decrescente in scadenzario
     * test attivazione automatica tunnel ssh su server TNX
     */
 /* static public String build = "20150422"; 
     * corretto problema in caricamento fattura su database via internet
     */
 /*static public String build = "20150423"; 
     * corretto problema in salvataggio dati su alcuni campi non inizializzati
     * corretto problema stampa movimenti filtrando per deposito
     */
 /*static public String build = "20150504"; 
     * preimpostato A4 su estrazioni in excel perchè con Excel su Osx proponeva formato US
     * tolte fatture proforma da export fatseq
     * blocco fatture di vendita e di acquisto su stampa registro iva definitivo
     * problema su anticipiazione, tornava null e quindi senza anticipazioni non funzionava il report
     */
 /*static public String build = "20150507"; 
     * corretto problema backup su data blocco
     * aggiunto controllo caricamento combo per problemi su apertura fatture
     * impostazione nomi file pdf
     */
 /*static public String build = "20150508"; 
     * corrette dimensioni colonne stampa elenco clienti/fornitori
     * corretto cambio stato ordine/preventivo in conversione ordini di acquisti (andava a cambiare lo stato degli ordini di vendita invece che di acquisto)
     * corretto apertura destinazioni diverse su anagrafica con paese inserito
     */

 /*static public String build = "20150518"; 
     * corretto import csv per impostare a null invece di 0 i campi numerici (borgo acciai)
     * corretto problema splash screen rimaneva visualizzato su osx
     * correzioni su backup database
     * possibilità di segnare fatture di vendita come anticipate e su quale banca, filtrabile anche in scadenzario
     * corretto problema cambio quantita in scelta lotti con calcolava nuovo totale di riga
     * corretti due memory leak (mysql dontTrackOpenResources=true e LockableCircularBusyPainterUI)
     * aggiunta gestione fatture anticipate
     */
 /*static public String build = "20150603"; 
     * Aggiunto allegato pdf cedolino bonifico su invio email fattura se attivo 'stampa cedolino bonifico' in impostazioni.
     * corretto duplica fattura che portava dietro il blocco e anticipazione della fattura
     * aggiunto conto su codici iva per export fatseq
     * corretto bug multiselezione su elenco fatture dovuto alla nuova funzione di fatture anticipate
     * migliorato import righe da csv per campi non null (prezzo_ivato, totale_ivato, totale_imponibile) e campo iva vuoto
     */
 /*static public String build = "20150604"; 
     * corretto problema su stampa registro iva fatture di vendita
     */
 /*static public String build = "20150617"; 
     * nuova gestione bollo in fattura (gestito anche per export fattura xml pa)
     */
 /*static public String build = "20150916"; 
     * cambio ver 1.8.8
     * blocco movimenti generati da programma
     * corretto problema in inserimneto articolo con lotto e omaggi, gli omaggi venivano avvalorati con il prezzo invece di essere a 0
     * nuovo calcolo documento per migliore gestione parte deducibile/indeducibile
     * nuove statistiche raggruppate per agente (pro/ent)
     * risolto bug aggiungi allegati su ubuntu
     * migliorato font su ubuntu (ridotto di due punti)
     */
 /*
     * possibilità gi archiviare gli allegati fuori dal database, su una condivisione di rete oppure su dropbox
     * corretto bug su foglio righe per cui non impostava il codice iva articolo
     * aggiunti pulsante + 'Fattura accompagnatoria' e '+ Nota di credito' in anag clienti 
     * gestione del campo Serie anche per le note di credito
     * nuova gestione delle note del cliente per separare le note in anagrafica del cliente da quelle da riportare sui documenti con possibilità di specificarle diverse per titpo di documento
     * nuova gestione contatti su anagrafica clienti con relative modifiche al plugin Email per inviarle in base ai nuovi contatti
     * sviluppo del plugin Email con anteprima del messaggio da inviare con possibilità di modifica
     * sviluppo della funzione di conversione documenti, dove si sceglie la quantità da evadere tiene in cosniderazione 
     eventuali articoli ripetuti e vengono presentate prima le righe degli ordini più vecchi    
     * gestione fido
    
     * prima nota, elenco, visualizzazione formato corretto per la colonna data
     * conversione ordini o ddt a fattura impostare il campo split payement in base al cliente
     * gestione serie per note di credito
     * avviso su nuovo articolo quando si sta inserendo un articolo già presente
     */
 /*static public String build = "20150921"; 
     * dbchanges2 problema lingua mysql
     * correzioni su gestione fido
     * correzione su nome file in crea pdf
     * stampa serie su note di credito
     * problema note che si ricaricano dal cliente in modifica ordine
     *--- bis   
     * correzione su calcolo totale documento in casi di codice iva con caratteri alfabetici in minuscolo
     * correzione su nuova fattura e rigenerazione scadenze (rigenerava sempre le scadenze su nuova fattura) 
     * correzione su salvataggio flag split payment (salvataggio era corretto, il problema era in apertura documento)    
     * corretto apertura impostazioni per errore con vecchie versioni di java 1.5
     * cambiato raggruppamento email su invio da solo email a email + nome cliente
     * aggiunta copia dei contatti in duplicazione cliente/fornitore
    
     static public String build = "20150922"; 
     * corretta stampa elenco clienti fornitori
     * corretto problema riapertura posizionamento e dimensioni logo se salvata a dimensioni minori di 1 pixel.
     *
     */
 /*static public String build = "20151008"; 
     * modifiche satinox, report satinox, contatto di riferimento su ordini
     * aggiunto campo categoria in import clienti/fornitori
     * aggiunti campo nazione e contatto principale in elenco clienti/fornitori
     * aggiunto il campo note fra i camp abilitati nella gestione movimenti di magazzino
     * giacenze per deposito su conversione documenti e su scelta lotto
     * aggiunti campo in import articoli: gestione lotti, gestione matricole, fornitore abituale, categoria, sottocategoria
     * corretto nome file pdf dell'ordine di acquisto se nomi personalizzati
     */

 /* static public String build = "20151015"; /*
     * correzione su nuova gestione lotti e deposito
     * correzione su elenco articoli per poter ordinare per colonna (se numero articoli inferiori a 10000)
     * migliorata funzione di arrotondamento
     * possibilità di ordinare le righe in scelta lotti e riempita tabelle con righe vuote per inserire nuovi lotti
     * aggiunta possibilità di editare la mail in html in anteprima email da inviare
     * aggiunti controlli per evitare blocco conversione allegati al riavvi odi Invoicex
     * migliorato import righe da CSV per evitare errori su campi non null
     */
 /*static public String build = "20151028"; 
     * aggiunta scelta formato (verticale/orizzontale) e dimensione font in stampa scadenzario
     * aggiunto permesso Gestione Ritenute e Rivalse nella gestione dei permessi utenti.
     * migliorata gestione collegamento documenti convertiti per evitare false indicazioni sotto la colonna convertito, ad esempio scegliendo due ordini da convertire in fattura, se le righe di un ordine 
     nella scelta delle quantità non vengono selezionate per la conversione allora non si riporta la conversione sulla testata dell'ordine
     * correzione su modifica lotti nella conversione da ordine a ddt/fattura
     * forzata serie in maiuscola nella scelta in conversione documenti
     */
 /*static public String build = "20151120";
     * richiesta del deposito predefinito se non presente (in caso di reinstallazione o azzeramento del param_prop.txt)
     * aggiornato import Clienti/Fornitori per gestire nuova tabella contatti
     * possibilità di usare in contemporanea più invoicex con database locale
     * avvio del teamviewer 7 per osx 10.5, 10.6 e 10.7
     * aggiunta ricerca anche sul campo descrizione_en in elenco articoli
     * su foglio righe quando si inserisce il nuovo articolo va ad inserire il prezzo in base al listino del cliente
     * migliorata gestione collegamento documenti convertiti per evitare false indicazioni sotto la colonna convertito, adesso
     vengono tolti i riferimenti anche dei ddt convertiti in fattura con raggruppamento degli articoli
     * nel tunnel verso server tnx forzato host remoto di redirect 127.0.0.1 invece che nome host (problemi collegamento wind)
     * aggiunta anagrafica modalità di consegna
     * aggiunta richiesta di sovrascrittura note su cambio stato ordine/preventivo o cambio cliente in modifica dei documenti
     * modificata stampa movimenti di magazzino per azzerare prezzo su articoli che compongono il kit
     * aggiunta estrazione excel dello scadenzario con dettaglio dei pagamenti
     * corretta chi/cosa avanzato per documenti di acquisto (problema campo agente)
     * adeguato formato zip in export fattura pa
     * corretto filtro per tipo su elenco clienti fornitori
     * aggiunti filtri su elenco articoli e migliorata stampa elenco articoli
     * corretta gestione permessi 'altre anagrafiche'
     * aumentato timeout collegamento con tunnel da 3 a 6 secondi
     */
 /* 20151124 corretto problema lock su aggiornaGiacenzeArticoli */
 /*static public String build = "20151211";
     * velocizzata giacenza su conversione documenti ed aggiunto visualizzazione del progresso della conversione
     * possibilità di visualizzare i dati del trasporto negli ordini a fornitore con click destro su spazio vuoto della testata
     * possibilità di impostare lo stato standard per preventivi e ordini in acquisto e vendita
     */
 /*static public String build = "20160216";
    
    * mario fia, patch per usare invio su inserimento righe (senza mouse)
    * satinox borgo acciai -> export import peso_kg
    * curreli export filconad
    * bloccata azzeramento e storicizzazione magazzino (manutenzione) se movimenati più depositi
    * risolta incompatibilità creazione database iniziale con mysql 5.7 (set storage_engine è diventato set_default_storage_engine)
    * aggiunto campo libero 1 su testate documenti, gestibile da tabella campi_liberi, raggruppabile da statistiche->chi cosa avanzato
    * group layout su finestre testate documenti
    
    * da Mario Fia
        se azioni pericolose è abilitato, chiede all'utente la data da utilizzare per generare la fattura in conversione da ddt
        permette di selezionare DDT di clienti diversi nella conversione da DDT a Fattura (ciclando per i diversi clienti)
    
    * filtra selezione cliente fornitore
    * dimensioni della finestra Impostazioni ottimizzate
    
    * stampa s_prezzo 0 invece di vuoto se quantita presente (come per s_importo)
    
    * variazione prezzi listini
    
     */
 /*static public String build = "20160223";    
    * correzioni su conversione documenti da DDT a Fatture con clienti diversi
    * corretto calcolo totale fattura su conversione ddt di acquisto a fattura 
    * correzioni grafiche su finestre testate documenti
    * cambiato suggerimento su flag 'descrizione' in quanto va comunque a movimentare il magazzino
    * corretto bug selezione prezzi ivati senza aver scelto il cliente/fornitore
    * correzione bug non si aggiornava situazione agenti filtrando solo per data fattura
    * rilascio nuovo Invoicex Base con jre incluso per osx
     */
 /*static public String build = "20160303";    
    * corretto duplica articolo per riportare anche sconti, fornitore abiutale e composizione del kit
    * corretto invio mail che non partiva con look Nimbus (null su color base DisabledGlassPane)
    * corretto problema note in automatico su ordini (prendeva quelle delle fatture)
    * migliorie grafiche alle combobox
    * corretto problema inversione localita e telefono in destinazione diversa
    * tolto messaggio sul giorno pagamento (richiesto da 1 a 28), se si inserisce giorno 30 e cade in Febbraio porto la data al 28 (o 29!)
     */

 /*static public String build = "20160329";    
    * corretto problema provvigioni toysforyou
    * corretto problema import articoli su update con mysql interno
    * Mario Fia: aggiunta gestione Causale trasporto e Tipo consegna standard in anagrafica Clienti per riportarlo su nuovi DDT 
    * corretto permesso di accesso alle anagrafiche clienti fornitori da testata documenti
    * varie correzioni minori su plugin Contabilità
    * Contabilità: in registrazione prima nota, il pulsante Stampa Elenco adesso rispecchia l'elenco filtrato delle registrazioni
    * Contabilità: aggiunto periodo in stampa Bilancio
    * Movimenti: aggiunto filtro per lotto e matricole in elenco movimenti e in stampa movimenti
    * Email: aggiunta gestione del riquadro Allegati in anteprima email
    * Fattura P.A.: Aggiunta possibilità di specificare in anagrafica articolo il tipo e codice dei dispositivi medici
     */
 /*static public String build = "20160331";    
    * corretto bug su anagrafica utenti, cliccando su nuovo rimaneva disabilitato username e password
    * corretto gestione allegati email
    * corretto bug stampa elenco articoli con sorgente finestra e prezzi
    * corretto bug gestione utenti non permetteva di impostare la password in alcuni casi
    * Contabilità: migiolarata stampa registrazioni
    * Email: migliorata gestione allegati in anteprima
     */
 /*static public String build = "20160505";    
    * plugin Ricerca: migliorata analisi del testo di ricerca in casi di spazi o parole minori di tre caratteri
    * DA SEGNALARE da aggiornamento precedente: Email: aggiunto pdf fattura in allegato sollecito (modificabile da impostazioni email)
    * aggiunta colonna categoria e listino fra le colonne aggiuntive in elenco dell'anagrafica clienti fornitori
    * corretto Statistiche->Elenco DDT, se si impostava una data come filtro in DDT di acquisto generava un errore
    * aggiunto campo categoria e sottocategoria articolo in export acquisti vendite
    * colonne aggiuntive categoria e listino in elenco clienti fornitori
    * report consegne, aggiunti documenti di acquisto e divisione colonna codice articolo da descrizione in export excel
    * aggiunta opzione stampa prezzi su ddt di acquisto
    * corretta giacenza su inserimento nuova riga quando si seleziona un articolo kit
     */

 /*static public String build = "20160506";    
    * corretto inserimento contatti clienti/fornitori
     */
 /*static public String build = "20160606";
    * corretta stampa statistiche per agente
    * corretto salvataggio del giorno del pagamento su fatture di acquisto
    * corretto problema stampa elenco articoli con ordinamento 'categoria, sottocategoria, descrizione articolo'
     */

 /*static public String build = "20160607";
    * corretto inserimento contatti in anagrafica clienti fornitori
     */
 /*static public String build = "20160627";
    /* 
    * corretto toysforyou giacenze deposito
    * corretto chiusura nuovo ordine/preventivo da X della finestra rimaneva riga vuota in elenco ordini/preventivi
    * corretto scelta fornitore su export acq e ven
    * prima versione plugin doc ricorrenti
    * aggiunto tipo, numero e data del documento in statistiche -> consegne
    * aggiunta multiselezione su modalità di consegna ed il totale nel report consegne
     */
 /* static public String build = "20160630";
    /* 
    * corretto problema invio email sollecito
    * nuova funzione salvataggio dimensioni finestre
    * nuova funzione salvataggio dimensioni colonne nelle griglie
    * corretto problema generazione movimenti inversi su deposito arrivo
     */
    
    /*static public String build = "20160809";
    
    * migliorato export in excel (limite righe a 65,536 e auto dimensionamento delle colonne)
    * Fattura P.A.: Separata serie da numero di fattura con il carattere '/' 
    * aggiunto filtro per Preventivi 'aperti' in elenco preventivi
    
    * problema storage_engine in ripristino corretto per mysql 5.6 o superiore
    * aggiunta colonna aggiuntiva in visualizza elenco clienti fornitori
    * corretto problema generazione provvigioni per note di credito
    * corretto problema gestione prezzi per cliente e listini (si azzeravano i prezzi per cliente al cambio dei prezzi di listino)
     */
    
    static public String build = "20161025"; /*
    * correzioni su gestione lotti e matricole
    * disabilitato alcune accellerazioni grafiche su windows per incompatibilità con alcune schede grafiche
    * aggiunto campo esigibilità differita iva in export FATSEQ teamsystem (+ campo versione ' 1' a fine file)
    * aggiunta gestione importo bollo in export FATSEQ teamsystem
    * aggiunto paramentro per auto update sulla settimana invece che immediato
    * aggiunto parametro per numerazione righe 1,2,3... / 10,20,30... / 100,200,300...
    * aggiunta gestione note in righe documenti (icona in alto a destra su modifca righe)
    * nuova opzione in Impostazioni->Altro, Se 'da Ordine' e 'da Preventivo' vengono lasciate vuote verrà usato lo 
    stato originale del documento come stringa per la conversione da ordine/preventivo a nuovo documento
    * aggiunto campo iban in griglia scadenze per generazione rid
    * correzioni documenti ricorrenti (sim informatica)
    * aggiunta esportazione rid
    * corretto ripristino database per tabella depositi con id = 0 (NO_AUTO_VALUE_ON_ZERO)
    * aggiunto CIG/CUP in record 50 tracciato export RIBA se presenti in Fattura P.A.
    * migliorato inserimento movimenti di magazzino controllando il codice articolo per minuscole/maiuscole ed inserito come in anagrafica articoli
    * corretto inserimento nuove scadenze da scadenzario (errore su secondo insert per gestione erata autoinc)
    * aggiunta visualizzazione prezzo in elenco articoli con selezione del listino
    * aggiunto filtro per agente in scadenzario 
    * cambiato getHostname per osx tramite comando hostname per risolvere problema http://stackoverflow.com/questions/10064581/how-can-i-eliminate-slow-resolving-loading-of-localhost-virtualhost-a-2-3-secon
    * aggiunto export SDD nel plugin Riba
    * varie migliore plugin Contabilità con la nuova possibilità della chiusura/apertura dell'esercizio contabile
   */

    static public Versione version = new Versione(1, 8, 9); //aumentare quando si rialscia un aggiornamento
    static public String versione = null; //versione del programma Base, Professional, Enterprise
    //static private menu padre;
    static public Menu padre;
    static public MenuFrame padre_frame;
    static public MenuPanel padre_panel;
    static public String wd = "";   //working dir
    static public iniFileProp fileIni;
    static public Utente utente;
    static public boolean iniFinestreGrandi;
//    static public String iniPercorsoLogoStampe;
//    static public String iniPercorsoLogoStampePdf;
    static public String iniPercorsoSfondoStampe;
    static public String iniPercorsoSfondoStampePdf;
    static public String iniPercorsoSfondoProforma;
    static public String iniComandoGs;
    static public String iniDirFatture;
    static public boolean iniFlagMagazzino;
    static public boolean iniPrezziCliente;
    static public boolean iniSerie;
    static public String hdserial = null;
    static public String serial;
    static public int anno;
    static public String login = "";
    static public boolean flagWebStart = false;
    static public String PERSONAL_LUXURY = "lux";
    static public String PERSONAL_CLIENT_MANAGER_1 = "cm1";
    static public String PERSONAL_IPSOA = "ipsoa";
    static public boolean luxStampaNera = false; //flag per luxury company per chiedere se stampare con sfondo nero la fattura
    static public String luxStampaValuta = "Euro"; //flag per luxury company per chiedere se stampare con sfondo nero la fattura
    static public boolean luxProforma = false;  //flag per stampare pro forma
    static public Properties applicationProps = new Properties();
    static public String homeDir = "";
    static public String inst_id = "";
    static public String inst_email = "";
    static public String inst_seriale = "";
    static public String inst_nome = "";
    static public String inst_cognome = "";
    static public String inst_nazione = "";
    public static Preferences prefs = Preferences.userNodeForPackage(main.class);
    static public String startDb = null;
    static public boolean startDbCheck = false;
    static public boolean startConDbCheck = false;
    static public int dbPortaOk = 0;
    static public InvoicexEvents events = new InvoicexEvents();
    static public ArrayList<String> pluginPresenti = new ArrayList();
    static public ArrayList<String> pluginAttivi = new ArrayList();
    static public Map<String, Map> pluginErroriAttivazioni = new HashMap();
    static public Hashtable plugins = new Hashtable();
    static public Hashtable<String, PluginEntry> pluginsAvviati = new Hashtable();
    public static boolean s1;
    static public boolean pluginInvoicex = false;
    static public boolean pluginBackupTnx = false;
    static public boolean pluginClientManager = false;
    static public boolean pluginJR = false;
    static public boolean pluginAchievo = false;
    static public boolean pluginAutoUpdate = false;
    static public boolean pluginEmail = false;
    static public boolean pluginBarCode = false;
    static public boolean pluginRitenute = false;
    static public boolean pluginRiba = false;
    static public boolean pluginRicerca = false;
    static public boolean pluginDdtIntra = false;
    static public boolean pluginScontrini = false;
    static public boolean pluginEbay = false;
    static public boolean pluginContabilita = false;
    public static boolean pluginImportExport = false;
    static public PluginFactory pf = null;
    static public String plugins_path = "plugins/";
    static public Attivazione attivazione = new Attivazione();
    static Process mysqlproc = null;
    static public String paramProp = "param_prop.txt";

//    static public frmIntro splash = null;
    static public JFrameIntro2 splash = null;
    static Timer timerMem = new Timer("timerMem");
    static TimerTask timerMemTask = new TimerTask() {
        @Override
        public void run() {
//            if (main.getPadrePanel() != null && main.getPadrePanel().menFunzioniManutenzione != null && main.getPadrePanel().menFunzioniManutenzione.isSelected()) {
            if (main.getPadrePanel() != null) {
                String msg = DebugUtils.getMem();

//                System.gc();
//                System.runFinalization();
//                System.out.println("--- Manutenzione Mem ----------------------------");
//                DebugUtils.dumpMem();
//
//                ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
//                ThreadGroup parentGroup;
//                while ((parentGroup = rootGroup.getParent()) != null) {
//                    rootGroup = parentGroup;
//                }
//                Thread[] threads = new Thread[rootGroup.activeCount()];
//                while (rootGroup.enumerate(threads, true) == threads.length) {
//                    threads = new Thread[threads.length * 2];
//                }
//                //DebugUtils.dump(threads);
//                System.out.println("--- Threads ----------------------------");
//                for (Thread t : threads) {
//                    if (t != null) {
//                        System.out.println("thread: " + t.getState() + " : " + t.toString());
//                    }
//                }
//                System.out.println("--- Fine Manutenzione ----------------------------");
                if (Sync.isActive()) {
                    msg += Sync.aggiornaDebug(main.getPadrePanel().autoUpdateTableSync.isSelected());
                }

                main.getPadrePanel().text_debug.setText(msg);
            }
        }
    };
    //no innodb
//    static public String win_startDb = ".\\mysql\\bin\\mysqld-nt.exe --no-defaults --standalone --console --basedir=mysql --skip-networking --enable-named-pipe --bind-address=localhost --skip-bdb --skip-innodb --pid-file=mysql_invoicex.pid";
//    static public String lin_startDb = "./mysql/bin/mysqld --no-defaults --basedir=mysql --user=root --port={port} --bind-address=127.0.0.1 --skip-bdb --skip-innodb --pid-file=mysql_invoicex.pid --socket=./invoicex_socket";
//    static public String mac_startDb = "./mysql/bin/mysqld --no-defaults --basedir=mysql --user=root --port={port} --bind-address=127.0.0.1 --skip-bdb --skip-innodb --pid-file=mysql_invoicex.pid --socket=./invoicex_socket";
    //con innodb
//    static public String win_startDb = ".\\mysql\\bin\\mysqld-nt.exe --no-defaults --standalone --console --basedir=mysql --skip-networking --enable-named-pipe --bind-address=localhost --pid-file=mysql_invoicex.pid --innodb_file_per_table --innodb_force_recovery=6";

    static public String pathmd5 = "";
    private static Object splash_last_value;

    static {
        try {
            pathmd5 = DigestUtils.md5Hex(new File(".").getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public String mysql_start_extraparam = "";

    //con named pipes
    //static public String win_startDb = ".\\mysql\\bin\\mysqld-nt.exe --no-defaults --standalone --console --basedir=mysql --skip-networking --enable-named-pipe --socket=mysql_" + pathmd5 + " --bind-address=127.0.0.1 --pid-file=mysql_invoicex.pid --innodb_file_per_table --max_allowed_packet=64M --innodb_flush_method=normal --language=italian";
    //con tcpip
    static public String win_startDb = ".\\mysql\\bin\\mysqld-nt.exe --no-defaults --standalone --console --basedir=mysql --skip-networking --enable-named-pipe --socket=mysql_" + pathmd5 + " --bind-address=127.0.0.1 --pid-file=mysql_invoicex.pid --innodb_file_per_table --max_allowed_packet=64M --innodb_flush_method=normal --language=italian";
    static public String lin_startDb = "./mysql/bin/mysqld --no-defaults --basedir=mysql --user=root --port={port} --bind-address=127.0.0.1 --pid-file=mysql_invoicex.pid --socket=./invoicex_socket --max_allowed_packet=64M";
    static public String mac_startDb = "./mysql/bin/mysqld --no-defaults --basedir=mysql --user=root --port={port} --bind-address=127.0.0.1 --pid-file=mysql_invoicex.pid --socket=./invoicex_socket --max_allowed_packet=64M";

    //per impostare una tmpdir (ad esempio su windows quando c'è l'errore 13)
    //aggiungere alla riga di comando di Invoicex.exe il paramentro -> mysql_extraparam=--tmpdir=../tmpdir
    //e creare la cartella tmpdir dentro la mysql di invoicex
    //kaspersky ? fare per tutti ?
    static public String win_stopDb = ".\\mysql\\bin\\mysqladmin.exe --no-defaults -W --socket=mysql_" + pathmd5 + " -u root -p{pwd} shutdown";
    static public String lin_stopDb = "./mysql/bin/mysqladmin --no-defaults --socket=./mysql/data/invoicex_socket --port={port} -u root -p{pwd} shutdown";
    static public String mac_stopDb = "./mysql/bin/mysqladmin --no-defaults --socket=./mysql/data/invoicex_socket --port={port} -u root -p{pwd} shutdown";
    static public String mac_stopDb_x86 = "./mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin --no-defaults --socket=./mysql/data/invoicex_socket --port={port} -u root -p{pwd} shutdown";
    static public String mac_stopDb_x86_file = "./mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin";

    static public boolean mysql_is_running = false;
    static public boolean apertura_manutenzione_incorso = false;

    static public boolean substance = false;

    static private String dir_user_home = null;

    static public String proxy = null;
    static public Font def_font = null;
    static public boolean primo_avvio = false;
    static public boolean db_in_rete = false;
    static public boolean fine_init_plugin = false;
    static JDialogPlugins dialogPlugins;
    static public boolean mysql_ready = false;
    static public String campiDatiAzienda = "ragione_sociale, indirizzo, localita, cap, provincia, telefono, fax, "
            + "intestazione_riga1, intestazione_riga2, intestazione_riga3, intestazione_riga4, intestazione_riga5, intestazione_riga6, "
            + "listino_base, targa, tipo_liquidazione_iva, id, piva,  cfiscale, flag_dati_inseriti, sito_web, email, testo_piede_fatt_v, "
            + "testo_piede_docu_v, testo_piede_ordi_v, testo_piede_fatt_a, testo_piede_docu_a, testo_piede_ordi_a, stampa_riga_invoicex, "
            + "label_cliente, label_destinazione, label_cliente_eng, label_destinazione_eng, provvigioni_tipo_data, stampare_timbro_firma, "
            + "testo_timbro_firma, tipo_numerazione, export_fatture_codice_iva, export_fatture_conto_ricavi, export_fatture_estrai_scadenze, "
            + "export_fatture_estrai_acconti, label_fornitore, label_fornitore_eng, provvigioni_tipo_calcolo, export_fatture_esig_diff_iva";
    static public boolean via_internet = false;
    static public boolean wizard_in_corso = false;
    private static boolean check_connessione_fail = false;
    private static boolean check_connessione_ok = false;
    public static Map<String, String> plugins_note_attivazione = new HashMap();
    public static String note_attivazione = "";

    public static boolean isBatch = false;
    public static boolean isServer = false;

    public static String int_dest_1_default = "<html><b><font size='3'>$F{ragione_sociale1}</font></b><br><font size='2'>$F{indirizzo1}<br>$F{cap_loc_prov1}</font><br><font size='1'>$F{piva_cfiscale_desc1}$F{recapito1}</font></html>";
    public static String int_dest_2_default = "<html><b><font size='3'>$F{ragione_sociale2}</font></b><br><font size='2'>$F{indirizzo2}<br>$F{cap_loc_prov2}</font><br><font size='1'>$F{recapito_2_sotto}$F{piva_cfiscale_desc_2_sotto}</font></html>";

//    public static String baseurlserver = "https://server.invoicex.it";        //problemi su windows xp, si bloccano le connessioni per alcuni secondi
    public static String baseurlserver = "http://server.invoicex.it";

    static public Magazzino magazzino = new Magazzino();

    static public int utenti = 0;

    public static int tempo_online = 60 * 3;  //secondi
//    public static int tempo_online = 10;  //secondi

    public static boolean db_gia_avviato_da_wizard = false;

    public static boolean edit_doc_in_temp = false;

    static public boolean splash_in_corso = false;

    static public final Map GLOB = new HashMap();

    static public Boolean pos = null;
    static public Integer disp_articoli_da_deposito = null;

    public static Frame getPadreWindow() {
        return main.padre_frame;
    }

    static public void splash(String string) {
        splash(string, null, null);
    }

    static public void splash(String string, Boolean indeterminate) {
        splash(string, indeterminate, null);
    }

    static public void splash(String string, Integer value) {
        splash(string, null, value);
    }

    static public void splash(final String string, final Boolean indeterminate, final Integer value) {
        if (value != null) {
            if (splash_last_value != null && cu.i0(value) < cu.i0(splash_last_value)) {
                System.out.println("SPLASH " + value);
                Thread.currentThread().dumpStack();
            }
            splash_last_value = value;
        }

        if (main.isBatch) {
            System.err.println("splash:" + string);
            return;
        }
        if (System.getProperty("java.awt.headless") != null && System.getProperty("java.awt.headless").equalsIgnoreCase("true")) {
//            System.out.println("splash headless: " + string + " indeterminate: " + indeterminate + " value: " + value);
            return;
        } else {
            if (splash_in_corso) {
                return;
            }
            splash_in_corso = true;
            SwingUtils.inEdt(new Runnable() {

                public void run() {

                    if (splash == null) {
                        splash = new JFrameIntro2();
                        splash.setLocationRelativeTo(null);
                        splash.pack();
                        splash.setVisible(true);
                    }
                    splash.labMess.setText(StringUtils.capitalize(string));
                    if (value != null) {
                        splash.jProgressBar1.setIndeterminate(false);
                        splash.jProgressBar1.setValue(value);
                    } else if (indeterminate != null) {
                        splash.jProgressBar1.setIndeterminate(indeterminate);
                    }

                    main.splash_in_corso = false;
                }
            });

        }
    }

    public static void check_connessione() throws Exception {
        //faccio test su download t che deve contenere 1
        System.out.println("! check conesione");
        if (main.check_connessione_fail) {
            System.out.println("! check conesione fail 1");
            throw new Exception("Impossibile connettersi al server di Invoicex");
        }
        if (main.check_connessione_ok) {
            System.out.println("! check conesione OK");
            return;
        }
        String surltest = main.baseurlserver + "/download/invoicex/t";
        InputStream is = null;
        try {
            String stest = HttpUtils.getUrlToStringUTF8(surltest);
            if (!stest.equals("1")) {
                System.out.println("!!! stest: " + stest);
                System.out.println("riprovo check_connessione a");
                stest = HttpUtils.getUrlToStringUTF8(surltest);
                if (!stest.equals("1")) {
                    main.check_connessione_fail = true;
                    System.out.println("! check conesione fail 2");
                    throw new Exception("Impossibile connettersi al server di Invoicex");
                }
            }
            main.check_connessione_ok = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("riprovo check_connessione b");
            try {
                String stest = HttpUtils.getUrlToStringUTF8(surltest);
                if (!stest.equals("1")) {
                    System.out.println("!!! stest: " + stest);
                    main.check_connessione_fail = true;
                    System.out.println("! check conesione fail 3");
                    throw new Exception("Impossibile connettersi al server di Invoicex");
                }
                System.out.println("! check conesione ok 2");
                main.check_connessione_ok = true;
                return;
            } catch (Exception e2) {
                main.check_connessione_fail = true;
                e2.printStackTrace();
                System.out.println("! check conesione fail 4");
                throw new Exception("Impossibile connettersi al server di Invoicex (e:" + e2.getMessage() + ")");
            }
        }
    }

    public main() {

    }

    private void controllaDati() {
        ResultSet r = null;
        boolean errors = false;
        Vector tablesWithError = new Vector();
        try {
            r = DbUtils.tryOpenResultSet(Db.getConn(), "show full tables");
            while (r.next()) {
                String tab = r.getString(1);
                String tipo = r.getString(2);
                if (tipo != null && !tipo.equalsIgnoreCase("VIEW")) {
                    String t = tab;
                    ResultSet rc = DbUtils.tryOpenResultSet(Db.getConn(), "check table " + t);
                    if (splash.isVisible()) {
                        splash("aggiornamenti struttura database ... check " + t);
                    }
                    try {
                        rc.next();
                        System.out.println(rc.getString(4));
                        System.out.println(r.getString(1) + " : checked");
                        if (!rc.getString(4).equals("OK")) {
                            if (rc.getString(4).indexOf("Found row where the auto_increment") < 0
                                    && !rc.getString(4).startsWith("View")) {
                                errors = true;
                                tablesWithError.add(r.getString(1));
                            }
                        }
                    } finally {
                        rc.getStatement().close();
                        rc.close();
                    }
                }
            }
        } catch (Exception err) {
            try {
                r.getStatement().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                r.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (errors == true) {
            //eseguo anche il repair
            try {
                for (int i = 0; i < tablesWithError.size(); i++) {
                    System.err.println("riparazione: " + tablesWithError.get(i).toString());
                    if (splash.isVisible()) {
                        splash("aggiornamenti struttura database ... repair " + tablesWithError.get(i).toString());
                    }
                    DbUtils.tryExecQuery(Db.getConn(), "repair table " + tablesWithError.get(i).toString());
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            //ricontrollo
            errors = false;
            tablesWithError.setSize(0);
            try {
                r = DbUtils.tryOpenResultSet(Db.getConn(), "show full tables");
                while (r.next()) {
                    String tab = r.getString(1);
                    String tipo = r.getString(2);
                    if (tipo != null && !tipo.equalsIgnoreCase("VIEW")) {
                        String t = tab;
                        splash("aggiornamenti struttura database ... check " + t);
                        ResultSet rc = DbUtils.tryOpenResultSet(Db.getConn(), "check table " + t);
                        try {
                            rc.next();
                            System.out.println(rc.getString(4));
                            System.out.println(r.getString(1) + " : checked");
                            if (!rc.getString(4).equals("OK")) {
                                if (rc.getString(4).indexOf("Found row where the auto_increment") < 0) {
                                    errors = true;
                                    tablesWithError.add(r.getString(1) + " : " + rc.getString(4));
                                }
                            }
                        } finally {
                            rc.getStatement().close();
                            rc.close();
                        }
                    }
                }
            } catch (Exception err) {
                try {
                    r.getStatement().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    r.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (errors) {
                String msg = "Le seguenti tabelle sono danneggiate:\n\n";
                for (Object t : tablesWithError) {
                    msg += String.valueOf(t) + "\n";
                }
                msg += "\nPotrebbero verificarsi problemi nell'uso del programma finchè non vengono riparate.";
                SwingUtils.showWarningMessage(main.getPadreWindow(), msg);
            }
        }

    }

    static public class ProxyAuthenticator extends Authenticator {

        private String user;
        char[] password;
        boolean chiesto = false;
        String proxy;

        public ProxyAuthenticator(String proxy) {
            this.proxy = proxy;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            if (System.getProperty("commonstnx.proxy.user", null) != null) {
                user = System.getProperty("commonstnx.proxy.user");
                password = System.getProperty("commonstnx.proxy.password").toCharArray();
                chiesto = true;
            }

            if (!chiesto) {
                JDialogProxyAuth dialog = new JDialogProxyAuth(main.getPadreFrame(), true);
                dialog.setTitle("Autenticazione Proxy: " + proxy);
                dialog.setLocationRelativeTo(main.getPadreWindow());
                dialog.setVisible(true);
                this.user = dialog.jTextField1.getText();
                this.password = dialog.jPasswordField1.getPassword();

                System.setProperty("invoicex.proxy.proxy", this.proxy);
                System.setProperty("invoicex.proxy.user", this.user);
                System.setProperty("invoicex.proxy.password", String.valueOf(this.password));

                chiesto = true;
            }
            return new PasswordAuthentication(user, password);
        }
    }

    public main(String[] args) {
        INSTANCE = this;

        //per debug mysql named pipe su windows su pipe fissa
        for (String arg : args) {
            if (arg.startsWith("pipe=")) {
                String pipe = StringUtils.substringAfter(arg, "pipe=");
                System.out.println("pipe = " + pipe);
                win_startDb = StringUtils.replace(win_startDb, "--socket=mysql_" + pathmd5, "--socket=mysql_" + pipe);
                pathmd5 = pipe;
                System.out.println("win_startDb = " + win_startDb);
            }
            //mysql_extraparam=--tmpdir=../tmp
            if (arg.startsWith("mysql_extraparam=")) {
                String mysql_extraparam = StringUtils.substringAfter(arg, "mysql_extraparam=");
                System.out.println("aggiungo mysql_extraparam = " + mysql_extraparam);
                win_startDb += " " + mysql_extraparam;
            }
        }

        //aggiungo gestore di errori generici
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();

                //cerco dentro allo stacktrace se ci sono errori derivanti dalle classi di Invoicex
                boolean trovate = false;
                for (StackTraceElement trace : e.getStackTrace()) {
                    if (trace.getClassName().indexOf("gestioneFatture") >= 0 || trace.getClassName().indexOf("it.tnx") >= 0 || trace.getClassName().indexOf("reports") >= 0) {
                        if (trace.getClassName().indexOf("MyEventQueue") < 0) {
                            trovate = true;
                        }
                        break;
                    }
                }

                if (trovate && !(e instanceof NullPointerException)) {
                    Frame comp = null;
                    if (getPadreWindow() != null) {
                        comp = getPadreWindow();
                    } else {
                        comp = Frame.getFrames()[0];
                        System.out.println("uncaughtException padre windows nulla, prendo frames[0]: " + comp);
                    }
                    //                SwingUtils.showErrorMessage(comp, "Sì è verificato il seguente errore:\n" + e.toString());
                    JDialogExc de = new JDialogExc(comp, true, e);
                    de.setLocationRelativeTo(null);
                    de.pack();
                    Toolkit.getDefaultToolkit().beep();
                    de.setVisible(true);
                } else {
                    e.printStackTrace();
                }
                if (padre_frame != null) {
                    padre_frame.setCursor(Cursor.getDefaultCursor());
                }
                if (getPadreWindow() != null) {
                    getPadreWindow().setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        //aggiungo controlli swing
        EventDispatchThreadHangMonitor.initMonitoring();

        //aggiungo altro controllo swing
//        RepaintManager.setCurrentManager(new TracingRepaintManager());
        //mio gestore coda edt per attaccare tasto destro su griglie
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new MyEventQueue());

        if (debug) {
            timerMem.schedule(timerMemTask, 1000 * 1, 1000 * 1);
        }

        //aggiungo gestione proxy automatica e proxy authentication
//        System.setProperty("http.proxyHost", "proxy host");
//        System.setProperty("http.proxyPort", "port");
//        System.setProperty("java.net.useSystemProxies", "true");  //spostato più in basso
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.out.println("properties principali");

        System.out.println("java.vendor:" + System.getProperty("java.vendor"));
        System.out.println("java.version:" + System.getProperty("java.version"));
        System.out.println("java.class.path:" + System.getProperty("java.class.path"));
        System.out.println("java.class.version:" + System.getProperty("java.class.version"));
        System.out.println("java.home:" + System.getProperty("java.home"));
        System.out.println("os.arch:" + System.getProperty("os.arch"));
        System.out.println("os.name:" + System.getProperty("os.name"));
        System.out.println("os.version:" + System.getProperty("os.version"));
        System.out.println("user.dir:" + System.getProperty("user.dir"));
        System.out.println("user.home:" + System.getProperty("user.home"));
        System.out.println("user.name:" + System.getProperty("user.name"));
        System.out.println("java.class.path:" + System.getProperty("java.class.path"));
        System.out.println("java.net.preferIPv4Stack:" + System.getProperty("java.net.preferIPv4Stack"));

        System.out.println("------------------------");

        System.out.println("parametri");
        try {
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            List<String> aList = bean.getInputArguments();
            for (int i = 0; i < aList.size(); i++) {
                System.out.println(aList.get(i));
            }
            System.out.println("max mem: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "mb");
            System.out.println("------------------------");
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (debug) {
            System.out.println("tutte le properties");
            Properties props = System.getProperties();
            props.list(System.out);

            System.out.println("------------------------");
        }

        File fwd = new File("./");
        try {
            wd = fwd.getCanonicalPath() + File.separator;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("wd:" + wd);

//        javax.xml.parsers.SAXParserFactory
//        org.apache.xerces.jaxp.SAXParserFactoryImpl
//        com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;     //per mac con problemi...
//        -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl     //per mac con errore creating sax parser..
//        System.out.println("javax.xml.parsers.SAXParserFactory prima:" + System.getProperty("javax.xml.parsers.SAXParserFactory"));
//        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
//        System.out.println("javax.xml.parsers.SAXParserFactory dopo:" + System.getProperty("javax.xml.parsers.SAXParserFactory"));
        //imposto anno di esercizio
        main.anno = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        //controllo quanti param_prop ci sono e se ce n'è più di uno faccio scegliere
        //param_prop.txt
        File filesParamDir = new File(main.wd);
        File[] filesParam = filesParamDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("param_prop") && name.endsWith(".txt")) {
                    return true;
                }
                return false;
            }
        });
        boolean scegli_config = false;
        if (filesParam.length > 1) {
            scegli_config = true;
        }
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.startsWith("config=")) {
                    paramProp = arg.substring(7);
                    scegli_config = false;
                }
            }
        }
        if (scegli_config) {
            Object ret = JOptionPane.showInputDialog(null, "Seleziona la configurazione",
                    "Attenzione",
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    filesParam, filesParam[0]);
            System.out.println("ret:" + ret);
            try {
                paramProp = ((File) ret).getName();
            } catch (Exception e) {
                System.exit(50);
            }

        }

        //creo il prop vuoto
        File f = new File(main.wd + paramProp);

        File fparamini = new File(main.wd + "param.ini");
        if (!f.exists() && !fparamini.exists()) {
            SwingUtils.showErrorMessage(main.getPadreWindow(), "Impossibile continuare, manca il file param.ini ed il file " + f.getName(), true);
            System.exit(0);
        }

        if (PlatformUtils.isWindows()) {
            //controllo se condiviso in rete
            File ftest = new File(".");
            System.out.println("f.getAbsolutePath() = " + f.getAbsolutePath());
            if (ftest.getAbsolutePath().startsWith("\\") || InvoicexUtil.isWindowsNetworkDrive(ftest.getAbsolutePath().substring(0, 2))) {
                System.out.println("is network !!!");
                String msg = "<html><font size=\"4\">Invoicex risulta installato in un disco di rete: ";
                msg += "<i>" + (new File("").getAbsolutePath()) + "</i><br>";
                msg += "<br><b>Invoicex non puo' funzionare in questo modo</b>, deve essere installato su un disco locale del computer.<br>";
                msg += "<br>Per usare Invoicex condiviso in rete è necessario installare MySql su di un server<br>";
                msg += "e configurare i client di Invoicex con i parametri del server.<br>";
                msg += "<br>Sicuro di continuare ?</font></html>";
                boolean continua = SwingUtils.showYesNoMessage(main.getPadreWindow(), msg, "ATTENZIONE !");
                if (!continua) {
                    System.exit(0);
                }
            }

            //controllo percorso di installazione
            try {
                Integer majv = Integer.parseInt(System.getProperty("os.version").substring(0, 1));
                Integer minv = Integer.parseInt(System.getProperty("os.version").substring(2));
                System.out.println("majv: " + majv);
                System.out.println("minv: " + minv);
                if (majv >= 6) {
                    String programfiles = System.getenv("ProgramFiles").toLowerCase();
                    System.out.println("programfiles = " + programfiles);
                    String apppath = new File("").getAbsolutePath().toLowerCase();
                    System.out.println("apppath = " + apppath);
                    if (apppath.startsWith(programfiles)) {
                        String msg = "<html><font size=\"4\"><b>Invoicex</b> risulta installato in<br>";
                        msg += "<b>\"" + (new File("").getAbsolutePath()) + "\"</b><br>";
                        msg += "Consigliamo caldamente di reinstallare Invoicex nel percorso suggerito <b>\"C:\\Users\\Public\"</b><br>";
                        msg += "per evitare problemi dovuti alla virtualizzazione del file system introdotte da Windows Vista<br></font></html>";
                        SwingUtils.showErrorMessage(main.getPadreWindow(), msg, true);
                    }
                }
            } catch (Exception e) {
            }
        }

        if (!f.exists()) {
            primo_avvio = true;

            try {
                Properties prop = new Properties();
                FileOutputStream fos = new FileOutputStream(main.wd + paramProp);
                prop.store(fos, "tnx properties file");
                fos.close();
            } catch (Exception err) {
                err.printStackTrace();
            }

            //converto il param.ini in param_prop.txt
            iniFile fileIniOld = new iniFile();
            iniFileProp fileIniProp = new iniFileProp();
            fileIniProp.realFileName = paramProp;
            fileIniOld.fileName = "param.ini";

            try {
                fileIniOld.loadFile();
                fileIniOld.parseLines();
                fileIniProp.setValue("db", "server", fileIniOld.getValue("db", "server"));
                fileIniProp.setValue("db", "nome_database", fileIniOld.getValue("db", "nome_database"));
                fileIniProp.setValue("db", "user", fileIniOld.getValue("db", "user"));
                fileIniProp.setValueCifrato("db", "pwd", fileIniOld.getValue("db", "pwd"));
                fileIniProp.setValue("db", "startdb", fileIniOld.getValue("db", "startdb"));
                fileIniProp.setValue("db", "stopdb", fileIniOld.getValue("db", "stopdb"));
                fileIniProp.setValue("db", "checkdb", fileIniOld.getValue("db", "checkdb"));
                fileIniProp.setValue("db", "startdbcheck", fileIniOld.getValue("db", "startdbcheck"));
                fileIniProp.setValue("varie", "finestre_grandi", fileIniOld.getValue("varie", "finestre_grandi"));
                fileIniProp.setValue("varie", "percorso_logo_stampe", StringUtils.replace(fileIniOld.getValue("varie", "percorso_logo_stampe"), "\\", "/"));
                fileIniProp.setValue("varie", "percorso_logo_stampe_pdf", StringUtils.replace(fileIniOld.getValue("varie", "percorso_logo_stampe"), "\\", "/"));
                fileIniProp.setValue("varie", "percorso_sfondo_proforma", fileIniOld.getValue("varie", "percorso_sfondo_proforma"));
//                fileIniProp.setValue("varie", "look", fileIniOld.getValue("varie", "look"));
                fileIniProp.setValue("varie", "look", "System");
                fileIniProp.setValue("varie", "prezziCliente", fileIniOld.getValue("varie", "prezziCliente"));
                fileIniProp.setValue("varie", "campoSerie", fileIniOld.getValue("varie", "campoSerie"));
                fileIniProp.setValue("varie", "non_stampare_logo", fileIniOld.getValue("varie", "non_stampare_logo"));
                fileIniProp.setValue("varie", "messaggio_stampa", fileIniOld.getValue("varie", "messaggio_stampa"));
                fileIniProp.setValue("personalizzazioni", "personalizzazioni", fileIniOld.getValue("personalizzazioni", "personalizzazioni"));
                fileIniProp.setValue("iva", "codiceIvaSpese", fileIniOld.getValue("iva", "codiceIvaSpese"));
                fileIniProp.setValue("iva", "codiceIvaDefault", fileIniOld.getValue("iva", "codiceIvaDefault"));
                fileIniProp.setValue("info", "inst_id", fileIniOld.getValue("info", "inst_id"));
                fileIniProp.setValue("info", "inst_email", fileIniOld.getValue("info", "inst_email"));
                fileIniProp.setValue("info", "inst_seriale", fileIniOld.getValue("info", "inst_seriale"));
                fileIniProp.setValue("info", "inst_nome", fileIniOld.getValue("info", "inst_nome"));
                fileIniProp.setValue("info", "inst_cognome", fileIniOld.getValue("info", "inst_cognome"));

//                //param def
//                try {
//                    java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);
//                    preferences.put("tipoStampa", "fattura_default.jrxml");
//                    preferences.put("tipoStampaFA", "fattura_acc_default.jrxml");
//                    preferences.put("tipoStampaDDT", "ddt_default.jrxml");
//                    preferences.put("tipoStampaOrdine", "ordine_default.jrxml");
//                    preferences.sync();
//                } catch (Exception ex1) {
//                    ex1.printStackTrace();
//                }
                //altri param di default
                fileIniProp.setValue("iva", "codiceIvaDefault", "22");
                fileIniProp.setValue("iva", "codiceIvaSpese", "");
                fileIniProp.setValue("pref", "generazione_movimenti", "1");
                fileIniProp.setValue("pref", "raggruppa_articoli", "2");
                fileIniProp.setValue("varie", "campoSerie", "N");
                fileIniProp.setValue("varie", "prezziCliente", "N");
                fileIniProp.setValue("pref", "soloItaliano", "true");
                fileIniProp.setValue("pref", "azioniPericolose", "true");
                fileIniProp.setValue("pref", "stampaPivaSotto", "true");

                fileIniProp.setValue("pref", "tipoStampa", "fattura_mod7_default.jrxml");
                fileIniProp.setValue("pref", "tipoStampaFA", "fattura_acc_mod7_default.jrxml");
                fileIniProp.setValue("pref", "tipoStampaOrdine", "ordine_mod7_default.jrxml");
                fileIniProp.setValue("pref", "tipoStampaDDT", "ddt_mod6_default.jrxml");
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        //----------------------------------------
        //if (!serial.equals("1")) {
        File test = new File(main.wd + paramProp);

        if (test.exists() == false) {
            javax.swing.JOptionPane.showMessageDialog(null, "Errore, Impossibile trovare i parametri");
            this.exitMain();
        }

        //controllo il param prop per il problema "\u0000\u0000\u0000\u0000\u0000"
        MicroBench mb = new MicroBench(true);
        boolean danneggiato = false;
        File fcheck0 = new File(main.wd + paramProp);
        mb.out("test param danneggiato 1");
        try {
            FileReader fcheck0reader = new FileReader(fcheck0);
            mb.out("test param danneggiato 2");
            Iterator fcheckiter = IOUtils.lineIterator(fcheck0reader);
            mb.out("test param danneggiato 3");
            while (fcheckiter.hasNext()) {
                String line = cu.toString(fcheckiter.next());
                if (line.indexOf("\\u0000\\u0000\\u0000") >= 0) {
                    fcheck0reader.close();
                    //problema riscontrato, se trovo il backup prendo quello                
                    danneggiato = true;
                    break;
                }
            }
            mb.out("test param danneggiato 4");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!danneggiato) {
            //controllo se vuoti i dati del db
            fileIni = new iniFileProp();
            mb.out("test param danneggiato 5");
            fileIni.realFileName = main.wd + paramProp;
            if (StringUtils.isBlank(cu.s(fileIni.getValue("db", "nome_database")))
                    && StringUtils.isBlank(cu.s(fileIni.getValue("db", "pwd")))
                    && StringUtils.isBlank(cu.s(fileIni.getValue("db", "server")))
                    && StringUtils.isBlank(cu.s(fileIni.getValue("db", "user")))
                    || StringUtils.isBlank(cu.s(fileIni.getValue("db", "server"))) || StringUtils.isBlank(cu.s(fileIni.getValue("db", "nome_database")))) {
                danneggiato = true;
            }
            mb.out("test param danneggiato 6");
        }
        if (danneggiato) {
            //problema riscontrato, se trovo il backup prendo quello                
            File fcheck0b = new File(main.wd + paramProp + ".backup");
            mb.out("test param danneggiato 7");
            if (fcheck0b.exists()) {
                try {
                    System.err.println("!!! problema nel param_prop !!! provo copia da backup");
                    FileUtils.copyFile(fcheck0b, fcheck0);
                    mb.out("test param danneggiato 8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.err.println("!!! problema nel param_prop !!! e non presente il backup");
                mb.out("test param danneggiato 9");
                SwingUtils.showErrorMessage(splash, "Il file di configurazione del programma (" + paramProp + ") è danneggiato\ne non è presente la copia di backup.\n\nImpossibile avviare il programma.\n\nPer avviare il programma con le impostazioni iniziali\neliminare manualmente il file di configurazione", true);
                mb.out("test param danneggiato 10");
                System.exit(1);
            }
        }

        mb.out("test param danneggiato fine");

        fileIni = new iniFileProp();
        fileIni.realFileName = main.wd + paramProp;

        if (fileIni.getValue("db", "nome_database", "").indexOf("toysforyou") >= 0 && !getPersonalContain("toysforyou")) {
            String nuovopers = fileIni.getValue("personalizzazioni", "personalizzazioni", "");
            nuovopers += ", toysforyou";
            fileIni.setValue("personalizzazioni", "personalizzazioni", nuovopers);
        }

        System.out.println("START APPLICATION");
        System.out.println("Versione " + version.toString() + " " + build);

        //setto italiano
        java.util.Locale.setDefault(java.util.Locale.ITALY);

        if (this.flagWebStart == false) {
            loadIni();
        }

        if (fileIni.getValue("db", "startdb") != null && fileIni.getValue("db", "startdb").equals("false")) {
            fileIni.setValue("db", "startdbcheck", "N");
            fileIni.saveFile();
        }

        //cambio invece che mettere startdb metto checkbox
        if (fileIni.getValue("db", "startdbcheck") == null || fileIni.getValue("db", "startdbcheck").length() == 0) {
            if (fileIni.getValue("db", "startdb") != null && fileIni.getValue("db", "startdb").length() > 0) {
                fileIni.setValue("db", "startdbcheck", "S");
            } else {
                fileIni.setValue("db", "startdbcheck", "N");
            }
            fileIni.saveFile();
        }

        if (fileIni.getValue("db", "startdbcheck") == null || fileIni.getValue("db", "startdbcheck").equalsIgnoreCase("N") || fileIni.getValue("db", "startdbcheck").equalsIgnoreCase("false")) {
            startDbCheck = false;
        } else {
            startDbCheck = true;
            startConDbCheck = true;
        }

        if (fileIni.getValueBoolean("proxy", "auto", true)) {
            System.setProperty("java.net.useSystemProxies", "true");

            //in caso in cui ci sia un proxy per http e non per socket normali come mysql sulle impostazioni del proxy mettere SOCKS senza proxy
            try {
                List l = ProxySelector.getDefault().select(new URI("http://www.tnx.it/"));
                for (Iterator iter = l.iterator(); iter.hasNext();) {
                    Proxy nproxy = (Proxy) iter.next();
                    System.err.println("proxy type : " + nproxy.type());
                    InetSocketAddress addr = (InetSocketAddress) nproxy.address();
                    if (addr == null) {
                        System.err.println("No Proxy");
                    } else {
                        System.err.println("proxy hostname : " + addr.getHostName());
                        System.err.println("proxy port : " + addr.getPort());
                        proxy = addr.getHostName() + ":" + addr.getPort();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.setProperty("java.net.useSystemProxies", "false");
            System.setProperty("http.proxyHost", fileIni.getValue("proxy", "server", ""));
            System.setProperty("http.proxyPort", fileIni.getValue("proxy", "porta", ""));
            System.setProperty("https.proxyHost", fileIni.getValue("proxy", "server", ""));
            System.setProperty("https.proxyPort", fileIni.getValue("proxy", "porta", ""));
            System.setProperty("socksProxyHost", fileIni.getValue("proxy", "server_socks", ""));
            System.setProperty("socksProxyPort", fileIni.getValue("proxy", "porta_socks", ""));
        }
        Authenticator.setDefault(new ProxyAuthenticator(proxy == null ? "" : proxy));

        //per splash screen con bordo
        //System.setProperty("sun.java2d.noddraw", "true");
        //wizard db
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception err) {
        }

//        UIManager.getDefaults().put("Button.showMnemonics", Boolean.TRUE);
        //prima di partire testo proxy
        try {
            check_connessione();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //----------------------------------------------------------------------
        //se attivo un mysql precedente lo chiudo prima dello wizard
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {

            //con windows utilizzo named pipes e non ho bisogno di porte aperte
            //test porte db
            //trovo una porta libera per mysql
            Db connessioneTest = new Db();
            if (startDbCheck) {
                int portaMin = 3306;
                //la porta la parso dal server //NO, parto dalla 3306 e provo ad aumentare
                //            if (Db.dbServ.length() > 0) {
                //                int ip = Db.dbServ.indexOf(":");
                //                if (ip > 0) {
                //                    String porta = Db.dbServ.substring(ip+1, Db.dbServ.length());
                //                    try {
                //                        portaMin = Integer.parseInt(porta);
                //                    } catch (NumberFormatException err) {
                //                        System.out.println("la porta nel nome del server non e' numerica:" + porta);
                //                    }
                //                }
                //            }
                int portaMax = portaMin + 10;
                int portaProva = portaMin;
                boolean portaOk = false;
                while (portaOk == false) {
                    try {
                        System.out.println("test porta : " + portaProva);
                        System.out.println("test porta esito ok: " + portaProva);
                        System.out.println("controllo che non ci sia già un mysql defunto..");

                        //controllo processi
                        String serviceName = "mysqld";
                        String s = "";
                        try {
                            Runtime Rt = Runtime.getRuntime();
                            InputStream ip = Rt.exec("ps axw").getInputStream();
                            BufferedReader in = new BufferedReader(new InputStreamReader(ip));
                            while ((s = in.readLine()) != null) {
                                System.out.println("ps:" + s);
                                if (s.indexOf(serviceName) >= 0 && s.indexOf(String.valueOf(portaProva)) >= 0) {
                                    System.out.println("!!:" + s);
                                    String pidps = s.trim().split(" ")[0];
                                    //confronto il pid da ps con quello in mysql\data
                                    String pidfile = null;
                                    try {
                                        File fpid = new File(main.wd + "mysql/data/mysql_invoicex.pid");
                                        if (fpid.exists()) {
                                            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(fpid)));
                                            pidfile = fr.readLine();
                                        }
                                    } catch (Exception ex) {
                                        System.out.println("ex:" + ex.toString());
                                    }
                                    System.out.println("pid da ps:" + pidps + " pid da file:" + pidfile);
                                    if (pidps.equals(pidfile)) {
                                        splash("chiusura precedente mysqld pid " + pidps);
                                        System.out.println("kill " + pidps);
                                        InputStream ip2 = Rt.exec("kill -9 " + pidps).getInputStream();
                                        BufferedReader in2 = new BufferedReader(new InputStreamReader(ip2));
                                        while ((s = in2.readLine()) != null) {
                                            System.out.println("out kill:" + s);
                                        }
                                        System.out.println("fine kill");
                                        try {
                                            Thread.sleep(3000);
                                        } catch (Exception ex) {
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //primo test con socket
                        try {
                            Socket client = new Socket();
                            client.connect(new InetSocketAddress("127.0.0.1", portaProva), 2000);
                            portaProva++;
                            client.close();
                        } catch (IOException ioexp0) {
                            //errore quindi dovrebbe essere libera..
                            //secondo test con server socket
                            ServerSocket socket = new ServerSocket(portaProva);
                            portaOk = true;
                            Db.dbPort = portaProva;
                            dbPortaOk = portaProva;
                            socket.close();
                        }

                    } catch (IOException ioexp) {
                        System.out.println("test porta esito KO: " + ioexp);
                        portaProva++;
                    }
                }
                if (portaOk == false) {
                    JOptionPane.showMessageDialog(null, "Impossibile attivare il database: nessuna porta libera da " + portaMin + " a " + portaMax, "Errore", JOptionPane.ERROR_MESSAGE);
                    exitMain(false);
                }
            } else if (Db.dbPort == 0) {
                Db.dbPort = 3306;
            }
        } else {
            //controllo su windows se è rimasto un mysql di invoicex aperto (solitamente quando si termina invoicex brutalmente)
            try {
                List<String> processes = SystemUtils.listRunningProcesses();
                String result = "";
                Iterator<String> it = processes.iterator();
                String pid = null, args2 = "";
                while (it.hasNext()) {
                    result = it.next();
//                    System.out.println("result = " + result);
//                    if (result.startsWith("mysqld-nt.exe")) {
                    if (result.contains("mysqld-nt.exe")) {
                        args2 = StringUtils.split(result, "|")[1];
                        if (args2.equalsIgnoreCase(win_startDb)) {
                            pid = StringUtils.split(result, "|")[2];
                            System.out.println("trovato mysqld e provo kill : " + pid + " args:" + result);
                            splash("chiusura precedente mysqld pid " + pid);
                            SystemUtils.killProcess(pid);
                            //attendo che venga chiuso
                            Thread.sleep(5000);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //----------------------------------------------------------------------

        WizardDb wdb = new WizardDb();
        wdb.execute();
    }

    static public void controllaFlagPlugin(String plugin) {
        if (plugin.equals("pluginBackupTnx")) {
            //pluginBackupTnx = true;
        } else if (plugin.equals("pluginJR")) {
            pluginJR = true;
        } else if (plugin.equals("pluginClientManager")) {
            pluginClientManager = true;
        } else if (plugin.equals("pluginClientManager")) {
            pluginClientManager = true;
        } else if (plugin.equals("pluginAchievo")) {
            pluginAchievo = true;
        } else if (plugin.equals("pluginAutoUpdate")) {
            pluginAutoUpdate = true;
        } else if (plugin.equals("plulginEbay")) {
            pluginEbay = true;
        }
    }

    public void aggiornaStatoPlugin() {
        //init plugins
        Collection plugcol = pf.getAllEntryDescriptor();
        Iterator plugiter = plugcol.iterator();
        while (plugiter.hasNext()) {
            EntryDescriptor pd = (EntryDescriptor) plugiter.next();
            pluginPresenti.add(pd.getName());
            controllaAttivazionePlugin(pd, attivazione, pf);
            controllaFlagPlugin(pd.getName());
        }
    }

    private boolean controllaAttivazionePlugin(EntryDescriptor pd, Attivazione attivazione, PluginFactory pf) {
        return false;
    }

    static public EntryDescriptor getPluginDescriptor(String nome_breve) {
        Collection plugcol = pf.getAllEntryDescriptor();
        Iterator plugiter = plugcol.iterator();
        while (plugiter.hasNext()) {
            EntryDescriptor pd = (EntryDescriptor) plugiter.next();
            if (pd.getName().equalsIgnoreCase(nome_breve)) {
                return pd;
            }
        }
        return null;
    }

    static public PluginEntry getPluginEntry(String nome_breve) {
        Collection plugcol = pf.getAllEntryDescriptor();
        Iterator plugiter = plugcol.iterator();
        while (plugiter.hasNext()) {
            EntryDescriptor pd = (EntryDescriptor) plugiter.next();
            if (pd.getName().equalsIgnoreCase(nome_breve)) {
                return pf.getPluginEntry(pd.getId());
            }
        }
        return null;
    }

    static public void controllaupd() {
        String vl = version + " (" + build + ")";
        try {
            String url = main.baseurlserver + "/v.php?v=" + URLEncoder.encode(vl, "ISO-8859-1") + (main.attivazione.getIdRegistrazione() != null ? "&i=" + main.attivazione.getIdRegistrazione() : "") + "&so=" + URLEncoder.encode(System.getProperty("os.name") + ":" + System.getProperty("os.version"), "ISO-8859-1") + "&javav=" + URLEncoder.encode(System.getProperty("java.version"), "ISO-8859-1");
            System.out.println("url: " + url);
            String v = getURL(url);
//            if (pluginAutoUpdate == false && fileIni.getValueBoolean("pref", "msg_plugins_upd", true)) {
            if (pluginAutoUpdate == false && !fileIni.getValueBoolean("pref", "msg_plugins_upd_v_" + v, false)) {
                if (v != null && !vl.equalsIgnoreCase(v)) {
                    JDialogUpd d = new JDialogUpd(getPadreWindow(), true, v);
                    d.pack();
                    d.setLocationRelativeTo(null);
                    d.setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String dbserver = URLEncoder.encode(main.fileIni.getValue("db", "server"), "ISO-8859-1");
            String dbname = URLEncoder.encode(main.fileIni.getValue("db", "nome_database"), "ISO-8859-1");
            String plugins = "";
            for (String p : main.pluginPresenti) {
                plugins += p;
                boolean attivo = false;
                for (String pa : main.pluginAttivi) {
                    if (pa.equals(p)) {
                        attivo = true;
                        break;
                    }
                }
                if (attivo) {
                    plugins += ";A";
                } else {
                    plugins += ";";
                }
                plugins += "|";
            }
            plugins = URLEncoder.encode(plugins, "ISO-8859-1");
            String url = main.baseurlserver + "/n.php?v=" + URLEncoder.encode(vl, "ISO-8859-1") + (main.attivazione.getIdRegistrazione() != null ? "&i=" + main.attivazione.getIdRegistrazione() : "") + "&so=" + URLEncoder.encode(System.getProperty("os.name") + ":" + System.getProperty("os.version"), "ISO-8859-1")
                    + "&dbs=" + URLEncoder.encode(dbserver, "ISO-8859-1")
                    + "&dbn=" + URLEncoder.encode(dbname, "ISO-8859-1")
                    + "&ver=" + URLEncoder.encode(main.versione, "ISO-8859-1")
                    + "&p=" + URLEncoder.encode(plugins, "ISO-8859-1");
            System.out.println("url: " + url);
            String news = getURL(url);
//            System.out.println("news: " + news);

            JSONParser p = new JSONParser();
            JSONObject jo = (JSONObject) p.parse(news);
//            System.out.println("jo: " + jo);
            JSONArray lista_news = (JSONArray) jo.get("n");
//            System.out.println(lista_news);
            for (Object nnews : lista_news) {
                try {
                    String id = (String) ((JSONObject) nnews).get("id");
                    if (main.fileIni.getValue("news", "non_visualizzare", "").indexOf(id + "|") < 0) {
                        JSONObject jn = (JSONObject) nnews;
                        String scadenza = (String) jn.get("scadenza");
                        if (scadenza != null && scadenza.length() > 0) {
                            try {
                                Date dscadenza = CastUtils.toDateIta(scadenza);
                                if (dscadenza != null) {
                                    if (dscadenza.before(new Date())) {
//                                        System.out.println("non faccio vedere news " + jn.get("id") + " perchè scaduta (scadenza: " + scadenza + ")");
                                        continue;
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }

                        main.INSTANCE.getPadrePanel().showNews((JSONObject) nnews);
                        Thread.sleep(1000);
                        while (getPadrePanel().news.size() > 0) {
                            Thread.sleep(1000);
//                            System.out.println("getPadrePanel().news.size():" + getPadrePanel().news.size());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getURL(String url) throws Exception {
        System.out.println("main.getURL-0: " + url);
        check_connessione();
        System.out.println("main.getURL-1: " + url);

        String ret = null;
        HttpClient httpclient = getHttpClient();
        GetMethod httpGet = new GetMethod(url);
        int retcode = 0;
        String message = null;
        try {
            retcode = httpclient.executeMethod(httpGet);
            if (debug) {
                System.out.println("httpClient getURL: status: " + httpGet.getStatusLine());
//                for (Header h : httpGet.getResponseHeaders()) {
//                    System.out.println("httpClient getURL: header: " + h);
//                }
            }
            retcode = httpGet.getStatusLine().getStatusCode();
            message = httpGet.getStatusLine().getReasonPhrase();
            ret = httpGet.getResponseBodyAsString();
        } catch (Exception ex) {
            throw ex;
        } finally {
            httpGet.releaseConnection();
        }

        if (debug) {
            System.out.println("httpClient getURL: ret: " + ret);
        }

        if (retcode != 200) {
            System.err.println("httpClient getURL: errore retcode:" + retcode + " msg:" + message + " daurl:" + url);
            return null;
        }

        return ret;
    }

    public static void main(String[] args) {
        main main1 = new main(args);
    }

    public static void startdb() {

        try {

            //            String tempStartDb = fileIni.getValue("db","startdb");
            //            if (tempStartDb.length() > 0) {
            if (startDbCheck) {
                via_internet = true;
                System.out.println("os.name:" + System.getProperty("os.name"));
                System.out.println("os.name check:mac " + (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0));

                if (System.getProperty("os.name").toLowerCase().startsWith("mac") || System.getProperty("os.name").toLowerCase().startsWith("lin")) {

                    try {
                        System.out.println("cambio permessi per esecuzione mysqld");
                        Process p = Runtime.getRuntime().exec("chmod 777 mysql/bin/mysqld");
                        int ret = p.waitFor();
                        System.out.println("ret:" + ret);

                        System.out.println("cambio permessi per esecuzione mysqladmin");
                        p = Runtime.getRuntime().exec("chmod 777 mysql/bin/mysqladmin");
                        ret = p.waitFor();
                        System.out.println("mysqlproc ret:" + ret);

                        if (PlatformUtils.isMac()) {
                            if (!System.getProperty("os.arch").equals("ppc")) {
                                p = Runtime.getRuntime().exec("chmod 777 mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin");
                                ret = p.waitFor();
                                System.out.println("mysqlproc-x86 ret:" + ret);
                            }
                        }
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }

                System.out.println("STARTING DB");

                class StartDbThread
                        extends Thread {

                    public String tempStartDb;

                    public void run() {

                        Runtime rt = Runtime.getRuntime();

                        try {

                            Process proc = rt.exec(tempStartDb);
                            mysqlproc = proc;

                            // any error message?
                            MyStreamGobblerMain errorGobbler = new MyStreamGobblerMain(proc.getErrorStream(), "ERROR") {
                                @Override
                                public void line(String line) {
                                    if (!mysql_ready) {
                                        if (line.indexOf("ready for connections") >= 0) {
                                            mysql_ready = true;
                                        }
                                    }
                                }
                            };

                            // any output?
                            MyStreamGobblerMain outputGobbler = new MyStreamGobblerMain(proc.getInputStream(), "OUTPUT");

                            // kick them off
                            errorGobbler.start();
                            outputGobbler.start();

                            mysql_is_running = true;

                            // any error???
                            int exitVal = proc.waitFor();
                            System.out.println("\t\t\t### dbt exit value: " + exitVal);
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                }

                StartDbThread dbt = new StartDbThread();
                dbt.tempStartDb = startDb;

                if (debug) {
                    System.out.println("stardb:" + dbt.tempStartDb);
                }

                dbt.start();
                System.out.println("STARTING DB");
            } else {
                System.out.println("NO DB TO START");
                if (fileIni.getValue("db", "server").toLowerCase().endsWith("tnx.it")) {
                    via_internet = true;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    String spezza(String stringa, int ogni) {

        String temp = "";

        for (int i = 0; i < stringa.length(); i = i + ogni) {

            if ((i + ogni) > stringa.length()) {
                temp += stringa.substring(i, stringa.length()) + "\n";
            } else {
                temp += stringa.substring(i, i + ogni) + "\n";
            }
        }

        return (temp);
    }

    public static Menu getPadre() {
        return (padre);
    }

    public static MenuFrame getPadreFrame() {
        return (padre_frame);
    }

    public static MenuPanel getPadrePanel() {
        return (padre_panel);
    }

    static public void loadIni() {
        fileIni.loadFile();
        fileIni.parseLines();

        String server = null;
        Db.dbServ = fileIni.getValue("db", "server").toLowerCase();

        //la porta la parso dal server
        if (Db.dbServ.length() > 0) {

            int ip = Db.dbServ.indexOf(":");

            if (ip > 0) {
                server = Db.dbServ.substring(0, ip);

                if (!startDbCheck) {

                    String porta = Db.dbServ.substring(ip + 1, Db.dbServ.length());

                    try {
                        Db.dbPort = Integer.parseInt(porta);
                    } catch (NumberFormatException err) {
                        System.out.println("la porta nel nome del server non e' numerica:" + porta);
                    }
                }
            }
        }

        //debug
        System.out.println("db server:" + Db.dbServ);
        Db.dbNameDB = fileIni.getValue("db", "nome_database");
        Db.dbName = fileIni.getValue("db", "user");
        //richiedo password
        if (!fileIni.getValueBoolean("pref", "richiediPassword", false)) {
            Db.dbPass = fileIni.getValueCifrato("db", "pwd");
        }

        if (fileIni.getValue("varie", "finestre_grandi").equalsIgnoreCase("si")) {
            iniFinestreGrandi = true;
        } else {
            iniFinestreGrandi = false;
        }

//        iniPercorsoLogoStampe = fileIni.getValue("varie", "percorso_logo_stampe");
//        iniPercorsoLogoStampePdf = fileIni.getValue("varie", "percorso_logo_stampe_pdf");
        if (fileIni.existKey("varie", "percorso_sfondo_stampe_" + Db.dbNameDB)) {
            iniPercorsoSfondoStampe = fileIni.getValue("varie", "percorso_sfondo_stampe_" + Db.dbNameDB);
        } else {
            iniPercorsoSfondoStampe = fileIni.getValue("varie", "percorso_sfondo_stampe");
        }
        if (fileIni.existKey("varie", "percorso_sfondo_stampe_pdf_" + Db.dbNameDB)) {
            iniPercorsoSfondoStampePdf = fileIni.getValue("varie", "percorso_sfondo_stampe_pdf_" + Db.dbNameDB);
        } else {
            iniPercorsoSfondoStampePdf = fileIni.getValue("varie", "percorso_sfondo_stampe_pdf");
        }

        System.out.println("main set iniPercorsoSfondoStampe:" + iniPercorsoSfondoStampe);
        System.out.println("main set iniPercorsoSfondoStampePdf:" + iniPercorsoSfondoStampePdf);

        iniPercorsoSfondoProforma = fileIni.getValue("varie", "percorso_sfondo_proforma");
        iniDirFatture = fileIni.getValue("varie", "percorso_fatture");
        iniComandoGs = fileIni.getValue("varie", "comando_gs");

        if (fileIni.getValue("varie", "gestione_magazzino").equalsIgnoreCase("no")) {
            iniFlagMagazzino = false;
        } else {
            iniFlagMagazzino = true;
        }

        if (fileIni.getValue("varie", "prezziCliente").equalsIgnoreCase("S")) {
            iniPrezziCliente = true;
        } else {
            iniPrezziCliente = false;
        }

        if (fileIni.getValue("varie", "campoSerie").equalsIgnoreCase("S")) {
            iniSerie = true;
        } else {
            iniSerie = false;
        }

        //debug
        //javax.swing.JOptionPane.showMessageDialog(null,"logo:" + iniPercorsoLogoStampe);
    }

    static private String apriFile(String nomeFile) {

        try {

            DataInputStream fileInput = new DataInputStream(new FileInputStream(new File(main.wd + nomeFile)));
            String righe = "";
            char in;
            int a = 1;

            try {

                while (a == 1) {
                    in = (char) fileInput.readByte();
                    righe += in;
                }
            } catch (EOFException err) {
            }

            return (righe);
        } catch (Exception err) {
            err.printStackTrace();

            return (null);
        }
    }

    public static void exitMain() {
        System.out.println("exitMain -> exitMain(true);");
        exitMain(true);
    }

    public static void exitMain(final boolean chiudiDb) {
        //premo annulla su eventuali testate di documento aperte
        try {
            Component[] arrComp = main.getPadrePanel().getDesktopPane().getComponents();
            for (int i = 0; i < arrComp.length; i++) {
                if (arrComp[i] instanceof frmTestFatt) {
                    System.out.println("chiudo frmTestFatt " + arrComp[i]);
                    ((frmTestFatt) arrComp[i]).annulla();
                } else if (arrComp[i] instanceof frmTestDocu) {
                    System.out.println("chiudo frmTestDocu " + arrComp[i]);
                    ((frmTestDocu) arrComp[i]).annulla();
                } else if (arrComp[i] instanceof frmTestOrdine) {
                    System.out.println("chiudo frmTestOrdine " + arrComp[i]);
                    ((frmTestOrdine) arrComp[i]).annulla();
                } else if (arrComp[i] instanceof frmTestFattAcquisto) {
                    System.out.println("chiudo frmTestFattAcquisto " + arrComp[i]);
                    ((frmTestFattAcquisto) arrComp[i]).annulla();
                }
            }
        } catch (Exception e) {
        }

        String quando = main.fileIni.getValue("backup", "quando", "Chiedere ogni 2 settimane");
//"Chiedere ogni 2 settimane", "Chiedere ogni settimana", "Chiedere tutti i giorni", "Chiedi alla chiusura di Invoicex", "Alla chiusura di Invoicex senza chiedere", "Mai" }));
        boolean farebackup = false;
        if (quando.equalsIgnoreCase("Chiedi alla chiusura di Invoicex")) {
            if (SwingUtils.showYesNoMessage(main.padre_frame, "Vuoi eseguire la copia di sicurezza dei dati ?")) {
                farebackup = true;
            }
        } else if (quando.equalsIgnoreCase("Alla chiusura di Invoicex senza chiedere")) {
            farebackup = true;
        }
        DumpThread dump2 = null;
        if (farebackup) {
//            if (pluginBackupTnx) {
//                System.out.println("inizio backup online");
//                main.getPadrePanel().callBackupOnline();
//                System.out.println("backup online completato");
//            } else {
            //eseguo un dump del database
            System.out.println("inizio backup locale");
            //visualizzo frame con log processo
            it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
            try {
                mess.setIconImage(getLogoIcon());
            } catch (Exception err) {
                err.printStackTrace();
            }
            mess.setBounds(100, 100, 400, 200);
            mess.show();

            dump2 = new DumpThread(mess);
            dump2.start();
//            }
        }
        final DumpThread fdump = dump2;

        Thread t = new Thread("chiusura") {
            boolean attendere = true;

            @Override
            public void run() {
                //attendo che vengano effettivamente chiuse altrimenti si rischia di perdere le righe dei documenti.

                while (attendere) {
                    attendere = false;
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                try {
                                    Component[] arrComp = main.getPadrePanel().getDesktopPane().getComponents();
                                    for (int i = 0; i < arrComp.length; i++) {
                                        if (arrComp[i] instanceof frmTestFatt) {
                                            attendere = true;
                                        } else if (arrComp[i] instanceof frmTestDocu) {
                                            attendere = true;
                                        } else if (arrComp[i] instanceof frmTestOrdine) {
                                            attendere = true;
                                        } else if (arrComp[i] instanceof frmTestFattAcquisto) {
                                            attendere = true;
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fdump != null && fdump.isAlive()) {
                        System.out.println("attendo backup");
                        attendere = true;
                    }
                    if (attendere) {
                        try {
                            System.out.println("attendo...");
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                    }
                }

                try {
                    if (!chiudiDb) {    //lo chiude dopo
                        Db.dbClose();
                    }
                } catch (Exception ex0) {
                    ex0.printStackTrace();
                }

                //stoppo i plugins
                try {
                    Set<Entry<String, PluginEntry>> pa = pluginsAvviati.entrySet();
                    for (Entry<String, PluginEntry> e : pa) {
                        try {
                            System.out.println("stoppo plugin: " + e.getKey());
                            MicroBench mb = new MicroBench();
                            mb.start();
                            PluginEntry pe = e.getValue();
                            pe.stopPluginEntry();
                            System.out.println("stoppo plugin: " + e.getKey() + " ... stoppato in " + mb.getDiff(""));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (chiudiDb) {
                    stopdb(true);
                }

            }
        };
        t.start();

    }

    public static void stopdb(final boolean exit) {
        /*
         SwingUtilities.invokeLater(new Runnable() {
         public void run() {
         final JDialog dialog = new JDialogChiusura();
         if (exit) {
         dialog.setLocationRelativeTo(null);
         dialog.setVisible(true);
         }
         }
         });

         if (mysqlproc != null) {
         try {
         System.out.println("\t\t\t### mysqlproc0:" + mysqlproc);
         mysqlproc.getOutputStream().write(0x03);
         mysqlproc.getOutputStream().flush();
         mysqlproc.getOutputStream().write(0x03);
         mysqlproc.getOutputStream().flush();
         System.out.println("\t\t\t### mysqlproc1:" + mysqlproc);
         } catch (Exception err) {
         System.out.println(err + " / " + err.getStackTrace()[0]);
         }
         try {
         System.out.println("\t\t\t### mysqlproc2:" + mysqlproc);
         mysqlproc.destroy();
         System.out.println("\t\t\t### mysqlproc3:" + mysqlproc);
         } catch (Exception err) {
         System.out.println(err + " / " + err.getStackTrace()[0]);
         }
         }

         if (exit) {
         System.out.println("\t\t\t### exit");
         System.exit(0);
         }
         */

        final JDialog dialog = new JDialogChiusura(main.getPadreWindow(), true);
        if (exit) {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        org.jdesktop.swingworker.SwingWorker wstop = new org.jdesktop.swingworker.SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                if (mysqlproc != null) {
                    Thread t = new Thread("wstop") {
                        @Override
                        public void run() {
                            try {
                                //killo le connessioni
                                Object conn_id = DbUtils.getObject(Db.conn, "select CONNECTION_ID()");
                                System.out.println("this conn id: " + conn_id);
                                List<Map> process = DbUtils.getListMap(Db.conn, "show processlist");
                                DebugFastUtils.dump(process);
                                for (Map m : process) {
                                    Long id = CastUtils.toLong(m.get("Id"));
                                    if (id.equals(conn_id)) {
                                        System.out.println("non killo conn: " + id + ", uguale a conn_id");
                                    } else {
//                                        System.out.println("kill conn: " + id);
//                                        DbUtils.tryExecQuery(Db.conn, "kill " + id);
                                    }
                                }
//                                System.out.println("kill my conn: " + conn_id);
                            } catch (Exception ex0) {
                                ex0.printStackTrace();
                            }
                            try {
                                Db.dbClose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Runtime rt = Runtime.getRuntime();
                                String stopdb = null;
                                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                                    System.out.println("stopdb per win");
                                    stopdb = win_stopDb;
                                } else if (System.getProperty("os.name").toLowerCase().indexOf("lin") >= 0) {
                                    System.out.println("stopdb per linux");
                                    stopdb = lin_stopDb;
                                } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                                    System.out.println("stopdb per mac");
                                    stopdb = mac_stopDb;
                                    try {
                                        if (!System.getProperty("os.arch").equals("ppc")) {
                                            File ftest = new File(mac_stopDb_x86_file);
                                            if (ftest.exists()) {
                                                stopdb = mac_stopDb_x86;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                stopdb = StringUtils.replace(stopdb, "{port}", String.valueOf(Db.dbPort));
                                System.out.println("stopdb = " + stopdb);
                                stopdb = StringUtils.replace(stopdb, "{pwd}", String.valueOf(Db.dbPass));

                                Process proc = rt.exec(stopdb);
                                MyStreamGobblerMain errorGobbler = new MyStreamGobblerMain(proc.getErrorStream(), "ERROR-mysqlstop");
                                MyStreamGobblerMain outputGobbler = new MyStreamGobblerMain(proc.getInputStream(), "OUTPUT-mysqlstop");
                                errorGobbler.start();
                                outputGobbler.start();

                                // any error???
                                int exitVal = proc.waitFor();
                                System.out.println("\t\t\t### mysqlstop exit value: " + exitVal);

                                mysql_is_running = false;

//                                System.out.println("\t\t\t### mysqlproc0:" + mysqlproc);
//                                mysqlproc.getOutputStream().write(0x03);
//                                mysqlproc.getOutputStream().flush();
//                                mysqlproc.getOutputStream().write(0x03);
//                                mysqlproc.getOutputStream().flush();
//                                System.out.println("\t\t\t### mysqlproc1:" + mysqlproc);
                            } catch (Exception err) {
                                System.out.println(err + " / " + err.getStackTrace()[0]);
                            }
//                            try {
//                                System.out.println("\t\t\t### mysqlproc2:" + mysqlproc);
//                                mysqlproc.destroy();
//                                System.out.println("\t\t\t### mysqlproc3:" + mysqlproc);
//                            } catch (Exception err) {
//                                System.out.println(err + " / " + err.getStackTrace()[0]);
//                            }
                        }
                    };
                    t.start();
                    long t1 = System.currentTimeMillis();
                    long t2 = System.currentTimeMillis();

                    while (t.isAlive() && (t2 - t1) < 10000 * 2) {
                        System.out.println("attendo mysqlproc");
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                        t2 = System.currentTimeMillis();
                    }
                    System.out.println("exit while wstop");
                }

                if (exit) {
                    System.out.println("\t\t\t### exit");
                    try {
                        if (main.getPadreFrame() != null) {
                            main.getPadreFrame().setVisible(false);
                            main.getPadreFrame().dispose();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }

                return null;
            }

            @Override
            protected void done() {
                if (dialog != null) {
                    dialog.dispose();
                }
            }

            @Override
            protected void process(List chunks) {
                super.process(chunks);
            }
        };
        wstop.execute();
    }

    public static java.net.URL getImageUrl(String value) {

        try {

            if (main.flagWebStart == false) {

                return new java.net.URL(value);
            } else {

                java.net.URL tmImageURL = Class.class.getResource("/" + value);

                return tmImageURL;
            }
        } catch (java.net.MalformedURLException errUrl) {
            System.err.println("getImageUrl:malformedUrl:" + value);

            return null;
        }
    }

    public static javax.swing.ImageIcon getImageIcon(Object parent, String value) {

        java.net.URL url = null;

        try {

            //javax.swing.JOptionPane.showMessageDialog(null, "value:" + value.substring(2));
            if (main.flagWebStart == false) {
                url = new java.net.URL("file:" + value);
            } else {

                // Get current classloader
                ClassLoader cl = parent.getClass().getClassLoader();

                // Create icons
                //Icon icon = new ImageIcon(cl.getResource("img/general/New16.gif"));
                return new javax.swing.ImageIcon(cl.getResource(value.substring(2)));

                //url = Class.class.getResource(value.substring(2));
                //return new javax.swing.ImageIcon(icon);
            }

            //javax.swing.JOptionPane.showMessageDialog(null, "url:" + url.toString());
            return new javax.swing.ImageIcon(url);
        } catch (Exception err) {
            System.err.println("getImageIcon:err:" + value);
            err.printStackTrace();

            return null;
        }
    }

    public static com.lowagie.text.Image getItextImage(Object parent, String fileName) {

        String fileNameCorrected;

        if (main.flagWebStart == false) {

            //in casoo venga lanciato da installazione normale
            try {

                return com.lowagie.text.Image.getInstance(fileName);
            } catch (Exception errNows) {
                errNows.printStackTrace();
            }
        } else {

            //in caso di webstart
            try {

                if (fileName.startsWith(".")) {
                    fileNameCorrected = fileName.substring(2);
                } else if (fileName.startsWith("/")) {
                    fileNameCorrected = fileName.substring(1);
                } else {
                    fileNameCorrected = fileName;
                }
            } catch (Exception errString) {
                fileNameCorrected = fileName;
            }

            try {

                ClassLoader cl = parent.getClass().getClassLoader();

                //javax.swing.JOptionPane.showMessageDialog(null, "file:" + cl.getResource("img/logo.gif").getFile());
                //InputStream iStream = cl.getResource("img/logo.gif").openStream();
                InputStream iStream = cl.getResource(fileNameCorrected).openStream();

                // Create an input stream
                int bufferSize = 8192;
                BufferedInputStream responseStream = new BufferedInputStream(iStream, bufferSize);

                // Read some data
                byte[] inputBuffer = new byte[bufferSize];
                ByteBuffer bb = new ByteBuffer();
                int inputSize = responseStream.read(inputBuffer);
                int count = 0;

                // while there is data read some and blast it into the
                while (inputSize > 0) {
                    System.out.println(inputBuffer);
                    bb.append(inputBuffer);
                    count += inputSize;

                    // read next chunk of data
                    inputSize = responseStream.read(inputBuffer);
                }

                responseStream.close();

                return com.lowagie.text.Image.getInstance(bb.toByteArray());
            } catch (Exception errImage) {
                errImage.printStackTrace();
            }
        }

        return null;
    }

    public static String getPersonal() {
        if (Util.getIniValue("personalizzazioni", "personalizzazioni").indexOf("lux1") >= 0) {
            return PERSONAL_LUXURY;
        }
        return "";
    }

    public static boolean getPersonalContain(String personal) {
        if (Util.getIniValue("personalizzazioni", "personalizzazioni").indexOf(personal) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getProperty(String prop) {

        if (applicationProps == null) {
            return null;
        }

        return applicationProps.getProperty(prop);
    }

    public static String getListinoBase() {

        ResultSet r = Db.openResultSet("select listino_base from dati_azienda");

        try {

            if (r.next()) {

                return r.getString(1);
            }
        } catch (java.sql.SQLException sqlErr) {
            sqlErr.printStackTrace();
        }

        return "-1";
    }

    public static String getTargaStandard() {

        ResultSet r = Db.openResultSet("select targa from dati_azienda");

        try {

            if (r.next()) {

                return r.getString(1);
            }
        } catch (java.sql.SQLException sqlErr) {
            sqlErr.printStackTrace();
        }

        return "";
    }

    public void post_wizard() throws Exception {
        try {
            //carico impostazioni personali
            // Get the Preferences object.  Note, the backing store is unspecified
            java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userNodeForPackage(main.class);

            String fontName = preferences.get("fontName", "Dialog");
            int fontSize = preferences.getInt("fontSize", 12);
            int fontSizePiccolo = preferences.getInt("fontSizePiccolo", 10);

            //look
            String look = main.fileIni.getValue("varie", "look");

            if (look.equalsIgnoreCase("Simple")) {
                System.out.println("Simple look and feel");
                MetalTheme theme = new gestioneFatture.look.TnxSandTheme();
                MetalLookAndFeel.setCurrentTheme(theme);
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else if (look.equalsIgnoreCase("System")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                try {
                    InvoicexUtil.checkGTKLookAndFeel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (look.equalsIgnoreCase("Substance Nebula")) {
                UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceNebulaLookAndFeel");
            } else if (look.equalsIgnoreCase("Substance BusinessBlackSteel")) {
                UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessBlueSteelLookAndFeel");
            } else if (look.equalsIgnoreCase("Substance Creme")) {
                UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceCremeLookAndFeel");
            } else if (look.equalsIgnoreCase("Tonic")) {
                UIManager.setLookAndFeel("com.digitprop.tonic.TonicLookAndFeel");
            } else if (look.equalsIgnoreCase("Office 2003")) {
                UIManager.setLookAndFeel("org.fife.plaf.Office2003.Office2003LookAndFeel");
            } else if (look.equalsIgnoreCase("Office XP")) {
                UIManager.setLookAndFeel("org.fife.plaf.OfficeXP.OfficeXPLookAndFeel");
            } else if (look.equalsIgnoreCase("Visual Studio 2005")) {
                UIManager.setLookAndFeel("org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel");
            } else if (look.equalsIgnoreCase("JGoodies Plastic XP")) {
                if (PlatformUtils.isMac()) {
                    UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
                } else {
                    UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
                }
            } else if (look.equalsIgnoreCase("Nimbus")) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } else if (PlatformUtils.isMac()) {
//                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceCremeLookAndFeel");
            } else {
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (UIManager.getLookAndFeel() instanceof PlasticLookAndFeel) {
            PlasticXPLookAndFeel l = new PlasticXPLookAndFeel();
            l.setPlasticTheme(new ExperienceBlue());
            try {
                UIManager.setLookAndFeel(l);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
//        if (PlatformUtils.isWindows()) {
//            UIDefaults uiDefaults = UIManager.getDefaults();
//            uiDefaults.put("Label.font", uiDefaults.get("TextField.font"));
//            uiDefaults.put("TextField.font", uiDefaults.get("Label.font"));
//        }
        def_font = UIManager.getDefaults().getFont("Label.font");
        System.out.println("exits font_family: " + fileIni.existKey("pref", "font_family"));
        System.out.println("font family: " + fileIni.getValue("pref", "font_family", main.def_font.getFamily()));
        System.out.println("font size: " + CastUtils.toInteger0(fileIni.getValue("pref", "font_size", CastUtils.toString(main.def_font.getSize()))));
        if (fileIni.existKey("pref", "font_family")) {
            String font_family = fileIni.getValue("pref", "font_family", main.def_font.getFamily());
            Integer font_size = CastUtils.toInteger0(fileIni.getValue("pref", "font_size", CastUtils.toString(main.def_font.getSize())));

//font_size = cu.toInteger0(JOptionPane.showInputDialog("font size"));
//fileIni.setValue("pref", "font_size", font_size);
            Font newf = new Font(font_family, Font.PLAIN, font_size);
            try {
                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                Enumeration e = uiDefaults.keys();
                while (e.hasMoreElements()) {
                    try {
                        Object item = e.nextElement();
                        if (item.toString().toLowerCase().indexOf("font") >= 0) {
                            Font foc = (Font) uiDefaults.get(item);
                            uiDefaults.put(item, newf);
//                        } else {
//                            String k = item.toString().toLowerCase();
//                            String ko = item.toString();
//                            if (k.indexOf("margin") >= 0 || k.indexOf("gap") >= 0 || k.indexOf("offset") >= 0 || k.indexOf("border") >= 0) {
//                                Object o = uiDefaults.get(ko);
//                                if (o != null) {
//                                    System.out.println(ko + "| " + o + " | class:" + o.getClass());
//                                    if (o instanceof Integer) {
//                                        uiDefaults.put(ko, 1);
//                                    } else if (o instanceof InsetsUIResource)  {
//                                        InsetsUIResource inset = (InsetsUIResource)o;
//                                        inset.set(1, 1, 1, 1);
//                                        uiDefaults.put(ko, inset);
//                                    }
//                                }
//                            }
                        }
                    } catch (Exception ex) {
                    }
                }
                System.out.println("fine ui");
            } catch (Exception ex2) {
            }
        } else if (PlatformUtils.isMac()) {
            try {
                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                Enumeration e = uiDefaults.keys();
                //Font fo = new Font("Geneva", Font.PLAIN, 10);
                Font fo = new Font("Lucida Grande", Font.PLAIN, def_font.getSize() - 3);
                while (e.hasMoreElements()) {
                    try {
                        Object item = e.nextElement();
                        if (item.toString().toLowerCase().indexOf("font") >= 0) {
                            Font foc = (Font) uiDefaults.get(item);
                            uiDefaults.put(item, fo);
                        }
                    } catch (Exception ex) {
                    }
                }
            } catch (Exception e) {
            }
        } else if (PlatformUtils.isLinux()) {
            try {
                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                Enumeration e = uiDefaults.keys();
                Font fo = new Font("Sans", Font.PLAIN, 10);
                while (e.hasMoreElements()) {
                    try {
                        Object item = e.nextElement();
                        if (item.toString().toLowerCase().indexOf("font") >= 0) {
                            Font foc = (Font) uiDefaults.get(item);
                            uiDefaults.put(item, fo);
                        }
                    } catch (Exception ex) {
                    }
                }
            } catch (Exception ex2) {
            }
        }

        if (UIManager.getLookAndFeel().getName().toLowerCase().indexOf("substance") >= 0) {
            substance = true;
        }
        
        


        //JIDE
        if (!PlatformUtils.isLinux()) {
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);
        }

        //shortcut per mac
        if (PlatformUtils.isMac() && !UIManager.getLookAndFeel().getName().equals(UIManager.getSystemLookAndFeelClassName())) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            im = (InputMap) UIManager.get("TextArea.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }

        try {
            //2016-09 disabilito accellerazione grafica su windows per problemi grafici 
            if (PlatformUtils.isWindows() && !(new File("Invoicex.l4j.ini").exists())) {
                List lines = new ArrayList();
                lines.add("# Launch4j runtime config");
                lines.add("-Dsun.java2d.noddraw=true");
                IOUtils.writeLines(lines, "\r\n", new FileOutputStream("Invoicex.l4j.ini"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //setto font per prova
//        //Segoe UI
//        try {
//            UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
//            Enumeration e = uiDefaults.keys();
//            Font fo = new Font("Segoe UI", Font.PLAIN, 13);
//            while (e.hasMoreElements()) {
//                try {
//                    Object item = e.nextElement();
//                    if (item.toString().toLowerCase().indexOf("font") >= 0) {
//                        Font foc = (Font) uiDefaults.get(item);
//                        uiDefaults.put(item, fo);
//                    }
//                } catch (Exception ex) {
//                }
//            }
//        } catch (Exception ex2) {
//        }
//        Splash splash1 = new Splash();
//        if (WindowUtils.isWindowAlphaSupported()) {
//            WindowUtils.setWindowAlpha(splash1, 0.9f);
//        }
//        splash1.setVisible(true);
        //intro
        splash("caricamento", 25);
        //--------------------------------------------------------------------

        //test Sync
        if (main.fileIni.getValueBoolean("sync", "attivo", false)) {
            //se attivo il sync testo login
            Sync.server = main.fileIni.getValue("sync", "server", "");
            Sync.nomedb = main.fileIni.getValue("sync", "nomedb", "");
            Sync.username = main.fileIni.getValue("sync", "username", "");
            Sync.password = main.fileIni.getValueCifrato("sync", "password", "");
            if (Sync.checkLogin(Sync.server, Sync.nomedb, Sync.username, Sync.password)) {
                Sync.setActive(true);
                tnxDbPanel.debug = true;
                edit_doc_in_temp = true;
            } else {
                System.out.println("check login sync fallito !");
                SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore nel login al server Sync", true);
                main.exitMain();
            }
        }

        //----------------------------------------------------------------------
        //richiedo password
        if (fileIni.getValueBoolean("pref", "richiediPassword", false)) {
            String pwd = SwingUtils.showInputPassword(splash, "Invoicex, password di accesso");
            if (pwd == null) {
                System.exit(0);
            }
            Db.dbPass = pwd;
        }

        if (splash != null) {
//            splash.toFront();
        }

        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                System.out.println("startdb per win");
                startDb = win_startDb;
            } else if (System.getProperty("os.name").toLowerCase().contains("lin")) {
                System.out.println("startdb per linux");
                startDb = StringUtils.replace(lin_startDb, "{port}", String.valueOf(Db.dbPort));
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                System.out.println("startdb per mac");
                startDb = StringUtils.replace(mac_startDb, "{port}", String.valueOf(Db.dbPort));
            }
            fileIni.setValue("db", "startdb", startDb);
        } catch (Exception err) {
            err.printStackTrace();
        }

        if (startDbCheck) {
            db_in_rete = false;
            if (this.flagWebStart == false) {
                if (!db_gia_avviato_da_wizard) {
                    startdb();
                    //prima controllo per 10 secondi se è arrivato il mysql_ready -> ready for connection da output
                    splash("attesa dell'avvio di mysql", true);
                    long t1 = System.currentTimeMillis();
                    long t2 = System.currentTimeMillis();
                    while (!mysql_ready && (t2 - t1) < 10000) {
                        System.out.println("attendo mysql_ready");
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                        }
                        t2 = System.currentTimeMillis();
                    }
                    System.out.println("mysql_ready: " + mysql_ready);
                }

                //se mac su x86 scarico mysqladmin
                try {
                    if (PlatformUtils.isMac()) {
                        if (!System.getProperty("os.arch").equals("ppc")) {
                            Thread t = new Thread("scarica mysqladmin") {
                                @Override
                                public void run() {
                                    try {
                                        File ren1 = new File("mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin.temp");
                                        File ren2 = new File("mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin");
                                        if (!ren2.exists()) {
                                            if (ren1.exists()) {
                                                ren1.delete();
                                            }
                                            HttpUtils.saveBigFile(main.baseurlserver + "/download/invoicex/utils/mysql-5.1.58-x86-32bit/mysqladmin", "mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin.temp");
                                            ren1.renameTo(ren2);
                                            System.out.println("cambio permessi per esecuzione mysqladmin x86");
                                            Process p = Runtime.getRuntime().exec("chmod 777 mysql/bin/mysql-5.1.58-x86-32bit-mysqladmin");
                                            int ret = p.waitFor();
                                            System.out.println("mysqlproc ret:" + ret);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            t.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            db_in_rete = true;
        }

        try {
            login = System.getProperty("user.name") + "@" + SystemUtils.getHostname();
        } catch (Exception err) {
            login = System.getProperty("user.name") + "@???";
        }
        login = DbUtils.aa(login);
        System.out.println("login:" + login);

        //cambio da pref a file
        if (!fileIni.existKey("pref", "noteStandard")) {
            fileIni.setValue("pref", "noteStandard", prefs.get("noteStandard", ""));
            fileIni.setValue("pref", "noteStandardDdt", prefs.get("noteStandardDdt", ""));
            fileIni.setValue("pref", "noteStandardPrev", prefs.get("noteStandardPrev", ""));
            fileIni.setValue("pref", "noteStandardOrdi", prefs.get("noteStandardOrdi", ""));
        }

        //passo tutte le altre prefs in file
        if (!fileIni.existKey("pref", "visualizzaTotali")) {
            fileIni.setValue("pref", "visualizzaTotali", prefs.get("visualizzaTotali", ""));
            fileIni.setValue("pref", "stampaTelefono", prefs.get("stampaCellulare", "false"));
            fileIni.setValue("pref", "stampaCellulare", prefs.get("stampaCellulare", "false"));
            fileIni.setValue("pref", "stampaDestDiversaSotto", prefs.get("stampaDestDiversaSotto", "false"));
            fileIni.setValue("pref", "stampaPdf", prefs.get("stampaPdf", "false"));
            fileIni.setValue("pref", "azioniPericolose", prefs.get("azioniPericolose", ""));
            fileIni.setValue("pref", "limit", prefs.get("limit", ""));
            fileIni.setValue("pref", "soloItaliano", prefs.get("soloItaliano", ""));
            fileIni.setValue("pref", "multiriga", prefs.get("multiriga", "true"));
//            fileIni.setValue("pref", "tipoStampa", prefs.get("tipoStampa", ""));
//            fileIni.setValue("pref", "tipoStampaFA", prefs.get("tipoStampaFA", ""));
//            fileIni.setValue("pref", "tipoStampaDDT", prefs.get("tipoStampaDDT", ""));
//            fileIni.setValue("pref", "tipoStampaOrdine", prefs.get("tipoStampaOrdine", ""));
        }

        if (!fileIni.existKey("pref", "colli_automatici")) {
            fileIni.setValue("pref", "colli_automatici", "0");
        }

        splash("tentativo di connessione al database", true);

        if (this.flagWebStart == false) {
            Db connessione = new Db();
            if (startDbCheck) {
                Db.dbServ = "127.0.0.1:" + dbPortaOk;
                Db.dbPort = dbPortaOk;
                if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                    Db.useNamedPipes = true;
                }
            }
            //faccio test di connessione
            int proveConn = 0;
            boolean connOk = false;
            while (!connOk && proveConn < 2) {
                System.out.println("prova connessione:" + proveConn);
                if (proveConn >= 1) {
                    splash("connessione al database, tentativo " + (proveConn + 1));
                }
                if (connessione.dbConnect(true)) {
                    connOk = true;
                    System.out.println("prova connessione:ok");
                } else {
                    System.out.println("prova connessione:ko aspetto...");
                    try {
                        Thread.sleep(1000);
                        if (startDbCheck) {
                            //tento stop e ritento start
                            stopdb(false);
                            Thread.sleep(1000);
                            startdb();
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ierr) {
                    }
                }
                proveConn++;
            }

            System.out.println("fine test conn db");

//            if (!connessione.dbConnect() == true) {
            if (!connOk) {
                String msg = null;
                if (connessione.last_connection_err_msg != null) {
                    msg = connessione.last_connection_err_msg;
                }
                Throwable e = connessione.last_connection_err;
                JDialogExc de = null;
                if (msg == null) {
                    if (db_in_rete) {
                        msg = "Impossibile collegarsi al database server\n\n";
                    } else {
                        msg = "Impossibile aprire i dati\n\n";
                    }
                    msg = msg + " [Errore:" + StringUtils.abbreviate(e.toString(), 50) + "]";
                    de = new JDialogExc(new JFrame(), true, e);
                    de.labInt.setFont(de.labInt.getFont().deriveFont(Font.BOLD, 14));
                } else {
                    if (msg.indexOf("Communications link failure") >= 0) {
                        if (db_in_rete) {
                            msg = "Impossibile collegarsi al database server\n\n";
                        } else {
                            msg = "Impossibile aprire i dati\n\n";
                        }
                    }
                    if (e.getCause() instanceof UnknownHostException) {
                        de = new JDialogExc(new JFrame(), true, null);
                    } else if (e.getMessage().equalsIgnoreCase("Unable to load class for logger 'it.tnx.invoicex.MysqlLogger'")) {
                        String msg_sotto = "<html>E' possibile che hai il driver mysql-connector installato nella cartella lib/ext di java.";
                        msg_sotto += "<br>Per far funzionare Invoicex devi toglierlo da lib/ext di java</html>";
                        de = new JDialogExc(new JFrame(), true, null, msg_sotto);
                    } else if (e != null && e.getCause() != null && e.getCause().getCause() != null) {
                        de = new JDialogExc(new JFrame(), true, e.getCause().getCause());
                    } else {
                        de = new JDialogExc(new JFrame(), true, e.getCause());
                    }
                    de.labInt.setFont(de.labInt.getFont().deriveFont(Font.BOLD, 16));
                }
                de.labInt.setText(msg);
                de.labe.setFont(de.labInt.getFont().deriveFont(Font.PLAIN, 14));
                de.pack();
                de.setLocationRelativeTo(null);
                de.setVisible(true);

                if (fileIni.getValueBoolean("pref", "richiediPassword", false)) {
                    exitMain(true);
                    return;
                } else if (SwingUtils.showYesNoMessage(null, "Vuoi provare a cambiare le impostazioni per risolvere il problema ?")) {
                    //provo ad impostare parametri
                    JDialogImpostazioni dialog = new JDialogImpostazioni(null, true, true);
                    if (main.wizard_in_corso) {
                        return;
                    } else {
                        //rileggo
                        fileIni.loadFile();
                        fileIni.parseLines();

                        if (fileIni.getValue("db", "startdbcheck") == null || fileIni.getValue("db", "startdbcheck").equalsIgnoreCase("N") || fileIni.getValue("db", "startdbcheck").equalsIgnoreCase("false")) {
                            startDbCheck = false;
                        } else {
                            startDbCheck = true;
                            startConDbCheck = true;
                        }

                        Db.useNamedPipes = false;
                        if (startDbCheck) {
                            Db.dbServ = "127.0.0.1:" + dbPortaOk;
                            if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                                Db.useNamedPipes = true;
                            }
                        } else {
                            Db.dbServ = fileIni.getValue("db", "server").toLowerCase();
                        }

                        Db.dbNameDB = fileIni.getValue("db", "nome_database");
                        Db.dbName = fileIni.getValue("db", "user");
                        //richiedo password
                        if (fileIni.getValueBoolean("pref", "richiediPassword", false)) {
                            String pwd = SwingUtils.showInputPassword(splash, "Invoicex, password di accesso");
                            if (pwd == null) {
                                System.exit(0);
                            }
                            Db.dbPass = pwd;
                        } else {
                            Db.dbPass = fileIni.getValueCifrato("db", "pwd");
                        }

                        if (!connessione.dbConnect() == true) {
                            exitMain();
                            return;
                        }
                    }
                } else {
                    exitMain();
                    return;
                }
            }

            //riparazione tabelle
            try {
                String tempCheck = fileIni.getValue("db", "checkdb");
                if (tempCheck.equalsIgnoreCase("si")) {
                    connessione.dbControllo(false, splash);
                } else {
                    System.out.println("NO CHECK ON START");
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        //test Sync dal db, se presente tabella di registrazione del client ed è registrato è una configurazione con sync attivo
        try {
            Integer sync_client_id = cu.i(dbu.getObject(Db.getConn(), "select client_id from sync_config", false));
            if (sync_client_id != null) {
                System.out.println("è una config con sync: " + sync_client_id);
                //se attivo il sync testo login
                Sync.server = main.fileIni.getValue("sync", "server", "");
                Sync.nomedb = main.fileIni.getValue("sync", "nomedb", "");
                Sync.username = main.fileIni.getValue("sync", "username", "");
                Sync.password = main.fileIni.getValueCifrato("sync", "password", "");
                if (Sync.checkLogin(Sync.server, Sync.nomedb, Sync.username, Sync.password)) {
                    Sync.setActive(true);
                    tnxDbPanel.debug = true;
                    edit_doc_in_temp = true;
                } else {
                    System.out.println("check login sync fallito !");
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Errore nel login al server Sync", true);
                    main.exitMain();
                }
            }
        } catch (Exception e) {
        }

        //apro connessioni hibernate
        //        Configuration cfg = new Configuration()
        //        .setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
        //        .setProperty("hibernate.connection.url", "jdbc:mysql://linux/newoffice")
        //        .setProperty("hibernate.connection.username", "root")
        //        .setProperty("hibernate.connection.password", "***")
        //        .setProperty("hibernate.connection.pool_size", "3")
        //        .setProperty("hibernate.show_sql", "true")
        //        .setProperty("hibernate.format_sql", "true");
        //controllo windows >= vista e installazione in programmi
//            String msg = "Le seguenti tabelle sono danneggiate:\n\n";
//            for (Object t : tablesWithError) {
//                msg += String.valueOf(t) + "\n";
//            }
//            msg += "\nPotrebbero verificarsi problemi nell'uso del programma finchè non vengono riparate.";
//            SwingUtils.showWarningMessage(main.getPadreWindow(), msg);
        //modifiche db
        //aggiungo eventuali aggiornamenti
        //aggiornamento 5
        splash("aggiornamenti struttura database ...");

        //controllo integrità dei dati
        splash("aggiornamenti struttura database ... check");
        if (!db_in_rete) {  //lo facci osolo se su postazione singola
            controllaDati();
        }

        //controllo se esiste la tabella 'db_version' che contiente la versione del db di Invoicex e dei plugin
        //se essite evito di fare i vecch idbchanges
        if (!dbu.existTable(Db.getConn(), "db_version") || !dbu.containRows(Db.getConn(), "select * from db_version where modulo = 'invoicex'")) {

            try {
                DbChanges dbchanges = new DbChanges();
                dbchanges.splash = splash;
                dbchanges.fileIni = fileIni;
                dbchanges.esegui_aggiornamenti();
            } catch (Exception e) {
                e.printStackTrace();
            }

            DbChanges2 dbchanges2 = new DbChanges2() {
                @Override
                public void post_execute_ok(int id_log, String id_plugin, String id_email, String sql) {
                    super.post_execute_ok(id_log, id_plugin, id_email, sql);
                    if (id_log == 143 && id_plugin.equals("") && id_email.equals("m.ceccarelli@tnx.it")) {
                        try {
                            if (!CastUtils.toString(DbUtils.getObject(Db.getConn(), "select logo_in_db from dati_azienda", false)).equalsIgnoreCase("S")) {
                                System.out.println("*** salvo logo in db ***");
                                splash("aggiornamenti struttura database ... salvataggio logo in db", 70);
                                InvoicexUtil.salvaImgInDb(main.fileIni.getValue("varie", "percorso_logo_stampe"));
                                InvoicexUtil.salvaImgInDb(main.fileIni.getValue("varie", "percorso_logo_stampe_pdf"), "logo_email");
                                InvoicexUtil.salvaImgInDb(main.fileIni.getValue("varie", "percorso_sfondo_stampe"), "sfondo");
                                InvoicexUtil.salvaImgInDb(main.fileIni.getValue("varie", "percorso_sfondo_stampe_pdf"), "sfondo_email");
                                DbUtils.tryExecQuery(Db.getConn(), "update dati_azienda set logo_in_db = 'S'");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void post_check_ok(int id_log, String id_plugin, String id_email) {
                    super.post_check_ok(id_log, id_plugin, id_email);
                    if (id_log == 141 && id_plugin.equals("") && id_email.equals("m.ceccarelli@tnx.it")) {
                        try {
                            if (!CastUtils.toString(DbUtils.getObject(Db.getConn(), "select logo_in_db from dati_azienda", false)).equalsIgnoreCase("S")) {
                                DbUtils.tryExecQuery(Db.getConn(), "update dati_azienda set logo_in_db = 'S'");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            dbchanges2.esegui_aggiornamenti();

        } else {
            //se esiste la db_version eseguo il nuovo dbChanges
            //controllo se la db versione ha tutti i campi necessari (bug dbv1.sql)
            try {
                if (!dbu.existColumn(Db.getConn(), "db_version", "running_chi")) {
                    dbu.tryExecQuery(Db.getConn(), "alter table db_version add 	`running_chi` VARCHAR(100) NULL DEFAULT NULL");
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e, "Errore in aggiornamento struttura database\ndb_version, campo mancante running_chi\nContattare l'assistenza");
            }
            try {
                if (!dbu.existColumn(Db.getConn(), "db_version", "running_quando")) {
                    dbu.tryExecQuery(Db.getConn(), "alter table db_version add 	`running_quando` TIMESTAMP NULL DEFAULT NULL");
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e, "Errore in aggiornamento struttura database\ndb_version, campo mancante running_quando\nContattare l'assistenza");
            }            
            try {
                DbVersionChanges dbvc = new DbVersionChanges("invoicex", new RunnableWithArgs() {
                    public void run() {
                        main.splash(cu.s(getArgs()[0]));
                    }
                });
                dbvc.esegui();
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtils.showExceptionMessage(main.getPadreFrame(), e, "Errore in aggiornamento struttura database\nContattare l'assistenza");
                //uscire ?
            }
        }

        try {
            MicroBench mb = new MicroBench(true);
            Sync.inittabpk(Db.getConn());
            mb.out("inittabpk");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //tolgo storico più vecchio di 6 mesi
        try {
            DbUtils.tryExecQuery(Db.getConn(), "delete from storico where data < DATE_ADD(CURDATE(), INTERVAL -12 MONTH)");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            if (InvoicexUtil.getIvaDefault().equals("20") || InvoicexUtil.getIvaSpese().equals("20")) {
//                if (DbUtils.containRows(Db.getConn(), "select codice from articoli where iva = '21'")) {
//                    DbUtils.tryExecQuery(Db.getConn(), "update dati_azienda set codiceIvaDefault = '21'");
//                    DbUtils.tryExecQuery(Db.getConn(), "update dati_azienda set codiceIvaSpese = '21'");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            if (!main.fileIni.getValueBoolean("varie", "correzione_movimenti_dep_arrivo", false)) {
                //controllo se sono stati fatti movimenti da ddt con deposito di arrivo dal 27/6 a oggi
                //se sì visualizzo alert per ricontrollare i DDT interessati

                String sql = "select m.id, t.id, t.serie, t.numero, t.data, t.deposito, t.deposito_arrivo, m.modificato_ts\n"
                        + " from movimenti_magazzino m\n"
                        + " join test_ddt t on m.da_tabella = 'test_ddt' and m.da_id = t.id\n"
                        + " where m.modificato_ts >= '2016-06-27' and m.modificato_ts <= now()"
                        + " and t.deposito_arrivo is not null"
                        + " group by t.id";
                List<Map> list = dbu.getListMap(Db.getConn(), sql);
                if (list.size() > 0) {
                    String msg1 = "";
                    for (Map m : list) {
                        msg1 += "DDT di vendita " + (StringUtils.isBlank(cu.s(m.get("serie"))) ? "" : (cu.s(m.get("serie")) + "/")) + cu.s(m.get("numero")) + " del " + DateUtils.formatDateIta(cu.toDate(m.get("data"))) + "\n";
                    }

                    JDialogBugMovDepArrivo dialog = new JDialogBugMovDepArrivo(main.getPadreFrame(), true);
                    String msg = "Con l'aggiornamento 2016-06-30 abbiamo risolto un bug nella generazione dei movimenti di magazzino sui DDT di vendita con deposito di arrivo inserito.\n"
                            + "Qui sotto puoi trovare l'elenco dei DDT che potrebbero risentire del bug.\n"
                            + "Per correggere il problema dovresti semplicemente aprirli e salvarli nuovamente, in questo caso verranno riscritti i movimenti di magazzino corretti.\n\n";
                    msg += msg1;
                    dialog.text.setText(msg);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                } else {
                    main.fileIni.setValue("varie", "correzione_movimenti_dep_arrivo", true);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!main.fileIni.getValueBoolean("varie", "correzione_stampa_fattura_nosconti", false)) {
                File dir = new File(it.tnx.shell.CurrentDir.getCurrentDir() + "/reports/fatture");
                File[] lista = dir.listFiles();
                for (File f : lista) {
                    if (f.getName().endsWith("_nosconto_gen_invoicex.jasper")) {
                        f.delete();
                    }
                }
                main.fileIni.setValue("varie", "correzione_stampa_fattura_nosconti", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!main.fileIni.getValueBoolean("varie", "nuova_funzione_salva_dimensioni_finestre", false)) {

                //riporto nelle properties nuove le dimensioni di scadenzario e nuova riga
                Properties p = new SortedProperties();
                String nomefile = System.getProperty("user.home") + "/.invoicex/finestre.txt";
                try {
                    if (new File(nomefile).exists()) {
                        p.load(new FileInputStream(nomefile));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Integer n = cu.i0(main.fileIni.getValue("situazione_clienti_fornitori", "frmwidth"));
                if (n > 0) {
                    p.put("it.tnx.invoicex.gui.JInternalFrameScadenzario_width", cu.s(n));
                }
                n = cu.i0(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_w"));
                if (n > 0) {
                    p.put("gestioneFatture.frmNuovRigaDescrizioneMultiRigaNew_width", cu.s(n));
                }
                n = cu.i0(main.fileIni.getValue("dimensioni", "frmNuovRigaDescrizioneMultiRiga_h"));
                if (n > 0) {
                    p.put("gestioneFatture.frmNuovRigaDescrizioneMultiRigaNew_height", cu.s(n));
                }
                try {
                    p.store(new FileOutputStream(nomefile), "");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                main.fileIni.setValue("varie", "nuova_funzione_salva_dimensioni_finestre", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        splash("aggiornamenti struttura database ... ok", 75);

        //if(fileIni.getValueBoolean("gestione_utenti", "attiva", false)){        
        try {
            utenti = CastUtils.toInteger0(DbUtils.getObject(Db.getConn(true), "select gestione_utenti from dati_azienda limit 1"));
        } catch (Exception e) {
            SwingUtils.showErrorMessage(main.getPadreFrame(), e.getMessage(), true);
        }
        if (utenti == 1) {
            splash("Login...", true);
            int tentativi = 0;
            boolean canLogin = false;
            boolean annullaLogin = false;
            String user = null;
            while (tentativi < 3 && !canLogin && !annullaLogin) {
                final JDialogAccesso dialog = new JDialogAccesso(splash, true, tentativi, user);
                dialog.setLocationRelativeTo(null);
                System.out.println("dialog set visible true");
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        dialog.setVisible(true);
                    }
                });
                //dialog.setVisible(true);
                System.out.println("uscito da jdialogaccesso");
                canLogin = dialog.canLogin();
                annullaLogin = dialog.annullaLogin();
                user = dialog.texUsername.getText();
                dialog.dispose();
                tentativi++;
            }

            if (annullaLogin) {
//                SwingUtils.showErrorMessage(splash, "Operazione di login annullata dall'utente", "Impossibile effettuare il login", true);
                main.exitMain();
                return;
            } else if (!canLogin) {
                SwingUtils.showErrorMessage(splash, "Hai esaurito i tentativi disponibili, accesso negato", "Impossibile effettuare il login", true);
                main.exitMain();
                return;
            }
            System.out.println("#############################  UTENTE  ############################## " + System.getProperty("line.separator") + main.utente);
            splash("Login effettuato: " + main.utente.getNomeUtente(), 75);
        } else {
            this.utente = new Utente(1);
        }

        //thread per keep alive connessione        
        Thread tmysqlkeep = new Thread("thread-mysql-keep-alive") {
            List<JInternalFrame> toremove = new ArrayList();

            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            //System.out.println("mysql keep alive " + DbUtils.getObject(Db.getConn(), "select NOW()"));
                            //|hostname|login_so|idutente|username|
                            String key = "|" + SystemUtils.getHostname() + "|" + main.login + "|" + main.utente.getIdUtente() + "|" + main.utente.getNomeUtente() + "|";
                            String sql = "replace accessi_utenti_online set utente_key = " + Db.pcs(key) + ", ts = NOW()";
                            System.out.println("sql = " + sql);
                            System.out.println("mysql keep alive " + DbUtils.tryExecQuery(Db.getConn(true), sql) + " time " + DateUtils.formatDateTimeFromMillis(System.currentTimeMillis()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            //controllo lock documenti                            
                            if (InvoicexUtil.lock_ts_timer != null && InvoicexUtil.lock_ts_timer.size() > 0) {
                                for (JInternalFrame frame : InvoicexUtil.lock_ts_timer.keySet()) {
                                    InvoicexUtil.LockKey key = InvoicexUtil.lock_ts_timer.get(frame);
                                    System.out.println("frame = " + frame + " key:" + key);
                                    if (frame.isClosed()) {
                                        toremove.add(frame);
                                    } else {
                                        String sql = "update locks set ts_timer = NOW() where lock_tabella = " + Db.pcs(key.tab) + " and lock_id = " + key.id;
                                        System.out.println("sql = " + sql);
                                        try {
                                            dbu.tryExecQuery(Db.getConn(), sql);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            //rimuovo dalla map
                            for (JInternalFrame frame : toremove) {
                                InvoicexUtil.lock_ts_timer.remove(frame);
                            }
                            toremove.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000 * tempo_online);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        };
        tmysqlkeep.start();

        //personalizzazioni via groovy scripts
        try {
            String nomefilegroovy = null;
            if (main.attivazione != null && main.attivazione.getDatiAzienda() != null && main.attivazione.getDatiAzienda().getPartita_iva() != null) {
                try {
                    File fdir = new File("./personal_groovy/");
                    if (!fdir.exists()) {
                        fdir.mkdir();
                    }
                    nomefilegroovy = "p" + URLEncoder.encode(main.attivazione.getDatiAzienda().getPartita_iva(), "UTF8") + ".groovy";
                    HttpUtils.saveFile("http://server.invoicex.it/personal/" + nomefilegroovy, "./personal_groovy/" + nomefilegroovy);
                } catch (Exception exget) {
                    if (exget.getMessage().indexOf("404") < 0) {
                        exget.printStackTrace();
                    }
                }
            }

            String dir_groovy = "personal_groovy";
//            if (main.debug) {
//                dir_groovy = "personal_groovy_test";
//            }
            String[] roots = new String[]{"./" + dir_groovy + "/"};
            File dirpersonal = new File(main.wd + dir_groovy + "/");
            if (dirpersonal.exists()) {
                if (dirpersonal.listFiles().length > 0) {
                    GroovyScriptEngine gse = new GroovyScriptEngine(roots);
                    Binding binding = new Binding();
                    binding.setVariable("input_param_prop", paramProp);
                    for (File fgroovy : dirpersonal.listFiles()) {
                        if (fgroovy.getAbsolutePath().endsWith(".groovy")) {
                            System.out.println("INIZIO PERSONAL " + fgroovy + " :");
                            try {
                                gse.run(fgroovy.getName(), binding);
                                System.out.println("OUTPUT PERSONAL: " + binding.getVariable("output"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (!main.debug) {
                                    File fgroovyexecuted = new File(fgroovy.getAbsolutePath() + ".executed");
                                    try {
                                        fgroovyexecuted.delete();
                                    } catch (Exception e) {
                                    }
                                    System.out.println("renameto:" + fgroovyexecuted + " esito:" + fgroovy.renameTo(fgroovyexecuted));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("FINE PERSONAL " + fgroovy);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        splash("apertura menu");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                splash("apertura menu.");
                padre_frame = new MenuFrame();
                padre_panel = padre_frame.getMenuPanel();
                padre_panel.postInit();
                splash("apertura menu..");
                padre_frame.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                padre = new Menu();
            }
        });

        splash("apertura menu...", 85);

        if (!fileIni.existKey("pref", "numerazioneNoteCredito")) {
            //controllo se già presente una nota di credito lascio a false altrimenti metto a true
            String sql = "select * from test_fatt where tipo_fattura = " + dbFattura.TIPO_FATTURA_NOTA_DI_CREDITO + " limit 1";
            ResultSet r = Db.openResultSet(sql);
            try {
                if (r.next()) {
                    fileIni.setValue("pref", "numerazioneNoteCredito", false);
                } else {
                    fileIni.setValue("pref", "numerazioneNoteCredito", true);
                }
            } catch (Exception ex) {
                fileIni.setValue("pref", "numerazioneNoteCredito", true);
                ex.printStackTrace();
            }
        }

        try {
            disp_articoli_da_deposito = cu.i(dbu.getObject(gestioneFatture.Db.getConn(), "select disp_articoli_da_deposito from dati_azienda", false));
        } catch (Exception e) {
        }

//        if (!fileIni.existKey("varie", "percorso_logo_stampe_pdf")) {
//            fileIni.setValue("varie", "percorso_logo_stampe_pdf", fileIni.getValue("varie", "percorso_logo_stampe"));
//            iniPercorsoLogoStampePdf = fileIni.getValue("varie", "percorso_logo_stampe");
//            fileIni.saveFile();
//        }
        splash("controllo tabelle temporanee", 90);

        //test meta data
        try {
            ResultSet rtest = dbu.tryOpenResultSet(Db.getConn(), "select banca_abi from clie_forn limit 0");
            ResultSetMetaData rmtest = rtest.getMetaData();
            int size = rmtest.getColumnDisplaySize(dbu.getColumnIndex(rmtest, "banca_abi"));
            if (size != 5) {
                SwingUtils.showErrorMessage(padre, "Problema nel controllo della struttura delle tabelle.\nErrata dimensione dei campi.\nContattare l'assistenza");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //---- fine modifiche -----
        if (!Sync.isActive()) {

            //creo righ_ddt_temp se e' cambiata la struttura
            if (db_in_rete && (Db.checkTableStructure("righ_ddt", "righ_ddt_temp", true) == false
                    || Db.checkTableStructure("righ_ddt_acquisto", "righ_ddt_acquisto_temp", true) == false
                    || Db.checkTableStructure("righ_fatt", "righ_fatt_temp", true) == false
                    || Db.checkTableStructure("righ_fatt_acquisto", "righ_fatt_acquisto_temp", true) == false
                    || Db.checkTableStructure("righ_ordi", "righ_ordi_temp", true) == false
                    || Db.checkTableStructure("righ_ordi_acquisto", "righ_ordi_acquisto_temp", true) == false)) {

                if (main.db_in_rete) {
                    SwingUtils.showInfoMessage(padre, "Attenzione, è necessario aggiornare la struttura del database.\nSe altri utenti stanno utilizzando Invoicex\ndovrebbero uscire prima di chiudere questo avviso !");
                }
            }

            if (Db.checkTableStructure("righ_ddt", "righ_ddt_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_ddt_temp", false, true);
                Db.duplicateTableStructure("righ_ddt", "righ_ddt_temp", true);
            }
            if (Db.checkTableStructure("righ_ddt_acquisto", "righ_ddt_acquisto_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_ddt_acquisto_temp", false, true);
                Db.duplicateTableStructure("righ_ddt_acquisto", "righ_ddt_acquisto_temp", true);
            }

            //creo righ_fatt_temp
            if (Db.checkTableStructure("righ_fatt", "righ_fatt_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_fatt_temp", false, true);
                Db.duplicateTableStructure("righ_fatt", "righ_fatt_temp", true);
            }

            //creo righ_fatt_temp
            if (Db.checkTableStructure("righ_fatt_acquisto", "righ_fatt_acquisto_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_fatt_acquisto_temp", false, true);
                Db.duplicateTableStructure("righ_fatt_acquisto", "righ_fatt_acquisto_temp", true);
            }

            //creo righ_ordi_temp
            if (Db.checkTableStructure("righ_ordi", "righ_ordi_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_ordi_temp", false, true);
                Db.duplicateTableStructure("righ_ordi", "righ_ordi_temp", true);
            }

            //creo righ_ordi_temp
            if (Db.checkTableStructure("righ_ordi_acquisto", "righ_ordi_acquisto_temp", true) == false) {
                dbu.tryExecQuery(Db.getConn(), "DROP TABLE IF EXISTS righ_ordi_acquisto_temp", false, true);
                Db.duplicateTableStructure("righ_ordi_acquisto", "righ_ordi_acquisto_temp", true);
            }
        } else {
            try {
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_ordi_temp", false, true);
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_ordi_acquisto_temp", false, true);
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_ddt_temp", false, true);
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_ddt_acquisto_temp", false, true);
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_fatt_temp", false, true);
                dbu.tryExecQuery(Db.getConn(), "drop table if exists righ_fatt_acquisto_temp", false, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //annoto versioni clients
        try {
            String host = SystemUtils.getHostname();
            String sql = "delete from versioni_clients where hostname = " + Db.pcs(host);
            DbUtils.tryExecQuery(Db.getConn(), sql);
            sql = "insert into versioni_clients set hostname = " + Db.pcs(host) + ", versione = " + Db.pcs(main.version.toString() + " " + main.build) + ", pacchetto = " + Db.pcs(main.versione);
            System.out.println("sql = " + sql);
            DbUtils.tryExecQuery(Db.getConn(), sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        splash("ok", 100);

        padre_frame.setVisible(true);
        padre_panel.checkPanBarr2();
        padre_frame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        padre_panel.lblInfoLoading2.setText("caricamento ...");

        try {
            if (Main.javafxver != null) {
                try {
                    Class clmainjfx = Class.forName("it.tnx.invoicex.MainJFX");
                    clmainjfx.newInstance();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
        }

        if (splash != null) {
            splash.dispose();
        }
        splash = null;

        //imposto icona sync
        if (Sync.isActive()) {
            padre_panel.addSyncIcon();
        }

        //controllo se ha inserito i dati azienda
        String sql = "select ragione_sociale, flag_dati_inseriti from dati_azienda";
        ResultSet r = Db.openResultSet(sql);
        try {
            if (r.next()) {
                if (r.getObject("flag_dati_inseriti") == null || !r.getString("flag_dati_inseriti").equalsIgnoreCase("S")) {
                    JDialogDatiAzienda datiAzienda = new JDialogDatiAzienda(main.getPadre(), true);
                    datiAzienda.setLocationRelativeTo(null);
                    datiAzienda.setVisible(true);
                } else {
                    attivazione.setDatiAziendaInseriti(true);
                }
            } else {
                JOptionPane.showMessageDialog(padre_frame, "Errore nel controllo della tabella dati_azienda, nessun record", "Errore", JOptionPane.ERROR_MESSAGE);
                DbUtils.tryExecQuery(Db.getConn(), "insert into dati_azienda set id = 1, ragione_sociale = ''");
            }
        } catch (SQLException sqlerr1) {
            JOptionPane.showMessageDialog(padre_frame, "Errore nel controllo della tabella dati_azienda, " + sqlerr1, "Errore", JOptionPane.ERROR_MESSAGE);
        }

        //imposto nuovo report ddt mod3 se mod2 o default
        if (fileIni.getValue("pref", "tipoStampaDDT", "ddt_default.jrxml").equals("ddt_default.jrxml") || fileIni.getValue("pref", "tipoStampaDDT", "ddt_default.jrxml").equals("ddt_mod2_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaDDT", "ddt_mod3_default.jrxml");
        }
        if (new File(main.wd + "reports/fatture/fattura_mod3_default.jrxml").exists() && (fileIni.getValue("pref", "tipoStampa", "fattura_default.jrxml").equals("fattura_default.jrxml") || fileIni.getValue("pref", "tipoStampa", "fattura_mod2_default.jrxml").equals("fattura_mod2_default.jrxml"))) {
            System.out.println("file exist mod3: " + new File(main.wd + "reports/fatture/fattura_mod3_default.jrxml").exists() + " && (" + fileIni.getValue("pref", "tipoStampa", "fattura_default.jrxml").equals("fattura_default.jrxml") + " || " + fileIni.getValue("pref", "tipoStampa", "fattura_mod2_default.jrxml").equals("fattura_mod2_default.jrxml") + ")");
            fileIni.setValue("pref", "tipoStampa", "fattura_mod3_default.jrxml");
        }

        //imposto nuovo report ordine mod2 se default
        if (!fileIni.existKey("pref", "flag_impostato_nuovo_report_ordine_mod2")) {
            fileIni.setValue("pref", "flag_impostato_nuovo_report_ordine_mod2", "N");
            if (fileIni.getValue("pref", "tipoStampaOrdine", "ddt_default.jrxml").equals("ordine_default.jrxml")) {
                if (new File(main.wd + "reports/fatture/ordine_mod2_default.jrxml").exists()) {
                    fileIni.setValue("pref", "tipoStampaOrdine", "ordine_mod2_default.jrxml");
                    fileIni.setValue("pref", "flag_impostato_nuovo_report_ordine_mod2", "S");
                }
            }
        }

        //imposto fattura_mod4 se non già impostato ed era mod3
        if (!fileIni.existKey("pref", "flag_fattura_mod4")
                && fileIni.getValue("pref", "tipoStampa", "fattura_mod3_default.jrxml").equals("fattura_mod3_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampa", "fattura_mod4_default.jrxml");
            fileIni.setValue("pref", "flag_fattura_mod4", "s");
        }
        if (!fileIni.existKey("pref", "flag_fattura_acc_mod4")
                && fileIni.getValue("pref", "tipoStampaFA", "fattura_acc_mod2_default.jrxml").equals("fattura_acc_mod2_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaFA", "fattura_acc_mod4_default.jrxml");
            fileIni.setValue("pref", "flag_fattura_acc_mod4", "s");
        }
        if (!fileIni.existKey("pref", "flag_ddt_mod4")
                && fileIni.getValue("pref", "tipoStampaDDT", "ddt_mod3_default.jrxml").equals("ddt_mod3_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaDDT", "ddt_mod4_default.jrxml");
            fileIni.setValue("pref", "flag_ddt_mod4", "s");
        }

        if (!fileIni.existKey("pref", "flag_ordine_mod4")
                && fileIni.getValue("pref", "tipoStampaOrdine", "ordine_mod2_default.jrxml").equals("ordine_mod2_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaOrdine", "ordine_mod4_default.jrxml");
            fileIni.setValue("pref", "flag_ordine_mod4", "s");
        }

        //imposto _mod5 se non già impostato ed era mod4
        if (!fileIni.existKey("pref", "flag_ddt_mod5")
                && fileIni.getValue("pref", "tipoStampaDDT", "ddt_mod4_default.jrxml").equals("ddt_mod4_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaDDT", "ddt_mod5_default.jrxml");
            fileIni.setValue("pref", "flag_ddt_mod5", "s");
        }

        //imposto _mod5 se non già impostato ed era mod4
        if (!fileIni.existKey("pref", "flag_mod6") && fileIni.getValue("pref", "tipoStampa", "fattura_mod6_default.jrxml").equals("fattura_mod5_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampa", "fattura_mod6_default.jrxml");
        }
        if (!fileIni.existKey("pref", "flag_mod6") && fileIni.getValue("pref", "tipoStampaFA", "fattura_acc_mod6_default.jrxml").equals("fattura_acc_mod5_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaFA", "fattura_acc_mod6_default.jrxml");
        }
        if (!fileIni.existKey("pref", "flag_mod6") && fileIni.getValue("pref", "tipoStampaOrdine", "ordine_mod6_default.jrxml").equals("ordine_mod5_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaOrdine", "ordine_mod6_default.jrxml");
        }
        if (!fileIni.existKey("pref", "flag_mod6") && fileIni.getValue("pref", "tipoStampaDDT", "ddt_mod6_default.jrxml").equals("ddt_mod5_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaDDT", "ddt_mod6_default.jrxml");
        }
        fileIni.setValue("pref", "flag_mod6", "s");

        //imposto _mod7 se non già impostato ed era mod5
        if (!fileIni.existKey("pref", "flag_mod7") && fileIni.getValue("pref", "tipoStampa", "fattura_mod7_default.jrxml").equals("fattura_mod6_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampa", "fattura_mod7_default.jrxml");
        }
        if (!fileIni.existKey("pref", "flag_mod7") && fileIni.getValue("pref", "tipoStampaFA", "fattura_acc_mod7_default.jrxml").equals("fattura_acc_mod6_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaFA", "fattura_acc_mod7_default.jrxml");
        }
        if (!fileIni.existKey("pref", "flag_mod7") && fileIni.getValue("pref", "tipoStampaOrdine", "ordine_mod7_default.jrxml").equals("ordine_mod6_default.jrxml")) {
            fileIni.setValue("pref", "tipoStampaOrdine", "ordine_mod7_default.jrxml");
        }
        fileIni.setValue("pref", "flag_mod7", "s");

        if (!fileIni.existKey("pref", "flag_stato_ordine_bug1")) {
            if (fileIni.getValue("pref", "stato_ordi", "").equals("Preventivo")) {
                main.fileIni.removeKey("pref", "stato_ordi");
            }
            fileIni.setValue("pref", "flag_stato_ordine_bug1", "s");
        }

        //imposto di caricare le immagini da file e non da db (da db solo per integrazione client manager)
        if (!fileIni.existKey("pref", "flag_immagini_da_db_per_client_manager")) {
            fileIni.setValue("pref", "flag_immagini_da_db_per_client_manager", "N");
        }

        if (!fileIni.existKey("varie", "int_dest_1") || StringUtils.isBlank(fileIni.getValue("varie", "int_dest_1"))) {
            fileIni.setValue("varie", "int_dest_1", int_dest_1_default);
        }
        if (!fileIni.existKey("varie", "int_dest_2") || StringUtils.isBlank(fileIni.getValue("varie", "int_dest_2"))) {
            fileIni.setValue("varie", "int_dest_2", int_dest_2_default);
        }

        if (!fileIni.existKey("pref", "flag_note_per_documento")) {
            fileIni.setValue("pref", "flag_note_per_documento", "s");
            fileIni.setValue("pref", "noteStandardDdt", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardOrdi", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardPrev", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardFattAcquisto", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardDdtAcquisto", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardOrdiAcquisto", fileIni.getValue("pref", "noteStandard"));
            fileIni.setValue("pref", "noteStandardPrevAcquisto", fileIni.getValue("pref", "noteStandard"));
        }

        //nuovo controllo registrazione
        if ((!attivazione.isFlagDatiInviatiPrimaVolta() && attivazione.isDatiAziendaInseriti()) || (!attivazione.isFlagDatiInviatiSuModifica() && attivazione.isFlagDatiModificati())) {
            //chiedo se vuol inviare i dati a tnx
//            if (JOptionPane.showConfirmDialog(getPadreWindow(), "Acconsenti all'invio della tua Ragione Sociale, Partita IVA ed il resto dei tuoi dati anagrafici aziendali a TNX snc ?\nI dati verranno utilizzati soltanto a scopo statistico o per attivare funzionalità aggiuntive,\nse non vengono inviati puoi comunque continuare ad usare il programma", "Registrazione programma", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION) {
            Boolean registraini = main.fileIni.getValueBoolean("varie", "dati_azienda_registra_invoicex", true);
            if (registraini) {
                System.out.println("attivazione: invio dati...");
                boolean ret = attivazione.registra();
                if (ret) {
                    main.attivazione.setDatiAziendaInseriti(true);
                    main.attivazione.setFlagDatiInviatiPrimaVolta(true);
                    if (attivazione.isFlagDatiModificati()) {
                        main.attivazione.setFlagDatiModificati(false);
                        main.attivazione.setFlagDatiInviatiSuModifica(true);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
//                            JOptionPane.showMessageDialog(main.this.getPadre(), "Invio completato.\nGrazie", "Informazioni", JOptionPane.INFORMATION_MESSAGE);
                            main.getPadre().aggiornaTitle();
                        }
                    });
                }
                System.out.println("attivazione: registrazione " + ret);
                System.out.println("attvazione: invio dati...finito");
            }
        }

        main.getPadrePanel().lblInfoLoading2.setVisible(true);
        main.getPadrePanel().lblInfoLoading2.setText("Controllo personalizzazioni ...");
        Thread tpersonal = new Thread("tpersonal") {
            @Override
            public void run() {
                System.out.println("tpersonal: Controllo personalizzazioni ...");
                try {
                    String vl = version + " (" + build + ")";
                    String url = main.baseurlserver + "/p.php?v=" + URLEncoder.encode(vl) + (attivazione.getIdRegistrazione() != null ? "&i=" + attivazione.getIdRegistrazione() : "");
                    String lista = getURL(url);
                    String[] files = StringUtils.split(lista, "\n");
                    DebugUtils.dump(files);
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            String urlfile = main.baseurlserver + "/personal/" + files[i];
                            String filelocale = files[i];
                            File filelocale_test = new File(main.wd + filelocale);
                            long last_modified_locale = filelocale_test.lastModified();
                            long last_modified_server = 0;
                            try {
                                last_modified_server = HttpUtils.getLastModified(urlfile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("tpersonal: download di:" + files[i] + " da:" + urlfile + " in:" + filelocale + " last_modified_locale:" + last_modified_locale + " last_modified_server:" + last_modified_server + " test s>l:" + (last_modified_server > last_modified_locale));
                            if (filelocale_test.exists() && last_modified_server > last_modified_locale) {
                                System.out.println("tpersonal: tento di eliminare il file che esiste gia':" + filelocale);
                                filelocale_test.delete();
                                System.out.println("tpersonal: tento di eliminare il file che esiste gia':" + filelocale + " ... eliminato!");
                            }
                            if (last_modified_server > last_modified_locale) {
                                try {
                                    HttpUtils.saveFile(urlfile, filelocale);
                                    //controllo se scarica un report glielo setto
                                    if (files[i].toLowerCase().startsWith("reports/fatture/")) {
                                        String nome_report = filelocale_test.getName();
                                        String tipo = "";
                                        if (nome_report.startsWith("fattura")) {
                                            if (!nome_report.startsWith("fattura_acc") && !nome_report.startsWith("fattura_acquisto")) {
                                                tipo = "tipoStampa";
                                            }
                                        } else if (nome_report.startsWith("fattura_acc")) {
                                            tipo = "tipoStampaFA";
                                        } else if (nome_report.startsWith("ddt")) {
                                            tipo = "tipoStampaDDT";
                                        } else if (nome_report.startsWith("ordine")) {
                                            tipo = "tipoStampaOrdine";
                                        }
                                        System.out.println("tpersonal: tento di impostre come report per:" + tipo + " il report:" + nome_report);
                                        fileIni.setValue("pref", tipo, nome_report);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        tpersonal.start();

        //init plugins - check aggiornamenti
//        menu1.lblInfoLoading2.setBounds( 5, menu1.getHeight() - 150, menu1.lblInfoLoading2.getWidth(), menu1.lblInfoLoading2.getHeight());
        main.getPadrePanel().lblInfoLoading2.setVisible(true);
        main.getPadrePanel().lblInfoLoading2.setText("Controllo aggiornamenti Plugins ...");

        //controllo se rimuovere dei plugin perchè richiesti dall'avvio precedente
        try {
            File fdirplugins = new File(main.wd + plugins_path);
            File[] files = fdirplugins.listFiles();
            for (File fplugin : files) {
                String key = "remove_" + fplugin.getName().toLowerCase();
                System.out.println("key = " + key);
                if (fileIni.existKey("plugins", key)) {
                    System.out.println("plugins: delete:" + fplugin + " esito:" + fplugin.delete());
                    fileIni.removeKey("plugins", key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File fdirplugins = new File(main.wd + plugins_path);
            fdirplugins.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (attivazione.getIdRegistrazione() != null) {
            pf = new PluginFactory();
            pf.loadPlugins(plugins_path);
            Collection plugcol = pf.getAllEntryDescriptor();
            if (plugcol != null) {
                Iterator plugiter = plugcol.iterator();
                //controllo aggiornamento dei plugins..
                List<Plugin> plugins_online = null;
                try {
//                    String url = it.tnx.invoicex.Main.url_server;
//                    HessianProxyFactory factory = new HessianProxyFactory();
//                    Hservices service = (Hservices) factory.create(Hservices.class, url);
//                    plugins_online = service.getPlugins3(false, true);
                    plugins_online = InvoicexUtil.getPlugins(false, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (plugins_online != null) {
                    while (plugiter.hasNext()) {
                        EntryDescriptor pd = (EntryDescriptor) plugiter.next();

                        //                    pluginPresenti.add(pd.getName());
                        //                    plugins.put(pd.getName(), pd);
                        //controllo aggiornamenti
                        try {
                            boolean nontrovato = true;
                            for (Plugin po : plugins_online) {
                                if (po.getNome_breve().equalsIgnoreCase(pd.getName())) {
                                    nontrovato = false;
                                    System.out.println("controllo aggiornamenti per:" + pd.getName() + " o:" + po.getNome_breve() + " jar:" + pd.getNomeFileJar());

//                                    test
//                                    scaricaAggiornamentoPlugin(po, pd);
                                    if (pd.getData() == null && !po.getVersione().equals("1.0")) {
                                        //aggiornarecontrollo aggiornamenti per:
                                        System.out.println("richiedo agg: " + pd.getData() + " ver:" + po.getVersione());
                                        scaricaAggiornamentoPlugin(po, pd);
                                    } else if (pd.getData() != null && pd.getData().before(po.getData_ultima_modifica())) {
                                        //aggiornare    
                                        System.out.println("richiedo agg: " + pd.getData() + " " + po.getData_ultima_modifica() + " pd.getData() before po.getData():" + pd.getData().before(po.getData_ultima_modifica()));
                                        scaricaAggiornamentoPlugin(po, pd);
                                    } else if (pd.getData() != null && pd.getData().equals(po.getData_ultima_modifica()) && CastUtils.toDouble0Eng(pd.getVer()) < CastUtils.toDouble0Eng(po.getVersione())) {
                                        //aggiornare
                                        System.out.println("richiedo agg: " + pd.getData() + " " + po.getData_ultima_modifica() + " pd.getData().equals(po.getData_ultima_modifica()):" + pd.getData().equals(po.getData_ultima_modifica()) + " pd ver:" + pd.getVer() + " po ver:" + po.getVersione());
                                        scaricaAggiornamentoPlugin(po, pd);
                                    }

                                    break;
                                }
                            }
                            if (nontrovato) {
                                System.err.println("!!!!!!!!!!!!!!!! plugin " + pd.getName() + " non trovato online !!!!!!!!!!!!!!!!!!!!");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        Thread tscaduto = new Thread("scaduto") {
            public void run() {
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                }
                System.out.println("controllo plugins: in attesa del caricamento dei plugins");
                long max = System.currentTimeMillis() + (1000 * 30);
                while (main.fine_init_plugin == false) {
                    try {
                        Thread.sleep(5000);
                        if (System.currentTimeMillis() > max) {
                            System.out.println("controllo plugins: timeout controllaPluginPerVersione");
                            return;
                        }
                        System.out.println("controllo plugins: in attesa del caricamento dei plugins...");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                System.out.println("controllo plugins: caricamento de plugin completato, attendo 5 secondi");
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }

                //controllo plugins disattivati
                if (fileIni.getValueBoolean("pref", "msg_plugins_riattiva2", true)) {
                    String[] pluginscheck = {"pluginRicerca", "pluginAutoUpdate", "pluginBackupTnx", "pluginRitenute", "pluginEmail", "pluginBarCode", "pluginRiba", "pluginCompry", "pluginContabilita"};
                    ArrayList pluginsdisat = new ArrayList();
                    String pluginsdisatm = "";
                    for (String s : pluginscheck) {
                        if (pluginPresenti.contains(s) && !pluginAttivi.contains(s)) {
                            pluginsdisat.add(s);
                            pluginsdisatm += s + "/";
                        }
                    }

////**************************
////test
//String s = "pluginRitenute";
//pluginsdisat.add(s);
//pluginsdisatm += s + "/";
////**************************
                    if (pluginsdisat.size() > 0) {
                        pluginsdisatm = StringUtils.chop(pluginsdisatm);
                        final String pluginsdisatm_f = pluginsdisatm;

                        String msg = "<html>";
                        String versioneu = main.fileIni.getValue("cache", "versioneu", "Base");
////**************************
////test
////versioneu = "Base";
//versioneu = "Professional Plus";
////**************************

                        boolean motivo_dati_non_corrispondenti = false;
                        boolean motivo_non_attivato = false;
                        boolean motivo_disattivato = false;

                        System.out.println("debug check attivazione");
                        System.out.println("versioneu:[" + versioneu + "]");
                        System.out.println("pluginsdisatm_f:[" + pluginsdisatm_f + "]");
                        System.out.println("pluginsdisatm_f.indexOf(\"pluginRitenute\"):[" + pluginsdisatm_f.indexOf("pluginRitenute") + "]");
                        System.out.println("pluginAttivi:[" + pluginAttivi + "]");
                        System.out.println("pluginAttivi.contains(\"pluginAutoUpdate\"):[" + pluginAttivi.contains("pluginAutoUpdate") + "]");

                        if (!versioneu.equalsIgnoreCase("Base")) {
                            for (String k : pluginErroriAttivazioni.keySet()) {
                                Map m = pluginErroriAttivazioni.get(k);
                                if (m.get("motivo_dati_non_corrispondenti") != null) {
                                    motivo_dati_non_corrispondenti = true;
                                }
                                if (m.get("motivo_non_attivato") != null) {
                                    motivo_non_attivato = true;
                                }
                                if (m.get("motivo_disattivato") != null) {
                                    motivo_disattivato = true;
                                    break;
                                }
                            }

                            //controllo se ha la professional ed ha il plugin autoupdate attivo e ritenuto disattivo
//                                    if (versioneu.equalsIgnoreCase("Professional") && 
//                                            (pluginsdisatm_f.indexOf("pluginRitenute") >= 0)
//                                            && pluginAttivi.contains("pluginAutoUpdate")) {
                            if (!motivo_disattivato && versioneu.equalsIgnoreCase("Professional") && (pluginsdisatm_f.indexOf("pluginRiba") >= 0
                                    || pluginsdisatm_f.indexOf("pluginContabilita") >= 0
                                    || pluginsdisatm_f.indexOf("pluginBarCode") >= 0
                                    || pluginsdisatm_f.indexOf("pluginRitenute") >= 0)) {
                                msg += "<font size='5' color='red'>Alcune funzioni disattivate:</font><br><br>";
                                if (pluginsdisatm_f.indexOf("pluginRiba") >= 0) {
                                    msg += " - Generazione file RiBa per banca (pluginRiba)<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginContabilita") >= 0) {
                                    msg += " - Contabilità (pluginContabilita)<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginBarCode") >= 0) {
                                    msg += " - Stampa dei codici a barre (pluginBarCode)<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginRitenute") >= 0) {
                                    msg += " - Fatture con ritenute d'acconto e rivalsa (pluginRitenute)<br>";
                                }
                                msg += "<br>";
                                msg += "<br>La tua versione " + main.versione + " non comprende le funzionalità di cui sopra.";
                                msg += "<br>Se lo desideri puoi passare alla versione Professional Plus o Enterprise.";
                                msg += "<br>Oppure puoi cliccare su Plugins e fare doppio click sui plugins<br> non più attivi per toglierli definitivamente";
                            } else {
                                for (String k : pluginErroriAttivazioni.keySet()) {
                                    Map m = pluginErroriAttivazioni.get(k);
                                    if (m.get("motivo_dati_non_corrispondenti") != null) {
                                        motivo_dati_non_corrispondenti = true;
                                    }
                                    if (m.get("motivo_non_attivato") != null) {
                                        motivo_non_attivato = true;
                                    }
                                    if (m.get("motivo_disattivato") != null) {
                                        motivo_disattivato = true;
                                        break;
                                    }
                                }

////**************************
////test
//motivo_dati_non_corrispondenti = false;
//motivo_non_attivato = true;
//motivo_disattivato = true;
////**************************
                                msg += "<b>";
                                if (motivo_disattivato) {
                                    msg += "<font size='5' color='red'>Il tuo Invoicex " + main.versione + " è scaduto !</font><br><br>";
                                } else if (motivo_dati_non_corrispondenti) {
                                    msg += "<font size='3' color='red'>Il tuo Invoicex " + main.versione + " non è registrato correttamente !</font><br><br>";
                                } else if (motivo_non_attivato) {
                                    msg += "<font size='3' color='red'>Il tuo Invoicex " + main.versione + " non è stato attivato !</font><br><br>";
                                } else {
                                }
                                msg += "</b>";
                                msg += "Senza riattivare Invoicex:<br>";
                                if (pluginsdisatm_f.indexOf("pluginAutoUpdate") >= 0) {
                                    msg += " - non si aggiornerà in automatico<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginBackupTnx") >= 0) {
                                    msg += " - non ti permetterà di eseguire i backup online sul nostro server<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginRicerca") >= 0) {
                                    msg += " - non potrai utilizzare la ricerca globale che si apriva all'avvio<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginRiba") >= 0) {
                                    msg += " - non potrai generare il file Riba da inviare tramite home banking<br>";
                                }
                                if (pluginsdisatm_f.indexOf("pluginRitenute") >= 0) {
                                    msg += " - non potrai gestire le ritenute e la rivalsa inps<br>";
                                }
                                msg += " - non sei più coperto dalla nostra Assistenza.";
                                if (motivo_disattivato) {
                                    msg += "<br><br><b>Motivo: il programma non è stato rinnovato alla sua scadenza.</b>";
                                } else if (motivo_dati_non_corrispondenti) {
                                    msg += "<br><br><b>Motivo: I dati inseriti in Anagrafiche -> Anagrafica Azienda non corrispondono con i dati di registrazione.</b>";
                                    msg += "<br>Vai in Anagrafiche -> Anagrafica Azienda, conferma i dati ed alla domanda 'Acconsenti all'invio...' rispondi<br> con 'Sì', attendi la registrazione e riavvia il programma";
                                } else if (motivo_non_attivato) {
                                    msg += "<br><br><b>Motivo: Il programma non è stato attivato.</b>";
                                    msg += "<br>Per l'attivazione clicca sul pulsante 'Attivazione' e segui le istruzioni";
                                }
                            }
                        } else {
                            msg += "<font size='4' color='red'><b>I plugins in prova sono scaduti !</b></font><br><br>";
                            msg += "Per riattivare tutte le funzionalità aggiuntive acquista Invoicex e procedi<br>";
                            msg += "all'attivazione con il codice che ti invieremo<br><br>";
                            msg += "Se hai già il codice clicca sul pulsante 'Attivazione' !<br>";
                        }

                        System.out.println("pluginErroriAttivazioni:" + pluginErroriAttivazioni);

                        String dettagli = "";
                        for (String k : pluginErroriAttivazioni.keySet()) {
                            System.out.println("k = " + k);
                            Map m = pluginErroriAttivazioni.get(k);
                            if (motivo_disattivato) {
                                if (m.get("motivo_msg") != null && m.get("motivo_msg").toString().indexOf("DISATTIVATO") >= 0) {
                                    dettagli += m.get("motivo_msg") + "<br>";
                                    break;
                                }
                            } else if (motivo_dati_non_corrispondenti) {
                                dettagli += m.get("motivo_msg") + "<br>";
                                DebugFastUtils.hexDump(dettagli);
                                break;
                            } else if (m.get("motivo_msg") != null) {
                                dettagli += k + ":" + m.get("motivo_msg") + "<br>";
                            }
                        }
                        final String dettaglif = dettagli;
                        final String msgf = msg;
                        SwingUtils.inEdt(new Runnable() {
                            public void run() {
                                padre.setIconImage(main.getLogoIcon());
                                final JDialogRiattivaPlugins rp = new JDialogRiattivaPlugins(padre, true);
                                String dettagli2 = dettaglif;
                                if (dettagli2.length() > 0) {
                                    if (dettagli2.length() > 200) {
                                        dettagli2 = dettagli2.substring(0, 200) + "...";
                                    }
                                    dettagli2 = StringUtils.replace(dettagli2, "\n", "<br>");
                                    rp.dettagli.setText("<html>Dettagli:<br>" + dettagli2 + "</html>");
                                }
                                String msg2 = msgf;
                                msg2 += "</html>";
                                rp.jLabel1.setText(msg2);

                                rp.validate();
                                rp.setLocationRelativeTo(null);
                                rp.pack();
                                rp.setVisible(true);
                            }
                        });
                    }
                }
            }
        };

        //init plugins
//        menu1.lblInfoLoading2.setBounds( 5, menu1.getHeight() - 150, menu1.lblInfoLoading2.getWidth(), menu1.lblInfoLoading2.getHeight());
        main.getPadrePanel().lblInfoLoading2.setVisible(true);
        main.getPadrePanel().lblInfoLoading2.setText("Caricamento Plugins ...");
        
        try {
            File fdirplugins = new File(main.wd + plugins_path);
            fdirplugins.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //controllo il plugin generale
        pf = new PluginFactory();
        pf.loadPlugins(plugins_path);
        boolean pi = false;
        Collection plugcol = pf.getAllEntryDescriptor();
        if (plugcol != null) {
            Iterator plugiter = plugcol.iterator();
            while (plugiter.hasNext()) {
                EntryDescriptor pd = (EntryDescriptor) plugiter.next();
                if (pd.getName().equalsIgnoreCase("pluginInvoicex")) {
                    pi = true;
                }
            }
        }
        if (!pi) {
            try {
                HttpUtils.saveBigFile(main.baseurlserver + "/" + InvoicexUtil.getDownloadDir() + "/plugins/InvoicexPluginInvoicex.jar", "plugins/InvoicexPluginInvoicex.jar");
            } catch (Exception e) {
                e.printStackTrace();
            }
            pf.loadPlugins(plugins_path);
            plugcol = pf.getAllEntryDescriptor();
        }
        if (plugcol != null) {
            Iterator plugiter = plugcol.iterator();
            while (plugiter.hasNext()) {
                EntryDescriptor pd = (EntryDescriptor) plugiter.next();
                if (pd.getName().equalsIgnoreCase("pluginInvoicex")) {
                    pi = true;
                    pluginPresenti.add(pd.getName());
                    plugins.put(pd.getName(), pd);
                    try {
                        PluginEntry pl = (PluginEntry) pf.getPluginEntry(pd.getId());
                        pl.initPluginEntry(null);
                        pl.startPluginEntry();
                        pluginsAvviati.put(pd.getName(), pl);
                    } catch (NoSuchFieldError nofield) {
                        nofield.printStackTrace();
                    } catch (NoClassDefFoundError noclass) {
                        noclass.printStackTrace();
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }
                    break;
                }
            }
        }

        if (!s1) {
            if (attivazione.getIdRegistrazione() != null) {
                pf = new PluginFactory();
                pf.loadPlugins(plugins_path);
                plugcol = pf.getAllEntryDescriptor();
                if (plugcol != null) {
                    Iterator plugiter = plugcol.iterator();
                    while (plugiter.hasNext()) {
                        EntryDescriptor pd = (EntryDescriptor) plugiter.next();
                        if (!pd.getName().equalsIgnoreCase("pluginInvoicex")) {
                            pluginPresenti.add(pd.getName());
                            plugins.put(pd.getName(), pd);

                            if (attivazione.getIdRegistrazione() != null) {
                                //avvio il plugin
                                try {
                                    MicroBench mb = new MicroBench();
                                    mb.start();
                                    main.getPadrePanel().lblInfoLoading2.setText("Caricamento " + pd.getName() + "...");
                                    PluginEntry pl = (PluginEntry) pf.getPluginEntry(pd.getId());
                                    pl.initPluginEntry(null);
                                    System.out.println(pd.getName() + " Init -> tempo: " + mb.getDiff("init"));
                                    pl.startPluginEntry();
                                    pluginsAvviati.put(pd.getName(), pl);
                                    main.getPadrePanel().lblInfoLoading2.setText(pd.getName() + " Caricato");
                                    System.out.println(pd.getName() + " Caricato -> tempo: " + mb.getDiff("caricamento"));
                                } catch (NoSuchFieldError nofield) {
                                    nofield.printStackTrace();
                                    SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>Manca il campo <b>" + nofield.getMessage() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                                } catch (NoClassDefFoundError noclass) {
                                    noclass.printStackTrace();
                                    SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>Manca la classe <b>" + noclass.getMessage() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                                } catch (Throwable tr) {
                                    tr.printStackTrace();
                                    SwingUtils.showErrorMessage(getPadreFrame(), "<html>Errore durante l'avvio del <b>" + pd.getName() + "</b><br>" + tr.toString() + "</b><br>Probabilmente devi aggiornare Invoicex Base all'ultima release per utilizzare questo plugin</html>");
                                }
                            }
                            controllaFlagPlugin(pd.getName());
                        }
                    }
                    if (!s1) {
                        tscaduto.start();
                    }
                }
                if (main.debug) {
                    //ricarico plugin ricompilati
                    Thread treloadplugins = new Thread("reload plugins") {
                        @Override
                        public void run() {
                            int w = 2000;
                            while (true) {
                                try {
                                    Thread.sleep(w);

                                    File[] files = new File(plugins_path).listFiles();
                                    for (File f : files) {
                                        if (f.getName().endsWith(".jar") && f.lastModified() >= (System.currentTimeMillis() - w)) {
                                            SwingUtils.showFlashMessage2("ricarico " + f.getName(), 3);

                                            PluginFactory pf = new PluginFactory();
                                            pf.loadPlugins(plugins_path);
                                            Collection list = pf.getAllEntryDescriptor();
                                            Iterator iter = list.iterator();
                                            while (iter.hasNext()) {
                                                EntryDescriptor entry = (EntryDescriptor) iter.next();
                                                if (entry.getNomeFileJar().equals(f.getName())) {
                                                    pluginsAvviati.get(entry.getName()).stopPluginEntry();

                                                    PluginEntry plugin = pf.getPluginEntryByName(entry.getName());
                                                    plugin.startPluginEntry();
                                                    pluginsAvviati.put(entry.getName(), plugin);

                                                    break;
                                                }
                                            }
                                        }
                                    }

                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                    };
                    treloadplugins.start();
                }
            }
        }

        //controllo se necessita ai alcuni plugin obbligatoriamente
        try {
            String plugin_necessari = cu.s(dbu.getObject(Db.getConn(), "select plugin_necessari from dati_azienda"));
            String[] plugins_necessari = StringUtils.split(plugin_necessari, ",");
            for (String plugin_necessario : plugins_necessari) {
                if (!pluginPresenti.contains(plugin_necessario)) {
                    SwingUtils.showErrorMessage(main.getPadreFrame(), "Non hai installato il '" + plugin_necessario + "' ma per questa installazione di Invoicex è necessario !", true);
                    exitMain();
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException && ((SQLException) e).getErrorCode() == 1054) {
                //ignoro mancanza del campo
            } else {
                e.printStackTrace();
            }
        }

//        //controllo se far vedere note di rilascio
//        File fnote = new File("note_" + version.toStringUnderscore() + ".html");
//        File fnote2 = new File("note_" + version.toStringUnderscore() + "_vis.html");
//        if (fnote.exists() && !fnote2.exists()) {
//            try {
//                JDialogNoteRilascio note = new JDialogNoteRilascio(getPadre(), true);
//                note.jTextPane1.setText(FileUtils.readContent(fnote));
//                note.setLocationRelativeTo(null);
//                note.setVisible(true);
//                File fren = new File("note_" + version.toStringUnderscore() + "_vis.html");
//                if (fren.exists()) {
//                    fren.delete();
//                }
//                fnote.renameTo(fren);
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        try {
//            Thread.sleep(3000);
//        } catch (Exception e) {
//        }
        main.fine_init_plugin = true;

        main.getPadrePanel().lblInfoLoading2.setVisible(false);

        //salvo il param_prop.txt in backup;
        Thread tsalvaparam = new Thread("salvaparam") {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                    File fparam1 = new File(main.wd + paramProp);
                    File fparam1b = new File(main.wd + paramProp + ".backup");
                    System.out.println("copio file param...");
                    FileUtils.copyFile(fparam1, fparam1b);
                    System.out.println("copio file param...copiato");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        tsalvaparam.start();

        if (s1) {
            return;
        }

        events.fireInvoicexEvent(new InvoicexEvent(this, InvoicexEvent.TYPE_MAIN_controlli));

        //controllo se ultimamente ha fatto un backup altrimenti glielo suggerisco
        if (!isPos() && !main.isBatch && !main.isServer) {
            File dirBackup = new File(main.wd + homeDir + "backup");

            if (!dirBackup.exists()) {
                dirBackup.mkdir();

                int ret = javax.swing.JOptionPane.showConfirmDialog(padre_frame, "Nell'ultima versione del programma abbiamo aggiunto la possibilita'\ndi eseguire delle copie dei dati dal menu' principale.\nVuoi subito eseguire una copia adesso?", "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

                if (ret == javax.swing.JOptionPane.YES_OPTION) {

                    //eseguo un dump del database
                    System.out.println("inizio backup");

                    //visualizzo frame con log processo
                    it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
                    try {
                        mess.setIconImage(getLogoIcon());
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                    mess.setBounds(100, 100, 400, 200);
                    mess.show();

                    DumpThread dump = new DumpThread(mess);
                    dump.start();
                    System.out.println("backup completato");
                }
            } else {

                //cerco ultimo file di backup
                File dir = new File(main.wd + homeDir + "backup");
                File[] lista = dir.listFiles();
                Vector listav = it.tnx.Util.getVectorFromArray(lista);

                if (listav != null) {
                    java.util.Collections.sort(listav, new java.util.Comparator() {
                        public int compare(Object o1, Object o2) {

                            File f1 = (File) o1;
                            File f2 = (File) o2;

                            if (f1.lastModified() > f2.lastModified()) {

                                return -1;
                            } else if (f1.lastModified() < f2.lastModified()) {

                                return 1;
                            } else {

                                return 0;
                            }
                        }
                    });

                    File dump = (File) listav.get(0);
                    Calendar cal = Calendar.getInstance();
                    Calendar calOggi = Calendar.getInstance();
                    Date dateModified = new Date(dump.lastModified());
                    cal.setTime(dateModified);
                    calOggi.setTime(new Date());

                    long ldate1 = dateModified.getTime();
                    long ldate2 = new Date().getTime();

                    // Use integer calculation, truncate the decimals
                    Integer quantigiorni = null;
                    String quando = main.fileIni.getValue("backup", "quando", "Chiedere ogni 2 settimane");
                    //"Chiedere ogni 2 settimane", "Chiedere ogni settimana", "Chiedere tutti i giorni", "Chiedi alla chiusura di Invoicex", "Alla chiusura di Invoicex senza chiedere", "Mai" }));
                    if (quando.equalsIgnoreCase("Chiedere ogni 2 settimane")) {
                        quantigiorni = 14;
                    } else if (quando.equalsIgnoreCase("Chiedere ogni settimana")) {
                        quantigiorni = 7;
                    } else if (quando.equalsIgnoreCase("Chiedere tutti i giorni")) {
                        quantigiorni = 1;
                    }
                    if (quantigiorni != null) {
                        int hr1 = (int) (ldate1 / 3600000); //60*60*1000
                        int hr2 = (int) (ldate2 / 3600000);
                        int days1 = (int) hr1 / 24;
                        int days2 = (int) hr2 / 24;
                        int dateDiff = days2 - days1;

                        if (dateDiff > quantigiorni) {
                            //                if (dateDiff >= 0) {

                            String richiesta = "Vuoi eseguire la copia di sicurezza dei dati ?";
                            if (pluginBackupTnx) {
                                richiesta += "\n\nLa copia sarà effettuata sui server TNX perchè hai il plugin Backup.";
                                richiesta += "\nSe vuoi eseguirla solo nel tuo computer ignora questa richiesta ed esegui";
                                richiesta += "\nsuccessivamente la copia locale da 'Utilità->Copia di sicurezza'";
                            }
                            int ret = javax.swing.JOptionPane.showConfirmDialog(padre_frame, richiesta, "Attenzione", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

                            if (ret == javax.swing.JOptionPane.YES_OPTION) {
                                if (pluginBackupTnx) {
                                    System.out.println("inizio backup online");
                                    main.getPadrePanel().callBackupOnline();
                                    System.out.println("backup online completato");
                                } else {
                                    //eseguo un dump del database
                                    System.out.println("inizio backup locale");
                                    //visualizzo frame con log processo
                                    it.tnx.JFrameMessage mess = new it.tnx.JFrameMessage();
                                    try {
                                        mess.setIconImage(getLogoIcon());
                                    } catch (Exception err) {
                                        err.printStackTrace();
                                    }
                                    mess.setBounds(100, 100, 400, 200);
                                    mess.show();

                                    DumpThread dump2 = new DumpThread(mess);
                                    dump2.start();
                                    System.out.println("backup completato");
                                }
                            }
                        }
                    }
                }
            }
        }

        //info plugins
        if (fileIni.getValueBoolean("pref", "msg_plugins", true)) {
            DebugUtils.dump(pluginPresenti);
            if (!pluginPresenti.contains("pluginRicerca")) {
                JDialogInstallaPlugins d = new JDialogInstallaPlugins(padre, true);
                d.setLocationRelativeTo(null);
                d.setVisible(true);
                if (d.si) {
                    if (attivazione.getIdRegistrazione() == null) {
                        if (SwingUtils.showYesNoMessage(padre, "Per utilizzare le funzionalità aggiuntive devi prima registrare il programma,\nclicca su Sì per proseguire nella registrazione")) {
                            main.getPadrePanel().apridatiazienda();
                            if (attivazione.getIdRegistrazione() != null) {
                                JDialogPlugins plugins = padre_panel.showplugins(true);
                            } else {
                                SwingUtils.showInfoMessage(main.padre, "Impossibile scaricare i plugin, il programma non e' registrato");
                            }
                        }
                    } else {
                        JDialogPlugins plugins = padre_panel.showplugins(true);
                    }
                } else if (d.jCheckBox1.isSelected()) {
                    fileIni.setValue("pref", "msg_plugins", false);
                }
            }
        }

//        adesso via news
//        //visualizzo note rilascio ?
//        String knote = "note_rilascio_181";
//        if (!fileIni.existKey("varie", knote)) {
//            fileIni.setValue("varie", knote, true);
//            getPadrePanel().visualizzaNote();
//        }
        //controllo aggiornamenti
        SwingWorker w = new SwingWorker() {
            @Override
            public Object construct() {
                try {
                    Thread.sleep(9000);
                    controllaupd();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        w.start();

        //controllo numerazione documenti
        if (fileIni.getValueBoolean("pref", "controlloNumeriAvvio", true)) {
            SwingWorker check_numerazioni = new SwingWorker() {
                @Override
                public Object construct() {
                    try {
                        Thread.sleep(5000);

                        String msg = "";
                        MicroBench mb = new MicroBench();
                        mb.start();
                        String msgf = controlloNumeri("test_fatt");
                        String msgd = controlloNumeri("test_ddt");
                        System.out.println("fine controllo numerazione tempo:" + mb.getDiff(""));
                        if (StringUtils.isNotBlank(msgf) || StringUtils.isNotBlank(msgd)) {
                            JPanel panel = new JPanel();
                            panel.setLayout(new GridBagLayout());
                            final JTextArea textArea = new JTextArea(10, 50);
                            msg = "Attenzione, problemi nella numerazione dei documenti:\n";
                            if (StringUtils.isNotBlank(msgf)) {
                                msg += "\nFatture di vendita:\n" + msgf;
                            }
                            if (StringUtils.isNotBlank(msgd)) {
                                msg += "\nDDT di vendita:\n" + msgd;
                            }
                            textArea.setText(msg);
                            textArea.setEditable(false);
                            JScrollPane scrollPane = new JScrollPane(textArea);
                            panel.add(scrollPane, new GridBagConstraints());
                            JButton print = new JButton("Stampa");
                            print.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    PrintUtilities p = new PrintUtilities(textArea);
                                    p.scala = 1;
                                    p.print();
                                }
                            });
                            GridBagConstraints gridBagConstraints = new GridBagConstraints();
                            gridBagConstraints.gridx = 0;
                            gridBagConstraints.gridy = 1;
                            gridBagConstraints.anchor = GridBagConstraints.EAST;
                            gridBagConstraints.insets = new Insets(6, 0, 3, 0);
                            panel.add(print, gridBagConstraints);
                            JOptionPane.showMessageDialog(main.getPadreFrame(), panel, "Attenzione !", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            check_numerazioni.start();
        }

        if (!fileIni.getValueBoolean("pref", "controlloRivalsaBugMassimale", false)) {
            SwingWorker check_massimale_rivalsa = new SwingWorker() {
                @Override
                public Object construct() {
                    try {
                        Thread.sleep(7000);

                        boolean flag = cu.toBoolean(DbUtils.getObject(Db.getConn(), "select flag_bug_massimale_rivalsa from dati_azienda"));
                        if (flag) {
                            fileIni.setValue("pref", "controlloRivalsaBugMassimale", true);
                        } else {
                            String msg = "";
                            MicroBench mb = new MicroBench();
                            mb.start();
                            String msg1 = controlloRivalsaBugMassimale("test_fatt");
                            String msg2 = controlloRivalsaBugMassimale("test_fatt_acquisto");

                            System.out.println("fine controlloRivalsaBugMassimale tempo:" + mb.getDiff(""));

                            if (StringUtils.isNotBlank(msg1) || StringUtils.isNotBlank(msg2)) {
                                JDialogBugMassimaleRivalsa dialog = new JDialogBugMassimaleRivalsa(main.getPadreFrame(), true);
                                msg = "Con l'aggiornamento 2014-08-04 abbiamo riscontrato e risolto un bug sul massimale rivalsa (se impostato in anagrafica Rivalsa) per cui nel totale da pagare poteva non venir considerato correttamente.\n"
                                        + "Qui sotto puoi trovare l'elenco delle fatture di vendita e di acquisto con rivalsa e massimale impostato ad un certo importo.\n"
                                        + "Per verificare dovresti aprirle per confrontare il totale da pagare attuale con quello registrato precedentemente.\n\n";
                                msg += msg1;
                                msg += msg2;
                                dialog.jTextArea1.setText(msg);
                                dialog.setLocationRelativeTo(null);
                                dialog.setVisible(true);
                            } else {
                                fileIni.setValue("pref", "controlloRivalsaBugMassimale", true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            check_massimale_rivalsa.start();
        }

//cambiata la struttura della tabella scadenze e provvigioni per collegarsi con id documento        
//        if (!fileIni.getValueBoolean("pref", "controlloBugProvvigioni", false)) {
//            SwingWorker check_bug_provvigioni = new SwingWorker() {
//                @Override
//                public Object construct() {
//                    try {
//                        Thread.sleep(2000);
//  
//                        boolean flag = cu.toBoolean(DbUtils.getObject(Db.getConn(), "select flag_bug_provvigioni from dati_azienda"));
//                        if (flag) {
//                            fileIni.setValue("pref", "controlloBugProvvigioni", true);
//                        } else {
//                            String msg = "";
//                            MicroBench mb = new MicroBench();
//                            mb.start();
//
//                            String msg1 = controlloBugProvvigioni();
//
//                            System.out.println("fine controlloBugProvvigioni tempo:" + mb.getDiff(""));
//
//                            if (StringUtils.isNotBlank(msg1)) {
//                                JDialogBugProvvigioni dialog = new JDialogBugProvvigioni(main.getPadreFrame(), true);
//                                msg = "Con l'aggiornamento 2015-01-22 abbiamo riscontrato e risolto un bug sulla generazione delle provvigioni\n"
//                                        + "introdotto con l'aggiornamento precedente di Invoicex.\n"
//                                        + "Qui sotto puoi trovare l'elenco delle fatture di vendita interessate dal problema di cui sopra.\n"
//                                        + "Per verificare se le provvigioni sono correte puoi farlo da Agenti->Situazione Agenti oppure direttamente\n"
//                                        + "cliccando il tasto destro sulla fattura (in elenco fatture) e selezionare 'Controlla Provvigioni'\n"
//                                        + "\n"
//                                        + "Se le provvigioni non sono state generate o sono errate puoi rigenerarle cliccando il tasto destro\n"
//                                        + "sulla fattura (in elenco fatture) e selezionando 'Rigenera Provvigioni'\n\n";
//                                msg += msg1;
//                                dialog.jTextArea1.setText(msg);
//                                dialog.setLocationRelativeTo(null);
//                                dialog.setVisible(true);
//                            } else {
//                                fileIni.setValue("pref", "controlloBugProvvigioni", true);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//            };
//            check_bug_provvigioni.start();
//        }
        //controllo avvisi clienti
        SwingWorker check_avvisi = new SwingWorker() {
            @Override
            public Object construct() {
                try {
                    Random r = new Random();
                    int waittime = (int) (r.nextFloat() * 3000);
                    Thread.sleep(waittime);
                    main.padre_panel.segnalaRapporti();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        check_avvisi.start();

        //controlli file temporanei
        Thread tfiletemp = new Thread("deletefiletemp") {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    eliminaFileTemporanei();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        tfiletemp.start();
    }

    ;

    public static void eliminaFileTemporanei() {
        //_old_
        //lib/_old_
        //tempStampa_
        //tempPrn
        //temp
        MicroBench mb = new MicroBench();
        int eliminati = 0;
        mb.start();
        File dir = new File(".");
        File[] files = dir.listFiles();
        Long now = System.currentTimeMillis();
        Long nowmeno7gg = now - (1000 * 60 * 60 * 24 * 7);
        Date d = new Date(nowmeno7gg);
        System.out.println("d = " + d);
        if (files != null) {
            for (File f : files) {
                boolean del = false;
                String n = f.getName();
                if (f.isFile()) {
                    if (n.startsWith("temp")) {
                        if (f.lastModified() < nowmeno7gg) {
                            del = true;
                        }
                    }
                    if (n.indexOf("_old_") > 0) {
                        if (f.lastModified() < (now - (1000l * 60l * 60l * 24l * 30l))) {
                            del = true;
                        }
                    }
                }
                if (del) {
                    System.out.println("delete di: " + f.getAbsolutePath());
                    f.delete();
                }
            }
        }
        dir = new File("lib");
        files = dir.listFiles();
        Long nowmeno30gg = now - (1000l * 60l * 60l * 24l * 30l);
        d = new Date(nowmeno30gg);
        System.out.println("d = " + d);
        if (files != null) {
            for (File f : files) {
                boolean del = false;
                String n = f.getName();
                if (f.isFile()) {
                    if (n.indexOf("_old_") > 0) {
                        if (f.lastModified() < nowmeno30gg) {
                            del = true;
                        }
                    }
                }
                if (del) {
                    System.out.println("delete di: " + f.getAbsolutePath());
                    f.delete();
                }
            }
        }
        mb.out("fine elimina files temp eliminati:" + eliminati);
    }

    public static String controlloNumeri(String nometab) {

        //testo connessione, se non c'è esco
        if (Db.getConn(true) == null) {
            return null;
        }

        String doc1 = "";
        String doc2 = "";
        String doc3 = "";
        if (nometab.equalsIgnoreCase("test_fatt")) {
            doc1 = "alla Fattura";
            doc2 = "Fatture";
            doc3 = "Fattura";
        } else if (nometab.equalsIgnoreCase("test_ddt")) {
            doc1 = "al DDT";
            doc2 = "DDT";
            doc3 = "DDT";
        }
        String msg = "";
        try {
            String sql = "select serie from " + nometab + " where year(data) = year(now()) ";
            if (nometab.equals("test_fatt")) {
                sql += " and tipo_fattura != " + dbFattura.TIPO_FATTURA_SCONTRINO + " and tipo_fattura != " + dbFattura.TIPO_FATTURA_PROFORMA;
            }
            sql += " group by serie";
            ArrayList<String> serie = DbUtils.getList(Db.getConn(true), sql);
            for (String s : serie) {
                System.out.println("s = " + s);
                sql = "select numero, data, serie, id from " + nometab + " where year(data) = year(now()) and serie = " + Db.pc(s, Types.VARCHAR);
                if (nometab.equals("test_fatt")) {
                    sql += " and tipo_fattura != " + dbFattura.TIPO_FATTURA_SCONTRINO + " and tipo_fattura != " + dbFattura.TIPO_FATTURA_PROFORMA;
                }
                sql += " order by data, numero";
                List<Map> numeri = DbUtils.getListMap(Db.getConn(true), sql);
                List<Long> numeri2 = DbUtils.getList(Db.getConn(true), sql);
                if (numeri.size() > 0) {
                    int start = CastUtils.toInteger0(numeri.get(0).get("numero"));
                    for (int i = 0; i < numeri.size(); i++) {
                        Map m = numeri.get(i);
                        int n = CastUtils.toInteger0(m.get("numero"));
                        int n0 = n;
                        int n2 = n;
                        if (i > 0) {
                            n0 = CastUtils.toInteger0(numeri.get(i - 1).get("numero"));
                        }
                        if (i < numeri.size() - 1) {
                            n2 = CastUtils.toInteger0(numeri.get(i + 1).get("numero"));
                        }
                        if (n != start && n0 < n - 1) {
                            if ((n0 + 1) == (n - 1)) {
                                //controllo se esiste o no
                                if (!numeri2.contains(((Integer) start).longValue())) {
                                    msg += doc3 + " numero " + s + (start) + " mancante\n";
                                } else {
                                    msg += doc3 + " numero " + s + (start) + " presente ma con data errata\n";
                                }
                            } else {
                                msg += doc2 + " mancanti dalla " + s + (n0 + 1) + " alla " + s + (n - 1) + "\n";
                            }
                            start = n;
                        } else if (n != start) {
                            //msg += "errore " + doc1 + " numero " + n + " (numero precedente " + n0 + ", numero successivo " + n2 + ")\n";
                            start = n;
                        }
                        start++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }

    public static String controlloRivalsaBugMassimale(String nometab) {
        String doc = "";
        if (nometab.equalsIgnoreCase("test_fatt")) {
            doc = "Fattura di vendita";
        } else if (nometab.equalsIgnoreCase("test_fatt_acquisto")) {
            doc = "Fattura di acquisto";
        }
        String msg = "";
        try {
            String sql = "select t.id, t.numero, t.serie, t.data, t.rivalsa, t.totaleRivalsa \n"
                    + " , tr.massimale, t.totale_da_pagare\n"
                    + " from " + nometab + " t left join tipi_rivalsa tr on t.rivalsa = tr.id\n"
                    + " where t.rivalsa is not null and IFNULL(tr.massimale,0) > 0 and t.totaleRivalsa >= tr.massimale and year(t.`data`) = YEAR(CURDATE())";
            List<Map> list = dbu.getListMap(Db.getConn(), sql);
            if (list != null && list.size() > 0) {
                for (Map m : list) {
                    msg += doc + " " + (StringUtils.isBlank(cu.s(m.get("serie"))) ? "" : (cu.s(m.get("serie")) + "/")) + cu.s(m.get("numero")) + " del " + DateUtils.formatDateIta(cu.toDate(m.get("data"))) + "\n";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }

    public static String controlloBugProvvigioni() {
        String msg = "";
        try {
            boolean per_scadenze = true;
            try {
                String provvigioni_tipo_data = CastUtils.toString(DbUtils.getObject(Db.getConn(), "select provvigioni_tipo_data from dati_azienda"));
                if (provvigioni_tipo_data.equalsIgnoreCase("data_fattura")) {
                    per_scadenze = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Integer tipo_calcolo = cu.i(dbu.getObject(Db.getConn(), "select provvigioni_tipo_calcolo from dati_azienda"));
            if (tipo_calcolo == null) {
                tipo_calcolo = ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE_MENO_SPESE;
            }
//!!! la struttura di scadenze e provvigioni adesso è diversa
//commentato il richiamo dal main            
            if (!per_scadenze || tipo_calcolo != ProvvigioniFattura.TIPO_CALCOLO_IMPONIBILE_MENO_SPESE) {
                String sql = "select t.id, r.id, t.agente_codice, t.agente_percentuale \n"
                        + " , r.provvigione, t.data, t.serie, t.numero, t.anno, t.ts \n"
                        + " , c.ragione_sociale \n"
                        + " , sum(p.importo_provvigione) as somma_prov \n"
                        + " from test_fatt t \n"
                        + " left join righ_fatt r on r.id_padre = t.id \n"
                        + " left join clie_forn c on t.cliente = c.codice \n"
                        + " left join provvigioni p on p.documento_tipo = 'FA' \n"
                        + "           and p.documento_serie = t.serie and p.documento_numero = t.numero and p.documento_anno = t.anno \n"
                        + " where t.data >= '2014-12-01' \n"
                        + " and IFNULL(agente_codice,0) != 0 \n"
                        + " and IFNULL(r.provvigione,0) != 0 \n"
                        + " group by t.id \n"
                        + " having somma_prov is null";
                List<Map> list = dbu.getListMap(Db.getConn(), sql);
                if (list != null && list.size() > 0) {
                    for (Map m : list) {
                        msg += (StringUtils.isBlank(cu.s(m.get("serie"))) ? "" : (cu.s(m.get("serie")) + "/")) + cu.s(m.get("numero")) + " del " + DateUtils.formatDateIta(cu.toDate(m.get("data"))) + " - " + cu.s(m.get("ragione_sociale")) + "\n";
                    }
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }

    public static Image getLogoIcon() {
        //setIconImage(new ImageIcon(getClass().getResource("/res/48x48.gif")).getImage());
        File newicon = new File("icone/48x48_8.gif");
        if (newicon.exists()) {
            return new ImageIcon(newicon.getAbsolutePath()).getImage();
        } else {
            return new ImageIcon(main.class.getResource("/res/48x48.gif")).getImage();
        }
    }

    private void publish(String string) {
        System.out.println(string);
    }

    public void scaricaAggiornamentoPlugin(Plugin po, EntryDescriptor pd) throws IOException {
        File f = new File(main.wd + "plugins/" + pd.getNomeFileJar() + ".tmp");

        try {
            System.out.println("scarico: file:" + f);
            if (f.exists()) {
                f.delete();
            }

            HttpUtils.saveFile(main.baseurlserver + "/" + InvoicexUtil.getDownloadDir() + "/plugins/" + pd.getNomeFileJar(), f.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (f.length() == 0) {
            System.out.println("errore in download, file " + f.getAbsolutePath() + " 0 len");
            return;
        }

        //controllo che sia jar apribile
        try {
            FileInputStream fis = new FileInputStream(f);
            JarInputStream jis = new JarInputStream(new FileInputStream(f));
            JarEntry jar = null;
            while ((jar = jis.getNextJarEntry()) != null) {
//                System.out.println("jar = " + jar);
            }
            jis.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        File f2 = new File(main.wd + "plugins/" + pd.getNomeFileJar());
        System.out.println("copio: f:" + f + " in f2:" + f2);
        if (f2.exists()) {
            boolean ret = f2.delete();
            System.out.println("elimino precedente:" + ret);
        }
        FileUtils.copyFile(f, f2);
        boolean ret = f.delete();
        System.out.println("elimino tmp:" + ret);

        publish("agg " + po + " ricevuto");
    }

    public void scaricaAggiornamentoPluginOld(Plugin po, EntryDescriptor pd) throws IOException {
//        String server = "www.tnx.it";
//        int port = 8080;
        String server = "s.invoicex.it";
//        String server = "s.invoicex.linux";
        int port = 80;
        String post_url = "/InvoicexWSServer/SendAggPlugins";

//        server = "192.168.0.115";
//        port = 8080;
//        post_url = "/InvoicexWSServer/SendAggPlugins";
        publish("Invio richiesta aggiornamento");
        Socket socket = new Socket(server, port);

        //altro test
        try {
            System.out.println("test: " + HttpUtils.getUrlToStringUTF8("http://s.invoicex.it/InvoicexWSServer/SendAggPlugins"));
        } catch (Exception e) {
        }

        OutputStream outs = socket.getOutputStream();
        InputStream ins = socket.getInputStream();

        String get = "GET " + post_url + " HTTP/1.1\n"
                + "Host: " + server + "\n"
                + "User-Agent: Invoicex/" + main.version + "\n"
                //                + "Accept: text/html,application/xhtml+xml,application/xml,application/octet-stream;q=0.9,*/*;q=0.8\n"
                //                + "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\n"
                //                + "x-id: " + attivazione.getIdRegistrazione() + "\n"
                //                + "x-plugin: " + pd.getNomeFileJar() + "\n"
                //                + "Connection: keep-alive\n"
                + "x-id: " + attivazione.getIdRegistrazione() + "\n"
                + "x-plugin: " + pd.getNomeFileJar() + "\n"
                + "\n";

        outs.write(get.getBytes());
        outs.flush();

        System.out.println("get:" + get);

        System.out.println("1");

        byte[] bytesIn = new byte[1024 * 8];
        int readed = 0;

        publish("Inizio download agg");

        //inizio a ricevere il file
        File fd = new File(main.wd + "agg");
        fd.mkdir();
        File f = new File(main.wd + "plugins/" + pd.getNomeFileJar() + ".tmp");
        System.out.println("scarico: file:" + f);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fos = new FileOutputStream(f);

        System.out.println("2");

        byte[] buff = new byte[10000];
        int read = 0;
        boolean headers = true;
        DecimalFormat f1 = new DecimalFormat("0,000.#");
        int toread = Integer.MAX_VALUE;
        while ((read = ins.read(buff)) > 0) {
            publish("Download " + f.getName() + " " + (int) ((double) readed / (double) toread * 100d) + "%");
            if (headers) {
                String temp1 = new String(buff);
                int ind1 = temp1.indexOf("\r\n\r\n");
                String sheaders = temp1.substring(0, ind1);
                System.out.println("headers:" + sheaders);
                String shs[] = sheaders.split("\\r\\n");
                for (String s : shs) {
                    System.out.println("s:" + s);
                    if (s.startsWith("HTTP/1.1 500")) {
                        System.out.println("problema nell'aggiornamento");
                        fos.close();
                        f.delete();
                        return;
                    }
                    if (s.startsWith("Content-Length")) {
                        String shs2[] = s.split(":");
                        toread = Integer.parseInt(shs2[1].trim());
                        System.out.println("toread:" + toread + " / " + shs2[1]);
                    }
                }
                readed += read - (ind1 + 4);
                fos.write(buff, ind1 + 4, read - (ind1 + 4));
                headers = false;
            } else {
                readed += read;
                //System.out.print(".");
                fos.write(buff, 0, read);
            }
            if (readed >= toread) {
                break;
            }
        }

        System.out.println("4 readed:" + readed);

        fos.flush();
        fos.close();
        System.out.println("");
        System.out.println("ricevuto:" + f + " / " + f.getAbsolutePath());

        File f2 = new File(main.wd + "plugins/" + pd.getNomeFileJar());
        System.out.println("copio: f:" + f + " in f2:" + f2);
        if (f2.exists()) {
            boolean ret = f2.delete();
            System.out.println("elimino precedente:" + ret);
        }
        FileUtils.copyFile(f, f2);
        boolean ret = f.delete();
        System.out.println("elimino tmp:" + ret);

        //analizzo plugin descriptor e aggiorno plugins
//        EntryDescriptor ed = pf.loadPluginByFileName(plugins_path, f2.getName());
        publish("Agg ricevuto");
    }
    public static ZipFile zf;
    public static final int EOF = -1;

    public static void unzip(File filezip, String dst) {
        Enumeration enum1;

        try {
            zf = new ZipFile(filezip.getAbsolutePath());
            enum1 = zf.entries();
            while (enum1.hasMoreElements()) {
                ZipEntry target = (ZipEntry) enum1.nextElement();
                System.out.print(target.getName() + " .");
                saveEntry(target, null, dst);
                System.out.println(". unpacked");
            }
        } catch (FileNotFoundException e) {
            System.out.println("zipfile not found");
        } catch (ZipException e) {
            System.out.println("zip error...");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO error...");
        }
    }

    public static void saveEntry(ZipEntry target, UnzipWorker work) throws ZipException, IOException {
        saveEntry(target, work, null);
    }

    public static void saveEntry(ZipEntry target, UnzipWorker work, String dst) throws ZipException, IOException {
        try {
            File file = new File(target.getName());
            if (dst != null) {
                file = new File(dst + target.getName());
            }
            if (target.isDirectory()) {
                file.mkdirs();
            } else {
                InputStream is = zf.getInputStream(target);
                BufferedInputStream bis = new BufferedInputStream(is);

                if (file.getParent() != null) {
                    File dir = new File(file.getParent());
                    dir.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int c;
                byte[] buff = new byte[1204 * 32];
                int writed = 0;
                //while ((c = bis.read()) != EOF) {
                int towrite = (int) target.getSize();
                while ((c = bis.read(buff)) > 0) {
                    writed += c;
                    //bos.write((byte)c);
                    bos.write(buff, 0, c);
                    if (work != null) {
                        work.publicPublish("Unzip " + target + " " + (writed * 100 / towrite) + "%");
                    }
                }

                bos.close();
                fos.close();
            }
        } catch (ZipException e) {
            if (work != null) {
                work.publicPublish(e.toString());
            }
            throw e;
        } catch (IOException e) {
            if (work != null) {
                work.publicPublish(e.toString());
            }
            throw e;
        }
    }

    static public boolean isPluginContabilitaAttivo() {
        if (main.pluginContabilita) {
            System.out.println("isPluginContabilitaAttivo:" + main.fileIni.getValueBoolean("plugin_contab", "attivo", false));
            return main.fileIni.getValueBoolean("plugin_contab", "attivo", false);
        } else {
            System.out.println("isPluginContabilitaAttivo:false");
            return false;
        }
    }

    static public String getUserHome() {
        if (main.dir_user_home == null) {
            main.dir_user_home = System.getProperty("user.home");
        }
        return main.dir_user_home;
    }

    static public String getUserHomeInvoicex() {
        return getUserHome() + File.separator + ".invoicex";
    }

    static public String getCacheImgDir() {
        return getUserHomeInvoicex() + File.separator + "cache_img" + File.separator;
    }

    static public Boolean isPos() {
        if (pos != null) {
            return pos;
        }
        try {
            String dbpos = cu.s(dbu.getObject(it.tnx.Db.getConn(), "select pos from accessi_utenti where id = " + main.utente.getIdUtente()));
            if (dbpos.equalsIgnoreCase("S")) {
                pos = true;
            } else {
                pos = false;
            }
        } catch (Exception e) {
            pos = false;
        }
        return pos;
    }
}
