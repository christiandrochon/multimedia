import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimilariteformeComponent } from './similariteforme.component';

describe('SimilariteformeComponent', () => {
  let component: SimilariteformeComponent;
  let fixture: ComponentFixture<SimilariteformeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimilariteformeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimilariteformeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
