import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DoctorAvailabilitiesPageComponent } from './doctor-availabilities-page.component';

describe('DoctorAvailabilitiesPageComponent', () => {
  let component: DoctorAvailabilitiesPageComponent;
  let fixture: ComponentFixture<DoctorAvailabilitiesPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorAvailabilitiesPageComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(DoctorAvailabilitiesPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
