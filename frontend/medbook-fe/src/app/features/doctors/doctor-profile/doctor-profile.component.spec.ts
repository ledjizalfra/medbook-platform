import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DoctorProfileComponent } from './doctor-profile.component';

describe('DoctorProfileComponent', () => {
  let component: DoctorProfileComponent;
  let fixture: ComponentFixture<DoctorProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorProfileComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorProfileComponent);
    component = fixture.componentInstance;
  });

  // TODO: aggiungere test
  // - ngOnInit(): carica il profilo tramite DoctorService.getMe()
  // - ngOnInit(): gestisce errore di caricamento (snackbar)
  // - startEdit(): popola editForm con i dati del profilo corrente
  // - onSubmit(): chiama DoctorService.update() con i dati del form
  // - onSubmit(): ricarica il profilo dopo salvataggio riuscito
  // - cancelEdit(): torna in modalità sola lettura senza salvare
  // - field(): restituisce '--' per valori null/undefined
  // - formatGender(): mappa MASCHIO/FEMMINA/MALE/FEMALE in italiano
});
