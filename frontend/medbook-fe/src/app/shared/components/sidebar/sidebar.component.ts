import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicStore } from '../../../core/store/clinic.store';
import { DoctorStore } from '../../../core/store/doctor.store';

interface MenuItem {
  icon: string;
  label: string;
  route: string;
  roles: string[];
}

/**
 * Sidebar con visibilita per ruolo.
 *
 * Per ADMIN espone anche lo stato del workflow (hasClinics/hasDoctors)
 * usato dai componenti figli per abilitare/disabilitare i tasti d'azione.
 */
@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  protected kc = inject(KeycloakService);
  private clinicService = inject(ClinicService);
  private doctorService = inject(DoctorService);
  private clinicStore = inject(ClinicStore);
  private doctorStore = inject(DoctorStore);

  /** Stato workflow ADMIN — usato dai componenti per abilitare/disabilitare tasti */
  hasClinics = signal(false);
  hasDoctors = signal(false);

  readonly menuItems: MenuItem[] = [
    { icon: 'dashboard',        label: 'Dashboard',             route: '/dashboard',    roles: ['PATIENT', 'DOCTOR', 'RECEPTIONIST', 'ADMIN'] },
    { icon: 'event_available',  label: 'Prenota',               route: '/availability', roles: ['PATIENT', 'RECEPTIONIST'] },
    { icon: 'calendar_today',   label: 'I miei appuntamenti',   route: '/appointments', roles: ['PATIENT', 'DOCTOR'] },
    { icon: 'list_alt',         label: 'Appuntamenti',          route: '/appointments', roles: ['RECEPTIONIST', 'ADMIN'] },
    { icon: 'person',           label: 'Il mio profilo',        route: '/profile',      roles: ['PATIENT', 'DOCTOR'] },
    { icon: 'business',         label: 'Cliniche',              route: '/clinics',      roles: ['ADMIN'] },
    { icon: 'medical_services', label: 'Medici',                route: '/doctors',      roles: ['ADMIN'] },
    { icon: 'group',            label: 'Pazienti',              route: '/patients',     roles: ['RECEPTIONIST', 'ADMIN'] },
    { icon: 'notifications',    label: 'Notifiche',             route: '/notifications',roles: ['PATIENT', 'ADMIN'] }
  ];

  get visibleItems(): MenuItem[] {
    return this.menuItems.filter(item =>
      item.roles.some(role => this.kc.hasRole(role))
    );
  }

  ngOnInit(): void {
    if (!this.kc.hasRole('ADMIN')) return;
    this.checkWorkflowState();
  }

  /** Verifica se esistono cliniche e medici — usa gli store con cache TTL */
  checkWorkflowState(): void {
    this.clinicStore.loadAll().subscribe({
      next: (list) => this.hasClinics.set(list.length > 0)
    });
    this.doctorStore.loadAll().subscribe({
      next: (list) => this.hasDoctors.set(list.length > 0)
    });
  }
}
