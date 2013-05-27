# brevis

brevis, a Second Generation artificial life simulator.

brevis is a revitalization of the spirit of the breve simulator in the functional
language, Clojure.
	
Documentation:   http://cs.brandeis.edu/~kyleh/brevis
Chat: #brevis@irc.freenode.net
  
by Kyle Harrington (kyleh@cs.brandeis.edu), 2011-2013.
   http://kyleharrington.com

## Prerequisites:

The prerequisites are an IDE, the Clojure libraries and development environment, the git version control system, and the brevis source code.

Software you will need (all work for Linux, OS X, and Windows):

Eclipse - http://www.eclipse.org/ (I generally download Eclipse Classic)

Counterclockwise Eclipse plugin - http://code.google.com/p/counterclockwise/wiki/Documentation#Install_Counterclockwise_plugin
[In Eclipse: Help menu->Install new software->Update site : http://ccw.cgrand.net/updatesite/]

Git version control can be performed within Eclipse (http://eclipse.github.com/); however, I use stand-alone git programs (see the "Applications" section at the bottom of https://github.com/).

## Usage

Developed for use with the Clojure Eclipse plugin, CounterClockwise (this must be installed).

0. Obtain brevis from http://github.com/kephale/brevis

1. Create a default project in Eclipse. 

Uncheck the default location and point it to the location to which brevis was obtained.

2. Right click on the new project Leiningen->Reset Project Configuration

3. Open brevis.example.swarm

4. Menu: Clojure -> Load file in REPL

Keyboard keys:

esc - kill the simulation

...

5. Try doing things in your repl like: (reset! avoidance 0.2)

6. Enjoy. There is more to come...

## License

This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington