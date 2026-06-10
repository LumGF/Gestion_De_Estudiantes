import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';
import { MainLayout } from './core/layout/main-layout/main-layout';
import { LoginPage } from './core/pages/login-page/login-page';
import { AlumnoListPage } from './gestion-acceso/pages/alumno-list-page/alumno-list-page';
import { AlumnoFormPage } from './gestion-acceso/pages/alumno-form-page/alumno-form-page';
import { AlumnoDetailPage } from './gestion-acceso/pages/alumno-detail-page/alumno-detail-page';
import { DashboardPage } from './gestion-acceso/pages/dashboard-page/dashboard-page';
import { PorteriaPage } from './gestion-acceso/pages/porteria-page/porteria-page';
import { AccesoHistorialPage } from './gestion-acceso/pages/acceso-historial-page/acceso-historial-page';
import { NotificacionesPage } from './gestion-acceso/pages/notificaciones-page/notificaciones-page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardPage },
      { path: 'porteria', component: PorteriaPage },
      { path: 'alumnos', component: AlumnoListPage },
      { path: 'alumnos/nuevo', component: AlumnoFormPage, canActivate: [adminGuard] },
      { path: 'alumnos/editar/:id', component: AlumnoFormPage, canActivate: [adminGuard] },
      { path: 'alumnos/:id', component: AlumnoDetailPage },
      { path: 'accesos', component: AccesoHistorialPage },
      { path: 'notificaciones', component: NotificacionesPage },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
