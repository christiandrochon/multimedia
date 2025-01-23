import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {NgForOf} from '@angular/common';
import {FaIconLibrary, FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {faArrowLeft, faArrowRight, faQuestionCircle, faSync} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-multimedia',
  standalone: true,
  templateUrl: './multimedia.component.html',
  styleUrls: ['./multimedia.component.css'],
  imports: [HttpClientModule, NgForOf, FontAwesomeModule]
})
export class MultimediaComponent implements OnInit {

  images: string[] = [];
  buttons: string[] = ['Load', 'Back', 'Next', 'Bouton 4', 'Bouton 5', 'Bouton 6'];

  constructor(private http: HttpClient, private library: FaIconLibrary) {
    // Charger dynamiquement les icônes dans la bibliothèque
    library.addIcons(faSync, faArrowLeft, faArrowRight, faQuestionCircle);
  }

  ngOnInit(): void {
    this.fetchImages();  // Charger les images initiales
  }

  // Fonction pour récupérer les images aléatoires depuis le backend
  fetchImages(): void {
    this.http.get<string[]>('http://localhost:8087/images')
      .subscribe(
        data => this.images = data.map(image => `http://localhost:8087${image}`),
        error => console.error('Error fetching images', error)
      );
  }

  // Fonction pour recharger les images aléatoires
  reloadImages(): void {
    this.fetchImages();  // Recharge les images
  }
}
