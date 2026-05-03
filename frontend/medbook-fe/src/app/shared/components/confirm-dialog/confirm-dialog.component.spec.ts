import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ConfirmDialogComponent, ConfirmDialogData } from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  let nativeEl: HTMLElement;
  let dialogRefSpy: { close: ReturnType<typeof vi.fn> };

  const defaultData: ConfirmDialogData = {
    title: 'Conferma eliminazione',
    message: 'Sei sicuro di voler eliminare questo paziente?'
  };

  beforeEach(async () => {
    dialogRefSpy = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: MAT_DIALOG_DATA, useValue: defaultData },
        { provide: MatDialogRef, useValue: dialogRefSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
    nativeEl = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // --- Rendering titolo ---

  it('should display the title in the dialog title', () => {
    const titleEl = nativeEl.querySelector('[mat-dialog-title]');
    expect(titleEl).toBeTruthy();
    expect(titleEl!.textContent!.trim()).toBe('Conferma eliminazione');
  });

  // --- Rendering messaggio ---

  it('should display the message in the dialog content', () => {
    const content = nativeEl.querySelector('mat-dialog-content p');
    expect(content).toBeTruthy();
    expect(content!.textContent!.trim()).toBe('Sei sicuro di voler eliminare questo paziente?');
  });

  // --- Pulsanti ---

  it('should render Annulla and Conferma buttons', () => {
    const buttons = nativeEl.querySelectorAll('mat-dialog-actions button');
    expect(buttons.length).toBe(2);

    const buttonTexts = Array.from(buttons).map(b => b.textContent!.trim());
    expect(buttonTexts).toContain('Annulla');
    expect(buttonTexts).toContain('Conferma');
  });

  // --- Click Annulla ---

  it('should close dialog with false when Annulla is clicked', () => {
    const buttons = nativeEl.querySelectorAll('mat-dialog-actions button');
    const annullaBtn = Array.from(buttons).find(b => b.textContent!.trim() === 'Annulla') as HTMLButtonElement;

    annullaBtn.click();

    expect(dialogRefSpy.close).toHaveBeenCalledWith(false);
  });

  // --- Click Conferma ---

  it('should close dialog with true when Conferma is clicked', () => {
    const buttons = nativeEl.querySelectorAll('mat-dialog-actions button');
    const confermaBtn = Array.from(buttons).find(b => b.textContent!.trim() === 'Conferma') as HTMLButtonElement;

    confermaBtn.click();

    expect(dialogRefSpy.close).toHaveBeenCalledWith(true);
  });

  // --- Stile pulsante Conferma ---

  it('should have warn color on Conferma button', () => {
    const buttons = nativeEl.querySelectorAll('mat-dialog-actions button');
    const confermaBtn = Array.from(buttons).find(b => b.textContent!.trim() === 'Conferma');

    expect(confermaBtn!.getAttribute('color')).toBe('warn');
  });

  // --- Dati custom ---

  it('should display custom title and message', async () => {
    const customData: ConfirmDialogData = {
      title: 'Cancella appuntamento',
      message: 'Vuoi cancellare questo appuntamento?'
    };

    const customFixture = TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: customData })
      .createComponent(ConfirmDialogComponent);
    customFixture.detectChanges();
    const el = customFixture.nativeElement;

    expect(el.querySelector('[mat-dialog-title]')!.textContent!.trim()).toBe('Cancella appuntamento');
    expect(el.querySelector('mat-dialog-content p')!.textContent!.trim()).toBe('Vuoi cancellare questo appuntamento?');
  });

  // --- Layout dialog actions ---

  it('should align dialog actions to end', () => {
    const actions = nativeEl.querySelector('mat-dialog-actions');
    expect(actions!.getAttribute('align')).toBe('end');
  });
});
