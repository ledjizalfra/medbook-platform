import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ReceptionistService } from './receptionist.service';
import { environment } from '../../../environments/environment';

describe('ReceptionistService', () => {
  let service: ReceptionistService;
  let httpMock: HttpTestingController;
  const base = environment.apiBaseUrl + '/receptionists';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ReceptionistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll deve fare GET con params', () => {
    service.getAll({ page: 0, size: 10 }).subscribe();
    const req = httpMock.expectOne(r => r.url === base && r.method === 'GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ data: { content: [] } });
  });

  it('getById deve fare GET con keycloakId', () => {
    service.getById('abc-123').subscribe();
    const req = httpMock.expectOne(`${base}/abc-123`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: {} });
  });

  it('create deve fare POST con body', () => {
    const body = { email: 'rec@test.it', firstName: 'Anna', lastName: 'Verdi', password: 'Test1234!' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ data: { keycloakId: 'new-id' } });
  });

  it('update deve fare PUT con keycloakId e body', () => {
    const body = { firstName: 'Updated' };
    service.update('abc-123', body).subscribe();
    const req = httpMock.expectOne(`${base}/abc-123`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
    req.flush({ success: true });
  });

  it('delete deve fare DELETE con keycloakId', () => {
    service.delete('abc-123').subscribe();
    const req = httpMock.expectOne(`${base}/abc-123`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ success: true });
  });
});
