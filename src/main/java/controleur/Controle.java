package controleur;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.SwingUtilities;

import modele.FormateurContenu;
import modele.GenerateurPdf;
import modele.GenerateurQrCode;
import modele.QrCodeException;
import modele.TypeContenu;
import vue.FrmQrCode;

public class Controle {

    private FrmQrCode frmQrCode;
    private final FormateurContenu formateurContenu = new FormateurContenu();
    private final GenerateurQrCode generateurQrCode = new GenerateurQrCode();
    private final GenerateurPdf generateurPdf = new GenerateurPdf();

    // dernier QR généré
    private String contenuCourant;
    private BufferedImage imageCourante;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Controle::new);
    }

    public Controle() {
        frmQrCode = new FrmQrCode(this);
        frmQrCode.setVisible(true);
    }

    // clic Générer
    public void demandeFrmQrCodeGenerer(TypeContenu type, String saisie) {
        try {
            String contenu = formateurContenu.formater(type, saisie);
            imageCourante = generateurQrCode.generer(contenu);
            contenuCourant = contenu;
            frmQrCode.afficheQrCode(imageCourante, contenu);
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    // clic Exporter
    public void demandeFrmQrCodeExporter(File fichier) {
        try {
            generateurPdf.exporter(imageCourante, contenuCourant, fichier);
            frmQrCode.afficheSucces("PDF enregistré :\n" + fichier.getAbsolutePath());
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }
}
