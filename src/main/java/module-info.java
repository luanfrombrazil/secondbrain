module io.github.luanfrombrazil.secondbrain {
    requires javafx.controls;
    requires org.snakeyaml.engine.v2;
    requires java.desktop;
    requires java.prefs;

    exports io.github.luanfrombrazil.secondbrain.app to javafx.graphics;
    exports io.github.luanfrombrazil.secondbrain.graph.lab to javafx.graphics;
}
