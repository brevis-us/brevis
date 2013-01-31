# brevis

brevis, a Second Generation artificial life simulator.

brevis is a revitalization of the spirit of the breve simulator in the functional
language, Clojure.
	
	http://cs.brandeis.edu/~kyleh/brevis

by Kyle Harrington (kyleh@cs.brandeis.edu), 2011-2013.
   http://kyleharrington.com

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

Copyright © 2013 Kyle Harrington

Distributed under the Eclipse Public License, the same as Clojure.
