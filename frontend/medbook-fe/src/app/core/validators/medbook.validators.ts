import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import CodiceFiscale from 'codice-fiscale-js';
import { GENDER_CF_MAP } from '../constants/ui.constants';

/**
 * Validatori centralizzati per tutti i form dell'applicazione.
 *
 * Ogni validatore restituisce null se valido, un oggetto errore se non valido.
 * I codici errore corrispondono a quelli gestiti da FieldErrorComponent.
 * Mai duplicare logica di validazione nei componenti — usare sempre questa classe.
 */
export class MedBookValidators {

  /*
   * Email con regex più restrittiva di quella Angular di default.
   * Richiede TLD di almeno 2 caratteri, blocca caratteri non standard.
   */
  static email(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const ok = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/.test(control.value);
      return ok ? null : { emailInvalid: true };
    };
  }

  /*
   * Telefono italiano senza prefisso paese.
   * Accetta solo cifre, spazi e trattini. Lunghezza: 9-10 cifre.
   * Esempi validi: "3331234567", "333 123 4567", "333-123-4567"
   */
  static telefono(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const cifre = control.value.replace(/[\s\-]/g, '');
      if (!/^\d+$/.test(cifre)) return { telefonoNonValido: true };
      if (cifre.length < 9 || cifre.length > 10) return { telefonoLunghezza: true };
      return null;
    };
  }

  /*
   * Codice fiscale: valida solo il formato (16 caratteri alfanumerici).
   * Nessuna verifica di concordanza con i dati anagrafici per ora.
   */
  static codiceFiscale(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const ok = /^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$/i.test(control.value);
      return ok ? null : { codiceFiscaleInvalid: true };
    };
  }

  /*
   * Password sicura — regole:
   * - Minimo 8 caratteri
   * - Almeno 1 lettera maiuscola (A-Z)
   * - Almeno 1 cifra (0-9)
   * - Almeno 1 carattere speciale tra: @#$%^&*!?._-
   */
  static password(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const val = control.value as string;
      if (val.length < 8) return { passwordTroppoCorta: true };
      if (!/[A-Z]/.test(val)) return { passwordSenzaMaiuscola: true };
      if (!/\d/.test(val)) return { passwordSenzaNumero: true };
      if (!/[@#$%^&*!?._\-]/.test(val)) return { passwordSenzaSpeciale: true };
      return null;
    };
  }

  /*
   * Verifica che i due campi password coincidano.
   * Va applicato al FormGroup, non al singolo control.
   */
  static passwordCoincidenti(campo1: string, campo2: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const p1 = group.get(campo1)?.value;
      const p2 = group.get(campo2)?.value;
      if (!p1 || !p2) return null;
      return p1 === p2 ? null : { passwordNonCoincidono: true };
    };
  }

  /*
   * Validazione di concordanza tra codice fiscale e dati anagrafici.
   * Usa la libreria codice-fiscale-js per generare il CF atteso e confrontarlo
   * con quello inserito dall'utente.
   *
   * Va applicato al FormGroup, non al singolo control.
   * Richiede i campi: fiscalCode, firstName, lastName, dateOfBirth, gender, comuneNascita.
   * La validazione viene eseguita solo se tutti i dati necessari sono presenti.
   * Per i nati all'estero (provinciaNascita = 'EE') la concordanza viene saltata
   * perché la libreria gestisce solo i comuni italiani.
   * Gestisce anche i casi di omocodia (più CF validi per la stessa persona).
   */
  static codiceFiscaleConcordanza(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const cf = group.get('fiscalCode')?.value;
      const firstName = group.get('firstName')?.value;
      const lastName = group.get('lastName')?.value;
      const dateOfBirth = group.get('dateOfBirth')?.value;
      const gender = group.get('gender')?.value;
      const comuneNascita = group.get('comuneNascita')?.value;
      const provinciaNascita = group.get('provinciaNascita')?.value;

      // Se manca uno dei campi necessari, non validare
      if (!cf || !firstName || !lastName || !dateOfBirth || !gender || !comuneNascita) {
        return null;
      }

      // Per i nati all'estero la libreria non ha i codici catastali: skip
      if (provinciaNascita === 'EE') {
        return null;
      }

      // Genere non mappabile a M/F per il CF: skip
      const cfGender = GENDER_CF_MAP[gender];
      if (!cfGender) {
        return null;
      }

      try {
        const dob = new Date(dateOfBirth);

        const cfAtteso = CodiceFiscale.compute({
          name: firstName,
          surname: lastName,
          gender: cfGender,
          day: dob.getUTCDate(),
          month: dob.getUTCMonth() + 1,
          year: dob.getUTCFullYear(),
          birthplace: comuneNascita,
          birthplaceProvincia: ''
        });

        // Confronto diretto + omocodie (varianti valide dello stesso CF)
        const upper = cf.toUpperCase();
        if (upper === cfAtteso.toUpperCase()) return null;

        // Verifica se il CF è una delle omocodie valide
        try {
          const omocodie = CodiceFiscale.getOmocodie(cfAtteso);
          if (omocodie.some(o => o.toUpperCase() === upper)) return null;
        } catch { /* omocodia check opzionale */ }

        return { cfNonConcordante: true };
      } catch {
        // Comune non trovato nella libreria o dati insufficienti: skip
        return null;
      }
    };
  }

  /*
   * Vincoli canali di notifica:
   * 1. Almeno un canale deve essere attivo
   * 2. SMS può essere attivo solo se il telefono è compilato
   *
   * Va applicato al FormGroup. Legge i campi:
   * notificationChannels.email, notificationChannels.sms, phone
   */
  static canaleDiNotificaValido(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const emailCh = group.get('notificationChannels.email')?.value;
      const smsCh = group.get('notificationChannels.sms')?.value;
      const phone = group.get('phone')?.value;

      // SMS attivo senza telefono
      if (smsCh && !phone) {
        return { smsRichiedeTelefono: true };
      }

      // Nessun canale attivo
      if (!emailCh && !smsCh) {
        return { nessunCanaleAttivo: true };
      }

      return null;
    };
  }

  /*
   * Data di nascita: non deve essere nel futuro.
   */
  static dataPassata(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return new Date(control.value) < new Date() ? null : { dataFutura: true };
    };
  }
}
