import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReceptionistFormComponent } from './receptionist-form.component';

describe('ReceptionistFormComponent', () => {
  let component: ReceptionistFormComponent;
  let fixture: ComponentFixture<ReceptionistFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ReceptionistFormComponent] }).compileComponents();
    fixture = TestBed.createComponent(ReceptionistFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
