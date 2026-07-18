import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { CommissionsService } from '../commissions.service';
import { Commission } from '../models/commission.model';
import { GoalDialog, GoalDialogData } from '../goal-dialog/goal-dialog';

@Component({
  selector: 'app-commissions-admin',
  imports: [CurrencyPipe, DecimalPipe, MatTableModule, MatButtonModule, MatIconModule],
  templateUrl: './commissions-admin.html',
  styleUrl: './commissions-admin.scss',
})
export class CommissionsAdmin {
  private readonly commissionsService = inject(CommissionsService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly displayedColumns = ['seller', 'target', 'achieved', 'attainment', 'commission', 'actions'];
  protected readonly month = signal<string>(new Date().toISOString().slice(0, 7));
  protected readonly rows = signal<Commission[]>([]);
  protected readonly loading = signal(false);

  protected readonly totalCommission = computed(() => this.rows().reduce((s, r) => s + r.commission, 0));
  protected readonly totalAchieved = computed(() => this.rows().reduce((s, r) => s + r.achievedSales, 0));

  constructor() {
    effect(() => {
      const month = this.month();
      this.loading.set(true);
      this.commissionsService.getCommissions(month).subscribe({
        next: (rows) => {
          this.rows.set(rows);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    });
  }

  protected onMonthChange(value: string): void {
    if (value) {
      this.month.set(value);
    }
  }

  protected editGoal(row: Commission): void {
    const data: GoalDialogData = {
      sellerId: row.sellerId,
      sellerName: row.sellerName,
      month: this.month(),
      targetAmount: row.targetAmount,
      commissionRate: row.commissionRate,
      bonusRate: row.bonusRate,
    };
    const ref = this.dialog.open(GoalDialog, { width: '420px', data });
    ref.afterClosed().subscribe((saved) => {
      if (saved) {
        this.refresh();
        this.snackBar.open('Meta guardada', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private refresh(): void {
    this.commissionsService.getCommissions(this.month()).subscribe((rows) => this.rows.set(rows));
  }
}
