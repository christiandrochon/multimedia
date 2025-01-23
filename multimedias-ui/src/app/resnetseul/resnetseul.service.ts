import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, map} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ResnetseulService {

  private baseUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {
  }

  getRandomImage(className: string): Observable<string> {
    return this.http.get<string>(`${this.baseUrl}/resnetimages/random`, {
      params: { className },
      responseType: 'text' as 'json'
    }).pipe(
      map(imageName => `${this.baseUrl}/images/${imageName}`)
    );
  }


  getClassDescriptors(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/resnetclasses`);
  }

  getImagesForClass(className: string, queryImage: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/resnetimages`, {
      params: { className, queryImage }
    }).pipe(
      map(images => images.map(image => `${this.baseUrl}/images/${image}`))
    );
  }

}
