import { Component } from '@angular/core';
import { LegalPageComponent } from './legal-page.component';

/** Pagina Privacy Policy — testo ispirato al GDPR, contestualizzato per MedBook. */
@Component({
  selector: 'app-privacy-policy',
  imports: [LegalPageComponent],
  templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicyComponent {}
