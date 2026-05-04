import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';

interface QuizQuestion {
  question: string;
  options: string[];
  correctIndex: number;
}

@Component({
  selector: 'app-quiz',
  standalone: true,
  templateUrl: './quiz.component.html',
  styleUrl: './quiz.component.scss',
})
export class QuizComponent implements OnInit {
  private http = inject(HttpClient);

  questions: QuizQuestion[] = [];
  selectedAnswers: (number | null)[] = [];
  submitted = false;
  score = 0;

  private readonly fallbackQuestions: QuizQuestion[] = [
    {
      question: 'Mida tähendab lühend „UCAV“?',
      options: [
        'Unmanned Combat Aerial Vehicle (mehitamata lahinguõhusõiduk)',
        'Universal Command Aviation Unit',
        'Unified Control Air Vessel',
        'Unmanned Carrier Attack Vehicle',
      ],
      correctIndex: 0,
    },
    {
      question: 'Milline droon sai laialdaselt tuntuks 2020. aasta Mägi-Karabahhi konflikti ajal?',
      options: [
        'MQ-9 Reaper',
        'Bayraktar TB2',
        'RQ-4 Global Hawk',
        'Predator C Avenger',
      ],
      correctIndex: 1,
    },
    {
      question: 'Mis tüüpi relv on Switchblade 600?',
      options: [
        'Tiibrakett',
        'Luuredroon',
        'Hõljuv lahingumoon (loitering munition)',
        'Strateegiline pommitaja-droon',
      ],
      correctIndex: 2,
    },
    {
      question: 'Mille jaoks on mõeldud nn „loyal wingman“ droon?',
      options: [
        'Asendada täielikult inimpiloodid',
        'Tegutseda mehitatud hävituslennukite kõrval',
        'Toimetada kohale humanitaarabi',
        'Teostada süvamere seiret',
      ],
      correctIndex: 1,
    },
  ];

  ngOnInit(): void {
    this.http
      .get<QuizQuestion[]>('/api/v1/quiz/drones')
      .pipe(catchError(() => of(this.fallbackQuestions)))
      .subscribe((questions) => {
        this.questions = questions;
        this.selectedAnswers = new Array(questions.length).fill(null);
      });
  }

  selectAnswer(questionIndex: number, optionIndex: number): void {
    if (this.submitted) return;
    this.selectedAnswers[questionIndex] = optionIndex;
  }

  submit(): void {
    if (this.selectedAnswers.some((a) => a === null)) return;
    this.submitted = true;
    this.score = this.questions.reduce(
      (acc, q, i) => acc + (this.selectedAnswers[i] === q.correctIndex ? 1 : 0),
      0
    );
  }

  reset(): void {
    this.submitted = false;
    this.score = 0;
    this.selectedAnswers = new Array(this.questions.length).fill(null);
  }

  get allAnswered(): boolean {
    return this.selectedAnswers.every((a) => a !== null);
  }

  get scorePercent(): number {
    if (this.questions.length === 0) return 0;
    return Math.round((this.score / this.questions.length) * 100);
  }
}
