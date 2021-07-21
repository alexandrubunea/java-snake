import java.awt.*;

public class Fruit {
    // fruit-props
    private int x;
    private int y;
    private final int radius;

    Fruit(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public void render(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(this.x, this.y, this.radius, this.radius);
    }

    // get-props
    public int x() { return this.x; }
    public int y() { return this.y; }

    // change-props
    public void setX(int value) { this.x = value; }
    public void setY(int value) { this.y = value; }
}
