package modele;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Enregistre et relit les projets et les profils au format JSON (Gson).
 */
public class Sauvegarde {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Enregistre un projet ou un profil en JSON.
     * @throws QrCodeException si le fichier ne peut pas être écrit
     */
    public void enregistrer(Object objet, File fichier) throws QrCodeException {
        try {
            Files.writeString(fichier.toPath(), gson.toJson(objet), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new QrCodeException("Impossible d'enregistrer « " + fichier.getName()
                    + " » (dossier inexistant ou protégé ?).", e);
        }
    }

    /** @throws QrCodeException si le fichier est introuvable, illisible ou n'est pas un projet */
    public Projet chargerProjet(File fichier) throws QrCodeException {
        Projet projet = lire(fichier, Projet.class);
        projet.valider();
        return projet;
    }

    /** @throws QrCodeException si le fichier est introuvable, illisible ou n'est pas un profil */
    public ProfilPdf chargerProfil(File fichier) throws QrCodeException {
        ProfilPdf profil = lire(fichier, ProfilPdf.class);
        profil.valider();
        return profil;
    }

    private <T> T lire(File fichier, Class<T> classe) throws QrCodeException {
        try {
            String json = Files.readString(fichier.toPath(), StandardCharsets.UTF_8);
            T objet = gson.fromJson(json, classe);
            if (objet == null) {
                throw new QrCodeException("Le fichier « " + fichier.getName() + " » est vide.");
            }
            return objet;
        } catch (NoSuchFileException e) {
            throw new QrCodeException("Fichier introuvable : " + fichier.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new QrCodeException("Impossible de lire « " + fichier.getName() + " ».", e);
        } catch (JsonParseException e) {
            throw new QrCodeException("Le fichier « " + fichier.getName() + " » est abîmé ou n'a pas le bon format.", e);
        }
    }
}
