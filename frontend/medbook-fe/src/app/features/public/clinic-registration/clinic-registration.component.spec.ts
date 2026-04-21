import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClinicRegistrationComponent } from './clinic-registration.component';

describe('ClinicRegistrationComponent', () => {
  let component: ClinicRegistrationComponent;
  let fixture: ComponentFixture<ClinicRegistrationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClinicRegistrationComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ClinicRegistrationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
