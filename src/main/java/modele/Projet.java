package modele;

import java.util.ArrayList;
import java.util.List;

/**
 * Projet sauvegardable : titre du PDF, contenu du QR, profil de style et images.
 */
public class Projet {

    /** Repère pour reconnaître un fichier de projet. */
    private String format = "qrcode-projet";

    private String titre = "QR code";
    private TypeContenu type = TypeContenu.TEXTE;
    private String saisie = "";
    private ProfilPdf profil = new ProfilPdf();
    private List<ImagePdf> images = new ArrayList<>();

    /**
     * Vérifie un projet relu depuis un fichier.
     * @throws QrCodeException si le fichier n'est pas un projet ou contient des valeurs invalides
     */
    public void valider() throws QrCodeException {
        if (!"qrcode-projet".equals(format)) {
            throw new QrCodeException("Ce fichier n'est pas un projet.");
        }
        if (titre == null || type == null || saisie == null || profil == null || images == null) {
            throw new QrCodeException("Projet invalide : informations manquantes.");
        }
        profil.valider();
        for (ImagePdf image : images) {
            if (image == null) {
                throw new QrCodeException("Projet invalide : image incomplète.");
            }
            image.valider();
        }
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public TypeContenu getType() {
        return type;
    }

    public void setType(TypeContenu type) {
        this.type = type;
    }

    public String getSaisie() {
        return saisie;
    }

    public void setSaisie(String saisie) {
        this.saisie = saisie;
    }

    public ProfilPdf getProfil() {
        return profil;
    }

    public void setProfil(ProfilPdf profil) {
        this.profil = profil;
    }

    public List<ImagePdf> getImages() {
        return images;
    }

    public void setImages(List<ImagePdf> images) {
        this.images = images;
    }
}
