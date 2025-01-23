import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimilariteComponent } from './similarite.component';

describe('SimilariteComponent', () => {
  let component: SimilariteComponent;
  let fixture: ComponentFixture<SimilariteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimilariteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimilariteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
