import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlumnoService } from '../../services/alumno.service';

@Component({
  selector: 'app-alumno-form-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './alumno-form-page.html',
  styleUrl: './alumno-form-page.css',
})
export class AlumnoFormPage implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private alumnoService = inject(AlumnoService);
  private fb = inject(FormBuilder);

  esEdicion = false;
  alumnoId: number | null = null;

  readonly prefijoPais = '+51';

  private readonly telefonoPeruValidators = [
    Validators.required,
    Validators.pattern(/^9\d{8}$/),
  ];

  form: FormGroup = this.fb.group({
    codigo: ['', Validators.required],
    nombre: ['', Validators.required],
    descripcion: ['', Validators.required],
    dni: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
    fechaRegistro: ['', Validators.required],
    telefonoPadre1: ['', this.telefonoPeruValidators],
    telefonoPadre2: ['', [Validators.pattern(/^$|^9\d{8}$/)]],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.esEdicion = true;
      this.alumnoId = Number(id);
      this.alumnoService.getAlumnoById(this.alumnoId).subscribe(data => {
        this.form.patchValue({
          ...data,
          telefonoPadre1: this.aNumeroLocal(data.telefonoPadre1),
          telefonoPadre2: this.aNumeroLocal(data.telefonoPadre2),
        });
      });
    }
  }

  guardar() {
    if (this.form.invalid) return;
    const payload = this.buildPayload();
    if (this.esEdicion && this.alumnoId) {
      this.alumnoService.actualizar(this.alumnoId, payload).subscribe(() => {
        this.router.navigate(['/alumnos']);
      });
    } else {
      this.alumnoService.crear(payload).subscribe(() => {
        this.router.navigate(['/alumnos']);
      });
    }
  }

  private buildPayload() {
    const raw = this.form.getRawValue();
    return {
      ...raw,
      telefonoPadre1: this.aNumeroCompleto(raw.telefonoPadre1),
      telefonoPadre2: raw.telefonoPadre2 ? this.aNumeroCompleto(raw.telefonoPadre2) : '',
    };
  }

  /** De 51987654321 a 987654321 para el input. */
  private aNumeroLocal(telefono: string | undefined): string {
    if (!telefono) return '';
    const digits = telefono.replace(/\D/g, '');
    return digits.startsWith('51') && digits.length === 11 ? digits.slice(2) : digits;
  }

  /** De 987654321 a 51987654321 para la API. */
  private aNumeroCompleto(telefono: string): string {
    const digits = telefono.replace(/\D/g, '');
    return digits.length === 9 ? `51${digits}` : digits;
  }

  cancelar() {
    this.router.navigate(['/alumnos']);
  }
}