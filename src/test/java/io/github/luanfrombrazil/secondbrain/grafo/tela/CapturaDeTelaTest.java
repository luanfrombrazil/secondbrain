package io.github.luanfrombrazil.secondbrain.grafo.tela;

import io.github.luanfrombrazil.secondbrain.grafo.Parametros;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gera imagens da tela. Só roda com: mvn test -Dcaptura=true -Dtest=CapturaDeTelaTest */
@EnabledIfSystemProperty(named = "captura", matches = "true")
class CapturaDeTelaTest {

    private static final int LARGURA = 1500;
    private static final int ALTURA = 900;

    @Test
    void capturar() throws Exception {
        Path pasta = Path.of("target", "capturas");
        Files.createDirectories(pasta);
        CountDownLatch fim = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                gerar(pasta);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                fim.countDown();
            }
        });
        assertTrue(fim.await(120, TimeUnit.SECONDS));
    }

    private void gerar(Path pasta) throws IOException {
        for (TipoDeDisposicao tipo : TipoDeDisposicao.values()) {
            capturar(pasta, tipo, 50, null, tipo.name().toLowerCase());
        }
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 200, null, "dependencia-200");
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 10, 0.08, "zoom-bolinha");
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 10, 0.2, "zoom-muito-afastado");
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 10, 0.42, "zoom-afastado");
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 10, 0.72, "zoom-medio");
        capturar(pasta, TipoDeDisposicao.DEPENDENCIA, 10, 1.0, "zoom-proximo");
    }

    private void capturar(Path pasta, TipoDeDisposicao tipo, int tamanho, Double zoom, String nome) throws IOException {
        TelaDoGrafo tela = new TelaDoGrafo(new Parametros());
        tela.pararAnimacao();
        new Scene(tela, LARGURA, ALTURA);
        tela.resize(LARGURA, ALTURA);
        tela.layout();
        Exemplos.Exemplo exemplo = Exemplos.gerar(tamanho);
        tela.mostrar(exemplo.grafo(), exemplo.cartoes(), exemplo.pontuais());
        tela.reorganizar(tipo);
        tela.assentar();
        tela.enquadrarSemAnimar();
        if (zoom != null) {
            double fator = zoom / tela.camera().escala();
            tela.camera().zoomNoCursor(LARGURA / 2.0, ALTURA / 2.0, fator);
        }
        tela.assentar();
        tela.desenharAgora();
        WritableImage imagem = tela.canvas().snapshot(null, null);
        ImageIO.write(paraBuffered(imagem), "png", pasta.resolve(nome + ".png").toFile());
    }

    private BufferedImage paraBuffered(WritableImage imagem) {
        int w = (int) imagem.getWidth();
        int h = (int) imagem.getHeight();
        BufferedImage saida = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader leitor = imagem.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                saida.setRGB(x, y, leitor.getArgb(x, y));
            }
        }
        return saida;
    }
}
