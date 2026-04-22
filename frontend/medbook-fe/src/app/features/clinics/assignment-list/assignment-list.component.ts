import { Component, inject, signal, OnInit } from '@angular/core';
import { SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { ClinicService } from '../../../core/services/clinic.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { MedBookTableComponent } from '../../../shared/components/medbook-table/medbook-table.component';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { TableColumn } from '../../../shared/components/medbook-table/medbook-table.models';

/**
 * Componente per la gestione delle assegnazioni medici a una clinica.
 *
 * Un'assegnazione collega un medico a una clinica con una specializzazione specifica.
 * È il prerequisito affinché il medico compaia nei risultati di ricerca disponibilità.
 *
 * Funzionalità:
 * - Visualizza le assegnazioni esistenti per la clinica (ID preso dalla rotta)
 * - Permette di creare una nuova assegnazione tramite un form inline
 *   che appare/scompare tramite il signal `showForm`
 * - Carica la lista dei medici all'inizializzazione per popolare il select
 *
 * La clinica di riferimento è identificata dal path param `:id` della rotta
 * `/clinics/:id/assignments`. Accessibile solo agli ADMIN.
 */
@Component({
  selector: 'app-assignment-list',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MedBookPageComponent,
    MedBookTableComponent,
    MedBookFormComponent,
    MedBookFormSlotDirective
  ],
  templateUrl: './assignment-list.component.html',
  styleUrl: './assignment-list.component.scss'
})
export class AssignmentListComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private clinicService = inject(ClinicService);
  private doctorService = inject(DoctorService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private fb = inject(FormBuilder);
  protected kc = inject(KeycloakService);

  // ID della clinica corrente, letto dalla rotta e usato in tutte le chiamate API
  protected clinicId = signal('');

  protected loading = signal(true);
  protected assignments = signal<unknown[]>([]);
  // Lista medici per il select del form - caricata una volta sola all'init
  protected doctors = signal<unknown[]>([]);
  // Controlla la visibilità del form inline di creazione
  protected showForm = signal(false);
  protected saving = signal(false);

  /** Configurazione colonne tabella assegnazioni — doctorName arricchito nel subscribe */
  protected readonly columns: TableColumn[] = [
    { key: '_doctorName',    header: 'Medico' },
    { key: 'specialization', header: 'Specializzazione' },
    { key: 'status',         header: 'Stato', type: 'badge' }
  ];

  /** Mappa doctorId => nome completo — costruita al caricamento dei medici */
  private doctorMap = new Map<string, string>();

  /** Gruppi del form nuova assegnazione: il select medico usa customTemplate per le opzioni dinamiche */
  protected readonly formGroups: MbFormGroup[] = [
    { id: 'doctorSelect', customTemplate: true },
    {
      id: 'specialization', columns: 1,
      cells: [
        { key: 'specialization', type: 'text', label: 'Specializzazione' }
      ]
    }
  ];

  // Form per la nuova assegnazione: medico e specializzazione sono obbligatori
  protected assignmentForm = this.fb.group({
    doctorId:       ['', Validators.required],
    specialization: ['', Validators.required]
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.clinicId.set(id);
    // Carica assegnazioni e medici in parallelo all'inizializzazione
    this.loadAssignments();
    this.loadDoctors();
  }

  protected loadAssignments(): void {
    this.loading.set(true);
    this.clinicService.getAssignments(this.clinicId()).subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        const list = Array.isArray(inner) ? inner as unknown[] : [];
        // Arricchisce con nome medico risolto per MedBookTable
        this.assignments.set(list.map(a => {
          const row = a as Record<string, unknown>;
          return {
            ...row,
            _doctorName: this.doctorMap.get(String(row['doctorId'])) ?? String(row['doctorId'] ?? '')
          };
        }));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Si è verificato un problema durante il caricamento dei dati. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  // Carica tutti i medici per popolare il select e la mappa di risoluzione nomi
  private loadDoctors(): void {
    this.doctorService.getAll().subscribe({
      next: (data: unknown) => {
        const inner = (data as Record<string, unknown>)['data'];
        const list = Array.isArray(inner) ? inner as unknown[] : [];
        this.doctors.set(list);
        list.forEach(d => {
          const doc = d as Record<string, unknown>;
          this.doctorMap.set(
            String(doc['doctorId']),
            String(doc['doctorFullName'] ?? '')
          );
        });
      }
    });
  }

  // Invia la nuova assegnazione; al successo chiude il form e ricarica la lista
  protected createAssignment(): void {
    if (this.assignmentForm.invalid) return;
    this.saving.set(true);

    this.clinicService.createAssignment(this.clinicId(), this.assignmentForm.value).subscribe({
      next: () => {
        this.saving.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Operazione completata', message: 'Assegnazione creata con successo!' }
        });
        this.showForm.set(false);
        this.assignmentForm.reset(); // Pulisce il form per un eventuale uso successivo
        this.loadAssignments();
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Errore durante la creazione. Riprova.', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getClinicsRoute()]);
  }

  protected formatName(obj: unknown): string {
    const r = obj as Record<string, unknown>;
    return String(r['doctorFullName'] ?? '');
  }

  protected getField(obj: unknown, field: string): unknown {
    return (obj as Record<string, unknown>)?.[field];
  }
}
