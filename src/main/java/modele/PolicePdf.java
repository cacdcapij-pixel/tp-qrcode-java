package modele;

import com.itextpdf.io.font.constants.StandardFonts;

/**
 * Polices proposées pour le PDF : les 3 polices standard du PDF ou un fichier .ttf/.otf choisi.
 */
public enum PolicePdf {

    HELVETICA("Helvetica", StandardFonts.HELVETICA, StandardFonts.HELVETICA_BOLD),
    TIMES("Times", StandardFonts.TIMES_ROMAN, StandardFonts.TIMES_BOLD),
    COURIER("Courier", StandardFonts.COURIER, StandardFonts.COURIER_BOLD),
    PERSONNALISEE("Personnalisée (fichier)", null, null);

    private final String libelle;
    private final String normale;
    private final String grasse;

    PolicePdf(String libelle, String normale, String grasse) {
        this.libelle = libelle;
        this.normale = normale;
        this.grasse = grasse;
    }

    /** Nom de la police standard iText, null pour une police personnalisée. */
    public String getNomStandard(boolean gras) {
        return gras ? grasse : normale;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
