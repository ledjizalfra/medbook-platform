import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { ReceptionistListComponent } from './receptionist-list.component';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('ReceptionistListComponent', () => {
  let component: ReceptionistListComponent;
  let fixture: ComponentFixture<ReceptionistListComponent>;

  const mockResponse = {
    data: {
      content: [
        { keycloakId: 'KC-1', firstName: 'Anna', lastName: 'Verdi', email: 'anna@test.it', enabled: true, createdAt: '2025-01-01' }
      ],
      totalElements: 1
    }
  };

  const receptionistServiceMock = {
    getAll: vi.fn().mockReturnValue(of(mockResponse)),
    delete: vi.fn()
  };

  const authServiceMock = {
    sendResetPasswordEmail: vi.fn()
  };

  const routerMock = { navigate: vi.fn() };
  const dialogMock = { open: vi.fn() };
  const snackBarMock = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();

    // Restore default return value
    receptionistServiceMock.getAll.mockReturnValue(of(mockResponse));

    await TestBed.configureTestingModule({
      imports: [ReceptionistListComponent, NoopAnimationsModule]
    })
    .overrideComponent(ReceptionistListComponent, {
      set: {
        providers: [
          { provide: ReceptionistService, useValue: receptionistServiceMock },
          { provide: AuthService, useValue: authServiceMock },
          { provide: Router, useValue: routerMock },
          { provide: MatDialog, useValue: dialogMock },
          { provide: MatSnackBar, useValue: snackBarMock }
        ]
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReceptionistListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load data on init', () => {
    fixture.detectChanges();

    expect(receptionistServiceMock.getAll).toHaveBeenCalledWith({ page: 0, size: 10 });
  });

  it('should populate receptionists signal after load', () => {
    fixture.detectChanges();

    const list = component['receptionists']();
    expect(list.length).toBe(1);
    // Verifica che enabled boolean sia stato convertito in label
    const row = list[0] as Record<string, unknown>;
    expect(row['enabled']).toBe('ATTIVO');
  });

  it('should set loading to false after data load', () => {
    fixture.detectChanges();

    expect(component['loading']()).toBe(false);
  });

  it('should navigate to new receptionist page on newReceptionist()', () => {
    fixture.detectChanges();

    component['newReceptionist']();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/admin/receptionists/new']);
  });

  it('should set loading to false on error', () => {
    receptionistServiceMock.getAll.mockReturnValue(throwError(() => new Error('Server error')));

    fixture.detectChanges();

    expect(component['loading']()).toBe(false);
  });

  it('should reload data on page change', () => {
    fixture.detectChanges();
    receptionistServiceMock.getAll.mockClear();

    component['onPageChange']({ pageIndex: 1, pageSize: 10, length: 20 });

    expect(receptionistServiceMock.getAll).toHaveBeenCalledWith({ page: 1, size: 10 });
  });
});
