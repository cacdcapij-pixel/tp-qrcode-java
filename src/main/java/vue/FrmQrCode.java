wdpackage vue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import controleur.Controle;

public class FrmQrCode extends JFrame {

    private final Controle controle;

    private JTextArea txtTexte;
    private JLabel lblApercu;
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

        // saisie
        txtTexte = new JTextArea(3, 30);
        txtTexte.setLineWrap(true);
        txtTexte.setWrapStyleWord(true);
        JPanel pnlSaisie = new JPanel(new BorderLayout(5, 5));
        pnlSaisie.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        pnlSaisie.add(new JLabel("Texte ou lien :"), BorderLayout.NORTH);
        pnlSaisie.add(new JScrollPane(txtTexte), BorderLayout.CENTER);
        add(pnlSaisie, BorderLayout.NORTH);

        // aperçu
        lblApercu = new JLabel("Aucun QR code", SwingConstants.CENTER);
        lblApercu.setPreferredSize(new Dimension(320, 320));
        add(lblApercu, BorderLayout.CENTER);

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

        pack();
        setLocationRelativeTo(null);
    }

    // clic Générer
    private void btnGenerer_clic() {
        controle.demandeFrmQrCodeGenerer(txtTexte.getText());
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
    public void afficheQrCode(BufferedImage image) {
        lblApercu.setText(null);
        lblApercu.setIcon(new ImageIcon(image));
        btnExporter.setEnabled(true);
    }

    public void afficheErreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public void afficheSucces(String message) {
        JOptionPane.showMessageDialog(this, message, "Export", JOptionPane.INFORMATION_MESSAGE);
    }
}
