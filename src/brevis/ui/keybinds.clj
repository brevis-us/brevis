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

(ns brevis.ui.keybinds
  (:import (javax.swing KeyStroke)
           (java.awt.event InputEvent)
           (javax.swing.text DefaultEditorKit)
           (java.awt Toolkit)))

(def standard
   [#_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_LEFT
                                                     java.awt.event.InputEvent/META_DOWN_MASK)
      :name "Beginning of line"
      :action DefaultEditorKit/beginLineAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_RIGHT
                                                     java.awt.event.InputEvent/META_DOWN_MASK)
      :name "End of line"
      :action DefaultEditorKit/endLineAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_Y
                                                     java.awt.event.InputEvent/CTRL_DOWN_MASK)
      :name "Paste"
      :action DefaultEditorKit/pasteAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_W
                                                     java.awt.event.InputEvent/CTRL_DOWN_MASK)
      :name "Cut"
      :action DefaultEditorKit/cutAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_W
                                                     java.awt.event.InputEvent/META_DOWN_MASK)
      :name "Copy"
      :action DefaultEditorKit/copyAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_F
                                                     #_(.getMenuShortcutKeyMask (java.awt.Toolkit/getDefaultToolkit))
                                                     java.awt.event.InputEvent/ALT_MASK)
      :name "Forward word"
      :action DefaultEditorKit/forwardAction}
    #_{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_B
                                                     #_(.getMenuShortcutKeyMask (java.awt.Toolkit/getDefaultToolkit)) 
                                                     java.awt.event.InputEvent/ALT_MASK)
      :name "Backward word"
      :action DefaultEditorKit/backwardAction}
    ])

;; Emacs keybinds

#_(add-keybind-handler
   "Start of line"
   {:context :input-text, :type :keyPressed}
   (fn [e] (and (= (.getKeyCode e) java.awt.event.KeyEvent/VK_CONTROL)
                (= (.getKeyCode e) java.awt.event.KeyEvent/VK_A)))
   (fn [e context] (println "start of line") (.setCaretPosition (:text-area context) 0)))

#_(add-keybind-handler
   "End of line"
   {:context :input-text, :type :keyPressed}
   (fn [e] (and (= (.getKeyCode e) java.awt.event.KeyEvent/VK_CONTROL)
                (= (.getKeyCode e) java.awt.event.KeyEvent/VK_E)))
   (fn [e context] (println "end of line") (.setCaretPosition (:text-area context) (.size (.getText (:text-area context))))))

#_(def emacs
   [{:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_A
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Beginning of line"
     :action DefaultEditorKit/beginLineAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_E
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "End of line"
     :action DefaultEditorKit/endLineAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_Y
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Paste"
     :action DefaultEditorKit/pasteAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_W
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Cut"
     :action DefaultEditorKit/cutAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_W
                                                    java.awt.event.InputEvent/META_DOWN_MASK)
     :name "Copy"
     :action DefaultEditorKit/copyAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_E
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "End of line"
     :action DefaultEditorKit/endLineAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_P
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Previous line"
     :action DefaultEditorKit/upAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_N
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Next line"
     :action DefaultEditorKit/downAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_F
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Forward character"
     :action DefaultEditorKit/forwardAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_B
                                                    java.awt.event.InputEvent/CTRL_DOWN_MASK)
     :name "Backward character"
     :action DefaultEditorKit/backwardAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_F
                                                    #_(.getMenuShortcutKeyMask (java.awt.Toolkit/getDefaultToolkit))
                                                    java.awt.event.InputEvent/ALT_MASK)
     :name "Forward word"
     :action DefaultEditorKit/forwardAction}
    {:keystroke (javax.swing.KeyStroke/getKeyStroke java.awt.event.KeyEvent/VK_B
                                                    #_(.getMenuShortcutKeyMask (java.awt.Toolkit/getDefaultToolkit)) 
                                                    java.awt.event.InputEvent/ALT_MASK)
     :name "Backward word"
     :action DefaultEditorKit/backwardAction}
    ])
    ;component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
;                            java.awt.event.InputEvent.CTRL_DOWN_MASK),
;                    "actionMapKey");
;component.getActionMap().put("actionMapKey",
;                     someAction);
