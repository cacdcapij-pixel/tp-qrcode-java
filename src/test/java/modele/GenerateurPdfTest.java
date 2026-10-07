package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

class GenerateurPdfTest {

    private final GenerateurPdf generateurPdf = new GenerateurPdf();

    @TempDir
    Path dossier;

    @Test
    void creePdf() throws Exception {
        BufferedImage image = new GenerateurQrCode().generer("https://exemple.fr");
        File fichier = dossier.resolve("test.pdf").toFile();

        generateurPdf.exporter(new Projet(), image, "https://exemple.fr", fichier);

        assertTrue(fichier.exists());
        assertTrue(fichier.length() > 0);
        byte[] debut = Files.readAllBytes(fichier.toPath());
        assertEquals("%PDF", new String(debut, 0, 4));
    }

    @Test
    void imageNull() {
        File fichier = dossier.resolve("vide.pdf").toFile();
        assertThrows(QrCodeException.class, () -> generateurPdf.exporter(new Projet(), null, "x", fichier));
    }

    @Test
    void dossierInexistant() throws Exception {
        BufferedImage image = new GenerateurQrCode().generer("test");
        File fichier = dossier.resolve("existe/pas/test.pdf").toFile();
        assertThrows(QrCodeException.class, () -> generateurPdf.exporter(new Projet(), image, "test", fichier));
    }

    private File creerImage(String nom, int largeur, int hauteur) throws Exception {
        BufferedImage img = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        img.getGraphics().fillRect(0, 0, largeur, hauteur);
        File f = dossier.resolve(nom).toFile();
        ImageIO.write(img, "png", f);
        return f;
    }

    private int compterImages(PdfPage page) {
        return page.getResources().getResourceNames(PdfName.XObject).size();
    }

    @Test
    void pdfAvecStyleEtImages() throws Exception {
        Projet projet = new Projet();
        projet.setTitre("Mon titre perso");
        ProfilPdf profil = projet.getProfil();
        profil.setPolice(PolicePdf.TIMES);
        profil.setTailleTitre(28);
        profil.setCouleurTitre(new Color(200, 0, 0));
        profil.setCouleurTexte(Color.BLUE);

        ImagePdf haut = new ImagePdf(creerImage("logo.png", 200, 100));
        haut.setAlignement(AlignementImage.GAUCHE);
        haut.setLargeur(20);
        ImagePdf bas = new ImagePdf(creerImage("bandeau.png", 600, 80));
        bas.setEmplacement(EmplacementImage.BAS);
        bas.setLargeur(100);
        projet.getImages().add(haut);
        projet.getImages().add(bas);

        BufferedImage qr = new GenerateurQrCode().generer("https://exemple.fr", new Color(0, 80, 0));
        File fichier = dossier.resolve("style.pdf").toFile();
        generateurPdf.exporter(projet, qr, "https://exemple.fr", fichier);

        try (PdfDocument pdf = new PdfDocument(new PdfReader(fichier))) {
            PdfPage page = pdf.getFirstPage();
            String texte = PdfTextExtractor.getTextFromPage(page);
            assertTrue(texte.contains("Mon titre perso"));
            assertTrue(texte.contains("https://exemple.fr"));
            assertEquals(3, compterImages(page));
        }
    }

    @Test
    void imageIntrouvable() throws Exception {
        Projet projet = new Projet();
        projet.getImages().add(new ImagePdf(dossier.resolve("absente.png").toFile()));
        BufferedImage qr = new GenerateurQrCode().generer("test");
        File fichier = dossier.resolve("rate.pdf").toFile();

        QrCodeException e = assertThrows(QrCodeException.class,
                () -> generateurPdf.exporter(projet, qr, "test", fichier));
        assertTrue(e.getMessage().startsWith("Image introuvable"));
        assertFalse(fichier.exists());
    }

    @Test
    void imageFormatInvalide() throws Exception {
        File faux = dossier.resolve("pas-une-image.png").toFile();
        Files.writeString(faux.toPath(), "ceci n'est pas une image");
        Projet projet = new Projet();
        projet.getImages().add(new ImagePdf(faux));
        BufferedImage qr = new GenerateurQrCode().generer("test");

        assertThrows(QrCodeException.class,
                () -> generateurPdf.exporter(projet, qr, "test", dossier.resolve("rate.pdf").toFile()));
    }

    @Test
    void policePersonnaliseeSansFichier() throws Exception {
        Projet projet = new Projet();
        projet.getProfil().setPolice(PolicePdf.PERSONNALISEE);
        BufferedImage qr = new GenerateurQrCode().generer("test");

        assertThrows(QrCodeException.class,
                () -> generateurPdf.exporter(projet, qr, "test", dossier.resolve("rate.pdf").toFile()));
    }

    @Test
    void policePersonnaliseeIntrouvable() throws Exception {
        Projet projet = new Projet();
        projet.getProfil().setPolice(PolicePdf.PERSONNALISEE);
        projet.getProfil().setFichierPolice(dossier.resolve("absente.ttf").toFile());
        BufferedImage qr = new GenerateurQrCode().generer("test");

        assertThrows(QrCodeException.class,
                () -> generateurPdf.exporter(projet, qr, "test", dossier.resolve("rate.pdf").toFile()));
    }

    @Test
    void policePersonnaliseeTtf() throws Exception {
        File arial = new File("C:/Windows/Fonts/arial.ttf");
        assumeTrue(arial.isFile());

        Projet projet = new Projet();
        projet.setTitre("Élève – police intégrée");
        projet.getProfil().setPolice(PolicePdf.PERSONNALISEE);
        projet.getProfil().setFichierPolice(arial);
        BufferedImage qr = new GenerateurQrCode().generer("test");
        File fichier = dossier.resolve("arial.pdf").toFile();

        generateurPdf.exporter(projet, qr, "test", fichier);

        try (PdfDocument pdf = new PdfDocument(new PdfReader(fichier))) {
            PdfPage page = pdf.getFirstPage();
            String texte = PdfTextExtractor.getTextFromPage(page);
            assertTrue(texte.contains("Élève – police intégrée"));

            PdfDictionary polices = page.getResources().getResource(PdfName.Font);
            boolean trouvee = false;
            for (PdfName cle : polices.keySet()) {
                PdfName nom = polices.getAsDictionary(cle).getAsName(PdfName.BaseFont);
                if (nom != null && nom.getValue().contains("Arial")) {
                    trouvee = true;
                }
            }
            assertTrue(trouvee);
        }
    }
}
