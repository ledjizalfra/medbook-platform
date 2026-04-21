import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SpecializationService } from './specialization.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../constants/api-endpoints';

describe('SpecializationService', () => {
  let service: SpecializationService;
  let httpMock: HttpTestingController;
  const BASE = environment.apiBaseUrl + API_ENDPOINTS.SPECIALIZATIONS;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SpecializationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll() dovrebbe fare GET all\'URL base senza parametri', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe restituire un Observable', () => {
    const result = service.getAll();
    // Verifica che il risultato sia un Observable sottoscrivibile
    expect(typeof result.subscribe).toBe('function');
    httpMock.expectOne(BASE).flush([]);
  });
});
