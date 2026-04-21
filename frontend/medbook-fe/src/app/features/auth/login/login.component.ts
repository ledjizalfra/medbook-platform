import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { KeycloakService } from '../../../core/auth/keycloak.service';

/**
 * Componente pagina di login.
 *
 * Non gestisce direttamente credenziali: al click del pulsante "Accedi"
 * chiama `kc.login()` che redirige l'utente alla pagina di login di Keycloak
 * (Authorization Code Flow + PKCE). Dopo l'autenticazione, Keycloak riporta
 * l'utente su `http://localhost:4200/` dove il token viene scambiato e
 * l'app lo riconosce come autenticato.
 *
 * La rotta `/login` è pubblica (nessun guard in app.routes.ts).
 */
@Component({
  selector: 'app-login',
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  protected kc = inject(KeycloakService);
}
