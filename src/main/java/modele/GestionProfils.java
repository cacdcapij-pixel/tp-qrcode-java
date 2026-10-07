package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Profils enregistrés par nom dans un dossier (un fichier .json par profil).
 */
public class GestionProfils {

    private static final String EXTENSION = ".json";
    private static final String REGEX_NOM = "^[^\\\\/:*?\"<>|]{1,40}$";

    private final File dossier;
    private final Sauvegarde sauvegarde = new Sauvegarde();

    public GestionProfils(File dossier) {
        this.dossier = dossier;
    }

    /** Dossier utilisé par l'application : ~/.qrcode/profils */
    public static File dossierParDefaut() {
        return new File(System.getProperty("user.home"), ".qrcode" + File.separator + "profils");
    }

    /** Noms des profils enregistrés, triés. Liste vide si le dossier n'existe pas encore. */
    public List<String> lister() {
        List<String> noms = new ArrayList<>();
        File[] fichiers = dossier.listFiles((d, nom) -> nom.toLowerCase().endsWith(EXTENSION));
        if (fichiers != null) {
            for (File f : fichiers) {
                noms.add(f.getName().substring(0, f.getName().length() - EXTENSION.length()));
            }
        }
        noms.sort(String.CASE_INSENSITIVE_ORDER);
        return noms;
    }

    /** Vrai si un profil porte déjà ce nom. */
    public boolean existe(String nom) {
        return nom != null && fichier(nom.trim()).exists();
    }

    /**
     * Enregistre le profil sous son nom (remplace s'il existe déjà).
     * @throws QrCodeException si le nom est invalide ou l'écriture impossible
     */
    public void enregistrer(ProfilPdf profil) throws QrCodeException {
        String nom = verifierNom(profil.getNom());
        profil.setNom(nom);
        if (!dossier.isDirectory() && !dossier.mkdirs()) {
            throw new QrCodeException("Impossible de créer le dossier des profils : " + dossier);
        }
        sauvegarde.enregistrer(profil, fichier(nom));
    }

    /** @throws QrCodeException si le profil n'existe pas ou est abîmé */
    public ProfilPdf charger(String nom) throws QrCodeException {
        nom = verifierNom(nom);
        ProfilPdf profil = sauvegarde.chargerProfil(fichier(nom));
        profil.setNom(nom);
        return profil;
    }

    /** @throws QrCodeException si le profil n'existe pas ou ne peut pas être supprimé */
    public void supprimer(String nom) throws QrCodeException {
        nom = verifierNom(nom);
        File f = fichier(nom);
        if (!f.exists()) {
            throw new QrCodeException("Le profil « " + nom + " » n'existe pas.");
        }
        if (!f.delete()) {
            throw new QrCodeException("Impossible de supprimer le profil « " + nom + " ».");
        }
    }

    private String verifierNom(String nom) throws QrCodeException {
        if (nom == null || nom.isBlank()) {
            throw new QrCodeException("Le nom du profil est vide.");
        }
        nom = nom.trim();
        if (!nom.matches(REGEX_NOM) || nom.startsWith(".")) {
            throw new QrCodeException("Nom de profil invalide : 40 caractères max, sans \\ / : * ? \" < > |");
        }
        return nom;
    }

    private File fichier(String nom) {
        return new File(dossier, nom + EXTENSION);
    }
}
