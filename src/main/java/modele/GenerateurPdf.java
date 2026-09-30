package modele;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

public class GenerateurPdf {

    // image + texte -> fichier PDF
    public void exporter(BufferedImage image, String texte, File fichier) throws QrCodeException {
        if (image == null) {
            throw new QrCodeException("Aucun QR code à exporter.");
        }
        if (fichier == null) {
            throw new QrCodeException("Aucun fichier choisi.");
        }

        try (Document doc = new Document(new PdfDocument(new PdfWriter(fichier.getAbsolutePath())))) {
            // titre
            doc.add(new Paragraph("QR code").setFontSize(20).setTextAlignment(TextAlignment.CENTER));

            // image
            Image qr = new Image(ImageDataFactory.create(versPng(image)));
            qr.setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(qr);

            // légende
            doc.add(new Paragraph(texte).setFontSize(11).setTextAlignment(TextAlignment.CENTER));
        } catch (IOException | PdfException e) {
            // fichier déjà ouvert, pas les droits...
            throw new QrCodeException("Impossible d'écrire le PDF (fichier ouvert ailleurs ou dossier protégé ?).", e);
        }
    }

    // BufferedImage -> octets PNG pour iText
    private byte[] versPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        ImageIO.write(image, "png", sortie);
        return sortie.toByteArray();
    }
}
