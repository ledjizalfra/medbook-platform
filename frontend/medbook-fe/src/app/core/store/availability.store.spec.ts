import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AvailabilityStore } from './availability.store';
import { DoctorService } from '../services/doctor.service';

describe('AvailabilityStore', () => {
  let store: AvailabilityStore;
  const serviceMock = {
    getAvailabilities: vi.fn()
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        AvailabilityStore,
        { provide: DoctorService, useValue: serviceMock }
      ]
    });
    store = TestBed.inject(AvailabilityStore);
  });

  it('should be created', () => {
    expect(store).toBeTruthy();
  });

  it('loadByDoctor() should call service and return mapped availabilities', (done) => {
    const mockAvails = [{ dayOfWeek: 'LUNEDI', startTime: '09:00' }, { dayOfWeek: 'MARTEDI', startTime: '10:00' }];
    serviceMock.getAvailabilities.mockReturnValue(of({ data: { availabilities: mockAvails } }));

    store.loadByDoctor('doc-1').subscribe(result => {
      expect(serviceMock.getAvailabilities).toHaveBeenCalledWith('doc-1');
      expect(result).toEqual(mockAvails);
      done();
    });
  });

  it('loadByDoctor() should return cached data on second call with same doctorId', (done) => {
    const mockAvails = [{ dayOfWeek: 'LUNEDI', startTime: '09:00' }];
    serviceMock.getAvailabilities.mockReturnValue(of({ data: { availabilities: mockAvails } }));

    store.loadByDoctor('doc-1').subscribe(() => {
      store.loadByDoctor('doc-1').subscribe(result => {
        expect(serviceMock.getAvailabilities).toHaveBeenCalledTimes(1);
        expect(result).toEqual(mockAvails);
        done();
      });
    });
  });

  it('loadByDoctor() should invalidate cache when doctorId changes', (done) => {
    const avails1 = [{ dayOfWeek: 'LUNEDI' }];
    const avails2 = [{ dayOfWeek: 'VENERDI' }];
    let callCount = 0;
    serviceMock.getAvailabilities.mockImplementation(() => {
      callCount++;
      return of({ data: { availabilities: callCount === 1 ? avails1 : avails2 } });
    });

    store.loadByDoctor('doc-1').subscribe(() => {
      store.loadByDoctor('doc-2').subscribe(result => {
        expect(serviceMock.getAvailabilities).toHaveBeenCalledTimes(2);
        expect(result).toEqual(avails2);
        done();
      });
    });
  });

  it('loadByDoctor() should return empty array when availabilities field is missing', (done) => {
    serviceMock.getAvailabilities.mockReturnValue(of({ data: {} }));

    store.loadByDoctor('doc-1').subscribe(result => {
      expect(result).toEqual([]);
      done();
    });
  });
});
