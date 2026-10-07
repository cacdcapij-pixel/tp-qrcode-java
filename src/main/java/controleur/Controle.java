package controleur;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import modele.FormateurContenu;
import modele.GenerateurPdf;
import modele.GenerateurQrCode;
import modele.GestionProfils;
import modele.ProfilPdf;
import modele.Projet;
import modele.QrCodeException;
import modele.Sauvegarde;
import modele.TypeContenu;
import vue.FrmQrCode;

/**
 * Contrôleur : reçoit les demandes de la vue, appelle le modèle et renvoie le résultat à la vue.
 */
public class Controle {

    private FrmQrCode frmQrCode;
    private final FormateurContenu formateurContenu = new FormateurContenu();
    private final GenerateurQrCode generateurQrCode = new GenerateurQrCode();
    private final GenerateurPdf generateurPdf = new GenerateurPdf();
    private final Sauvegarde sauvegarde = new Sauvegarde();
    private final GestionProfils gestionProfils = new GestionProfils(GestionProfils.dossierParDefaut());

    // contenu du dernier QR généré
    private String contenuCourant;

    public static void main(String[] args) {
        // apparence du système (Windows)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // on garde l'apparence par défaut
        }
        SwingUtilities.invokeLater(Controle::new);
    }

    public Controle() {
        frmQrCode = new FrmQrCode(this);
        frmQrCode.majLstProfils(gestionProfils.lister(), null);
        frmQrCode.setVisible(true);
    }

    // ---------- QR code et PDF ----------

    /** Clic Générer : formate la saisie, génère le QR dans la couleur du profil et l'affiche. */
    public void demandeFrmQrCodeGenerer(TypeContenu type, String saisie, Color couleurQr) {
        try {
            String contenu = formateurContenu.formater(type, saisie);
            BufferedImage image = generateurQrCode.generer(contenu, couleurQr);
            contenuCourant = contenu;
            frmQrCode.afficheQrCode(image, contenu);
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    /**
     * Clic Exporter : crée le PDF du dernier QR généré avec le style et les images du projet.
     * Le travail est fait en arrière-plan pour ne pas figer la fenêtre.
     */
    public void demandeFrmQrCodeExporter(Projet projet, File fichier) {
        if (contenuCourant == null) {
            frmQrCode.afficheErreur("Aucun QR code à exporter.");
            return;
        }
        String contenu = contenuCourant;
        frmQrCode.afficheTravail(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // QR refait avec la couleur actuelle du profil
                BufferedImage image = generateurQrCode.generer(contenu, projet.getProfil().getCouleurQr());
                generateurPdf.exporter(projet, image, contenu, fichier);
                return null;
            }

            // retour dans le thread de la fenêtre
            @Override
            protected void done() {
                frmQrCode.afficheTravail(false);
                try {
                    get();
                    frmQrCode.afficheExportReussi(fichier);
                } catch (ExecutionException e) {
                    // erreur de l'export
                    frmQrCode.afficheErreur(e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ---------- projets ----------

    /** Menu Nouveau : formulaire vide. */
    public void demandeFrmQrCodeNouveauProjet() {
        contenuCourant = null;
        frmQrCode.afficheProjet(new Projet(), null);
    }

    /** Menu Enregistrer : écrit le projet dans le fichier choisi. */
    public void demandeFrmQrCodeEnregistrerProjet(Projet projet, File fichier) {
        try {
            sauvegarde.enregistrer(projet, fichier);
            frmQrCode.afficheProjetEnregistre(fichier);
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    /** Menu Ouvrir : relit le projet, remplit la fenêtre et régénère le QR. */
    public void demandeFrmQrCodeOuvrirProjet(File fichier) {
        try {
            Projet projet = sauvegarde.chargerProjet(fichier);
            contenuCourant = null;
            frmQrCode.afficheProjet(projet, fichier);
            if (!projet.getSaisie().isBlank()) {
                demandeFrmQrCodeGenerer(projet.getType(), projet.getSaisie(), projet.getProfil().getCouleurQr());
            }
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    // ---------- profils ----------

    /** Vrai si un profil porte déjà ce nom (la vue demande alors confirmation). */
    public boolean demandeFrmQrCodeProfilExiste(String nom) {
        return gestionProfils.existe(nom);
    }

    /** Bouton Enregistrer le profil : sauvegarde sous son nom et met à jour la liste. */
    public void demandeFrmQrCodeEnregistrerProfil(ProfilPdf profil) {
        try {
            gestionProfils.enregistrer(profil);
            frmQrCode.majLstProfils(gestionProfils.lister(), profil.getNom());
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    /** Choix dans la liste des profils : null = style par défaut. */
    public void demandeFrmQrCodeChargerProfil(String nom) {
        if (nom == null) {
            frmQrCode.afficheProfil(new ProfilPdf());
            return;
        }
        try {
            frmQrCode.afficheProfil(gestionProfils.charger(nom));
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }

    /** Bouton Supprimer le profil. */
    public void demandeFrmQrCodeSupprimerProfil(String nom) {
        try {
            gestionProfils.supprimer(nom);
            frmQrCode.majLstProfils(gestionProfils.lister(), null);
        } catch (QrCodeException e) {
            frmQrCode.afficheErreur(e.getMessage());
        }
    }
}
