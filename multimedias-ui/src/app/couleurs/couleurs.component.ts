import {HttpClient} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute} from '@angular/router';

interface CheckerboardResponse {
  checkerboardImage: string;
}

interface ImageResponse {
  randomImage: string;
  swappedImage: string;
}

@Component({
  selector: 'app-image-swap',
  templateUrl: './couleurs.component.html',
  standalone: true,
  styleUrls: ['./couleurs.component.css'],
  imports: [CommonModule]  // Necessaire pour ngif dans le html
})
export class CouleursComponent implements OnInit {

  randomImageUrl: string = '';
  swappedImageUrl: string = '';
  randomImageUrls: string[] = [];
  imageUrl: string = '';
  checkerboardImageUrl: string = '';

  constructor(private http: HttpClient, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    //route pour obtenir l'url de la page et afficher des resultat differents en fontion de l'url
    this.route.url.subscribe(url => {
      this.imageUrl = url.join('/');
      if (this.imageUrl.includes('swapRandomImage')) {
        this.swapRandomImage();
      } else if (this.imageUrl.includes('createCheckerboard')) {
        this.createCheckerboard();
      }
    });

    // this.swapRandomImage();
    // this.createCheckerboard()
  }

  /**
   * Affiche une image aleatoire et affiche à sa droite la meme image avec les couleurs transformées RGB -> GBR
   */
  swapRandomImage(): void {
    // Reset the swappedImageUrl before making the HTTP request
    // this.swappedImageUrl = '';

    this.http.get<ImageResponse>('http://localhost:8087/swapRandomImage', {responseType: 'json'})
      .subscribe(response => {
        this.randomImageUrl = 'http://localhost:8087' + response.randomImage;
        // Add a timestamp to the swappedImageUrl to prevent caching
        this.swappedImageUrl = 'http://localhost:8087' + response.swappedImage + '?t=' + new Date().getTime();
      }, error => {
        console.error('Error fetching images', error);
      });
  }

  /**
   * Créé un damier rouge et vert
   */
  createCheckerboard(): void {
    this.http.get<CheckerboardResponse>('http://localhost:8087/createCheckerboard')
      .subscribe(response => {
        this.checkerboardImageUrl = 'http://localhost:8087' + response.checkerboardImage;
      }, error => {
        console.error('Error creating checkerboard image', error);
      });
  }
}
