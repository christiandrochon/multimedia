import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimilariteglobaleComponent } from './similariteglobale.component';

describe('SimilariteglobaleComponent', () => {
  let component: SimilariteglobaleComponent;
  let fixture: ComponentFixture<SimilariteglobaleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimilariteglobaleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimilariteglobaleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
