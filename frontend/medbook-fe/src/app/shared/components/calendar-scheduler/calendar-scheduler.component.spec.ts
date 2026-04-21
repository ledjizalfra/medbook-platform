import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarSchedulerComponent } from './calendar-scheduler.component';

describe('CalendarSchedulerComponent', () => {
  let component: CalendarSchedulerComponent;
  let fixture: ComponentFixture<CalendarSchedulerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarSchedulerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarSchedulerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
