import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {SimilariteService} from '../similaritecouleur/similarite.service';
import {SimilaritetextureService} from './similaritetexture.service';

@Component({
  selector: 'app-similaritetexture',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './similaritetexture.component.html',
  styleUrl: './similaritetexture.component.css'
})
export class SimilaritetextureComponent implements OnInit {

  queryImage: string = '';  // URL de l'image de requête
  similarImages: string[] = [];  // Liste des images similaires
  loading = false;  // État de chargement pour le spinner

  constructor(private similariteService: SimilaritetextureService) {
  }

  ngOnInit(): void {
    this.loadRandomImage();
  }

  // Charge une image aléatoire depuis le backend
  loadRandomImage() {
    this.similarImages = []; // Clear the similar images array
    this.similariteService.getRandomImage().subscribe({
      next: (path: string) => {
        this.queryImage = `http://localhost:8087${path}`;
        console.log("Chemin de l'image :", this.queryImage);  // Affichez pour confirmation
      },
      error: (err) => console.error("Erreur lors du chargement de l'image:", err)
    });
  }

  // Lance la recherche par similarité par la texture
  searchSimilarImages() {
    this.loading = true;
    this.similariteService.getSimilarImages(this.queryImage, 8).subscribe(
      images => {
        this.similarImages = images.map(img => `http://localhost:8087${img}`);
        this.loading = false;
      },
      error => {
        console.error("Erreur lors de la recherche d'images similaires:", error);
        this.loading = false;
      }
    );
  }

}
