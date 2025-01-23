import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PrecisionRecallService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {
  }

  // Obtenir une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/evaluation`, {responseType: 'text'})
      .pipe(
        map((response: string) => response.replace(/['"]/g, '').trim()) // Retirer les guillemets et les espaces
      );
  }

  /**
   * Obtenir les images les plus similaires à une image donnée, et recupere les points de la courbe de précision/rappel
   * @param queryImage URL de l'image de requête
   * @param topK Nombre d'images les plus similaires à récupérer
   */
  getPrecisionRecall(queryImage: string, topK: number): Observable<{
    Precision: { recall: number; precision: number }[];
    Recall: { recall: number; precision: number }[];
    nearestNeighbors: string[];
  }> {
    return this.http.get<{
      Precision: { recall: number; precision: number }[];
      Recall: { recall: number; precision: number }[];
      nearestNeighbors: string[];
    }>(`${this.apiUrl}/precision-recall?queryImage=${queryImage}&topK=${topK}`);
  }

}
