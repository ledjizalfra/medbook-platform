import { Component, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';
import { SpecializationService } from '../../../core/services/specialization.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { SPECIALIZATION_ICON_MAP } from '../../../core/constants/ui.constants';

/**
 * Pagina home pubblica - punto di ingresso della vetrina.
 *
 * Carica dati reali dal backend:
 * - Specializzazioni da GET /bff/v1/specializations (chip cliccabili)
 * - Cliniche da GET /bff/v1/clinics (card con nome e città)
 *
 * Sezioni:
 * 1. Hero — headline, CTA primario e secondario
 * 2. Come funziona — 3 passi statici
 * 3. Specializzazioni — chip cliccabili con dati reali
 * 4. Cliniche — card con dati reali
 * 5. Sezione cliniche (B2B) — invito per le cliniche
 * 6. CTA paziente — invito alla registrazione
 */
@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    PublicNavbarComponent,
    PublicFooterComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  private specializationService = inject(SpecializationService);
  private clinicService = inject(ClinicService);
  protected kc = inject(KeycloakService);
  private router = inject(Router);

  protected loadingSpecs = signal(false);
  protected loadingClinics = signal(false);
  protected specializations = signal<unknown[]>([]);
  protected clinics = signal<unknown[]>([]);

  // Icone Material associate a specializzazioni — centralizzate in ui.constants.ts
  private readonly specIcons = SPECIALIZATION_ICON_MAP;

  // I 3 passi del flusso di prenotazione
  protected readonly steps = [
    { icon: 'search',        title: 'Cerca',    desc: 'Specializzazione, medico o clinica nella tua città' },
    { icon: 'event',         title: 'Scegli',   desc: 'Data e orario tra quelli disponibili' },
    { icon: 'check_circle',  title: 'Conferma', desc: 'Ricevi conferma via email o SMS' },
  ];

  ngOnInit(): void {
    this.loadSpecializations();
    this.loadClinics();
  }

  private loadSpecializations(): void {
    this.loadingSpecs.set(true);
    this.specializationService.getAll().subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        const specs = (inner as Record<string, unknown>)?.['specializations'];
        this.specializations.set(Array.isArray(specs) ? specs as unknown[] : []);
        this.loadingSpecs.set(false);
      },
      error: () => this.loadingSpecs.set(false)
    });
  }

  private loadClinics(): void {
    this.loadingClinics.set(true);
    this.clinicService.getAll().subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        this.clinics.set(Array.isArray(inner) ? inner as unknown[] : []);
        this.loadingClinics.set(false);
      },
      error: () => this.loadingClinics.set(false)
    });
  }

  /** Naviga alla ricerca disponibilità con filtro specializzazione pre-impostato */
  protected goToAvailabilityWithSpec(spec: unknown): void {
    const id = (spec as Record<string, unknown>)['specializationId'] as string;
    if (this.kc.isLoggedIn()) {
      this.router.navigate(['/availability'], { queryParams: { specialization: id } });
    } else {
      this.kc.login();
    }
  }

  protected goToAvailability(): void {
    if (this.kc.isLoggedIn()) {
      this.router.navigate(['/availability']);
    } else {
      this.kc.login();
    }
  }

  protected getSpecIcon(spec: unknown): string {
    const name = ((spec as Record<string, unknown>)['name'] as string ?? '').toLowerCase();
    return this.specIcons[name] ?? 'medical_services';
  }

  protected getField(obj: unknown, field: string): unknown {
    return (obj as Record<string, unknown>)?.[field];
  }
}
