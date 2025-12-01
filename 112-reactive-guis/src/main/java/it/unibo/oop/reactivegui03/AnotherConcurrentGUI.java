package it.unibo.oop.reactivegui03;

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
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final transient Agent agent;
    private final JButton up;
    private final JButton down;
    private final JButton stop;

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
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
        agent = new Agent();
        new Thread(agent).start();
        final NewAgent newAgent = new NewAgent();
        new Thread(newAgent).start();
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
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
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

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stopButton = true;
            SwingUtilities.invokeLater(() -> {
                up.setEnabled(false);
                down.setEnabled(false);
                AnotherConcurrentGUI.this.stop.setEnabled(false);
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

    /**
     * This agent manages the 10 second timer to stop the game.
     */
    private final class NewAgent implements Runnable {

        private static final int TIMER = 10_000;

        @Override
        public void run() {
            try {
                Thread.sleep(TIMER);
                SwingUtilities.invokeLater(agent::stopCounting);
            } catch (final InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
