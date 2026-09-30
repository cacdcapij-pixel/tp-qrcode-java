# TP Java – Générateur de QR code

Application Swing en MVC : saisie d'un texte ou d'un lien, génération du QR code, export en PDF.

- **modele** : `GenerateurQrCode` (ZXing), `GenerateurPdf` (iText), `QrCodeException`
- **vue** : `FrmQrCode`
- **controleur** : `Controle` (main)

## Lancer

```
mvn test          # tests unitaires
```

Puis exécuter `controleur.Controle` depuis l'IDE.
