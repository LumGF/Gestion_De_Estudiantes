// Modelos del módulo gestion-acceso (antes en src/app/models)
export interface Alumno {
  id?: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  dni: string;
  fechaRegistro: string;
  /** Formato almacenado: 51 + 9 dígitos (ej. 51987654321). */
  telefonoPadre1: string;
  telefonoPadre2: string;
}