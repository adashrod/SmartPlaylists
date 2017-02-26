package com.adashrod.smartplaylists.guicomponent.event;

import java.util.EventListener;

/**
 * A PreferencesSaveListener can be registered on a {@link com.adashrod.smartplaylists.guicomponent.PreferencesWindow} to
 * receive events when the preferences window is closed with the save button.
 */
public interface PreferencesSaveListener extends EventListener {
    void onSave();
}
