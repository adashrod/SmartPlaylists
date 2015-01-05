package com.aaron.smartplaylists.guicomponent;

import com.aaron.smartplaylists.FileComparator;
import com.aaron.smartplaylists.InvalidPathException;
import com.aaron.smartplaylists.guicomponent.event.DirectoryTreeSelectionListener;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;

/**
 * DirectoryTree is a specialized JTree that displays a file system. It initially displays the root directory and lazy
 * loads child directories only when a directory is expanded. Selecting a file on the tree will fire an event to registered
 * listeners.
 * todo: support windows file systems, especially multiple drives
 * support trees whose root isn't the file system root?
 */
public class DirectoryTree extends JTree {
    private static final File defaultSelfDirectory = new File(".");
    private static final File defaultRootFile = new File("/");
    private static final DefaultMutableTreeNode defaultRootNode = new DefaultMutableTreeNode(defaultRootFile);

    // maybe put these somewhere else so that they can be used elsewhere
    public static final int FILES_ONLY = 0;
    public static final int DIRECTORIES_ONLY = 1;
    public static final int FILES_AND_DIRECTORIES = 2;

    private static final Comparator<File> treeSortFunction = FileComparator.name().caseInsensitive();

    private int filterMode = DIRECTORIES_ONLY;
    private boolean showHiddenFiles = true;
    private boolean showHiddenDirectories = true;
    private boolean showBackupFiles = true;

    static {
        // the dummy selfNode is here so that the root node is not treated as a leaf and will therefore be expandable
        defaultRootNode.add(new DefaultMutableTreeNode(defaultSelfDirectory));
    }

    private final Collection<EventListener> listeners = new ArrayList<>();

    public DirectoryTree() {
        super(defaultRootNode);
        setup();
    }

    public DirectoryTree(final File rootDir) {
        super(new DefaultMutableTreeNode(rootDir));
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.add(new DefaultMutableTreeNode(defaultSelfDirectory));
        setup();
    }

    private void setup() {
        collapseRow(0);

        // this is a convenience so that listeners can easily get a reference to the file that was selected
        addTreeSelectionListener((final TreeSelectionEvent event) -> {
            final File selectedDirectory = (File) ((DefaultMutableTreeNode) event.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();
            fireOnSelect(selectedDirectory);
        });
        addTreeWillExpandListener(new TreeWillExpandListener() {
            public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
                lazyLoadChildren((DefaultMutableTreeNode) event.getPath().getLastPathComponent());
                SwingUtilities.invokeLater(DirectoryTree.this::updateUI);
            }
            public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {}
        });
        setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
                    final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                final Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf,
                    row, hasFocus);
                final File file = (File) node.getUserObject();
                final String name = file.getName();
                setText(name.isEmpty() ? "/" : name);
                if (file.isDirectory()) {
                    setIcon(expanded ? getOpenIcon() : getClosedIcon());
                }
                return component;
            }
        });
    }

    private void lazyLoadChildren(final DefaultMutableTreeNode node) {
        @SuppressWarnings("unchecked") final List<DefaultMutableTreeNode> childNodes = Collections.list(node.children());
        if (childNodes.size() == 1 && childNodes.get(0).getUserObject().equals(defaultSelfDirectory)) {
            // remove the dummy self directory child and lazy load the real children
            node.removeAllChildren();
        } else {
            // children are already loaded; do nothing
            return;
        }
        final File directoryToExpand = (File) node.getUserObject();
        final File[] childFiles = directoryToExpand.listFiles((final File file) -> {
            if (filterMode == DIRECTORIES_ONLY) {
                return file.isDirectory() && (showHiddenDirectories || !file.getName().startsWith("."));
            } else { // FILES_AND_DIRECTORIES
                if (file.isDirectory()) {
                    return showHiddenDirectories || !file.getName().startsWith(".");
                } else {
                    if (showHiddenFiles) {
                        return showBackupFiles || !file.getName().endsWith("~");
                    } else {
                        if (showBackupFiles) {
                            return !file.getName().startsWith(".");
                        } else {
                            return !file.getName().startsWith(".") && !file.getName().endsWith("~");
                        }
                    }
                }
            }
        });
        // this happens when trying to read a directory without sufficient permissions
        if (childFiles == null) {
            return;
        }
        Arrays.sort(childFiles, treeSortFunction);
        for (final File file: childFiles) {
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
            // see note above about leaf nodes
            if (file.isDirectory()) {
                childNode.add(new DefaultMutableTreeNode(defaultSelfDirectory));
            }
            node.add(childNode);
        }
    }

    /**
     * Attempts to set the selected file on the directory tree to newPath. newPath must be an absolute path. If
     * newPath can't be resolved to a file on the tree, then an InvalidPathException will be thrown
     * @param newPath a path to a file
     * @throws InvalidPathException newPath couldn't be found or wasn't absolute
     * @throws IOException problem getting the canonical path of the specified path
     */
    public void setPath(final String newPath) throws InvalidPathException, IOException {
        if (newPath == null || newPath.isEmpty()) {
            throw new InvalidPathException("Empty string is not allowed");
        }
        final File destination = new File(newPath);
        final String path = destination.getCanonicalPath();
        final String[] parts = path.split("/");
        if (parts.length > 0 && !"".equals(parts[0])) {
            throw new InvalidPathException("Path must start with /");
        }
        DefaultMutableTreeNode current = (DefaultMutableTreeNode) treeModel.getRoot();
        final Collection<Object> nodes = new ArrayList<>();
        nodes.add(current);
        for (int i = 1; i < parts.length; i++) {
            lazyLoadChildren(current);
            @SuppressWarnings("unchecked") final List<DefaultMutableTreeNode> children = Collections.list(current.children());
            boolean found = false;
            for (final DefaultMutableTreeNode child: children) {
                final File dir = (File) child.getUserObject();
                if (parts[i].equals(dir.getName())) {
                    current = child;
                    nodes.add(current);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidPathException("Couldn't find path: " + newPath);
            }
        }
        final TreePath treePath = new TreePath(nodes.toArray());
        setSelectionPath(treePath);
        scrollPathToVisible(treePath);
    }

    /**
     * Sets the directory tree to display either directories only or both files and directories
     * @param filterMode see {@link DirectoryTree#DIRECTORIES_ONLY}, {@link DirectoryTree#FILES_AND_DIRECTORIES}
     */
    public void setFilterMode(final int filterMode) {
        if (filterMode != DIRECTORIES_ONLY && filterMode != FILES_AND_DIRECTORIES) {
            throw new IllegalArgumentException("Only \"directories only\" and \"files and directories\" modes are supported");
        }
        this.filterMode = filterMode;
    }

    /**
     * Adds a tree selection listener to the directory tree. Events are fired whenever a file in the tree is clicked.
     * Events are also fired after a call to setPath, since setPath will ultimately select a file to update the view.
     * @param listener the listener to register
     */
    public void addTreeSelectionListener(final EventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered listener.
     * @param listener the listener to remove
     */
    public void removeTreeSelectionListener(final EventListener listener) {
        listeners.remove(listener);
    }

    public DirectoryTree setShowHiddenFiles(final boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
        return this;
    }

    public DirectoryTree setShowHiddenDirectories(final boolean showHiddenDirectories) {
        this.showHiddenDirectories = showHiddenDirectories;
        return this;
    }

    public DirectoryTree setShowBackupFiles(final boolean showBackupFiles) {
        this.showBackupFiles = showBackupFiles;
        return this;
    }

    private void fireOnSelect(final File selectedDirectory) {
        listeners.stream()
            .filter((final EventListener listener) -> {
                return listener instanceof DirectoryTreeSelectionListener;
            }).forEach((final EventListener listener) ->
                    ((DirectoryTreeSelectionListener) listener).onSelect(selectedDirectory));
    }
}
