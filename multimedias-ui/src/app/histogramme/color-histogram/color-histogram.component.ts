import {Component, OnInit} from '@angular/core';
import {HistogrammeService} from '../histogrammeservice/histogramme.service';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {BaseChartDirective} from 'ng2-charts';

@Component({
  selector: 'app-color-histogram',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './color-histogram.component.html',
  styleUrl: './color-histogram.component.css'
})
export class ColorHistogramComponent implements OnInit {

  numBins: number = 8;  // Valeur par défaut pour le nombre de bins
  imageUrl: string = '';  // URL de l'image

  // Histogrammes RVB
  averageHistogram: number[] = [];
  redHistogram: number[] = [];
  greenHistogram: number[] = [];
  blueHistogram: number[] = [];

  // Intervalles des niveaux de couleur
  binRanges: any[] = [];
  redAverages: number[] = [];
  greenAverages: number[] = [];
  blueAverages: number[] = [];
  binRangesRed: { label: string, start: number, end: number }[] = [];
  binRangesGreen: { label: string, start: number, end: number }[] = [];
  binRangesBlue: { label: string, start: number, end: number }[] = [];

  // Configuration des données pour le graphique
  chartData: any[] = [];
  chartLabels: string[] = [];
  chartOptions = {
    responsive: true,
    scales: {
      x: { title: { display: true, text: 'Bins' } },
      y: { title: { display: true, text: 'Pixels' } }
    }
  };

  constructor(private histogramService: HistogrammeService) {
  }

  ngOnInit(): void {
    // this.updateBins();
    this.binRanges = this.generateBinRanges(this.numBins);
    this.loadHistogram();
    // this.fetchRandomImage();
  }

  // Charger l'histogramme moyen depuis l'API ainsi que chaque valeur de RVB de chacun des bins en dessous du chart
  loadHistogram(): void {
    this.histogramService.getRGBAndAverageHistogram(this.numBins).subscribe(response => {
      this.averageHistogram = response.averageHistogram;
      this.redHistogram = response.redHistogram;
      this.greenHistogram = response.greenHistogram;
      this.blueHistogram = response.blueHistogram;
      this.redAverages = response.redAverages;
      this.greenAverages = response.greenAverages;
      this.blueAverages = response.blueAverages;
      this.imageUrl = response.imageUrl;

      // Générer les plages des bins
      this.binRanges = this.generateBinRanges(this.numBins);

      // Mettre à jour le graphique
      this.updateChart();
    }, error => {
      console.error('Erreur lors du chargement de l\'histogramme', error);
    });
  }


  // Mettre à jour les données du graphique
  updateChart(): void {
    console.log('Updating chart with data:', this.averageHistogram);
    // Générer les étiquettes des bins
    this.chartLabels = Array.from({ length: this.numBins }, (_, i) => `Bin ${i + 1}`);

    // Mettre à jour les données du graphique
    this.chartData = [{
      data: this.averageHistogram,
      label: 'Histogramme moyen',
      backgroundColor: 'rgba(75,192,192,0.6)',
      borderColor: 'rgba(75,192,192,1)',
      borderWidth: 1
    }];
  }

  // Mettre à jour le nombre d'intervalles
  updateBins(): void {
    this.binRanges = this.generateBinRanges(this.numBins);
    this.loadHistogram();
  }

  generateBinRanges(numBins: number): any[] {
    const binSize = Math.floor(256 / numBins); // Taille de chaque bin
    const bins = [];

    for (let i = 0; i < numBins; i++) {
      const start = i * binSize;
      const end = (i + 1) * binSize - 1;

      // Calcul de la valeur moyenne RGB pour le bin
      const binAverage = Math.floor((start + end) / 2);

      bins.push({
        label: `Bin ${i + 1}`,
        start: start,
        end: end,
        redValue: binAverage,    // Valeur moyenne du rouge pour ce bin
        greenValue: binAverage,  // Valeur moyenne du vert pour ce bin
        blueValue: binAverage    // Valeur moyenne du bleu pour ce bin
      });

      // bins.push({
      //   label: `Bin ${i + 1}`,
      //   start: start,
      //   end: end
      // });

      // bins.push({
      //   label: `Bin ${i + 1}`,
      //   redStart: start,
      //   redEnd: end,
      //   greenStart: start,
      //   greenEnd: end,
      //   blueStart: start,
      //   blueEnd: end
      // });
    }

    return bins;
  }


  // Méthode appelée pour charger une nouvelle image et les histogrammes RVB
  // loadHistogram(): void {
  //   this.histogramService.getRGBHistogram(this.numBins).subscribe(
  //     (response: { redHistogram: number[], greenHistogram: number[], blueHistogram: number[], imageUrl: string }) => {
  //       this.redHistogram = response.redHistogram;
  //       this.greenHistogram = response.greenHistogram;
  //       this.blueHistogram = response.blueHistogram;
  //       this.imageUrl = response.imageUrl;
  //
  //       this.updateChart();  // Mettre à jour le graphique avec les nouvelles données
  //     },
  //     (error) => {
  //       console.error('Erreur lors du chargement de l\'histogramme', error);
  //     }
  //   );
  // }
  //
  // updateChart(): void {
  //   this.chartLabels = Array.from({ length: this.numBins }, (_, i) => `Bin ${i}`);
  //
  //   this.chartData = [
  //     { data: this.redHistogram, label: 'Rouge', backgroundColor: 'rgba(255, 99, 132, 0.5)', borderColor: 'rgba(255, 99, 132, 1)' },
  //     { data: this.greenHistogram, label: 'Vert', backgroundColor: 'rgba(75, 192, 192, 0.5)', borderColor: 'rgba(75, 192, 192, 1)' },
  //     { data: this.blueHistogram, label: 'Bleu', backgroundColor: 'rgba(54, 162, 235, 0.5)', borderColor: 'rgba(54, 162, 235, 1)' }
  //   ];
  // }
  //
  // // Méthode appelée pour calculer et afficher les intervalles des bins pour chaque couleur
  // updateBins(): void {
  //   const binSize = Math.floor(256 / this.numBins);
  //
  //   // Réinitialisation des intervalles
  //   this.binRangesRed = [];
  //   this.binRangesGreen = [];
  //   this.binRangesBlue = [];
  //
  //   for (let i = 0; i < this.numBins; i++) {
  //     const start = i * binSize;
  //     const end = (i === this.numBins - 1) ? 255 : (start + binSize - 1);
  //     this.binRangesRed.push({ label: `Bin ${i}`, start, end });
  //     this.binRangesGreen.push({ label: `Bin ${i}`, start, end });
  //     this.binRangesBlue.push({ label: `Bin ${i}`, start, end });
  //   }
  // }

  // Méthode pour recharger une nouvelle image aléatoire
  loadNewImage(): void {
    this.loadHistogram();
  }
}
