import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Employee } from '../models/payroll.model';
import { PayrollService } from '../payroll.service';

export interface EmployeeFormData {
  employee?: Employee;
}

@Component({
  selector: 'app-employee-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatButtonModule,
  ],
  templateUrl: './employee-form.html',
  styleUrl: './employee-form.scss',
})
export class EmployeeForm {
  private readonly fb = inject(FormBuilder);
  private readonly payroll = inject(PayrollService);
  private readonly dialogRef = inject(MatDialogRef<EmployeeForm>);
  private readonly data = inject<EmployeeFormData | null>(MAT_DIALOG_DATA);

  protected readonly isEdit = !!this.data?.employee;
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    fullName: [this.data?.employee?.fullName ?? '', [Validators.required, Validators.maxLength(150)]],
    position: [this.data?.employee?.position ?? '', [Validators.required, Validators.maxLength(100)]],
    salary: [this.data?.employee?.salary ?? 0, [Validators.required, Validators.min(0)]],
    active: [this.data?.employee?.active ?? true],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();

    const request$ =
      this.isEdit && this.data?.employee
        ? this.payroll.update(this.data.employee.id, value)
        : this.payroll.create({ fullName: value.fullName, position: value.position, salary: value.salary });

    request$.subscribe({
      next: (employee) => {
        this.saving.set(false);
        this.dialogRef.close(employee);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el empleado.');
      },
    });
  }
}
