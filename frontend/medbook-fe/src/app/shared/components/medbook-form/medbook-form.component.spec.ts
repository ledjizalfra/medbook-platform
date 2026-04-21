import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MedBookFormComponent } from './medbook-form.component';

describe('MedBookFormComponent', () => {
  let component: MedBookFormComponent;
  let fixture: ComponentFixture<MedBookFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookFormComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookFormComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
