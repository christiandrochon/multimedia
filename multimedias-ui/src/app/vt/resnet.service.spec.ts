import { TestBed } from '@angular/core/testing';

import { ResnetService } from './resnet.service';

describe('ResnetService', () => {
  let service: ResnetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ResnetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
