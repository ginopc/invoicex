/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.model.JSONString;
import com.sdicons.json.parser.JSONParser;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.protocol.Protocol;
// import org.apache.log4j.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author mceccarelli
 */
public class HttpUtils {

    private static boolean debug = true;
    private static HttpClient httpclient = null;
    public static int conta_credentials = 0;

    synchronized static public HttpClient getHttpClient() {
        if (httpclient != null) return httpclient;
        
        try {
            Logger.getLogger("org.apache.commons.httpclient.").setLevel(Level.OFF);
            Logger.getLogger("org.apache.commons.httpclient.").setLevel(Level.OFF);
            Logger.getLogger("httpclient.wire.content").setLevel(Level.OFF);
            Logger.getLogger("httpclient.wire.header").setLevel(Level.OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
//        int timeout = 30;
        int timeout = 240;
        try {
            Protocol.registerProtocol("https",
                    new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
            
            MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
            
            httpclient = new HttpClient(connectionManager);
            
            String proxy_host = null;
            Integer proxy_port = null;
            if (System.getProperty("java.net.useSystemProxies", "false").equals("true")) {
                List<Proxy> proxys;
                try {                    
                    proxys = ProxySelector.getDefault().select(new URI("socket://www.invoicex.it"));
                    System.out.println("HttpUtils getHttpClient proxys socket = " + proxys);

                    proxys = ProxySelector.getDefault().select(new URI("http://www.invoicex.it"));
                    System.out.println("HttpUtils getHttpClient proxys http = " + proxys);
                    
                    InetSocketAddress proxyaddr = (InetSocketAddress)proxys.get(0).address();
                    if (proxyaddr != null) {
                        proxy_host = proxyaddr.getHostName();
                        proxy_port = proxyaddr.getPort();
                    }
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }            
            } else {
                if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyHost").length() > 0) {
                    proxy_host = System.getProperty("http.proxyHost");
                    proxy_port = Integer.parseInt(System.getProperty("http.proxyPort"));
                }
            }
            if (proxy_host != null) {
                httpclient.getHostConfiguration().setProxy(proxy_host, proxy_port);
                httpclient.getParams().setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {
                    public Credentials getCredentials(AuthScheme as, String proxy_host, int proxy_port, boolean bln) throws CredentialsNotAvailableException {
                        conta_credentials++;
                        if (conta_credentials > 10) return null;
                        System.out.println("authscheme = " + as + " " + as.getRealm());
                        System.out.println("proxy_host = " + proxy_host);

                        if (System.getProperty("invoicex.proxy.user") != null) {
                            return new UsernamePasswordCredentials(System.getProperty("invoicex.proxy.user"), System.getProperty("invoicex.proxy.password"));
                        } else if (System.getProperty("commonstnx.proxy.user") != null) {
                            return new UsernamePasswordCredentials(System.getProperty("commonstnx.proxy.user"), System.getProperty("commonstnx.proxy.password"));
                        } else {
                            JDialogProxyAuth dialog = new JDialogProxyAuth(null, true);
                            dialog.setTitle("Autenticazione Proxy: " + proxy_host + ":" + proxy_port);
                            dialog.setLocationRelativeTo(null);
                            dialog.setVisible(true);
                            
                            System.setProperty("commonstnx.proxy.proxy", proxy_host + ":" + proxy_port);
                            System.setProperty("commonstnx.proxy.user", dialog.jTextField1.getText());
                            System.setProperty("commonstnx.proxy.password", String.valueOf(dialog.jPasswordField1.getPassword()));
                            
                            return new UsernamePasswordCredentials(dialog.jTextField1.getText(), String.valueOf(dialog.jPasswordField1.getPassword()));
                        }
                    }
                });
            }
            
//            System.out.println("timeout socket = " + httpclient.getParams().getParameter("http.socket.timeout"));
//            System.out.println("timeout socket = " + httpclient.getParams().getParameter("http.connection.timeout"));
//            System.out.println("timeout socket = " + httpclient.getParams().getParameter("http.connection-manager.timeout"));
            
            httpclient.getParams().setParameter("http.socket.timeout", new Integer(timeout * 1000));
            httpclient.getParams().setParameter("http.connection.timeout", new Integer(timeout * 1000));
            httpclient.getParams().setParameter("http.connection-manager.timeout", new Long(timeout * 1000));
            
            //POST in UTF8
            httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            
            //http.protocol.unambiguous-statusline

            return httpclient;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public String getUrl(HttpServletRequest req) {
        String out = req.getRequestURI();
        if (req.getQueryString() != null && req.getQueryString().length() > 0) {
            out += "?" + req.getQueryString();
        }
        return out;
    }

    static public String getUrlToStringUTF8(String url) throws Exception {
//        byte[] buff = new byte[1024 * 1024];
//        int readed = 0;
//        int readed_tot = 0;
//        URL url2 = new URL(url);
//        HttpURLConnection urlconn = (HttpURLConnection) url2.openConnection();
//        urlconn.setConnectTimeout(15000);
//        urlconn.setReadTimeout(15000);
//        urlconn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
//        urlconn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)");
//        int toread = urlconn.getContentLength();
//        InputStream input = urlconn.getInputStream();
//        String buffs = "";
//        while ((readed = input.read(buff)) > 0) {
//            buffs += new String(buff, 0, readed, "UTF8");
//            readed_tot += readed;
//        }
//        input.close();
//        return buffs;

        String ret = null;

        if (debug) {
            System.out.println("httpClient getUrlToStringUTF8: url: " + url);
        }

        HttpClient httpclient = getHttpClient();
        GetMethod httpGet = new GetMethod(url);
        try {
            httpclient.executeMethod(httpGet);
            if (debug) {
                System.out.println("httpClient getUrlToStringUTF8: url: " + url + " status: " + httpGet.getStatusLine());
//                for (Header h : httpGet.getResponseHeaders()) {
//                    System.out.println("httpClient getUrlToStringUTF8: url: " + url + " header: " + h);
//                }
            }
            ret = new String(httpGet.getResponseBody(), "UTF-8");
        } catch (Exception ex) {
            System.out.println("httpClient getUrlToStringUTF8: url: " + url + " ex: " + ex.toString());
            throw ex;
        } finally {
            httpGet.releaseConnection();
        }

        if (debug) {
            System.out.println("httpClient getUrlToStringUTF8: url: " + url + " ret: " + ret);
        }

        return ret;
    }

//    static public byte[] getUrlToByteArray(String url) throws Exception {
//        byte[] buff = new byte[1024 * 1024];
//        int readed = 0;
//        int readed_tot = 0;
//        URL url2 = new URL(url);
//        HttpURLConnection urlconn = (HttpURLConnection) url2.openConnection();
//        urlconn.setConnectTimeout(15000);
//        urlconn.setReadTimeout(15000);
//
////        urlconn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
//        System.out.println(urlconn.getContentType());
//        int toread = urlconn.getContentLength();
//        InputStream input = urlconn.getInputStream();
//        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
//        while ((readed = input.read(buff)) > 0) {
//            byteout.write(buff, 0, readed);
//            readed_tot += readed;
//        }
//        input.close();
//        return byteout.toByteArray();
//    }

    static public long getLastModified(String url) throws Exception {
//        URL url2 = new URL(url);
//        HttpURLConnection urlconn = (HttpURLConnection) url2.openConnection();
//        urlconn.setConnectTimeout(15000);
//        urlconn.setReadTimeout(15000);
//        urlconn.setRequestMethod("HEAD");
//        int toread = urlconn.getContentLength();
//        long lastm = urlconn.getLastModified();
//        System.out.println("getLastModified size:" + toread + " type:" + urlconn.getContentType() + " " + lastm);
//        InputStream is = urlconn.getInputStream();
//        IOUtils.toString(is);
//        is.close();
        
        HttpClient httpclient = getHttpClient();
        HttpMethod httpHead = new HeadMethod(url);
        long lastm = 0;
        try {
            httpclient.executeMethod(httpHead);
            if (debug) {
                System.out.println(httpHead.getStatusLine());
                for (Header h : httpHead.getResponseHeaders()) {
                    System.out.println("h = " + h);
                }                
            }
            lastm = Date.parse(httpHead.getResponseHeader("Last-Modified").getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            httpHead.releaseConnection();
        }
        
        return lastm;
    }

    static public Date getLastModifiedDate(String url) throws Exception {
        return new Date(getLastModified(url));
    }

    static public void saveFile(String url, String file) throws Exception {
        saveFile(url, file, 1024 * 64);
    }

    static public void saveFile(String url, String file, int bufferSize) throws Exception {
        byte[] buff = new byte[1024 * 1024];
        int readed = 0;
        int readed_tot = 0;
        
        
//        URL url2 = new URL(url);
//        HttpURLConnection urlconn = (HttpURLConnection) url2.openConnection();
//        urlconn.setConnectTimeout(15000);
//        urlconn.setReadTimeout(15000);
//
//        int retcode1 = urlconn.getResponseCode();
//        System.out.println("retcode1 = " + retcode1);
//
//        int toread = urlconn.getContentLength();
//        System.out.println("saveFile size:" + toread + " type:" + urlconn.getContentType());
//        InputStream input = urlconn.getInputStream();
//        while ((readed = input.read(buff)) > 0) {
//            fileout.write(buff, 0, readed);
//            readed_tot += readed;
//            System.out.println("saveFile " + FormatUtils.formatPerc((double) readed_tot / 1024d / 1024d) + "mb rimane:" + FormatUtils.formatPerc((double) (toread - readed_tot) / 1024d / 1024d) + "mb");
//        }
//        fileout.close();
//        input.close();
        
        
        HttpClient httpclient = getHttpClient();
        GetMethod httpGet = new GetMethod(url);
        FileOutputStream fileout = null;
        try {
            httpclient.executeMethod(httpGet);
            if (debug) {
                System.out.println("httpClient saveFile status: " + httpGet.getStatusLine());
                for (Header h : httpGet.getResponseHeaders()) {
                    System.out.println("httpClient saveFile h: " + h);
                }
            }
            if (httpGet.getStatusCode() != 200) {
                throw new Exception(url + " > " + httpGet.getStatusLine().toString());
            }
//testare 404
            long toread = Long.parseLong(httpGet.getResponseHeader("Content-Length").getValue());
            InputStream input = httpGet.getResponseBodyAsStream();
            fileout = new FileOutputStream(file);
            if (input != null) {
                while ((readed = input.read(buff)) > 0) {
                    fileout.write(buff, 0, readed);
                    readed_tot += readed;
                    System.out.println("httpClient saveFile " + FormatUtils.formatPerc((double) readed_tot / 1024d / 1024d) + "mb rimane:" + FormatUtils.formatPerc((double) (toread - readed_tot) / 1024d / 1024d) + "mb");
                }                
                input.close();
            }
        } finally {
            httpGet.releaseConnection();
            if (fileout != null) fileout.close();
        }        

    }

    static public void saveBigFile(String url, String file) throws Exception {
        saveBigFile(url, file, null);
    }

    public static interface SaveFileEventListener {

        void event(float progression);
    }

    static public void saveBigFile(String url, String file, SaveFileEventListener eventListener) throws Exception {
        byte[] buff = new byte[1024 * 1024];
        int readed = 0;
        int readed_tot = 0;
        FileOutputStream fileout = null;
        
//        URL url2 = new URL(url);
//        URLConnection urlconn = url2.openConnection();
//        urlconn.setConnectTimeout(15000);
//        urlconn.setReadTimeout(15000);
//
////        DebugUtils.dump(urlconn.getHeaderFields());
//        int toread = urlconn.getContentLength();
//        System.out.println("saveBigFile size:" + toread + " type:" + urlconn.getContentType());
//        InputStream input = urlconn.getInputStream();
//        while ((readed = input.read(buff)) > 0) {
//            fileout.write(buff, 0, readed);
//            readed_tot += readed;
////            System.out.println("saveBigFile " +  FormatUtils.formatPerc((double)readed_tot / 1024d / 1024d) + "mb");
//            System.out.println("saveBigFile " + FormatUtils.formatPerc((double) readed_tot / 1024d / 1024d) + "mb rimane:" + FormatUtils.formatPerc((double) (toread - readed_tot) / 1024d / 1024d) + "mb");
//            eventListener.event((float) readed_tot * 100f / (float) toread);
//        }
//        fileout.close();
//        input.close();
        
        
        HttpClient httpclient = getHttpClient();
        GetMethod httpGet = new GetMethod(url);
        try {
            httpclient.executeMethod(httpGet);
            if (debug) {
                System.out.println("httpClient saveBigFile status: " + httpGet.getStatusLine());
                for (Header h : httpGet.getResponseHeaders()) {
                    System.out.println("httpClient saveBigFile h: " + h);
                }
            }
            long toread = Long.parseLong(httpGet.getResponseHeader("Content-Length").getValue());
            InputStream input = httpGet.getResponseBodyAsStream();
            fileout = new FileOutputStream(file);
            if (input != null) {
                while ((readed = input.read(buff)) > 0) {
                    fileout.write(buff, 0, readed);
                    readed_tot += readed;
                    System.out.println("httpClient saveBigFile " + FormatUtils.formatPerc((double) readed_tot / 1024d / 1024d) + "mb rimane:" + FormatUtils.formatPerc((double) (toread - readed_tot) / 1024d / 1024d) + "mb");
                    if (eventListener != null) {
                        eventListener.event((float) readed_tot * 100f / (float) toread);
                    }
                }                
                input.close();
            }        
        } finally {
            httpGet.releaseConnection();
            if (fileout != null) fileout.close();
        }        

    }

    static public void dumpRequest(HttpServletRequest hr) {
        System.out.println("-- inizio dumpRequest hr:" + hr.getRequestURI() + " qs:" + hr.getQueryString());
        System.out.println("\tattributes");
        Enumeration e1 = hr.getAttributeNames();
        while (e1.hasMoreElements()) {
            String en = (String) e1.nextElement();
            System.out.println("\t\t" + en + ":" + hr.getAttribute(en));
        }
        System.out.println("\tparams");
        e1 = hr.getParameterNames();
        while (e1.hasMoreElements()) {
            String en = (String) e1.nextElement();
            System.out.println("\t\t" + en + ":" + hr.getParameter(en));
        }
        System.out.println("\theaders");
        e1 = hr.getHeaderNames();
        while (e1.hasMoreElements()) {
            String en = (String) e1.nextElement();
            System.out.println("\t\t" + en + ":" + hr.getHeader(en));
        }
        System.out.println("-- fine dumpRequest hr:" + hr);
    }

    public static void main(String[] args) {
        try {
            String test = getUrlToStringUTF8("http://www.panoramio.com/map/get_panoramas.php?order=popularity&set=public&from=0&to=16&minx=7.4591243&miny=43.6795062&maxx=7.8591243&maxy=44.079506200000004&size=square");
            FileOutputStream fout = new FileOutputStream("c:\\test.html");
            fout.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /></head><body>".getBytes());
            JSONParser parser = new JSONParser(new StringReader(test));
            JSONObject obj = (JSONObject) parser.nextValue();
            JSONArray photos = (JSONArray) obj.get("photos");
            ArrayList photosl = new ArrayList();
            for (int i = 0; i < photos.size(); i++) {
                String title = ((JSONString) ((JSONObject) photos.get(i)).get("photo_title")).getValue();
                fout.write((title + "<br />").getBytes());
            }
            fout.write("</body></html>".getBytes());
            fout.close();
            System.err.println("test: " + test);
        } catch (Exception ex) {
            Logger.getLogger(HttpUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
