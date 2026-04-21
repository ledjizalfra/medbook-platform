import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsentToggleComponent } from './consent-toggle.component';

describe('ConsentToggleComponent', () => {
  let component: ConsentToggleComponent;
  let fixture: ComponentFixture<ConsentToggleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsentToggleComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ConsentToggleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
