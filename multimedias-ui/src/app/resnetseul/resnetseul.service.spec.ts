import { TestBed } from '@angular/core/testing';

import { ResnetseulService } from './resnetseul.service';

describe('ResnetseulService', () => {
  let service: ResnetseulService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ResnetseulService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
