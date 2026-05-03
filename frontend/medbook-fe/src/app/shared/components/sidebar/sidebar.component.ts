import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ClinicStore } from '../../../core/store/clinic.store';
import { DoctorStore } from '../../../core/store/doctor.store';

interface MenuItem {
  icon: string;
  label: string;
  route: string;
}

/**
 * Sidebar con visibilita per ruolo.
 *
 * Le route vengono calcolate dinamicamente in base al ruolo dell'utente
 * tramite KeycloakService (es. /patient/dashboard, /admin/doctors).
 * Questo garantisce che routerLinkActive evidenzi correttamente la voce attiva.
 */
@Component({
  selector: 'app-sidebar',
  imports: [MatListModule, MatIconModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent implements OnInit {
  protected kc = inject(KeycloakService);
  private clinicStore = inject(ClinicStore);
  private doctorStore = inject(DoctorStore);

  /** Stato workflow ADMIN — usato dai componenti per abilitare/disabilitare tasti */
  hasClinics = signal(false);
  hasDoctors = signal(false);

  /** Voci di menu visibili per il ruolo corrente — calcolate una volta al login */
  visibleItems: MenuItem[] = [];

  ngOnInit(): void {
    this.visibleItems = this.buildMenuForRole();

    if (this.kc.hasRole('ADMIN')) {
      this.checkWorkflowState();
    }
  }

  /** Costruisce il menu in base al ruolo dell'utente autenticato */
  private buildMenuForRole(): MenuItem[] {
    const items: MenuItem[] = [];

    // Dashboard — tutti i ruoli
    items.push({ icon: 'dashboard', label: 'Dashboard', route: this.kc.getRoleDashboardRoute() });

    // Prenota — PATIENT, RECEPTIONIST, ADMIN
    if (this.kc.hasRole('PATIENT') || this.kc.hasRole('RECEPTIONIST') || this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'event_available', label: 'Prenota', route: this.kc.getAvailabilityRoute() });
    }

    // Appuntamenti — tutti i ruoli
    if (this.kc.hasRole('PATIENT') || this.kc.hasRole('DOCTOR')) {
      items.push({ icon: 'calendar_today', label: 'I miei appuntamenti', route: this.kc.getAppointmentsRoute() });
    } else {
      items.push({ icon: 'list_alt', label: 'Appuntamenti', route: this.kc.getAppointmentsRoute() });
    }

    // Profilo — PATIENT, DOCTOR
    const profileRoute = this.kc.getProfileRoute();
    if (profileRoute) {
      items.push({ icon: 'person', label: 'Il mio profilo', route: profileRoute });
    }

    // Cliniche — ADMIN
    if (this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'business', label: 'Cliniche', route: this.kc.getClinicsRoute() });
    }

    // Medici — ADMIN
    if (this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'medical_services', label: 'Medici', route: this.kc.getDoctorsRoute() });
    }

    // Pazienti — RECEPTIONIST, ADMIN
    if (this.kc.hasRole('RECEPTIONIST') || this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'group', label: 'Pazienti', route: this.kc.getPatientsRoute() });
    }

    // Receptionist — ADMIN
    if (this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'badge', label: 'Receptionist', route: '/admin/receptionists' });
    }

    // Notifiche — PATIENT, ADMIN
    if (this.kc.hasRole('PATIENT') || this.kc.hasRole('ADMIN')) {
      items.push({ icon: 'notifications', label: 'Notifiche', route: this.kc.getNotificationsRoute() });
    }

    return items;
  }

  /** Verifica se esistono cliniche e medici — usa gli store con cache TTL */
  private checkWorkflowState(): void {
    this.clinicStore.loadAll().subscribe({
      next: (list) => this.hasClinics.set(list.length > 0)
    });
    this.doctorStore.loadAll().subscribe({
      next: (list) => this.hasDoctors.set(list.length > 0)
    });
  }
}
