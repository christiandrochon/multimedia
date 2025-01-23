import {Component, OnInit} from '@angular/core';
import {PrecisionRecallService} from './precision-recall.service';
import Chart from 'chart.js/auto';
import {registerables} from 'chart.js';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-precision-recall',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './precision-recall.component.html',
  styleUrls: ['./precision-recall.component.css']
})
export class PrecisionRecallComponent implements OnInit {
  selectedImage: string | null = null;
  topK: number = 100;
  nearestNeighborImages: string[] = [];
  imageUrl: string = '';
  private chart: Chart | null = null; // Référence au graphique
  loading = false; // État de chargement pour le spinner

  constructor(private precisionRecallService: PrecisionRecallService) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.generateRandomImage()
  }

  /**
   * Charge une image aléatoire, affiche les courbes et les 8 images voisines les plus proches, et appelle la fonction de génération de courbe.
   */
  generateRandomImage(): void {
    this.loading = true; // Démarrer le chargement
    this.precisionRecallService.getRandomImage().subscribe({
      next: (path: string) => {
        const imageName = path.split('/').pop(); // Récupère uniquement le nom de l'image
        this.selectedImage = imageName || ''; // Stocke uniquement le nom de l'image
        this.imageUrl = `http://localhost:8087/images/${imageName}`; // Construit l'URL complète pour l'affichage
        console.log('Nom de l\'image :', this.selectedImage);
        console.log('URL de l\'image :', this.imageUrl);

        // Charger les courbes et les voisins
        this.generateCurve();
      },
      error: (err) => {
        console.error('Erreur lors du chargement de l\'image :', err);
        this.loading = false; // Arrêter le chargement en cas d'erreur
      },
    });
  }

  /**
   * Génère la courbe précision/rappel et charge les voisins proches pour l'image sélectionnée.
   * POur chaque nouvelle génération d'image aleatoire, on effac d'abord les 8 images et le diagramme
   */
  generateCurve(): void {
    if (!this.selectedImage) {
      alert('Aucune image sélectionnée.');
      this.loading = false;
      return;
    }

    this.precisionRecallService
      .getPrecisionRecall(this.selectedImage, this.topK)
      .subscribe({
        next: (data) => {
          // Afficher les courbes précision/rappel
          this.renderChart(data.Precision, data.Recall);

          // Construire les URLs pour les 8 images les plus proches voisins
          this.nearestNeighborImages = data.nearestNeighbors.map(
            (imageName) => `http://localhost:8087/images/${imageName}`
          );

          console.log('Données reçues :', data);
          console.log('Images des voisins les plus proches :', this.nearestNeighborImages);

          this.loading = false; // Arrêter le chargement une fois les données reçues
        },
        error: (err) => {
          console.error('Erreur lors de la génération de la courbe :', err);
          this.loading = false; // Arrêter le chargement en cas d'erreur
        },
      });
  }

  /**
   * Affiche le graphique de précision/rappel
   * @param precision Données de précision
   * @param recall Données de rappel
   */
  renderChart(precision: { recall: number; precision: number }[], recall: { recall: number; precision: number }[]): void {
    console.log('Début du rendu du graphique...');

    // Vérifiez si un graphique existe déjà et détruisez-le
    if (this.chart) {
      console.log('Destruction de l\'ancien graphique...');
      this.chart.destroy();
    }

    // Extraire les valeurs de précision et de rappel
    const recallValues = precision.map((point) => point.recall); // Abscisses
    const precisionValues = precision.map((point) => point.precision); // Ordonnées

    console.log('Rappel (x-axis) :', recallValues);
    console.log('Précision (y-axis) :', precisionValues);

    // Vérifiez que les données ne sont pas vides
    if (recallValues.length === 0 || precisionValues.length === 0) {
      console.error('Les données pour le graphique sont vides.');
      return;
    }

    // Rechercher l'élément canvas
    const canvas = document.getElementById('precisionRecallChart') as HTMLCanvasElement;
    if (!canvas) {
      console.error('Élément canvas introuvable.');
      return;
    }

    // Créer le graphique
    this.chart = new Chart(canvas, {
      type: 'line',
      data: {
        labels: recallValues,
        datasets: [
          {
            label: 'Courbe Précision/Rappel',
            data: precisionValues,
            borderColor: 'blue',
            borderWidth: 1,
            pointRadius: 2,
            fill: false,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
          },
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Rappel',
            },
          },
          y: {
            title: {
              display: true,
              text: 'Précision',
            },
          },
        },
      },
    });

    console.log('Graphique rendu avec succès.');
  }

}
