import { Component } from '@angular/core';

interface DroneItem {
  name: string;
  type: string;
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
      name: 'MQ-9 Reaper',
      type: 'MALE UCAV',
      description:
        'The General Atomics MQ-9 Reaper is a remotely piloted aircraft capable of carrying precision-guided munitions. With an endurance of over 27 hours, it serves as the backbone of US drone strike operations.',
    },
    {
      name: 'Bayraktar TB2',
      type: 'Tactical UCAV',
      description:
        'Developed by Turkey\'s Baykar, the TB2 gained fame in conflicts across Libya, Syria, and Ukraine. Affordable and highly effective, it changed the calculus of asymmetric warfare.',
    },
    {
      name: 'MQ-1 Predator',
      type: 'Pioneer UCAV',
      description:
        'The aircraft that started the drone revolution. Originally designed for reconnaissance, it was later armed with Hellfire missiles, setting the template for modern combat drones.',
    },
    {
      name: 'Switchblade 600',
      type: 'Loitering Munition',
      description:
        'A tube-launched kamikaze drone that loiters over the battlefield until a target is identified. Combines the precision of a guided missile with the patience of a surveillance platform.',
    },
    {
      name: 'Shahed-136',
      type: 'One-Way Attack Drone',
      description:
        'An Iranian-designed delta-wing drone used for long-range saturation attacks. Its low cost and simplicity allow deployment in large swarms to overwhelm air defenses.',
    },
    {
      name: 'XQ-58A Valkyrie',
      type: 'Loyal Wingman',
      description:
        'A stealthy unmanned combat air vehicle designed to operate alongside manned fighter jets. Represents the next generation of human-machine teaming in aerial combat.',
    },
  ];

  openLightbox(drone: DroneItem): void {
    this.selectedDrone = drone;
  }

  closeLightbox(): void {
    this.selectedDrone = null;
  }
}
