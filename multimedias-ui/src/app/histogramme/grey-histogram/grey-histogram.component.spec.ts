import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GreyHistogramComponent } from './grey-histogram.component';

describe('GreyHistogramComponent', () => {
  let component: GreyHistogramComponent;
  let fixture: ComponentFixture<GreyHistogramComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GreyHistogramComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GreyHistogramComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
