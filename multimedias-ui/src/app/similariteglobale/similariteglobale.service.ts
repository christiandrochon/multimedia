import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SimilariteglobaleService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  // Récupère une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http.get(`${this.apiUrl}/similariteglobale`, { responseType: 'text' });
  }

  // Recherche d'images similaires par la texture
  getSimilarImages(queryImage: string, k: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/recherchesimilaireglobale`, {
      params: { queryImage, k: k.toString() }
    });
  }
}
