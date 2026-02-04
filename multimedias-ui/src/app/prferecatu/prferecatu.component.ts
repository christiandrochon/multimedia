import {Component, OnInit} from '@angular/core';
import {PrferecatuService, PRResponse} from './prferecatu.service';
import {BaseChartDirective} from 'ng2-charts';
import {CommonModule, DecimalPipe, NgFor, NgIf} from '@angular/common';

@Component({
  selector: 'app-prferecatu',
  standalone: true,
  imports: [
    CommonModule, NgIf, NgFor, DecimalPipe, BaseChartDirective
  ],
  templateUrl: './prferecatu.component.html',
  styleUrl: './prferecatu.component.css'
})
export class PrferecatuComponent implements OnInit {
  recall: number[] = [];
  precision: number[] = [];
  loading = true;
  error: string | null = null;

  constructor(private prService: PrferecatuService) {}

  ngOnInit(): void {
    this.prService.getPRCurve().subscribe({
      next: data => {
        this.recall = data.recall;
        this.precision = data.precision;
        this.loading = false;
      },
      error: err => {
        this.error = err.message || 'Erreur inconnue';
        this.loading = false;
      }
    });
  }

}
