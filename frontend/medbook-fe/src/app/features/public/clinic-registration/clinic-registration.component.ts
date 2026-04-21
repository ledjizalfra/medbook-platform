import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';

/**
 * Pagina pubblica B2B per le cliniche interessate a MedBook.
 *
 * Non contiene un form di registrazione — la registrazione avviene
 * tramite il team MedBook contattabile per telefono o email.
 * La pagina spiega il processo e fornisce i contatti.
 */
@Component({
  selector: 'app-clinic-registration',
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    PublicNavbarComponent,
    PublicFooterComponent
  ],
  templateUrl: './clinic-registration.component.html',
  styleUrl: './clinic-registration.component.scss'
})
export class ClinicRegistrationComponent {}
