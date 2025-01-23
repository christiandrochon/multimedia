import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrecisionRecallComponent } from './precision-recall.component';

describe('PrecisionRecallComponent', () => {
  let component: PrecisionRecallComponent;
  let fixture: ComponentFixture<PrecisionRecallComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrecisionRecallComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrecisionRecallComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
