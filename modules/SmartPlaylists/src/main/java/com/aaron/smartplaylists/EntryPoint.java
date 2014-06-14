package com.aaron.smartplaylists;

/**
 * This is the entry point of the program. It decides whether to pass command line arguments to the command line program
 * or to start the GUI
 */
public class EntryPoint {
    public static void main(final String[] arguments) {
        if (arguments.length > 0) {
            CommandLineConverter.run(arguments);
        } else {
            new GuiConverter();
        }
    }
}
