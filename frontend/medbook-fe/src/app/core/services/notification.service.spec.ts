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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll() dovrebbe fare GET all\'URL base', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getAll() dovrebbe passare i filtri opzionali come query params', () => {
    service.getAll({ status: 'INVIATA', channel: 'EMAIL' }).subscribe();
    const req = httpMock.expectOne(r => r.url === BASE);
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
});
