package modele;

/**
 * Où placer une image dans le PDF, par rapport au QR code.
 */
public enum EmplacementImage {

    HAUT("Au-dessus du QR"),
    BAS("Sous le QR");

    private final String libelle;

    EmplacementImage(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
