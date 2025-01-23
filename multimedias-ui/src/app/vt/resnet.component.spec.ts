import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResnetComponent } from './resnet.component';

describe('ResnetComponent', () => {
  let component: ResnetComponent;
  let fixture: ComponentFixture<ResnetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResnetComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResnetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
