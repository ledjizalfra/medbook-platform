import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { DoctorListComponent } from './doctor-list.component';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { SpecializationService } from '../../../core/services/specialization.service';
import { SpecializationStore } from '../../../core/store/specialization.store';
import { ClinicStore } from '../../../core/store/clinic.store';
import { DoctorStore } from '../../../core/store/doctor.store';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('DoctorListComponent', () => {
  let component: DoctorListComponent;
  let fixture: ComponentFixture<DoctorListComponent>;

  const doctorServiceMock = {
    getAll: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn()
  };

  const clinicServiceMock = {
    getAll: vi.fn()
  };

  const specializationServiceMock = {
    getAll: vi.fn()
  };

  const specStoreMock = {
    loadAll: vi.fn().mockReturnValue(of([]))
  };

  const clinicStoreMock = {
    loadAll: vi.fn().mockReturnValue(of([]))
  };

  const doctorStoreMock = {
    loadAll: vi.fn().mockReturnValue(of([])),
    invalidate: vi.fn()
  };

  const keycloakMock = {
    hasRole: vi.fn().mockReturnValue(true),
    getDoctorsRoute: vi.fn().mockReturnValue('/admin/doctors')
  };

  const authServiceMock = {
    sendResetPasswordEmail: vi.fn()
  };

  const routerMock = { navigate: vi.fn() };
  const dialogMock = { open: vi.fn() };
  const snackBarMock = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();

    // Restore default return values
    specStoreMock.loadAll.mockReturnValue(of([]));
    clinicStoreMock.loadAll.mockReturnValue(of([]));
    doctorStoreMock.loadAll.mockReturnValue(of([]));
    keycloakMock.hasRole.mockReturnValue(true);
    keycloakMock.getDoctorsRoute.mockReturnValue('/admin/doctors');

    await TestBed.configureTestingModule({
      imports: [DoctorListComponent, NoopAnimationsModule]
    })
    .overrideComponent(DoctorListComponent, {
      set: {
        providers: [
          { provide: DoctorService, useValue: doctorServiceMock },
          { provide: ClinicService, useValue: clinicServiceMock },
          { provide: SpecializationService, useValue: specializationServiceMock },
          { provide: SpecializationStore, useValue: specStoreMock },
          { provide: ClinicStore, useValue: clinicStoreMock },
          { provide: DoctorStore, useValue: doctorStoreMock },
          { provide: KeycloakService, useValue: keycloakMock },
          { provide: AuthService, useValue: authServiceMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock },
          { provide: MatSnackBar, useValue: snackBarMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoctorListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load specializations on init via SpecializationStore', () => {
    const mockSpecs = [{ name: 'CARDIOLOGIA' }, { name: 'DERMATOLOGIA' }];
    specStoreMock.loadAll.mockReturnValue(of(mockSpecs));

    fixture.detectChanges();

    expect(specStoreMock.loadAll).toHaveBeenCalled();
  });

  it('should check clinics and doctors workflow on init', () => {
    clinicStoreMock.loadAll.mockReturnValue(of([{ clinicId: '1' }]));
    doctorStoreMock.loadAll.mockReturnValue(of([{ doctorId: '1' }]));

    fixture.detectChanges();

    expect(clinicStoreMock.loadAll).toHaveBeenCalled();
    expect(doctorStoreMock.loadAll).toHaveBeenCalled();
  });

  it('should call loadDoctors on applyFilters when filters are set', () => {
    fixture.detectChanges();

    const mockResponse = { data: [{ doctorId: '1', doctorFullName: 'Dr. Rossi', specializations: [] }], page: { totalElements: 1 } };
    doctorServiceMock.getAll.mockReturnValue(of(mockResponse));

    // Imposta un filtro per permettere la ricerca
    component['filterForm'].patchValue({ lastName: 'Rossi' });
    component['applyFilters']();

    expect(doctorServiceMock.getAll).toHaveBeenCalled();
    const callArgs = doctorServiceMock.getAll.mock.calls[doctorServiceMock.getAll.mock.calls.length - 1][0] as Record<string, unknown>;
    expect(callArgs['lastName']).toBe('Rossi');
    expect(callArgs['page']).toBe(0);
  });

  it('should NOT call loadDoctors on applyFilters when no filters are set', () => {
    fixture.detectChanges();

    component['applyFilters']();

    expect(doctorServiceMock.getAll).not.toHaveBeenCalled();
  });

  it('should reset filters and clear data on resetFilters', () => {
    fixture.detectChanges();

    // Prima imposta dei dati
    component['filterForm'].patchValue({ lastName: 'Rossi' });

    component['resetFilters']();

    expect(component['filterForm'].get('lastName')?.value).toBe('');
    expect(component['doctors']()).toEqual([]);
    expect(component['totalElements']()).toBe(0);
    expect(component['searchPerformed']()).toBe(false);
  });

  it('should navigate to new doctor page on newDoctor()', () => {
    fixture.detectChanges();

    component['newDoctor']();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/admin/doctors', 'new']);
  });

  it('should open error dialog when loadDoctors fails', () => {
    fixture.detectChanges();

    doctorServiceMock.getAll.mockReturnValue(throwError(() => new Error('Server error')));

    component['filterForm'].patchValue({ lastName: 'Test' });
    component['applyFilters']();

    expect(dialogMock.open).toHaveBeenCalled();
  });
});
