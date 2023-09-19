import java.util.Random;
import java.awt.Rectangle;


public class UNCItem {
    private int x;
    private int y;
    private boolean removed;
    private int width;
    private int height;

    public UNCItem(int maxX, int maxY) {
        Random random = new Random();
        int padding = 300; // Add some padding to avoid the footballs spawning too close to the edge
    
        // Calculate the maximum x and y positions based on the screen size and the padding
        int maxSpawnX = maxX - padding;
        int maxSpawnY = maxY - padding;
    
        // Generate random positions within the screen bounds
        x = random.nextInt(maxSpawnX) + padding;
        y = random.nextInt(maxSpawnY) + padding;

        width = 50; // set default width to 50
        height = 50; // set default height to 50
        removed = false; // set default state to not removed
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isRemoved() {
        return removed;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
}
