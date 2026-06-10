import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface WhatsAppEstado {
  bridgeHabilitado: boolean;
  listo: boolean;
  mensaje: string;
}

export interface WhatsAppEnvioResultado {
  telefono: string;
  enviado: boolean;
  detalle: string;
}

export interface WhatsAppPruebaResponse {
  alumnoId: number;
  alumnoNombre: string;
  mensajeEnviado: string;
  exito: boolean;
  resumen: string;
  resultados: WhatsAppEnvioResultado[];
}

@Injectable({ providedIn: 'root' })
export class WhatsAppService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/whatsapp`;

  consultarEstado() {
    return this.http.get<WhatsAppEstado>(`${this.apiUrl}/estado`);
  }

  enviarPrueba(alumnoId: number) {
    return this.http.post<WhatsAppPruebaResponse>(`${this.apiUrl}/prueba/${alumnoId}`, {});
  }
}
