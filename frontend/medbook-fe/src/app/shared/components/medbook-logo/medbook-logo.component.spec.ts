import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MedbookLogoComponent } from './medbook-logo.component';

describe('MedbookLogoComponent', () => {
  let component: MedbookLogoComponent;
  let fixture: ComponentFixture<MedbookLogoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedbookLogoComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MedbookLogoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
