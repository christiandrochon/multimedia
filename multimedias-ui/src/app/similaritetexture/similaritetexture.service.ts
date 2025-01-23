import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SimilaritetextureService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  // Récupère une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http.get(`${this.apiUrl}/similaritetexture`, { responseType: 'text' });
  }

  // Recherche d'images similaires par la texture
  getSimilarImages(queryImage: string, k: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/recherchesimilairetexture`, {
      params: { queryImage, k: k.toString() }
    });
  }
}
