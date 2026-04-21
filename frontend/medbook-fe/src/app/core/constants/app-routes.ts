/**
 * Segmenti e percorsi completi delle rotte dell'applicazione.
 *
 * Struttura con prefissi per ruolo: ogni ruolo naviga sotto il proprio prefisso
 * (es. /patient/dashboard, /doctor/appointments) in modo che l'URL rifletta
 * chiaramente il contesto operativo dell'utente.
 *
 * Usato in tre contesti:
 * - `app.routes.ts` => campo `path` di ogni rotta
 * - `KeycloakService` => calcolo della rotta post-login e del link profilo
 * - Componenti => `router.navigate(...)` e `routerLink`
 */
export const APP_ROUTES = {
  // =========================================================================
  // Area pubblica
  // =========================================================================
  HOME:         '',
  HOW_IT_WORKS: 'come-funziona',
  SERVICES:     'servizi',
  ABOUT:        'chi-siamo',
  CONTACT:             'contatti',
  CLINIC_REGISTRATION: 'registra-clinica',
  PRIVACY_POLICY:      'privacy-policy',
  TERMS:               'termini-utilizzo',
  COOKIE_POLICY:       'cookie-policy',
  LOGIN:               'login',
  REGISTER:     'register',

  // =========================================================================
  // Prefissi di ruolo (usati come parent path nei child routes)
  // =========================================================================
  PATIENT:      'patient',
  DOCTOR:       'doctor',
  RECEPTIONIST: 'receptionist',
  ADMIN:        'admin',

  // =========================================================================
  // Segmenti comuni (concatenati al prefisso ruolo)
  // =========================================================================
  CONSENT:      'consent',
  DASHBOARD:    'dashboard',
  APPOINTMENTS: 'appointments',
  PROFILE:      'profile',
  NOTIFICATIONS:'notifications',
  AVAILABILITY: 'availability',

  // =========================================================================
  // Segmenti riservati ad admin / receptionist / clinic
  // =========================================================================
  PATIENTS:     'patients',
  DOCTORS:      'doctors',
  CLINICS:      'clinics',
  CLINIC:       'clinic',
  SETTINGS:     'settings',

  // =========================================================================
  // Percorsi completi calcolati — usati nei router.navigate e routerLink
  // =========================================================================
  PATIENT_DASHBOARD:      '/patient/dashboard',
  PATIENT_APPOINTMENTS:   '/patient/appointments',
  PATIENT_PROFILE:        '/patient/profile',
  PATIENT_NOTIFICATIONS:  '/patient/notifications',
  PATIENT_AVAILABILITY:   '/patient/availability',

  DOCTOR_CONSENT:         '/doctor/consent',
  DOCTOR_DASHBOARD:       '/doctor/dashboard',
  DOCTOR_APPOINTMENTS:    '/doctor/appointments',
  DOCTOR_PROFILE:         '/doctor/profile',
  DOCTOR_NOTIFICATIONS:   '/doctor/notifications',

  RECEPTIONIST_DASHBOARD:    '/receptionist/dashboard',
  RECEPTIONIST_APPOINTMENTS: '/receptionist/appointments',
  RECEPTIONIST_PATIENTS:     '/receptionist/patients',
  RECEPTIONIST_AVAILABILITY: '/receptionist/availability',

  CLINIC_DASHBOARD:     '/clinic/dashboard',
  CLINIC_DOCTORS:       '/clinic/doctors',
  CLINIC_APPOINTMENTS:  '/clinic/appointments',
  CLINIC_PROFILE:       '/clinic/profile',
  CLINIC_SETTINGS:      '/clinic/settings',

  ADMIN_DASHBOARD:      '/admin/dashboard',
  ADMIN_APPOINTMENTS:   '/admin/appointments',
  ADMIN_PATIENTS:       '/admin/patients',
  ADMIN_DOCTORS:        '/admin/doctors',
  ADMIN_CLINICS:        '/admin/clinics',
  ADMIN_NOTIFICATIONS:  '/admin/notifications',
} as const;
