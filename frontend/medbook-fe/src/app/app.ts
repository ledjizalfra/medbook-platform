import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { KeycloakService } from './core/auth/keycloak.service';
import { UserContextService } from './core/auth/user-context.service';
import { ThemeService } from './core/services/theme.service';
import { APP_ROUTES } from './core/constants/app-routes';

/**
 * Componente radice dell'applicazione.
 *
 * Seleziona il layout in base alla rotta corrente:
 * - Pagine pubbliche e di accesso => solo router-outlet
 * - Pagine autenticate => navbar + sidebar + router-outlet
 *
 * Tutta la logica sul tema CSS è delegata a ThemeService.
 * Il redirect post-login (Keycloak => home pubblica => dashboard) è gestito qui
 * perché dipende dalla navigazione, non dal tema.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected kc = inject(KeycloakService);
  private readonly userContext = inject(UserContextService);
  private readonly router = inject(Router);
  private readonly theme = inject(ThemeService);
  private readonly destroyRef = inject(DestroyRef);

  // true quando la rotta corrente usa il layout pubblico (senza navbar/sidebar)
  protected isPublicPage = signal(true);

  /**
   * Lifecycle hook Angular - eseguito una volta dopo la creazione del componente.
   *
   * In questo componente gestisce tre responsabilità in sequenza:
   * 1. Applica il tema corretto per l'URL già attivo al bootstrap
   * 2. Gestisce il redirect post-login (Keycloak torna sull'URL di partenza)
   * 3. Sottoscrive gli eventi di navigazione per aggiornare layout e tema ad ogni rotta
   */
  ngOnInit(): void {
    const currentUrl = this.router.url;

    // Applica subito il tema dell'URL attivo (il primo NavigationEnd è già scattato)
    this.theme.apply(currentUrl);

    if (this.kc.isLoggedIn()) {
      // Carica il profilo utente dal backend (best-effort, non blocca il rendering)
      this.userContext.loadProfile();

      // Dopo il login Keycloak reindirizza all'URL di partenza (es. /).
      // Se l'utente è autenticato e atterra su una pagina pubblica, lo inviamo alla dashboard.
      if (this.theme.isPublicRoute(currentUrl)) {
        this.router.navigate(['/' + APP_ROUTES.DASHBOARD]);
      }
    }

    // Aggiorna layout e tema ad ogni cambio di rotta
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(e => {
      const url = (e as NavigationEnd).urlAfterRedirects;
      this.isPublicPage.set(this.theme.isPublicRoute(url));
      this.theme.apply(url);
    });
  }
}
