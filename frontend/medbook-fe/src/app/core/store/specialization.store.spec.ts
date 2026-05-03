import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SpecializationStore } from './specialization.store';
import { SpecializationService } from '../services/specialization.service';

describe('SpecializationStore', () => {
  let store: SpecializationStore;
  const serviceMock = {
    getAll: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        SpecializationStore,
        { provide: SpecializationService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(SpecializationStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadAll() should call service and return mapped specializations', (done) => {
    const mockSpecs = [{ id: 1, name: 'Cardiologia' }, { id: 2, name: 'Ortopedia' }];
    serviceMock.getAll.mockReturnValue(of({ data: { specializations: mockSpecs } }));

    store.loadAll().subscribe(result => {
      expect(serviceMock.getAll).toHaveBeenCalled();
      expect(result).toEqual(mockSpecs);
      done();
    });
  });

  it('loadAll() should return cached data on second call', (done) => {
    const mockSpecs = [{ id: 1, name: 'Cardiologia' }];
    serviceMock.getAll.mockReturnValue(of({ data: { specializations: mockSpecs } }));

    store.loadAll().subscribe(() => {
      store.loadAll().subscribe(result => {
        expect(serviceMock.getAll).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockSpecs);
        done();
      });
    });
  });

  it('loadAll() should return empty array when specializations field is missing', (done) => {
    serviceMock.getAll.mockReturnValue(of({ data: {} }));

    store.loadAll().subscribe(result => {
      expect(result).toEqual([]);
      done();
    });
  });
});
