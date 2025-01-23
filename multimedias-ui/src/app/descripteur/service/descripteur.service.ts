import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DescripteurService {
  private apiUrl = 'http://localhost:8087';  // URL de votre API Spring Boot

  constructor(private http: HttpClient) { }

  processRVB666(): Observable<string> {
    return this.http.get(`${this.apiUrl}/rvb666`, { responseType: 'text' });
  }

  processRVB444(): Observable<string> {
    return this.http.get(`${this.apiUrl}/rvb444`, { responseType: 'text' });
  }

  processRVB222(): Observable<string> {
    return this.http.get(`${this.apiUrl}/rvb222`, { responseType: 'text' });
  }

  processGrey256(): Observable<string> {
    return this.http.get(`${this.apiUrl}/grey256`, { responseType: 'text' });
  }

  processGrey64(): Observable<string> {
    return this.http.get(`${this.apiUrl}/grey64`, { responseType: 'text' });
  }

  processGrey16(): Observable<string> {
    return this.http.get(`${this.apiUrl}/grey16`, { responseType: 'text' });
  }

  processForme(): Observable<string> {
    return this.http.get(`${this.apiUrl}/genererforme`, { responseType: 'text' });
  }

  processTexture(): Observable<string> {
    return this.http.get(`${this.apiUrl}/generertexture`, { responseType: 'text' });
  }
}
