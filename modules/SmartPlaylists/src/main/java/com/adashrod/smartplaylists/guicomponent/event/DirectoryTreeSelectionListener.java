package com.adashrod.smartplaylists.guicomponent.event;

import java.io.File;
import java.util.EventListener;

/**
 * A DirectoryTreeSelectionListener can be registered on a {@link com.adashrod.smartplaylists.guicomponent.DirectoryTree}
 * to receive events when a new file is selected on the tree.
 */
public interface DirectoryTreeSelectionListener extends EventListener {
    /**
     * Fired whenever a file on the tree is selected
     * @param file the file that was selected
     */
    void onSelect(File file);
}
