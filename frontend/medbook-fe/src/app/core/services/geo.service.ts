import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, tap } from 'rxjs';

/** Tipo per un paese estero (nome italiano + codice catastale Z-XXX) */
export interface Paese {
  nome: string;
  codice: string;
}

/**
 * Servizio per l'autocompletamento geografico progressivo.
 *
 * Carica il JSON dei comuni italiani una volta sola e lo tiene in cache.
 * Cascata: Regione => Provincia => Comune => CAP.
 *
 * La provincia e identificata da sigla (TO) e nome esteso (Torino).
 * Nei select: value = NOME ESTESO uppercase, label = "NOME (SIGLA)".
 * A DB si salva il nome esteso. La sigla si usa nel codice per il CF.
 */
@Injectable({ providedIn: 'root' })
export class GeoService {

  private readonly http = inject(HttpClient);

  private comuni: any[] = [];
  private paesi: Paese[] = [];

  private load(): Observable<any[]> {
    if (this.comuni.length > 0) return of(this.comuni);
    return this.http.get<any[]>('data/comuni.json').pipe(
      tap(data => this.comuni = data)
    );
  }

  private loadPaesi(): Observable<Paese[]> {
    if (this.paesi.length > 0) return of(this.paesi);
    return this.http.get<Paese[]>('data/paesi.json').pipe(
      tap(data => this.paesi = data)
    );
  }

  /** Lista regioni uniche, ordinate alfabeticamente */
  getRegioni(): Observable<string[]> {
    return this.load().pipe(
      map(comuni => [...new Set(comuni.map((c: any) => c.regione.nome))].sort())
    );
  }

  /** Tutte le province italiane — restituisce sigla + nome, ordinate alfabeticamente */
  getAllProvince(): Observable<{ sigla: string; nome: string }[]> {
    return this.load().pipe(
      map(comuni => {
        const m = new Map<string, string>();
        comuni.forEach((c: any) => m.set(c.sigla, c.provincia.nome));
        return [...m.entries()]
          .map(([sigla, nome]) => ({ sigla, nome }))
          .sort((a, b) => a.nome.localeCompare(b.nome));
      })
    );
  }

  /** Province filtrate per regione — restituisce sigla + nome */
  getProvince(regione: string): Observable<{ sigla: string; nome: string }[]> {
    return this.load().pipe(
      map(comuni => {
        const m = new Map<string, string>();
        comuni
          .filter((c: any) => c.regione.nome === regione)
          .forEach((c: any) => m.set(c.sigla, c.provincia.nome));
        return [...m.entries()]
          .map(([sigla, nome]) => ({ sigla, nome }))
          .sort((a, b) => a.nome.localeCompare(b.nome));
      })
    );
  }

  /** Comuni filtrati per nome provincia (case-insensitive), ordinati alfabeticamente */
  getComuni(nomeProvincia: string): Observable<string[]> {
    const upper = nomeProvincia?.toUpperCase();
    return this.load().pipe(
      map(comuni => comuni
        .filter((c: any) => (c.provincia.nome as string).toUpperCase() === upper)
        .map((c: any) => c.nome)
        .sort()
      )
    );
  }

  /** Primo CAP del comune — sincrono (richiede cache caricata) */
  getCap(nomeComune: string): string | null {
    const c = this.comuni.find((c: any) => c.nome === nomeComune);
    return c?.cap?.[0] ?? null;
  }

  /** Risolve un nome provincia (es. "TORINO") nella sigla (es. "TO").
   *  Sincrono — richiede cache caricata. Usato per il calcolo del CF. */
  getSigla(nomeProvincia: string): string {
    if (!nomeProvincia) return '';
    const upper = nomeProvincia.toUpperCase();
    const c = this.comuni.find((c: any) =>
      (c.provincia.nome as string).toUpperCase() === upper
    );
    return c?.sigla ?? nomeProvincia;
  }

  /** Pre-carica la cache — da chiamare all'init dei componenti che usano metodi sincroni */
  preload(): Observable<void> {
    return this.load().pipe(map(() => undefined));
  }

  /** Lista paesi del mondo ordinata alfabeticamente */
  getPaesi(): Observable<Paese[]> {
    return this.loadPaesi().pipe(
      map(paesi => [...paesi].sort((a, b) => a.nome.localeCompare(b.nome)))
    );
  }
}
