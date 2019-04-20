package us.brevis;

// inspired by https://raw.github.com/oakes/Nightcode/da619d15e87e45238e9bc0810edf4200a3adab76/src/java/nightcode/Main.java


import java.awt.SplashScreen;

import java.lang.reflect.Method;

/**
 * This class handles loading of Nightcode namespaces while the splash image
 * is displayed on the screen. See entry point main() method in this class. How
 * it works:
 * 0. Assumes the splash screen is loaded (see project.clj - :manifest entry)
 * 1. Loads nightcode.core/-main using reflection (it takes time)
 * 2. Closes splash screen
 * 3. Invokes nightcode.core/-main
 * See: http://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html
 * See (creating animated loader image): http://preloaders.net/en/search/text
 */
public class Launcher {

    private Method loadBrevisMain() throws Exception {
        final String cn = "brevis.ui.core";
    	//final String cn = "brevis.examples.swarm";
        Class<?> clazz = Class.forName(cn);
        if (clazz == null) {
            throw new RuntimeException("Cannot load class " + cn);
        }
        Method m = clazz.getMethod("main", String[].class);
        if (m == null) {
            throw new RuntimeException("Cannot obtain method 'main'");
        }
        return m;
    }

    private void invokeBrevisMain(Method m, String[] args) throws Exception {
        m.invoke(null, (Object) args);
    }

    private void init(String[] args) throws Exception {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        //System.err.println("Splash " + splash.getImageURL() );
        if (splash == null) {
        	System.err.println("Cannot launch splash screen. Proceeding.");
        }
        Method m = loadBrevisMain();
        if (splash != null) {
            splash.close();
        }
        invokeBrevisMain(m, args);
    }

    public static void main(String[] args) throws Exception {
        new Launcher().init(args);
    }

}