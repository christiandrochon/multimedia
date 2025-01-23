import { TestBed } from '@angular/core/testing';

import { SimilariteformeService } from './similariteforme.service';

describe('SimilariteformeService', () => {
  let service: SimilariteformeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SimilariteformeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
