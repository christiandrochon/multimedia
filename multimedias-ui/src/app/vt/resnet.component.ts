import {Component, OnInit} from '@angular/core';
import {ResnetService} from './resnet.service';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-resnet',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './resnet.component.html',
  styleUrl: './resnet.component.css'
})
export class ResnetComponent implements OnInit {

  classDescriptors: string[] = [];
  selectedClass: string = '';
  images: string[] = [];
  randomImage: string = ''; // Image aléatoire affichée
  similarityImages: string[] = []; // Images similaires retournées
  loading: boolean = false; // État de chargement pour le spinner

  constructor(private resnetService: ResnetService) {}

  ngOnInit(): void {
    this.loadRandomImage()

    // this.resnetService.getRandomImage().subscribe(image => {
    //   this.randomImage = image;
    //   this.similarityImages = []; // Réinitialiser les résultats de similarité
    // });

    // Charger les classes de descripteur
    // this.resnetService.getClassDescriptors().subscribe(classes => {
    //   this.classDescriptors = classes;
    // });
  }

  // permet à un user de selectionner une classe de descripteur
  // onClassSelect(): void {
  //   // Récupérer une image aléatoire pour la classe sélectionnée
  //   this.resnetService.getRandomImageForClass(this.selectedClass).subscribe(image => {
  //     this.randomImage = image;
  //     this.similarityImages = []; // Réinitialiser les résultats de similarité
  //     // this.cdr.detectChanges(); // Rafraîchir la vue
  //   });
  // }

  // Charge une image aléatoire depuis le backend
  loadRandomImage() {
    this.similarityImages = []; // Réinitialiser les résultats de similarité
    this.resnetService.getRandomImage().subscribe({
      next: (path: string) => {
        this.randomImage = `${path}`;
        console.log("Chemin de l'image :", this.randomImage);  // Affichez pour confirmation
      },
      error: (err) => console.error("Erreur lors du chargement de l'image:", err)
    });
  }

  onSimilaritySearch(): void {
    console.log('Similarity search triggered');
    const imageName = this.randomImage.split('/').pop(); // Obtenir seulement le nom du fichier
    console.log('Similarity search triggered for', imageName);

    this.resnetService.getImagesForClassWithSimilarity(this.selectedClass, <string>imageName).subscribe(images => {
      this.similarityImages = images;
    });
  }
}
