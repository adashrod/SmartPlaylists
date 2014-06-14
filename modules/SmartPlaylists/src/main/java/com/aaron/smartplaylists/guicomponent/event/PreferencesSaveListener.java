package com.aaron.smartplaylists.guicomponent.event;

import java.util.EventListener;

/**
 * A PreferencesSaveListener can be registered on a {@link com.aaron.smartplaylists.guicomponent.PreferencesWindow} to
 * receive events when the preferences window is closed with the save button.
 */
public interface PreferencesSaveListener extends EventListener {
    void onSave();
}
