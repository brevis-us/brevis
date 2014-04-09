#_"This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
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
                                                                                                                                                                                     
Copyright 2012-2014 Kyle Harrington"     

(ns brevis.plot
  (:import (org.jfree.chart ChartFactory ChartPanel JFreeChart)
           (org.jfree.chart.axis NumberAxis)
           (org.jfree.chart.plot CombinedDomainXYPlot PlotOrientation XYPlot)
           (org.jfree.chart.renderer.xy StandardXYItemRenderer XYLineAndShapeRenderer)
           (org.jfree.data.xy XYSeries XYSeriesCollection)
           (org.jfree.data.time Month TimeSeries TimeSeriesCollection)
           (org.jfree.ui ApplicationFrame RefineryUtilities)
           (java.text SimpleDateFormat)
           (java.awt Color)
           (brevis.plot Plotter))
  (:use [brevis.random]))

(defn make-xy-dataset
  "Convert a hash-map or vector of pairs into a plottable XY dataset."
  [data]
  (let [xyseries (XYSeries. (gensym "dataset"))]
    (doseq [[k v] data]
      (.addOrUpdate xyseries k v))
    (let [xycoll (XYSeriesCollection. xyseries)]
      xycoll)))

(defn example-plotter
  "Do an example plot."
  []
  (let [;dataset (example-dataset)
        ;chart (example-chart dataset)
        xrange (range 0 10 0.1)
        yrange (map #(java.lang.Math/sin %) xrange)
        plotter (brevis.plot.Plotter. "my title" (make-xy-dataset (zipmap xrange yrange)))]
    (.pack plotter)
    (RefineryUtilities/centerFrameOnScreen plotter)
    (.setVisible plotter true)))

#_(example-plotter)

