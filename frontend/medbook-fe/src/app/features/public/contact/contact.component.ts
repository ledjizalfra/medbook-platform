import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';

/**
 * Pagina "Contatti" - form statico solo UI, non funzionante.
 *
 * Mostra un form di contatto con i campi nome, email e messaggio.
 * Il pulsante "Invia" è presente visivamente ma non effettua
 * nessuna chiamata al backend (placeholder per futuri sviluppi).
 *
 * Include anche i dati di contatto placeholder della piattaforma.
 */
@Component({
  selector: 'app-contact',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    PublicNavbarComponent,
    PublicFooterComponent
  ],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss'
})
export class ContactComponent {
  private fb = inject(FormBuilder);

  // Form reattivo con validazioni - solo UI, nessun submit al BE
  protected form = this.fb.group({
    name:    ['', Validators.required],
    email:   ['', [Validators.required, Validators.email]],
    subject: ['', Validators.required],
    message: ['', Validators.required]
  });

  // Opzioni del campo Oggetto
  protected readonly subjects = [
    'Registrazione clinica',
    'Assistenza',
    'Altro'
  ];
}
