/*
 * Copyright (c) 2011, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @bug 8160266 8225790
 * @key headful
 * @summary See <rdar://problem/3429130>: Events: actionPerformed() method not
 *          called when it is button is clicked (system load related)
 * @run main NestedModalDialogTest
 */

//////////////////////////////////////////////////////////////////////////////
//  NestedModalDialogTest.java
// The test launches a parent frame. From this parent frame it launches a modal
// dialog. From the modal dialog it launches a second modal dialog with a text
// field in it and tries to write into the text field. The test succeeds if you
// are successfully able to write into this second Nested Modal Dialog
//////////////////////////////////////////////////////////////////////////////
// classes necessary for this test

import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class NestedModalDialogTest {
    private static StartFrame frame;
    private static IntermediateDialog interDiag;
    private static TextDialog txtDiag;

    // Global variables so the robot thread can locate things.
    private static TextField robot_text = null;
    private static Robot robot = null;

    private static void blockTillDisplayed(Component comp) {
        Point p = null;
        while (p == null) {
            try {
                p = comp.getLocationOnScreen();
            } catch (IllegalStateException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private static void clickOnComp(Component comp) {
        robot.waitForIdle();
        robot.delay(1000);

        Rectangle bounds = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
        robot.mouseMove(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        robot.waitForIdle();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
    }

    public void testModalDialogs() throws Exception {
        try {
            robot = new Robot();
            robot.setAutoDelay(100);

            // launch first frame with firstButton
            frame = new StartFrame();
            blockTillDisplayed(frame);
            clickOnComp(frame.button);

            // Dialog must be created and onscreen before we proceed.
            blockTillDisplayed(interDiag);
            clickOnComp(interDiag.button);

            // Again, the Dialog must be created and onscreen before we proceed.
            blockTillDisplayed(robot_text);
            clickOnComp(robot_text);

            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_H);
            robot.keyRelease(KeyEvent.VK_H);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            robot.waitForIdle();

            robot.keyPress(KeyEvent.VK_E);
            robot.keyRelease(KeyEvent.VK_E);
            robot.waitForIdle();

            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.waitForIdle();

            robot.keyPress(KeyEvent.VK_L);
            robot.keyRelease(KeyEvent.VK_L);
            robot.waitForIdle();

            robot.keyPress(KeyEvent.VK_O);
            robot.keyRelease(KeyEvent.VK_O);
            robot.waitForIdle();
        } finally {
            if (frame != null) {
                frame.dispose();
            }
            if (interDiag != null) {
                interDiag.dispose();
            }
            if (txtDiag != null) {
                txtDiag.dispose();
            }
        }
    }

    //////////////////// Start Frame ///////////////////
    /**
     * Launches the first frame with a button in it
     */
    class StartFrame extends Frame {

        public volatile Button button;

        /**
         * Constructs a new instance.
         */
        public StartFrame() {
            super("First Frame");
            setLayout(new GridBagLayout());
            setLocation(375, 200);
            setSize(271, 161);
            Button but = new Button("Make Intermediate");
            but.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    interDiag = new IntermediateDialog(StartFrame.this);
                    interDiag.setSize(300, 200);

                    // may need listener to watch this move.
                    interDiag.setLocation(getLocationOnScreen());
                    interDiag.pack();
                    interDiag.setVisible(true);
                }
            });
            Panel pan = new Panel();
            pan.add(but);
            add(pan);
            setVisible(true);
            button = but;
        }
    }

    ///////////////////////////// MODAL DIALOGS /////////////////////////////
    /* A Dialog that launches a sub-dialog */
    class IntermediateDialog extends Dialog {

        Dialog m_parent;
        public volatile Button button;

        public IntermediateDialog(Frame parent) {
            super(parent, "Intermediate Modal", true /*Modal*/);
            m_parent = this;
            Button but = new Button("Make Text");
            but.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    txtDiag = new TextDialog(m_parent);
                    txtDiag.setSize(300, 100);
                    txtDiag.setVisible(true);
                }
            });
            Panel pan = new Panel();
            pan.add(but);
            add(pan);
            pack();
            button = but;
        }
    }

    /* A Dialog that just holds a text field */
    class TextDialog extends Dialog {

        public TextDialog(Dialog parent) {
            super(parent, "Modal Dialog", true /*Modal*/);
            TextField txt = new TextField("", 10);
            Panel pan = new Panel();
            pan.add(txt);
            add(pan);
            pack();

            // The robot needs to know about us, so set global
            robot_text = txt;
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new NestedModalDialogTest().testModalDialogs();
        } catch (Exception e) {
            throw new RuntimeException("NestedModalDialogTest object creation "
                    + "failed", e);
        }
    }
}
