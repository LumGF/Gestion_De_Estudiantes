import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthUser } from '../models/common.model';
import { AuthService } from './auth.service';

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private readonly url = `${environment.apiUrl}/auth`;

  login(credentials: LoginRequest) {
    return this.http.post<AuthUser>(`${this.url}/login`, credentials);
  }

  saveSession(user: AuthUser) {
    this.auth.setSession(user);
  }
}
