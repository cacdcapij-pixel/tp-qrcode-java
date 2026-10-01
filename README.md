# TP Java – Générateur de QR code

Application Swing en MVC : saisie d'un texte, lien, email ou numéro de téléphone, génération du QR code, export en PDF personnalisé (police, couleurs, images), sauvegarde des projets et des profils de style.

- **modele** : `TypeContenu`, `FormateurContenu`, `GenerateurQrCode` (ZXing), `GenerateurPdf` (iText), `QrCodeException`, `Projet`, `ProfilPdf`, `PolicePdf`, `ImagePdf`, `EmplacementImage`, `AlignementImage`, `Sauvegarde` (Gson), `GestionProfils`
- **vue** : `FrmQrCode`
- **controleur** : `Controle` (main)

Rapports :
- partie 1 : [RAPPORT.md](RAPPORT.md)
- partie 2 (police, couleurs, images, sauvegarde) : [RAPPORT_PARTIE2.md](RAPPORT_PARTIE2.md)

## Commandes

```
mvn test               # tests unitaires
mvn javadoc:javadoc    # documentation -> target/reports/apidocs/index.html
```

Lancer l'application : exécuter `controleur.Controle` depuis l'IDE.

Les profils sont enregistrés dans `~/.qrcode/profils`, les projets là où l'utilisateur le choisit (`.qrproj`).
