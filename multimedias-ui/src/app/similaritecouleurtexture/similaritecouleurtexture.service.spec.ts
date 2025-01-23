import { TestBed } from '@angular/core/testing';

import { SimilaritecouleurtextureService } from './similaritecouleurtexture.service';

describe('SimilaritecouleurtextureService', () => {
  let service: SimilaritecouleurtextureService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SimilaritecouleurtextureService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
