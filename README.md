SmartPlaylists
==============

This program can be used on the command line or in a GUI to convert between various smart playlist file formats.

A smart playlist is a media playlist that consists of a list of rules rather than a list of files. The rules specify things like artist, year, album, song title, etc and loading a playlist matches files in the media library against those rules, adding whichever files matched the rules. This allows for playlists that automatically update whenever songs are added to or removed from the library.

Support
-------
Currently it supports XBMC (music) smart playlists versions 11 and 12:

http://wiki.xbmc.org/?title=Smart_playlists

and GoneMad Media Player smart playlists (up to v 1.5.0.5):

https://play.google.com/store/apps/details?id=gonemad.gmmp

http://gonemadmusicplayer.blogspot.com/2012/12/a-look-at-smart-playlists.html

It hasn't yet been tested with XBMC version 13 or versions below 11.

Prerequisites
-------------
* Java SDK 8
* ant, the Java-based make tool

Usage
-----
Building and Running
The ant task "build.all" will build the jars and put everything into {project root}/antout. SmartPlaylists.jar can be run from the command line the following ways:

`java -jar SmartPlaylists.jar`    (runs the GUI)

`java -jar SmartPlaylists.jar -h` (running with any arguments will run the CLI, "-h" displays the help text)

the ant task "run" will build everything and run the GUI.
