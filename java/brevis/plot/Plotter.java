package brevis.plot;

import java.awt.Color;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

/**
 * An example of a time series chart.  For the most part, default settings are 
 * used, except that the renderer is modified to show filled shapes (as well as 
 * lines) at each data point.
 */
public class Plotter extends ApplicationFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4224772067055836525L;

	/**
     * A demonstration application showing how to create a simple time series 
     * chart.  This example uses monthly data.
     *
     * @param title  the frame title.
     */
    public Plotter(String title, XYDataset dataset) {
        super(title);
        ChartPanel chartPanel = (ChartPanel) createDemoPanel( dataset );
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }

    /**
     * Creates a chart.
     * 
     * @param dataset  a dataset.
     * 
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset) {

        //JFreeChart chart = ChartFactory.createTimeSeriesChart(
    	JFreeChart chart = ChartFactory.createXYLineChart(
            "Brevis plot",  // title
            "",             // x-axis label
            "",   // y-axis label
            dataset,            // data
            PlotOrientation.VERTICAL,
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
        }
        
        //DateAxis axis = (DateAxis) plot.getDomainAxis();
        //axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
        
        return chart;

    }
    
    /**
     * Creates a dataset, consisting of two series of monthly data.
     *
     * @return The dataset.
     */
    private static XYDataset createDataset() {

        TimeSeries s1 = new TimeSeries("L&G European Index Trust", Month.class);
        s1.addOrUpdate(new Month(2, 2001), 181.8);
        s1.addOrUpdate(new Month(3, 2001), 167.3);
        s1.addOrUpdate(new Month(4, 2001), 153.8);
        s1.addOrUpdate(new Month(5, 2001), 167.6);
        s1.addOrUpdate(new Month(6, 2001), 158.8);
        s1.addOrUpdate(new Month(7, 2001), 148.3);
        s1.addOrUpdate(new Month(8, 2001), 153.9);
        s1.addOrUpdate(new Month(9, 2001), 142.7);
        s1.addOrUpdate(new Month(10, 2001), 123.2);
        s1.addOrUpdate(new Month(11, 2001), 131.8);
        s1.addOrUpdate(new Month(12, 2001), 139.6);
        s1.addOrUpdate(new Month(1, 2002), 142.9);
        s1.addOrUpdate(new Month(2, 2002), 138.7);
        s1.addOrUpdate(new Month(3, 2002), 137.3);
        s1.addOrUpdate(new Month(4, 2002), 143.9);
        s1.addOrUpdate(new Month(5, 2002), 139.8);
        s1.addOrUpdate(new Month(6, 2002), 137.0);
        s1.addOrUpdate(new Month(7, 2002), 132.8);

        TimeSeries s2 = new TimeSeries("L&G UK Index Trust", Month.class);
        s2.addOrUpdate(new Month(2, 2001), 129.6);
        s2.addOrUpdate(new Month(3, 2001), 123.2);
        s2.addOrUpdate(new Month(4, 2001), 117.2);
        s2.addOrUpdate(new Month(5, 2001), 124.1);
        s2.addOrUpdate(new Month(6, 2001), 122.6);
        s2.addOrUpdate(new Month(7, 2001), 119.2);
        s2.addOrUpdate(new Month(8, 2001), 116.5);
        s2.addOrUpdate(new Month(9, 2001), 112.7);
        s2.addOrUpdate(new Month(10, 2001), 101.5);
        s2.addOrUpdate(new Month(11, 2001), 106.1);
        s2.addOrUpdate(new Month(12, 2001), 110.3);
        s2.addOrUpdate(new Month(1, 2002), 111.7);
        s2.addOrUpdate(new Month(2, 2002), 111.0);
        s2.addOrUpdate(new Month(3, 2002), 109.6);
        s2.addOrUpdate(new Month(4, 2002), 113.2);
        s2.addOrUpdate(new Month(5, 2002), 111.6);
        s2.addOrUpdate(new Month(6, 2002), 108.8);
        s2.addOrUpdate(new Month(7, 2002), 101.6);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        //dataset.addSeries(s2);

        dataset.setDomainIsPointsInTime(true);

        return dataset;

    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     * 
     * @return A panel.
     */
    public static JPanel createDemoPanel( XYDataset dataset ) {
        JFreeChart chart = createChart( dataset );
        return new ChartPanel(chart);
    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    /*public static void main(String[] args) {

        Plotter demo = new Plotter(
            "Time Series Chart Demo 1"
        );
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }*/

}

