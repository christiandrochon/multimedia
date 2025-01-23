import { TestBed } from '@angular/core/testing';

import { SimilariteglobaleService } from './similariteglobale.service';

describe('SimilariteglobaleService', () => {
  let service: SimilariteglobaleService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SimilariteglobaleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
