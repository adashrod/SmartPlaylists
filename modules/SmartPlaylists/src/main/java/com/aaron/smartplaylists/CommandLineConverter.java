package com.aaron.smartplaylists;

import com.aaron.smartplaylists.api.ConverterApi;
import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.playlists.GmmpSmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The command line version of the SmartPlaylists program. This converts a playlist file that was passed in on the
 * command line.
 */
public class CommandLineConverter {
    private static final Logger logger = Logger.getLogger(CommandLineConverter.class);

    private static final ConverterApi converterApi = new ConverterApi();
    private static final Map<String, Class<? extends FormattedSmartPlaylist>> stringConverterMap = new HashMap<>();
    private static final Options programOptions = new Options()
        .addOption("h", "help", false, "Display this help information");
    static {
        stringConverterMap.put("xbmc11", XbmcV11SmartPlaylist.class);
        stringConverterMap.put("xbmc", XbmcV12SmartPlaylist.class);
        stringConverterMap.put("gmmp", GmmpSmartPlaylist.class);

        final Option outputFileOption = new Option("o", "output-file", true, "The name of the file to write");
        outputFileOption.setRequired(true);
        outputFileOption.setArgs(1);
        final Option formatOption = new Option("f", "format", true, "The playlist type of the output playlist: allowed values are \"xbmc11\", \"xbmc\" (XBMC v12+), and \"gmmp\". Optional: \"gmmp\" is the default");
        formatOption.setArgs(1);
        programOptions.addOption(outputFileOption).addOption(formatOption);
    }

    private static boolean hasHelpOption(final Option[] options) {
        for (final Option option: options) {
            if (option.getOpt().equals("h")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHelpOption(final String[] arguments) {
        for (final String arg: arguments) {
            if (arg.equals("-h") || arg.equals("--help")) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp("CLI: java -jar SmartPlaylists.jar -o <output_file> [-f (xbmc11|xbmc|gmmp)] <input_file>\nGUI: java -jar SmartPlaylists.jar <no arguments>",
            programOptions);
        System.exit(0);
    }

    private static FormattedSmartPlaylist readFile(final File file) {
        final FormattedSmartPlaylist inputPlaylist;
        try {
             inputPlaylist = converterApi.loadFromFile(file);
        } catch (final FileNotFoundException fnf) {
            System.err.println(fnf.getMessage());
            System.exit(1);
            return null; // because the java compiler is annoying
        } catch (final IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.exit(1);
            return null; // because the java compiler is annoying
        }
        return inputPlaylist;
    }

    private static void writeFile(final FormattedSmartPlaylist outputPlaylist, final String outputFilename) {
        FileWriter fileWriter = null;
        try {
            try {
                fileWriter = new FileWriter(outputFilename);
                fileWriter.write(outputPlaylist.toString());
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        } catch (final IOException ioe) {
            logger.error(ioe.getMessage());
            System.exit(-1);
        }
    }

    public static void run(final String[] arguments) {
        final CommandLineParser parser = new BasicParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(programOptions, arguments);
        } catch (final UnrecognizedOptionException uoe) {
            System.err.println(uoe.getMessage());
            System.err.println("Use option -h (--help) for usage");
            System.exit(1);
            return;
        } catch (final ParseException pe) {
            if (hasHelpOption(arguments)) {
                printHelp();
            }
            System.err.println(pe.getMessage());
            System.err.println("Use option -h (--help) for usage");
            System.exit(1);
            return;
        }
        if (hasHelpOption(commandLine.getOptions())) {
            printHelp();
        }

        final String outputFilename = commandLine.getOptionValue("o");
        final Class<? extends FormattedSmartPlaylist> outputType = stringConverterMap.get(commandLine.getOptionValue('f', "gmmp").toLowerCase());
        final String[] theRest = commandLine.getArgs();
        if (theRest.length == 0) {
            System.err.println("No input file specified. Use option -h (--help) for usage");
            System.exit(1);
            return;
        }
        final File inputFile = new File(theRest[0]);

        final FormattedSmartPlaylist inputPlaylist = readFile(inputFile);
        final FormattedSmartPlaylist outputPlaylist = converterApi.convert(inputPlaylist, outputType);
        writeFile(outputPlaylist, outputFilename);
    }
}
