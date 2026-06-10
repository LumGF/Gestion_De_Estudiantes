import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Acceso, AccesoService } from '../../services/acceso.service';
import { etiquetaTipoAcceso, claseBadgeTipo } from '../../utils/acceso-tipo.util';

@Component({
  selector: 'app-acceso-historial-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './acceso-historial-page.html',
  styleUrl: './acceso-historial-page.css',
})
export class AccesoHistorialPage implements OnInit {
  private accesoService = inject(AccesoService);

  accesos = signal<Acceso[]>([]);
  page = 0;
  totalPages = 0;
  loading = signal(true);

  etiquetaTipo = etiquetaTipoAcceso;
  claseBadge = claseBadgeTipo;

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.accesoService.historial(this.page).subscribe({
      next: (data) => {
        this.accesos.set(data.content);
        this.totalPages = data.totalPages;
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  anterior() {
    if (this.page > 0) {
      this.page--;
      this.cargar();
    }
  }

  siguiente() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.cargar();
    }
  }
}
