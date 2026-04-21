import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { KeycloakService } from '../../../core/auth/keycloak.service';

describe('SidebarComponent - visibleItems', () => {
  let component: SidebarComponent;
  let kcMock: { hasRole: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    kcMock = { hasRole: vi.fn().mockReturnValue(false) };

    TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideRouter([]),
        { provide: KeycloakService, useValue: kcMock },
      ],
    });

    const fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
  });

  it('non dovrebbe mostrare voci se l\'utente non ha ruoli', () => {
    kcMock.hasRole.mockReturnValue(false);
    // Solo Dashboard è visibile a tutti i ruoli autenticati
    // Con hasRole sempre false, nessuna voce deve comparire
    expect(component.visibleItems).toHaveLength(0);
  });

  it('dovrebbe mostrare le voci corrette per il ruolo PATIENT', () => {
    kcMock.hasRole.mockImplementation((r: string) => r === 'PATIENT');
    const labels = component.visibleItems.map(i => i.label);
    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Prenota');
    expect(labels).toContain('I miei appuntamenti');
    expect(labels).toContain('Il mio profilo');
    expect(labels).toContain('Notifiche');
    // Voci riservate ad altri ruoli non devono comparire
    expect(labels).not.toContain('Pazienti');
    expect(labels).not.toContain('Medici');
    expect(labels).not.toContain('Sedi');
  });

  it('dovrebbe mostrare le voci corrette per il ruolo DOCTOR', () => {
    kcMock.hasRole.mockImplementation((r: string) => r === 'DOCTOR');
    const labels = component.visibleItems.map(i => i.label);
    expect(labels).toContain('Dashboard');
    expect(labels).toContain('I miei appuntamenti');
    expect(labels).not.toContain('Prenota');
    expect(labels).not.toContain('Pazienti');
  });

  it('dovrebbe mostrare le voci corrette per il ruolo RECEPTIONIST', () => {
    kcMock.hasRole.mockImplementation((r: string) => r === 'RECEPTIONIST');
    const labels = component.visibleItems.map(i => i.label);
    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Prenota');
    expect(labels).toContain('Appuntamenti');
    expect(labels).toContain('Pazienti');
    // RECEPTIONIST non vede Medici e Sedi (solo ADMIN)
    expect(labels).not.toContain('Medici');
    expect(labels).not.toContain('Sedi');
  });

  it('dovrebbe mostrare tutte le voci per il ruolo ADMIN', () => {
    kcMock.hasRole.mockImplementation((r: string) =>
      ['ADMIN', 'RECEPTIONIST'].includes(r)
    );
    const labels = component.visibleItems.map(i => i.label);
    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Pazienti');
    expect(labels).toContain('Medici');
    expect(labels).toContain('Sedi');
    expect(labels).toContain('Notifiche');
  });

  it('non dovrebbe mostrare voci duplicate anche se l\'utente ha più ruoli', () => {
    // Un utente con più ruoli non dovrebbe vedere voci ripetute
    kcMock.hasRole.mockReturnValue(true);
    const routes = component.visibleItems.map(i => i.route);
    const uniqueRoutes = new Set(routes);
    expect(routes.length).toBe(uniqueRoutes.size);
  });
});
