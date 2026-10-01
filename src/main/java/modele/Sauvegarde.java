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

    // indenté, apostrophes non échappées : fichier lisible à la main
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** @throws QrCodeException si le fichier ne peut pas être écrit */
    public void enregistrerProjet(Projet projet, File fichier) throws QrCodeException {
        ecrire(projet, fichier);
    }

    /** @throws QrCodeException si le fichier est introuvable, illisible ou n'est pas un projet */
    public Projet chargerProjet(File fichier) throws QrCodeException {
        Projet projet = lire(fichier, Projet.class);
        projet.valider();
        return projet;
    }

    /** @throws QrCodeException si le fichier ne peut pas être écrit */
    public void enregistrerProfil(ProfilPdf profil, File fichier) throws QrCodeException {
        ecrire(profil, fichier);
    }

    /** @throws QrCodeException si le fichier est introuvable, illisible ou n'est pas un profil */
    public ProfilPdf chargerProfil(File fichier) throws QrCodeException {
        ProfilPdf profil = lire(fichier, ProfilPdf.class);
        profil.valider();
        return profil;
    }

    // objet -> JSON -> fichier
    private void ecrire(Object objet, File fichier) throws QrCodeException {
        if (objet == null || fichier == null) {
            throw new QrCodeException("Rien à enregistrer.");
        }
        try {
            Files.writeString(fichier.toPath(), gson.toJson(objet), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new QrCodeException("Impossible d'enregistrer « " + fichier.getName()
                    + " » (dossier inexistant ou protégé ?).", e);
        }
    }

    // fichier -> JSON -> objet
    private <T> T lire(File fichier, Class<T> classe) throws QrCodeException {
        if (fichier == null) {
            throw new QrCodeException("Aucun fichier choisi.");
        }
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
            // JSON cassé, ou valeur inconnue (ex : type de contenu qui n'existe pas)
            throw new QrCodeException("Le fichier « " + fichier.getName() + " » est abîmé ou n'a pas le bon format.", e);
        }
    }
}
