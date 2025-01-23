import { Component } from '@angular/core';
import { DescripteurService } from '../service/descripteur.service';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-descripteur',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './descripteur.component.html',
  styleUrl: './descripteur.component.css'
})
export class DescripteurComponent {

  message: string | null = null;
  loading: boolean = false;

  constructor(private descripteurService: DescripteurService) { }

  generateRVB666() {
    this.loading = true;
    this.descripteurService.processRVB666().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du RVB 6x6x6';
        this.loading = false;
      }
    );
  }

  generateRVB444() {
    this.loading = true;
    this.descripteurService.processRVB444().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du RVB 4x4x4';
        this.loading = false;
      }
    );
  }

  generateRVB222() {
    this.loading = true;
    this.descripteurService.processRVB222().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du RVB 2x2x2';
        this.loading = false;
      }
    );
  }

  generateGrey256() {
    this.loading = true;
    this.descripteurService.processGrey256().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du gris 256';
        this.loading = false;
      }
    );
  }

  generateGrey64() {
    this.loading = true;
    this.descripteurService.processGrey64().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du gris 64';
        this.loading = false;
      }
    );
  }

  generateGrey16() {
    this.loading = true;
    this.descripteurService.processGrey16().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération du gris 16';
        this.loading = false;
      }
    );
  }

  generateForme() {
    this.loading = true;
    this.descripteurService.processForme().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération de la forme';
        this.loading = false;
      }
    );
  }

  generateTexture() {
    this.loading = true;
    this.descripteurService.processTexture().subscribe(
      (response) => {
        this.message = response;
        this.loading = false;
      },
      (error) => {
        this.message = 'Erreur lors de la génération de la texture';
        this.loading = false;
      }
    );
  }
}
