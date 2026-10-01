package modele;

import java.io.File;

/**
 * Image ajoutée au PDF : fichier, emplacement, alignement et largeur.
 */
public class ImagePdf {

    public static final int LARGEUR_MIN = 5;
    public static final int LARGEUR_MAX = 100;

    private String chemin;
    private EmplacementImage emplacement = EmplacementImage.HAUT;
    private AlignementImage alignement = AlignementImage.CENTRE;
    // en % de la largeur utile de la page
    private int largeur = 30;

    // utilisé par Gson à la lecture
    private ImagePdf() {
    }

    public ImagePdf(File fichier) {
        this.chemin = fichier.getAbsolutePath();
    }

    /** Copie d'une image. */
    public ImagePdf(ImagePdf autre) {
        chemin = autre.chemin;
        emplacement = autre.emplacement;
        alignement = autre.alignement;
        largeur = autre.largeur;
    }

    /**
     * Vérifie une image relue depuis un fichier de projet.
     * @throws QrCodeException si une valeur est absente ou hors limites
     */
    public void valider() throws QrCodeException {
        if (chemin == null || chemin.isBlank() || emplacement == null || alignement == null) {
            throw new QrCodeException("Projet invalide : image incomplète.");
        }
        if (largeur < LARGEUR_MIN || largeur > LARGEUR_MAX) {
            throw new QrCodeException("Projet invalide : largeur d'image " + largeur + " % hors limites.");
        }
    }

    // getters / setters

    public File getFichier() {
        return new File(chemin);
    }

    public EmplacementImage getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(EmplacementImage emplacement) {
        this.emplacement = emplacement;
    }

    public AlignementImage getAlignement() {
        return alignement;
    }

    public void setAlignement(AlignementImage alignement) {
        this.alignement = alignement;
    }

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }

    // affiché dans la liste des images
    @Override
    public String toString() {
        return getFichier().getName() + "  –  " + emplacement + ", " + alignement.toString().toLowerCase()
                + ", " + largeur + " %";
    }
}
