import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DoctorAssignmentsPageComponent } from './doctor-assignments-page.component';

describe('DoctorAssignmentsPageComponent', () => {
  let component: DoctorAssignmentsPageComponent;
  let fixture: ComponentFixture<DoctorAssignmentsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorAssignmentsPageComponent]
    }).compileComponents();
    fixture = TestBed.createComponent(DoctorAssignmentsPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
