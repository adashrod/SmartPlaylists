package com.aaron.smartplaylists.guicomponent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A popup dialog for displaying messages and errors
 */
public class MessageWindow extends JDialog {
    private final JTextArea messageArea = new JTextArea();
    private final JButton clearButton = new JButton();

    private final ActionListener clearButtonClickListener = (final ActionEvent e) -> {
        messageArea.setText("");
        clearButton.setEnabled(false);
    };

    public void addMessage(final String message) {
        messageArea.append(String.format("%s\n", message));
        clearButton.setEnabled(true);
    }

    public void display(final Component parent) {
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void createGui() {
        clearButton.setText("Clear");
        clearButton.addActionListener(clearButtonClickListener);
        clearButton.setEnabled(false);

        final JPanel mainPanel = new JPanel();
        messageArea.setPreferredSize(new Dimension(400, 300));
        messageArea.setEditable(false);
        final JPanel clearButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        clearButtonPanel.add(clearButton);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(messageArea);
        mainPanel.add(clearButtonPanel);

        final Container container = getContentPane();
        container.add(mainPanel);

        pack();
        setVisible(false);
    }

    public MessageWindow(final JFrame parent) {
        super(parent);
        SwingUtilities.invokeLater(this::createGui);
    }
}
