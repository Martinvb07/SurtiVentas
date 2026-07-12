import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, startWith } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { SuppliersService } from '../suppliers.service';
import { Supplier } from '../models/supplier.model';
import { SupplierForm } from '../supplier-form/supplier-form';

@Component({
  selector: 'app-supplier-list',
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './supplier-list.html',
  styleUrl: './supplier-list.scss',
})
export class SupplierList {
  private readonly suppliersService = inject(SuppliersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  protected readonly displayedColumns = ['name', 'contactName', 'phone', 'email', 'actions'];

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly search = toSignal(
    this.searchControl.valueChanges.pipe(debounceTime(350), startWith('')),
    { initialValue: '' },
  );

  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly suppliers = signal<Supplier[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  protected readonly canManageSuppliers = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);

  constructor() {
    effect(() => {
      const params = { page: this.pageIndex(), size: this.pageSize(), active: true, search: this.search() || null };
      this.loading.set(true);
      this.suppliersService.search(params).subscribe({
        next: (page) => {
          this.suppliers.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(SupplierForm, { width: '520px' });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.refresh();
        this.snackBar.open('Proveedor creado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openEditDialog(supplier: Supplier): void {
    const ref = this.dialog.open(SupplierForm, { width: '520px', data: { supplier } });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Proveedor actualizado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected viewCatalog(supplier: Supplier): void {
    this.router.navigate(['/app/suppliers', supplier.id, 'catalog']);
  }

  protected deactivate(supplier: Supplier): void {
    if (!confirm(`¿Desactivar "${supplier.name}"?`)) {
      return;
    }
    this.suppliersService.deactivate(supplier.id).subscribe(() => {
      this.refresh();
      this.snackBar.open('Proveedor desactivado', 'Cerrar', { duration: 3000 });
    });
  }

  private refresh(): void {
    this.suppliersService
      .search({ page: this.pageIndex(), size: this.pageSize(), active: true, search: this.search() || null })
      .subscribe((page) => {
        this.suppliers.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}
