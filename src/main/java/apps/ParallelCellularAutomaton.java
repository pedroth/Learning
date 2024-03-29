package apps;

import visualization.TextFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Parallel Asynchronous cellular automaton
 * <p>
 * for every pixel calculates update function of the automaton
 * <p>
 * I need another algorithms to update the automaton one that is O(n) n = number
 * of alive cells and not O(A/nCores) where A is area of pixels
 *
 * @author pedro
 */

public class ParallelCellularAutomaton extends JFrame implements MouseMotionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private static final TextFrame HELP_FRAME = TextFrame.builder()
            .addLine("< mouse > : draw initial state")
            .addLine("< 1 - 9 > : select type of automata")
            .addLine("< 0 > : generate random automata")
            .addLine("<space> : starts animation")
            .addLine("<r> : reset")
            .addLine("Made by Pedroth")
            .buildWithTitle("Help");
    private BufferedImage buffer;
    private int wChanged, hChanged;
    private boolean[][] space;
    private Graphics gimg;
    private boolean timerStarted;
    private ThreadManager threadManager;
    //List of automata rules according to S/B, survive/born rules.
    // https://web.archive.org/web/20210512105747/http://psoup.math.wisc.edu/mcell/rullex_life.html
    private final List<Pair<int[], int[]>> automatonRules = List.of(
            Pair.of(new int[]{1, 2, 3, 4, 5}, new int[]{3}),
            Pair.of(new int[]{3, 4}, new int[]{3, 4}),
            Pair.of(new int[]{0, 2, 3, 6}, new int[]{1}),
            Pair.of(new int[]{1, 3, 5, 7, 8}, new int[]{3, 5, 7}),
            Pair.of(new int[]{4, 5, 6, 7, 8}, new int[]{3}),
            Pair.of(new int[]{2, 3, 5, 6, 7, 8}, new int[]{3, 7, 8}),
            Pair.of(new int[]{1, 3, 5, 7}, new int[]{1, 3, 5, 7}),
            Pair.of(new int[]{1}, new int[]{1}),
            Pair.of(new int[]{0,1,2,3,4,5,6,7,8}, new int[]{3})

    );

    private int selectedType = 0;
    private Pair<int[], int[]> randomRule = getRandomRule();

    private Map<Integer, Runnable> keysMapping = Map.of(
            KeyEvent.VK_H, () -> HELP_FRAME.setVisible(true),
            KeyEvent.VK_R, () -> {
                this.timerStarted = false;
                myInit();
            },
            KeyEvent.VK_SPACE, () -> this.timerStarted = true
    );

    public ParallelCellularAutomaton(boolean isApplet) {
        super("Cellular Automaton - Press h for Help");
        this.setLayout(null);
        // Set default close operation for JFrame
        if (!isApplet) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        this.setSize(500, 500);
        this.addMouseMotionListener(this);
        buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        space = new boolean[this.getWidth()][this.getHeight()];
        gimg = buffer.getGraphics();
        gimg.setColor(Color.black);
        wChanged = this.getWidth();
        hChanged = this.getHeight();
        gimg.fillRect(0, 0, wChanged, hChanged);
        threadManager = new ThreadManager();
        this.addKeyListener(this);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new ParallelCellularAutomaton(false);
    }

    /* synchronized */
    public void drawPoint(int x, int y, Color c) {
        int rgbColor = c.getRGB();
        int[] pixels = ((java.awt.image.DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        int w = wChanged;
        pixels[y * w + x] = rgbColor;
    }

    public void myInit() {
        this.setSize(500, 500);
        buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        space = new boolean[this.getWidth()][this.getHeight()];
        gimg = buffer.getGraphics();
        gimg.setColor(Color.black);
        wChanged = this.getWidth();
        hChanged = this.getHeight();
        gimg.fillRect(0, 0, wChanged, hChanged);
        threadManager = new ThreadManager();
    }

    public void paint(Graphics g) {
        if (Math.abs(wChanged - this.getWidth()) > 0 || Math.abs(hChanged - this.getHeight()) > 0) {
            buffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            space = new boolean[this.getWidth()][this.getHeight()];
            gimg = buffer.getGraphics();
            gimg.setColor(Color.black);
            wChanged = this.getWidth();
            hChanged = this.getHeight();
            gimg.fillRect(0, 0, wChanged, hChanged);
        }
        update(g);
    }

    public void update(Graphics g) {
        if (timerStarted) {
            threadManager.run();
        }
        buffer.getGraphics().setColor(Color.green);
        g.drawImage(buffer, 0, 0, wChanged, hChanged, null);
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int k = Math.max(e.getX(), 0);
        int l = Math.max(e.getY(), 0);
        k = Math.min(k, wChanged - 1);
        l = Math.min(l, hChanged - 1);
        space[k][l] = true;
        if (!timerStarted) {
            gimg.setColor(Color.green);
            gimg.drawLine(k, l, k, l);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keysMapping.containsKey(keyCode)) {
            keysMapping.get(keyCode).run();
        } else if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            this.selectedType = (keyCode & 0xF) - 1;
            this.timerStarted = false;
            this.myInit();
        } else {
            this.selectedType = -1;
            this.randomRule = getRandomRule();
            this.timerStarted = false;
            this.myInit();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    private static Pair<int[], int[]> getRandomRule() {
        Random random = new Random();
        ArrayList<Integer> surviveRule = new ArrayList<>();
        ArrayList<Integer> bornRule = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            int surviveCoin = random.nextInt(2);
            int bornCoin = random.nextInt(2);
            if (surviveCoin == 1) {
                surviveRule.add(i);
            }
            if (bornCoin == 1) {
                bornRule.add(i);
            }
        }
        Pair<int[], int[]> pair = Pair.of(
                surviveRule.stream().mapToInt(Integer::intValue).toArray(),
                bornRule.stream().mapToInt(Integer::intValue).toArray()
        );
        System.out.println("Random rule (S/B):" + pair);
        return pair;
    }

    class CellularAutomaton implements Runnable {
        private int discreteTime;
        private int begin, end;
        /**
         * change rules where according to
         * http://psoup.math.wisc.edu/mcell/rullex_life.html
         */
        private int[] survive;
        private int[] born;

        public CellularAutomaton(int begin, int end) {
            discreteTime = 0;
            this.begin = begin;
            this.end = end;
            if (selectedType >= 0 && selectedType < automatonRules.size()) {
                Pair<int[], int[]> sbRule = automatonRules.get(selectedType);
                int[] surviveRule = sbRule.getKey();
                int[] bornRule = sbRule.getValue();
                this.survive = Arrays.copyOf(surviveRule, surviveRule.length);
                this.born = Arrays.copyOf(bornRule, bornRule.length);
            } else {
                Pair<int[], int[]> sbRule = randomRule;
                int[] surviveRule = sbRule.getKey();
                int[] bornRule = sbRule.getValue();
                this.survive = Arrays.copyOf(surviveRule, surviveRule.length);
                this.born = Arrays.copyOf(bornRule, bornRule.length);
            }
        }

        boolean cellularFunction(int x, int y, int t) {
            int acm = 0;
            /**
             * check neighbors cells.
             */
            boolean isAlive = space[x][y];
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int k = Math.max(x + i, 0);
                    int l = Math.max(y + j, 0);
                    k = Math.min(k, wChanged - 1);
                    l = Math.min(l, hChanged - 1);
                    if (space[k][l]) acm++;
                }
            }
            if (!isAlive && bornRule(acm))
                return true;
            return isAlive && surviveRule(acm);
        }

        public boolean bornRule(int acm) {
            boolean orIden = false;
            for (int v : born)
                orIden = orIden || (acm == v);
            return orIden;
        }

        public boolean surviveRule(int acm) {
            boolean orIden = false;
            for (int v : survive)
                orIden = orIden || (acm == v);
            return orIden;
        }

        @Override
        public void run() {
            for (int i = begin; i < end; i++) {
                for (int j = 0; j < hChanged; j++) {
                    boolean isAlive = cellularFunction(i, j, discreteTime);
                    if (isAlive) {
                        drawPoint(i, j, Color.green);
                    } else {
                        drawPoint(i, j, Color.black);
                    }
                }
            }
            for (int i = begin; i < end; i++) {
                for (int j = 0; j < hChanged; j++) {
                    if (buffer.getRGB(i, j) == Color.green.getRGB()) {
                        space[i][j] = true;
                    } else {
                        space[i][j] = false;
                    }
                }
            }

            discreteTime++;
        }
    }

    class ThreadManager {
        private Thread[] threads;
        private int nCores;
        private CellularAutomaton[] cells;

        ThreadManager() {
            nCores = Runtime.getRuntime().availableProcessors();
            threads = new Thread[nCores];
            cells = new CellularAutomaton[nCores];
            for (int i = 0; i < nCores; i++) {
                cells[i] = new CellularAutomaton(
                        i * (wChanged / nCores),
                        (i + 1) * (wChanged / nCores)
                );
            }
        }

        public void run() {
            for (int i = 0; i < nCores; i++) {
                threads[i] = new Thread(cells[i]);
                threads[i].start();
            }
            for (int i = 0; i < nCores; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            repaint();
        }
    }
}

class Pair<K, V> {
    private final K key;
    private final V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key=" + (key.getClass().isArray() ? Arrays.toString((int[]) key) : key.toString()) +
                ", value=" + (value.getClass().isArray() ? Arrays.toString((int[]) value) : value.toString()) +
                '}';
    }

    public static <A, B> Pair<A, B> of(A key, B value) {
        return new Pair<>(key, value);
    }
}
