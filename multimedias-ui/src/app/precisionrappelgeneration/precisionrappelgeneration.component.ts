import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PrecisionrappelgenerationService} from './precisionrappelgeneration.service';

@Component({
  selector: 'app-precisionrappelgeneration',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './precisionrappelgeneration.component.html',
  styleUrl: './precisionrappelgeneration.component.css'
})
export class PrecisionrappelgenerationComponent {
  isLoading: boolean = false; // État pour afficher/masquer le spinner
  responseMessage: string = ''; // Message de réponse à afficher à l'utilisateur

  constructor(private prService: PrecisionrappelgenerationService) {}

  generateJson(type: 'gris' | 'couleur' | 'gris-couleur-resnet'): void {
    this.isLoading = true; // Activer le spinner

    let serviceCall;
    switch (type) {
      case 'gris':
        serviceCall = this.prService.processPRGris();
        break;
      case 'couleur':
        serviceCall = this.prService.processPRCouleur();
        break;
      case 'gris-couleur-resnet':
        serviceCall = this.prService.processPRTous();
        break;
      default:
        this.responseMessage = 'Type de traitement invalide.';
        this.isLoading = false;
        return;
    }

    // Appeler le service et gérer la réponse
    serviceCall.subscribe(
      (response) => {
        this.responseMessage = `Succès : ${response}`;
        this.isLoading = false; // Désactiver le spinner après succès
      },
      (error) => {
        this.responseMessage = `Erreur : ${error.message}`;
        this.isLoading = false; // Désactiver le spinner après une erreur
      }
    );
  }
}
