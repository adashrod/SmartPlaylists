package com.aaron.smartplaylists;

import com.aaron.smartplaylists.api.ConverterApi;
import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.guicomponent.PreferencesWindow;
import com.aaron.smartplaylists.guicomponent.DirectoryTree;
import com.aaron.smartplaylists.guicomponent.MessageWindow;
import com.aaron.smartplaylists.guicomponent.event.DirectoryTreeSelectionListener;
import com.aaron.smartplaylists.guicomponent.event.PreferencesSaveListener;
import com.aaron.smartplaylists.playlists.GmmpSmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GUI version of the program. The main upside to using the GUI as opposed to the CLI is that this supports
 * selecting a batch of files and converting them all at once.
 * todo: add more detail to error messages for context so that they're understandable without the FQ class name
 */
public class GuiConverter extends JFrame {

    private final JTextField currentDirectoryField = new JTextField();
    private final DirectoryTree directoryTree = new DirectoryTree();
    private final JButton sortByNameButton = new JButton();
    private final JButton sortByTypeButton = new JButton();
    private final DefaultListModel<File> fileListModel = new DefaultListModel<>();
    private final JList<File> fileList = new JList<>(fileListModel);
    private final JRadioButton xbmc11RadioButton = new JRadioButton();
    private final JRadioButton xbmcRadioButton = new JRadioButton();
    private final JRadioButton gmmpRadioButton = new JRadioButton();
    private final JTextField outputFilenameFormatField = new JTextField("$1_new", 20);
    private final JButton convertButton = new JButton();
    private final JButton showMessageWindowButton = new JButton();
    private final JButton showConfigWindowButton = new JButton();

    private final MessageWindow messageWindow = new MessageWindow(this);
    private final PreferencesWindow preferencesWindow = new PreferencesWindow(this);

    private FileListSortType fileListSortType = FileListSortType.ASCENDING_NAME;
    private static final Comparator<File> sortFilesByName = FileComparator.name().ascending().caseInsensitive();
    private static final Comparator<File> sortFilesByType = FileComparator.type().ascending().caseInsensitive();

    private OutputType outputType;
    private static final Map<OutputType, Class<? extends FormattedSmartPlaylist>> outputTypeMap = new HashMap<>();
    static {
        outputTypeMap.put(OutputType.XBMC11, XbmcV11SmartPlaylist.class);
        outputTypeMap.put(OutputType.XBMC12, XbmcV12SmartPlaylist.class);
        outputTypeMap.put(OutputType.GMMP, GmmpSmartPlaylist.class);
    }

    private final ConverterApi converterApi = new ConverterApi();

    /**
     * Attempts to update the selected directory in the tree with whatever has been typed into the text field. If the
     * text doesn't match an existing directory, the color is changed to indicate an error condition.
     */
    private final KeyListener currentDirectoryKeyListener = new KeyAdapter() {
        public void keyReleased(final KeyEvent e) {
            try {
                directoryTree.setPath(currentDirectoryField.getText());
            } catch (final InvalidPathException ipe) {
                currentDirectoryField.setBackground(Color.PINK);
                currentDirectoryField.setForeground(Color.WHITE);
                return;
            } catch (final IOException ignored) {}
            currentDirectoryField.setBackground(Color.WHITE);
            currentDirectoryField.setForeground(Color.BLACK);
        }
    };

    /**
     * Updates the value in the text field to reflect the currently selected directory and refreshes the list of files
     * displayed.
     */
    private final DirectoryTreeSelectionListener directoryTreeSelectionListener = (final File dir) -> {
        final File currentFile = new File(currentDirectoryField.getText());
        try {
            if (!currentFile.getCanonicalPath().equals(dir.getCanonicalPath())) {
                currentDirectoryField.setText(dir.getPath());
                currentDirectoryField.setBackground(Color.WHITE);
                currentDirectoryField.setForeground(Color.BLACK);
            }
        } catch (final IOException ignored) {}
        fileListModel.removeAllElements();
        final File[] files = dir.listFiles((final File file) -> {
            if (file.isDirectory()) {
                return false;
            } else {
                if (preferencesWindow.isShowHiddenFiles()) {
                    return preferencesWindow.isShowBackupFiles() || !file.getName().endsWith("~");
                } else {
                    if (preferencesWindow.isShowBackupFiles()) {
                        return !file.getName().startsWith(".");
                    } else {
                        return !file.getName().startsWith(".") && !file.getName().endsWith("~");
                    }
                }
            }
        });
        // this happens when trying to read a directory without sufficient permissions
        if (files == null) {
            return;
        }
        Arrays.sort(files, preferencesWindow.getFileListSortType() == FileListSortType.ASCENDING_NAME ?
            sortFilesByName : sortFilesByType);
        fileListSortType = FileListSortType.ASCENDING_NAME;
        for (final File file: files) {
            fileListModel.addElement(file);
        }
    };

    private final PreferencesSaveListener preferencesSaveListener = () -> directoryTree.setShowHiddenDirectories(preferencesWindow.isShowHiddenDirectories());

    /**
     * Re-sorts the files by name, descending if they're currently sorted by name ascending
     */
    private final ActionListener sortByNameClickListener = (final ActionEvent e) -> {
        final Enumeration elementEnumeration = fileListModel.elements();
        @SuppressWarnings("unchecked") final List<File> elementList = Collections.list(elementEnumeration);
        if (fileListSortType.isNameType()) {
            Collections.sort(elementList, fileListSortType.getComplementComparator());
            fileListSortType = fileListSortType.getComplement();
        } else {
            Collections.sort(elementList, sortFilesByName);
            fileListSortType = FileListSortType.ASCENDING_NAME;
        }
        for (int i = 0, elementListSize = elementList.size(); i < elementListSize; i++) {
            final File file = elementList.get(i);
            fileListModel.setElementAt(file, i);
        }
    };

    /**
     * Re-sorts the files by type, descending if they're currently sorted by type ascending
     */
    private final ActionListener sortByTypeClickListener = (final ActionEvent e) -> {
        final Enumeration elementEnumeration = fileListModel.elements();
        @SuppressWarnings("unchecked") final List<File> elementList = Collections.list(elementEnumeration);
        if (fileListSortType.isTypeType()) {
            Collections.sort(elementList, fileListSortType.getComplementComparator());
            fileListSortType = fileListSortType.getComplement();
        } else {
            Collections.sort(elementList, sortFilesByType);
            fileListSortType = FileListSortType.ASCENDING_TYPE;
        }
        for (int i = 0, elementListSize = elementList.size(); i < elementListSize; i++) {
            final File file = elementList.get(i);
            fileListModel.setElementAt(file, i);
        }
    };

    /**
     * Updates the state of the convert button
     */
    private final ListSelectionListener fileListSelectionListener = (final ListSelectionEvent e) -> convertButton.setEnabled(fileList.getSelectedIndices().length > 0);

    private final ActionListener xbmc11RadioButtonClickListener = (final ActionEvent e) -> {
        outputType = OutputType.XBMC11;
        setOutputFilenameExtension(XbmcV11SmartPlaylist.DEFAULT_FILE_EXTENSION);
    };

    private final ActionListener xbmc12RadioButtonClickListener = (final ActionEvent e) -> {
        outputType = OutputType.XBMC12;
        setOutputFilenameExtension(XbmcV12SmartPlaylist.DEFAULT_FILE_EXTENSION);
    };

    private final ActionListener gmmpRadioButtonClickListener = (final ActionEvent e) -> {
        outputType = OutputType.GMMP;
        setOutputFilenameExtension(GmmpSmartPlaylist.DEFAULT_FILE_EXTENSION);
    };

    /**
     * Does the file conversion with the selected files and selected options
     */
    private final ActionListener convertButtonClickListener = (final ActionEvent e) -> {
        final List selectedFiles = fileList.getSelectedValuesList();
        for (final Object inputFileObject: selectedFiles) {
            final File inputFile = (File) inputFileObject;
            try {
                final FormattedSmartPlaylist inputPlaylist = converterApi.loadFromFile(inputFile);
                final Class<? extends FormattedSmartPlaylist> outputTypeClass = outputTypeMap.get(outputType);
                if (outputTypeClass == null) {
                    continue;
                }
                final FormattedSmartPlaylist outputPlaylist = converterApi.convert(inputPlaylist, outputTypeClass);
                final int lastDot = inputFile.getName().lastIndexOf('.');
                final String nameWoExtension = lastDot != -1 ? inputFile.getName().substring(0, lastDot) : inputFile.getName();
                final String outputFilename = outputFilenameFormatField.getText().replaceAll("\\$1", nameWoExtension);
                FileWriter fileWriter = null;
                try {
                    try {
                        fileWriter = new FileWriter(inputFile.getParent() + "/" + outputFilename);
                        fileWriter.write(outputPlaylist.toString());
                    } finally {
                        if (fileWriter != null) {
                            fileWriter.close();
                        }
                    }
                } catch (final IOException ioe) {
                    messageWindow.addMessage(String.format("%s: %s", inputFile.getName(), ioe.getMessage()));
                    continue;
                }
                if (converterApi.getErrorLog().isEmpty()) {
                    messageWindow.addMessage(String.format("%s: success", inputFile.getName()));
                } else {
                    for (final String error: converterApi.getErrorLog()) {
                        messageWindow.addMessage(String.format("%s: %s", inputFile.getName(), error));
                    }
                    converterApi.clearLog();
                }
            } catch (final FileNotFoundException | IllegalArgumentException ex) {
                messageWindow.addMessage(String.format("%s: %s", inputFile.getName(), ex.getMessage()));
            }
        }
        if (preferencesWindow.isAutoPopMessageWindow()) {
            messageWindow.display(GuiConverter.this);
        }
    };

    private final ActionListener messageWindowButtonClickListener = (final ActionEvent e) -> messageWindow.display(GuiConverter.this);

    private final ActionListener configWindowButtonClickListener = (final ActionEvent e) -> preferencesWindow.display(GuiConverter.this);

    /**
     * A custom renderer to only show the file names rather than their fully qualified names
     */
    @SuppressWarnings("unchecked")
    private final ListCellRenderer<File> fileListCellRenderer = (ListCellRenderer) new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            final File file = (File) value;
            setText(file.getName());
            return component;
        }
    };

    private void layoutPanels(final Container container, final JPanel directoryChooserPanel,
            final JPanel fileChooserPanel, final JPanel outputFormatChooserPanel, final JPanel outputFilenamePanel,
            final JPanel convertButtonPanel, final JPanel showMessageWindowButtonPanel,
            final JPanel showConfigWindowButtonPanel) {
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        final JPanel filePanel = new JPanel(new FlowLayout());
        filePanel.add(directoryChooserPanel);
        filePanel.add(fileChooserPanel);
        final JPanel outputPanel = new JPanel(new FlowLayout());
        outputPanel.add(outputFormatChooserPanel);
        outputPanel.add(outputFilenamePanel);
        final JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(convertButtonPanel);
        buttonPanel.add(showMessageWindowButtonPanel);
        buttonPanel.add(showConfigWindowButtonPanel);

        container.add(filePanel);
        container.add(outputPanel);
        container.add(buttonPanel);
    }

    private void setOutputFilenameExtension(final String extension) {
        final String current = outputFilenameFormatField.getText();
        final int lastDot = current.lastIndexOf('.');
        final String prefix = lastDot != -1 ? current.substring(0, lastDot) : current;
        outputFilenameFormatField.setText(String.format("%s.%s", prefix, extension));
    }

    private void createAndShowGui() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel directoryChooserPanel = new JPanel(new BorderLayout());
        currentDirectoryField.setText("/");
        currentDirectoryField.setColumns(30);
        currentDirectoryField.addKeyListener(currentDirectoryKeyListener);
        directoryChooserPanel.add(currentDirectoryField, BorderLayout.NORTH);
        directoryTree.addTreeSelectionListener(directoryTreeSelectionListener);
        directoryTree.setFilterMode(DirectoryTree.DIRECTORIES_ONLY);
        directoryTree.setShowHiddenDirectories(preferencesWindow.isShowHiddenDirectories());
        final JScrollPane directoryScrollPane = new JScrollPane(directoryTree);
        directoryScrollPane.setPreferredSize(new Dimension(200, 400));
        directoryChooserPanel.add(directoryScrollPane, BorderLayout.SOUTH);

        final JPanel fileChooserPanel = new JPanel(new BorderLayout());
        final JPanel sortButtonsPanel = new JPanel();
        fileChooserPanel.add(sortButtonsPanel, BorderLayout.NORTH);
        sortByNameButton.setText("Name");
        sortByTypeButton.setText("Type");
        sortButtonsPanel.add(new JLabel("sort by:"));
        sortButtonsPanel.add(sortByNameButton);
        sortButtonsPanel.add(sortByTypeButton);
        sortByNameButton.addActionListener(sortByNameClickListener);
        sortByTypeButton.addActionListener(sortByTypeClickListener);
        final JScrollPane fileListScrollPane = new JScrollPane(fileList);
        fileListScrollPane.setPreferredSize(new Dimension(200, 400));
        fileChooserPanel.add(fileListScrollPane, BorderLayout.SOUTH);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setLayoutOrientation(JList.VERTICAL);
        fileList.setCellRenderer(fileListCellRenderer);
        fileList.addListSelectionListener(fileListSelectionListener);

        final JPanel outputFormatChooserPanel = new JPanel();
        outputFormatChooserPanel.setLayout(new BoxLayout(outputFormatChooserPanel, BoxLayout.Y_AXIS));
        final ButtonGroup radioButtons = new ButtonGroup();
        radioButtons.add(xbmc11RadioButton);
        radioButtons.add(xbmcRadioButton);
        radioButtons.add(gmmpRadioButton);
        xbmc11RadioButton.setText("XBMC 11");
        xbmc11RadioButton.setToolTipText("XBMC version 11 and lower");
        xbmc11RadioButton.addActionListener(xbmc11RadioButtonClickListener);
        xbmcRadioButton.setText("XBMC");
        xbmcRadioButton.setToolTipText("XBMC version 12 and higher");
        xbmcRadioButton.addActionListener(xbmc12RadioButtonClickListener);
        gmmpRadioButton.setText("GMMP");
        gmmpRadioButton.setToolTipText("GoneMad Music Player");
        gmmpRadioButton.addActionListener(gmmpRadioButtonClickListener);
        gmmpRadioButton.doClick();
        outputFormatChooserPanel.add(new JLabel("Output file(s) format:"));
        outputFormatChooserPanel.add(xbmc11RadioButton);
        outputFormatChooserPanel.add(xbmcRadioButton);
        outputFormatChooserPanel.add(gmmpRadioButton);

        final JPanel outputFilenamePanel = new JPanel();
        final String filenameTooltip = "Use $1 for the original filename (without extension)";
        outputFilenamePanel.setLayout(new BoxLayout(outputFilenamePanel, BoxLayout.Y_AXIS));
        outputFilenamePanel.add(new JLabel("Output file(s) name:"));
        outputFilenamePanel.add(outputFilenameFormatField);
        outputFilenameFormatField.setToolTipText(filenameTooltip);
        outputFilenamePanel.setToolTipText(filenameTooltip);

        final JPanel convertButtonPanel = new JPanel();
        convertButton.setText("Convert");
        convertButton.setEnabled(false);
        convertButton.addActionListener(convertButtonClickListener);
        convertButtonPanel.add(convertButton);

        final JPanel showMessageWindowButtonPanel = new JPanel();
        showMessageWindowButton.setText("Message Window");
        showMessageWindowButton.addActionListener(messageWindowButtonClickListener);
        showMessageWindowButtonPanel.add(showMessageWindowButton);

        final JPanel showConfigWindowButtonPanel = new JPanel();
        showConfigWindowButton.setText("Preferences");
        showConfigWindowButton.addActionListener(configWindowButtonClickListener);
        showConfigWindowButtonPanel.add(showConfigWindowButton);

        layoutPanels(getContentPane(), directoryChooserPanel, fileChooserPanel, outputFormatChooserPanel,
            outputFilenamePanel, convertButtonPanel, showMessageWindowButtonPanel, showConfigWindowButtonPanel);

        messageWindow.setTitle("Message Window");
        preferencesWindow.setTitle("Preferences");
        preferencesWindow.addSaveListener(preferencesSaveListener);

        pack();
        setVisible(true);
    }

    public GuiConverter() {
        super("Smart Playlists");
        SwingUtilities.invokeLater(this::createAndShowGui);
    }

    private static enum OutputType {
        XBMC11,
        XBMC12,
        GMMP
    }
}
