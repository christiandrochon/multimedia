import { TestBed } from '@angular/core/testing';

import { PrecisionrappelgenerationService } from './precisionrappelgeneration.service';

describe('PrecisionrappelgenerationService', () => {
  let service: PrecisionrappelgenerationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrecisionrappelgenerationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
