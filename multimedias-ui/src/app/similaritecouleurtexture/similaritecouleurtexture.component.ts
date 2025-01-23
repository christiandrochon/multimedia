import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {SimilaritecouleurtextureService} from './similaritecouleurtexture.service';

@Component({
  selector: 'app-similaritecouleurtexture',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './similaritecouleurtexture.component.html',
  styleUrl: './similaritecouleurtexture.component.css'
})
export class SimilaritecouleurtextureComponent implements OnInit {

  queryImage: string = '';  // URL of the query image
  similarImages: string[] = [];  // List of similar images
  loading = false;  // Loading state for the spinner

  constructor(private similariteCouleurTextureService: SimilaritecouleurtextureService) {
  }

  ngOnInit(): void {
    this.loadRandomImage();
  }

  // Load a random image from the backend
  loadRandomImage() {
    this.similarImages = []; // Clear the similar images array
    this.similariteCouleurTextureService.getRandomImage().subscribe({
      next: (path: string) => {
        this.queryImage = `http://localhost:8087${path}`;
        console.log("Image path:", this.queryImage);  // Log for confirmation
      },
      error: (err) => console.error("Error loading image:", err)
    });
  }

  // Search for similar images based on color and texture
  searchSimilarImages() {
    this.loading = true;
    this.similariteCouleurTextureService.getSimilarImages(this.queryImage, 8).subscribe(
      images => {
        this.similarImages = images.map(img => `http://localhost:8087${img}`);
        this.loading = false;
      },
      error => {
        console.error("Error searching for similar images:", error);
        this.loading = false;
      }
    );
  }

}
