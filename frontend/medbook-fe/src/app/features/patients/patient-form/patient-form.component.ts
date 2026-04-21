import { Component, inject, signal, OnInit } from '@angular/core';
import { AsyncPipe, UpperCasePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { KeycloakService } from '../../../core/auth/keycloak.service';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';

import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InfoDialogComponent } from '../../../shared/components/info-dialog/info-dialog.component';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MedBookPageComponent } from '../../../shared/components/medbook-page/medbook-page.component';
import { PatientService } from '../../../core/services/patient.service';
import { GeoService } from '../../../core/services/geo.service';
import { MedBookValidators } from '../../../core/validators/medbook.validators';
import { NOTIFICATION_CHANNEL, SNACKBAR_DURATION } from '../../../core/constants/ui.constants';
import { MedBookFormComponent } from '../../../shared/components/medbook-form/medbook-form.component';
import { MedBookFormSlotDirective } from '../../../shared/components/medbook-form/medbook-form-slot.directive';
import { MbFormGroup } from '../../../shared/components/medbook-form/medbook-form.models';
import { Paese } from '../../../core/services/geo.service';

/**
 * Componente form per la creazione e modifica di un paziente.
 *
 * I campi standard sono renderizzati da MedBookFormComponent tramite [groups].
 * Le sezioni complesse (luogo di nascita, residenza, consensi) usano
 * ng-template medbookFormSlot per il rendering custom.
 */
@Component({
  selector: 'app-patient-form',
  imports: [
    AsyncPipe,
    UpperCasePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatSnackBarModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MedBookFormComponent,
    MedBookFormSlotDirective,
    MedBookPageComponent
  ],
  templateUrl: './patient-form.component.html',
  styleUrl: './patient-form.component.scss'
})
export class PatientFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private patientService = inject(PatientService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  protected geoService = inject(GeoService);
  private kc = inject(KeycloakService);

  protected patientId = signal<string | null>(null);
  protected loading = signal(false);
  protected saving = signal(false);
  protected natoEstero = signal(false);

  // Observable per i select geografici in cascata — residenza
  protected regioni$ = this.geoService.getRegioni();
  protected province$: Observable<{ sigla: string; nome: string }[]> = of([]);
  protected comuni$: Observable<string[]> = of([]);

  // Observable per i select geografici in cascata — nascita
  protected regioniNascita$ = this.geoService.getRegioni();
  protected provinceNascita$: Observable<{ sigla: string; nome: string }[]> = of([]);
  protected comuniNascita$: Observable<string[]> = of([]);

  // Lista paesi del mondo — per nati all'estero
  protected paesi$: Observable<Paese[]> = this.geoService.getPaesi();

  protected form = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    email:       ['', [Validators.required, MedBookValidators.email()]],
    phone:       ['', MedBookValidators.telefono()],
    dateOfBirth: [null as Date | null, [Validators.required, MedBookValidators.dataPassata()]],
    gender:      ['', Validators.required],
    fiscalCode:  ['', [Validators.required, MedBookValidators.codiceFiscale()]],
    address:     [''],
    regione:     [''],
    province:    [{ value: '', disabled: true }],
    city:        [{ value: '', disabled: true }],
    postalCode:  [''],
    // Luogo di nascita
    regioneNascita:   [''],
    provinciaNascita: [{ value: '', disabled: true }],
    comuneNascita:    [{ value: '', disabled: true }],
    // Consensi GDPR
    consensoPrivacy:      [false, Validators.requiredTrue],
    consensoCommerciale:  [false],
    consensoProfilazione: [false],
    notificationChannels: this.fb.group({
      email: [true],
      sms:   [false]
    })
  }, { validators: [
    MedBookValidators.codiceFiscaleConcordanza(),
    MedBookValidators.canaleDiNotificaValido()
  ] });

  /** Configurazione dichiarativa dei gruppi del form */
  protected readonly formGroups: MbFormGroup[] = [
    {
      id: 'anagrafica', columns: 2,
      cells: [
        { key: 'firstName', type: 'text', label: 'Nome' },
        { key: 'lastName',  type: 'text', label: 'Cognome' }
      ]
    },
    {
      id: 'contatti', columns: 1,
      cells: [
        { key: 'email', type: 'email', label: 'Email' }
      ]
    },
    {
      id: 'telefono', columns: 1,
      cells: [
        { key: 'phone', type: 'text', label: 'Telefono', hint: 'Es: 333 123 4567' }
      ]
    },
    {
      id: 'nascita', columns: 2,
      cells: [
        { key: 'dateOfBirth', type: 'date', label: 'Data di nascita' },
        { key: 'gender', type: 'select', label: 'Genere',
          options: [{ value: 'MASCHILE', label: 'Maschile' }, { value: 'FEMMINILE', label: 'Femminile' }] }
      ]
    },
    {
      id: 'cf', columns: 1,
      cells: [
        { key: 'fiscalCode', type: 'text', label: 'Codice Fiscale' }
      ]
    },
    { id: 'luogoNascita', label: 'Luogo di nascita', customTemplate: true },
    { id: 'residenza', label: 'Residenza', customTemplate: true },
    {
      id: 'notifiche', columns: 1,
      cells: [
        { key: 'notificationChannels', type: 'checkbox-group', label: 'Canali di notifica',
          formGroupName: 'notificationChannels',
          checkboxes: [
            { key: 'email', label: 'Email' },
            { key: 'sms',   label: 'SMS' }
          ]
        }
      ]
    },
    { id: 'consensi', label: 'Consensi', customTemplate: true }
  ];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.patientId.set(id);
      this.loadPatient(id);
    }
  }

  private loadPatient(id: string): void {
    this.loading.set(true);
    this.patientService.getById(id).subscribe({
      next: (data: unknown) => {
        const patient = (data as Record<string, unknown>)['data'] as Record<string, unknown> ?? {};
        const channels = (patient['notificationChannels'] as string[] | undefined) ?? [];

        // Se provincia nascita è 'EE' il paziente è nato all'estero
        const provNascita = patient['provinciaNascita'] as string ?? '';
        if (provNascita === 'EE') {
          this.natoEstero.set(true);
          this.form.get('comuneNascita')?.enable();
        }

        this.form.patchValue({
          firstName:   patient['firstName']   as string,
          lastName:    patient['lastName']    as string,
          email:       patient['email']       as string,
          phone:       patient['phone']       as string,
          dateOfBirth: patient['dateOfBirth'] ? new Date(patient['dateOfBirth'] as string) : null,
          gender:      patient['gender']      as string,
          fiscalCode:  patient['fiscalCode']  as string,
          address:     patient['address']     as string,
          city:        patient['city']        as string,
          postalCode:  patient['postalCode']  as string,
          province:    patient['province']    as string,
          regioneNascita:   patient['regioneNascita']   as string,
          provinciaNascita: patient['provinciaNascita']  as string,
          comuneNascita:    patient['comuneNascita']     as string,
          consensoPrivacy:      patient['consensoPrivacy']      as boolean ?? false,
          consensoCommerciale:  patient['consensoCommerciale']  as boolean ?? false,
          consensoProfilazione: patient['consensoProfilazione'] as boolean ?? false,
          notificationChannels: {
            email: channels.includes(NOTIFICATION_CHANNEL.EMAIL),
            sms:   channels.includes(NOTIFICATION_CHANNEL.SMS)
          }
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.dialog.open(InfoDialogComponent, {
          data: { title: 'Errore', message: 'Impossibile caricare i dettagli. Riprova più tardi.', icon: 'error_outline' }
        });
      }
    });
  }

  /** Quando cambia la regione: resetta provincia, comune, CAP e aggiorna la lista */
  protected onRegioneChange(regione: string): void {
    this.form.get('province')?.enable();
    this.form.get('city')?.disable();
    this.form.patchValue({ province: '', city: '', postalCode: '' });
    this.province$ = this.geoService.getProvince(regione);
    this.comuni$   = of([]);
  }

  protected onProvinciaChange(nomeProvincia: string): void {
    this.form.get('city')?.enable();
    this.form.patchValue({ city: '', postalCode: '' });
    this.comuni$ = this.geoService.getComuni(nomeProvincia);
  }

  /** Quando cambia il comune: auto-popola il CAP */
  protected onComuneChange(nomeComune: string): void {
    const cap = this.geoService.getCap(nomeComune);
    if (cap) this.form.patchValue({ postalCode: cap });
  }

  // --- Luogo di nascita ---

  protected onNatoEsteroChange(estero: boolean): void {
    this.natoEstero.set(estero);
    this.form.patchValue({ regioneNascita: '', provinciaNascita: '', comuneNascita: '' });
    if (estero) {
      this.form.get('regioneNascita')?.enable();
      this.form.get('provinciaNascita')?.disable();
      this.form.get('comuneNascita')?.enable();
    } else {
      this.form.get('regioneNascita')?.enable();
      this.form.get('provinciaNascita')?.disable();
      this.form.get('comuneNascita')?.disable();
      this.provinceNascita$ = of([]);
      this.comuniNascita$ = of([]);
    }
  }

  protected onRegioneNascitaChange(regione: string): void {
    this.form.get('provinciaNascita')?.enable();
    this.form.get('comuneNascita')?.disable();
    this.form.patchValue({ provinciaNascita: '', comuneNascita: '' });
    this.provinceNascita$ = this.geoService.getProvince(regione);
    this.comuniNascita$ = of([]);
  }

  protected onProvinciaNascitaChange(nomeProvincia: string): void {
    this.form.get('comuneNascita')?.enable();
    this.form.patchValue({ comuneNascita: '' });
    this.comuniNascita$ = this.geoService.getComuni(nomeProvincia);
  }

  protected onSubmit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);

    const id = this.patientId();
    const channels = this.form.value.notificationChannels;
    const payload = {
      ...this.form.value,
      emailEnabled: channels?.email ?? false,
      smsEnabled:   channels?.sms   ?? false,
      // Nascita — per nati all'estero la provincia è 'EE'
      provinciaNascita: this.natoEstero()
        ? 'EE'
        : this.form.value.provinciaNascita,
    };
    delete (payload as Record<string, unknown>)['notificationChannels'];
    delete (payload as Record<string, unknown>)['regione'];

    const operation = id
      ? this.patientService.update(id, payload)
      : this.patientService.create(payload);

    operation.subscribe({
      next: () => {
        this.saving.set(false);
        const title = id ? 'Aggiornamento completato' : 'Operazione completata';
        const msg = id ? 'Paziente aggiornato con successo!' : 'Paziente creato con successo!';
        this.dialog.open(InfoDialogComponent, { data: { title, message: msg } });
        this.router.navigate([this.kc.getPatientsRoute()]);
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Errore durante il salvataggio. Riprova.', 'Chiudi', { duration: SNACKBAR_DURATION.LONG });
      }
    });
  }

  protected goBack(): void {
    this.router.navigate([this.kc.getPatientsRoute()]);
  }

  protected get pageTitle(): string {
    return this.patientId() ? 'Modifica paziente' : 'Nuovo paziente';
  }
}
