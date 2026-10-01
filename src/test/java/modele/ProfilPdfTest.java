package modele;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.io.File;

import org.junit.jupiter.api.Test;

class ProfilPdfTest {

    @Test
    void profilParDefautValide() {
        assertDoesNotThrow(() -> new ProfilPdf().valider());
    }

    @Test
    void couleurAllerRetour() {
        ProfilPdf profil = new ProfilPdf();
        profil.setCouleurTexte(new Color(18, 52, 86));
        assertEquals(new Color(18, 52, 86), profil.getCouleurTexte());
    }

    @Test
    void tailleHorsLimites() {
        ProfilPdf profil = new ProfilPdf();
        profil.setTailleTitre(ProfilPdf.TAILLE_MAX + 1);
        assertThrows(QrCodeException.class, profil::valider);
        profil.setTailleTitre(20);
        profil.setTailleTexte(ProfilPdf.TAILLE_MIN - 1);
        assertThrows(QrCodeException.class, profil::valider);
    }

    @Test
    void policePersonnaliseeSansFichier() {
        ProfilPdf profil = new ProfilPdf();
        profil.setPolice(PolicePdf.PERSONNALISEE);
        assertThrows(QrCodeException.class, profil::valider);
        profil.setFichierPolice(new File("police.ttf"));
        assertDoesNotThrow(profil::valider);
    }

    @Test
    void memeStyle() {
        ProfilPdf a = new ProfilPdf();
        ProfilPdf b = new ProfilPdf();
        b.setNom("Autre nom");
        assertTrue(a.memeStyle(b));
        b.setCouleurQr(Color.BLUE);
        assertFalse(a.memeStyle(b));
    }

    @Test
    void copieIndependante() {
        ProfilPdf original = new ProfilPdf();
        ProfilPdf copie = new ProfilPdf(original);
        copie.setCouleurQr(Color.BLUE);
        assertEquals(Color.BLACK, original.getCouleurQr());
    }
}
