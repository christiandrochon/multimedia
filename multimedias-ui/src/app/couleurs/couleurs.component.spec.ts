import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CouleursComponent } from './couleurs.component';

describe('CouleursComponent', () => {
  let component: CouleursComponent;
  let fixture: ComponentFixture<CouleursComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CouleursComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CouleursComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
