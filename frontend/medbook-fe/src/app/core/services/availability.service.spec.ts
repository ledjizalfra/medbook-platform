import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AvailabilityService } from './availability.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('AvailabilityService', () => {
  let service: AvailabilityService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.AVAILABILITY;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AvailabilityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('search() dovrebbe fare GET all\'URL base senza filtri', () => {
    service.search({}).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('search() dovrebbe passare tutti i filtri come query params', () => {
    service.search({
      clinicId: 'CLN-001',
      specialization: 'CARDIOLOGIA',
      doctorId: 'DOC-001',
      dateFrom: '2026-04-01',
      dateTo: '2026-04-30',
    }).subscribe();

    const req = httpMock.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('clinicId')).toBe('CLN-001');
    expect(req.request.params.get('specialization')).toBe('CARDIOLOGIA');
    expect(req.request.params.get('doctorId')).toBe('DOC-001');
    expect(req.request.params.get('dateFrom')).toBe('2026-04-01');
    expect(req.request.params.get('dateTo')).toBe('2026-04-30');
    req.flush([]);
  });

  it('getFilters() dovrebbe fare GET su /availability/filters', () => {
    service.getFilters().subscribe();
    const req = httpMock.expectOne(`${BASE}/filters`);
    expect(req.request.method).toBe('GET');
    req.flush({ specializations: [], doctors: [] });
  });
});
