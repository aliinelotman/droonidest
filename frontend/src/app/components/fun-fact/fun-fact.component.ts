import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-fun-fact',
  standalone: true,
  templateUrl: './fun-fact.component.html',
  styleUrl: './fun-fact.component.scss',
})
export class FunFactComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value!: string;
  @Input({ required: true }) description!: string;
}
