package modele;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Génère l'image d'un QR code avec ZXing.
 */
public class GenerateurQrCode {

    /** Taille de l'image en pixels (carrée). */
    public static final int TAILLE = 300;

    // au-delà, les modules sont trop clairs sur fond blanc pour être scannés
    private static final int LUMINOSITE_MAX = 150;

    /**
     * Transforme un texte en image de QR code noir.
     * @throws QrCodeException si le texte est vide ou trop long
     */
    public BufferedImage generer(String texte) throws QrCodeException {
        return generer(texte, Color.BLACK);
    }

    /**
     * Transforme un texte en image de QR code de la couleur choisie, sur fond blanc.
     * @throws QrCodeException si le texte est vide ou trop long, ou la couleur trop claire
     */
    public BufferedImage generer(String texte, Color couleur) throws QrCodeException {
        if (texte == null || texte.isBlank()) {
            throw new QrCodeException("Le texte est vide.");
        }
        if (couleur == null) {
            couleur = Color.BLACK;
        }
        if (luminosite(couleur) > LUMINOSITE_MAX) {
            throw new QrCodeException("Couleur du QR code trop claire : il ne serait pas lisible. Choisissez une couleur plus foncée.");
        }

        // options : accents, correction d'erreur, marge
        Map<EncodeHintType, Object> options = new EnumMap<>(EncodeHintType.class);
        options.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        options.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        options.put(EncodeHintType.MARGIN, 2);

        try {
            BitMatrix matrice = new QRCodeWriter().encode(texte, BarcodeFormat.QR_CODE, TAILLE, TAILLE, options);
            MatrixToImageConfig config = new MatrixToImageConfig(couleur.getRGB(), Color.WHITE.getRGB());
            return MatrixToImageWriter.toBufferedImage(matrice, config);
        } catch (WriterException e) {
            // ZXing lève ça quand le texte dépasse la capacité max
            throw new QrCodeException("Texte trop long pour un QR code.", e);
        }
    }

    // luminosité perçue 0 (noir) à 255 (blanc)
    private static int luminosite(Color c) {
        return (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
    }
}
