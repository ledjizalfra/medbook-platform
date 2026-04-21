import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { RegisterComponent } from './register.component';
import { PatientService } from '../../../core/services/patient.service';

describe('RegisterComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNativeDateAdapter(),
        provideAnimationsAsync(),
        { provide: PatientService, useValue: { create: vi.fn() } },
      ],
    });
  });

  // TODO: aggiungere test
  // - form invalido con campi vuoti
  // - validazione password (minLength, pattern)
  // - passwordMismatch validator
  // - dateNotFuture validator
  // - successo: snackbar + redirect /login
  // - errore 409: errore inline su campo email
  // - errore generico: snackbar errore
});
