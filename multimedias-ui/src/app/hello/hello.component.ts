import {Component, OnInit} from '@angular/core';
import {HelloService} from './hello.service';

@Component({
  selector: 'app-hello',
  standalone: true,
  imports: [],
  templateUrl: './hello.component.html',
  styleUrl: './hello.component.css'
})
export class HelloComponent implements OnInit {
  greeting: string | undefined;

  constructor(private helloService: HelloService) {
  }

  ngOnInit(): void {
    this.helloService.getGreeting().subscribe(
      (data: string) => {
        this.greeting = data;
      },
      (error) => {
        console.error('Error fetching data from backend', error);
      }
    );
  }

}
