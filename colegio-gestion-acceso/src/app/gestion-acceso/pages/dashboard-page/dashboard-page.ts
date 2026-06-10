import { Component, inject, OnInit, signal } from '@angular/core';
import { Acceso, AccesoService, DashboardResumen, DashboardService } from '../../services/acceso.service';
import { etiquetaTipoAcceso, claseBadgeTipo } from '../../utils/acceso-tipo.util';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage implements OnInit {
  private dashboardService = inject(DashboardService);
  private accesoService = inject(AccesoService);

  resumen = signal<DashboardResumen | null>(null);
  accesosHoy = signal<Acceso[]>([]);
  loading = signal(true);

  etiquetaTipo = etiquetaTipoAcceso;
  claseBadge = claseBadgeTipo;

  ngOnInit() {
    this.dashboardService.obtenerResumen().subscribe({
      next: (data) => {
        this.resumen.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.accesoService.listarHoy().subscribe({
      next: (data) => this.accesosHoy.set(data),
    });
  }

  formatHora(fecha: string): string {
    return new Date(fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
  }
}
