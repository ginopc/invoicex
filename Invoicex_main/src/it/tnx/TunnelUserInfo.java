/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import gestioneFatture.iniFileProp;
import gestioneFatture.main;
import it.tnx.commons.SwingUtils;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author Marco
 */
public class TunnelUserInfo implements UserInfo, UIKeyboardInteractive {
    boolean tunnel_force = false;
    
    TunnelUserInfo(boolean tunnel_force) {
        this.tunnel_force = tunnel_force;
    }

    //UserInfo
    public String getPassphrase() {
        if (tunnel_force) {
            return iniFileProp.decrypt("kY6SP7ixn5eFIuVwZxaYnA==");
        } else {
            return main.fileIni.getValueCifrato("db", "ssh_password");
        }
    }

    public String getPassword() {
        if (tunnel_force) {
            return iniFileProp.decrypt("kY6SP7ixn5eFIuVwZxaYnA==");
        } else {
            return main.fileIni.getValueCifrato("db", "ssh_password");
        }
    }

    public boolean promptPassword(String message) {
        System.err.println("ssh promptPassword message: " + message);
        return true;
    }

    public boolean promptPassphrase(String message) {
        System.err.println("ssh promptPassphrase message: " + message);
        return false;
    }

    public boolean promptYesNo(String message) {
        System.err.println("ssh promptYesNo message: " + message);
        if (message.startsWith("The authenticity of host '")) {
            return true;
        }
        return SwingUtils.showYesNoMessage(main.getPadreFrame() == null ? main.splash : main.getPadreFrame(), message, "Invoicex - SSH");
    }

    public void showMessage(String message) {
        System.err.println("ssh showMessage message: " + message);
    }

    //UIKeyboardInteractive
    String passwd;
    JTextField passwordField = (JTextField) new JPasswordField(20);
    Container panel;
    final GridBagConstraints gbc
            = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST,
                    GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0);

    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
        System.out.println("destination = " + destination);
        System.out.println("name = " + name);
        System.out.println("instruction = " + instruction);
        for (String s : prompt) {
            System.out.println("prompt = " + s);
        }
        for (boolean b : echo) {
            System.out.println("echo = " + b);
        }

        if (prompt.length > 0 && prompt[0].startsWith("Password for")) {
            return new String[] {tunnel_force ? iniFileProp.decrypt("kY6SP7ixn5eFIuVwZxaYnA==") : main.fileIni.getValueCifrato("db", "ssh_password")};
        } else {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if (echo[i]) {
                    texts[i] = new JTextField(20);
                } else {
                    texts[i] = new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            if (JOptionPane.showConfirmDialog(null, panel,
                    destination + ": " + name,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.OK_OPTION) {
                String[] response = new String[prompt.length];
                for (int i = 0; i < prompt.length; i++) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null;  // cancel
            }
        }

    }
}
