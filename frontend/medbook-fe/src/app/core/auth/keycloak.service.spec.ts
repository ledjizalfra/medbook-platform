import { TestBed } from '@angular/core/testing';
import { KeycloakService } from './keycloak.service';

// Mock dell'intera libreria keycloak-js: sostituisce il costruttore con una spy factory
vi.mock('keycloak-js', () => {
  const MockKeycloak = vi.fn().mockImplementation(() => ({
    init:             vi.fn().mockResolvedValue(true),
    login:            vi.fn().mockResolvedValue(undefined),
    logout:           vi.fn().mockResolvedValue(undefined),
    updateToken:      vi.fn().mockResolvedValue(true),
    hasRealmRole:     vi.fn().mockReturnValue(false),
    hasResourceRole:  vi.fn().mockReturnValue(false),
    authenticated:    false,
    token:            undefined,
    tokenParsed:      undefined,
  }));
  return { default: MockKeycloak };
});

describe('KeycloakService', () => {
  let service: KeycloakService;
  // Riferimento diretto all'istanza mock creata dal costruttore
  let keycloakMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KeycloakService);
    // Recupera l'istanza mock dalla proprietà privata per poterne modificare i valori
    keycloakMock = (service as unknown as { keycloak: typeof keycloakMock }).keycloak;
  });

  describe('isLoggedIn()', () => {
    it('dovrebbe restituire true se l\'utente è autenticato', () => {
      keycloakMock.authenticated = true;
      expect(service.isLoggedIn()).toBe(true);
    });

    it('dovrebbe restituire false se l\'utente non è autenticato', () => {
      keycloakMock.authenticated = false;
      expect(service.isLoggedIn()).toBe(false);
    });

    it('dovrebbe restituire false se authenticated è undefined', () => {
      keycloakMock.authenticated = undefined;
      expect(service.isLoggedIn()).toBe(false);
    });
  });

  describe('getToken()', () => {
    it('dovrebbe restituire il token JWT se presente', () => {
      keycloakMock.token = 'abc.def.ghi';
      expect(service.getToken()).toBe('abc.def.ghi');
    });

    it('dovrebbe restituire una stringa vuota se il token non è presente', () => {
      keycloakMock.token = undefined;
      expect(service.getToken()).toBe('');
    });
  });

  describe('hasRole()', () => {
    it('dovrebbe aggiungere il prefisso ROLE_ se assente prima del controllo', () => {
      service.hasRole('ADMIN');
      expect(keycloakMock.hasRealmRole).toHaveBeenCalledWith('ROLE_ADMIN');
    });

    it('non dovrebbe duplicare il prefisso ROLE_ se già presente', () => {
      service.hasRole('ROLE_ADMIN');
      expect(keycloakMock.hasRealmRole).toHaveBeenCalledWith('ROLE_ADMIN');
    });

    it('dovrebbe restituire true se il ruolo è presente nel realm', () => {
      keycloakMock.hasRealmRole.mockReturnValue(true);
      expect(service.hasRole('PATIENT')).toBe(true);
    });

    it('dovrebbe controllare i client roles se il realm role non è trovato', () => {
      keycloakMock.hasRealmRole.mockReturnValue(false);
      keycloakMock.hasResourceRole.mockReturnValue(true);
      expect(service.hasRole('DOCTOR')).toBe(true);
    });

    it('dovrebbe restituire false se il ruolo non è né realm né client role', () => {
      keycloakMock.hasRealmRole.mockReturnValue(false);
      keycloakMock.hasResourceRole.mockReturnValue(false);
      expect(service.hasRole('UNKNOWN')).toBe(false);
    });
  });

  describe('getUsername()', () => {
    it('dovrebbe restituire il preferred_username dal token', () => {
      keycloakMock.tokenParsed = { preferred_username: 'mario.rossi' };
      expect(service.getUsername()).toBe('mario.rossi');
    });

    it('dovrebbe restituire una stringa vuota se il token non è presente', () => {
      keycloakMock.tokenParsed = undefined;
      expect(service.getUsername()).toBe('');
    });
  });

  describe('getTokenParsed()', () => {
    it('dovrebbe restituire il payload decodificato del token', () => {
      const payload = { sub: 'user-123', realm_access: { roles: ['ROLE_PATIENT'] } };
      keycloakMock.tokenParsed = payload;
      expect(service.getTokenParsed()).toEqual(payload);
    });

    it('dovrebbe restituire undefined se il token non è presente', () => {
      keycloakMock.tokenParsed = undefined;
      expect(service.getTokenParsed()).toBeUndefined();
    });
  });

  describe('updateToken()', () => {
    it('dovrebbe chiamare updateToken(30) per rinnovare il token prima della scadenza', async () => {
      await service.updateToken();
      expect(keycloakMock.updateToken).toHaveBeenCalledWith(30);
    });
  });
});
