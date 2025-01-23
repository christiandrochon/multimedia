import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimilaritetextureComponent } from './similaritetexture.component';

describe('SimilaritetextureComponent', () => {
  let component: SimilaritetextureComponent;
  let fixture: ComponentFixture<SimilaritetextureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimilaritetextureComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimilaritetextureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
