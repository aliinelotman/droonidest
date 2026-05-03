import { Component, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import {
  CdkDragDrop,
  CdkDrag,
  CdkDropList,
} from '@angular/cdk/drag-drop';

export interface DronePart {
  id: string;
  name: string;
  description: string;
  correctZones: string[];
  quantity: number;
  remaining: number;
}

export interface DropZone {
  id: string;
  partId: string;
  parts: DronePart[];
}

const PART_DEFINITIONS: ReadonlyArray<Omit<DronePart, 'remaining'>> = [
  {
    id: 'frame',
    name: 'Raam',
    description: 'Drooni põhikonstruktsioon, mis hoiab kõik komponendid koos. Tavaliselt süsinikkiust.',
    correctZones: ['zone-frame'],
    quantity: 1,
  },
  {
    id: 'fc',
    name: 'Lennukontroller',
    description: 'Drooni "aju" – töötleb anduriandmeid ja annab käsklusi mootoritele.',
    correctZones: ['zone-fc'],
    quantity: 1,
  },
  {
    id: 'esc',
    name: 'ESC',
    description: 'Elektrooniline kiiruseregulaator – juhib mootorite kiirust lennukontrolleri käskude põhjal.',
    correctZones: ['zone-esc'],
    quantity: 1,
  },
  {
    id: 'motor',
    name: 'Mootor',
    description: 'Brushless mootor, mis paneb propelleri pöörlema. Quadcopteril on neid neli – üks iga arme tipus.',
    correctZones: ['zone-motor-tl', 'zone-motor-tr', 'zone-motor-bl', 'zone-motor-br'],
    quantity: 4,
  },
  {
    id: 'propeller',
    name: 'Propeller',
    description: 'Tekitab tõstejõudu mootori pöörlemise abil. Pooled pöörlevad päripäeva, pooled vastupäeva.',
    correctZones: ['zone-propeller-tl', 'zone-propeller-tr', 'zone-propeller-bl', 'zone-propeller-br'],
    quantity: 4,
  },
  {
    id: 'camera',
    name: 'FPV kaamera',
    description: 'Edastab piloodile reaalajas pilti drooni vaatest videosaatja kaudu.',
    correctZones: ['zone-camera'],
    quantity: 1,
  },
  {
    id: 'vtx',
    name: 'Videosaatja',
    description: 'Saadab kaamera videopilti raadiosignaalina prillidesse või ekraanile.',
    correctZones: ['zone-vtx'],
    quantity: 1,
  },
  {
    id: 'receiver',
    name: 'Vastuvõtja',
    description: 'Võtab vastu juhtimissignaale piloodi puldist ja edastab need lennukontrollerile.',
    correctZones: ['zone-receiver'],
    quantity: 1,
  },
  {
    id: 'battery',
    name: 'Aku',
    description: 'LiPo aku, mis annab toite kõigile drooni komponentidele. Tavaliselt 3S–6S.',
    correctZones: ['zone-battery'],
    quantity: 1,
  },
  {
    id: 'antenna',
    name: 'Antenn',
    description: 'Parandab videosaatja signaali tugevust ja ulatust. Sageli ringpolarisatsiooniga.',
    correctZones: ['zone-antenna'],
    quantity: 1,
  },
];

const PART_ICONS: Record<string, string> = {
  frame: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><line x1="4" y1="4" x2="20" y2="20"/><line x1="20" y1="4" x2="4" y2="20"/><rect x="8" y="8" width="8" height="8" rx="1.5"/><circle cx="4" cy="4" r="2"/><circle cx="20" cy="4" r="2"/><circle cx="4" cy="20" r="2"/><circle cx="20" cy="20" r="2"/></svg>`,

  fc: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="8" y="8" width="8" height="8" rx="1"/><line x1="12" y1="4" x2="12" y2="8"/><line x1="12" y1="16" x2="12" y2="20"/><line x1="4" y1="12" x2="8" y2="12"/><line x1="16" y1="12" x2="20" y2="12"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/></svg>`,

  esc: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="3" y="7" width="18" height="10" rx="2"/><line x1="7" y1="10" x2="7" y2="14"/><line x1="10" y1="10" x2="10" y2="14"/><line x1="13" y1="10" x2="13" y2="14"/><line x1="7" y1="17" x2="7" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/><line x1="17" y1="17" x2="17" y2="21"/></svg>`,

  motor: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3"/><circle cx="12" cy="12" r="1" fill="currentColor"/><line x1="12" y1="4" x2="12" y2="1"/><line x1="12" y1="23" x2="12" y2="20"/></svg>`,

  propeller: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><circle cx="12" cy="12" r="2"/><path d="M12 10 C8 6 4 5 3 7 2 9 6 11 12 12" fill="currentColor" opacity="0.3" stroke="currentColor"/><path d="M14 12 C18 8 22 7 23 9 24 11 18 13 14 12" fill="currentColor" opacity="0.3" stroke="currentColor"/><path d="M12 14 C16 18 20 19 21 17 22 15 18 13 12 12" fill="currentColor" opacity="0.3" stroke="currentColor"/><path d="M10 12 C6 16 2 17 1 15 0 13 6 11 10 12" fill="currentColor" opacity="0.3" stroke="currentColor"/></svg>`,

  camera: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="2" y="6" width="15" height="12" rx="2"/><circle cx="9.5" cy="12" r="3.5"/><circle cx="9.5" cy="12" r="1.5" fill="currentColor" opacity="0.4"/><polygon points="17,9 22,6 22,18 17,15"/></svg>`,

  vtx: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="4" y="10" width="16" height="10" rx="2"/><line x1="12" y1="10" x2="12" y2="2"/><circle cx="12" cy="2" r="1.5" fill="currentColor"/><path d="M8 7 C8 4 12 2 12 2" /><path d="M16 7 C16 4 12 2 12 2"/><line x1="8" y1="14" x2="16" y2="14"/><line x1="8" y1="17" x2="13" y2="17"/></svg>`,

  receiver: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="5" y="8" width="14" height="12" rx="2"/><line x1="5" y1="2" x2="5" y2="8"/><line x1="19" y1="4" x2="19" y2="8"/><circle cx="9" cy="14" r="1.5" fill="currentColor" opacity="0.4"/><rect x="13" y="12" width="3" height="4" rx="0.5" fill="currentColor" opacity="0.2"/></svg>`,

  battery: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><rect x="2" y="7" width="18" height="12" rx="2"/><rect x="20" y="10" width="2.5" height="6" rx="1"/><line x1="6" y1="11" x2="6" y2="15"/><line x1="4" y1="13" x2="8" y2="13"/><line x1="12" y1="11" x2="12" y2="15"/></svg>`,

  antenna: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><line x1="12" y1="22" x2="12" y2="8"/><circle cx="12" cy="6" r="2"/><path d="M7 4 C7 1 12 0 12 0"/><path d="M17 4 C17 1 12 0 12 0"/><path d="M5 8 C4 4 12 0 12 0"/><path d="M19 8 C20 4 12 0 12 0"/><rect x="9" y="18" width="6" height="4" rx="1"/></svg>`,
};

const ZONE_DEFINITIONS: ReadonlyArray<{ id: string; partId: string }> = [
  { id: 'zone-frame', partId: 'frame' },
  { id: 'zone-fc', partId: 'fc' },
  { id: 'zone-esc', partId: 'esc' },
  { id: 'zone-motor-tl', partId: 'motor' },
  { id: 'zone-motor-tr', partId: 'motor' },
  { id: 'zone-motor-bl', partId: 'motor' },
  { id: 'zone-motor-br', partId: 'motor' },
  { id: 'zone-propeller-tl', partId: 'propeller' },
  { id: 'zone-propeller-tr', partId: 'propeller' },
  { id: 'zone-propeller-bl', partId: 'propeller' },
  { id: 'zone-propeller-br', partId: 'propeller' },
  { id: 'zone-camera', partId: 'camera' },
  { id: 'zone-vtx', partId: 'vtx' },
  { id: 'zone-receiver', partId: 'receiver' },
  { id: 'zone-battery', partId: 'battery' },
  { id: 'zone-antenna', partId: 'antenna' },
];

@Component({
  selector: 'app-drone-assembly',
  standalone: true,
  imports: [CdkDrag, CdkDropList],
  templateUrl: './drone-assembly.component.html',
  styleUrl: './drone-assembly.component.scss',
})
export class DroneAssemblyComponent {
  private sanitizer = inject(DomSanitizer);
  private iconCache = new Map<string, SafeHtml>();
  partsPanel: DronePart[] = [];
  dropZones: DropZone[] = [];
  allDropListIds: string[] = [];
  wrongFlashId: string | null = null;

  constructor() {
    this.resetExercise();
  }

  getPartIcon(partId: string): SafeHtml {
    let cached = this.iconCache.get(partId);
    if (!cached) {
      cached = this.sanitizer.bypassSecurityTrustHtml(PART_ICONS[partId] ?? '');
      this.iconCache.set(partId, cached);
    }
    return cached;
  }

  zoneSuffix(zone: DropZone): string {
    return zone.id.replace(/^zone-/, '');
  }

  zoneClasses(zone: DropZone): string {
    const filled = zone.parts.length > 0;
    return [
      'assembly__drop-zone',
      `assembly__drop-zone--${this.zoneSuffix(zone)}`,
      filled ? 'assembly__drop-zone--filled' : '',
      filled ? 'assembly__drop-zone--correct' : '',
      this.wrongFlashId === zone.id ? 'assembly__drop-zone--wrong-flash' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  get allPlaced(): boolean {
    return this.partsPanel.length === 0;
  }

  onDrop(event: CdkDragDrop<DronePart[]>): void {
    // Same container drop — ignore (no reordering inside the parts panel)
    if (event.previousContainer === event.container) {
      return;
    }

    const targetZone = this.dropZones.find((z) => z.id === event.container.id);
    if (!targetZone) {
      // Drops onto the parts panel from a zone shouldn't happen (filled zones are
      // disabled), but guard just in case.
      return;
    }

    if (targetZone.parts.length > 0) {
      this.flashWrong(targetZone.id);
      return;
    }

    const part = event.item.data as DronePart;
    if (part.correctZones.includes(targetZone.id)) {
      // Place a copy so the placed instance is independent from the panel source
      targetZone.parts.push({ ...part });
      part.remaining -= 1;
      if (part.remaining <= 0) {
        const idx = this.partsPanel.indexOf(part);
        if (idx >= 0) {
          this.partsPanel.splice(idx, 1);
        }
      }
    } else {
      this.flashWrong(targetZone.id);
    }
  }

  private flashWrong(zoneId: string): void {
    this.wrongFlashId = zoneId;
    setTimeout(() => {
      if (this.wrongFlashId === zoneId) {
        this.wrongFlashId = null;
      }
    }, 700);
  }

  resetExercise(): void {
    this.wrongFlashId = null;
    this.partsPanel = PART_DEFINITIONS.map((p) => ({ ...p, remaining: p.quantity })).sort(
      () => Math.random() - 0.5,
    );
    this.dropZones = ZONE_DEFINITIONS.map((z) => ({ ...z, parts: [] }));
    this.allDropListIds = ['parts-panel', ...this.dropZones.map((z) => z.id)];
  }
}
