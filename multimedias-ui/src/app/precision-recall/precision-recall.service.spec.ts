import { TestBed } from '@angular/core/testing';

import { PrecisionRecallService } from './precision-recall.service';

describe('PrecisionRecallService', () => {
  let service: PrecisionRecallService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrecisionRecallService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
