import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServicesService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) { }

  // getGreeting(): Observable<string> {
  //   return this.http.get(this.apiUrl, { responseType: 'text' });
  // }

  getImages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/images`);
  }

  // Fonction pour récupérer l'histogramme avec un nombre de bins donné
  // getHistogram(numBins: number): Observable<number[]> {
  //   return this.http.get<number[]>(`${this.apiUrl}/histogramme-grey?numBins=${numBins}`);
  // }
}
