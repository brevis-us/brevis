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
    [clojure.tools.nrepl :as nrepl]
    [leiningen.repl :as repl]
    [leiningen.core.project :as project]
    [leiningen.core.eval :as eval]
    [leiningen.core.main :as lein-main])
  (:use [clojure.java.io :only [file]] 
        [seesaw core font color graphics]))

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

;https://encrypted.google.com/url?sa=t&rct=j&q=swing%20inputstream%20outputstream&source=web&cd=7&cad=rja&ved=0CGYQFjAG&url=http%3A%2F%2Fzcu.arcao.com%2Fkiv%2Fos%2Fsemestralka%2Fspravce-virtualnich-stroju%2Fodevzdani%2Fsrc%2Fcz%2Fzcu%2Fkiv%2Fos%2Fconsole%2FConsoleWindow.java&ei=xLWUUv3MO4SgyAG1toGwCw&usg=AFQjCNG9ep9O6ku06YyWEmyXCR3pJULixQ&sig2=vf_-K5aeDpaffIK-fgFp9A&bvm=bv.57155469,d.aWc

(def project-filename "/Users/kyle/git/brevis/project.clj")
(def filename "/Users/kyle/git/brevis/src/brevis/example/swarm.clj")

;(def screen-resolution 

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
    (if (.exists (file filename))
      (alert "File already exists.")
      (do #_(set-current-file selected)
          (.setText (get-editor) "")
          #_(set-status "Created a new file.")))))

(defn a-open [e]
  (let [selected (select-file)] #_(set-current-file selected))
  (.setText (get-editor) (slurp filename))
  #_(set-status "Opened " filename "."))

(defn a-save [e]
  (spit filename (.getText (get-editor)))
  #_(set-status "Wrote " filename "."))

(defn a-save-as [e]
  (when-let [selected (select-file)]
    #_(set-current-file selected)
    (spit filename (.getText (get-editor)))
    #_(set-status "Wrote " filename ".")))

(defn a-exit  [e] (System/exit 0))
(defn a-copy  [e] (.copy (get-editor)))
(defn a-cut   [e] (.cut (get-editor)))
(defn a-paste [e] (.paste (get-editor)))

(defn a-eval-file
  "Evaluate a file."
  [e]
  (eval/eval-in (project/read project-filename)
                (.getText (get-editor))))

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
          menus (menubar
                  :items [(menu :text "File" :items [a-new a-open a-save a-save-as a-exit])
                          (menu :text "Edit" :items [a-copy a-cut a-paste])
                          (menu :text "Run" :items [a-eval-file])])
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
            (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_ENTER)
              (.append textArea "\n")
              ;; This shouldn't be here:
              (text! (:text-area @repl-output-window) (.toString (get-repl-outputstream)))
              (try 
                (let [startPosition 0
                      line (.getText textArea startPosition (- (.getLength (.getDocument textArea)) startPosition 1))]
                  (.setText textArea "")
                  (println "Sending to repl:" line)      
                  (reply.eval-modes.nrepl/execute-with-client (:client @repl) #_@reply.eval-modes.nrepl/current-connection
                            (assoc {}
                                   :read-input-line-fn (partial reply.reader.simple-jline/safe-read-line {:no-jline true :prompt-string ""})                                                          
                                   :interactive true)
                            line)
                  #_(java.io.ByteArrayInputStream.
                     (.getBytes "(println 'foobar)\nexit\n(println 'foobar)\n"))
                  #_(System/setIn (ByteArrayInputStream. (.getBytes (str line "\n") "UTF-8")))                    
                  #_(.add (get-repl-inputstream) (str line "\n"))                  
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

#_(defn repl-output-window
     "Make a REPL output window."
     [params]
     (let [
           textArea (RSyntaxTextArea. 25 115)
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
                   :items [(menu :text "File" :items [a-save a-save-as a-exit])
                           (menu :text "Edit" :items [a-copy a-cut a-paste])])
           f (frame :title "Brevis - REPL Output" :menubar menus)]
       (.setSyntaxEditingStyle textArea (cond (= (:language params) :java) 
                                              (SyntaxConstants/SYNTAX_STYLE_JAVA)
                                              :else
                                              (SyntaxConstants/SYNTAX_STYLE_CLOJURE)))
       (.setCodeFoldingEnabled textArea true)      
       (display f sp)
       (-> f pack! show!)
       #_(println (.getLocation f))
       (.setLocation f 850 680)      
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
         area (scrollable text-area)]
    (display f area)
    (-> f pack! show!)
    (.setLocation f 850 650)      
    {:frame f
     :scrollable area
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
        project #_(project/read project-filename)
        (assoc (project/read project-filename)
               :repl-options {:input-stream r-is :output-stream r-os})
        repl-cfg {:host (repl/repl-host project)
                  :port (repl/repl-port project)}
        repl-server-port (repl/server project repl-cfg false)
        repl-client-thread (Thread. (fn [] (repl/client project repl-server-port) ))          
        #_(lein-main/apply-task "repl" project [])
        #_(apply eval/eval-in-project project
                           (server-forms project cfg (ack-port project)
                                         true))
        #_(apply eval/eval-in-project project
                              (server-forms project cfg (ack-port project)
                                            true))]
    (.start repl-client-thread)
    (reset! repl {:server repl-server-port :client-thread repl-client-thread
                  ;:repl-inputstream r-is :repl-outputstream r-os})
                  :client (nrepl/client @reply.eval-modes.nrepl/current-connection Long/MAX_VALUE)
                  :repl-inputstream r-is :repl-outputstream r-os})
    (reset! repl-inputstream r-is)
    (reset! repl-outputstream r-os)
    (reset! editor-window ew)
    (reset! repl-input-window ri)
    (reset! repl-output-window ro)
    (.setText (:text-area ew) (slurp filename))))

(when (find-ns 'ccw.complete)
  (-main))
