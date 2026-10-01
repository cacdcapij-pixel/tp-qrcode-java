package modele;

/**
 * Erreur métier du modèle, attrapée par le contrôleur et affichée par la vue.
 */
public class QrCodeException extends Exception {

    public QrCodeException(String message) {
        super(message);
    }

    public QrCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
