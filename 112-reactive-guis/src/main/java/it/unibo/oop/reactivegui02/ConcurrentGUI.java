package it.unibo.oop.reactivegui02;

import it.unibo.oop.JFrameUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second version of the gui.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton up;
    private final JButton down;
    private final JButton stop;

    /**
     * Builds a new CGUI.
     */
    public ConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        up = new JButton("up");
        panel.add(up);
        down = new JButton("down");
        panel.add(down);
        stop = new JButton("stop");
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        final Agent agent = new Agent();
        new Thread(agent).start();
        up.addActionListener(e -> agent.goingUp());
        down.addActionListener(e -> agent.goingDown());
        stop.addActionListener(e -> agent.stopCounting());
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean stopButton;
        private volatile boolean direction = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stopButton) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                    if (this.direction) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        public void stopCounting() {
            this.stopButton = true;
            SwingUtilities.invokeLater(() -> {
                up.setEnabled(false);
                down.setEnabled(false);
                ConcurrentGUI.this.stop.setEnabled(false);
            });
        }

        /**
         * External command to count upwards.
         */
        public void goingUp() {
            this.direction = true;
        }

        /**
         * External command to count downwards.
         */
        public void goingDown() {
            this.direction = false;
        }
    }
}
