package modele;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FormateurContenuTest {

    private final FormateurContenu formateur = new FormateurContenu();

    // texte
    @Test
    void texteInchange() throws Exception {
        assertEquals("Bonjour BTS SIO", formateur.formater(TypeContenu.TEXTE, "Bonjour BTS SIO"));
    }

    // liens
    @Test
    void lienAvecHttps() throws Exception {
        assertEquals("https://exemple.fr", formateur.formater(TypeContenu.LIEN, "https://exemple.fr"));
    }

    @Test
    void lienSansHttps() throws Exception {
        assertEquals("https://exemple.fr/page", formateur.formater(TypeContenu.LIEN, "exemple.fr/page"));
    }

    @Test
    void lienHttpGarde() throws Exception {
        assertEquals("http://exemple.fr", formateur.formater(TypeContenu.LIEN, "http://exemple.fr"));
    }

    @Test
    void lienInvalide() {
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.LIEN, "pas un lien"));
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.LIEN, "exemple"));
    }

    // email
    @Test
    void emailValide() throws Exception {
        assertEquals("mailto:eleve@saint-luc.fr", formateur.formater(TypeContenu.EMAIL, " eleve@saint-luc.fr "));
    }

    @Test
    void emailInvalide() {
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.EMAIL, "eleve.saint-luc.fr"));
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.EMAIL, "eleve@"));
    }

    // téléphone
    @Test
    void telephoneNettoye() throws Exception {
        assertEquals("tel:0612345678", formateur.formater(TypeContenu.TELEPHONE, "06 12 34 56 78"));
        assertEquals("tel:+33612345678", formateur.formater(TypeContenu.TELEPHONE, "+33 6.12.34.56.78"));
    }

    @Test
    void telephoneInvalide() {
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.TELEPHONE, "abc"));
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.TELEPHONE, "12"));
    }

    // cas limites
    @Test
    void saisieVide() {
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.TEXTE, ""));
        assertThrows(QrCodeException.class, () -> formateur.formater(TypeContenu.LIEN, "   "));
    }

    @Test
    void typeNull() {
        assertThrows(QrCodeException.class, () -> formateur.formater(null, "test"));
    }
}
