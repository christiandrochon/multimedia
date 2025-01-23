import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  urls = [
    { path: '/hello', description: 'Test' },
    { path: '/images', description: 'Affichage dynamique de 16 images aléatoires et de boutons interactifs' },
    { path: '/createCheckerboard', description: 'Création d\'un damier' },
    { path: '/swapRandomImage', description: 'Inversion des couleurs vert sur bleu sur une image aléatoire' },
    { path: '/histogramme-gris/:bins', description: 'Visualisation dynamique des histogrammes de niveau de gris sur une image aléatoire. Possibilité de changer le nombre de bins' },
    { path: '/histogramme-couleur/:bins', description: 'Visualisation dynamique des histogrammes couleur sur une image aléatoire. Possibilité de changer le nombre de bins' },
    { path: '/descripteurs', description: 'Génération de histogrammes de gris ou de couleur' },
    { path: '/similaritecouleur', description: 'Recherche par similarité sur les histogrammes couleur (le niveau de couleur est à definir dans le code)' },
    { path: '/resnetclasses', description: 'Recherche par similarité avec descripteurs ResNet uniquement' },
    { path: '/VTclasses/similar', description: 'Recherche par similarité avec classes VT et histogrammes  de 216 couleurs' },
    { path: '/similaritetexture', description: 'Recherche par similarité sur les descripteurs de texture' },
    { path: '/similaritecouleurtexture', description: 'Recherche par similarité sur les descripteurs de couleur et de texture' },
    { path: '/similariteforme', description: 'Recherche par similarité sur les descripteurs de forme' },
    { path: '/similariteglobale', description: 'Recherche par similarité sur les descripteurs de couleur + forme + texture' },
    { path: '/evaluation', description: 'Affichage des courbes de précision/rappel pour une classe de descripteurs' },
    {path: '/precision-rappel', description: 'Génération des courbes de précision/rappel pour les descripteurs'},
  ];
}
