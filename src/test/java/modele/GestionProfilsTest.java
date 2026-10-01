package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GestionProfilsTest {

    @TempDir
    Path dossier;

    private ProfilPdf profil(String nom, Color couleurTitre) {
        ProfilPdf p = new ProfilPdf();
        p.setNom(nom);
        p.setCouleurTitre(couleurTitre);
        return p;
    }

    @Test
    void dossierPasEncoreCree() {
        GestionProfils gestion = new GestionProfils(dossier.resolve("profils").toFile());
        assertTrue(gestion.lister().isEmpty());
    }

    @Test
    void enregistrerListerCharger() throws Exception {
        // sous-dossier créé au premier enregistrement
        GestionProfils gestion = new GestionProfils(dossier.resolve("profils").toFile());
        gestion.enregistrer(profil("Saint-Luc", Color.BLUE));
        gestion.enregistrer(profil("affiche", Color.RED));

        // tri sans tenir compte des majuscules
        assertEquals(List.of("affiche", "Saint-Luc"), gestion.lister());
        assertTrue(gestion.existe("Saint-Luc"));
        assertEquals(Color.BLUE, gestion.charger("Saint-Luc").getCouleurTitre());
    }

    @Test
    void remplacerUnProfil() throws Exception {
        GestionProfils gestion = new GestionProfils(dossier.toFile());
        gestion.enregistrer(profil("Perso", Color.BLUE));
        gestion.enregistrer(profil("Perso", Color.GREEN));

        assertEquals(1, gestion.lister().size());
        assertEquals(Color.GREEN, gestion.charger("Perso").getCouleurTitre());
    }

    @Test
    void supprimer() throws Exception {
        GestionProfils gestion = new GestionProfils(dossier.toFile());
        gestion.enregistrer(profil("A supprimer", Color.BLACK));
        gestion.supprimer("A supprimer");

        assertFalse(gestion.existe("A supprimer"));
        assertThrows(QrCodeException.class, () -> gestion.supprimer("A supprimer"));
    }

    @Test
    void chargerInconnu() {
        GestionProfils gestion = new GestionProfils(dossier.toFile());
        assertThrows(QrCodeException.class, () -> gestion.charger("Inconnu"));
    }

    @Test
    void nomsInvalides() {
        GestionProfils gestion = new GestionProfils(dossier.toFile());
        assertThrows(QrCodeException.class, () -> gestion.enregistrer(profil("", Color.BLACK)));
        assertThrows(QrCodeException.class, () -> gestion.enregistrer(profil("   ", Color.BLACK)));
        assertThrows(QrCodeException.class, () -> gestion.enregistrer(profil("a/b", Color.BLACK)));
        assertThrows(QrCodeException.class, () -> gestion.enregistrer(profil("quoi?", Color.BLACK)));
        assertThrows(QrCodeException.class, () -> gestion.enregistrer(profil("x".repeat(41), Color.BLACK)));
        // rien n'a été écrit
        assertTrue(gestion.lister().isEmpty());
    }

    @Test
    void espacesAutourDuNomRetires() throws Exception {
        GestionProfils gestion = new GestionProfils(dossier.toFile());
        gestion.enregistrer(profil("  Bleu  ", Color.BLUE));
        assertEquals(List.of("Bleu"), gestion.lister());
    }
}
