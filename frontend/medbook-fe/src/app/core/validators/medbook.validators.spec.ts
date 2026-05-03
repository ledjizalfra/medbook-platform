import { FormBuilder, FormGroup } from '@angular/forms';
import { MedBookValidators } from './medbook.validators';

describe('MedBookValidators', () => {
  const fb = new FormBuilder();

  // =========================================================================
  // email()
  // =========================================================================
  describe('email()', () => {
    const validator = MedBookValidators.email();

    it('null se campo vuoto', () => {
      expect(validator({ value: '' } as any)).toBeNull();
    });

    it('null se email valida', () => {
      expect(validator({ value: 'test@example.com' } as any)).toBeNull();
    });

    it('null se email con sotto-dominio', () => {
      expect(validator({ value: 'user@sub.domain.com' } as any)).toBeNull();
    });

    it('errore se manca @', () => {
      expect(validator({ value: 'testexample.com' } as any)).toEqual({ emailInvalid: true });
    });

    it('errore se manca TLD', () => {
      expect(validator({ value: 'test@example' } as any)).toEqual({ emailInvalid: true });
    });

    it('errore se TLD troppo corto (1 char)', () => {
      expect(validator({ value: 'test@example.a' } as any)).toEqual({ emailInvalid: true });
    });

    it('null se TLD lungo', () => {
      expect(validator({ value: 'test@example.info' } as any)).toBeNull();
    });

    it('errore se contiene spazi', () => {
      expect(validator({ value: 'test @example.com' } as any)).toEqual({ emailInvalid: true });
    });
  });

  // =========================================================================
  // telefono()
  // =========================================================================
  describe('telefono()', () => {
    const validator = MedBookValidators.telefono();

    it('null se campo vuoto', () => {
      expect(validator({ value: '' } as any)).toBeNull();
    });

    it('null se 10 cifre senza separatori', () => {
      expect(validator({ value: '3331234567' } as any)).toBeNull();
    });

    it('null se 9 cifre', () => {
      expect(validator({ value: '333123456' } as any)).toBeNull();
    });

    it('null se cifre con spazi', () => {
      expect(validator({ value: '333 123 4567' } as any)).toBeNull();
    });

    it('null se cifre con trattini', () => {
      expect(validator({ value: '333-123-4567' } as any)).toBeNull();
    });

    it('errore se contiene lettere', () => {
      expect(validator({ value: '333abc4567' } as any)).toEqual({ telefonoNonValido: true });
    });

    it('errore se meno di 9 cifre', () => {
      expect(validator({ value: '33312345' } as any)).toEqual({ telefonoLunghezza: true });
    });

    it('errore se piu di 10 cifre', () => {
      expect(validator({ value: '33312345678' } as any)).toEqual({ telefonoLunghezza: true });
    });
  });

  // =========================================================================
  // codiceFiscale()
  // =========================================================================
  describe('codiceFiscale()', () => {
    const validator = MedBookValidators.codiceFiscale();

    it('null se campo vuoto', () => {
      expect(validator({ value: '' } as any)).toBeNull();
    });

    it('null se formato valido (16 caratteri)', () => {
      expect(validator({ value: 'RSSMRA85M01H501Z' } as any)).toBeNull();
    });

    it('null se lowercase (case insensitive)', () => {
      expect(validator({ value: 'rssmra85m01h501z' } as any)).toBeNull();
    });

    it('errore se troppo corto', () => {
      expect(validator({ value: 'RSSMRA85M01' } as any)).toEqual({ codiceFiscaleInvalid: true });
    });

    it('errore se formato sbagliato', () => {
      expect(validator({ value: '1234567890123456' } as any)).toEqual({ codiceFiscaleInvalid: true });
    });
  });

  // =========================================================================
  // password()
  // =========================================================================
  describe('password()', () => {
    const validator = MedBookValidators.password();

    it('null se campo vuoto', () => {
      expect(validator({ value: '' } as any)).toBeNull();
    });

    it('null se password valida', () => {
      expect(validator({ value: 'Test1234!' } as any)).toBeNull();
    });

    it('errore se meno di 8 caratteri', () => {
      expect(validator({ value: 'Te1!' } as any)).toEqual({ passwordTroppoCorta: true });
    });

    it('errore se senza maiuscola', () => {
      expect(validator({ value: 'test1234!' } as any)).toEqual({ passwordSenzaMaiuscola: true });
    });

    it('errore se senza numero', () => {
      expect(validator({ value: 'Testtest!' } as any)).toEqual({ passwordSenzaNumero: true });
    });

    it('errore se senza carattere speciale', () => {
      expect(validator({ value: 'Test12345' } as any)).toEqual({ passwordSenzaSpeciale: true });
    });

    it('null con tutti i caratteri speciali ammessi', () => {
      const specials = ['@', '#', '$', '%', '^', '&', '*', '!', '?', '.', '_', '-'];
      specials.forEach(s => {
        expect(validator({ value: `Test1234${s}` } as any)).toBeNull();
      });
    });
  });

  // =========================================================================
  // passwordCoincidenti()
  // =========================================================================
  describe('passwordCoincidenti()', () => {
    it('null se le password coincidono', () => {
      const group = fb.group({
        password: 'Test1234!',
        confirmPassword: 'Test1234!'
      }, { validators: [MedBookValidators.passwordCoincidenti('password', 'confirmPassword')] });
      expect(group.errors).toBeNull();
    });

    it('errore se le password non coincidono', () => {
      const group = fb.group({
        password: 'Test1234!',
        confirmPassword: 'DiversA1!'
      }, { validators: [MedBookValidators.passwordCoincidenti('password', 'confirmPassword')] });
      expect(group.errors).toEqual({ passwordNonCoincidono: true });
    });

    it('null se uno dei due campi e vuoto', () => {
      const group = fb.group({
        password: 'Test1234!',
        confirmPassword: ''
      }, { validators: [MedBookValidators.passwordCoincidenti('password', 'confirmPassword')] });
      expect(group.errors).toBeNull();
    });
  });

  // =========================================================================
  // canaleDiNotificaValido()
  // =========================================================================
  describe('canaleDiNotificaValido()', () => {
    const buildGroup = (email: boolean, sms: boolean, phone: string): FormGroup =>
      fb.group({
        notificationChannels: fb.group({ email, sms }),
        phone
      }, { validators: [MedBookValidators.canaleDiNotificaValido()] });

    it('null se email attivo', () => {
      expect(buildGroup(true, false, '').errors).toBeNull();
    });

    it('null se sms attivo con telefono', () => {
      expect(buildGroup(false, true, '3331234567').errors).toBeNull();
    });

    it('null se entrambi attivi con telefono', () => {
      expect(buildGroup(true, true, '3331234567').errors).toBeNull();
    });

    it('errore se nessun canale attivo', () => {
      expect(buildGroup(false, false, '').errors).toEqual({ nessunCanaleAttivo: true });
    });

    it('errore se sms attivo senza telefono', () => {
      expect(buildGroup(false, true, '').errors).toEqual({ smsRichiedeTelefono: true });
    });
  });

  // =========================================================================
  // dataPassata()
  // =========================================================================
  describe('dataPassata()', () => {
    const validator = MedBookValidators.dataPassata();

    it('null se campo vuoto', () => {
      expect(validator({ value: null } as any)).toBeNull();
    });

    it('null se data nel passato', () => {
      expect(validator({ value: '2000-01-01' } as any)).toBeNull();
    });

    it('errore se data nel futuro', () => {
      const futuro = new Date();
      futuro.setFullYear(futuro.getFullYear() + 1);
      expect(validator({ value: futuro.toISOString() } as any)).toEqual({ dataFutura: true });
    });
  });
});
