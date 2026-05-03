import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService } from './appointment.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.APPOINTMENTS;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ---------------------------------------------------------------------------
  // Lettura appuntamenti
  // ---------------------------------------------------------------------------

  it('getAll() dovrebbe fare GET all\'URL base senza parametri', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i filtri come query params', () => {
    service.getAll({ status: 'PRENOTATO', dateFrom: '2026-04-01' }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('PRENOTATO');
    expect(req.request.params.get('dateFrom')).toBe('2026-04-01');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /appointments/{id}', () => {
    service.getById('APT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/APT-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  // ---------------------------------------------------------------------------
  // Prenotazione
  // ---------------------------------------------------------------------------

  it('book() dovrebbe fare POST all\'URL base con i dati dello slot', () => {
    const slot = { slotId: 'SLT-001', doctorId: 'DOC-001' };
    service.book(slot).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(slot);
    req.flush({});
  });

  // ---------------------------------------------------------------------------
  // Transizioni di stato
  // ---------------------------------------------------------------------------

  it('cancel() dovrebbe fare PATCH su /appointments/{id}/cancel con motivo e canali', () => {
    const body = {
      cancellationReason: 'Impegno personale',
      notificationChannels: ['EMAIL'],
    };
    service.cancel('APT-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/APT-001/cancel`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(null);
  });

  it('start() dovrebbe fare PATCH su /appointments/{id}/start con body vuoto', () => {
    service.start('APT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/APT-001/start`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });

  // ---------------------------------------------------------------------------
  // Trigger manuali ADMIN
  // ---------------------------------------------------------------------------

  it('triggerCloseDay() dovrebbe fare POST su /appointments/jobs/close-day', () => {
    service.triggerCloseDay().subscribe();
    const req = httpMock.expectOne(`${BASE}/jobs/close-day`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });

  it('triggerReminders() dovrebbe fare POST su /appointments/jobs/reminders', () => {
    service.triggerReminders().subscribe();
    const req = httpMock.expectOne(`${BASE}/jobs/reminders`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });
});
