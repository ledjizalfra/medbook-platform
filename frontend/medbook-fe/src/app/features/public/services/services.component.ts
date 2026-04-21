import { Component, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PublicNavbarComponent } from '../../../shared/components/public-navbar/public-navbar.component';
import { PublicFooterComponent } from '../../../shared/components/public-footer/public-footer.component';
import { SpecializationService } from '../../../core/services/specialization.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { SPECIALIZATION_ICON_MAP } from '../../../core/constants/ui.constants';

/**
 * Pagina "Servizi / Specializzazioni" — dati reali da GET /bff/v1/specializations.
 *
 * Mostra una griglia di card cliccabili. Il click naviga alla ricerca
 * disponibilità con il filtro specializzazione pre-impostato.
 */
@Component({
  selector: 'app-services',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    PublicNavbarComponent,
    PublicFooterComponent
  ],
  templateUrl: './services.component.html',
  styleUrl: './services.component.scss'
})
export class ServicesComponent implements OnInit {
  private specializationService = inject(SpecializationService);
  private kc = inject(KeycloakService);
  private router = inject(Router);

  protected loading = signal(false);
  protected specializations = signal<unknown[]>([]);

  // Icone Material per specializzazioni — centralizzate in ui.constants.ts
  private readonly specIcons = SPECIALIZATION_ICON_MAP;

  ngOnInit(): void {
    this.loading.set(true);
    this.specializationService.getAll().subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        const specs = (inner as Record<string, unknown>)?.['specializations'];
        this.specializations.set(Array.isArray(specs) ? specs as unknown[] : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  /** Click su una specializzazione — naviga a /availability con filtro */
  protected goToSpec(spec: unknown): void {
    const id = (spec as Record<string, unknown>)['specializationId'] as string;
    if (this.kc.isLoggedIn()) {
      this.router.navigate(['/availability'], { queryParams: { specialization: id } });
    } else {
      this.kc.login();
    }
  }

  protected getIcon(spec: unknown): string {
    const name = ((spec as Record<string, unknown>)['name'] as string ?? '').toLowerCase();
    return this.specIcons[name] ?? 'medical_services';
  }

  protected getField(obj: unknown, field: string): unknown {
    return (obj as Record<string, unknown>)?.[field];
  }
}
