import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DescripteurComponent } from './descripteur.component';

describe('DescripteurComponent', () => {
  let component: DescripteurComponent;
  let fixture: ComponentFixture<DescripteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DescripteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DescripteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
