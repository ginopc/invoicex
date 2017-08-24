/*
 * ImgUtil.java
 *
 * Created on 18 ottobre 2007, 10.18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import java.util.logging.Logger;

public class ImgUtils {

    public ImgUtils() {

    }

    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        //int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        Graphics2D g2 = null;
        if (img.getWidth() > targetWidth && img.getHeight() > targetHeight) {
            //l'immagine richiesta � pi� piccola di quella in archivio
            if (higherQuality) {
                // Use multi-step technique: start with original size, then
                // scale down in multiple passes with drawImage()
                // until the target size is reached
                w = img.getWidth();
                h = img.getHeight();
            } else {
                // Use one-step technique: scale directly from original
                // size to target size with a single drawImage() call
                w = targetWidth;
                h = targetHeight;
            }

            do {
                if (higherQuality && w > targetWidth) {
                    w /= 2;
                    if (w < targetWidth) {
                        w = targetWidth;
                    }
                }

                if (higherQuality && h > targetHeight) {
                    h /= 2;
                    if (h < targetHeight) {
                        h = targetHeight;
                    }
                }

                BufferedImage tmp = new BufferedImage(w, h, type);
                g2 = tmp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
                g2.drawImage(ret, 0, 0, w, h, null);

                ret = tmp;
            } while (w != targetWidth || h != targetHeight);
        } else {
            w = targetWidth;
            h = targetHeight;
            //l'immagine richiesta � pi� grande di quella in archivio
            BufferedImage tmp = new BufferedImage(w, h, type);
            g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);

            ret = tmp;
        }

        return ret;
    }

    static public BufferedImage applyFrame1(BufferedImage i, Color sfondo) {
        int w, h;
        w = i.getWidth();
        h = i.getHeight();

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage outj = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) out.createGraphics();
        Graphics2D g2j = (Graphics2D) outj.createGraphics();

        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //tolgo contorno
        g2.setColor(sfondo);
        g2.setStroke(new BasicStroke(9));
        g2.drawRect(4, 4, w - 9, h - 9);

        //applico ombreggiatura
//        g2.setStroke(new BasicStroke(3));
//        g2.setColor(new Color(100, 100, 100, 70));
//        g2.draw(new RoundRectangle2D.Float(6f, 6f, w-12, h-12, 20f, 20f));
//        g2.setColor(new Color(100, 100, 100, 50));
//        g2.draw(new RoundRectangle2D.Float(5f, 5f, w-10, h-10, 25f, 25f));
//        g2.setColor(new Color(100, 100, 100, 35));
//        g2.draw(new RoundRectangle2D.Float(4f, 4f, w-8, h-8, 30f, 30f));
//        g2.setColor(new Color(100, 100, 100, 25));
//        g2.draw(new RoundRectangle2D.Float(3f, 3f, w-6, h-6, 35f, 35f));
//        g2.setColor(new Color(100, 100, 100, 10));
//        g2.draw(new RoundRectangle2D.Float(2f, 2f, w-4, h-4, 40f, 40f));
//        g2.setColor(new Color(100, 100, 100, 10));
//        g2.draw(new RoundRectangle2D.Float(1f, 1f, w-2, h-2, 45f, 45f));
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(100, 100, 100, 70));
        g2.draw(new RoundRectangle2D.Float(6f, 6f, w - 12, h - 12, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 50));
        g2.draw(new RoundRectangle2D.Float(5f, 5f, w - 10, h - 10, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 35));
        g2.draw(new RoundRectangle2D.Float(4f, 4f, w - 8, h - 8, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 25));
        g2.draw(new RoundRectangle2D.Float(3f, 3f, w - 6, h - 6, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(2f, 2f, w - 3, h - 3, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(0f, 0f, w, h, 8f, 8f));

        //disegno l'immagine senza sovrascrivere l'ombra
        g2.setClip(new Rectangle(8, 8, w - 16, h - 16));
        g2.drawImage(i, 0, 0, null);
        g2.setClip(null);

        //bordo interno grigio
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(100, 100, 100));
        g2.draw(new Rectangle(10, 10, w - 20, h - 20));

        //bordo frame bianco
        g2.setStroke(new BasicStroke(4));
        g2.setColor(new Color(250, 250, 250));
        g2.draw(new Rectangle(8, 8, w - 16, h - 16));

        //stampo il copyright
        if (w > 160) {
            String copy = "\u00a9 " + Calendar.getInstance().get(Calendar.YEAR) + " - VillaTuscanyItaly.com";
            g2.setColor(new Color(0, 0, 0, 100));
            g2.setFont(new Font("Trebuchet MS", Font.BOLD, 9));
            g2.drawString(copy, w - 157, h - 15);
            g2.setColor(new Color(250, 250, 250, 100));
            g2.setFont(new Font("Trebuchet MS", Font.BOLD, 9));
            g2.drawString(copy, w - 158, h - 16);
        }

        //disegno su jpg senza trasparenza
        g2j.drawImage(out, 0, 0, null);

//        //salvo con compressione
//        ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
//        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//        iwparam.setCompressionQuality(0.5f);//Set here your compression rate
//        ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
//        ImageOutputStream ios = ImageIO.createImageOutputStream(new File("c:\\h.jpg"));//file is the file you want to create
//        iw.setOutput(ios);
//        iw.write(null, new IIOImage(outj, null, null), iwparam);
//        iw.dispose();
//        ios.close();
        return outj;
    }

    static public BufferedImage applyFrame2(BufferedImage i, Color sfondo) {
        int w, h;
        w = i.getWidth();
        h = i.getHeight();

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage outj = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) out.createGraphics();
        Graphics2D g2j = (Graphics2D) outj.createGraphics();

        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //tolgo contorno
        g2.setColor(sfondo);
        g2.setStroke(new BasicStroke(6));
        g2.drawRect(0, 0, w - 1, h - 1);

        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(100, 100, 100, 70));
        g2.draw(new RoundRectangle2D.Float(2f, 2f, w - 5, h - 5, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 35));
        g2.draw(new RoundRectangle2D.Float(1f, 1f, w - 3, h - 3, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(0f, 0f, w, h, 8f, 8f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(0f, 0f, w, h, 8f, 8f));

        //disegno l'immagine senza sovrascrivere l'ombra
        g2.setClip(new Rectangle(3, 3, w - 7, h - 7));
        g2.drawImage(i, 0, 0, null);
        g2.setClip(null);

        //bordo interno grigio
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(100, 100, 100));
        g2.draw(new Rectangle(4, 4, w - 9, h - 9));

        //bordo frame bianco
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(250, 250, 250));
        g2.draw(new Rectangle(3, 3, w - 7, h - 7));

        //disegno su jpg senza trasparenza
        g2j.drawImage(out, 0, 0, null);

        return outj;
    }

    static public BufferedImage applyFrame3(BufferedImage i, Color sfondo) {
        int w, h;
        w = i.getWidth();
        h = i.getHeight();

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage outj = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) out.createGraphics();
        Graphics2D g2j = (Graphics2D) outj.createGraphics();

        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //tolgo contorno
//        g2.setColor(sfondo);
//        g2.setStroke(new BasicStroke(6));
//        g2.drawRect(0, 0, w-1, h-1);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(100, 100, 100, 70));
        g2.draw(new RoundRectangle2D.Float(3f, 3f, w - 6, h - 6, 5f, 5f));
        g2.setColor(new Color(100, 100, 100, 35));
        g2.draw(new RoundRectangle2D.Float(2f, 2f, w - 4, h - 4, 10f, 10f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(1f, 1f, w - 2, h - 2, 15f, 15f));
        g2.setColor(new Color(100, 100, 100, 10));
        g2.draw(new RoundRectangle2D.Float(0f, 0f, w - 1, h - 1, 20f, 20f));

        //disegno l'immagine senza sovrascrivere l'ombra
        g2.setClip(new Rectangle(4, 4, w - 7, h - 7));
        g2.drawImage(i, 0, 0, null);
        g2.setClip(null);

//        //bordo interno grigio
//        g2.setStroke(new BasicStroke(1));
//        g2.setColor(new Color(100, 100, 100));
//        g2.draw(new Rectangle(4, 4, w-9, h-9));
//        //bordo frame bianco
//        g2.setStroke(new BasicStroke(1));
//        g2.setColor(new Color(250, 250, 250));
//        g2.draw(new Rectangle(3, 3, w-7, h-7));
        //disegno su jpg senza trasparenza
        g2j.drawImage(out, 0, 0, null);

        return outj;
    }

    public static Color getMixedColor(Color c1, float pct1, Color c2, float pct2) {
        float[] clr1 = c1.getComponents(null);
        float[] clr2 = c2.getComponents(null);
        for (int i = 0; i < clr1.length; i++) {
            clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);
        }
        return new Color(clr1[0], clr1[1], clr1[2], clr1[3]);
    }

    // metodo Nearest il metodo restituisce l'immagine ridimensionata velocemente ma con minor qualit�
    public static BufferedImage resizeSpeed(BufferedImage img, Dimension dim) {
        return resizeSpeed(img, dim.width, dim.height);
    }

    public static BufferedImage resizeSpeed(BufferedImage img, int w, int h) {
        return getScaledInstance(img, w, h, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
    }

    // metodo One-Step Bilinear il metodo restituisce l'immagine ridimensionata in tempo medio con qualit� media
    public static BufferedImage resizeMedium(BufferedImage img, Dimension dim) {
        return resizeMedium(img, dim.width, dim.height);
    }

    public static BufferedImage resizeMedium(BufferedImage img, int w, int h) {
        return getScaledInstance(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
    }

    // metodo Multi-Step Bilinear il metodo restituisce l'immagine ridimensionata pi� lentamente con qualit� migliore
    public static BufferedImage resizeQuality(BufferedImage img, Dimension dim) {
        return resizeQuality(img, dim.width, dim.height);
    }

    public static BufferedImage resizeQuality(BufferedImage img, int w, int h) {
        return getScaledInstance(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
    }

    public static Dimension getDimension(int xorig, int yorig, int xdest, int ydest) {
        float cx = (float) xdest / (float) xorig;
        float cy = (float) ydest / (float) yorig;
        Dimension dret = new Dimension();
        if (cx > cy) {
            float crx = (float) yorig / ydest;
            int w = (int) ((float) xorig / crx);
            dret.setSize(w, ydest);
        } else {
            float cry = (float) xorig / xdest;
            int h = (int) ((float) yorig / cry);
            dret.setSize(xdest, h);
        }
        if (dret.getHeight() < 1) {
            dret.setSize(dret.getWidth(), 1);
        }
        if (dret.getWidth() < 1) {
            dret.setSize(1, dret.getHeight());
        }
        return dret;
    }

    public static void writeJpeg(BufferedImage bufi, String file, float quality) throws IOException {
        ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwparam.setCompressionQuality(quality);
        ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
        File filef = new File(file);
        filef.getParentFile().mkdirs();
        ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(filef));
        iw.setOutput(ios);
        iw.write(null, new IIOImage(bufi, null, null), iwparam);
        ios.close();
    }

    public static byte[] writeJpegByteArray(BufferedImage bufi, float quality) throws IOException {
        ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwparam.setCompressionQuality(quality);
        ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream bytea = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(bytea);
        iw.setOutput(ios);
        iw.write(null, new IIOImage(bufi, null, null), iwparam);
        ios.close();
        return bytea.toByteArray();
    }

    static int conta_getImage = 0;

    synchronized static public BufferedImage getImage(String path, int w, int h, String tipo_resize, boolean crop, double z, String path_cache) throws Throwable {
        conta_getImage++;
        System.out.println("ImgUtils.getImage start " + conta_getImage);
        Logger log = Logger.getLogger(ImgUtils.class.getName());
        long t1 = System.currentTimeMillis();
        try {
            //parametri via get
            //n = percorso immagine
            //w e h = dimensioni
            //tr = tipo resize > S speed / M medium / Q quality
            //c = crop 1 per si
            //z = fattore di compressione (0.0 peggiore 1.0 migliore) - default 0.8

            if (tipo_resize == null || tipo_resize.trim().length() == 0) {
                tipo_resize = "Q";
            }
            if (z == 0) {
                z = 0.8d;
            }

            File fileSorgente = new File(path);
            if (!fileSorgente.exists()) {
                log.info("il file " + fileSorgente + " non esiste");
                return null;
            }

            //controllo se esiste gi? in cache e controllo le date
            String nomeFileNormalizzato = StringUtils.replace(fileSorgente.getParentFile().getAbsolutePath(), ":", "") + "/" + fileSorgente.getName();
            String sfileCache = path_cache + "/" + nomeFileNormalizzato;
            File fileCache = new File(sfileCache);

            String fparams = "_w" + w + "_h" + h + "_tr" + tipo_resize + "_c" + crop + "_z" + StringUtils.replace(String.valueOf(z), ".", "_");
            String snewFileCache = fileCache.getParent() + "/" + FilenameUtils.getBaseName(fileCache.getPath()) + fparams + "." + FilenameUtils.getExtension(fileCache.getName());
            fileCache = new File(snewFileCache);
            log.config("fileCache:" + fileCache);

            if (fileCache.exists() && fileCache.lastModified() >= fileSorgente.lastModified()) {
                //forwardo a cache
                log.config("USATA CACHE");
                return ImageIO.read(fileCache);
            }

            BufferedImage bufi = null;
            if ( fileSorgente.getName().toLowerCase().endsWith(".jpg") || fileSorgente.getName().toLowerCase().endsWith(".jpeg") ) {
                // JPEGCoded is deprecated from JDK7
                // bufi = JPEGCodec.createJPEGDecoder(new FileInputStream(fileSorgente)).decodeAsBufferedImage();
                
                bufi = ImageIO.read(fileSorgente);
            } else {
                bufi = ImageIO.read(fileSorgente);
            }
            if (bufi == null) {
                return null;
            }

            int offx = 0;
            int offy = 0;
            int neww = 0;
            int newh = 0;
            int origw = bufi.getWidth();
            int origh = bufi.getHeight();
            if (w == 0 && h == 0) {
                w = bufi.getWidth();
                h = bufi.getHeight();
                neww = w;
                newh = h;
            } else if (w > 0 && h == 0) {
                double cr = (double) bufi.getWidth() / w;
                h = (int) ((double) bufi.getHeight() / cr);
                neww = w;
                newh = h;
            } else if (h > 0 && w == 0) {
                double cr = (double) bufi.getHeight() / h;
                w = (int) ((double) bufi.getWidth() / cr);
                neww = w;
                newh = h;
            } else {
                if (crop) {
                    double cr = (double) bufi.getWidth() / (double) bufi.getHeight();
                    double cd = (double) w / (double) h;
                    if ((double) bufi.getWidth() / (double) w < (double) bufi.getHeight() / (double) h) {
                        log.config("crop verticale");
                        //crop vert.
                        neww = (int) w;
                        newh = (int) (w / cr);
                        offy = (newh - h) / 2;
                    } else {
                        log.config("crop oriz");
                        //crop oriz.
                        neww = (int) (h * cr);
                        newh = (int) h;
                        offx = (neww - w) / 2;
                    }
                } else {
                    neww = w;
                    newh = h;
                }
            }

            log.config("resize dim. orig.:" + origw + "x" + origh + " nuova:" + neww + "x" + newh);

            if (tipo_resize.equalsIgnoreCase("S")) {
                bufi = ImgUtils.resizeSpeed(bufi, neww, newh);
            } else if (tipo_resize.equalsIgnoreCase("M")) {
                bufi = ImgUtils.resizeMedium(bufi, neww, newh);
            } else if (tipo_resize.equalsIgnoreCase("Q")) {
                bufi = ImgUtils.resizeQuality(bufi, neww, newh);
            }
            if (crop) {
                log.config("crop dim. orig.:" + origw + "x" + origh + " nuova:" + neww
                        + "x" + newh + " crop: offx=" + offx + " offy=" + offy + " w="
                        + (w) + " h=" + (h));
                BufferedImage bufic = bufi.getSubimage(offx, offy, w, h);
                bufi = bufic;
            }

            //salvo cache
            ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality((float) z);
            ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
            try {
                fileCache.getParentFile().mkdirs();
                ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(fileCache));
                iw.setOutput(ios);
                iw.write(null, new IIOImage(bufi, null, null), iwparam);
                ios.close();
            } catch (Exception err0) {
                log.config(String.format("Errore su salvataggio cache miniatura per %s / err: %d", path, err0));
            }

//            //invio con compressione
//            response.setHeader("Cache-Control", "no-cache");    //HTTP 1.1
//            response.setHeader("Cache-Control", "no-store");    //HTTP 1.1
//            response.setHeader("Pragma", "no-cache");           //HTTP 1.0
//            response.setHeader("Expires", "0");                 //prevents caching at the proxy server
//            response.setContentType("image/jpeg");
//            ImageOutputStream ios = ImageIO.createImageOutputStream(response.getOutputStream());
//            iw.setOutput(ios);
//            iw.write(null, new IIOImage(bufi, null, null), iwparam);
//            iw.dispose();
//            ios.close();
            //forwardo a cache
            log.config("NON USATA CACHE");
            return ImageIO.read(fileCache);
        } catch (Exception err) {
            err.printStackTrace();
            log.fine(String.format("Errore su miniatura per %s / err: %d", path, err));
            throw err;
        } catch (Throwable tr) {
            tr.printStackTrace();
            throw tr;
        } finally {
            long t2 = System.currentTimeMillis();
            System.out.println("ImgUtils.getImage stop " + conta_getImage);
            log.info("tempo millis:" + (t2 - t1));
        }
    }
}
