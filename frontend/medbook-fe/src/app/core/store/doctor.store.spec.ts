import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { DoctorStore } from './doctor.store';
import { DoctorService } from '../services/doctor.service';

describe('DoctorStore', () => {
  let store: DoctorStore;
  const serviceMock = {
    getAll: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        DoctorStore,
        { provide: DoctorService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(DoctorStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadAll() should call service with default params and return mapped data', (done) => {
    const mockDoctors = [{ id: '1', name: 'Dr. Rossi' }, { id: '2', name: 'Dr. Bianchi' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockDoctors }));

    store.loadAll().subscribe(result => {
      expect(serviceMock.getAll).toHaveBeenCalledWith({ size: 100, status: 'ATTIVO' });
      expect(result).toEqual(mockDoctors);
      done();
    });
  });

  it('loadAll() should return cached data on second call', (done) => {
    const mockDoctors = [{ id: '1', name: 'Dr. Rossi' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockDoctors }));

    store.loadAll().subscribe(() => {
      store.loadAll().subscribe(result => {
        expect(serviceMock.getAll).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockDoctors);
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
