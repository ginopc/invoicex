/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex;

import gestioneFatture.main;
import javafx.embed.swing.JFXPanel;

/**
 *
 * @author Marco
 */
public class MainJFX {

    public static JFXPanel jfxpanel = null;

    public MainJFX() {
        jfxpanel = new javafx.embed.swing.JFXPanel();
        jfxpanel.setSize(1, 1);
        jfxpanel.setVisible(true);
        main.getPadrePanel().getDesktopPane().add(jfxpanel);
        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run() {
                javafx.scene.Group root = new javafx.scene.Group();
                javafx.scene.Scene scene = new javafx.scene.Scene(root, javafx.scene.paint.Color.ALICEBLUE);
                javafx.scene.text.Text text = new javafx.scene.text.Text();
                text.setX(20);
                text.setY(20);
                text.setText("Welcome JavaFX!");
                root.getChildren().add(text);
                jfxpanel.setScene(scene);
            }
        });
    }

}
