package modele;

/**
 * Alignement horizontal d'une image dans le PDF.
 */
public enum AlignementImage {

    GAUCHE("Gauche"),
    CENTRE("Centre"),
    DROITE("Droite");

    private final String libelle;

    AlignementImage(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
