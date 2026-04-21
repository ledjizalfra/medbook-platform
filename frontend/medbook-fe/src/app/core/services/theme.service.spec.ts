import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';
import { KeycloakService } from '../auth/keycloak.service';

describe('ThemeService', () => {
  let service: ThemeService;
  let kcMock: { hasRole: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    kcMock = { hasRole: vi.fn().mockReturnValue(false) };
    TestBed.configureTestingModule({
      providers: [{ provide: KeycloakService, useValue: kcMock }],
    });
    service = TestBed.inject(ThemeService);
    // Pulisce il classList del body prima di ogni test
    document.body.className = '';
  });

  // -------------------------------------------------------------------------
  // isPublicRoute()
  // -------------------------------------------------------------------------
  describe('isPublicRoute()', () => {
    it('dovrebbe restituire true per la homepage (/)', () => {
      expect(service.isPublicRoute('/')).toBe(true);
    });

    it('dovrebbe restituire true per le pagine pubbliche statiche', () => {
      expect(service.isPublicRoute('/come-funziona')).toBe(true);
      expect(service.isPublicRoute('/servizi')).toBe(true);
      expect(service.isPublicRoute('/chi-siamo')).toBe(true);
      expect(service.isPublicRoute('/contatti')).toBe(true);
    });

    it('dovrebbe restituire true per login e register', () => {
      expect(service.isPublicRoute('/login')).toBe(true);
      expect(service.isPublicRoute('/register')).toBe(true);
    });

    it('dovrebbe restituire false per le rotte protette', () => {
      expect(service.isPublicRoute('/dashboard')).toBe(false);
      expect(service.isPublicRoute('/patients')).toBe(false);
      expect(service.isPublicRoute('/appointments')).toBe(false);
      expect(service.isPublicRoute('/profile')).toBe(false);
    });

    it('dovrebbe ignorare i segmenti successivi al primo (es. /patients/123/edit)', () => {
      expect(service.isPublicRoute('/patients/123/edit')).toBe(false);
    });
  });

  // -------------------------------------------------------------------------
  // apply()
  // -------------------------------------------------------------------------
  describe('apply()', () => {
    it('dovrebbe applicare theme-public per le rotte pubbliche', () => {
      service.apply('/login');
      expect(document.body.classList.contains('theme-public')).toBe(true);
    });

    it('dovrebbe applicare theme-patient per il ruolo PATIENT', () => {
      kcMock.hasRole.mockImplementation((r: string) => r === 'PATIENT');
      service.apply('/dashboard');
      expect(document.body.classList.contains('theme-patient')).toBe(true);
    });

    it('dovrebbe applicare theme-doctor per il ruolo DOCTOR', () => {
      kcMock.hasRole.mockImplementation((r: string) => r === 'DOCTOR');
      service.apply('/dashboard');
      expect(document.body.classList.contains('theme-doctor')).toBe(true);
    });

    it('dovrebbe applicare theme-admin per il ruolo ADMIN', () => {
      kcMock.hasRole.mockImplementation((r: string) => r === 'ADMIN');
      service.apply('/dashboard');
      expect(document.body.classList.contains('theme-admin')).toBe(true);
    });

    it('dovrebbe rimuovere il tema precedente prima di applicarne uno nuovo', () => {
      document.body.classList.add('theme-patient');
      kcMock.hasRole.mockImplementation((r: string) => r === 'ADMIN');
      service.apply('/dashboard');
      expect(document.body.classList.contains('theme-patient')).toBe(false);
      expect(document.body.classList.contains('theme-admin')).toBe(true);
    });

    it('non dovrebbe applicare alcun tema se il ruolo è sconosciuto', () => {
      kcMock.hasRole.mockReturnValue(false);
      service.apply('/dashboard');
      const themes = ['theme-public','theme-patient','theme-doctor','theme-receptionist','theme-admin'];
      themes.forEach(t => expect(document.body.classList.contains(t)).toBe(false));
    });
  });
});
