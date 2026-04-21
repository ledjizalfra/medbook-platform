import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { KeycloakService } from './keycloak.service';

describe('authGuard', () => {
  let kcMock: { isLoggedIn: ReturnType<typeof vi.fn>; login: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    kcMock = {
      isLoggedIn: vi.fn(),
      login: vi.fn(),
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: KeycloakService, useValue: kcMock },
        { provide: Router, useValue: {} },
      ],
    });
  });

  it('dovrebbe consentire la navigazione se l\'utente è autenticato', () => {
    kcMock.isLoggedIn.mockReturnValue(true);
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never)
    );
    expect(result).toBe(true);
    expect(kcMock.login).not.toHaveBeenCalled();
  });

  it('dovrebbe bloccare la navigazione e avviare il login se non autenticato', () => {
    kcMock.isLoggedIn.mockReturnValue(false);
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never)
    );
    expect(result).toBe(false);
    expect(kcMock.login).toHaveBeenCalledOnce();
  });
});
