package controleur;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.SwingUtilities;

import modele.GenerateurPdf;
import modele.GenerateurQrCode;
import modele.QrCodeException;
import vue.FrmQrCode;

public class Controle {

    private FrmQrCode frmQrCode;
    private final GenerateurQrCode generateurQrCode = new GenerateurQrCode();
    private final GenerateurPdf generateurPdf = new GenerateurPdf();

    // dernier QR généré
    private String texteCourant;
    private BufferedImage imageCourante;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Controle::new);
    }

    public Controle() {
        frmQrCode = new FrmQrCode(this);
        frmQrCode.setVisible(true);
    }

    // clic Générer
    public void demandeFrmQrCodeGenerer(String texte) {
        try {
            imageCourante = generateurQrCode.generer(texte);
            texteCourant = texte;
            frmQrCode.afficheQrCode(imageCourante);
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    // clic Exporter
    public void demandeFrmQrCodeExporter(File fichier) {
        try {
            generateurPdf.exporter(imageCourante, texteCourant, fichier);
            frmQrCode.afficheSucces("PDF enregistré :\n" + fichier.getAbsolutePath());
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }
}
