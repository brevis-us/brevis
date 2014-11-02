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
  (:use [brevis.physics.utils]; for add-update-handler
        [brevis utils]
        [brevis.random]))

;(def plot-handlers (atom []))

(defn make-xy-dataset
  "Convert a hash-map or vector of pairs into a plottable XY dataset."
  ([data] (make-xy-dataset data (gensym "dataset")))
  ([data dataset-name] (make-xy-dataset data dataset-name false false))
  ([data dataset-name auto-sort allow-duplicates]
    (let [xyseries (XYSeries. dataset-name auto-sort allow-duplicates)]
      (doseq [[k v] data]
        (.addOrUpdate xyseries k v))
      (let [xycoll (XYSeriesCollection. xyseries)]
        {:data-collection xycoll
         :series [xyseries]}))))

(defn make-xy-plot
  "Just make me a friggin XY plot!"
  [data]
  (let [plotter (brevis.plot.Plotter. "my title" (:data-collection (make-xy-dataset data)))]
    (.pack plotter)
    (RefineryUtilities/centerFrameOnScreen plotter)
    (.setVisible plotter true)
    plotter))

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

(defn add-plot-handler
  "Add a plot handler. Remove assumes you want to remove the min x, value, this is best for timeseries"
  [xy-fn & {:keys [interval
                   title
                   priority]
            :or {interval 200
                 title "Brevis"
                 priority 100}}]
  (when-not (System/getProperty "brevisHeadless")
    (let [plot-data (make-xy-dataset [] title)
          plotter (brevis.plot.Plotter. title (:data-collection plot-data))
          handler-fn (fn []
                       (when (> (.getItemCount (first (:series plot-data))) interval)                              
                         (.remove ^XYSeries (first (:series plot-data)) 
                           (.getMinX ^XYSeries (first (:series plot-data))))
                         (let [miny (.getMinY ^XYSeries (first (:series plot-data)))
                               maxy (.getMaxY ^XYSeries (first (:series plot-data)))]
                           
                           (.setYRange ^Plotter plotter miny maxy)))
                       (let [[x y] (xy-fn)]
                         (.addOrUpdate ^XYSeries (first (:series plot-data)) x y)))]
      (add-destroy-hook (fn [] (.dispose plotter)))              
      (.pack plotter)
      (RefineryUtilities/positionFrameRandomly plotter)
      (.setVisible plotter true)      
      (add-global-update-handler priority handler-fn)))); should keep track of plotters in the engine or somewhere and delete then when the window is destroyed

(defn add-scatter-handler
  "Add a plot handler. Remove assumes you want to remove the min x, value, this is best for timeseries"
  [xy-fn & {:keys [interval
                   title
                   priority]
            :or {interval 200
                 title "Brevis"
                 priority 100}}]
  (when-not (System/getProperty "brevisHeadless")
    (let [plot-data (make-xy-dataset [] title)
          plotter (brevis.plot.Plotter. title (:data-collection plot-data))
          handler-fn (fn []
                       (.clear ^XYSeries (first (:series plot-data)))
                       (doseq [[x y] (xy-fn)]
                         (.addOrUpdate ^XYSeries (first (:series plot-data)) x y))
                       #_(when (> (.getItemCount (first (:series plot-data))) interval)                              
                          (.remove (first (:series plot-data)) 
                            (.getMinX (first (:series plot-data)))))
                       #_(let [[x y] (xy-fn)]
                          (.addOrUpdate (first (:series plot-data)) x y)))]
      (add-destroy-hook (fn [] (.dispose plotter)))        
      (.pack plotter)
      (RefineryUtilities/positionFrameRandomly plotter)
      (.setVisible plotter true)      
      (add-global-update-handler priority handler-fn))))

#_(defn make-histogram-dataset
   "Convert a vector of elements into a plottable histogram dataset."
   ([data] (make-histogram-dataset data (gensym "dataset")))
   ([data dataset-name]
     (let [dataset (SimpleHistogramDataset. dataset-name (into (double-array)
                                                               data))]
       {:series [dataset]})))

(defn add-histogram-handler
  "Add a plot handler. Plots a lazy histogram, requires numbers."
  [x-fn & {:keys [priority
                  title]
           :or {title "Brevis"
                priority 100}}]
  (when-not (System/getProperty "brevisHeadless")
    (let [plot-data (make-xy-dataset [] title)
          plotter (brevis.plot.Plotter. title (:data-collection plot-data))
          handler-fn (fn []                       
                       (let [xs (x-fn)
                             hist (frequencies xs)]
                         (.clear ^XYSeries (first (:series plot-data)))
                         (doseq [[k v] hist]
                           (.add ^XYSeries (first (:series plot-data)) 
                             ^double (double k) ^double (double v)))))]
      (.setLinesVisible (.renderer plotter) false)
      (add-destroy-hook (fn [] (.dispose plotter)))              
      (.pack plotter)
      (RefineryUtilities/positionFrameRandomly plotter)
      (.setVisible plotter true)      
      (add-global-update-handler priority handler-fn))))

(defn make-multiseries-xy-dataset
  "Convert a hash-map or vector of pairs into a plottable XY dataset."
  ([n] (make-multiseries-xy-dataset n (repeatedly n #(gensym "dataset"))))
  ([n dataset-names] (make-multiseries-xy-dataset n dataset-names false false))
  ([n dataset-names auto-sort allow-duplicates]
    (let [xyseries (map #(XYSeries. % auto-sort allow-duplicates) dataset-names)]
      (let [xycoll (XYSeriesCollection.)]
        (doseq [s xyseries]
          (.addSeries xycoll ^XYSeries s)) 
        {:data-collection xycoll
         :series (into [] xyseries)}))))

(defn add-multiplot-handler
  "Add a plot handler. Remove assumes you want to remove the min x, value, this is best for timeseries"
  [& {:keys [interval
             title
             priority
             xy-fns
             legends]
      :or {interval 200
           title "Brevis"
           priority 100
           xy-fns []
           legends []}}]
  (when-not (System/getProperty "brevisHeadless")
    (let [n (count xy-fns)
          legends (if (empty? legends) (for [k (range n)] (str title " " k)) legends)
          plot-data (make-multiseries-xy-dataset n legends)
          plotter (brevis.plot.Plotter. title (:data-collection plot-data))
          handler-fn (fn []
                       (dotimes [k (count xy-fns)]
                         (let [series (nth (:series plot-data) k)
                               xy-fn (nth xy-fns k)]
                           (when (> (.getItemCount series) interval)                              
                             (.remove ^XYSeries series
                               (.getMinX series)))
                           (let [[x y] (xy-fn)]
                             (.addOrUpdate ^XYSeries series x y)))))]
      (add-destroy-hook (fn [] (.dispose plotter)))              
      (.pack plotter)
      (RefineryUtilities/positionFrameRandomly plotter)
      (.setVisible plotter true)      
      (add-global-update-handler priority handler-fn)))); should keep track of plotters in the engine or somewhere and delete then when the window is destroyed

