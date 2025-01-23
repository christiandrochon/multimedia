import { TestBed } from '@angular/core/testing';

import { SimilariteService } from './similarite.service';

describe('SimilariteService', () => {
  let service: SimilariteService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SimilariteService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
