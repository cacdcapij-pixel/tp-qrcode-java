# TP Java – Générateur de QR code

Application Swing en MVC : saisie d'un texte, lien, email ou numéro de téléphone, génération du QR code, export en PDF.

- **modele** : `TypeContenu`, `FormateurContenu`, `GenerateurQrCode` (ZXing), `GenerateurPdf` (iText), `QrCodeException`
- **vue** : `FrmQrCode`
- **controleur** : `Controle` (main)

Le fonctionnement détaillé est dans [RAPPORT.md](RAPPORT.md).

## Commandes

```
mvn test               # tests unitaires
mvn javadoc:javadoc    # documentation -> target/reports/apidocs/index.html
```

Lancer l'application : exécuter `controleur.Controle` depuis l'IDE.
