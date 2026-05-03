import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { ClinicListComponent } from './clinic-list.component';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { ClinicStore } from '../../../core/store/clinic.store';
import { DoctorStore } from '../../../core/store/doctor.store';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('ClinicListComponent', () => {
  let component: ClinicListComponent;
  let fixture: ComponentFixture<ClinicListComponent>;

  const clinicServiceMock = {
    getAll: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn()
  };

  const doctorServiceMock = {
    getAll: vi.fn()
  };

  const clinicStoreMock = {
    loadAll: vi.fn(),
    invalidate: vi.fn()
  };

  const doctorStoreMock = {
    loadAll: vi.fn().mockReturnValue(of([]))
  };

  const keycloakMock = {
    hasRole: vi.fn().mockReturnValue(true),
    getClinicsRoute: vi.fn().mockReturnValue('/admin/clinics')
  };

  const routerMock = { navigate: vi.fn() };
  const dialogMock = { open: vi.fn() };
  const snackBarMock = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();

    // Restore default return values
    doctorStoreMock.loadAll.mockReturnValue(of([]));
    keycloakMock.hasRole.mockReturnValue(true);
    keycloakMock.getClinicsRoute.mockReturnValue('/admin/clinics');

    await TestBed.configureTestingModule({
      imports: [ClinicListComponent, NoopAnimationsModule]
    })
    .overrideComponent(ClinicListComponent, {
      set: {
        providers: [
          { provide: ClinicService, useValue: clinicServiceMock },
          { provide: DoctorService, useValue: doctorServiceMock },
          { provide: ClinicStore, useValue: clinicStoreMock },
          { provide: DoctorStore, useValue: doctorStoreMock },
          { provide: KeycloakService, useValue: keycloakMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock },
          { provide: MatSnackBar, useValue: snackBarMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClinicListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should check doctors workflow on init', () => {
    doctorStoreMock.loadAll.mockReturnValue(of([{ doctorId: '1' }]));

    fixture.detectChanges();

    expect(doctorStoreMock.loadAll).toHaveBeenCalled();
  });

  it('should set hasDoctors to true when doctors exist', () => {
    doctorStoreMock.loadAll.mockReturnValue(of([{ doctorId: 'D1' }]));

    fixture.detectChanges();

    expect(component['hasDoctors']()).toBe(true);
    expect(component['checkingWorkflow']()).toBe(false);
  });

  it('should call loadClinics on applyFilters when filters are set', () => {
    fixture.detectChanges();

    const mockResponse = { data: [{ clinicId: '1', name: 'Clinica Rossi' }], page: { totalElements: 1 } };
    clinicServiceMock.getAll.mockReturnValue(of(mockResponse));

    component['filterForm'].patchValue({ name: 'Rossi' });
    component['applyFilters']();

    expect(clinicServiceMock.getAll).toHaveBeenCalled();
    const callArgs = clinicServiceMock.getAll.mock.calls[clinicServiceMock.getAll.mock.calls.length - 1][0] as Record<string, unknown>;
    expect(callArgs['name']).toBe('Rossi');
    expect(callArgs['page']).toBe(0);
  });

  it('should NOT call loadClinics on applyFilters when no filters are set', () => {
    fixture.detectChanges();

    component['applyFilters']();

    expect(clinicServiceMock.getAll).not.toHaveBeenCalled();
  });

  it('should navigate to new clinic page on newClinic()', () => {
    fixture.detectChanges();

    component['newClinic']();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/admin/clinics', 'new']);
  });

  it('should reset filters and clear data on resetFilters', () => {
    fixture.detectChanges();

    component['filterForm'].patchValue({ name: 'Test', city: 'Roma' });
    component['resetFilters']();

    expect(component['filterForm'].get('name')?.value).toBe('');
    expect(component['filterForm'].get('city')?.value).toBe('');
    expect(component['clinics']()).toEqual([]);
    expect(component['totalElements']()).toBe(0);
    expect(component['searchPerformed']()).toBe(false);
  });

  it('should open error dialog when loadClinics fails', () => {
    fixture.detectChanges();

    clinicServiceMock.getAll.mockReturnValue(throwError(() => new Error('Server error')));

    component['filterForm'].patchValue({ name: 'Test' });
    component['applyFilters']();

    expect(dialogMock.open).toHaveBeenCalled();
  });
});
