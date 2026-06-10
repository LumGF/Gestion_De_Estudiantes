import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Notificacion, NotificacionService } from '../../services/acceso.service';

@Component({
  selector: 'app-notificaciones-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './notificaciones-page.html',
  styleUrl: './notificaciones-page.css',
})
export class NotificacionesPage implements OnInit {
  private notificacionService = inject(NotificacionService);

  notificaciones = signal<Notificacion[]>([]);
  page = 0;
  totalPages = 0;
  loading = signal(true);
  reintentando = signal(false);

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.notificacionService.listarLog(this.page).subscribe({
      next: (data) => {
        this.notificaciones.set(data.content);
        this.totalPages = data.totalPages;
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  reintentar() {
    this.reintentando.set(true);
    this.notificacionService.reintentarPendientes().subscribe({
      next: () => {
        this.reintentando.set(false);
        this.cargar();
      },
      error: () => this.reintentando.set(false),
    });
  }

  formatTelefono(t: string): string {
    const digits = t.replace(/\D/g, '');
    const local = digits.startsWith('51') && digits.length === 11 ? digits.slice(2) : digits;
    return `+51 ${local}`;
  }
}
