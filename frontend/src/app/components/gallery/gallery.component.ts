import { Component } from '@angular/core';

interface DroneItem {
  image: string;
  description: string;
}

@Component({
  selector: 'app-gallery',
  standalone: true,
  templateUrl: './gallery.component.html',
  styleUrl: './gallery.component.scss',
})
export class GalleryComponent {
  selectedDrone: DroneItem | null = null;

  drones: DroneItem[] = [
    {
      image:
        'https://upload.wikimedia.org/wikipedia/commons/1/16/UA_Vampire_UCAV_01.jpg',
      description: 'Ukrainas toodetud Vampire droon ("Baba Yaga")',
    },
    {
      image:
      'https://upload.wikimedia.org/wikipedia/commons/8/80/UA_fiber-optic_FPV_drone_02.webp?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original',
      description: 'Fiiberoptilise kaabliga varustatud FPV droon',
    },
    {
      image:
        'https://upload.wikimedia.org/wikipedia/commons/0/0b/UA_military_FPV_drones_10_%28cropped%29.jpg',
      description:
        'Nn kamikadze FPV droon, mis hävineb ise sihtmärgiga kohtudes',
    },
  ];

  openLightbox(drone: DroneItem): void {
    this.selectedDrone = drone;
  }

  closeLightbox(): void {
    this.selectedDrone = null;
  }
}
