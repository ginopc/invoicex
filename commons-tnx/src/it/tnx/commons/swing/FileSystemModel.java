/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.swing;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mceccarelli
 */
public class FileSystemModel implements TreeModel {

    private String root; // The root identifier
    private boolean onlyFolders = false;
    private Vector listeners; // Declare the listeners vector
    private HashMap cache = new HashMap();
    FileFilter filtro = null;

    public FileSystemModel() {
        root = System.getProperty("user.home");
        File tempFile = new File(root);
        root = tempFile.getParent();
        listeners = new Vector();
    }

    public FileSystemModel(String rootFile, File startPath, boolean onlyFolders) {
        this(rootFile, startPath, onlyFolders, null);
    }

    public FileSystemModel(String rootFile, File startPath, boolean onlyFolders, FileFilter filtro) {
        if (rootFile != null) {
            this.root = rootFile;
        } else {
            this.root = null;
        }
        this.onlyFolders = onlyFolders;
        this.filtro = filtro;
        listeners = new Vector();
    }

    public Object getRoot() {
        if (root == null) {
            return "Computer";
        }
        return (new File(root));
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof String) {
            return File.listRoots()[index];
        } else {
            File directory = (File) parent;
            File[] directoryMembers = null;
            if (cache.containsKey(parent)) {
                directoryMembers = (File[]) cache.get(parent);
            } else {
                System.out.println("parent1:" + parent);
                directoryMembers = directory.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        if (filtro != null) {
                            if (!filtro.accept(pathname)) return false;
                        }
                        if (onlyFolders) {
                            if (pathname.isFile()) return false;
                            return true;
                        } else {
                            return true;
                        }
                    }
                });
                Arrays.sort(directoryMembers);
                cache.put(parent, directoryMembers);
            }
            return directoryMembers[index];
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof String) {
            return File.listRoots().length;
        } else {
            final File fileSystemMember = (File) parent;
            if (fileSystemMember.isDirectory()) {
                File[] directoryMembers = null;
                if (cache.containsKey(parent)) {
                    directoryMembers = (File[]) cache.get(parent);
                } else {
                    System.out.println("parent2:" + parent);
                    directoryMembers = fileSystemMember.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            if (filtro != null) {
                                if (!filtro.accept(pathname)) return false;
                            }
                            if (onlyFolders) {
                                if (pathname.isFile()) return false;
                                if (fileSystemMember.getAbsolutePath().toUpperCase().startsWith("T")) {
                                    File test_locale = new File("c" + StringUtils.substring(pathname.getAbsolutePath(), 1));
                                    if (!test_locale.exists()) return false;
                                }
                                return true;
                            } else {
                                return true;
                            }
                        }
                    });
                    Arrays.sort(directoryMembers);
                    cache.put(parent, directoryMembers);
                }
                return directoryMembers.length;
            } else {
                return 0;
            }
        }
    }

    public int getIndexOfChild(Object parent, Object child) {
        File directory = (File) parent;
        File directoryMember = (File) child;
        String[] directoryMemberNames = directory.list();
        int result = -1;

        for (int i = 0; i < directoryMemberNames.length; ++i) {
            if (directoryMember.getName().equals(directoryMemberNames[i])) {
                result = i;
                break;
            }
        }

        return result;
    }

    public boolean isLeaf(Object node) {
        if (node instanceof String) {
            return false;
        }
        return ((File) node).isFile();
    }

    public void addTreeModelListener(TreeModelListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null) {
            listeners.removeElement(l);
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // Does Nothing!
    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesRemoved(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesRemoved(e);
        }

    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesChanged(e);
        }

    }

    public void fireTreeStructureChanged(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeStructureChanged(e);
        }

    }
}