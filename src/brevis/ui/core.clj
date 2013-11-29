(ns brevis.ui.core
  (:gen-class)
  (:import (org.fife.ui.rsyntaxtextarea RSyntaxTextArea SyntaxConstants
                                        TokenMakerFactory)	
           (org.fife.ui.rtextarea RTextScrollPane)
           [javax.swing JFileChooser JEditorPane JScrollPane BorderFactory]
           (java.awt.event KeyAdapter)
           (java.io ByteArrayInputStream)
           java.awt.Font)
  (:require  
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
        [seesaw core font color graphics chooser]
        [brevis.ui.profile]))

;; ## Globals

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

(def repl-outputstream
  (atom nil))

;;

(def current-filename (atom nil))

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

(defn select-file []
  (let [chooser (JFileChooser.)]
    (.showDialog chooser nil "Select")
    (.getSelectedFile chooser)))

(defn a-new [e]
  (let [selected (select-file)] 
    (if (.exists (file selected))
      (alert "File already exists.")
      (do #_(set-current-file selected)
          (.setText (:text-area (get-editor)) "")
          #_(set-status "Created a new file.")))))

(defn a-open [e]
  (let [selected (select-file)] #_(set-current-file selected)
    (.setText (:text-area (get-editor)) (slurp selected))
    #_(set-status "Opened " filename ".")))

(defn a-save [e]
  (spit @current-filename (.getText (:text-area (get-editor))))
  #_(set-status "Wrote " filename "."))

(defn a-save-as [e]
  (when-let [selected (select-file)]
    #_(set-current-file selected)
    (spit selected (.getText (:text-area (get-editor))))
    #_(set-status "Wrote " filename ".")))

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
  (eval-and-print (.getText (:text-area (get-editor))) false)
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

(defn make-editor-window
    "Make an editor window."
    [params]
    (let [
          textArea (RSyntaxTextArea. 42 115)
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
          f (frame :title "Brevis - Editor Window" :menubar menus)]
      (.setSyntaxEditingStyle textArea (cond (= (:language params) :java) 
                                             (SyntaxConstants/SYNTAX_STYLE_JAVA)
                                             :else
                                             (SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
      (.setCodeFoldingEnabled textArea true)      
      (display f sp)
      (-> f pack! show!)
      (.setLocation f 0 0)      
      {:frame f
       :text-area textArea
       :scroll-pane sp
       :menus menus}))

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
          f (frame :title "Brevis - REPL Input" :menubar menus)]
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
      (display f sp)
      (-> f pack! show!)
      #_(println (.getLocation f))
      (.setLocation f 0 680)      
      {:frame f
       :text-area textArea
       :scroll-pane sp
       :menus menus}))

(defn make-repl-output-window
   "Make an editor window."
   [params]
   (let [f (frame :title "Brevis - REPL Output" :width 800 :height 200 :minimum-size [800 :by 360])
         text-area (text :multi-line? true :font "MONOSPACED-PLAIN-14"
                                          :text "> ")
         area (scrollable text-area)
         listener-thread (Thread. (fn []  
                                    (loop []
                                      (let [resp (nrepl.transport/recv (:client @repl) 20)]
                                        (when resp
                                          (when (:out resp) (write-stdout-repl (:out resp)))
                                          (when (:value resp) (write-value-repl (:value resp)))))
                                      (recur))))]
    (display f area)
    (-> f pack! show!)
    (.setLocation f 850 650)      
    {:frame f
     :scrollable area
     :listener-thread listener-thread
     :text-area text-area}))

(defn -main 
  "Start from command line."
  [& args]
  (init-ui)
  (let [ew (make-editor-window {:language :clojure})
        ri (make-repl-input-window {:language :clojure})
        ro (make-repl-output-window {:language :clojure})
        r-is System/in #_(java.io.ByteArrayInputStream.
                          #_(.getBytes "(println 'foobar)\nexit\n(println 'foobar)\n"))
        r-os (java.io.ByteArrayOutputStream.)
        project (project/read (str (:current-project @current-profile) "/project.clj"))
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
                                            true))]
    #_(.start repl-client-thread)
    (reset! repl {:server repl-server ;:client-thread repl-client-thread
                  ;:repl-inputstream r-is :repl-outputstream r-os})
                  ;:client (nrepl/client @reply.eval-modes.nrepl/current-connection Long/MAX_VALUE)
                  :connection repl-connection
                  :client repl-client})
                  ;:repl-inputstream r-is :repl-outputstream r-os})
    (reset! repl-inputstream r-is)
    (reset! repl-outputstream r-os)
    (reset! editor-window ew)
    (reset! repl-input-window ri)
    (reset! repl-output-window ro)
    (reset! current-filename (:current-filename @current-profile))
    (.setText (:text-area ew) (slurp @current-filename))))

(when (find-ns 'ccw.complete)
  (-main))
