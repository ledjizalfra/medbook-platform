import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MedBookPageComponent } from './medbook-page.component';

describe('MedBookPageComponent', () => {
  let component: MedBookPageComponent;
  let fixture: ComponentFixture<MedBookPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookPageComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
