import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppointmentCardComponent } from './appointment-card.component';

describe('AppointmentCardComponent', () => {
  let component: AppointmentCardComponent;
  let fixture: ComponentFixture<AppointmentCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppointmentCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AppointmentCardComponent);
    component = fixture.componentInstance;
    component.appointment = {
      appointmentId: 'APT-1',
      slotDate: '2026-04-15',
      startTime: '09:00',
      endTime: '09:30',
      status: 'PRENOTATO'
    };
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
