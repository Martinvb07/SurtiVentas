import { CurrencyPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, startWith } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/models/role.enum';
import { CatalogService } from '../catalog.service';
import { Category } from '../models/category.model';
import { Product } from '../models/product.model';
import { ProductForm } from '../product-form/product-form';
import { StockAdjustDialog } from '../stock-adjust-dialog/stock-adjust-dialog';

@Component({
  selector: 'app-product-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
  ],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
})
export class ProductList {
  private readonly catalogService = inject(CatalogService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['sku', 'name', 'category', 'price', 'stock', 'unit', 'actions'];

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  private readonly search = toSignal(
    this.searchControl.valueChanges.pipe(debounceTime(350), startWith('')),
    { initialValue: '' },
  );

  protected readonly categoryId = signal<number | null>(null);
  protected readonly lowStockOnly = signal(false);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(20);

  protected readonly categories = signal<Category[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);

  protected readonly canManageCatalog = computed(() => this.authService.currentUser()?.role === Role.ADMINISTRADOR);
  protected readonly canAdjustStock = computed(() => {
    const role = this.authService.currentUser()?.role;
    return role === Role.ADMINISTRADOR || role === Role.BODEGUERO;
  });

  constructor() {
    this.catalogService.getCategories().subscribe((categories) => this.categories.set(categories));

    effect(() => {
      const params = {
        page: this.pageIndex(),
        size: this.pageSize(),
        categoryId: this.categoryId(),
        active: true,
        lowStock: this.lowStockOnly() ? true : null,
        search: this.search() || null,
      };
      this.loading.set(true);
      this.catalogService.searchProducts(params).subscribe({
        next: (page) => {
          this.products.set(page.content);
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

  protected onCategoryChange(value: number | null): void {
    this.categoryId.set(value);
    this.pageIndex.set(0);
  }

  protected onLowStockChange(checked: boolean): void {
    this.lowStockOnly.set(checked);
    this.pageIndex.set(0);
  }

  protected openCreateDialog(): void {
    const ref = this.dialog.open(ProductForm, {
      width: '520px',
      data: { categories: this.categories() },
    });
    ref.afterClosed().subscribe((created) => {
      if (created) {
        this.refresh();
        this.snackBar.open('Producto creado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openEditDialog(product: Product): void {
    const ref = this.dialog.open(ProductForm, {
      width: '520px',
      data: { product, categories: this.categories() },
    });
    ref.afterClosed().subscribe((updated) => {
      if (updated) {
        this.refresh();
        this.snackBar.open('Producto actualizado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected openStockDialog(product: Product): void {
    const ref = this.dialog.open(StockAdjustDialog, { width: '420px', data: { product } });
    ref.afterClosed().subscribe((adjusted) => {
      if (adjusted) {
        this.refresh();
        this.snackBar.open('Stock ajustado', 'Cerrar', { duration: 3000 });
      }
    });
  }

  protected deactivate(product: Product): void {
    if (!confirm(`¿Desactivar "${product.name}"? Ya no aparecerá disponible para pedidos.`)) {
      return;
    }
    this.catalogService.deactivateProduct(product.id).subscribe(() => {
      this.refresh();
      this.snackBar.open('Producto desactivado', 'Cerrar', { duration: 3000 });
    });
  }

  private refresh(): void {
    this.pageIndex.update((v) => v);
    this.catalogService
      .searchProducts({
        page: this.pageIndex(),
        size: this.pageSize(),
        categoryId: this.categoryId(),
        active: true,
        lowStock: this.lowStockOnly() ? true : null,
        search: this.search() || null,
      })
      .subscribe((page) => {
        this.products.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }
}
