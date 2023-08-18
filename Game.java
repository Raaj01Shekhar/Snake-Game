import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

public class Game extends JPanel {
    private Timer timer;
    private Snake snake;
    private Point cherry;
    private int points = 0;
    private int best = 0;
    private BufferedImage image;
    private GameStatus status;
    private boolean didLoadCherryImage = true;

    private static Font FONT_M = new Font("Mistral", Font.PLAIN, 25);
    private static Font FONT_M_ITALIC = new Font("Papyrus", Font.PLAIN, 25);
    private static Font FONT_L = new Font("Chiller", Font.PLAIN, 125);
    private static Font FONT_XL = new Font("Viner Hand ITC", Font.PLAIN, 150);
    private static int WIDTH = 760;
    private static int HEIGHT = 520;
    private static int DELAY = 50;

    public Game() {
        try {
            image = ImageIO.read(new File("cherry.png"));
        } catch (IOException e) {
          didLoadCherryImage = false;
        }

        addKeyListener(new KeyListener());
        setFocusable(true);
        setBackground(Color.black);
        setDoubleBuffered(true);

        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        status = GameStatus.NOT_STARTED;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        render(g);

        Toolkit.getDefaultToolkit().sync();
    }

    private void update() {
        snake.move();

        if (cherry != null && snake.getHead().intersects(cherry, 20)) {
            snake.addTail();
            cherry = null;
            points++;
        }

        if (cherry == null) {
            spawnCherry();
        }

        checkForGameOver();
    }

    private void reset() {
        points = 0;
        cherry = null;
        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        setStatus(GameStatus.RUNNING);
    }

    private void setStatus(GameStatus newStatus) {
        switch(newStatus) {
            case RUNNING:
                timer = new Timer();
                timer.schedule(new GameLoop(), 0, DELAY);
                break;
            case PAUSED:
                timer.cancel();
            case GAME_OVER:
                timer.cancel();
                best = points > best ? points : best;
                break;
		default:
			break;
        }

        status = newStatus;
    }

    private void togglePause() {
        setStatus(status == GameStatus.PAUSED ? GameStatus.RUNNING : GameStatus.PAUSED);
    }

    private void checkForGameOver() {
        Point head = snake.getHead();
        boolean hitBoundary = head.getX() <= 20
            || head.getX() >= WIDTH + 10
            || head.getY() <= 40
            || head.getY() >= HEIGHT + 30;

        boolean ateItself = false;

        for(Point t : snake.getTail()) {
            ateItself = ateItself || head.equals(t);
        }

        if (hitBoundary || ateItself) {
            setStatus(GameStatus.GAME_OVER);
        }
    }

    public void drawCenteredString(Graphics g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (WIDTH - metrics.stringWidth(text)) / 2;

        g.setFont(font);
        g.drawString(text, x, y);
    }

    private <g2d> void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 246));
        g2d.setFont(FONT_M);

        if (status == GameStatus.NOT_STARTED) {
        	g2d.setColor(new Color(50, 205, 50));
          drawCenteredString(g2d, "SNAKE", FONT_XL, 200);
          drawCenteredString(g2d, "GAME", FONT_XL, 325);
          drawCenteredString(g2d, "Press  'SPACE'  to  start.", FONT_M_ITALIC, 375);
          return;
        }

        Point p = snake.getHead();

        g2d.drawString("SCORE: " + String.format ("%04d", points), 20, 30);
        g2d.drawString("BEST: " + String.format ("%04d", best), 690, 30);
        
        if (cherry != null) {
          if (didLoadCherryImage) {
            g2d.drawImage(image, cherry.getX(), cherry.getY(), 60, 60, null);
          } else {
            g2d.setColor(Color.RED);
            g2d.fillOval(cherry.getX(), cherry.getY(), 10, 10);
            g2d.setColor(new Color(24, 0, 80));
          }
        }

        if (status == GameStatus.GAME_OVER) {
        	g2d.setColor(new Color(247, 0, 0));
            drawCenteredString(g2d, "Press  'ENETR'  to  start  again.", FONT_M_ITALIC, 330);
            drawCenteredString(g2d, "GAME OVER", FONT_L, 300);
         
        }

        if (status == GameStatus.PAUSED) {
            g2d.drawString("Paused", 600, 14);
        }

        g2d.setColor(new Color(92, 247, 0));
        g2d.fillRect(p.getX(), p.getY(), 10, 10);

        for(int i = 0, size = snake.getTail().size(); i < size; i++) {
            Point t = snake.getTail().get(i);

            g2d.fillRect(t.getX(), t.getY(), 10, 10);
        }

        g2d.setColor(new Color(247, 209, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 40, WIDTH, HEIGHT);
    }

    public void spawnCherry() {
        cherry = new Point((new Random()).nextInt(WIDTH - 60) + 20,
            (new Random()).nextInt(HEIGHT - 60) + 40);
    }

    private class KeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (status == GameStatus.RUNNING) {
                switch(key) {
                    case KeyEvent.VK_LEFT: 
                    	snake.turn(Direction.LEFT); 
                    	break;
                    case KeyEvent.VK_RIGHT: 
                    	snake.turn(Direction.RIGHT); 
                    	break;
                    case KeyEvent.VK_UP: 
                    	snake.turn(Direction.UP); 
                    	break;
                    case KeyEvent.VK_DOWN: 
                    	snake.turn(Direction.DOWN); 
                    	break;
                }
            }

            if (status == GameStatus.NOT_STARTED && key == KeyEvent.VK_SPACE) {
                setStatus(GameStatus.RUNNING);
            }

            if (status == GameStatus.GAME_OVER && key == KeyEvent.VK_ENTER) {
                reset();
            }

            if (key == KeyEvent.VK_P) {
                togglePause();
            }
        }
    }

    private class GameLoop extends java.util.TimerTask {
        public void run() {
            update();
            repaint();
        }
    }
}
