import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SimilariteService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  // Récupère une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http.get(`${this.apiUrl}/similaritecouleur`, { responseType: 'text' });
  }

  // Recherche d'images similaires
  getSimilarImages(queryImage: string, k: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/recherchesimilaire`, {
      params: { queryImage, k: k.toString() }
    });
  }
}
