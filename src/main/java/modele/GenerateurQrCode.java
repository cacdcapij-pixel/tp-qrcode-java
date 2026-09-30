package modele;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class GenerateurQrCode {

    public static final int TAILLE = 300;

    // texte -> image du QR
    public BufferedImage generer(String texte) throws QrCodeException {
        if (texte == null || texte.isBlank()) {
            throw new QrCodeException("Le texte est vide.");
        }

        // options : accents, correction d'erreur, marge
        Map<EncodeHintType, Object> options = new EnumMap<>(EncodeHintType.class);
        options.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        options.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        options.put(EncodeHintType.MARGIN, 2);

        try {
            BitMatrix matrice = new QRCodeWriter().encode(texte, BarcodeFormat.QR_CODE, TAILLE, TAILLE, options);
            return MatrixToImageWriter.toBufferedImage(matrice);
        } catch (WriterException e) {
            // ZXing lève ça quand le texte dépasse la capacité max
            throw new QrCodeException("Texte trop long pour un QR code.", e);
        }
    }
}
