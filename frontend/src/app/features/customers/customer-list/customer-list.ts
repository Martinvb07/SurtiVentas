import { CurrencyPipe } from '@angular/common';
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
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, startWith } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { CustomersService } from '../customers.service';
import { CustomerClassification, Customer } from '../models/customer.model';
import { CustomerForm } from '../customer-form/customer-form';
import { DebtAdjustDialog } from '../debt-adjust-dialog/debt-adjust-dialog';

@Component({
  selector: 'app-customer-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './customer-list.html',
  styleUrl: './customer-list.scss',
})
export class CustomerList {
  private readonly customersService = inject(CustomersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['storeName', 'ownerName', 'phone', 'classification', 'creditLimit', 'currentDebt', 'actions'];

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly search = toSignal(
    this.searchControl.valueChanges.pipe(debounceTime(350), startWith('')),
    { initialValue: '' },
  );

  protected readonly classification = signal<CustomerClassification | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly customers = signal<Customer[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  protected readonly canManageCustomers = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);
  protected readonly canAdjustDebt = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.ADMINISTRADOR || role === Role.FACTURADOR;
  });

  constructor() {
    effect(() => {
      const params = {
        page: this.pageIndex(),
        size: this.pageSize(),
        classification: this.classification(),
        active: true,
        search: this.search() || null,
      };
      this.loading.set(true);
      this.customersService.search(params).subscribe({
        next: (page) => {
          this.customers.set(page.content);
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

  protected onClassificationChange(value: CustomerClassification | null): void {
    this.classification.set(value);
    this.pageIndex.set(0);
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(CustomerForm, { width: '520px' });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.refresh();
        this.snackBar.open('Cliente creado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openEditDialog(customer: Customer): void {
    const ref = this.dialog.open(CustomerForm, { width: '520px', data: { customer } });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Cliente actualizado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openDebtDialog(customer: Customer): void {
    const ref = this.dialog.open(DebtAdjustDialog, { width: '420px', data: { customer } });
    ref.afterClosed().subscribe((adjusted) => {
      if (adjusted) {
        this.refresh();
        this.snackBar.open('Cartera ajustada', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected deactivate(customer: Customer): void {
    if (!confirm(`¿Desactivar "${customer.storeName}"? Ya no aparecerá disponible para pedidos.`)) {
      return;
    }
    this.customersService.deactivate(customer.id).subscribe(() => {
      this.refresh();
      this.snackBar.open('Cliente desactivado', 'Cerrar', { duration: 3000 });
    });
  }

  private refresh(): void {
    this.customersService
      .search({
        page: this.pageIndex(),
        size: this.pageSize(),
        classification: this.classification(),
        active: true,
        search: this.search() || null,
      })
      .subscribe((page) => {
        this.customers.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}
