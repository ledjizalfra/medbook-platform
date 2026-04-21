import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PatientService } from './patient.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('PatientService', () => {
  let service: PatientService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.PATIENTS;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PatientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll() dovrebbe fare GET all\'URL base', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i parametri come query string', () => {
    service.getAll({ search: 'rossi', page: 0 }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.params.get('search')).toBe('rossi');
    expect(req.request.params.get('page')).toBe('0');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /patients/{id}', () => {
    service.getById('PAT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/PAT-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('create() dovrebbe fare POST all\'URL base con il body', () => {
    const body = { firstName: 'Mario', lastName: 'Rossi' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('update() dovrebbe fare PATCH su /patients/{id} con il body', () => {
    const body = { phone: '3331234567' };
    service.update('PAT-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/PAT-001`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(null);
  });

  it('delete() dovrebbe fare DELETE su /patients/{id}', () => {
    service.delete('PAT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/PAT-001`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('restore() dovrebbe fare PATCH su /patients/{id}/restore', () => {
    service.restore('PAT-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/PAT-001/restore`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });
});
