import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppointmentStore } from './appointment.store';
import { AppointmentService } from '../services/appointment.service';

describe('AppointmentStore', () => {
  let store: AppointmentStore;
  const serviceMock = {
    getAll: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        AppointmentStore,
        { provide: AppointmentService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(AppointmentStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadAll() should call service with default params and return mapped data', (done) => {
    const mockAppointments = [{ id: '1', date: '2026-05-01' }, { id: '2', date: '2026-05-02' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockAppointments }));

    store.loadAll().subscribe(result => {
      expect(serviceMock.getAll).toHaveBeenCalledWith({});
      expect(result).toEqual(mockAppointments);
      done();
    });
  });

  it('loadAll() should return cached data on second call', (done) => {
    const mockAppointments = [{ id: '1', date: '2026-05-01' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockAppointments }));

    store.loadAll().subscribe(() => {
      store.loadAll().subscribe(result => {
        expect(serviceMock.getAll).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockAppointments);
        done();
      });
    });
  });

  it('loadAll() should return empty array when data is not an array', (done) => {
    serviceMock.getAll.mockReturnValue(of({ data: null }));

    store.loadAll().subscribe(result => {
      expect(result).toEqual([]);
      done();
    });
  });
});
