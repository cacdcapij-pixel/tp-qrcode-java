package modele;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Vérifie la saisie et la met au format attendu par les lecteurs de QR code.
 */
public class FormateurContenu {

    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private static final String REGEX_TELEPHONE = "^\\+?[0-9]{6,15}$";

    /**
     * Renvoie le contenu à encoder selon le type choisi.
     * @throws QrCodeException si la saisie est vide ou invalide
     */
    public String formater(TypeContenu type, String saisie) throws QrCodeException {
        if (type == null) {
            throw new QrCodeException("Aucun type de contenu choisi.");
        }
        if (saisie == null || saisie.isBlank()) {
            throw new QrCodeException("Le texte est vide.");
        }

        String valeur = saisie.trim();
        switch (type) {
            case LIEN:
                return formaterLien(valeur);
            case EMAIL:
                return formaterEmail(valeur);
            case TELEPHONE:
                return formaterTelephone(valeur);
            default:
                // texte libre gardé tel quel
                return saisie;
        }
    }

    // ajoute https:// si oublié + vérifie le domaine
    private String formaterLien(String lien) throws QrCodeException {
        String lower = lien.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            lien = "https://" + lien;
        }
        try {
            String hote = new URI(lien).getHost();
            if (hote == null || !hote.contains(".")) {
                throw new QrCodeException("Lien invalide : " + lien);
            }
        } catch (URISyntaxException e) {
            throw new QrCodeException("Lien invalide : " + lien, e);
        }
        return lien;
    }

    // mailto:
    private String formaterEmail(String email) throws QrCodeException {
        if (!email.matches(REGEX_EMAIL)) {
            throw new QrCodeException("Adresse email invalide : " + email);
        }
        return "mailto:" + email;
    }

    // tel: sans espaces/points/tirets
    private String formaterTelephone(String tel) throws QrCodeException {
        String numero = tel.replaceAll("[\\s.\\-]", "");
        if (!numero.matches(REGEX_TELEPHONE)) {
            throw new QrCodeException("Numéro de téléphone invalide : " + tel);
        }
        return "tel:" + numero;
    }
}
