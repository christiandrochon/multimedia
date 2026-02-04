import { TestBed } from '@angular/core/testing';

import { PrferecatuService } from './prferecatu.service';

describe('PrferecatuService', () => {
  let service: PrferecatuService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrferecatuService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
