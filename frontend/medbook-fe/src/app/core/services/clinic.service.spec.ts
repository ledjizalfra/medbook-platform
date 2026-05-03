import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ClinicService } from './clinic.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('ClinicService', () => {
  let service: ClinicService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.CLINICS;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClinicService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll() dovrebbe fare GET all\'URL base senza parametri', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i parametri come query string', () => {
    service.getAll({ search: 'Roma', page: 0 }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('search')).toBe('Roma');
    expect(req.request.params.get('page')).toBe('0');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /clinics/{id}', () => {
    service.getById('CLN-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('create() dovrebbe fare POST all\'URL base con il body', () => {
    const body = { name: 'Poliambulatorio Roma', city: 'Roma' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('update() dovrebbe fare PATCH su /clinics/{id} con il body', () => {
    const body = { phone: '0612345' };
    service.update('CLN-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(null);
  });

  it('delete() dovrebbe fare DELETE su /clinics/{id}', () => {
    service.delete('CLN-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('restore() dovrebbe fare PATCH su /clinics/{id}/restore', () => {
    service.restore('CLN-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001/restore`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });
});
