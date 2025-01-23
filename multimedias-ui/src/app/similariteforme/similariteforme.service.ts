import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SimilariteformeService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {
  }

  // Récupère une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http.get(`${this.apiUrl}/similariteforme`, {responseType: 'text'});
  }

  // Recherche d'images similaires par la forme
  // getSimilarImages(queryImage: string, k: number): Observable<string[]> {
  //   return this.http.get<string[]>(`${this.apiUrl}/recherchesimilaireforme`, {
  //     params: { queryImage, k: k.toString() }
  //   });
  // }

  // Recherche d'images similaires par la forme
  getSimilarImages(queryImage: string, k: number): Observable<string[]> {
    const imageName = queryImage.split('/').pop() || queryImage; // Extraire uniquement le nom de l'image
    return this.http.get<string[]>(`${this.apiUrl}/recherchesimilaireforme`, {
      params: {queryImage: imageName, k: k.toString()},
    });
  }

}
