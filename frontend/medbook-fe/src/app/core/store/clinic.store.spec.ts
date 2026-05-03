import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ClinicStore } from './clinic.store';
import { ClinicService } from '../services/clinic.service';

describe('ClinicStore', () => {
  let store: ClinicStore;
  const serviceMock = {
    getAll: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        ClinicStore,
        { provide: ClinicService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(ClinicStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadAll() should call service with default params and return mapped data', (done) => {
    const mockClinics = [{ id: '1', name: 'Clinica Roma' }, { id: '2', name: 'Clinica Milano' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockClinics }));

    store.loadAll().subscribe(result => {
      expect(serviceMock.getAll).toHaveBeenCalledWith({ size: 100, status: 'ATTIVO' });
      expect(result).toEqual(mockClinics);
      done();
    });
  });

  it('loadAll() should return cached data on second call', (done) => {
    const mockClinics = [{ id: '1', name: 'Clinica Roma' }];
    serviceMock.getAll.mockReturnValue(of({ data: mockClinics }));

    store.loadAll().subscribe(() => {
      store.loadAll().subscribe(result => {
        expect(serviceMock.getAll).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockClinics);
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
