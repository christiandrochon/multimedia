import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimilaritecouleurtextureComponent } from './similaritecouleurtexture.component';

describe('SimilaritecouleurtextureComponent', () => {
  let component: SimilaritecouleurtextureComponent;
  let fixture: ComponentFixture<SimilaritecouleurtextureComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimilaritecouleurtextureComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimilaritecouleurtextureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
