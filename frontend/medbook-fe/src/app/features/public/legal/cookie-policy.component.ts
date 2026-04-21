import { Component } from '@angular/core';
import { LegalPageComponent } from './legal-page.component';

/** Pagina Cookie Policy — informativa sui cookie tecnici e analitici. */
@Component({
  selector: 'app-cookie-policy',
  imports: [LegalPageComponent],
  templateUrl: './cookie-policy.component.html'
})
export class CookiePolicyComponent {}
