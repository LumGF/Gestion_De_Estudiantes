import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlumnoService } from '../../services/alumno.service';
import { Alumno } from '../../models/alumno.model';
import { WhatsAppService, WhatsAppPruebaResponse } from '../../services/whatsapp.service';

@Component({
  selector: 'app-alumno-detail-page',
  standalone: true,
  imports: [],
  templateUrl: './alumno-detail-page.html',
  styleUrl: './alumno-detail-page.css',
})
export class AlumnoDetailPage implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private alumnoService = inject(AlumnoService);
  private whatsAppService = inject(WhatsAppService);

  alumno = signal<Alumno | undefined>(undefined);
  whatsAppListo = signal(false);
  whatsAppMensaje = signal('Consultando estado de WhatsApp...');
  enviandoPrueba = signal(false);
  resultadoPrueba = signal<WhatsAppPruebaResponse | null>(null);
  errorPrueba = signal<string | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.alumnoService.getAlumnoById(id).subscribe(data => {
      this.alumno.set(data);
    });
    this.cargarEstadoWhatsApp();
  }

  cargarEstadoWhatsApp() {
    this.whatsAppService.consultarEstado().subscribe({
      next: (estado) => {
        this.whatsAppListo.set(estado.listo);
        this.whatsAppMensaje.set(estado.mensaje);
      },
      error: () => {
        this.whatsAppListo.set(false);
        this.whatsAppMensaje.set(
          'No se pudo consultar WhatsApp. Verifique que el backend y whatsapp-bridge estén activos.'
        );
      },
    });
  }

  goBack() {
    this.router.navigate(['/alumnos']);
  }

  formatTelefono(telefono: string | undefined): string {
    if (!telefono) return '—';
    const digits = telefono.replace(/\D/g, '');
    const local = digits.startsWith('51') && digits.length === 11 ? digits.slice(2) : digits;
    return `+51 ${local}`;
  }

  enviarPruebaWhatsApp() {
    const alumno = this.alumno();
    if (!alumno?.id) return;

    this.enviandoPrueba.set(true);
    this.errorPrueba.set(null);
    this.resultadoPrueba.set(null);

    this.whatsAppService.enviarPrueba(alumno.id).subscribe({
      next: (res) => {
        this.resultadoPrueba.set(res);
        this.enviandoPrueba.set(false);
        this.cargarEstadoWhatsApp();
      },
      error: () => {
        this.errorPrueba.set('No se pudo enviar la prueba. Revise el bridge y reinicie el backend.');
        this.enviandoPrueba.set(false);
      },
    });
  }
}