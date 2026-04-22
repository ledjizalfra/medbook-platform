import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { consentGuard } from './core/auth/consent.guard';
import { roleGuard } from './core/auth/role.guard';
import { APP_ROUTES } from './core/constants/app-routes';
import { KeycloakService } from './core/auth/keycloak.service';

/**
 * Configurazione delle rotte dell'applicazione.
 *
 * **Struttura per ruolo**: ogni ruolo naviga sotto il proprio prefisso URL.
 * Questo rende immediatamente visibile nell'URL il contesto operativo
 * (es. /doctor/dashboard, /patient/appointments).
 *
 * **Rotte pubbliche**: nessun guard, accessibili a chiunque.
 *
 * **Rotte protette**: authGuard (autenticazione) + roleGuard (autorizzazione).
 *
 * Il redirect generico `/dashboard` e `/appointments` reindirizzano alla
 * versione con prefisso grazie al guard che legge il ruolo dal JWT.
 *
 * Tutte le rotte usano `loadComponent` per il lazy loading.
 */
export const routes: Routes = [

  // ===========================================================
  // ROTTE PUBBLICHE — nessun guard
  // ===========================================================
  {
    path: APP_ROUTES.HOME,
    loadComponent: () =>
      import('./features/public/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: APP_ROUTES.HOW_IT_WORKS,
    loadComponent: () =>
      import('./features/public/how-it-works/how-it-works.component').then(m => m.HowItWorksComponent)
  },
  {
    path: APP_ROUTES.SERVICES,
    loadComponent: () =>
      import('./features/public/services/services.component').then(m => m.ServicesComponent)
  },
  {
    path: APP_ROUTES.ABOUT,
    loadComponent: () =>
      import('./features/public/about/about.component').then(m => m.AboutComponent)
  },
  {
    path: APP_ROUTES.CONTACT,
    loadComponent: () =>
      import('./features/public/contact/contact.component').then(m => m.ContactComponent)
  },
  {
    path: APP_ROUTES.CLINIC_REGISTRATION,
    loadComponent: () =>
      import('./features/public/clinic-registration/clinic-registration.component').then(m => m.ClinicRegistrationComponent)
  },
  {
    path: APP_ROUTES.PRIVACY_POLICY,
    loadComponent: () =>
      import('./features/public/legal/privacy-policy.component').then(m => m.PrivacyPolicyComponent)
  },
  {
    path: APP_ROUTES.TERMS,
    loadComponent: () =>
      import('./features/public/legal/termini-utilizzo.component').then(m => m.TerminiUtilizzoComponent)
  },
  {
    path: APP_ROUTES.COOKIE_POLICY,
    loadComponent: () =>
      import('./features/public/legal/cookie-policy.component').then(m => m.CookiePolicyComponent)
  },
  {
    path: APP_ROUTES.LOGIN,
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: APP_ROUTES.REGISTER,
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },

  // ===========================================================
  // ROTTE PAZIENTE — prefisso /patient
  // ===========================================================
  {
    path: APP_ROUTES.PATIENT,
    canActivate: [authGuard, roleGuard([APP_ROUTES.PATIENT.toUpperCase()])],
    children: [
      { path: APP_ROUTES.DASHBOARD, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: APP_ROUTES.APPOINTMENTS, loadComponent: () => import('./features/appointments/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent) },
      { path: `${APP_ROUTES.APPOINTMENTS}/:id`, loadComponent: () => import('./features/appointments/appointment-detail/appointment-detail.component').then(m => m.AppointmentDetailComponent) },
      { path: APP_ROUTES.AVAILABILITY, loadComponent: () => import('./features/appointments/availability-search/availability-search.component').then(m => m.AvailabilitySearchComponent) },
      { path: APP_ROUTES.PROFILE, loadComponent: () => import('./features/patients/patient-profile/patient-profile.component').then(m => m.PatientProfileComponent) },
      { path: APP_ROUTES.NOTIFICATIONS, loadComponent: () => import('./features/notifications/notification-list/notification-list.component').then(m => m.NotificationListComponent) },
      { path: '', redirectTo: APP_ROUTES.DASHBOARD, pathMatch: 'full' }
    ]
  },

  // ===========================================================
  // ROTTE MEDICO — prefisso /doctor
  // ===========================================================
  {
    path: APP_ROUTES.DOCTOR,
    canActivate: [authGuard, roleGuard([APP_ROUTES.DOCTOR.toUpperCase()])],
    children: [
      // Pagina consensi — accessibile senza consentGuard (e la destinazione del redirect)
      { path: APP_ROUTES.CONSENT, loadComponent: () => import('./features/doctors/doctor-consent/doctor-consent.component').then(m => m.DoctorConsentComponent) },
      // Tutte le altre pagine — protette dal consentGuard (redirect a /doctor/consent se PENDING)
      {
        path: '',
        canActivate: [consentGuard],
        children: [
          { path: APP_ROUTES.DASHBOARD, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: APP_ROUTES.APPOINTMENTS, loadComponent: () => import('./features/appointments/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent) },
          { path: `${APP_ROUTES.APPOINTMENTS}/:id`, loadComponent: () => import('./features/appointments/appointment-detail/appointment-detail.component').then(m => m.AppointmentDetailComponent) },
          { path: APP_ROUTES.PROFILE, loadComponent: () => import('./features/doctors/doctor-profile/doctor-profile.component').then(m => m.DoctorProfileComponent) },
          { path: APP_ROUTES.NOTIFICATIONS, loadComponent: () => import('./features/notifications/notification-list/notification-list.component').then(m => m.NotificationListComponent) },
          { path: '', redirectTo: APP_ROUTES.DASHBOARD, pathMatch: 'full' }
        ]
      }
    ]
  },

  // ===========================================================
  // ROTTE RECEPTIONIST — prefisso /receptionist
  // ===========================================================
  {
    path: APP_ROUTES.RECEPTIONIST,
    canActivate: [authGuard, roleGuard([APP_ROUTES.RECEPTIONIST.toUpperCase()])],
    children: [
      { path: APP_ROUTES.DASHBOARD, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: APP_ROUTES.APPOINTMENTS, loadComponent: () => import('./features/appointments/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent) },
      { path: `${APP_ROUTES.APPOINTMENTS}/:id`, loadComponent: () => import('./features/appointments/appointment-detail/appointment-detail.component').then(m => m.AppointmentDetailComponent) },
      { path: APP_ROUTES.AVAILABILITY, loadComponent: () => import('./features/appointments/availability-search/availability-search.component').then(m => m.AvailabilitySearchComponent) },
      { path: APP_ROUTES.PATIENTS, loadComponent: () => import('./features/patients/patient-list/patient-list.component').then(m => m.PatientListComponent) },
      { path: `${APP_ROUTES.PATIENTS}/new`, loadComponent: () => import('./features/patients/patient-form/patient-form.component').then(m => m.PatientFormComponent) },
      { path: `${APP_ROUTES.PATIENTS}/:id/edit`, loadComponent: () => import('./features/patients/patient-form/patient-form.component').then(m => m.PatientFormComponent) },
      { path: '', redirectTo: APP_ROUTES.DASHBOARD, pathMatch: 'full' }
    ]
  },

  // ===========================================================
  // ROTTE CLINIC — prefisso /clinic (gestione clinica)
  // ===========================================================
  {
    path: APP_ROUTES.CLINIC,
    canActivate: [authGuard, roleGuard([APP_ROUTES.ADMIN.toUpperCase()])],
    children: [
      { path: APP_ROUTES.DASHBOARD, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: APP_ROUTES.DOCTORS, loadComponent: () => import('./features/doctors/doctor-list/doctor-list.component').then(m => m.DoctorListComponent) },
      { path: `${APP_ROUTES.DOCTORS}/:id`, loadComponent: () => import('./features/doctors/doctor-form/doctor-form.component').then(m => m.DoctorFormComponent) },
      { path: APP_ROUTES.APPOINTMENTS, loadComponent: () => import('./features/appointments/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent) },
      { path: `${APP_ROUTES.APPOINTMENTS}/:id`, loadComponent: () => import('./features/appointments/appointment-detail/appointment-detail.component').then(m => m.AppointmentDetailComponent) },
      { path: `${APP_ROUTES.PROFILE}/:id`, loadComponent: () => import('./features/clinics/clinic-profile/clinic-profile.component').then(m => m.ClinicProfileComponent) },
      { path: '', redirectTo: APP_ROUTES.DASHBOARD, pathMatch: 'full' }
    ]
  },

  // ===========================================================
  // ROTTE ADMIN — prefisso /admin
  // ===========================================================
  {
    path: APP_ROUTES.ADMIN,
    canActivate: [authGuard, roleGuard([APP_ROUTES.ADMIN.toUpperCase()])],
    children: [
      { path: APP_ROUTES.DASHBOARD, loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: APP_ROUTES.AVAILABILITY, loadComponent: () => import('./features/appointments/availability-search/availability-search.component').then(m => m.AvailabilitySearchComponent) },
      { path: APP_ROUTES.APPOINTMENTS, loadComponent: () => import('./features/appointments/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent) },
      { path: `${APP_ROUTES.APPOINTMENTS}/:id`, loadComponent: () => import('./features/appointments/appointment-detail/appointment-detail.component').then(m => m.AppointmentDetailComponent) },
      { path: APP_ROUTES.PATIENTS, loadComponent: () => import('./features/patients/patient-list/patient-list.component').then(m => m.PatientListComponent) },
      { path: `${APP_ROUTES.PATIENTS}/new`, loadComponent: () => import('./features/patients/patient-form/patient-form.component').then(m => m.PatientFormComponent) },
      { path: `${APP_ROUTES.PATIENTS}/:id/edit`, loadComponent: () => import('./features/patients/patient-form/patient-form.component').then(m => m.PatientFormComponent) },
      { path: APP_ROUTES.DOCTORS, loadComponent: () => import('./features/doctors/doctor-list/doctor-list.component').then(m => m.DoctorListComponent) },
      { path: `${APP_ROUTES.DOCTORS}/new`, loadComponent: () => import('./features/doctors/doctor-form/doctor-form.component').then(m => m.DoctorFormComponent) },
      { path: `${APP_ROUTES.DOCTORS}/:id/edit`, loadComponent: () => import('./features/doctors/doctor-form/doctor-form.component').then(m => m.DoctorFormComponent) },
      { path: `${APP_ROUTES.DOCTORS}/availabilities`, loadComponent: () => import('./features/doctors/doctor-availabilities-page/doctor-availabilities-page.component').then(m => m.DoctorAvailabilitiesPageComponent) },
      { path: `${APP_ROUTES.DOCTORS}/assignments`, loadComponent: () => import('./features/doctors/doctor-assignments-page/doctor-assignments-page.component').then(m => m.DoctorAssignmentsPageComponent) },
      { path: APP_ROUTES.CLINICS, loadComponent: () => import('./features/clinics/clinic-list/clinic-list.component').then(m => m.ClinicListComponent) },
      { path: `${APP_ROUTES.CLINICS}/new`, loadComponent: () => import('./features/clinics/clinic-form/clinic-form.component').then(m => m.ClinicFormComponent) },
      { path: `${APP_ROUTES.CLINICS}/:id/edit`, loadComponent: () => import('./features/clinics/clinic-form/clinic-form.component').then(m => m.ClinicFormComponent) },
      { path: `${APP_ROUTES.CLINICS}/:id/assignments`, loadComponent: () => import('./features/clinics/assignment-list/assignment-list.component').then(m => m.AssignmentListComponent) },
      { path: APP_ROUTES.NOTIFICATIONS, loadComponent: () => import('./features/notifications/notification-list/notification-list.component').then(m => m.NotificationListComponent) },
      { path: '', redirectTo: APP_ROUTES.DASHBOARD, pathMatch: 'full' }
    ]
  },

  // ===========================================================
  // REDIRECT GENERICI — rotte senza prefisso di ruolo usate dalla sidebar
  // Ogni percorso viene risolto al path corretto in base al ruolo corrente,
  // seguendo lo stesso pattern del redirect /dashboard.
  // ===========================================================
  { path: 'dashboard',     redirectTo: () => inject(KeycloakService).getRoleDashboardRoute() },
  { path: 'profile',       redirectTo: () => inject(KeycloakService).getProfileRoute() ?? '/' },
  { path: 'appointments',  redirectTo: () => inject(KeycloakService).getAppointmentsRoute() },
  { path: 'availability',  redirectTo: () => inject(KeycloakService).getAvailabilityRoute() },
  { path: 'notifications', redirectTo: () => inject(KeycloakService).getNotificationsRoute() },
  { path: 'patients',      redirectTo: () => inject(KeycloakService).getPatientsRoute() },
  { path: 'doctors',       redirectTo: () => inject(KeycloakService).getDoctorsRoute() },
  { path: 'clinics',       redirectTo: () => inject(KeycloakService).getClinicsRoute() },

  // Fallback — reindirizza alla home pubblica
  { path: '**', redirectTo: APP_ROUTES.HOME }
];
