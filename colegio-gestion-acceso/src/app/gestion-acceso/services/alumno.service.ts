import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/models/common.model';
import { Alumno } from '../models/alumno.model';
import { finalize, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AlumnoService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/alumnos`;

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private alumnosSignal = signal<Alumno[]>([]);
  readonly alumnos = this.alumnosSignal.asReadonly();

  loadAlumnos(page = 0, size = 10, q = '') {
    this.loading.set(true);
    this.error.set(null);
    let params = new HttpParams().set('page', page).set('size', size);
    if (q) params = params.set('q', q);
    return this.http.get<PageResponse<Alumno>>(this.apiUrl, { params }).pipe(
      tap({
        next: (data) => this.alumnosSignal.set(data.content),
        error: () => this.error.set('Error al cargar alumnos'),
      }),
      finalize(() => this.loading.set(false))
    );
  }

  getAlumnoById(id: number) {
    return this.http.get<Alumno>(`${this.apiUrl}/${id}`);
  }

  buscarPorCodigo(codigo: string) {
    return this.http.get<Alumno>(`${this.apiUrl}/codigo/${encodeURIComponent(codigo)}`);
  }

  crear(alumno: Alumno) {
    return this.http.post<Alumno>(this.apiUrl, alumno);
  }

  actualizar(id: number, alumno: Alumno) {
    return this.http.put<Alumno>(`${this.apiUrl}/${id}`, alumno);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
