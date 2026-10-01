package modele;

/**
 * Types de contenu qu'on peut mettre dans un QR code.
 */
public enum TypeContenu {

    TEXTE("Texte"),
    LIEN("Lien"),
    EMAIL("Email"),
    TELEPHONE("Téléphone");

    private final String libelle;

    TypeContenu(String libelle) {
        this.libelle = libelle;
    }

    // affiché dans la liste déroulante
    @Override
    public String toString() {
        return libelle;
    }
}
