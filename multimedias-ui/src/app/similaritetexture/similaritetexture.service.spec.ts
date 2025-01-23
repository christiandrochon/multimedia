import { TestBed } from '@angular/core/testing';

import { SimilaritetextureService } from './similaritetexture.service';

describe('SimilaritetextureService', () => {
  let service: SimilaritetextureService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SimilaritetextureService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
