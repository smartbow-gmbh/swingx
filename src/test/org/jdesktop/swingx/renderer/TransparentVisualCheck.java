/*
 * $Id$
 *
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.ListModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.InteractiveTestCase;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.action.ActionContainerFactory;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ConditionalHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter.UIAlternateRowHighlighter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
import org.jdesktop.test.AncientSwingTeam;

/**
 * Experiments with transparent highlighters.<p>
 * 
 * Links
 * <ul>
 * <li> <a href="">Sneak preview II - Transparent Highlighter</a>
 * </ul>
 * 
 * @author Jeanette Winzenburg
 */
public class TransparentVisualCheck extends InteractiveTestCase {
    public static void main(String args[]) {
//      setSystemLF(true);
      TransparentVisualCheck test = new TransparentVisualCheck();
      try {
         test.runInteractiveTests();
      } catch (Exception e) {
          System.err.println("exception when executing interactive tests:");
          e.printStackTrace();
      }
  }
    
//  ---------------------- Transparent gradients on PainterAwareLabel
    
    /**
     * Use GradientPainter for value-based background highlighting with SwingX
     * extended default renderer. Trying to get the highlighter transparent:
     * the background color of the cell should shine through in the "white"
     * region of the value-hint. This uses a PainterAwareLabel as rendering 
     * component
     */
    public void interactiveTransparentGradientHighlightPlusStriping() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        table.setBackground(Highlighter.ledgerBackground.getBackground());
        RenderingComponentController<JLabel> numberRendering = new RenderingLabelController(
                JLabel.RIGHT) {
                    @Override
                    protected JLabel createRendererComponent() {
                        return new PainterAwareLabel();
                    }
            
        };
        table.setDefaultRenderer(Number.class, new DefaultTableRenderer(
                numberRendering));
        final TransparentGradientHighlighter gradientHighlighter = createTransparentGradientHighlighter();
        Highlighter alternateRowHighlighter = new UIAlternateRowHighlighter();
        table.addHighlighter(alternateRowHighlighter);
        table.addHighlighter(gradientHighlighter);
        // re-use component controller and highlighter in a JXList
        JXList list = new JXList(createListNumberModel(), true);
        list.setCellRenderer(new DefaultListRenderer(numberRendering));
        list.addHighlighter(alternateRowHighlighter);
        list.addHighlighter(gradientHighlighter);
        list.toggleSortOrder();
        final JXFrame frame = showWithScrollingInFrame(table, list,
                "transparent value relative highlighting plus striping");
        addStatusMessage(frame,
                "uses a PainterAwareLabel in renderer");
        // crude binding to play with options - the factory is incomplete...
        ActionContainerFactory factory = new ActionContainerFactory();
        // toggle opaque optimatization
        AbstractActionExt overrideOpaque = new AbstractActionExt("yellow transparent") {

            public void actionPerformed(ActionEvent e) {
                gradientHighlighter.setYellowTransparent(isSelected());
                frame.repaint();
            }
            
        };
        overrideOpaque.setStateAction();
        JCheckBox box = new JCheckBox();
        factory.configureButton(box, overrideOpaque, null);
        getStatusBar(frame).add(box);
        frame.pack();
    }

    /**
     * Use GradientPainter for value-based background highlighting with SwingX
     * extended default renderer. Trying to get the highlighter transparent:
     * the background color of the cell should shine through in the "white"
     * region of the value-hint. This uses a PainterAwareLabel as rendering 
     * component
     */
    public void interactiveTransparentGradientHighlight() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        table.setBackground(Highlighter.ledgerBackground.getBackground());
        RenderingComponentController<JLabel> numberRendering = new RenderingLabelController(
                JLabel.RIGHT) {
                    @Override
                    protected JLabel createRendererComponent() {
                        return new PainterAwareLabel();
                    }
            
        };
        table.setDefaultRenderer(Number.class, new DefaultTableRenderer(
                numberRendering));
        Highlighter gradientHighlighter = createTransparentGradientHighlighter();
        table.addHighlighter(gradientHighlighter);
        // re-use component controller and highlighter in a JXList
        JXList list = new JXList(createListNumberModel(), true);
        list.setBackground(table.getBackground());
        list.setCellRenderer(new DefaultListRenderer(numberRendering));
        list.addHighlighter(gradientHighlighter);
        list.toggleSortOrder();
        JXFrame frame = showWithScrollingInFrame(table, list,
                "transparent value relative highlighting");
        addStatusMessage(frame,
                "uses a PainterAwareLabel in renderer");
        frame.pack();
    }


    public static class PainterAwareLabel extends JRendererLabel {
        @Override
        protected void paintComponent(Graphics g) {
            if (painter != null) {
                // we have a custom (background) painter
                // try to inject if possible
                // there's no guarantee - some LFs have their own background 
                // handling  elsewhere
                if (isOpaque()) {
                    // replace the paintComponent completely 
                    paintComponentWithPainter((Graphics2D) g);
                } else {
                    // transparent apply the background painter before calling super
                    painter.paint((Graphics2D) g, this);
                    super.paintComponent(g);
                }
            } else {
                // nothing to worry about - delegate to super
                super.paintComponent(g);
            }
        }

        /**
         * PRE: painter != null, isOpaque()
         * @param g
         */
        protected void paintComponentWithPainter(Graphics2D g) {
            // 1. be sure to fill the background
            // 2. paint the painter
            // by-pass ui.update and hook into ui.paint directly
            if (ui != null) {
                Graphics scratchGraphics = (g == null) ? null : g.create();
                try {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    painter.paint(g, this);
                    ui.paint(scratchGraphics, this);
                }
                finally {
                    scratchGraphics.dispose();
                }
            }
            
        }

        /**
         * called from super.paint - overridden to do nothing if the painter
         * is called in super.paint.
         */
        @Override
        protected void paintPainter(Graphics2D g) {
        }
        
        
        
        
    }
    
//----------------- Transparent gradient on default (swingx) rendering label
    /**
         * Use GradientPainter for value-based background highlighting with SwingX
         * extended default renderer. Trying to get the highlighter transparent:
         * the background color of the cell should shine through in the "white"
         * region of the value-hint.
         */
        public void interactiveTableAndListNumberProportionalGradientHighlight() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        table.setBackground(Highlighter.ledgerBackground.getBackground());
        RenderingComponentController<JLabel> numberRendering = new RenderingLabelController(
                JLabel.RIGHT);
        table.setDefaultRenderer(Number.class, new DefaultTableRenderer(
                numberRendering));
        Highlighter gradientHighlighter = createTransparentGradientHighlighter();
        table.addHighlighter(gradientHighlighter);
        // re-use component controller and highlighter in a JXList
        JXList list = new JXList(createListNumberModel(), true);
        list.setBackground(table.getBackground());
        list.setCellRenderer(new DefaultListRenderer(numberRendering));
        list.addHighlighter(gradientHighlighter);
        list.toggleSortOrder();
        JXFrame frame = showWithScrollingInFrame(table, list,
                "transparent value relative highlighting");
        addStatusMessage(frame,
                "uses the default painter-aware label in renderer");
        frame.pack();
    }


//---------------- Transparent renderer on experimenting rendering label
        
        /**
         * Use GradientPainter for value-based background highlighting with SwingX
         * extended default renderer. Trying to get the highlighter transparent:
         * the background color of the cell should shine through in the "white"
         * region of the value-hint.
         */
        public void interactiveNumberProportionalGradientHighlightExperimentWithStriping() {
            TableModel model = new AncientSwingTeam();
            JXTable table = new JXTable(model);
            // dirty, dirty - but I want to play with the options later on ...
            final RenderingLabel label = new RenderingLabel();
            RenderingComponentController<JLabel> numberRendering = new RenderingLabelController(
                    JLabel.RIGHT) {

                        @Override
                        protected JLabel createRendererComponent() {
                            return label;
                        }
                
            };
            table.setDefaultRenderer(Number.class, new DefaultTableRenderer(
                    numberRendering));
            Highlighter gradientHighlighter = createTransparentGradientHighlighter();
            Highlighter alternateRowHighlighter = new UIAlternateRowHighlighter();
            table.addHighlighter(alternateRowHighlighter);
            table.addHighlighter(gradientHighlighter);
            // re-use component controller and highlighter in a JXList
            JXList list = new JXList(createListNumberModel(), true);
            list.setCellRenderer(new DefaultListRenderer(numberRendering));
            list.addHighlighter(alternateRowHighlighter);
            list.addHighlighter(gradientHighlighter);
            list.toggleSortOrder();
            final JXFrame frame = showWithScrollingInFrame(table, list,
                    "transparent value relative highlighting - with striping");
            addStatusMessage(frame,
                    "uses the play-with painter-aware label");
            
            // crude binding to play with options - the factory is incomplete...
            ActionContainerFactory factory = new ActionContainerFactory();
            // toggle opaque optimatization
            AbstractActionExt overrideOpaque = new AbstractActionExt("plain opque") {

                public void actionPerformed(ActionEvent e) {
                    label.overrideSuperIsOpaque = isSelected();
                    frame.repaint();
                }
                
            };
            overrideOpaque.setStateAction();
            JCheckBox box = new JCheckBox();
            factory.configureButton(box, overrideOpaque, null);
            getStatusBar(frame).add(box);
            // call painter in paintComponent
            AbstractActionExt paintComponent = new AbstractActionExt("paintComponent") {

                public void actionPerformed(ActionEvent e) {
                    label.overrideSuperPainter = isSelected();
                    frame.repaint();
                }
                
            };
            paintComponent.setStateAction();
            box = new JCheckBox();
            factory.configureButton(box, paintComponent, null);
            getStatusBar(frame).add(box);
            // call painter in paintComponent
            AbstractActionExt opaqueDepends = new AbstractActionExt("opaqueDepends") {

                public void actionPerformed(ActionEvent e) {
                    label.adjustOpaqueWithPainter = isSelected();
                    frame.repaint();
                }
                
            };
            opaqueDepends.setStateAction();
            box = new JCheckBox();
            factory.configureButton(box, opaqueDepends, null);
            getStatusBar(frame).add(box);

            frame.pack();
        }
      


    /**
     * Use GradientPainter for value-based background highlighting with SwingX
     * extended default renderer. Trying to get the highlighter transparent:
     * the background color of the cell should shine through in the "white"
     * region of the value-hint.
     */
    public void interactiveTableAndListNumberProportionalGradientHighlightExperiment() {
        TableModel model = new AncientSwingTeam();
        JXTable table = new JXTable(model);
        table.setBackground(Highlighter.ledgerBackground.getBackground());
        // dirty, dirty - but I want to play with the options later on ...
        final RenderingLabel label = new RenderingLabel();
        RenderingComponentController<JLabel> numberRendering = new RenderingLabelController(
                JLabel.RIGHT) {

                    @Override
                    protected JLabel createRendererComponent() {
                        return label;
                    }
            
        };
        table.setDefaultRenderer(Number.class, new DefaultTableRenderer(
                numberRendering));
        Highlighter gradientHighlighter = createTransparentGradientHighlighter();
        table.addHighlighter(gradientHighlighter);
        // re-use component controller and highlighter in a JXList
        JXList list = new JXList(createListNumberModel(), true);
        list.setBackground(table.getBackground());
        list.setCellRenderer(new DefaultListRenderer(numberRendering));
        list.addHighlighter(gradientHighlighter);
        list.toggleSortOrder();
        final JXFrame frame = showWithScrollingInFrame(table, list,
                "transparent value relative highlighting");
        addStatusMessage(frame,
                "uses the play-with painter-aware label");
        
        // crude binding to play with options - the factory is incomplete...
        ActionContainerFactory factory = new ActionContainerFactory();
        // toggle opaque optimatization
        AbstractActionExt overrideOpaque = new AbstractActionExt("plain opque") {

            public void actionPerformed(ActionEvent e) {
                label.overrideSuperIsOpaque = isSelected();
                frame.repaint();
            }
            
        };
        overrideOpaque.setStateAction();
        JCheckBox box = new JCheckBox();
        factory.configureButton(box, overrideOpaque, null);
        getStatusBar(frame).add(box);
        // call painter in paintComponent
        AbstractActionExt paintComponent = new AbstractActionExt("paintComponent") {

            public void actionPerformed(ActionEvent e) {
                label.overrideSuperPainter = isSelected();
                frame.repaint();
            }
            
        };
        paintComponent.setStateAction();
        box = new JCheckBox();
        factory.configureButton(box, paintComponent, null);
        getStatusBar(frame).add(box);
        // call painter in paintComponent
        AbstractActionExt opaqueDepends = new AbstractActionExt("opaqueDepends") {

            public void actionPerformed(ActionEvent e) {
                label.adjustOpaqueWithPainter = isSelected();
                frame.repaint();
            }
            
        };
        opaqueDepends.setStateAction();
        box = new JCheckBox();
        factory.configureButton(box, opaqueDepends, null);
        getStatusBar(frame).add(box);

        frame.pack();
    }
  
    /**
     * to play with screws to make transparency work.
     */
    public static class RenderingLabel extends JRendererLabel {

        private boolean opaque;
        private boolean overrideSuperIsOpaque;
        private boolean overrideSuperPainter;
        private boolean adjustOpaqueWithPainter;

        public RenderingLabel() {
            super();
            // the "real" flag
            setOpaque(true);
        }

        @Override
        public void setOpaque(boolean opaque) {
            this.opaque = opaque;
            super.setOpaque(opaque);
        }
        
        @Override
        public boolean isOpaque() { 
            if (overrideSuperIsOpaque) {
                // super does some optimization which might (or might not) interfere
                return opaque;
            }
            return super.isOpaque();
        }

        
        @Override
        public void setPainter(Painter painter) {
            super.setPainter(painter);
            if (adjustOpaqueWithPainter) {
                if (painter != null) {
                    setOpaque(false);
                } else {
                    setOpaque(true);
                }
            } else {
                setOpaque(true);
            }
        }

        @Override
        public void paint(Graphics g) {
            // super calls the painter before calling its super.paint
            super.paint(g);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintPainter((Graphics2D)g, true);
        }

        /**
         * called from super.paint - overridden to do nothing if the painter
         * is called in super.paint.
         */
        @Override
        protected void paintPainter(Graphics2D g) {
            if (overrideSuperPainter) return;
            super.paintPainter(g);
        }
        
        /** 
         * called from paintComponent.
         * @param g
         * @param dummy
         */
        protected void paintPainter(Graphics2D g, boolean dummy) {
            if (!overrideSuperPainter) return;
            if (painter != null) {
                painter.paint(g, this);
            }
        }
    }

//-------------------- transparent gradient highlighter  
    
    public static class TransparentGradientHighlighter extends ConditionalHighlighter {
        float maxValue = 100;
        private Painter painter;
        private boolean yellowTransparent;

        /**
         */
        public TransparentGradientHighlighter() {
            super(null, null, -1, -1);
        }

        /**
         * @param b
         */
        public void setYellowTransparent(boolean b) {
            this.yellowTransparent = b;
        }

        @Override
        public Component highlight(Component renderer,
                ComponentAdapter adapter) {
            boolean highlight = needsHighlight(adapter);
            if (highlight && (renderer instanceof PainterAware)) {
                float end = getEndOfGradient((Number) adapter.getValue());
                if (end > 1) {
                    renderer.setBackground(Color.YELLOW.darker());
                } else if (end > 0.02) {
                    Painter painter = getPainter(end);
                    ((PainterAware) renderer).setPainter(painter);
                }
                return renderer;
            }
            return renderer;
        }

        private Painter getPainter(float end) {
                Color startColor = getTransparentColor(Color.YELLOW, yellowTransparent ? 
                        125 : 254);
                Color endColor = getTransparentColor(Color.WHITE, 0);
             painter = new BasicGradientPainter(0.0f, 0.0f,
                    startColor, end, 0.f, endColor);
            return painter;
        }

        private Color getTransparentColor(Color base, int transparency) {
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), transparency);
        }
        private float getEndOfGradient(Number number) {
            float end = number.floatValue() / maxValue;
            return end;
        }

        @Override
        protected boolean test(ComponentAdapter adapter) {
            return adapter.getValue() instanceof Number;
        }

        
    }
    /**
     * creates and returns a highlighter with a value-based transparent 
     * gradient if the cell content type is a Number.  
     * 
     * @return 
     */
    private TransparentGradientHighlighter createTransparentGradientHighlighter() {
        return new TransparentGradientHighlighter();
    }

//----------------- Utility    
    /**
     * 
     * @return a ListModel wrapped around the AncientSwingTeam's Number column.
     */
    private ListModel createListNumberModel() {
        AncientSwingTeam tableModel = new AncientSwingTeam();
        int colorColumn = 3;
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            model.addElement(tableModel.getValueAt(i, colorColumn));
        }
        return model;
    }

}