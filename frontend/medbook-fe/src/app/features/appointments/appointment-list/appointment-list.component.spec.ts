import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AppointmentListComponent } from './appointment-list.component';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PatientService } from '../../../core/services/patient.service';
import { SpecializationService } from '../../../core/services/specialization.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('AppointmentListComponent', () => {
  let component: AppointmentListComponent;
  let fixture: ComponentFixture<AppointmentListComponent>;

  const mockClinics = { data: [{ clinicId: 'C1', name: 'Clinica Sole' }], page: {} };
  const mockDoctors = { data: [{ doctorId: 'D1', doctorFullName: 'Dr. Rossi' }], page: {} };
  const mockPatients = { data: [{ patientId: 'P1', firstName: 'Mario', lastName: 'Bianchi' }], page: {} };
  const mockSpecs = { data: { specializations: [{ name: 'CARDIOLOGIA' }] } };

  const mockAppointments = {
    data: [
      { appointmentId: 'A1', patientId: 'P1', doctorId: 'D1', clinicId: 'C1', specialization: 'CARDIOLOGIA', slotDate: '2025-06-01', startTime: '09:00', status: 'PRENOTATO' }
    ],
    page: { totalElements: 1 }
  };

  const appointmentServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockAppointments)),
    cancel: vi.fn()
  };

  const clinicServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockClinics))
  };

  const doctorServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockDoctors))
  };

  const patientServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockPatients))
  };

  const specializationServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockSpecs))
  };

  const keycloakMock = {
    hasRole: vi.fn().mockReturnValue(true),
    getAppointmentDetailRoute: vi.fn()
  };

  const routerMock = { navigate: vi.fn(), navigateByUrl: vi.fn() };
  const dialogMock = { open: vi.fn() };
  const snackBarMock = { open: vi.fn() };

  beforeEach(async () => {
    // Reset all mocks before each test
    vi.clearAllMocks();

    // Restore default return values
    clinicServiceMock.getAll.mockReturnValue(of(mockClinics));
    doctorServiceMock.getAll.mockReturnValue(of(mockDoctors));
    patientServiceMock.getAll.mockReturnValue(of(mockPatients));
    specializationServiceMock.getAll.mockReturnValue(of(mockSpecs));
    appointmentServiceMock.getAll.mockReturnValue(of(mockAppointments));
    keycloakMock.hasRole.mockReturnValue(true);

    await TestBed.configureTestingModule({
      imports: [AppointmentListComponent, NoopAnimationsModule]
    })
    .overrideComponent(AppointmentListComponent, {
      set: {
        providers: [
          { provide: AppointmentService, useValue: appointmentServiceMock },
          { provide: ClinicService, useValue: clinicServiceMock },
          { provide: DoctorService, useValue: doctorServiceMock },
          { provide: PatientService, useValue: patientServiceMock },
          { provide: SpecializationService, useValue: specializationServiceMock },
          { provide: KeycloakService, useValue: keycloakMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock },
          { provide: MatSnackBar, useValue: snackBarMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppointmentListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load reference data on init via forkJoin', () => {
    fixture.detectChanges();

    expect(clinicServiceMock.getAll).toHaveBeenCalled();
    expect(doctorServiceMock.getAll).toHaveBeenCalled();
    expect(patientServiceMock.getAll).toHaveBeenCalled();
    expect(specializationServiceMock.getAll).toHaveBeenCalled();
  });

  it('should load appointments after reference data is loaded', () => {
    fixture.detectChanges();

    expect(appointmentServiceMock.getAll).toHaveBeenCalled();
    expect(component['loading']()).toBe(false);
  });

  it('should enrich appointments with resolved names', () => {
    fixture.detectChanges();

    const list = component['appointments']();
    expect(list.length).toBe(1);
    const row = list[0] as Record<string, unknown>;
    expect(row['_clinicName']).toBe('Clinica Sole');
    expect(row['_doctorName']).toBe('Dr. Rossi');
    expect(row['_patientName']).toBeTruthy();
  });

  it('should populate clinics signal with reference data', () => {
    fixture.detectChanges();

    const clinics = component['clinics']();
    expect(clinics.length).toBe(1);
  });

  it('should open error dialog when forkJoin fails', () => {
    clinicServiceMock.getAll.mockReturnValue(throwError(() => new Error('Network error')));

    fixture.detectChanges();

    expect(dialogMock.open).toHaveBeenCalled();
    expect(component['loading']()).toBe(false);
  });

  it('should reset page index on applyFilters', () => {
    fixture.detectChanges();
    appointmentServiceMock.getAll.mockClear();

    component['pageIndex'].set(2);
    component['applyFilters']();

    expect(component['pageIndex']()).toBe(0);
    expect(appointmentServiceMock.getAll).toHaveBeenCalled();
  });
});
