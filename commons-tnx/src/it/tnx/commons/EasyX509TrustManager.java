/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Marco
 */
public class EasyX509TrustManager implements X509TrustManager {

    private X509TrustManager standardTrustManager = null;

    /**
     * Log object for this class.
     */
    private static final Log LOG = LogFactory.getLog(EasyX509TrustManager.class);

    /**
     * Constructor for EasyX509TrustManager.
     */
    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    /**
     * @see
     * javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String
     * authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType)  {
        try {
            standardTrustManager.checkClientTrusted(certificates, authType);
        } catch (java.security.cert.CertificateException ex) {
            Logger.getLogger(EasyX509TrustManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @see
     * javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String
     * authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) {
        if ((certificates != null) && LOG.isDebugEnabled()) {
            LOG.debug("Server certificate chain:");
            for (int i = 0; i < certificates.length; i++) {
                LOG.debug("X509Certificate[" + i + "]=" + certificates[i]);
            }
        }
        if ((certificates != null) && (certificates.length == 1)) {
            try {
                certificates[0].checkValidity();
            } catch (CertificateExpiredException ex) {
                Logger.getLogger(EasyX509TrustManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateNotYetValidException ex) {
                Logger.getLogger(EasyX509TrustManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                standardTrustManager.checkServerTrusted(certificates, authType);
            } catch (java.security.cert.CertificateException ex) {
                Logger.getLogger(EasyX509TrustManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}
