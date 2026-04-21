import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AuthHeaderComponent } from './auth-header.component';

describe('AuthHeaderComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuthHeaderComponent],
      providers: [provideRouter([]), provideAnimationsAsync()]
    });
  });

  // TODO: aggiungere test
  // - click freccia indietro chiama Location.back()
  // - click logo naviga a '/'
  // - click "Home" naviga a '/'
});
