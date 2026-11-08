package io.github.luanfrombrazil.secondbrain.graph.view;

/** The camera of the infinite canvas: screen = world × scale + offset. Eases toward its target. */
public final class Camera {

    public static final double MIN_SCALE = 0.03;
    public static final double MAX_SCALE = 2.5;
    private static final double SPEED = 14;
    private static final double CLOSE_ENOUGH = 0.05;

    private double scale = 1;
    private double dx;
    private double dy;
    private double targetScale = 1;
    private double targetDx;
    private double targetDy;

    public double scale() {
        return scale;
    }

    public double screenX(double worldX) {
        return worldX * scale + dx;
    }

    public double screenY(double worldY) {
        return worldY * scale + dy;
    }

    public double worldX(double screenX) {
        return (screenX - dx) / scale;
    }

    public double worldY(double screenY) {
        return (screenY - dy) / scale;
    }

    /** The world point under the cursor stays under it. */
    public void zoomAtCursor(double cursorX, double cursorY, double factor) {
        double updated = Math.clamp(targetScale * factor, MIN_SCALE, MAX_SCALE);
        double worldX = (cursorX - targetDx) / targetScale;
        double worldY = (cursorY - targetDy) / targetScale;
        targetScale = updated;
        targetDx = cursorX - worldX * updated;
        targetDy = cursorY - worldY * updated;
    }

    /** Dragging is direct: the world follows the mouse without delay. */
    public void drag(double deltaX, double deltaY) {
        dx += deltaX;
        dy += deltaY;
        targetDx += deltaX;
        targetDy += deltaY;
    }

    /** Moves the view by this many screen pixels, easing there. */
    public void panSmoothly(double deltaX, double deltaY) {
        targetDx += deltaX;
        targetDy += deltaY;
    }

    public void frame(double minX, double minY, double maxX, double maxY, double width, double height) {
        double margin = 60;
        double scaleX = (width - 2 * margin) / Math.max(1, maxX - minX);
        double scaleY = (height - 2 * margin) / Math.max(1, maxY - minY);
        targetScale = Math.clamp(Math.min(scaleX, scaleY), MIN_SCALE, 1);
        targetDx = width / 2 - (minX + maxX) / 2 * targetScale;
        targetDy = height / 2 - (minY + maxY) / 2 * targetScale;
    }

    public void jumpToTarget() {
        scale = targetScale;
        dx = targetDx;
        dy = targetDy;
    }

    /** Returns true while still moving. */
    public boolean update(double seconds) {
        double t = 1 - Math.exp(-seconds * SPEED);
        if (Math.abs(targetScale - scale) * 1000 < CLOSE_ENOUGH
                && Math.abs(targetDx - dx) < CLOSE_ENOUGH
                && Math.abs(targetDy - dy) < CLOSE_ENOUGH) {
            boolean changed = scale != targetScale || dx != targetDx || dy != targetDy;
            jumpToTarget();
            return changed;
        }
        scale += (targetScale - scale) * t;
        dx += (targetDx - dx) * t;
        dy += (targetDy - dy) * t;
        return true;
    }
}
