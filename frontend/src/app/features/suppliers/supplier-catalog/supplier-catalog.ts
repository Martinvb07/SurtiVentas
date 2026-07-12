import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { SuppliersService } from '../suppliers.service';
import { Supplier, SupplierProduct } from '../models/supplier.model';
import { SupplierProductForm } from '../supplier-product-form/supplier-product-form';

@Component({
  selector: 'app-supplier-catalog',
  imports: [CurrencyPipe, MatTableModule, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './supplier-catalog.html',
  styleUrl: './supplier-catalog.scss',
})
export class SupplierCatalog {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly suppliersService = inject(SuppliersService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['productSku', 'productName', 'supplierSku', 'cost', 'actions'];

  protected readonly supplierId = Number(this.route.snapshot.paramMap.get('id'));
  protected readonly supplier = signal<Supplier | null>(null);
  protected readonly supplierProducts = signal<SupplierProduct[]>([]);

  protected readonly canManage = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);

  constructor() {
    this.suppliersService.getById(this.supplierId).subscribe((supplier) => this.supplier.set(supplier));
    this.refresh();
  }

  protected goBack(): void {
    this.router.navigate(['/app/suppliers']);
  }

  protected openAddDialog(): void {
    const ref = this.dialog.open(SupplierProductForm, { width: '480px', data: { supplierId: this.supplierId } });
    ref.afterClosed().subscribe((added) => {
      if (added) {
        this.refresh();
        this.snackBar.open('Producto agregado al catálogo', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openEditDialog(supplierProduct: SupplierProduct): void {
    const ref = this.dialog.open(SupplierProductForm, {
      width: '480px',
      data: { supplierId: this.supplierId, supplierProduct },
    });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Producto actualizado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected remove(supplierProduct: SupplierProduct): void {
    if (!confirm(`¿Quitar "${supplierProduct.productName}" del catálogo de este proveedor?`)) {
      return;
    }
    this.suppliersService.removeProduct(this.supplierId, supplierProduct.id).subscribe(() => {
      this.refresh();
      this.snackBar.open('Producto removido del catálogo', 'Cerrar', { duration: 3000 });
    });
  }

  private refresh(): void {
    this.suppliersService.getProducts(this.supplierId).subscribe((products) => this.supplierProducts.set(products));
  }
}
