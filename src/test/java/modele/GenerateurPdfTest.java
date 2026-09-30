package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateurPdfTest {

    private final GenerateurPdf generateurPdf = new GenerateurPdf();

    // dossier temporaire supprimé après le test
    @TempDir
    Path dossier;

    @Test
    void creePdf() throws Exception {
        BufferedImage image = new GenerateurQrCode().generer("https://exemple.fr");
        File fichier = dossier.resolve("test.pdf").toFile();

        generateurPdf.exporter(image, "https://exemple.fr", fichier);

        assertTrue(fichier.exists());
        assertTrue(fichier.length() > 0);
        // signature d'un PDF
        byte[] debut = Files.readAllBytes(fichier.toPath());
        assertEquals("%PDF", new String(debut, 0, 4));
    }

    @Test
    void imageNull() {
        File fichier = dossier.resolve("vide.pdf").toFile();
        assertThrows(QrCodeException.class, () -> generateurPdf.exporter(null, "x", fichier));
    }

    @Test
    void dossierInexistant() throws Exception {
        BufferedImage image = new GenerateurQrCode().generer("test");
        File fichier = dossier.resolve("existe/pas/test.pdf").toFile();
        assertThrows(QrCodeException.class, () -> generateurPdf.exporter(image, "test", fichier));
    }
}
