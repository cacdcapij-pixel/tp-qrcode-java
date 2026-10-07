package modele;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

/**
 * Crée le fichier PDF contenant le QR code avec iText, mis en forme selon le profil du projet.
 */
public class GenerateurPdf {

    // A4 moins les marges par défaut d'iText (36 pt de chaque côté)
    private static final float LARGEUR_UTILE = PageSize.A4.getWidth() - 2 * 36;
    // hauteur max d'une image ajoutée, pour garder le QR sur la page
    private static final float HAUTEUR_IMAGE_MAX = 250;

    /**
     * Écrit un PDF : titre, images du haut, QR code, contenu en légende, images du bas.
     * Le PDF est construit en mémoire puis écrit d'un coup : en cas d'erreur, aucun fichier à moitié écrit.
     * @throws QrCodeException si rien à exporter, police ou image invalide, ou fichier impossible à écrire
     */
    public void exporter(Projet projet, BufferedImage image, String texte, File fichier) throws QrCodeException {
        if (image == null) {
            throw new QrCodeException("Aucun QR code à exporter.");
        }
        ProfilPdf profil = projet.getProfil();

        // polices et images chargées avant de construire le PDF
        PdfFont policeTitre = creerPolice(profil, profil.isTitreGras());
        PdfFont policeTexte = creerPolice(profil, false);
        List<Image> imagesHaut = new ArrayList<>();
        List<Image> imagesBas = new ArrayList<>();
        for (ImagePdf img : projet.getImages()) {
            Image element = creerImage(img);
            if (img.getEmplacement() == EmplacementImage.BAS) {
                imagesBas.add(element);
            } else {
                imagesHaut.add(element);
            }
        }

        ByteArrayOutputStream memoire = new ByteArrayOutputStream();
        try (Document doc = new Document(new PdfDocument(new PdfWriter(memoire)), PageSize.A4)) {
            // titre
            String titre = projet.getTitre() == null || projet.getTitre().isBlank() ? "QR code" : projet.getTitre();
            Paragraph pTitre = new Paragraph(titre)
                    .setFont(policeTitre)
                    .setFontSize(profil.getTailleTitre())
                    .setFontColor(new DeviceRgb(profil.getCouleurTitre()))
                    .setTextAlignment(TextAlignment.CENTER);
            // police perso : pas de variante grasse, gras simulé
            if (profil.isTitreGras() && profil.getPolice() == PolicePdf.PERSONNALISEE) {
                pTitre.setBold();
            }
            doc.add(pTitre);

            // images du haut
            for (Image element : imagesHaut) {
                doc.add(element);
            }

            // QR code
            Image qr = new Image(ImageDataFactory.create(versPng(image)));
            qr.setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(qr);

            // légende
            doc.add(new Paragraph(texte)
                    .setFont(policeTexte)
                    .setFontSize(profil.getTailleTexte())
                    .setFontColor(new DeviceRgb(profil.getCouleurTexte()))
                    .setTextAlignment(TextAlignment.CENTER));

            // images du bas
            for (Image element : imagesBas) {
                doc.add(element);
            }
        } catch (IOException | RuntimeException e) {
            // erreurs iText pendant la construction (police sans certains caractères...)
            throw new QrCodeException("Erreur pendant la création du PDF : " + e.getMessage(), e);
        }

        try {
            Files.write(fichier.toPath(), memoire.toByteArray());
        } catch (IOException e) {
            // fichier déjà ouvert, pas les droits...
            throw new QrCodeException("Impossible d'écrire le PDF (fichier ouvert ailleurs ou dossier protégé ?).", e);
        }
    }

    // police standard ou fichier .ttf/.otf intégré au PDF
    private PdfFont creerPolice(ProfilPdf profil, boolean gras) throws QrCodeException {
        PolicePdf police = profil.getPolice();
        try {
            if (police != PolicePdf.PERSONNALISEE) {
                return PdfFontFactory.createFont(police.getNomStandard(gras));
            }
            File f = profil.getFichierPolice();
            if (f == null) {
                throw new QrCodeException("Aucun fichier de police choisi.");
            }
            if (!f.isFile()) {
                throw new QrCodeException("Police introuvable : " + f.getAbsolutePath());
            }
            return PdfFontFactory.createFont(f.getAbsolutePath(), PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (IOException | RuntimeException e) {
            throw new QrCodeException("Police invalide : " + (profil.getFichierPolice() == null
                    ? police : profil.getFichierPolice().getName()), e);
        }
    }

    // fichier image -> élément iText aligné
    private Image creerImage(ImagePdf img) throws QrCodeException {
        File f = img.getFichier();
        if (!f.isFile()) {
            throw new QrCodeException("Image introuvable : " + f.getAbsolutePath());
        }
        ImageData data;
        try {
            data = ImageDataFactory.create(f.getAbsolutePath());
        } catch (IOException | RuntimeException e) {
            throw new QrCodeException("Format d'image non pris en charge : " + f.getName(), e);
        }
        Image element = new Image(data);
        element.setMarginTop(5).setMarginBottom(5);
        switch (img.getAlignement()) {
            case GAUCHE:
                element.setHorizontalAlignment(HorizontalAlignment.LEFT);
                break;
            case DROITE:
                element.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                break;
            default:
                element.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
        // taille : % de la largeur utile, proportions gardées, hauteur plafonnée
        float largeur = LARGEUR_UTILE * img.getLargeur() / 100f;
        float hauteur = largeur * element.getImageHeight() / element.getImageWidth();
        if (hauteur > HAUTEUR_IMAGE_MAX) {
            largeur = largeur * HAUTEUR_IMAGE_MAX / hauteur;
            hauteur = HAUTEUR_IMAGE_MAX;
        }
        return element.scaleAbsolute(largeur, hauteur);
    }

    // BufferedImage -> octets PNG pour iText
    private byte[] versPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        ImageIO.write(image, "png", sortie);
        return sortie.toByteArray();
    }
}
