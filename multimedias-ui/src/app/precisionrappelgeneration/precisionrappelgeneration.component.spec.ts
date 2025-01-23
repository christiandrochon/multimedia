import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrecisionrappelgenerationComponent } from './precisionrappelgeneration.component';

describe('PrecisionrappelgenerationComponent', () => {
  let component: PrecisionrappelgenerationComponent;
  let fixture: ComponentFixture<PrecisionrappelgenerationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrecisionrappelgenerationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrecisionrappelgenerationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
