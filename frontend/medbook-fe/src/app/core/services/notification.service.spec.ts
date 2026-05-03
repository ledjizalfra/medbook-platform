import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.NOTIFICATIONS;
  const PREFS_BASE = environment.apiBaseUrl + API_ENDPOINTS.NOTIFICATION_PREFERENCES;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ---------------------------------------------------------------------------
  // Notifiche
  // ---------------------------------------------------------------------------

  it('getAll() dovrebbe fare GET all\'URL base senza parametri', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i filtri opzionali come query params', () => {
    service.getAll({ status: 'INVIATA', channel: 'EMAIL' }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('INVIATA');
    expect(req.request.params.get('channel')).toBe('EMAIL');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /notifications/{id}', () => {
    service.getById('NOT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/NOT-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('retry() dovrebbe fare POST su /notifications/{id}/retry', () => {
    service.retry('NOT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/NOT-001/retry`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });

  // ---------------------------------------------------------------------------
  // Preferenze di notifica
  // ---------------------------------------------------------------------------

  it('getMyPreferences() dovrebbe fare GET su /notification-preferences', () => {
    service.getMyPreferences().subscribe();
    const req = httpMock.expectOne(PREFS_BASE);
    expect(req.request.method).toBe('GET');
    req.flush({ emailEnabled: true, smsEnabled: false });
  });

  it('updateMyPreferences() dovrebbe fare PATCH su /notification-preferences con il body', () => {
    service.updateMyPreferences(true, false).subscribe();
    const req = httpMock.expectOne(PREFS_BASE);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ emailEnabled: true, smsEnabled: false });
    req.flush(null);
  });
});
