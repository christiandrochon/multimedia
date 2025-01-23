import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ResnetseulService} from './resnetseul.service';

@Component({
  selector: 'app-resnetseul',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './resnetseul.component.html',
  styleUrl: './resnetseul.component.css'
})
export class ResnetseulComponent implements OnInit {

  classDescriptors: string[] = [];
  selectedClass: string = '';
  images: string[] = [];
  randomImage: string = ''; // Image aléatoire affichée
  similarityImages: string[] = []; // Images similaires retournées
  loading: boolean = false; // État de chargement pour le spinner


  constructor(private resnetService: ResnetseulService) {}

  ngOnInit(): void {
    this.loadClassDescriptors();
    // this.loadRandomImage();
  }
  onClassSelect(): void {
    this.similarityImages = [];
    this.loadRandomImage(this.selectedClass);
  }

  loadRandomImage(className: string): void {
    this.resnetService.getRandomImage(className).subscribe({
      next: (path: string) => {
        this.randomImage = path;
      },
      error: (err) => console.error("Erreur lors du chargement de l'image aléatoire:", err)
    });
  }

  loadClassDescriptors() {
    this.resnetService.getClassDescriptors().subscribe({
      next: (descriptors: string[]) => {
        this.classDescriptors = descriptors;
      },
      error: (err) => console.error("Error loading class descriptors:", err)
    });
  }

  onSimilaritySearch(): void {
    console.log('Similarity search triggered');
    const imageName = this.randomImage.split('/').pop(); // Obtenir seulement le nom du fichier
    console.log('Similarity search triggered for', imageName);

    this.resnetService.getImagesForClass(this.selectedClass, <string> imageName).subscribe(images => {
      this.similarityImages = images;
    });
  }
}
