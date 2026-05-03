import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { DoctorService } from './doctor.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('DoctorService', () => {
  let service: DoctorService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS;
  const ME_URL = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME;
  const CONSENT_STATUS_URL = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME_CONSENT_STATUS;
  const CONSENT_URL = environment.apiBaseUrl + API_ENDPOINTS.DOCTORS_ME_CONSENT;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DoctorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ---------------------------------------------------------------------------
  // Profilo medico autenticato
  // ---------------------------------------------------------------------------

  it('getMe() dovrebbe fare GET su /doctors/me', () => {
    service.getMe().subscribe();
    const req = httpMock.expectOne(ME_URL);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 'DOC-001', firstName: 'Laura' });
  });

  // ---------------------------------------------------------------------------
  // CRUD medici
  // ---------------------------------------------------------------------------

  it('getAll() dovrebbe fare GET all\'URL base senza parametri', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i parametri come query string', () => {
    service.getAll({ specialization: 'CARDIOLOGIA', page: 0 }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('specialization')).toBe('CARDIOLOGIA');
    expect(req.request.params.get('page')).toBe('0');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /doctors/{id}', () => {
    service.getById('DOC-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('create() dovrebbe fare POST all\'URL base con il body', () => {
    const body = { firstName: 'Laura', lastName: 'Bianchi' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('update() dovrebbe fare PATCH su /doctors/{id} con il body', () => {
    const body = { phone: '0612345' };
    service.update('DOC-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(null);
  });

  it('delete() dovrebbe fare DELETE su /doctors/{id}', () => {
    service.delete('DOC-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('restore() dovrebbe fare PATCH su /doctors/{id}/restore', () => {
    service.restore('DOC-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001/restore`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  // ---------------------------------------------------------------------------
  // Consensi
  // ---------------------------------------------------------------------------

  it('getConsentStatus() dovrebbe fare GET su /doctors/me/consent-status', () => {
    service.getConsentStatus().subscribe();
    const req = httpMock.expectOne(CONSENT_STATUS_URL);
    expect(req.request.method).toBe('GET');
    req.flush({ privacyConsentAccepted: true, marketingConsentAccepted: false });
  });

  it('acceptConsent() dovrebbe fare POST su /doctors/me/consent con il body', () => {
    const body = { privacyConsentAccepted: true, marketingConsentAccepted: false };
    service.acceptConsent(body).subscribe();
    const req = httpMock.expectOne(CONSENT_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('updateConsent() dovrebbe fare PATCH su /doctors/me/consent con il body', () => {
    const body = { marketingConsentAccepted: true };
    service.updateConsent(body).subscribe();
    const req = httpMock.expectOne(CONSENT_URL);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  // ---------------------------------------------------------------------------
  // Disponibilita
  // ---------------------------------------------------------------------------

  it('getAvailabilities() dovrebbe fare GET su /doctors/{id}/availabilities', () => {
    service.getAvailabilities('DOC-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001/availabilities`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('createAvailability() dovrebbe fare POST su /doctors/{id}/availabilities', () => {
    const body = { dayOfWeek: 'MONDAY', startTime: '09:00', clinicId: 'CLN-001' };
    service.createAvailability('DOC-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001/availabilities`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('deleteAvailability() dovrebbe fare DELETE su /doctors/{id}/availabilities/{clinicId}/{day}/{time}', () => {
    service.deleteAvailability('DOC-001', 'CLN-001', 'MONDAY', '09:00').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001/availabilities/CLN-001/MONDAY/09:00`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
