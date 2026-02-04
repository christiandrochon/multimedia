import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, throwError, timeout} from 'rxjs';
import {catchError} from 'rxjs/operators';

export interface PRResponse {
  recall: number[];
  precision: number[];
}

@Injectable({
  providedIn: 'root'
})
export class PrferecatuService {

  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  getPRCurve(): Observable<any> {
    return this.http.get<any>(this.apiUrl+"/precision-rappel/PR-curve").pipe(
      timeout(120000), // 2 minutes max
      catchError(err => {
        console.error('Erreur de récupération des courbes PR :', err);
        return throwError(() => new Error('Erreur réseau ou délai dépassé'));
      })
    );
  }
}
