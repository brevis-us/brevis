package brevis;

import graphics.scenery.SceneryBase;
import io.scif.SCIFIOService;

import net.imagej.ImageJService;

import net.imagej.lut.LUTService;
import org.scijava.Context;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import cleargl.GLVector;
import sc.iview.SciView;
import sc.iview.SciViewService;

/**
 * Entry point for testing SciView functionality.
 * 
 * @author Kyle Harrington
 */
public class Main {
    public static void main( String... args ) {
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class);

        UIService ui = context.service( UIService.class );
        if( !ui.isVisible() ) ui.showUI();

        SciViewService sciViewService = context.service( SciViewService.class );
        SciView sciView = sciViewService.getOrCreateActiveSciView();        //Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class, SciViewService.class, LUTService.class);

        //String namespace = args[0];

        //System.out.println("Running namespace " + namespace);



        //UIService ui = context.service( UIService.class );
        //if( !ui.isVisible() ) ui.showUI();

        //SciViewService sciViewService = context.service( SciViewService.class );
        //SciView sciView = sciViewService.getOrCreateActiveSciView();
    }
}
