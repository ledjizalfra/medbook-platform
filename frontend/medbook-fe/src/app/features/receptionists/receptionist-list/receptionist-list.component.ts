import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { TableColumn, TableAction } from '../../../shared/components/medbook-table/medbook-table.models';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';

@Component({
  selector: 'app-receptionist-list',
  imports: [MatButtonModule, MatIconModule, MatSnackBarModule,
            MedBookTableComponent, MedBookPageComponent],
  templateUrl: './receptionist-list.component.html',
  styleUrl: './receptionist-list.component.scss'
})
export class ReceptionistListComponent implements OnInit {
  private service = inject(ReceptionistService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  protected loading = signal(true);
  protected receptionists = signal<unknown[]>([]);
  protected pageIndex = signal(0);
  protected pageSize = signal(10);
  protected totalElements = signal(0);

  protected readonly columns: TableColumn[] = [
    { key: 'firstName', header: 'Nome' },
    { key: 'lastName', header: 'Cognome' },
    { key: 'email', header: 'Email' },
    { key: 'enabled', header: 'Stato', type: 'badge' },
    { key: 'createdAt', header: 'Creato il', type: 'date', dateFormat: 'dd/MM/yyyy HH:mm' }
  ];

  protected readonly tableActions: TableAction[] = [
    { icon: 'edit', tooltip: 'Modifica', onClick: (row) => this.edit(row) },
    { icon: 'delete', tooltip: 'Elimina', color: 'warn', onClick: (row) => this.confirmDelete(row) }
  ];

  ngOnInit(): void { this.loadData(); }

  protected onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadData();
  }

  protected newReceptionist(): void {
    this.router.navigate(['/admin/receptionists/new']);
  }

  private loadData(): void {
    this.loading.set(true);
    this.service.getAll({ page: this.pageIndex(), size: this.pageSize() }).subscribe({
      next: (resp: unknown) => {
        const r = (resp as Record<string, unknown>)['data'] as Record<string, unknown>;
        const content = r['content'] as unknown[] ?? [];
        // Converti enabled boolean in label leggibile
        content.forEach(item => {
          const row = item as Record<string, unknown>;
          row['enabled'] = row['enabled'] ? 'ATTIVO' : 'DISABILITATO';
        });
        this.receptionists.set(content);
        this.totalElements.set(r['totalElements'] as number ?? content.length);
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  private edit(row: unknown): void {
    const id = (row as Record<string, unknown>)['keycloakId'];
    this.router.navigate(['/admin/receptionists', id, 'edit']);
  }

  private confirmDelete(row: unknown): void {
    const r = row as Record<string, unknown>;
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Elimina receptionist', message: `Eliminare definitivamente ${r['firstName']} ${r['lastName']}?` }
    }).afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.service.delete(r['keycloakId'] as string).subscribe({
        next: () => {
          this.dialog.open(InfoDialogComponent, { data: { title: 'Eliminato', message: 'Receptionist eliminato.' } });
          this.loadData();
        },
        error: () => this.snackBar.open('Errore', 'Chiudi', { duration: SNACKBAR_DURATION.LONG })
      });
    });
  }
}
