import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { CatalogService } from '../catalog.service';
import { Category } from '../models/category.model';

@Component({
  selector: 'app-category-list',
  imports: [ReactiveFormsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './category-list.html',
  styleUrl: './category-list.scss',
})
export class CategoryList {
  private readonly catalogService = inject(CatalogService);

  protected readonly displayedColumns = ['name', 'description'];
  protected readonly categories = signal<Category[]>([]);
  protected readonly saving = signal(false);

  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
  });

  constructor() {
    this.load();
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const value = this.form.getRawValue();
    this.catalogService.createCategory({ name: value.name, description: value.description || null }).subscribe({
      next: () => {
        this.saving.set(false);
        this.form.reset();
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  private load(): void {
    this.catalogService.getCategories().subscribe((categories) => this.categories.set(categories));
  }
}
