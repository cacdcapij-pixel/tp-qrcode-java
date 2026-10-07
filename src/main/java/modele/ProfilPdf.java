package modele;

import java.awt.Color;
import java.io.File;
import java.util.Objects;

/**
 * Profil de style du PDF : police, tailles et couleurs.
 * Les couleurs sont gardées en texte "#RRGGBB" pour que le fichier JSON reste lisible.
 */
public class ProfilPdf {

    public static final int TAILLE_MIN = 6;
    public static final int TAILLE_MAX = 48;

    private static final String REGEX_COULEUR = "^#[0-9A-Fa-f]{6}$";

    // repère pour reconnaître un fichier de profil
    private String format = "qrcode-profil";

    private String nom = "";
    private PolicePdf police = PolicePdf.HELVETICA;
    private String cheminPolice;
    private int tailleTitre = 20;
    private int tailleTexte = 11;
    private boolean titreGras = true;
    private String couleurTitre = "#000000";
    private String couleurTexte = "#000000";
    private String couleurQr = "#000000";

    /**
     * Vérifie un profil relu depuis un fichier.
     * @throws QrCodeException si une valeur est absente ou incohérente
     */
    public void valider() throws QrCodeException {
        if (!"qrcode-profil".equals(format)) {
            throw new QrCodeException("Ce fichier n'est pas un profil.");
        }
        if (police == null) {
            throw new QrCodeException("Profil invalide : police inconnue.");
        }
        if (police == PolicePdf.PERSONNALISEE && (cheminPolice == null || cheminPolice.isBlank())) {
            throw new QrCodeException("Profil invalide : aucun fichier de police.");
        }
        verifierTaille(tailleTitre);
        verifierTaille(tailleTexte);
        verifierCouleur(couleurTitre);
        verifierCouleur(couleurTexte);
        verifierCouleur(couleurQr);
    }

    private static void verifierTaille(int taille) throws QrCodeException {
        if (taille < TAILLE_MIN || taille > TAILLE_MAX) {
            throw new QrCodeException("Profil invalide : taille " + taille + " hors limites ("
                    + TAILLE_MIN + " à " + TAILLE_MAX + ").");
        }
    }

    private static void verifierCouleur(String couleur) throws QrCodeException {
        if (couleur == null || !couleur.matches(REGEX_COULEUR)) {
            throw new QrCodeException("Profil invalide : couleur « " + couleur + " » incorrecte.");
        }
    }

    /** Vrai si les deux profils donnent le même rendu (le nom n'est pas comparé). */
    public boolean memeStyle(ProfilPdf autre) {
        return autre != null
                && police == autre.police
                // le fichier ne compte que pour une police personnalisée
                && (police != PolicePdf.PERSONNALISEE || Objects.equals(cheminPolice, autre.cheminPolice))
                && tailleTitre == autre.tailleTitre
                && tailleTexte == autre.tailleTexte
                && titreGras == autre.titreGras
                && couleurTitre.equalsIgnoreCase(autre.couleurTitre)
                && couleurTexte.equalsIgnoreCase(autre.couleurTexte)
                && couleurQr.equalsIgnoreCase(autre.couleurQr);
    }

    // Color <-> "#RRGGBB"
    private static String versTexte(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    // getters / setters

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public PolicePdf getPolice() {
        return police;
    }

    public void setPolice(PolicePdf police) {
        this.police = police;
    }

    /** Fichier .ttf/.otf, utilisé seulement pour une police personnalisée. */
    public File getFichierPolice() {
        return cheminPolice == null || cheminPolice.isBlank() ? null : new File(cheminPolice);
    }

    public void setFichierPolice(File fichier) {
        cheminPolice = fichier == null ? null : fichier.getAbsolutePath();
    }

    public int getTailleTitre() {
        return tailleTitre;
    }

    public void setTailleTitre(int tailleTitre) {
        this.tailleTitre = tailleTitre;
    }

    public int getTailleTexte() {
        return tailleTexte;
    }

    public void setTailleTexte(int tailleTexte) {
        this.tailleTexte = tailleTexte;
    }

    public boolean isTitreGras() {
        return titreGras;
    }

    public void setTitreGras(boolean titreGras) {
        this.titreGras = titreGras;
    }

    public Color getCouleurTitre() {
        return Color.decode(couleurTitre);
    }

    public void setCouleurTitre(Color c) {
        couleurTitre = versTexte(c);
    }

    public Color getCouleurTexte() {
        return Color.decode(couleurTexte);
    }

    public void setCouleurTexte(Color c) {
        couleurTexte = versTexte(c);
    }

    public Color getCouleurQr() {
        return Color.decode(couleurQr);
    }

    public void setCouleurQr(Color c) {
        couleurQr = versTexte(c);
    }

    // affiché dans la liste des profils
    @Override
    public String toString() {
        return nom;
    }
}
