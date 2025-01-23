import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, map} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ResnetService {

  private baseUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {
  }

  // getClassDescriptors(): Observable<string[]> {
  //   return this.http.get<string[]>(`${this.baseUrl}/resnetclasses`);
  // }
  //
  // getImagesForClass(className: string): Observable<string[]> {
  //   return this.http.get<string[]>(`${this.baseUrl}/resnetimages`, {
  //     params: {className}
  //   });
  // }


  // Récupérer les noms de classes de descripteurs
  // getClassDescriptors(): Observable<string[]> {
  //   return this.http.get<string[]>(`${this.baseUrl}/resnetclasses/similar`);
  // }

  // getRandomImage(): Observable<string> {
  //   return this.http.get<string>(`${this.baseUrl}/resnetclasses/similar`);
  // }
  // Récupérer une image aléatoire
  getRandomImage(): Observable<string> {
    return this.http.get<string>(`${this.baseUrl}/VTclasses/similar`, {
      responseType: 'text' as 'json'  // Pour éviter que le navigateur ne traite l'image comme un objet JSON
    }).pipe(
      map(image => `${this.baseUrl}/images/${image}`)
    );
  }
  // // Récupérer une image aléatoire pour une classe donnée
  // getRandomImageForClass(className: string): Observable<string> {
  //   return this.http.get<string>(`${this.baseUrl}/resnetimages/random`, {
  //     params: { className },
  //     responseType: 'text' as 'json'  // Pour éviter que le navigateur ne traite l'image comme un objet JSON
  //   }).pipe(
  //     map(image => `${this.baseUrl}/images/${image}`)  // Construire l'URL de l'image aléatoire
  //   );
  // }

  // Récupérer les images similaires pour une classe donnée
  getImagesForClassWithSimilarity(className: string, queryImage: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/VTimages/similar`, {
      params: { className, queryImage }
    }).pipe(
      map(images => images.map(image => `${this.baseUrl}/images/${image}`))  // Construire l'URL de chaque image
    );
  }
}
