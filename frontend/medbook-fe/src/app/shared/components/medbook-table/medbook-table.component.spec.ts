import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MedBookTableComponent } from './medbook-table.component';

describe('MedBookTableComponent', () => {
  let component: MedBookTableComponent;
  let fixture: ComponentFixture<MedBookTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedBookTableComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MedBookTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
