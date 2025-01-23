import {Component, OnInit} from '@angular/core';
import {HistogrammeService} from '../histogrammeservice/histogramme.service';
import {ActivatedRoute} from '@angular/router';
import {CommonModule} from '@angular/common';
import {BaseChartDirective} from 'ng2-charts';
import { FormsModule } from '@angular/forms';

//ATTENTION!!!! POur les charts!!!!
// Importations spécifiques de Chart.js
import { Chart, registerables } from 'chart.js';  // Importation des composants nécessaires de Chart.js
// Enregistrement des composants nécessaires de Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-grey-histogram',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, FormsModule],
  templateUrl: './grey-histogram.component.html',
  styleUrl: './grey-histogram.component.css'
})
export class GreyHistogramComponent implements OnInit {

  grayHistogram: number[] = [];  // Histogramme reçu du serveur
  numBins: number = 8;  // Valeur par défaut
  imageUrl: string = '';  // URL de l'image renvoyée par le serveur
  binRanges: { label: string, start: number, end: number }[] = [];

  // Configuration des données pour le chart
  chartData: any[] = [];
  chartLabels: string[] = [];
  chartOptions = {
    responsive: true,
    scales: {
      x: { title: { display: true, text: 'Intervalles de gris' } },
      y: { title: { display: true, text: 'Pixels' } }
    }
  };

  constructor(
    private histogramService: HistogrammeService,
    private route: ActivatedRoute  // Pour récupérer le paramètre de bins depuis l'URL
  ) {
  }

  ngOnInit(): void {
    this.updateBins();  // Calcul initial des bins
    this.loadHistogram();  // Charger une image lors de l'initialisation

    // Récupérer le paramètre bins depuis l'URL
    // this.route.params.subscribe(params => {
    //   this.updateBins();
    //   this.numBins = +params['bins'] || 8;  // Si le paramètre n'est pas défini, utilise 8 par défaut
    //   this.loadHistogram();
    // });
  }

  loadHistogram(): void {
    // Appel au service pour récupérer l'histogramme
    this.histogramService.getGrayLevelHistogram(this.numBins).subscribe(
      (response: { histogram: number[], imageUrl: string }) => {
        this.grayHistogram = response.histogram;  // Histogramme reçu du serveur
        this.imageUrl = response.imageUrl;  // URL de l'image reçue du serveur
        this.updateChart();  // Mettre à jour le graphique
      },
      (error) => {
        console.error('Erreur lors du chargement de l\'histogramme', error);
      }
    );
  }

  updateChart(): void {
    // Crée les labels pour chaque bin
    this.chartLabels = this.grayHistogram.map((_, i) => `Bin ${i}`);

    // Met à jour les données du chart
    this.chartData = [{
      data: this.grayHistogram,
      label: 'Histogramme en Niveaux de Gris'
    }];
  }

  // Méthode appelée pour calculer et afficher les intervalles des bins
  updateBins(): void {
    const binSize = Math.floor(256 / this.numBins);  // Taille de chaque bin
    this.binRanges = [];  // Réinitialiser les intervalles des bins

    for (let i = 0; i < this.numBins; i++) {
      const start = i * binSize;
      const end = (i === this.numBins - 1) ? 255 : (start + binSize - 1);
      this.binRanges.push({ label: `Bin ${i}`, start, end });
    }
  }

  // Méthode appelée pour charger une nouvelle image
  loadNewImage(): void {
    this.loadHistogram();  // Appel à la méthode existante pour recharger une nouvelle image
  }


  // fetchRandomImage(): void {
  //   // Appel API qui récupère un fichier JSON avec les informations de l'image
  //   this.http.get<any>('http://localhost:8087/histogramme-gris').subscribe(data => {
  //     this.imageUrl = data.imageUrl;  // URL de l'image pour affichage
  //     this.extractImageData(data.imageData);  // Données de l'image en base64
  //   });
  // }
  //
  // extractImageData(base64ImageData: string): void {
  //   // Création d'une nouvelle image à partir des données base64
  //   const img = new Image();
  //   img.src = base64ImageData;
  //   img.onload = () => {
  //     // Création d'un canvas temporaire pour extraire les données des pixels
  //     const canvas = document.createElement('canvas');
  //     canvas.width = img.width;
  //     canvas.height = img.height;
  //     const ctx = canvas.getContext('2d');
  //     if (ctx) {
  //       ctx.drawImage(img, 0, 0);
  //       const imageData = ctx.getImageData(0, 0, img.width, img.height);
  //       // Calcul de l'histogramme en niveaux de gris à partir des données des pixels
  //       this.grayHistogram = this.histogramService.computeGrayLevelHistogram(imageData.data, 16);
  //     }
  //   };
  // }
}
