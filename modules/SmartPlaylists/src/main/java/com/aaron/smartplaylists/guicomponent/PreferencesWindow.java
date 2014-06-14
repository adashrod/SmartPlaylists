package com.aaron.smartplaylists.guicomponent;

import com.aaron.smartplaylists.FileListSortType;
import com.aaron.smartplaylists.guicomponent.event.PreferencesSaveListener;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.EventListener;
import java.util.List;
import java.util.Properties;

/**
 * A popup dialog for configuring the application. Preferences are saved to a properties file
 */
public class PreferencesWindow extends JDialog {
    private static final Logger logger = Logger.getLogger(PreferencesWindow.class);

    private final static String configFilename = "config.properties";

    private final JCheckBox showHiddenFilesCheckBox = new JCheckBox();
    private final JCheckBox showHiddenDirectoriesCheckBox = new JCheckBox();
    private final JCheckBox showBackupFilesCheckBox = new JCheckBox();
    private final JCheckBox autoPopMessageWindowCheckBox = new JCheckBox();
    private final JRadioButton autoSortByNameRadioButton = new JRadioButton();
    private final JRadioButton autoSortByTypeRadioButton = new JRadioButton();
    private static final String showHiddenFilesPropName = "showHiddenFiles";
    private static final String showHiddenDirectoriesPropName = "showHiddenDirectories";
    private static final String showBackupFilesPropName = "showBackupFiles";
    private static final String autoPopMessageWindowPropName = "autoPopMessageWindow";
    private static final String autoSortPropName = "autoSort";
    private static final int showHiddenFilesIndex = 0;
    private static final int showHiddenDirectoriesIndex = 1;
    private static final int showBackupFilesIndex = 2;
    private static final int autoPopMessageWindowIndex = 3;
    private static final int autoSortByNameIndex = 4;
    private static final int autoSortByTypeIndex = 5;

    private final JButton cancelButton = new JButton();
    private final JButton saveButton = new JButton();

    private final List<EventListener> eventListeners = Lists.newArrayList();

    /**
     * What the states of the buttons were the last time they were saved
     */
    private final BitSet oldButtonStates = new BitSet(6);
    /**
     * The states of the buttons as they currently are in the UI
     */
    private final BitSet currentButtonStates = new BitSet(6);

    private final ActionListener optionButtonClickListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            saveButtonStatesToBitSet(currentButtonStates);
            saveButton.setEnabled(!oldButtonStates.equals(currentButtonStates));
        }
    };

    private final ActionListener cancelButtonClickListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            loadButtonStatesFromBitSet(oldButtonStates);
            setVisible(false);
        }
    };

    private final ActionListener saveButtonClickListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            persistConfig();
            setVisible(false);
        }
    };

    /**
     * takes the states of the buttons themselves and saves them to a BitSet
     * @param bitSet the target BitSet to save state into
     */
    private void saveButtonStatesToBitSet(final BitSet bitSet) {
        bitSet.set(showHiddenFilesIndex, showHiddenFilesCheckBox.isSelected());
        bitSet.set(showHiddenDirectoriesIndex, showHiddenDirectoriesCheckBox.isSelected());
        bitSet.set(showBackupFilesIndex, showBackupFilesCheckBox.isSelected());
        bitSet.set(autoPopMessageWindowIndex, autoPopMessageWindowCheckBox.isSelected());
        bitSet.set(autoSortByNameIndex, autoSortByNameRadioButton.isSelected());
        bitSet.set(autoSortByTypeIndex, autoSortByTypeRadioButton.isSelected());
    }

    /**
     * Takes the states of buttons from bitSet and sets the buttons to selected/de-selected accordingly
     * @param bitSet new states of the buttons
     */
    private void loadButtonStatesFromBitSet(final BitSet bitSet) {
        showHiddenFilesCheckBox.setSelected(bitSet.get(showHiddenFilesIndex));
        showHiddenDirectoriesCheckBox.setSelected(bitSet.get(showHiddenDirectoriesIndex));
        showBackupFilesCheckBox.setSelected(bitSet.get(showBackupFilesIndex));
        autoPopMessageWindowCheckBox.setSelected(bitSet.get(autoPopMessageWindowIndex));
        autoSortByNameRadioButton.setSelected(bitSet.get(autoSortByNameIndex));
        autoSortByTypeRadioButton.setSelected(bitSet.get(autoSortByTypeIndex));
    }

    /**
     * When preferences have been "saved", saves the current button states (currentButtonStates) into oldButtonStates and also
     * saves that info to a file
     */
    private void persistConfig() {
        // write current button states in currentButtonStates into oldButtonStates
        oldButtonStates.clear();
        oldButtonStates.or(currentButtonStates);
        // save properties to a file
        final Properties properties = new Properties();
        properties.setProperty(showHiddenFilesPropName, Boolean.toString(showHiddenFilesCheckBox.isSelected()));
        properties.setProperty(showHiddenDirectoriesPropName, Boolean.toString(showHiddenDirectoriesCheckBox.isSelected()));
        properties.setProperty(showBackupFilesPropName, Boolean.toString(showBackupFilesCheckBox.isSelected()));
        properties.setProperty(autoPopMessageWindowPropName, Boolean.toString(autoPopMessageWindowCheckBox.isSelected()));
        properties.setProperty(autoSortPropName, autoSortByNameRadioButton.isSelected() ? "name" : "type");

        final FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(configFilename);
            properties.store(fileWriter, null);
            fileWriter.close();
            fireOnSave();
        } catch (final IOException ioe) {
            logger.error(ioe.getMessage());
        }
    }

    /**
     * Loads config from a file and sets the states of the buttons accordingly
     */
    private void loadConfig() {
        final Properties properties = new Properties();
        final FileReader fileReader;
        try {
            fileReader = new FileReader(configFilename);
            properties.load(fileReader);
        } catch (final FileNotFoundException ignored) {} catch (final IOException ioe) {
            logger.error(ioe.getMessage());
            return;
        }
        showHiddenFilesCheckBox.setSelected(Boolean.parseBoolean(properties.getProperty(showHiddenFilesPropName, "false")));
        showHiddenDirectoriesCheckBox.setSelected(Boolean.parseBoolean(properties.getProperty(showHiddenDirectoriesPropName, "false")));
        showBackupFilesCheckBox.setSelected(Boolean.parseBoolean(properties.getProperty(showBackupFilesPropName, "false")));
        autoPopMessageWindowCheckBox.setSelected(Boolean.parseBoolean(properties.getProperty(autoPopMessageWindowPropName, "true")));
        final boolean isName = properties.getProperty(autoSortPropName, "name").equals("name");
        autoSortByNameRadioButton.setSelected(isName);
        autoSortByTypeRadioButton.setSelected(!isName);
        saveButtonStatesToBitSet(oldButtonStates);
        saveButtonStatesToBitSet(currentButtonStates);
    }

    /**
     * Pops the dialog and sets the button states
     * @param parent the parent frame of this dialog
     */
    public void display(final Component parent) {
        loadButtonStatesFromBitSet(oldButtonStates);
        setLocationRelativeTo(parent);
        setVisible(true);
        saveButton.setEnabled(false);
    }

    public boolean isShowHiddenFiles() {
        return oldButtonStates.get(showHiddenFilesIndex);
    }

    public boolean isShowHiddenDirectories() {
        return oldButtonStates.get(showHiddenDirectoriesIndex);
    }

    public boolean isShowBackupFiles() {
        return oldButtonStates.get(showBackupFilesIndex);
    }

    public boolean isAutoPopMessageWindow() {
        return oldButtonStates.get(autoPopMessageWindowIndex);
    }

    public FileListSortType getFileListSortType() {
        return oldButtonStates.get(autoSortByNameIndex) ? FileListSortType.ASCENDING_NAME : FileListSortType.ASCENDING_TYPE;
    }

    public void addSaveListener(final PreferencesSaveListener preferencesSaveListener) {
        eventListeners.add(preferencesSaveListener);
    }

    public void removeSaveListener(final PreferencesSaveListener preferencesSaveListener) {
        eventListeners.remove(preferencesSaveListener);
    }

    private void fireOnSave() {
        for (final EventListener listener: eventListeners) {
            if (listener instanceof PreferencesSaveListener) {
                ((PreferencesSaveListener) listener).onSave();
            }
        }
    }

    private void createGui() {
        showHiddenFilesCheckBox.setText("show hidden files");
        showHiddenFilesCheckBox.addActionListener(optionButtonClickListener);
        showHiddenDirectoriesCheckBox.setText("show hidden directories (requires restart)");
        showHiddenDirectoriesCheckBox.addActionListener(optionButtonClickListener);
        showBackupFilesCheckBox.setText("show backup files");
        showBackupFilesCheckBox.addActionListener(optionButtonClickListener);
        autoPopMessageWindowCheckBox.setText("automatically display message window after conversion");
        autoPopMessageWindowCheckBox.addActionListener(optionButtonClickListener);
        final ButtonGroup radioButtonGroup = new ButtonGroup();
        autoSortByNameRadioButton.setText("name");
        autoSortByNameRadioButton.addActionListener(optionButtonClickListener);
        autoSortByTypeRadioButton.setText("type");
        autoSortByTypeRadioButton.addActionListener(optionButtonClickListener);
        radioButtonGroup.add(autoSortByNameRadioButton);
        radioButtonGroup.add(autoSortByTypeRadioButton);

        final JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.add(showHiddenFilesCheckBox);
        checkboxPanel.add(showHiddenDirectoriesCheckBox);
        checkboxPanel.add(showBackupFilesCheckBox);
        checkboxPanel.add(autoPopMessageWindowCheckBox);

        final JPanel radioButtonPanel = new JPanel();
        radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.Y_AXIS));
        radioButtonPanel.add(new JLabel("by default, sort files by:"));
        radioButtonPanel.add(autoSortByNameRadioButton);
        radioButtonPanel.add(autoSortByTypeRadioButton);

        final JPanel optionsPanel = new JPanel();
        optionsPanel.add(checkboxPanel);
        optionsPanel.add(radioButtonPanel);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(cancelButtonClickListener);
        saveButton.setText("Save");
        saveButton.addActionListener(saveButtonClickListener);
        saveButton.setEnabled(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(optionsPanel);
        mainPanel.add(buttonPanel);

        getContentPane().add(mainPanel);
        pack();
        setVisible(false);

        loadConfig();
        saveButtonStatesToBitSet(oldButtonStates);
    }

    public PreferencesWindow(final JFrame parent) {
        super(parent);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGui();
            }
        });
    }
}
