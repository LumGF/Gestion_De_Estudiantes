import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PageResponse } from '../../core/models/common.model';

export type TipoAcceso = 'ENTRADA' | 'SALIDA' | 'TARDANZA' | 'NO_ASISTIO' | 'NO_ASISTIO_CON_PERMISO';

export interface Acceso {
  id: number;
  alumnoId: number;
  alumnoNombre: string;
  alumnoCodigo: string;
  tipo: TipoAcceso;
  fechaHora: string;
  registradoPor: string;
  observacion?: string;
  notificaciones?: Notificacion[];
}

export interface Notificacion {
  id: number;
  accesoId: number;
  telefono: string;
  mensaje: string;
  estado: 'PENDIENTE' | 'ENVIADO' | 'FALLIDO';
  intentos: number;
  error?: string;
  fechaCreacion: string;
  fechaEnvio?: string;
  alumnoNombre?: string;
}

export interface AccesoRequest {
  alumnoId: number;
  tipo: TipoAcceso;
  observacion?: string;
}

@Injectable({ providedIn: 'root' })
export class AccesoService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/accesos`;

  registrar(request: AccesoRequest) {
    return this.http.post<Acceso>(this.apiUrl, request);
  }

  historial(page = 0, size = 20, alumnoId?: number) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (alumnoId) params = params.set('alumnoId', alumnoId);
    return this.http.get<PageResponse<Acceso>>(this.apiUrl, { params });
  }

  listarHoy() {
    return this.http.get<Acceso[]>(`${this.apiUrl}/hoy`);
  }

  buscarPorId(id: number) {
    return this.http.get<Acceso>(`${this.apiUrl}/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/notificaciones`;

  listarLog(page = 0, size = 20) {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Notificacion>>(this.apiUrl, { params });
  }

  reintentarPendientes() {
    return this.http.post<void>(`${this.apiUrl}/reintentar`, {});
  }
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  obtenerResumen() {
    return this.http.get<DashboardResumen>(this.apiUrl);
  }
}

export interface DashboardResumen {
  totalAlumnos: number;
  accesosHoy: number;
  entradasHoy: number;
  salidasHoy: number;
  noAsistioHoy: number;
  noAsistioConPermisoHoy: number;
  tardanzasHoy: number;
  notificacionesEnviadas: number;
  notificacionesFallidas: number;
  whatsAppListo: boolean;
  whatsAppMensaje: string;
}
