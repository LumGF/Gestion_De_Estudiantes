import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Acceso, AccesoService, TipoAcceso } from '../../services/acceso.service';
import { AlumnoService } from '../../services/alumno.service';
import { Alumno } from '../../models/alumno.model';
import { etiquetaTipoAcceso, previewMensajeWhatsapp } from '../../utils/acceso-tipo.util';

@Component({
  selector: 'app-porteria-page',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './porteria-page.html',
  styleUrl: './porteria-page.css',
})
export class PorteriaPage {
  private alumnoService = inject(AlumnoService);
  private accesoService = inject(AccesoService);

  codigoBusqueda = '';
  observacionPermiso = '';
  observacionTardanza = '';
  mostrarFormPermiso = signal(false);
  mostrarConfirmacionFalta = signal(false);
  mostrarFormTardanza = signal(false);
  alumno = signal<Alumno | null>(null);
  ultimoAcceso = signal<Acceso | null>(null);
  buscando = signal(false);
  registrando = signal(false);
  error = signal('');
  mensaje = signal('');

  etiquetaTipo = etiquetaTipoAcceso;
  previewMensaje = previewMensajeWhatsapp;

  buscar() {
    if (!this.codigoBusqueda.trim()) return;
    this.buscando.set(true);
    this.error.set('');
    this.alumno.set(null);
    this.cerrarFormularios();
    this.alumnoService.buscarPorCodigo(this.codigoBusqueda.trim()).subscribe({
      next: (a) => {
        this.alumno.set(a);
        this.buscando.set(false);
      },
      error: () => {
        this.error.set('Alumno no encontrado con ese código');
        this.buscando.set(false);
      },
    });
  }

  registrar(tipo: TipoAcceso) {
    if (tipo === 'NO_ASISTIO') {
      if (!this.mostrarConfirmacionFalta()) {
        this.cerrarFormularios();
        this.mostrarConfirmacionFalta.set(true);
        return;
      }
    }

    if (tipo === 'NO_ASISTIO_CON_PERMISO') {
      if (!this.mostrarFormPermiso()) {
        this.cerrarFormularios();
        this.mostrarFormPermiso.set(true);
        return;
      }
      if (!this.observacionPermiso.trim()) {
        this.error.set('Indique el motivo del permiso');
        return;
      }
    }

    if (tipo === 'TARDANZA') {
      if (!this.mostrarFormTardanza()) {
        this.cerrarFormularios();
        this.mostrarFormTardanza.set(true);
        return;
      }
    }

    const a = this.alumno();
    if (!a?.id) return;

    this.registrando.set(true);
    this.mensaje.set('');
    this.error.set('');

    const observacion =
      tipo === 'NO_ASISTIO_CON_PERMISO'
        ? this.observacionPermiso.trim()
        : tipo === 'TARDANZA' && this.observacionTardanza.trim()
          ? this.observacionTardanza.trim()
          : undefined;

    this.accesoService.registrar({ alumnoId: a.id, tipo, observacion }).subscribe({
      next: (acceso) => {
        this.ultimoAcceso.set(acceso);
        const enviados = acceso.notificaciones?.filter(n => n.estado === 'ENVIADO').length ?? 0;
        const total = acceso.notificaciones?.length ?? 0;
        this.mensaje.set(
          `${etiquetaTipoAcceso(tipo)} registrado. WhatsApp: ${enviados}/${total} enviado(s).`
        );
        this.registrando.set(false);
        this.codigoBusqueda = '';
        this.observacionPermiso = '';
        this.observacionTardanza = '';
        this.cerrarFormularios();
        this.alumno.set(null);
      },
      error: (err) => {
        const msg = err?.error?.message ?? err?.error?.fieldErrors?.observacion;
        this.error.set(msg || 'Error al registrar');
        this.registrando.set(false);
      },
    });
  }

  cancelarPermiso() {
    this.mostrarFormPermiso.set(false);
    this.observacionPermiso = '';
    this.error.set('');
  }

  cancelarFalta() {
    this.mostrarConfirmacionFalta.set(false);
    this.error.set('');
  }

  cancelarTardanza() {
    this.mostrarFormTardanza.set(false);
    this.observacionTardanza = '';
    this.error.set('');
  }

  private cerrarFormularios() {
    this.mostrarFormPermiso.set(false);
    this.mostrarConfirmacionFalta.set(false);
    this.mostrarFormTardanza.set(false);
    this.observacionPermiso = '';
    this.observacionTardanza = '';
    this.error.set('');
  }
}
