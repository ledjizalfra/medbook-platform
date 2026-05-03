import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { AvailabilitySearchComponent } from './availability-search.component';
import { AvailabilityService } from '../../../core/services/availability.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

describe('AvailabilitySearchComponent', () => {
  let component: AvailabilitySearchComponent;
  let fixture: ComponentFixture<AvailabilitySearchComponent>;

  const mockFilters = {
    data: {
      specializations: ['CARDIOLOGIA', 'DERMATOLOGIA'],
      doctors: [
        { doctorId: 'D1', fullName: 'Dr. Rossi', specializations: ['CARDIOLOGIA'], clinicIds: ['C1'] },
        { doctorId: 'D2', fullName: 'Dr. Bianchi', specializations: ['DERMATOLOGIA'], clinicIds: ['C2'] }
      ],
      clinics: [
        { clinicId: 'C1', name: 'Clinica Sole', city: 'Roma', province: 'RM' },
        { clinicId: 'C2', name: 'Clinica Luna', city: 'Milano', province: 'MI' }
      ]
    }
  };

  const mockSearchResults = {
    data: [
      { slotDate: '2025-06-01', startTime: '09:00', doctorFullName: 'Dr. Rossi', clinicName: 'Clinica Sole', clinicProvince: 'RM', clinicCity: 'Roma', specialization: 'CARDIOLOGIA', status: 'LIBERO' }
    ],
    page: { totalElements: 1 }
  };

  const availabilityServiceMock = {
    search: vi.fn(),
    getFilters: vi.fn().mockReturnValue(of(mockFilters))
  };

  const appointmentServiceMock = {
    book: vi.fn()
  };

  const routerMock = { navigate: vi.fn() };
  const dialogMock = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();

    // Restore default return values
    availabilityServiceMock.getFilters.mockReturnValue(of(mockFilters));

    await TestBed.configureTestingModule({
      imports: [AvailabilitySearchComponent, NoopAnimationsModule]
    })
    .overrideComponent(AvailabilitySearchComponent, {
      set: {
        providers: [
          { provide: AvailabilityService, useValue: availabilityServiceMock },
          { provide: AppointmentService, useValue: appointmentServiceMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(AvailabilitySearchComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load reference data (filters) on construction', () => {
    fixture.detectChanges();

    expect(availabilityServiceMock.getFilters).toHaveBeenCalled();
    expect(component['specializations']()).toEqual(['CARDIOLOGIA', 'DERMATOLOGIA']);
    expect(component['loading']()).toBe(false);
  });

  it('should populate filtered doctors and clinics from reference data', () => {
    fixture.detectChanges();

    // Senza filtro specializzazione, tutti i medici sono visibili
    expect(component['filteredDoctors']().length).toBe(2);
    expect(component['filteredClinics']().length).toBe(2);
  });

  it('should filter doctors by specialization via cascade', () => {
    fixture.detectChanges();

    component['filterForm'].patchValue({ specialization: 'CARDIOLOGIA' });
    component['onSpecializationChange']();

    const filtered = component['filteredDoctors']();
    expect(filtered.length).toBe(1);
    expect(filtered[0].fullName).toBe('Dr. Rossi');
  });

  it('should call search and populate results', () => {
    fixture.detectChanges();

    availabilityServiceMock.search.mockReturnValue(of(mockSearchResults));

    component['filterForm'].patchValue({ specialization: 'CARDIOLOGIA' });
    component['search']();

    expect(availabilityServiceMock.search).toHaveBeenCalled();
    const results = component['results']();
    expect(results.length).toBe(1);
    expect(component['loading']()).toBe(false);
  });

  it('should open error dialog when search fails', () => {
    fixture.detectChanges();

    availabilityServiceMock.search.mockReturnValue(throwError(() => new Error('Server error')));

    component['search']();

    expect(dialogMock.open).toHaveBeenCalled();
    expect(component['loading']()).toBe(false);
  });

  it('should reset all filters and clear results on resetFilters', () => {
    fixture.detectChanges();

    component['filterForm'].patchValue({ specialization: 'CARDIOLOGIA', doctorId: 'D1' });
    component['resetFilters']();

    expect(component['filterForm'].get('specialization')?.value).toBe('');
    expect(component['filterForm'].get('doctorId')?.value).toBe('');
    expect(component['results']()).toEqual([]);
    expect(component['totalElements']()).toBe(0);
  });

  it('should open error dialog when getFilters fails', () => {
    availabilityServiceMock.getFilters.mockReturnValue(throwError(() => new Error('Network error')));

    // Ricreiamo il componente per testare il fallimento del costruttore
    fixture = TestBed.createComponent(AvailabilitySearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(dialogMock.open).toHaveBeenCalled();
    expect(component['loading']()).toBe(false);
  });
});
