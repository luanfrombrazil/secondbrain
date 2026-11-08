module io.github.luanfrombrazil.secondbrain {
    requires javafx.controls;
    requires org.snakeyaml.engine.v2;
    requires java.desktop;
    requires java.prefs;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires org.apache.pdfbox;

    exports io.github.luanfrombrazil.secondbrain.app to javafx.graphics;
    exports io.github.luanfrombrazil.secondbrain.graph.lab to javafx.graphics;
}
