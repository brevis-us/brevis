# brevis

brevis, an open-source, functional scientific and artificial life simulator.

brevis is a 3D scientific, artificial life and complex systems simulator where simulations are written in the functional programming language Clojure. 
	
On the web:   http://brevis.us
Chat: #brevis@irc.freenode.net
  
by Kyle Harrington (kharrin3@bidmc.harvard.edu), 2011-2014.
   http://kyleharrington.com
      
# Citing:

Harrington, K. I. (2014). Brevis (version X.Y.Z). where X, Y, Z are the version numbers found in project.clj for the Brevis version that you are using. Other Brevis publications can be found on the Brevis website.  

## Getting started:

[There is a prototype IDE that allows Brevis to be run with no extra software added. Be on the look out in 2014 for this fancy new version.]

brevis is designed to be compatible with Eclipse and a couple of Eclipse addons. These instructions should work with Eclipse versions on all major OS platforms.

1. Install Eclipse - The development environment - http://www.eclipse.org/ 

2. Install the EGit addon for Eclipse - Version control -  http://www.eclipse.org/egit/

3. Install the CounterClockwise - Clojure support in Eclipse - http://code.google.com/p/counterclockwise/

4. Download brevis. Within Eclipse, Import Project->Team->Git URI, choose an address listed on https://github.com/kephale/brevis

5. One-time project configuration. Within Eclipse, right click on brevis (the project you just imported), choose Leiningen->Reset Project Configuration

## Usage

Developed for use with the Clojure Eclipse plugin, CounterClockwise (this must be installed).

1. Open brevis.example.swarm

2. Menu: Clojure -> Load file in REPL

## Controlling the simulation

Default input:

esc - kill the simulation  
left click + drag - rotate camera  
w - move camera forward  
s - move camera backward  
a - move camera left  
d - move camera right  
z - move camera up  
c - move camera down  
o - screenshot  

## License

Brevis is licensed under GPLv3. LGPL licenses may hesitantly be granted upon request.

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
