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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DoctorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll() dovrebbe fare GET all\'URL base', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /doctors/{id}', () => {
    service.getById('DOC-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('create() dovrebbe fare POST all\'URL base', () => {
    const body = { firstName: 'Laura', lastName: 'Bianchi' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('update() dovrebbe fare PATCH su /doctors/{id}', () => {
    service.update('DOC-001', { phone: '0612345' }).subscribe();
    const req = httpMock.expectOne(`${BASE}/DOC-001`);
    expect(req.request.method).toBe('PATCH');
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

  // --- Disponibilità ---

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

  it('deleteAvailability() dovrebbe fare DELETE su /doctors/{id}/availabilities con query params', () => {
    service.deleteAvailability('DOC-001', 'CLN-001', 'MONDAY', '09:00').subscribe();
    const req = httpMock.expectOne(r =>
      r.url === `${BASE}/DOC-001/availabilities` && r.method === 'DELETE'
    );
    expect(req.request.params.get('clinicId')).toBe('CLN-001');
    expect(req.request.params.get('dayOfWeek')).toBe('MONDAY');
    expect(req.request.params.get('startTime')).toBe('09:00');
    req.flush(null);
  });
});
