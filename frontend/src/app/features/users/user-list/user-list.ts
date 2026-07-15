import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { UserAccount } from '../models/user-account.model';
import { ResetPasswordDialog } from '../reset-password-dialog/reset-password-dialog';
import { roleLabel } from '../role-ui';
import { UserForm } from '../user-form/user-form';
import { UsersService } from '../users.service';

@Component({
  selector: 'app-user-list',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss',
})
export class UserList {
  private readonly usersService = inject(UsersService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly roleLabel = roleLabel;

  protected readonly displayedColumns = ['fullName', 'email', 'role', 'status', 'actions'];
  protected readonly users = signal<UserAccount[]>([]);
  protected readonly loading = signal(false);

  constructor() {
    this.refresh();
  }

  protected openCreate(): void {
    this.dialog
      .open(UserForm, { width: '460px' })
      .afterClosed()
      .subscribe((created) => {
        if (created) {
          this.refresh();
          this.snackBar.open('Usuario creado', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected edit(user: UserAccount): void {
    this.dialog
      .open(UserForm, { width: '460px', data: { user } })
      .afterClosed()
      .subscribe((updated) => {
        if (updated) {
          this.refresh();
          this.snackBar.open('Usuario actualizado', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected resetPassword(user: UserAccount): void {
    this.dialog
      .open(ResetPasswordDialog, { width: '420px', data: { user } })
      .afterClosed()
      .subscribe((done) => {
        if (done) {
          this.snackBar.open('Contraseña actualizada', 'Cerrar', { duration: 3000 });
        }
      });
  }

  protected toggleActive(user: UserAccount): void {
    this.usersService
      .update(user.id, { fullName: user.fullName, role: user.role, active: !user.active })
      .subscribe(() => {
        this.refresh();
        this.snackBar.open(user.active ? 'Usuario desactivado' : 'Usuario activado', 'Cerrar', {
          duration: 3000,
        });
      });
  }

  private refresh(): void {
    this.loading.set(true);
    this.usersService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
