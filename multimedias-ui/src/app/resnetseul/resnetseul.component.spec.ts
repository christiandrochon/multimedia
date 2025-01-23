import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResnetseulComponent } from './resnetseul.component';

describe('ResnetseulComponent', () => {
  let component: ResnetseulComponent;
  let fixture: ComponentFixture<ResnetseulComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResnetseulComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResnetseulComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
