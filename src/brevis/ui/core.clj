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
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington"     

(ns brevis.ui.core  
  (:import (org.fife.ui.rsyntaxtextarea RSyntaxTextArea SyntaxConstants
                                        TokenMakerFactory)	
           (org.fife.ui.rtextarea RTextScrollPane)
           [javax.swing JFileChooser JEditorPane JScrollPane BorderFactory]
           (java.awt.event KeyAdapter)
           (java.io ByteArrayInputStream)
           [java.io File]
           [javax.swing.filechooser FileSystemView]
           java.awt.Font
           [java.awt Component])
  (:require  
    [brevis.ui.keybinds :as keybinds]
    [clojure.string :as string]
    [clojure.tools.nrepl.transport :as nrepl.transport]
    [clojure.tools.nrepl.server :as nrepl.server]
    [clojure.tools.nrepl :as nrepl]
    [clojure.tools.nrepl.misc :as nrepl.misc]
    [leiningen.repl :as repl]
    [leiningen.core.project :as project]
    [leiningen.core.eval :as eval]
    [leiningen.core.main :as lein-main])
  (:use [clojure.java.io :only [file]] 
        [clojure.pprint]
        [seesaw.util :only [illegal-argument to-seq check-args
                            constant-map resource resource-key?
                            to-dimension to-insets to-url try-cast
                            cond-doto to-mnemonic-keycode]]
        [seesaw core font color graphics chooser mig tree]
        [brevis.ui.profile])
  (:gen-class
    :name brevis.ui.BrevisUICore
    ;:init init
    :main main))

;; Todo:
;;
;; - prompt to setup profile info
;; - keybinds
;; - find/replace
;; - tabbed windows
;; - autosave
;; - generalized directories
;; - profile
;; - popups for projects
;;

;; ## Globals

#_(def workspace-directory (atom (str (System/getProperty "user.home") File/separator "git")))

(def editor-window
  "Keeping track of the open editor window." (atom nil))
  
(def repl 
  "The REPL server itself."
  (atom nil))

(def repl-input-window
  "Keeping track of the open REPL input window."(atom nil))

(def repl-inputstream 
  (atom nil))

(def repl-output-window   
  "Keeping track of the open REPL output window."(atom nil))

(def project-browser
  (atom nil))

(def repl-outputstream
  (atom nil))

(def content-pane-params (atom {}))

(def ui-window
  "The complete UI panel." (atom nil))

(def chooser (javax.swing.JFileChooser.)) ; FileChooser hack to get system icons


(defn get-editor
  "return the first editor window."
  []
  @editor-window)

(defn init-ui
  "Do all the one-time initializations for UI functionality."
  []
  (native!))

(defn display
  "Display the content in the given frame."
  [f content]
  (config! f :content content)
  content)

(defn select-file 
  "File selection dialog."
  ([]
    (let [chooser (JFileChooser.)]
    (.showDialog chooser nil "Select")
    (.getSelectedFile chooser)))
  ([directory]
    (let [chooser (JFileChooser. (file directory))]
    (.showDialog chooser nil "Select")
    (.getSelectedFile chooser))))

(defn a-new [e]
  (let [selected (select-file)] 
    (if (.exists (file selected))
      (alert "File already exists.")
      (do #_(set-current-file selected)
          (.setText (:text-area (get-editor)) "")
          #_(set-status "Created a new file.")))))

(defn is-open?
  "Check if a file is already open."
  [filename]
  (let [num-tabs (.getTabCount (:tabbed-panel @editor-window))
        tab-names (map #(.getToolTipTextAt (:tabbed-panel @editor-window) %) (range num-tabs))]
    (some #(when (= filename (nth tab-names %)) %)
          (range (count tab-names)))))

(declare add-content-tab-from-filename)
(defn open-file
  "Open a file and add  the tab if it isnt already open."
  [filename]
  (if-let [tab-idx (is-open? filename)]
    (.setSelectedIndex (:tabbed-panel @editor-window) tab-idx)
    (add-content-tab-from-filename filename @content-pane-params)))

(defn a-open [e]
  (open-file (select-file)))

(defn get-text-from-tab
  "Return the text from a tab."
  [tab-idx]
  (let [component (.getTabComponentAt (:tabbed-panel @editor-window) tab-idx)]
    (.getText (.getTextArea component))))
  
(defn current-tab-index
  "Return the index of the currently selected tab."
  []
  (.getSelectedIndex (:tabbed-panel @editor-window)))

(defn a-save [e]
  (let [tab-idx (current-tab-index)]
    (spit (.getToolTipTextAt (:tabbed-panel @editor-window) tab-idx)
          (get-text-from-tab tab-idx))))

(defn a-save-as [e]
  (let [tab-idx (.getSelectedIndex (:tabbed-panel @editor-window))
        current-filename (.getToolTipTextAt (:tabbed-panel @editor-window) tab-idx)
        current-file (file current-filename)]
    (when-let [selected (if current-filename
                          (select-file (.getParent current-file))
                          (select-file))]      
      (spit selected
            (get-text-from-tab tab-idx)))))

(defn a-exit  [e] (System/exit 0))
(defn a-copy  [e] (.copy (:text-area (get-editor))))
(defn a-cut   [e] (.cut (:text-area (get-editor))))
(defn a-paste [e] (.paste (:text-area (get-editor))))

(def repl-output (atom []))
(def repl-input (atom []))
(def repl-input-index (atom 0));; 0 is most recent, then go backwards, this is used with (- (count @repl-input) index)

(defn update-repl-output
  "Update the REPL output window."
  []
  #_(println "count repl-output" (count @repl-output))
  #_(doseq [line @repl-output]
     (println line))
  (text! (:text-area @repl-output-window)
         (with-out-str
           (doseq [line @repl-output]
             (println line)))))

(defn write-stdout-repl
  "Write a stdout message for to the REPL output."
  [s]
  (swap! repl-output conj s);; probably should limit history
  (update-repl-output))

(def write-value-repl write-stdout-repl)
(def write-form-repl write-stdout-repl)

(defn eval-and-print
  "Evaluate something, print all outputs, and print the final returned value."
  [thing echo?]
  (when echo?
    (swap! repl-input conj thing);; probably should limit history
    (write-form-repl (str "> " thing)))
  (let [response-vals (nrepl/message (:client @repl) {:op "eval" :code thing})]
    (doseq [resp response-vals]
      (when (:out resp) (write-stdout-repl (:out resp))#_(println (:out resp)))
      (when (:value resp) (write-value-repl (:value resp)))
      #_(when (:value resp) (println (:value resp))))
    #_(write-value-repl (:value (last response-vals)))
    #_(println (:value (last response-vals)))
    #_(with-out-str (pprint (doall response-vals)))))

(defn a-eval-file
  "Evaluate a file."
  [e]
  (eval-and-print (get-text-from-tab (current-tab-index)) false)
  #_(let [response-vals (nrepl/message (:client @repl) {:op "eval" :code (.getText (:text-area (get-editor)))})]
     (text! (:text-area @repl-output-window)
                          (with-out-str (doseq [resp response-vals]
                                          (when (:out resp) (println (:out resp)))
                                          (when (:value resp) (println (:value resp))))
                                        #_(with-out-str (pprint (doall response-vals)))))))

(defn filename-to-syntaxtype
 "Figure out the syntax type for a given file."
 [filename]
 #_(println "filename-to-syntaxtype" filename)
 (let [extension (string/split filename #"\.")]       
   (cond
     (= (last extension) "java") SyntaxConstants/SYNTAX_STYLE_JAVA 
     (= (last extension) "clj") SyntaxConstants/SYNTAX_STYLE_CLOJURE 
     :else SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
 

(defn make-project-window
  "Make a project window for a given project."
  [proj]
  (let [f (frame :title (str "Brevis - " (:name proj)) :width 600 :height 200 :minimum-size [800 :by 360])
        text-area (text :multi-line? true :font "MONOSPACED-PLAIN-14"
                        :text (with-out-str (pprint proj)))
        area (scrollable text-area)
        dialog (choose-file nil
                            :type :open
                            :dir (:directory proj)
                            :selection-mode :files-only
                            :remember-directory? false
                            :success-fn (fn [fc file]
                                          (.setSyntaxEditingStyle
                                            (:text-area (get-editor)) 
                                            (filename-to-syntaxtype (.toString file)))
                                          (.setText (:text-area (get-editor)) (slurp file))))]
    (display f area)
    (-> f pack! show!)
    (.setLocation f 850 0)      
    {:frame f
     :scrollable area
     :text-area text-area}))

(defn make-a-active-project
  "Make an action function for switching between projects."
  [proj]
  (fn [e]
    #_(:menus @editor-window)
    (make-project-window proj)
    #_(println "Switching project:" proj)))

(def ^{:private true} make-icon icon)



(defn add-content-tab
  "Add a tab to the content pane."
  [tab-map]
  (let [tp (:tabbed-panel @editor-window)
        {:keys [title content tip icon]} tab-map
        title-cmp (try-cast Component title)
        index (.getTabCount tp)]
    (cond-doto tp
               true (.addTab (if-not title-cmp (resource title)) (make-icon icon) (make-widget content) (resource tip))
               title-cmp (.setTabComponentAt index title-cmp))))

(defn add-content-tab-from-filename
  "Add a tab for a filename, autoconstruct the component."
  [filename params]
  (let [textArea (RSyntaxTextArea. 42 115)
        sp (RTextScrollPane. textArea)]
    (when (:keybinds params)
             (doseq [keybind (:keybinds params)]
               #_(println keybind)
               (.put (.getInputMap textArea)
                 (:keystroke keybind)
                 (:action keybind))
               #_(.put (.getActionMap textArea)
                  (:name keybind)
                  (:action keybind))))
    (.setSyntaxEditingStyle textArea (cond (= (:language params) :java) 
                                           (SyntaxConstants/SYNTAX_STYLE_JAVA)
                                           :else
                                           (SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
    (.setCodeFoldingEnabled textArea true)      
    (.setText textArea (slurp filename))
    (add-content-tab {:title (label;; title is not a string but a label component
                               :id (str (gensym "editor-panel"))
                               ;; current-filename isn't set when this function is called
                               :text filename #_(last (string/split @current-filename #"/")) #_@current-filename;; with text set to txt
                               :foreground :blue) 
                      ;:filename @current-filename 
                      :tip filename
                      :icon (.getIcon chooser (file filename))
                      :content (RTextScrollPane. textArea)})))
        
(defn make-editor-window
     "Make an editor window."
     [params]
     (let [;textArea (RSyntaxTextArea. 42 115)
           ;textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
           ;textArea.setCodeFoldingEnabled(true);      
           ;sp (RTextScrollPane. textArea)
           tabs (tabbed-panel :placement :top ;:overflow :wrap 
                              :tabs [#_{:title (label;; title is not a string but a label component
                                                    :id (str (gensym "editor-panel"))
                                                    ;; current-filename isn't set when this function is called
                                                    :text @current-filename #_(last (string/split @current-filename #"/")) #_@current-filename;; with text set to txt
                                                    :foreground :blue) 
                                       #_@current-filename 
                                       :tip @current-filename 
                                       :icon (.getIcon chooser (file @current-filename))
                                       :content sp}])
           a-new (action :handler a-new :name "New" :tip "Create a new file.")
           a-open (action :handler a-open :name "Open" :tip "Open a file")
           a-save (action :handler a-save :name "Save" :tip "Save the current file.")
           a-exit (action :handler a-exit :name "Exit" :tip "Exit the editor.")
           a-copy (action :handler a-copy :name "Copy" :tip "Copy selected text to the clipboard.")
           a-paste (action :handler a-paste :name "Paste" :tip "Paste text from the clipboard.")
           a-cut (action :handler a-cut :name "Cut" :tip "Cut text to the clipboard.")
           a-save-as (action :handler a-save-as :name "Save As" :tip "Save the current file.")
           a-eval-file (action :handler a-eval-file :name "Evaluate" :tip "Evaluate the current file.")
           a-projects (map #(action :handler (make-a-active-project %)
                                    :name (str (:group %) "/" (:name %))
                                    :tip (str (:group %) "/" (:name %)))
                           (:projects @current-profile))
           menus (menubar
                   :items [(menu :text "File" :items [a-new a-open a-save a-save-as a-exit])
                           (menu :text "Edit" :items [a-copy a-cut a-paste])
                           (menu :text "Run" :items [a-eval-file])
                           (menu :text "Projects" :items (into [] a-projects))
                           (menu :text "Git" :items [])
                           #_(menu :text "My Project" :items [(action :handler (fn [e] nil) :name "Open a project")
                                                             (action :handler (fn [e] nil) :name "in")
                                                             (action :handler (fn [e] nil) :name "projects menu")])])
           ;f (frame :title "Brevis - Editor Window" :menubar menus)
           ]
       #_(.addKeyListener textArea input-text-keyhandler)
       #_(when (:keybinds params)
          (doseq [keybind (:keybinds params)]
            #_(println keybind)
            (.put (.getInputMap textArea)
              (:keystroke keybind)
              (:action keybind))
            #_(.put (.getActionMap textArea)
               (:name keybind)
               (:action keybind))))
       #_(.setSyntaxEditingStyle textArea (cond (= (:language params) :java) 
                                               (SyntaxConstants/SYNTAX_STYLE_JAVA)
                                               :else
                                               (SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
       #_(.setCodeFoldingEnabled textArea true)      
       #_(display f sp)
       #_(-> f pack! show!)
       #_(.setLocation f 0 0)      
       {;:frame f
        ;:text-area textArea
        :tabbed-panel tabs
        ;:scroll-pane sp
        :menus menus
        }))

(defn get-repl-inputstream
  "Return the input stream for the REPL."
  []
  @repl-inputstream)

(defn get-repl-outputstream
  "Return the output stream for the REPL."
  []
  @repl-outputstream)

(defn make-repl-input-window
     "Make a REPL input window."
     [params]
     (let [textArea (RSyntaxTextArea. 25 115)          
           ;textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
           ;textArea.setCodeFoldingEnabled(true);      
           sp (RTextScrollPane. textArea)
           a-new (action :handler a-new :name "New" :tip "Create a new file.")
           a-open (action :handler a-open :name "Open" :tip "Open a file")
           a-save (action :handler a-save :name "Save" :tip "Save the current file.")
           a-exit (action :handler a-exit :name "Exit" :tip "Exit the editor.")
           a-copy (action :handler a-copy :name "Copy" :tip "Copy selected text to the clipboard.")
           a-paste (action :handler a-paste :name "Paste" :tip "Paste text from the clipboard.")
           a-cut (action :handler a-cut :name "Cut" :tip "Cut text to the clipboard.")
           a-save-as (action :handler a-save-as :name "Save As" :tip "Save the current file.")
           menus (menubar
                   :items [#_(menu :text "File" :items [a-new a-open a-save a-save-as a-exit])
                           (menu :text "Edit" :items [a-copy a-cut a-paste])])
           ;f (frame :title "Brevis - REPL Input" :menubar menus)
           ]
       (.addKeyListener textArea 
         (proxy [java.awt.event.KeyAdapter] []          
           (keyPressed [#^java.awt.event.KeyEvent e]
             (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_UP)
               (try 
                 (let [startPosition 0]
                   (.remove (.getDocument textArea) startPosition (- (.getLength (.getDocument textArea)) startPosition 1))                  
                   (.setText textArea (nth @repl-input (- (dec (count @repl-input)) @repl-input-index)))
                   (reset! repl-input-index (min (inc @repl-input-index) (dec (count @repl-input))))
                   #_(.setText textArea (last @repl-input)))
                 (catch Exception e (println (.getMessage e))))
               (.consume e))
             (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_DOWN)
               (try 
                 (let [startPosition 0]
                   (.remove (.getDocument textArea) startPosition (- (.getLength (.getDocument textArea)) startPosition 1))                  
                   (.setText textArea (nth @repl-input (- (dec (count @repl-input)) @repl-input-index)))
                   (reset! repl-input-index (max (dec @repl-input-index) 0))
                   #_(.setText textArea (last @repl-input)))
                 (catch Exception e (println (.getMessage e))))
               (.consume e))
             (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_ENTER)
               (.append textArea "\n")
               ;; This shouldn't be here:
               #_(text! (:text-area @repl-output-window) (.toString (get-repl-outputstream)))
               (try 
                 (let [startPosition 0
                       line (.getText textArea startPosition (- (.getLength (.getDocument textArea)) startPosition 1))
                       ;response-vals (nrepl/message (:client @repl) {:op "eval" :code line})
                       #_session-sender #_(nrepl/client-session @reply.eval-modes.nrepl/current-connection :session @reply.eval-modes.nrepl/current-session)]
                   (reset! repl-input-index 0)
                   (.setText textArea "")
                   (eval-and-print line true)           
                   ;; Add to a console history
                   )
                 (catch Exception e (println (.getMessage e))))
               (.consume e)))))
       (.setSyntaxEditingStyle textArea (cond (= (:language params) :java) 
                                              (SyntaxConstants/SYNTAX_STYLE_JAVA)
                                              :else
                                              (SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
       #_(.setCodeFoldingEnabled textArea true)      
       #_(display f sp)
       #_(-> f pack! show!)
       #_(println (.getLocation f))
       #_(.setLocation f 0 680)      
       {;:frame f
        :text-area textArea
        :scroll-pane sp
        :menus menus}))

(defn make-repl-output-window
    "Make an editor window."
    [params]
    (let [;f (frame :title "Brevis - REPL Output" :width 800 :height 200 :minimum-size [800 :by 360])
          text-area (text :multi-line? true :font "MONOSPACED-PLAIN-14"
                                           :text "> ")
          area (scrollable text-area)
          listener-thread (Thread. (fn []  
                                     (loop [] ;; should probably at least check something for a termination condition
                                       (let [resp (nrepl.transport/recv (:client @repl) 20)]
                                         (when resp
                                           (when (:out resp) (write-stdout-repl (:out resp)))
                                           (when (:value resp) (write-value-repl (:value resp)))))
                                       (recur))))]
     #_(display f area)
     #_(-> f pack! show!)
     #_(.setLocation f 850 650)      
     {;:frame f
      :scrollable area
      :listener-thread listener-thread
      :text-area text-area}))


;; Some of the project browser code from https://github.com/daveray/seesaw/blob/develop/test/seesaw/test/examples/explorer.clj
(defn tree-project-model
  []
  (simple-tree-model
    #(.isDirectory %) 
    #_(fn [f] (filter #(.isDirectory %) (.listFiles f)))
    (fn [f] (filter #(or (.isDirectory %) (.isFile %)) (.listFiles f)))
    (File. (:workspace-directory @current-profile))
    #_(File. ".")))

(defn render-file-item
  [renderer {:keys [value]}]
  (config! renderer :text (.getName value)
           :icon (.getIcon chooser value)))

(defn make-project-browser
  "Make a widget for browsing projects."
  [params]
  (let [pt (tree :id :project-tree :model (tree-project-model) :renderer render-file-item)
        browser (scrollable pt)]
    {:project-tree pt
     :scrollable browser}))

(defn make-ui
  "Make a multi-widget UI."
  []
  (let [a-new (action :handler a-new :name "New" :tip "Create a new file.")
        a-open (action :handler a-open :name "Open" :tip "Open a file")
        a-save (action :handler a-save :name "Save" :tip "Save the current file.")
        a-exit (action :handler a-exit :name "Exit" :tip "Exit the editor.")
        a-copy (action :handler a-copy :name "Copy" :tip "Copy selected text to the clipboard.")
        a-paste (action :handler a-paste :name "Paste" :tip "Paste text from the clipboard.")
        a-cut (action :handler a-cut :name "Cut" :tip "Cut text to the clipboard.")
        a-save-as (action :handler a-save-as :name "Save As" :tip "Save the current file.")
        a-eval-file (action :handler a-eval-file :name "Evaluate" :tip "Evaluate the current file.")
        a-restart-brevis
        (action :handler (fn [e]
                           (System/exit 1))
                :name "Restart Brevis"
                :tip "Completely restarts the application")
        a-projects (map #(action :handler (make-a-active-project %)
                                 :name (str (:group %) "/" (:name %))
                                 :tip (str (:group %) "/" (:name %)))
                        (:projects @current-profile))
        menus (menubar
                :items [(menu :text "File" :items [a-new a-open a-save a-save-as a-exit])
                        (menu :text "Edit" :items [a-copy a-cut a-paste])
                        (menu :text "Run" :items [a-eval-file])
                        (menu :text "Projects" :items (into [] a-projects))
                        (menu :text "Git" :items [])
                        (menu :text "Brevis" :items [a-restart-brevis]) 
                        #_(menu :text "My Project" :items [(action :handler (fn [e] nil) :name "Open a project")
                                                          (action :handler (fn [e] nil) :name "in")
                                                          (action :handler (fn [e] nil) :name "projects menu")])])
        panel (mig-panel :constraints [#_"wrap 2"]
                         :items [[#_(:scroll-pane (get-editor))
                                  (left-right-split
                                    (:scrollable @project-browser)
                                    (:tabbed-panel (get-editor)) #_(:scroll-pane (get-editor))
                                    :divider-location 0.2) "wrap, span 2, w 100%, h 75%"]                                 
                                 [(:scroll-pane @repl-input-window) "w 50%, h 25%"]
                                 [(:scrollable @repl-output-window) "w 50%, h 25%"]]
                         #_[[(:scroll-pane (get-editor)) "wrap, span 2, w 100%, h 75%"]
                           [(:scroll-pane @repl-input-window) "w 50%, h 25%"]
                           [(:scrollable @repl-output-window) "w 50%, h 25%"]])
        f (frame :title "Brevis - UI" :width 1024 :height 768 #_:minimum-size #_[800 :by 600] :menubar menus)]
    (listen (:project-tree @project-browser) #_(select f [:#project-tree]) :selection
            (fn [e]
              (if-let [dir (last (selection e))]
                (let [files (.listFiles dir)]
                  (when (and (.isFile dir)
                             (or (.contains (.toString dir) "clj")
                                 (.contains (.toString dir) "java")))
                    #_(reset! current-filename (.toString dir))
                    #_(.setText (:text-area (get-editor)) (slurp @current-filename))
                    (open-file (.toString dir)))
                  #_(println dir)
                  (config! (select f [:#current-dir]) :text (.getAbsolutePath dir))
                  (config! (select f [:#status]) :text (format "Ready (%d items)" (count files)))
                  (config! (select f [:#list]) :model files)))))
    (display f panel)
    (-> f #_pack! show!)
    (.setLocation f 0 0)      
    {:frame f
     :panel panel}))

(defn -main 
  "Start from command line."
  [& args]
  (init-ui)
  (let [params {:language :clojure
                :keybinds keybinds/standard}
        ew (make-editor-window params)
        ri (make-repl-input-window params)
        ro (make-repl-output-window params)
        r-is System/in #_(java.io.ByteArrayInputStream.
                          #_(.getBytes "(println 'foobar)\nexit\n(println 'foobar)\n"))
        r-os (java.io.ByteArrayOutputStream.)
        project (project/read (str (:current-project @current-profile) File/separator "project.clj"))
        #_(project/read project-filename)
        #_(assoc (project/read (str (:current-project @current-profile) "/project.clj") #_project-filename)
                :repl-options {:input-stream r-is :output-stream r-os})
        repl-cfg {:host (repl/repl-host project)
                  :port (repl/repl-port project)}
        ;repl-server-port (repl/server project repl-cfg false)
        repl-server (nrepl.server/start-server :port 59258)
        ;repl-client-thread (Thread. (fn [] (repl/client project repl-server-port) ))
        repl-connection (nrepl/connect :port 59258)
        repl-client (nrepl/client repl-connection Long/MAX_VALUE)
        #_(lein-main/apply-task "repl" project [])
        #_(apply eval/eval-in-project project
                           (server-forms project cfg (ack-port project)
                                         true))
        #_(apply eval/eval-in-project project
                              (server-forms project cfg (ack-port project)
                                            true))
        pb (make-project-browser {})
        ]    
    #_(.start repl-client-thread)
    (reset! content-pane-params params)
    (reset! repl {:server repl-server ;:client-thread repl-client-thread
                  ;:repl-inputstream r-is :repl-outputstream r-os})
                  ;:client (nrepl/client @reply.eval-modes.nrepl/current-connection Long/MAX_VALUE)
                  :connection repl-connection
                  :client repl-client})
                  ;:repl-inputstream r-is :repl-outputstream r-os})
    (reset! repl-inputstream r-is)
    (reset! repl-outputstream r-os)
    (reset! editor-window ew)
    (reset! project-browser pb)
    (reset! repl-input-window ri)
    (reset! repl-output-window ro)
    (make-ui)
    (load-file (str "src" File/separator "brevis" File/separator "ui" File/separator "keybinds.clj"))
    "Launched BrIDE"
    #_(add-content-tab-from-filename @current-filename params)
    #_(when-not (empty? (:current-filename @current-profile))      
       (.setText (:text-area ew) (slurp @current-filename)))))

(when (find-ns 'ccw.complete)
  (-main))
