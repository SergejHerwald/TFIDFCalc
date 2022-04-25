import { TestBed } from '@angular/core/testing';

import { FileuploaderService } from './fileuploader.service';

describe('FileuploaderService', () => {
  let service: FileuploaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileuploaderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
