package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

class GenerateurQrCodeTest {

    private final GenerateurQrCode generateur = new GenerateurQrCode();

    // relit le QR avec ZXing
    private String decoder(BufferedImage image) throws Exception {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new MultiFormatReader().decode(bitmap).getText();
    }

    @Test
    void allerRetourLien() throws Exception {
        String texte = "https://exemple.fr";
        assertEquals(texte, decoder(generateur.generer(texte)));
    }

    @Test
    void allerRetourAccents() throws Exception {
        String texte = "Café à Cambrai, élève de BTS";
        assertEquals(texte, decoder(generateur.generer(texte)));
    }

    @Test
    void tailleImage() throws Exception {
        BufferedImage image = generateur.generer("test");
        assertEquals(GenerateurQrCode.TAILLE, image.getWidth());
        assertEquals(GenerateurQrCode.TAILLE, image.getHeight());
    }

    @Test
    void texteVide() {
        assertThrows(QrCodeException.class, () -> generateur.generer(""));
        assertThrows(QrCodeException.class, () -> generateur.generer("   "));
    }

    @Test
    void texteNull() {
        assertThrows(QrCodeException.class, () -> generateur.generer(null));
    }

    @Test
    void texteTropLong() {
        assertThrows(QrCodeException.class, () -> generateur.generer("a".repeat(5000)));
    }
}
