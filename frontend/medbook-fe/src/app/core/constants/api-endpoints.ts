/**
 * URL base dei microservizi esposti dal BFF (Backend For Frontend).
 *
 * Tutti i servizi Angular concatenano questi path con `environment.apiBaseUrl`.
 * Centralizzati qui per facilitare aggiornamenti di versione (es. v1 => v2)
 * o cambi di prefisso senza dover toccare ogni singolo service.
 */
export const API_ENDPOINTS = {
  APPOINTMENTS:             '/bff/v1/appointments',
  PATIENTS:                 '/bff/v1/patients',
  PATIENTS_ME:              '/bff/v1/patients/me',
  DOCTORS:                  '/bff/v1/doctors',
  DOCTORS_ME:               '/bff/v1/doctors/me',
  DOCTORS_ME_CONSENT_STATUS:'/bff/v1/doctors/me/consent-status',
  DOCTORS_ME_CONSENT:       '/bff/v1/doctors/me/consent',
  CLINICS:                  '/bff/v1/clinics',
  SPECIALIZATIONS:          '/bff/v1/specializations',
  NOTIFICATIONS:            '/bff/v1/notifications',
  NOTIFICATION_PREFERENCES: '/bff/v1/notification-preferences',
  AVAILABILITY:             '/bff/v1/availability',
} as const;
