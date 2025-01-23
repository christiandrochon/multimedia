import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HelloService {
  private apiUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  getGreeting(): Observable<string> {
    return this.http.get(this.apiUrl, {responseType: 'text'});
  }
}
