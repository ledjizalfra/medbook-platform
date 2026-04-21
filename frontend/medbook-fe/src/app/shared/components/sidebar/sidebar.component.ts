import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { KeycloakService } from '../../../core/auth/keycloak.service';

/**
 * Struttura dati di una voce di menu.
 * - `icon`: nome dell'icona Material Icons
 * - `label`: testo mostrato nella sidebar
 * - `route`: percorso di navigazione
 * - `roles`: lista dei ruoli che possono vedere questa voce (logica OR)
 */
interface MenuItem {
  icon: string;
  label: string;
  route: string;
  roles: string[];
}

/**
 * Componente sidebar di navigazione con visibilità condizionale per ruolo.
 *
 * Il menu completo è definito staticamente in `menuItems`. Al render, viene
 * filtrato dal getter `visibleItems` che mostra solo le voci accessibili
 * all'utente corrente in base ai suoi ruoli Keycloak.
 *
 * Questo approccio centralizza la configurazione dei permessi UI in un solo
 * posto, rendendo facile aggiungere o modificare voci senza toccare i template.
 */
@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  protected kc = inject(KeycloakService);

  /**
   * Definizione completa delle voci di menu con i rispettivi ruoli autorizzati.
   * Nota: PATIENT e RECEPTIONIST/ADMIN vedono la stessa rotta /appointments
   * ma con etichette diverse ("I miei appuntamenti" vs "Appuntamenti").
   */
  readonly menuItems: MenuItem[] = [
    { icon: 'dashboard',        label: 'Dashboard',             route: '/dashboard',    roles: ['PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN'] },
    { icon: 'event_available',  label: 'Prenota',               route: '/availability', roles: ['PATIENT', 'RECEPTIONIST'] },
    { icon: 'calendar_today',   label: 'I miei appuntamenti',   route: '/appointments', roles: ['PATIENT', 'DOCTOR'] },
    { icon: 'list_alt',         label: 'Appuntamenti',          route: '/appointments', roles: ['RECEPTIONIST', 'ADMIN'] },
    { icon: 'person',           label: 'Il mio profilo',        route: '/profile',      roles: ['PATIENT', 'DOCTOR'] },
    { icon: 'group',            label: 'Pazienti',              route: '/patients',     roles: ['RECEPTIONIST', 'ADMIN'] },
    { icon: 'medical_services', label: 'Medici',                route: '/doctors',      roles: ['ADMIN'] },
    { icon: 'business',         label: 'Sedi',                  route: '/clinics',      roles: ['ADMIN'] },
    { icon: 'notifications',    label: 'Notifiche',             route: '/notifications',roles: ['PATIENT', 'ADMIN'] }
  ];

  /**
   * Restituisce solo le voci di menu visibili per l'utente corrente.
   * Il filtro usa `some()` per la logica OR: basta che l'utente abbia
   * almeno uno dei ruoli della voce per vederla.
   */
  get visibleItems(): MenuItem[] {
    return this.menuItems.filter(item =>
      item.roles.some(role => this.kc.hasRole(role))
    );
  }
}
