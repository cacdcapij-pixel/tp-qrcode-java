package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import controleur.Controle;
import modele.AlignementImage;
import modele.EmplacementImage;
import modele.ImagePdf;
import modele.PolicePdf;
import modele.ProfilPdf;
import modele.Projet;
import modele.TypeContenu;

/**
 * Fenêtre principale : onglets Contenu / Style / Images, aperçu du QR code, export PDF,
 * sauvegarde des projets et des profils.
 */
public class FrmQrCode extends JFrame {

    private static final String TITRE_FENETRE = "Générateur de QR code";
    private static final String PROFIL_DEFAUT = "Par défaut";
    private static final String EXT_PROJET = "qrproj";

    private final Controle controle;

    // fichier du projet ouvert (null si jamais enregistré)
    private File fichierProjet;
    // dernier dossier utilisé dans les sélecteurs de fichiers
    private File dernierDossier;
    // vrai quand on remplit les champs par code : on ignore leurs événements
    private boolean majEnCours;
    // l'aperçu correspond à la saisie actuelle
    private boolean apercuAJour;
    // export en cours
    private boolean travailEnCours;

    // onglet Contenu
    private JTextField txtTitre;
    private JComboBox<TypeContenu> cboType;
    private JTextArea txtTexte;

    // onglet Style
    private JComboBox<String> cboProfils;
    private JButton btnEnregistrerProfil;
    private JButton btnSupprimerProfil;
    private JComboBox<PolicePdf> cboPolice;
    private JButton btnFichierPolice;
    private JLabel lblFichierPolice;
    private File fichierPolice;
    private JSpinner spnTailleTitre;
    private JCheckBox chkTitreGras;
    private JSpinner spnTailleTexte;
    private JButton btnCouleurTitre;
    private JButton btnCouleurTexte;
    private JButton btnCouleurQr;
    private Color couleurTitre;
    private Color couleurTexte;
    private Color couleurQr;

    // onglet Images
    private final DefaultListModel<ImagePdf> contenuLstImages = new DefaultListModel<>();
    private JList<ImagePdf> lstImages;
    private JButton btnAjouterImage;
    private JButton btnRetirerImage;
    private JComboBox<EmplacementImage> cboEmplacement;
    private JComboBox<AlignementImage> cboAlignement;
    private JSpinner spnLargeur;

    // aperçu et boutons
    private JLabel lblApercu;
    private JLabel lblContenu;
    private JButton btnGenerer;
    private JButton btnExporter;
    private JMenuItem mnuExporter;
    private JProgressBar barProgression;

    public FrmQrCode(Controle controle) {
        this.controle = controle;
        init();
    }

    private void init() {
        setTitle(TITRE_FENETRE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setJMenuBar(creerMenu());

        // onglets
        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("Contenu", creerOngletContenu());
        onglets.addTab("Style", creerOngletStyle());
        onglets.addTab("Images", creerOngletImages());
        JPanel pnlGauche = new JPanel(new BorderLayout());
        pnlGauche.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        pnlGauche.add(onglets);
        add(pnlGauche, BorderLayout.CENTER);

        // aperçu
        lblApercu = new JLabel("Aucun QR code", SwingConstants.CENTER);
        lblApercu.setPreferredSize(new Dimension(310, 310));
        lblContenu = new JLabel(" ", SwingConstants.CENTER);
        // largeur fixe : un long contenu est coupé avec « … » au lieu d'élargir la fenêtre
        lblContenu.setPreferredSize(new Dimension(310, lblContenu.getPreferredSize().height));
        JPanel pnlApercu = new JPanel(new BorderLayout());
        pnlApercu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 10), BorderFactory.createTitledBorder("Aperçu")));
        pnlApercu.add(lblApercu, BorderLayout.CENTER);
        pnlApercu.add(lblContenu, BorderLayout.SOUTH);
        add(pnlApercu, BorderLayout.EAST);

        // boutons + barre de progression
        btnGenerer = new JButton("Générer");
        btnExporter = new JButton("Exporter en PDF");
        JPanel pnlBoutons = new JPanel(new FlowLayout());
        pnlBoutons.add(btnGenerer);
        pnlBoutons.add(btnExporter);
        barProgression = new JProgressBar();
        barProgression.setStringPainted(true);
        barProgression.setString("Création du PDF…");
        barProgression.setVisible(false);
        JPanel pnlBas = new JPanel(new BorderLayout());
        pnlBas.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlBas.add(pnlBoutons, BorderLayout.CENTER);
        pnlBas.add(barProgression, BorderLayout.SOUTH);
        add(pnlBas, BorderLayout.SOUTH);

        // événements
        btnGenerer.addActionListener(e -> btnGenerer_clic());
        btnExporter.addActionListener(e -> btnExporter_clic());
        cboType.addActionListener(e -> saisieModifiee());
        txtTexte.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { saisieModifiee(); }
            public void removeUpdate(DocumentEvent e) { saisieModifiee(); }
            public void changedUpdate(DocumentEvent e) { saisieModifiee(); }
        });

        // style et images de départ
        afficheProfil(new ProfilPdf());
        majChampsImage();
        majBoutons();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    // ---------- construction de l'interface ----------

    private JMenuBar creerMenu() {
        JMenu mnuFichier = new JMenu("Fichier");
        mnuFichier.add(itemMenu("Nouveau projet", KeyEvent.VK_N, 0, e -> controle.demandeFrmQrCodeNouveauProjet()));
        mnuFichier.add(itemMenu("Ouvrir un projet…", KeyEvent.VK_O, 0, e -> mnuOuvrir_clic()));
        mnuFichier.add(itemMenu("Enregistrer", KeyEvent.VK_S, 0, e -> mnuEnregistrer_clic(false)));
        mnuFichier.add(itemMenu("Enregistrer sous…", KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK,
                e -> mnuEnregistrer_clic(true)));
        mnuFichier.addSeparator();
        mnuExporter = itemMenu("Exporter en PDF…", KeyEvent.VK_E, 0, e -> btnExporter_clic());
        mnuFichier.add(mnuExporter);
        mnuFichier.addSeparator();
        mnuFichier.add(itemMenu("Quitter", KeyEvent.VK_Q, 0, e -> dispose()));
        JMenuBar barre = new JMenuBar();
        barre.add(mnuFichier);
        return barre;
    }

    // élément de menu avec raccourci Ctrl (+ modificateur)
    private JMenuItem itemMenu(String texte, int touche, int modificateur, ActionListener action) {
        JMenuItem item = new JMenuItem(texte);
        item.setAccelerator(KeyStroke.getKeyStroke(touche, InputEvent.CTRL_DOWN_MASK | modificateur));
        item.addActionListener(action);
        return item;
    }

    private JPanel creerOngletContenu() {
        txtTitre = new JTextField("QR code", 22);
        cboType = new JComboBox<>(TypeContenu.values());
        txtTexte = new JTextArea(6, 22);
        txtTexte.setLineWrap(true);
        txtTexte.setWrapStyleWord(true);
        // même police que les autres champs (sinon police machine à écrire)
        txtTexte.setFont(txtTitre.getFont());

        JPanel p = formulaire();
        ligne(p, 0, "Titre du PDF :", txtTitre, false);
        ligne(p, 1, "Type :", cboType, false);
        ligne(p, 2, "Contenu :", new JScrollPane(txtTexte), true);
        return p;
    }

    private JPanel creerOngletStyle() {
        // profils
        cboProfils = new JComboBox<>();
        // largeur fixe, même vide
        cboProfils.setPrototypeDisplayValue("Nom de profil assez long");
        btnEnregistrerProfil = new JButton("Enregistrer…");
        btnSupprimerProfil = new JButton("Supprimer");
        JPanel pnlProfil = rangee(cboProfils, btnEnregistrerProfil, btnSupprimerProfil);

        // police
        cboPolice = new JComboBox<>(PolicePdf.values());
        btnFichierPolice = new JButton("Choisir un fichier…");
        lblFichierPolice = new JLabel(" ");
        JPanel pnlPolice = rangee(cboPolice, btnFichierPolice);

        spnTailleTitre = new JSpinner(new SpinnerNumberModel(20, ProfilPdf.TAILLE_MIN, ProfilPdf.TAILLE_MAX, 1));
        chkTitreGras = new JCheckBox("Titre en gras");
        spnTailleTexte = new JSpinner(new SpinnerNumberModel(11, ProfilPdf.TAILLE_MIN, ProfilPdf.TAILLE_MAX, 1));
        btnCouleurTitre = new JButton();
        btnCouleurTexte = new JButton();
        btnCouleurQr = new JButton();

        JPanel p = formulaire();
        ligne(p, 0, "Profil :", pnlProfil, false);
        ligne(p, 1, "", new JSeparator(), false);
        ligne(p, 2, "Police :", pnlPolice, false);
        ligne(p, 3, "", lblFichierPolice, false);
        ligne(p, 4, "Taille du titre :", rangee(spnTailleTitre, chkTitreGras), false);
        ligne(p, 5, "Taille du texte :", rangee(spnTailleTexte), false);
        ligne(p, 6, "Couleur du titre :", rangee(btnCouleurTitre), false);
        ligne(p, 7, "Couleur du texte :", rangee(btnCouleurTexte), false);
        ligne(p, 8, "Couleur du QR code :", rangee(btnCouleurQr), false);
        ligne(p, 9, "", new JLabel(), true);

        // événements
        cboProfils.addActionListener(e -> cboProfils_choix());
        btnEnregistrerProfil.addActionListener(e -> btnEnregistrerProfil_clic());
        btnSupprimerProfil.addActionListener(e -> btnSupprimerProfil_clic());
        cboPolice.addActionListener(e -> majChampsPolice());
        btnFichierPolice.addActionListener(e -> btnFichierPolice_clic());
        btnCouleurTitre.addActionListener(e -> btnCouleur_clic(btnCouleurTitre));
        btnCouleurTexte.addActionListener(e -> btnCouleur_clic(btnCouleurTexte));
        btnCouleurQr.addActionListener(e -> btnCouleur_clic(btnCouleurQr));
        return p;
    }

    private JPanel creerOngletImages() {
        lstImages = new JList<>(contenuLstImages);
        lstImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstImages.setVisibleRowCount(5);
        btnAjouterImage = new JButton("Ajouter…");
        btnRetirerImage = new JButton("Retirer");
        cboEmplacement = new JComboBox<>(EmplacementImage.values());
        cboAlignement = new JComboBox<>(AlignementImage.values());
        spnLargeur = new JSpinner(new SpinnerNumberModel(30, ImagePdf.LARGEUR_MIN, ImagePdf.LARGEUR_MAX, 5));

        JPanel p = formulaire();
        ligne(p, 0, "Images :", new JScrollPane(lstImages), true);
        ligne(p, 1, "", rangee(btnAjouterImage, btnRetirerImage), false);
        ligne(p, 2, "Emplacement :", rangee(cboEmplacement), false);
        ligne(p, 3, "Alignement :", rangee(cboAlignement), false);
        ligne(p, 4, "Largeur (% page) :", rangee(spnLargeur), false);

        // événements
        lstImages.addListSelectionListener(e -> majChampsImage());
        btnAjouterImage.addActionListener(e -> btnAjouterImage_clic());
        btnRetirerImage.addActionListener(e -> btnRetirerImage_clic());
        cboEmplacement.addActionListener(e -> imageModifiee());
        cboAlignement.addActionListener(e -> imageModifiee());
        spnLargeur.addChangeListener(e -> imageModifiee());
        return p;
    }

    // panneau libellé / champ
    private JPanel formulaire() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

    // une ligne du formulaire ; etirer = prend la hauteur restante
    private void ligne(JPanel p, int y, String libelle, JComponent champ, boolean etirer) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = etirer ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
        c.gridx = 0;
        p.add(new JLabel(libelle), c);
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = etirer ? 1 : 0;
        c.fill = etirer ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        p.add(champ, c);
    }

    // composants côte à côte, alignés à gauche
    private JPanel rangee(JComponent... composants) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (JComponent c : composants) {
            p.add(c);
        }
        return p;
    }

    // ---------- événements : contenu et export ----------

    // saisie changée : l'aperçu ne correspond plus, on bloque l'export
    private void saisieModifiee() {
        apercuAJour = false;
        majBoutons();
    }

    // clic Générer
    private void btnGenerer_clic() {
        controle.demandeFrmQrCodeGenerer((TypeContenu) cboType.getSelectedItem(), txtTexte.getText(), couleurQr);
    }

    // clic Exporter
    private void btnExporter_clic() {
        File fichier = choisirFichier("Enregistrer le PDF", true, "pdf", "Fichier PDF", "qrcode.pdf");
        if (fichier != null) {
            controle.demandeFrmQrCodeExporter(lireProjet(), fichier);
        }
    }

    // export possible seulement avec un aperçu à jour et rien en cours
    private void majBoutons() {
        btnGenerer.setEnabled(!travailEnCours);
        btnExporter.setEnabled(apercuAJour && !travailEnCours);
        mnuExporter.setEnabled(btnExporter.isEnabled());
    }

    // ---------- événements : projets ----------

    // menu Ouvrir
    private void mnuOuvrir_clic() {
        File fichier = choisirFichier("Ouvrir un projet", false, EXT_PROJET, "Projet QR code", null);
        if (fichier != null) {
            controle.demandeFrmQrCodeOuvrirProjet(fichier);
        }
    }

    // menu Enregistrer / Enregistrer sous
    private void mnuEnregistrer_clic(boolean sous) {
        File fichier = fichierProjet;
        if (sous || fichier == null) {
            fichier = choisirFichier("Enregistrer le projet", true, EXT_PROJET, "Projet QR code", "projet." + EXT_PROJET);
        }
        if (fichier != null) {
            controle.demandeFrmQrCodeEnregistrerProjet(lireProjet(), fichier);
        }
    }

    // ---------- événements : style ----------

    // choix dans la liste des profils
    private void cboProfils_choix() {
        btnSupprimerProfil.setEnabled(cboProfils.getSelectedIndex() > 0);
        if (majEnCours || cboProfils.getSelectedIndex() < 0) {
            return;
        }
        String nom = cboProfils.getSelectedIndex() == 0 ? null : (String) cboProfils.getSelectedItem();
        controle.demandeFrmQrCodeChargerProfil(nom);
    }

    // clic Enregistrer le profil
    private void btnEnregistrerProfil_clic() {
        String propose = cboProfils.getSelectedIndex() > 0 ? (String) cboProfils.getSelectedItem() : "";
        String nom = (String) JOptionPane.showInputDialog(this, "Nom du profil :", "Enregistrer le profil",
                JOptionPane.PLAIN_MESSAGE, null, null, propose);
        // annulé
        if (nom == null) {
            return;
        }
        if (controle.demandeFrmQrCodeProfilExiste(nom)) {
            int rep = JOptionPane.showConfirmDialog(this, "Le profil « " + nom.trim() + " » existe déjà. Le remplacer ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (rep != JOptionPane.YES_OPTION) {
                return;
            }
        }
        ProfilPdf profil = lireProfil();
        profil.setNom(nom);
        controle.demandeFrmQrCodeEnregistrerProfil(profil);
    }

    // clic Supprimer le profil
    private void btnSupprimerProfil_clic() {
        String nom = (String) cboProfils.getSelectedItem();
        int rep = JOptionPane.showConfirmDialog(this, "Supprimer le profil « " + nom + " » ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (rep == JOptionPane.YES_OPTION) {
            controle.demandeFrmQrCodeSupprimerProfil(nom);
        }
    }

    // le bouton fichier ne sert que pour une police personnalisée
    private void majChampsPolice() {
        boolean perso = cboPolice.getSelectedItem() == PolicePdf.PERSONNALISEE;
        btnFichierPolice.setEnabled(perso);
        lblFichierPolice.setText(!perso ? " " : fichierPolice == null ? "Aucun fichier choisi" : fichierPolice.getName());
    }

    // clic Choisir un fichier de police
    private void btnFichierPolice_clic() {
        JFileChooser choix = new JFileChooser(dernierDossier);
        choix.setDialogTitle("Choisir une police");
        choix.setFileFilter(new FileNameExtensionFilter("Police TrueType / OpenType", "ttf", "otf"));
        if (choix.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fichierPolice = choix.getSelectedFile();
            dernierDossier = fichierPolice.getParentFile();
            majChampsPolice();
        }
    }

    // clic sur un bouton de couleur
    private void btnCouleur_clic(JButton bouton) {
        Color actuelle = bouton == btnCouleurTitre ? couleurTitre : bouton == btnCouleurTexte ? couleurTexte : couleurQr;
        Color choisie = JColorChooser.showDialog(this, "Choisir une couleur", actuelle);
        // annulé
        if (choisie == null) {
            return;
        }
        majCouleur(bouton, choisie);
        // aperçu refait dans la nouvelle couleur
        if (bouton == btnCouleurQr && apercuAJour) {
            btnGenerer_clic();
        }
    }

    // mémorise la couleur et l'affiche sur le bouton
    private void majCouleur(JButton bouton, Color c) {
        if (bouton == btnCouleurTitre) {
            couleurTitre = c;
        } else if (bouton == btnCouleurTexte) {
            couleurTexte = c;
        } else {
            couleurQr = c;
        }
        BufferedImage carre = new BufferedImage(36, 14, BufferedImage.TYPE_INT_RGB);
        Graphics g = carre.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, 36, 14);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, 35, 13);
        g.dispose();
        bouton.setIcon(new ImageIcon(carre));
        bouton.setText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
    }

    // ---------- événements : images ----------

    // clic Ajouter des images
    private void btnAjouterImage_clic() {
        JFileChooser choix = new JFileChooser(dernierDossier);
        choix.setDialogTitle("Ajouter des images");
        choix.setMultiSelectionEnabled(true);
        choix.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, gif, bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (choix.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        for (File f : choix.getSelectedFiles()) {
            contenuLstImages.addElement(new ImagePdf(f));
            dernierDossier = f.getParentFile();
        }
        lstImages.setSelectedIndex(contenuLstImages.size() - 1);
    }

    // clic Retirer
    private void btnRetirerImage_clic() {
        int indice = lstImages.getSelectedIndex();
        if (indice >= 0) {
            contenuLstImages.remove(indice);
            lstImages.setSelectedIndex(Math.min(indice, contenuLstImages.size() - 1));
        }
    }

    // sélection changée : remplit les réglages de l'image choisie
    private void majChampsImage() {
        ImagePdf image = lstImages.getSelectedValue();
        boolean choisie = image != null;
        btnRetirerImage.setEnabled(choisie);
        cboEmplacement.setEnabled(choisie);
        cboAlignement.setEnabled(choisie);
        spnLargeur.setEnabled(choisie);
        if (choisie) {
            majEnCours = true;
            cboEmplacement.setSelectedItem(image.getEmplacement());
            cboAlignement.setSelectedItem(image.getAlignement());
            spnLargeur.setValue(image.getLargeur());
            majEnCours = false;
        }
    }

    // réglage changé : appliqué à l'image choisie
    private void imageModifiee() {
        ImagePdf image = lstImages.getSelectedValue();
        if (majEnCours || image == null) {
            return;
        }
        image.setEmplacement((EmplacementImage) cboEmplacement.getSelectedItem());
        image.setAlignement((AlignementImage) cboAlignement.getSelectedItem());
        image.setLargeur((Integer) spnLargeur.getValue());
        lstImages.repaint();
    }

    // ---------- lecture des champs ----------

    /** Style actuellement réglé dans l'onglet Style. */
    private ProfilPdf lireProfil() {
        ProfilPdf profil = new ProfilPdf();
        profil.setNom(cboProfils.getSelectedIndex() > 0 ? (String) cboProfils.getSelectedItem() : "");
        profil.setPolice((PolicePdf) cboPolice.getSelectedItem());
        profil.setFichierPolice(fichierPolice);
        profil.setTailleTitre((Integer) spnTailleTitre.getValue());
        profil.setTitreGras(chkTitreGras.isSelected());
        profil.setTailleTexte((Integer) spnTailleTexte.getValue());
        profil.setCouleurTitre(couleurTitre);
        profil.setCouleurTexte(couleurTexte);
        profil.setCouleurQr(couleurQr);
        return profil;
    }

    /** Projet complet tel qu'il est dans la fenêtre (copies : la vue peut continuer à changer). */
    private Projet lireProjet() {
        Projet projet = new Projet();
        projet.setTitre(txtTitre.getText());
        projet.setType((TypeContenu) cboType.getSelectedItem());
        projet.setSaisie(txtTexte.getText());
        projet.setProfil(lireProfil());
        for (int k = 0; k < contenuLstImages.size(); k++) {
            projet.getImages().add(new ImagePdf(contenuLstImages.get(k)));
        }
        return projet;
    }

    // sélecteur de fichier ; enregistrer = ajoute l'extension et demande avant d'écraser
    private File choisirFichier(String titre, boolean enregistrer, String extension, String description, String nomPropose) {
        JFileChooser choix = new JFileChooser(dernierDossier);
        choix.setDialogTitle(titre);
        choix.setFileFilter(new FileNameExtensionFilter(description + " (." + extension + ")", extension));
        if (nomPropose != null) {
            choix.setSelectedFile(new File(dernierDossier, nomPropose));
        }
        int rep = enregistrer ? choix.showSaveDialog(this) : choix.showOpenDialog(this);
        // annulé
        if (rep != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File fichier = choix.getSelectedFile();
        dernierDossier = fichier.getParentFile();
        if (!enregistrer) {
            return fichier;
        }

        // ajoute l'extension si oubliée
        if (!fichier.getName().toLowerCase().endsWith("." + extension)) {
            fichier = new File(fichier.getParentFile(), fichier.getName() + "." + extension);
        }
        // fichier déjà existant
        if (fichier.exists()) {
            int ecraser = JOptionPane.showConfirmDialog(this, "« " + fichier.getName() + " » existe déjà. Le remplacer ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (ecraser != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return fichier;
    }

    // ---------- appelés par le contrôleur ----------

    /** Affiche l'aperçu du QR et son contenu, active l'export. */
    public void afficheQrCode(BufferedImage image, String contenu) {
        lblApercu.setText(null);
        lblApercu.setIcon(new ImageIcon(image));
        lblContenu.setText(contenu);
        // texte complet au survol
        lblContenu.setToolTipText(contenu);
        apercuAJour = true;
        majBoutons();
    }

    /** Affiche ou cache la barre de progression et bloque les boutons pendant l'export. */
    public void afficheTravail(boolean enCours) {
        travailEnCours = enCours;
        barProgression.setIndeterminate(enCours);
        barProgression.setVisible(enCours);
        majBoutons();
    }

    /** Export terminé : propose d'ouvrir le PDF. */
    public void afficheExportReussi(File fichier) {
        int rep = JOptionPane.showConfirmDialog(this, "PDF enregistré :\n" + fichier.getAbsolutePath()
                + "\n\nL'ouvrir maintenant ?", "Export", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (rep == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(fichier);
            } catch (IOException | UnsupportedOperationException e) {
                afficheErreur("Impossible d'ouvrir le PDF : aucun lecteur PDF trouvé.");
            }
        }
    }

    /** Remplit toute la fenêtre avec un projet (nouveau ou ouvert). */
    public void afficheProjet(Projet projet, File fichier) {
        txtTitre.setText(projet.getTitre());
        cboType.setSelectedItem(projet.getType());
        txtTexte.setText(projet.getSaisie());
        afficheProfil(projet.getProfil());
        contenuLstImages.clear();
        for (ImagePdf image : projet.getImages()) {
            contenuLstImages.addElement(image);
        }
        majChampsImage();

        // aperçu vidé
        lblApercu.setIcon(null);
        lblApercu.setText("Aucun QR code");
        lblContenu.setText(" ");
        lblContenu.setToolTipText(null);
        apercuAJour = false;
        majBoutons();

        fichierProjet = fichier;
        majTitreFenetre();
    }

    /** Projet enregistré : retient son fichier pour « Enregistrer ». */
    public void afficheProjetEnregistre(File fichier) {
        fichierProjet = fichier;
        majTitreFenetre();
    }

    // nom du projet dans la barre de titre
    private void majTitreFenetre() {
        setTitle(fichierProjet == null ? TITRE_FENETRE : TITRE_FENETRE + " – " + fichierProjet.getName());
    }

    /** Remplit l'onglet Style avec un profil. */
    public void afficheProfil(ProfilPdf profil) {
        majEnCours = true;
        cboPolice.setSelectedItem(profil.getPolice());
        fichierPolice = profil.getFichierPolice();
        majChampsPolice();
        spnTailleTitre.setValue(profil.getTailleTitre());
        chkTitreGras.setSelected(profil.isTitreGras());
        spnTailleTexte.setValue(profil.getTailleTexte());
        majCouleur(btnCouleurTitre, profil.getCouleurTitre());
        majCouleur(btnCouleurTexte, profil.getCouleurTexte());
        majCouleur(btnCouleurQr, profil.getCouleurQr());
        selectionnerProfil(profil);
        majEnCours = false;

        // la couleur du QR a pu changer
        if (apercuAJour) {
            btnGenerer_clic();
        }
    }

    /**
     * Recharge la liste des profils enregistrés.
     * @param aSelectionner profil à sélectionner (celui qu'on vient d'enregistrer), ou null
     */
    public void majLstProfils(List<String> noms, String aSelectionner) {
        majEnCours = true;
        cboProfils.removeAllItems();
        cboProfils.addItem(PROFIL_DEFAUT);
        for (String nom : noms) {
            cboProfils.addItem(nom);
        }
        ProfilPdf actuel = lireProfil();
        actuel.setNom(aSelectionner);
        selectionnerProfil(actuel);
        majEnCours = false;
        btnSupprimerProfil.setEnabled(cboProfils.getSelectedIndex() > 0);
    }

    // sélection dans la liste sans recharger : le profil par son nom,
    // sinon « Par défaut » si c'est le style par défaut, sinon rien (style non enregistré)
    private void selectionnerProfil(ProfilPdf profil) {
        // liste pas encore remplie (ouverture de la fenêtre)
        if (cboProfils.getItemCount() == 0) {
            return;
        }
        int indice = -1;
        for (int k = 1; k < cboProfils.getItemCount(); k++) {
            if (cboProfils.getItemAt(k).equals(profil.getNom())) {
                indice = k;
            }
        }
        if (indice < 0 && profil.memeStyle(new ProfilPdf())) {
            indice = 0;
        }
        cboProfils.setSelectedIndex(indice);
    }

    /** Affiche un message d'erreur. */
    public void afficheErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
