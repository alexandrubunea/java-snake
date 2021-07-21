import java.awt.*;

public class Snake {
    // snake-props
    private int x;
    private int y;
    private final int width;
    private final int height;
    private final boolean isHead;

    // constructor
    Snake(int x, int y, int width, int height, boolean isHead) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isHead = isHead;
    }

    // render
    public void render(Graphics g) {
        if(isHead) g.setColor(Color.YELLOW);
        else g.setColor(Color.ORANGE);
        g.fillRect(this.x, this.y, this.width, this.height);
    }

    // get-props
    public int x() { return this.x; }
    public int y() { return this.y; }

    // change-props
    public void setX(int value) { this.x = value; }
    public void setY(int value) { this.y = value; }
}
