import { TestBed } from '@angular/core/testing';
import { UserContextService } from './user-context.service';

describe('UserContextService', () => {
  let service: UserContextService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserContextService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // TODO: test loadProfile() per ruolo DOCTOR
  // TODO: test loadProfile() per ruolo PATIENT
  // TODO: test getDoctorTitle() con gender FEMMINA → 'Dott.ssa'
  // TODO: test getDoctorTitle() con gender MASCHIO → 'Dott.'
  // TODO: test getDoctorTitle() con profilo null → fallback 'Dott.'
});
