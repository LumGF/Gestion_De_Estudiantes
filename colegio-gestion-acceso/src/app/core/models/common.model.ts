export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export type Rol = 'ADMIN' | 'PERSONAL';

export interface AuthUser {
  token: string;
  email: string;
  nombre: string;
  rol: Rol;
}
