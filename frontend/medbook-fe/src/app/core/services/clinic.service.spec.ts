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

  it('getAll() dovrebbe fare GET all\'URL base', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getById() dovrebbe fare GET su /clinics/{id}', () => {
    service.getById('CLN-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('create() dovrebbe fare POST all\'URL base', () => {
    const body = { name: 'Poliambulatorio Roma', city: 'Roma' };
    service.create(body).subscribe();
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('update() dovrebbe fare PATCH su /clinics/{id}', () => {
    service.update('CLN-001', { phone: '0612345' }).subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001`);
    expect(req.request.method).toBe('PATCH');
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

  // --- Assegnazioni ---

  it('getAssignments() dovrebbe fare GET su /clinics/{id}/assignments', () => {
    service.getAssignments('CLN-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001/assignments`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('createAssignment() dovrebbe fare POST su /clinics/{id}/assignments', () => {
    const body = { doctorId: 'DOC-001', specialization: 'CARDIOLOGIA' };
    service.createAssignment('CLN-001', body).subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001/assignments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('updateAssignment() dovrebbe fare PATCH su /clinics/{clinicId}/assignments/{assignmentId}', () => {
    service.updateAssignment('CLN-001', 'ASG-001', { specialization: 'NEUROLOGIA' }).subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001/assignments/ASG-001`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  it('deleteAssignment() dovrebbe fare DELETE su /clinics/{clinicId}/assignments/{assignmentId}', () => {
    service.deleteAssignment('CLN-001', 'ASG-001').subscribe();
    const req = httpMock.expectOne(`${BASE}/CLN-001/assignments/ASG-001`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
