import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Role } from '../../../core/auth/models/role.enum';
import { UserAccount } from '../models/user-account.model';
import { ROLES, roleLabel } from '../role-ui';
import { UsersService } from '../users.service';

export interface UserFormData {
  user?: UserAccount;
}

@Component({
  selector: 'app-user-form',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
  ],
  templateUrl: './user-form.html',
  styleUrl: './user-form.scss',
})
export class UserForm {
  private readonly fb = inject(FormBuilder);
  private readonly usersService = inject(UsersService);
  private readonly dialogRef = inject(MatDialogRef<UserForm>);
  private readonly data = inject<UserFormData | null>(MAT_DIALOG_DATA);

  protected readonly roles = ROLES;
  protected readonly roleLabel = roleLabel;
  protected readonly isEdit = !!this.data?.user;
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: [{ value: this.data?.user?.email ?? '', disabled: this.isEdit }, [Validators.required, Validators.email]],
    fullName: [this.data?.user?.fullName ?? '', [Validators.required, Validators.maxLength(150)]],
    role: [this.data?.user?.role ?? Role.VENDEDOR, [Validators.required]],
    password: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(6)]],
    active: [this.data?.user?.active ?? true],
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
      this.isEdit && this.data?.user
        ? this.usersService.update(this.data.user.id, {
            fullName: value.fullName,
            role: value.role,
            active: value.active,
          })
        : this.usersService.create({
            email: value.email,
            fullName: value.fullName,
            role: value.role,
            password: value.password,
          });

    request$.subscribe({
      next: (user) => {
        this.saving.set(false);
        this.dialogRef.close(user);
      },
      error: (error) => {
        this.saving.set(false);
        this.errorMessage.set(error.error?.message ?? 'No se pudo guardar el usuario.');
      },
    });
  }
}
