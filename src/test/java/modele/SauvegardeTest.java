package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SauvegardeTest {

    private final Sauvegarde sauvegarde = new Sauvegarde();

    @TempDir
    Path dossier;

    private Projet projetExemple() {
        Projet projet = new Projet();
        projet.setTitre("Carte de visite");
        projet.setType(TypeContenu.EMAIL);
        projet.setSaisie("eleve@saint-luc.fr");
        projet.getProfil().setPolice(PolicePdf.COURIER);
        projet.getProfil().setTailleTitre(30);
        projet.getProfil().setTitreGras(false);
        projet.getProfil().setCouleurQr(new Color(0, 51, 153));
        ImagePdf image = new ImagePdf(new File("logo.png"));
        image.setEmplacement(EmplacementImage.BAS);
        image.setAlignement(AlignementImage.DROITE);
        image.setLargeur(45);
        projet.getImages().add(image);
        return projet;
    }

    @Test
    void allerRetourProjet() throws Exception {
        File fichier = dossier.resolve("test.qrproj").toFile();
        sauvegarde.enregistrer(projetExemple(), fichier);

        Projet relu = sauvegarde.chargerProjet(fichier);
        assertEquals("Carte de visite", relu.getTitre());
        assertEquals(TypeContenu.EMAIL, relu.getType());
        assertEquals("eleve@saint-luc.fr", relu.getSaisie());
        assertEquals(PolicePdf.COURIER, relu.getProfil().getPolice());
        assertEquals(30, relu.getProfil().getTailleTitre());
        assertFalse(relu.getProfil().isTitreGras());
        assertEquals(new Color(0, 51, 153), relu.getProfil().getCouleurQr());

        assertEquals(1, relu.getImages().size());
        ImagePdf image = relu.getImages().get(0);
        assertEquals("logo.png", image.getFichier().getName());
        assertEquals(EmplacementImage.BAS, image.getEmplacement());
        assertEquals(AlignementImage.DROITE, image.getAlignement());
        assertEquals(45, image.getLargeur());
    }

    @Test
    void fichierLisible() throws Exception {
        File fichier = dossier.resolve("test.qrproj").toFile();
        sauvegarde.enregistrer(projetExemple(), fichier);
        String json = Files.readString(fichier.toPath());
        assertTrue(json.contains("\"titre\": \"Carte de visite\""));
        assertTrue(json.contains("\"couleurQr\": \"#003399\""));
    }

    @Test
    void allerRetourProfil() throws Exception {
        ProfilPdf profil = new ProfilPdf();
        profil.setNom("Rouge");
        profil.setCouleurTitre(Color.RED);
        profil.setTailleTexte(14);
        File fichier = dossier.resolve("rouge.json").toFile();
        sauvegarde.enregistrer(profil, fichier);

        ProfilPdf relu = sauvegarde.chargerProfil(fichier);
        assertEquals("Rouge", relu.getNom());
        assertEquals(Color.RED, relu.getCouleurTitre());
        assertEquals(14, relu.getTailleTexte());
    }

    @Test
    void fichierInexistant() {
        File fichier = dossier.resolve("absent.qrproj").toFile();
        QrCodeException e = assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
        assertTrue(e.getMessage().startsWith("Fichier introuvable"));
    }

    @Test
    void jsonAbime() throws Exception {
        File fichier = dossier.resolve("casse.qrproj").toFile();
        Files.writeString(fichier.toPath(), "{ \"titre\": \"pas fini");
        assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
    }

    @Test
    void fichierVide() throws Exception {
        File fichier = dossier.resolve("vide.qrproj").toFile();
        Files.writeString(fichier.toPath(), "");
        assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
    }

    @Test
    void profilOuvertCommeProjet() throws Exception {
        File fichier = dossier.resolve("profil.json").toFile();
        sauvegarde.enregistrer(new ProfilPdf(), fichier);
        QrCodeException e = assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
        assertEquals("Ce fichier n'est pas un projet.", e.getMessage());
    }

    @Test
    void projetOuvertCommeProfil() throws Exception {
        File fichier = dossier.resolve("projet.qrproj").toFile();
        sauvegarde.enregistrer(projetExemple(), fichier);
        QrCodeException e = assertThrows(QrCodeException.class, () -> sauvegarde.chargerProfil(fichier));
        assertEquals("Ce fichier n'est pas un profil.", e.getMessage());
    }

    @Test
    void valeurInvalideDansLeFichier() throws Exception {
        File fichier = dossier.resolve("bidouille.qrproj").toFile();
        sauvegarde.enregistrer(projetExemple(), fichier);
        String json = Files.readString(fichier.toPath()).replace("#003399", "bleu");
        Files.writeString(fichier.toPath(), json);
        assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
    }

    @Test
    void typeInconnuDansLeFichier() throws Exception {
        File fichier = dossier.resolve("bidouille.qrproj").toFile();
        sauvegarde.enregistrer(projetExemple(), fichier);
        String json = Files.readString(fichier.toPath()).replace("\"EMAIL\"", "\"FAX\"");
        Files.writeString(fichier.toPath(), json);
        assertThrows(QrCodeException.class, () -> sauvegarde.chargerProjet(fichier));
    }

    @Test
    void enregistrerDansDossierInexistant() {
        File fichier = dossier.resolve("existe/pas/test.qrproj").toFile();
        assertThrows(QrCodeException.class, () -> sauvegarde.enregistrer(projetExemple(), fichier));
    }
}
