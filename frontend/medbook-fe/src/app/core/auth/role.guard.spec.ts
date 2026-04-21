import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { roleGuard } from './role.guard';
import { KeycloakService } from './keycloak.service';

describe('roleGuard', () => {
  let kcMock: { hasRole: ReturnType<typeof vi.fn> };
  let routerMock: { parseUrl: ReturnType<typeof vi.fn> };
  const fakeUrlTree = {} as UrlTree;

  beforeEach(() => {
    kcMock = { hasRole: vi.fn() };
    routerMock = { parseUrl: vi.fn().mockReturnValue(fakeUrlTree) };

    TestBed.configureTestingModule({
      providers: [
        { provide: KeycloakService, useValue: kcMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  it('dovrebbe consentire la navigazione se l\'utente ha uno dei ruoli richiesti', () => {
    kcMock.hasRole.mockImplementation((r: string) => r === 'ADMIN');
    const guard = roleGuard(['ADMIN', 'RECEPTIONIST']);
    const result = TestBed.runInInjectionContext(() =>
      guard({} as never, {} as never)
    );
    expect(result).toBe(true);
  });

  it('dovrebbe consentire la navigazione anche con logica OR tra ruoli (basta uno)', () => {
    // L'utente ha RECEPTIONIST ma non ADMIN: deve comunque passare
    kcMock.hasRole.mockImplementation((r: string) => r === 'RECEPTIONIST');
    const guard = roleGuard(['ADMIN', 'RECEPTIONIST']);
    const result = TestBed.runInInjectionContext(() =>
      guard({} as never, {} as never)
    );
    expect(result).toBe(true);
  });

  it('dovrebbe reindirizzare alla dashboard se l\'utente non ha nessuno dei ruoli richiesti', () => {
    kcMock.hasRole.mockReturnValue(false);
    const guard = roleGuard(['ADMIN']);
    const result = TestBed.runInInjectionContext(() =>
      guard({} as never, {} as never)
    );
    expect(result).toBe(fakeUrlTree);
    expect(routerMock.parseUrl).toHaveBeenCalledWith('/dashboard');
  });

  it('dovrebbe bloccare una lista vuota di ruoli richiesti', () => {
    kcMock.hasRole.mockReturnValue(false);
    const guard = roleGuard([]);
    const result = TestBed.runInInjectionContext(() =>
      guard({} as never, {} as never)
    );
    expect(result).toBe(fakeUrlTree);
  });
});
