import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReceptionistListComponent } from './receptionist-list.component';

describe('ReceptionistListComponent', () => {
  let component: ReceptionistListComponent;
  let fixture: ComponentFixture<ReceptionistListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ReceptionistListComponent] }).compileComponents();
    fixture = TestBed.createComponent(ReceptionistListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
