import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PatientStore } from './patient.store';
import { PatientService } from '../services/patient.service';

describe('PatientStore', () => {
  let store: PatientStore;
  const serviceMock = {
    getAll: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        PatientStore,
        { provide: PatientService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(PatientStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadAll() should call service with default params and return mapped data', (done) => {
    const mockPatients = [{ id: '1', name: 'Mario Rossi' }, { id: '2', name: 'Luca Bianchi' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockPatients }));

    store.loadAll().subscribe(result => {
      expect(serviceMock.getAll).toHaveBeenCalledWith({ size: 100 });
      expect(result).toEqual(mockPatients);
      done();
    });
  });

  it('loadAll() should return cached data on second call', (done) => {
    const mockPatients = [{ id: '1', name: 'Mario Rossi' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockPatients }));

    store.loadAll().subscribe(() => {
      store.loadAll().subscribe(result => {
        expect(serviceMock.getAll).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockPatients);
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
