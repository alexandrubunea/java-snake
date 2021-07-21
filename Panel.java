import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Panel extends JPanel implements Runnable {
    // screen-props
    private static final int SCREEN_HEIGHT = 800;
    private static final int SCREEN_WIDTH = 800;
    private static final int UNIT_SIZE = 25;

    // snake-props
    ArrayList<Snake> snake;
    char direction;

    // fruit-props
    Fruit fruit;

    // game-props
    Thread gameThread;
    private final Random random = new Random();
    private boolean gameOver = false;
    private int score;
    private boolean AI = true;

    // constructor
    Panel() {
        this.setPreferredSize(new Dimension(SCREEN_HEIGHT, SCREEN_WIDTH));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        initGame();
        this.addKeyListener(new MyKeyAdapter());
        gameThread = new Thread(this);
        gameThread.start();
    }

    // game
    private void initGame() {
        snake = new ArrayList<>();
        int sX, sY, fX, fY, d;
        sX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        sY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        do {
            fX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            fY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        } while(sX == fX && sY == fY);

        // snake-head
        snake.add(new Snake(sX, sY, UNIT_SIZE, UNIT_SIZE,true));
        snake.add(new Snake(sX, sY, UNIT_SIZE, UNIT_SIZE,false));

        // fruit
        fruit = new Fruit(fX, fY, UNIT_SIZE);

        // direction
        d = random.nextInt(4) + 1;
        switch (d) {
            case 1 -> direction = 'U';
            case 2 -> direction = 'D';
            case 3 -> direction = 'L';
            case 4 -> direction = 'R';
        }

        // score
        score = 0;
    }

    // new-fruit
    private void newFruit() {
        int fX, fY;
        AtomicBoolean isOccupied = new AtomicBoolean(false);
        do {
            fX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            fY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            int finalFX = fX;
            int finalFY = fY;
            snake.forEach((s) -> {
                if(s.x() == finalFX && s.y() == finalFY) isOccupied.set(true);
            });

        } while(isOccupied.get());
        fruit.setX(fX);
        fruit.setY(fY);
    }

    // draw
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    private void scoreRender(Graphics g) {
        if(!AI) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("SCORE: " + score, 10, 25);
        }
    }
    private void renderInfo(Graphics g) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SNAKE", SCREEN_WIDTH / 2 - 70, SCREEN_HEIGHT / 2 - 110);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("PRESS ENTER TO START", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - 80);
    }
    private void renderGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("GAME OVER", SCREEN_WIDTH / 2 - 110, SCREEN_HEIGHT / 2 - 110);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("SCORE: " + score, SCREEN_WIDTH / 2 - 30, SCREEN_HEIGHT / 2 - 80);
        g.setColor(Color.BLUE);
        g.drawString("PRESS SPACE TO RESTART", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - 50);
    }
    private void draw(Graphics g) {
        if(!gameOver) {
            // render-start-text
            if(AI) renderInfo(g);

            // score-render
            scoreRender(g);

            // render-fruit
            fruit.render(g);

            // render-snake
            snake.forEach(s -> s.render(g));

        } else renderGameOver(g);
    }

    // movement
    private void snakeMove() {
        AtomicInteger lastComponentX = new AtomicInteger(snake.get(0).x());
        AtomicInteger lastComponentY = new AtomicInteger(snake.get(0).y());
        snake.subList(1, snake.size()).forEach((s) -> {
            int oldX = s.x();
            int oldY = s.y();

            s.setX(lastComponentX.get());
            s.setY(lastComponentY.get());

            lastComponentX.set(oldX);
            lastComponentY.set(oldY);
        });

        switch (direction) {
            case 'U' -> snake.get(0).setY(snake.get(0).y() - UNIT_SIZE);
            case 'D' -> snake.get(0).setY(snake.get(0).y() + UNIT_SIZE);
            case 'L' -> snake.get(0).setX(snake.get(0).x() - UNIT_SIZE);
            case 'R' -> snake.get(0).setX(snake.get(0).x() + UNIT_SIZE);
        }
    }

    // AI
    private boolean doesNotIntersect(int x, int y) {
        AtomicBoolean result = new AtomicBoolean(false);
        snake.subList(1, snake.size() - 1).forEach((s) -> {
            if(s.x() == x && s.y() == y) result.set(true);
        });
        return !result.get();
    }
    private int modulo(int x) {
        if(x < 0) x = -x;
        return x;
    }
    private void snakeAI() {
        Snake head = snake.get(0);

        // Not on the same axis with the fruit
        if(head.y() > fruit.y() && direction != 'U') {
            if(direction != 'D' && doesNotIntersect(head.x(), head.y() - UNIT_SIZE)) direction = 'U';
            if(direction == 'D') {
                int left_distance = modulo(head.x() - UNIT_SIZE - fruit.x());
                int right_distance = modulo(head.x() + UNIT_SIZE - fruit.x());
                if(left_distance <= right_distance) {
                    if(doesNotIntersect(head.x() - UNIT_SIZE, head.y())) direction = 'L';
                } else {
                    if(doesNotIntersect(head.x() + UNIT_SIZE, head.y())) direction = 'R';
                }
            }
        } else if(head.y() < fruit.y() && direction != 'D') {
            if(direction != 'U' && doesNotIntersect(head.x(), head.y() - UNIT_SIZE)) direction = 'D';
            if(direction == 'U') {
                int left_distance = modulo(head.x() - UNIT_SIZE - fruit.x());
                int right_distance = modulo(head.x() + UNIT_SIZE - fruit.x());
                if(left_distance <= right_distance) {
                    if(doesNotIntersect(head.x() - UNIT_SIZE, head.y())) direction = 'L';
                } else {
                    if(doesNotIntersect(head.x() + UNIT_SIZE, head.y())) direction = 'R';
                }
            }
        }

        if(head.x() > fruit.x() && direction != 'L') {
            if(direction != 'R' && doesNotIntersect(head.x() - UNIT_SIZE, head.y())) direction = 'L';
            if(direction == 'R') {
                int left_distance = modulo(head.y() - UNIT_SIZE - fruit.y());
                int right_distance = modulo(head.y() + UNIT_SIZE - fruit.y());
                if(left_distance <= right_distance) {
                    if(doesNotIntersect(head.x(), head.y() - UNIT_SIZE)) direction = 'U';
                } else {
                    if(doesNotIntersect(head.x(), head.y() + UNIT_SIZE)) direction = 'D';
                }
            }
        } else if(head.x() < fruit.x() && direction != 'R') {
            if(direction != 'L' && doesNotIntersect(head.x() + UNIT_SIZE, head.y())) direction = 'R';
            if(direction == 'L') {
                int left_distance = modulo(head.y() - UNIT_SIZE - fruit.y());
                int right_distance = modulo(head.y() + UNIT_SIZE - fruit.y());
                if(left_distance <= right_distance) {
                    if(doesNotIntersect(head.x(), head.y() - UNIT_SIZE)) direction = 'U';
                } else {
                    if(doesNotIntersect(head.x(), head.y() + UNIT_SIZE)) direction = 'D';
                }
            }
        }

        // Obstacle ahead
        switch(direction) {
            case 'U' -> {
                if(!doesNotIntersect(head.x(), head.y() - UNIT_SIZE)) {
                    int left_distance = modulo(head.x() - UNIT_SIZE - fruit.x());
                    int right_distance = modulo(head.x() + UNIT_SIZE - fruit.x());
                    if(left_distance <= right_distance) direction = 'L';
                    else direction = 'R';
                }
            }
            case 'D' -> {
                if(!doesNotIntersect(head.x(), head.y() + UNIT_SIZE)) {
                    int left_distance = modulo(head.x() - UNIT_SIZE - fruit.x());
                    int right_distance = modulo(head.x() + UNIT_SIZE - fruit.x());
                    if(left_distance <= right_distance) direction = 'L';
                    else direction = 'R';
                }
            }
            case 'L' -> {
                if(!doesNotIntersect(head.x() - UNIT_SIZE, head.y())) {
                    int left_distance = modulo(head.y() - UNIT_SIZE - fruit.y());
                    int right_distance = modulo(head.y() + UNIT_SIZE - fruit.y());
                    if(left_distance <= right_distance) direction = 'U';
                    else direction = 'D';
                }
            }
            case 'R' -> {
                if(!doesNotIntersect(head.x() + UNIT_SIZE, head.y())) {
                    int left_distance = modulo(head.y() - UNIT_SIZE - fruit.y());
                    int right_distance = modulo(head.y() + UNIT_SIZE - fruit.y());
                    if(left_distance <= right_distance) direction = 'U';
                    else direction = 'D';
                }
            }
        }

    }

    // collisions
    private void snakeCollide() {
        Snake head = snake.get(0);

        // with-the-walls
        if(head.x() >= SCREEN_WIDTH) head.setX(0);
        else if(head.x() + UNIT_SIZE <= 0) head.setX(SCREEN_WIDTH - UNIT_SIZE);

        if(head.y() >= SCREEN_HEIGHT) head.setY(0);
        else if(head.y() + UNIT_SIZE <= 0) head.setY(SCREEN_HEIGHT - UNIT_SIZE);

        // with-the-fruit
        if(head.x() == fruit.x() && head.y() == fruit.y()) {
            if(!AI || snake.size() < 8) {
                Snake component = new Snake(snake.get(1).x(), snake.get(1).y(), UNIT_SIZE, UNIT_SIZE, false);
                snake.add(component);
            }
            newFruit();
            score += 10;
        }

        // with-self
        AtomicBoolean isOver = new AtomicBoolean(false);
        snake.subList(1, snake.size()).forEach((s) -> {
            if(head.x() == s.x() && head.y() == s.y()) isOver.set(true);
        });
        if(isOver.get() && !AI) {
            gameOver = true;
        }
    }

    // key-adapter
    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> { if(direction != 'D' && !AI) direction = 'U'; }
                case KeyEvent.VK_S -> { if(direction != 'U'&& !AI) direction = 'D'; }
                case KeyEvent.VK_A -> { if(direction != 'R'&& !AI) direction = 'L'; }
                case KeyEvent.VK_D -> { if(direction != 'L'&& !AI) direction = 'R'; }
                case KeyEvent.VK_SPACE -> {
                    if(gameOver) {
                        initGame();
                        gameOver = false;
                    }
                }
                case KeyEvent.VK_ENTER -> {
                    if(AI) {
                        AI = false;
                        initGame();
                    }
                }
            }
        }
    }

    // game-loop
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 15.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if (delta >= 1) {

                // repaint
                repaint();

                if(!gameOver) {
                    // snake-ai
                    if(AI) snakeAI();

                    // snake-movement
                    snakeMove();

                    // snake-collisions
                    snakeCollide();
                }

                delta --;
            }
        }
    }
}
