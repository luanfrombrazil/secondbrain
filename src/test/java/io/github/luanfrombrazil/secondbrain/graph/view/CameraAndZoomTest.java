package io.github.luanfrombrazil.secondbrain.graph.view;

import io.github.luanfrombrazil.secondbrain.graph.view.camera.ZoomLevel;
import io.github.luanfrombrazil.secondbrain.graph.view.camera.Camera;
import io.github.luanfrombrazil.secondbrain.graph.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraAndZoomTest {

    private static final double[] THRESHOLDS = {0.14, 0.55, 0.86};

    @Test
    void centerOnPutsThePointInTheMiddleAndKeepsTheZoom() {
        Camera c = new Camera();
        c.zoomAtCursor(0, 0, 0.5);
        c.jumpToTarget();
        c.centerOn(300, -200, 1000, 600);
        c.jumpToTarget();
        assertEquals(500, c.screenX(300), 1e-9);
        assertEquals(300, c.screenY(-200), 1e-9);
        assertEquals(0.5, c.scale(), 1e-9);
    }

    @Test
    void curveIsCutAtTheRequestedDistanceFromItsEnd() {
        double[] xs = {0, 100, 100, 200};
        double[] ys = {0, 0, 100, 100};
        double t = GraphRenderer.parameterAtDistance(xs, ys, 20);
        double x = GraphRenderer.subcurve(xs, t)[3];
        double y = GraphRenderer.subcurve(ys, t)[3];
        assertEquals(20, Math.hypot(200 - x, 100 - y), 1e-6);
        assertEquals(200, GraphRenderer.subcurve(xs, 1)[3], 1e-9);
    }

    @Test
    void connectionsFadeWithDistanceFromTheScreen() {
        assertEquals(1, GraphRenderer.closeness(500, 300, 1000, 600));
        assertEquals(0.5, GraphRenderer.closeness(1375, 300, 1000, 600), 1e-9);
        assertEquals(0, GraphRenderer.closeness(-5000, 300, 1000, 600));
    }

    @Test
    void linkedElementSlotsDoNotOverlapEachOtherOrTheCard() {
        double chipWidth = 150;
        double chipHeight = 40;
        for (int i = 0; i < 30; i++) {
            double[] a = GraphRenderer.slotOffset(i);
            assertTrue(Math.abs(a[1]) >= Node.HEIGHT / 2 + chipHeight / 2);
            for (int j = i + 1; j < 30; j++) {
                double[] b = GraphRenderer.slotOffset(j);
                assertTrue(Math.abs(a[0] - b[0]) >= chipWidth || Math.abs(a[1] - b[1]) >= chipHeight);
            }
        }
    }

    @Test
    void zoomKeepsPointUnderCursor() {
        Camera c = new Camera();
        c.drag(120, -40);
        double worldX = c.worldX(300);
        double worldY = c.worldY(200);
        c.zoomAtCursor(300, 200, 1.7);
        c.jumpToTarget();
        assertEquals(worldX, c.worldX(300), 1e-9);
        assertEquals(worldY, c.worldY(200), 1e-9);
    }

    @Test
    void eachScaleMapsToTheRightLevel() {
        assertEquals(ZoomLevel.FARTHEST, ZoomLevel.forScale(0.05, ZoomLevel.MEDIUM, THRESHOLDS));
        assertEquals(ZoomLevel.FAR, ZoomLevel.forScale(0.42, ZoomLevel.MEDIUM, THRESHOLDS));
        assertEquals(ZoomLevel.MEDIUM, ZoomLevel.forScale(0.72, ZoomLevel.MEDIUM, THRESHOLDS));
        assertEquals(ZoomLevel.NEAR, ZoomLevel.forScale(1, ZoomLevel.MEDIUM, THRESHOLDS));
    }

    @Test
    void hysteresisHoldsLevelNearThreshold() {
        assertEquals(ZoomLevel.MEDIUM, ZoomLevel.forScale(0.87, ZoomLevel.MEDIUM, THRESHOLDS));
        assertEquals(ZoomLevel.NEAR, ZoomLevel.forScale(0.84, ZoomLevel.NEAR, THRESHOLDS));
    }
}
