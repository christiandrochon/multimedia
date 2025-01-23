import { TestBed } from '@angular/core/testing';

import { DescripteurService } from './descripteur.service';

describe('DescripteurService', () => {
  let service: DescripteurService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DescripteurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
