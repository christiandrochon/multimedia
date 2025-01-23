import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PrecisionrappelgenerationService {

  private apiUrl = 'http://localhost:8087';  // URL de votre API Spring Boot

  constructor(private http : HttpClient) { }

  processPRGris(): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/precision-rappel/gris`, { responseType: 'text' })
      .pipe(
        catchError((error) => {
          console.warn('Une erreur est survenue mais le traitement continue.', error);
          return of('Le fichier des descripteurs de niveaux de gris est en cours de génération asynchrone.'); // Message par défaut
        })
      );
  }

  processPRCouleur(): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/precision-rappel/couleur`, { responseType: 'text' })
      .pipe(
        catchError((error) => {
          console.warn('Une erreur est survenue mais le traitement continue.', error);
          return of('Le fichier Json des descripteurs de niveaux de couleur est en cours de génération asynchrone.');
        })
      );
  }

  processPRTous(): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/precision-rappel/gris-couleur-resnet`, { responseType: 'text' })
      .pipe(
        catchError((error) => {
          console.warn('Une erreur est survenue mais le traitement continue.', error);
          return of('Le fichier Json pour les descripteurs de gris-couleur-resnet est en cours de génération asynchrone.');
        })
      );
  }
}
