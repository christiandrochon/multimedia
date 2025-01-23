import { TestBed } from '@angular/core/testing';

import { HistogrammeService } from './histogramme.service';

describe('HistogrammeService', () => {
  let service: HistogrammeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistogrammeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
