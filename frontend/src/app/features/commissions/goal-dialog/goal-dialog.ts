import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommissionsService } from '../commissions.service';

export interface GoalDialogData {
  sellerId: number;
  sellerName: string;
  month: string;
  targetAmount: number | null;
  commissionRate: number | null;
  bonusRate: number | null;
}

@Component({
  selector: 'app-goal-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './goal-dialog.html',
  styleUrl: './goal-dialog.scss',
})
export class GoalDialog {
  private readonly fb = inject(FormBuilder);
  private readonly commissionsService = inject(CommissionsService);
  private readonly dialogRef = inject(MatDialogRef<GoalDialog>);
  protected readonly data = inject<GoalDialogData>(MAT_DIALOG_DATA);

  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    targetAmount: [this.data.targetAmount ?? 0, [Validators.required, Validators.min(0)]],
    commissionRate: [this.data.commissionRate ?? 2, [Validators.required, Validators.min(0), Validators.max(100)]],
    bonusRate: [this.data.bonusRate ?? 0, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    this.commissionsService
      .upsertGoal({
        sellerId: this.data.sellerId,
        month: this.data.month,
        targetAmount: value.targetAmount,
        commissionRate: value.commissionRate,
        bonusRate: value.bonusRate,
      })
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(err?.error?.message ?? 'No se pudo guardar la meta.');
        },
      });
  }
}
