import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { CatalogService } from '../catalog.service';
import { UnitOfMeasure } from '../models/unit-of-measure.model';

@Component({
  selector: 'app-unit-list',
  imports: [ReactiveFormsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './unit-list.html',
  styleUrl: './unit-list.scss',
})
export class UnitList {
  private readonly catalogService = inject(CatalogService);

  protected readonly displayedColumns = ['name', 'abbreviation'];
  protected readonly units = signal<UnitOfMeasure[]>([]);
  protected readonly saving = signal(false);

  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(60)]],
    abbreviation: ['', [Validators.required, Validators.maxLength(10)]],
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
    this.catalogService.createUnit(value).subscribe({
      next: () => {
        this.saving.set(false);
        this.form.reset();
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  private load(): void {
    this.catalogService.getUnits().subscribe((units) => this.units.set(units));
  }
}
