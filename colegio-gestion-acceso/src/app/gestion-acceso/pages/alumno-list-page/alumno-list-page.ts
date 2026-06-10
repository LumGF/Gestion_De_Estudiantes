import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AlumnoService } from '../../services/alumno.service';

@Component({
  selector: 'app-alumno-list-page',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './alumno-list-page.html',
  styleUrl: './alumno-list-page.css',
})
export class AlumnoListPage implements OnInit {
  private alumnoService = inject(AlumnoService);
  private router = inject(Router);
  auth = inject(AuthService);

  alumnos = this.alumnoService.alumnos;
  loading = this.alumnoService.loading;
  error = this.alumnoService.error;

  busqueda = '';
  page = 0;
  totalPages = 0;

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.alumnoService.loadAlumnos(this.page, 10, this.busqueda).subscribe({
      next: (data) => (this.totalPages = data.totalPages),
    });
  }

  buscar() {
    this.page = 0;
    this.cargar();
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

  goToNuevo() {
    this.router.navigate(['/alumnos/nuevo']);
  }

  goToEditar(id: number) {
    this.router.navigate(['/alumnos/editar', id]);
  }

  goToDetalle(id: number) {
    this.router.navigate(['/alumnos', id]);
  }

  eliminar(id: number) {
    if (confirm('¿Estás seguro de eliminar este alumno?')) {
      this.alumnoService.eliminar(id).subscribe(() => this.cargar());
    }
  }

  formatTelefono(telefono: string | undefined): string {
    if (!telefono) return '—';
    const digits = telefono.replace(/\D/g, '');
    const local = digits.startsWith('51') && digits.length === 11 ? digits.slice(2) : digits;
    return `+51 ${local}`;
  }
}
