import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { KeycloakService } from '../../../core/auth/keycloak.service';

/**
 * Componente barra di navigazione superiore (toolbar).
 *
 * Mostra il titolo dell'applicazione e, a destra, il nome dell'utente
 * autenticato. Il nome è un link verso il profilo personale (ruolo-dipendente)
 * oppure semplice testo per i ruoli senza profilo (RECEPTIONIST, ADMIN).
 * Il pulsante di logout invalida la sessione Keycloak e reindirizza alla homepage.
 */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink, MatToolbarModule, MatButtonModule, MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  protected kc = inject(KeycloakService);
}
