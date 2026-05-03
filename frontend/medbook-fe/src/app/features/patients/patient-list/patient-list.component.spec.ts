import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { PatientListComponent } from './patient-list.component';
import { PatientService } from '../../../core/services/patient.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('PatientListComponent', () => {
  let component: PatientListComponent;
  let fixture: ComponentFixture<PatientListComponent>;

  const patientServiceMock = {
    getAll: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn()
  };

  const keycloakMock = {
    hasRole: vi.fn().mockReturnValue(true),
    getPatientsRoute: vi.fn().mockReturnValue('/admin/patients')
  };

  const authServiceMock = {
    sendResetPasswordEmail: vi.fn()
  };

  const routerMock = { navigate: vi.fn() };
  const dialogMock = { open: vi.fn() };
  const snackBarMock = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();

    keycloakMock.hasRole.mockReturnValue(true);
    keycloakMock.getPatientsRoute.mockReturnValue('/admin/patients');

    await TestBed.configureTestingModule({
      imports: [PatientListComponent, NoopAnimationsModule]
    })
    .overrideComponent(PatientListComponent, {
      set: {
        providers: [
          { provide: PatientService, useValue: patientServiceMock },
          { provide: KeycloakService, useValue: keycloakMock },
          { provide: AuthService, useValue: authServiceMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock },
          { provide: MatSnackBar, useValue: snackBarMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should NOT load patients on init (requires filters)', () => {
    fixture.detectChanges();

    expect(patientServiceMock.getAll).not.toHaveBeenCalled();
    expect(component['searchPerformed']()).toBe(false);
  });

  it('should call loadPatients on applyFilters when filters are set', () => {
    fixture.detectChanges();

    const mockResponse = {
      data: [{ patientId: '1', firstName: 'Mario', lastName: 'Rossi', email: 'mario@test.it' }],
      page: { totalElements: 1 }
    };
    patientServiceMock.getAll.mockReturnValue(of(mockResponse));

    component['filterForm'].patchValue({ lastName: 'Rossi' });
    component['applyFilters']();

    expect(patientServiceMock.getAll).toHaveBeenCalled();
    const callArgs = patientServiceMock.getAll.mock.calls[patientServiceMock.getAll.mock.calls.length - 1][0] as Record<string, unknown>;
    expect(callArgs['lastName']).toBe('Rossi');
    expect(callArgs['page']).toBe(0);
  });

  it('should NOT call loadPatients on applyFilters when no filters are set', () => {
    fixture.detectChanges();

    component['applyFilters']();

    expect(patientServiceMock.getAll).not.toHaveBeenCalled();
  });

  it('should enrich patient data with _fullName after loading', () => {
    fixture.detectChanges();

    const mockResponse = {
      data: [{ patientId: '1', firstName: 'Mario', lastName: 'Rossi' }],
      page: { totalElements: 1 }
    };
    patientServiceMock.getAll.mockReturnValue(of(mockResponse));

    component['filterForm'].patchValue({ lastName: 'Rossi' });
    component['applyFilters']();

    const patients = component['patients']();
    expect(patients.length).toBe(1);
    // Il nome completo viene costruito da formatFullName
    const row = patients[0] as Record<string, unknown>;
    expect(row['_fullName']).toBeTruthy();
  });

  it('should reset filters and clear data on resetFilters', () => {
    fixture.detectChanges();

    component['filterForm'].patchValue({ lastName: 'Rossi', firstName: 'Mario' });
    component['resetFilters']();

    expect(component['filterForm'].get('lastName')?.value).toBe('');
    expect(component['filterForm'].get('firstName')?.value).toBe('');
    expect(component['patients']()).toEqual([]);
    expect(component['totalElements']()).toBe(0);
    expect(component['searchPerformed']()).toBe(false);
  });

  it('should open error dialog when loadPatients fails', () => {
    fixture.detectChanges();

    patientServiceMock.getAll.mockReturnValue(throwError(() => new Error('Server error')));

    component['filterForm'].patchValue({ lastName: 'Test' });
    component['applyFilters']();

    expect(dialogMock.open).toHaveBeenCalled();
  });
});
