package vue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import controleur.Controle;
import modele.TypeContenu;

/**
 * Fenêtre principale : saisie, aperçu du QR code et export PDF.
 */
public class FrmQrCode extends JFrame {

    private final Controle controle;

    private JComboBox<TypeContenu> cboType;
    private JTextArea txtTexte;
    private JLabel lblApercu;
    private JLabel lblContenu;
    private JButton btnGenerer;
    private JButton btnExporter;

    public FrmQrCode(Controle controle) {
        this.controle = controle;
        init();
    }

    private void init() {
        setTitle("Générateur de QR code");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // type de contenu
        cboType = new JComboBox<>(TypeContenu.values());
        JPanel pnlType = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlType.add(new JLabel("Type :"));
        pnlType.add(cboType);

        // saisie
        txtTexte = new JTextArea(3, 30);
        txtTexte.setLineWrap(true);
        txtTexte.setWrapStyleWord(true);
        JPanel pnlSaisie = new JPanel(new BorderLayout(5, 5));
        pnlSaisie.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        pnlSaisie.add(pnlType, BorderLayout.NORTH);
        pnlSaisie.add(new JScrollPane(txtTexte), BorderLayout.CENTER);
        add(pnlSaisie, BorderLayout.NORTH);

        // aperçu
        lblApercu = new JLabel("Aucun QR code", SwingConstants.CENTER);
        lblApercu.setPreferredSize(new Dimension(320, 320));
        lblContenu = new JLabel(" ", SwingConstants.CENTER);
        JPanel pnlApercu = new JPanel(new BorderLayout());
        pnlApercu.add(lblApercu, BorderLayout.CENTER);
        pnlApercu.add(lblContenu, BorderLayout.SOUTH);
        add(pnlApercu, BorderLayout.CENTER);

        // boutons
        btnGenerer = new JButton("Générer");
        btnExporter = new JButton("Exporter en PDF");
        btnExporter.setEnabled(false);
        JPanel pnlBoutons = new JPanel(new FlowLayout());
        pnlBoutons.add(btnGenerer);
        pnlBoutons.add(btnExporter);
        add(pnlBoutons, BorderLayout.SOUTH);

        // événements
        btnGenerer.addActionListener(e -> btnGenerer_clic());
        btnExporter.addActionListener(e -> btnExporter_clic());
        cboType.addActionListener(e -> saisieModifiee());
        txtTexte.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { saisieModifiee(); }
            public void removeUpdate(DocumentEvent e) { saisieModifiee(); }
            public void changedUpdate(DocumentEvent e) { saisieModifiee(); }
        });

        pack();
        setLocationRelativeTo(null);
    }

    // saisie changée : l'aperçu ne correspond plus, on bloque l'export
    private void saisieModifiee() {
        btnExporter.setEnabled(false);
    }

    // clic Générer
    private void btnGenerer_clic() {
        controle.demandeFrmQrCodeGenerer((TypeContenu) cboType.getSelectedItem(), txtTexte.getText());
    }

    // clic Exporter
    private void btnExporter_clic() {
        JFileChooser choix = new JFileChooser();
        choix.setDialogTitle("Enregistrer le PDF");
        choix.setFileFilter(new FileNameExtensionFilter("Fichier PDF", "pdf"));
        choix.setSelectedFile(new File("qrcode.pdf"));

        // annulé
        if (choix.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // ajoute .pdf si oublié
        File fichier = choix.getSelectedFile();
        if (!fichier.getName().toLowerCase().endsWith(".pdf")) {
            fichier = new File(fichier.getParentFile(), fichier.getName() + ".pdf");
        }

        // fichier déjà existant
        if (fichier.exists()) {
            int rep = JOptionPane.showConfirmDialog(this, "Le fichier existe déjà. Le remplacer ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (rep != JOptionPane.YES_OPTION) {
                return;
            }
        }

        controle.demandeFrmQrCodeExporter(fichier);
    }

    // appelés par le contrôleur

    /** Affiche l'aperçu du QR et son contenu, active l'export. */
    public void afficheQrCode(BufferedImage image, String contenu) {
        lblApercu.setText(null);
        lblApercu.setIcon(new ImageIcon(image));
        lblContenu.setText(contenu);
        btnExporter.setEnabled(true);
    }

    /** Affiche un message d'erreur. */
    public void afficheErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    /** Affiche un message de confirmation. */
    public void afficheSucces(String message) {
        JOptionPane.showMessageDialog(this, message, "Export", JOptionPane.INFORMATION_MESSAGE);
    }
}
