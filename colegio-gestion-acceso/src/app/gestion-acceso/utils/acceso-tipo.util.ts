import { TipoAcceso } from '../services/acceso.service';

const ETIQUETAS: Record<TipoAcceso, string> = {
  ENTRADA: 'Entrada',
  SALIDA: 'Salida',
  TARDANZA: 'Tardanza',
  NO_ASISTIO: 'No asistió',
  NO_ASISTIO_CON_PERMISO: 'No asistió (con permiso)',
};

export function etiquetaTipoAcceso(tipo: TipoAcceso): string {
  return ETIQUETAS[tipo] ?? tipo;
}

export function claseBadgeTipo(tipo: TipoAcceso): string {
  switch (tipo) {
    case 'ENTRADA': return 'entrada';
    case 'SALIDA': return 'salida';
    case 'TARDANZA': return 'tardanza';
    case 'NO_ASISTIO': return 'falta';
    case 'NO_ASISTIO_CON_PERMISO': return 'permiso';
    default: return '';
  }
}

/** Texto que recibirán los padres por WhatsApp (vista previa en portería). */
export function previewMensajeWhatsapp(
  nombre: string,
  codigo: string,
  tipo: TipoAcceso,
  observacion?: string
): string {
  const hora = new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
  const obs = observacion?.trim();

  let accion: string;
  switch (tipo) {
    case 'ENTRADA':
      accion = 'ingresó al colegio';
      break;
    case 'SALIDA':
      accion = 'salió del colegio';
      break;
    case 'TARDANZA':
      accion = 'llegó tarde al colegio';
      break;
    case 'NO_ASISTIO':
      accion = 'NO ASISTIÓ al colegio el día de hoy (sin permiso registrado)';
      break;
    case 'NO_ASISTIO_CON_PERMISO':
      accion = 'NO ASISTIÓ al colegio el día de hoy (con permiso autorizado)';
      break;
    default:
      accion = tipo;
  }

  let texto =
    `Colegio - Gestión de Acceso\n\n` +
    `El alumno ${nombre} (${codigo}) ${accion}.\n` +
    `Registrado a las ${hora}.`;

  if (obs) {
    if (tipo === 'NO_ASISTIO_CON_PERMISO') {
      texto += `\nMotivo del permiso: ${obs}.`;
    } else if (tipo === 'TARDANZA') {
      texto += `\nDetalle: ${obs}.`;
    }
  }

  texto += `\n\nEste es un mensaje automático del sistema de control de acceso.`;
  return texto;
}
