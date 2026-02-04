import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrferecatuComponent } from './prferecatu.component';

describe('PrferecatuComponent', () => {
  let component: PrferecatuComponent;
  let fixture: ComponentFixture<PrferecatuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrferecatuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrferecatuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
