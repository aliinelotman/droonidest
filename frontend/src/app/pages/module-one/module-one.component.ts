import {
  Component,
  AfterViewInit,
  OnDestroy,
  QueryList,
  ViewChildren,
  ElementRef,
  ViewChild,
  inject,
  computed,
} from '@angular/core';
import { HeroSectionComponent } from '../../components/hero-section/hero-section.component';
import { InfoCardComponent } from '../../components/info-card/info-card.component';
import { NarrativeSectionComponent } from '../../components/narrative-section/narrative-section.component';
import {
  NarrativeTrioComponent,
  NarrativeTrioItem,
} from '../../components/narrative-trio/narrative-trio.component';
import { GalleryComponent } from '../../components/gallery/gallery.component';
import { FunFactComponent } from '../../components/fun-fact/fun-fact.component';
import { ClosingSectionComponent } from '../../components/closing-section/closing-section.component';
import { QuizComponent } from '../../components/quiz/quiz.component';
import { DroneAssemblyComponent } from '../../components/drone-assembly/drone-assembly.component';
import {
  SectionNavComponent,
  NavSection,
  ModuleLink,
} from '../../components/section-nav/section-nav.component';
import { ModuleCardComponent } from '../../components/module-card/module-card.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-module-one',
  standalone: true,
  imports: [
    HeroSectionComponent,
    InfoCardComponent,
    NarrativeSectionComponent,
    NarrativeTrioComponent,
    GalleryComponent,
    FunFactComponent,
    ClosingSectionComponent,
    QuizComponent,
    DroneAssemblyComponent,
    SectionNavComponent,
    ModuleCardComponent,
  ],
  templateUrl: './module-one.component.html',
  styleUrl: './module-one.component.scss',
})
export class ModuleOneComponent implements AfterViewInit, OnDestroy {
  protected readonly authService = inject(AuthService);

  @ViewChildren('fadeTarget') fadeTargets!: QueryList<ElementRef>;
  @ViewChild('sectionNav') sectionNav!: SectionNavComponent;

  private fadeObserver!: IntersectionObserver;
  private navObserver!: IntersectionObserver;

  readonly isModuleTwoLocked = computed(() => !this.authService.currentUser());

  navSections: NavSection[] = [
    { id: 'hero', label: 'Sissejuhatus' },
    { id: 'overview', label: 'Ülevaade' },
    { id: 'exercise', label: 'Harjutus' },
    { id: 'gallery', label: 'Galerii' },
    { id: 'operations', label: 'Juhtimine' },
    { id: 'quiz', label: 'Test' },
    { id: 'modules', label: 'Moodulid' },
  ];

  moduleLinks: ModuleLink[] = [
    { label: 'Moodul 1', route: '/', active: true },
    { label: 'Moodul 2', route: '/module-two' },
  ];

  multirotorTrioItems: NarrativeTrioItem[] = [
    {
      body: `Mitme tiivikuga väikesed droonid, mida kasutatakse palju ka hobideks ja igapäevatööks väljaspool sõjakonteksti, nö 'tavaline poedroon'. Kuidas teda ära tunda? Nagu droonifotograaf Kaupo Kalda kirjutab: 'Tavaline poedroon näeb välja nagu lendav kassipoeg, jalad laiali. Ta on umbes paberilehe suurune, tal on 4 käppa, 4 propellerit ja tihti seisatab, teeb pilti ja vahetab taas asukohta.'

Rootorite arv võib sellisel droonil olla kolmest kaheksani, tavaliselt oleme harjunud nägema nelja rootoriga droone.`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/c/c9/DJI_Phantom_4_Being_Released_from_Ship.jpg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt: 'DJI Phantom 4 kvadrokopter laevatekilt õhku tõusmas (Wikimedia Commons)',
    },
    {
      body: `Kui sellisele droonile lisada juurde kaamera, saab temast 'first person view' ehk FPV droon ning seda juhitakse kaamerast nähtava otsepildi abil, mida edastatakse raadiolainete teel droonipiloodile. Sõjalises kontekstis kasutatakse multirootor ja FPV droone näiteks luurel kas tava- või termokaamera pildi abil andmete kogumiseks. Samuti on Ukraina sõjas kasutusel ka poedroonidest ümber ehitatud või kohapeal valmistatud FPV droone, millele lisatakse väike lõhkekeha.`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/b/b5/Sky_Soldiers_test_FlyingBasket_drone_for_transporting_equipment_and_injuries_%288913243%29.jpg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt:
        'USA Sky Soldiers sõdurid testivad FlyingBasket transpordidrooni varustuse ja vigastatute teisaldamiseks (Wikimedia Commons)',
    },
    {
      body: `Kuna rindel kasutavad mõlemad osapooled raadiolainete segajaid, on viimastel aastatel väikeseid FPV rünnakdroone hakatud juhtima fiiberoptiliste kaabite abil, mida droon lennates alaosas asuvast kassetist lahti kerib. Kaabli abil saadetakse piloodile videopilti ning piloodilt tagasi droonile sõidujuhiseid.`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/c/c1/TT-Copter_OctoCOpter_high_lifting.png?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt: 'TT-Copter oktokopter ehk kaheksa rootoriga droon raskeid esemeid tõstmas (Wikimedia Commons)',
    },
  ];

  attackDroneTrioItems: NarrativeTrioItem[] = [
    {
      body: `Suuremõõdulised ründedroonid erinevad välimuselt FPV või multirootordroonidest märkimisväärselt – need sarnanevad pigem väikelennukitele, deltaplaanidele või tiibrakettidele. Nende kehaehitus on aerodünaamilisem, tiivaulatused suuremad ning lennukaugus ja kandejõud oluliselt suuremad kui väikedroondidel. Tuntud näited on Iraani päritolu Shahed-136 – ühekordseks kasutamiseks mõeldud kamikaze-droon, mida on Ukraina sõjas laialdaselt kasutatud – ning Türgi valmistatav Bayraktar TB2, mis suudab kanda kuni 150 kg ja lennata kuni 27 tundi järjest.`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/e/eb/PAF_Bayraktar_TB2_at_Radom-2023.jpg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt:
        'Türgi valmistatud Bayraktar TB2 ründedroon Radomi lennundusmessil 2023 (Wikimedia Commons)',
    },
    {
      body: `Suuremõõtmeliste ründedroonide põhieesmärk on sihtmärgi hävitamine. Sisuliselt toimivad need juhitavate rakettidena – kas toimetades plahvatuslõhkelaengu otse sihtmärgini (nagu Shahed-136) või lastes täpsusrelvi maapinnale (nagu TB2). Ohtlikuks teeb need eelkõige suur lennukaugus, madal lennutrajektoor ning kamikaze-tüüpi droonide puhul ka suhteline odavus, mistõttu neid kasutatakse tihti suurtes kogustes korraga.`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/0/09/Shahed_136_rendering.png?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt:
        'Iraani päritolu Shahed-136 kamikaze-drooni illustratsioon (Wikimedia Commons)',
    },
    {
      body: `Kui suuremõõduline ründedroon satub Eesti õhuruumi, on tegemist võimaliku õhuohuga. Sellisel juhul teavitab Kaitsevägi tsiviilisikuid kas SMSi kaudu (ES-Alert süsteem) või sireenihäirega. Loe lähemalt, kuidas sel juhul käituda, siit: kriis.ee/droonioht`,
      imageUrl:
        'https://upload.wikimedia.org/wikipedia/commons/7/73/Remains_of_Shahed_drone_in_Chernihiv_Oblast%2C_2026-02-19_%2801%29.jpg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      imageAlt:
        'Allatulistatud Shahed-drooni rusud Tšernihivi oblastis, 19. veebruar 2026 (Wikimedia Commons)',
    },
  ];

  ngAfterViewInit(): void {
    this.setupFadeObserver();
    this.setupNavObserver();
  }

  ngOnDestroy(): void {
    this.fadeObserver?.disconnect();
    this.navObserver?.disconnect();
  }

  private setupFadeObserver(): void {
    this.fadeObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
          }
        });
      },
      { threshold: 0.15 }
    );

    this.fadeTargets.forEach((el) => this.fadeObserver.observe(el.nativeElement));
  }

  private setupNavObserver(): void {
    this.navObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && this.sectionNav) {
            this.sectionNav.setActive(entry.target.id);
          }
        });
      },
      { threshold: 0.3 }
    );

    this.navSections.forEach((s) => {
      const el = document.getElementById(s.id);
      if (el) this.navObserver.observe(el);
    });
  }
}
