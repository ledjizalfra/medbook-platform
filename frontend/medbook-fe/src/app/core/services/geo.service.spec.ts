import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { GeoService } from './geo.service';

/**
 * Mock dei dati comuni italiani restituiti dal JSON statico.
 * Struttura minima che rispecchia il formato reale del file data/comuni.json.
 */
const MOCK_COMUNI = [
  {
    nome: 'Torino',
    sigla: 'TO',
    regione: { nome: 'Piemonte' },
    provincia: { nome: 'Torino' },
    cap: ['10121'],
  },
  {
    nome: 'Moncalieri',
    sigla: 'TO',
    regione: { nome: 'Piemonte' },
    provincia: { nome: 'Torino' },
    cap: ['10024'],
  },
  {
    nome: 'Milano',
    sigla: 'MI',
    regione: { nome: 'Lombardia' },
    provincia: { nome: 'Milano' },
    cap: ['20121'],
  },
  {
    nome: 'Roma',
    sigla: 'RM',
    regione: { nome: 'Lazio' },
    provincia: { nome: 'Roma' },
    cap: ['00118'],
  },
];

const MOCK_PAESI = [
  { nome: 'Francia', codice: 'Z110' },
  { nome: 'Germania', codice: 'Z112' },
];

describe('GeoService', () => {
  let service: GeoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GeoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ---------------------------------------------------------------------------
  // Helper: carica la cache dei comuni e dei paesi
  // ---------------------------------------------------------------------------

  /** Sottoscrive un metodo che triggera il caricamento del JSON e fluscia il mock. */
  function flushComuni(): void {
    const req = httpMock.expectOne('data/comuni.json');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_COMUNI);
  }

  function flushPaesi(): void {
    const req = httpMock.expectOne('data/paesi.json');
    expect(req.request.method).toBe('GET');
    req.flush(MOCK_PAESI);
  }

  // ---------------------------------------------------------------------------
  // getRegioni()
  // ---------------------------------------------------------------------------

  it('getRegioni() dovrebbe fare GET su data/comuni.json e restituire le regioni uniche ordinate', () => {
    let result: string[] = [];
    service.getRegioni().subscribe(r => result = r);
    flushComuni();
    expect(result).toEqual(['Lazio', 'Lombardia', 'Piemonte']);
  });

  it('getRegioni() dovrebbe usare la cache alla seconda chiamata (no seconda richiesta HTTP)', () => {
    service.getRegioni().subscribe();
    flushComuni();

    let result: string[] = [];
    service.getRegioni().subscribe(r => result = r);
    // Nessuna ulteriore richiesta HTTP — httpMock.verify() lo garantisce
    expect(result.length).toBe(3);
  });

  // ---------------------------------------------------------------------------
  // getAllProvince()
  // ---------------------------------------------------------------------------

  it('getAllProvince() dovrebbe restituire tutte le province con sigla e nome', () => {
    let result: { sigla: string; nome: string }[] = [];
    service.getAllProvince().subscribe(r => result = r);
    flushComuni();
    expect(result.length).toBe(3);
    expect(result.map(p => p.sigla).sort()).toEqual(['MI', 'RM', 'TO']);
  });

  // ---------------------------------------------------------------------------
  // getProvince(regione)
  // ---------------------------------------------------------------------------

  it('getProvince() dovrebbe restituire solo le province della regione indicata', () => {
    let result: { sigla: string; nome: string }[] = [];
    service.getProvince('Piemonte').subscribe(r => result = r);
    flushComuni();
    expect(result.length).toBe(1);
    expect(result[0].sigla).toBe('TO');
    expect(result[0].nome).toBe('Torino');
  });

  // ---------------------------------------------------------------------------
  // getComuni(nomeProvincia)
  // ---------------------------------------------------------------------------

  it('getComuni() dovrebbe restituire i comuni della provincia ordinati alfabeticamente', () => {
    let result: string[] = [];
    service.getComuni('Torino').subscribe(r => result = r);
    flushComuni();
    expect(result).toEqual(['Moncalieri', 'Torino']);
  });

  it('getComuni() dovrebbe essere case-insensitive sul nome provincia', () => {
    let result: string[] = [];
    service.getComuni('TORINO').subscribe(r => result = r);
    flushComuni();
    expect(result.length).toBe(2);
  });

  // ---------------------------------------------------------------------------
  // getCap(nomeComune) — sincrono, richiede cache pre-caricata
  // ---------------------------------------------------------------------------

  it('getCap() dovrebbe restituire il primo CAP del comune dopo il preload', () => {
    service.preload().subscribe();
    flushComuni();
    expect(service.getCap('Torino')).toBe('10121');
  });

  it('getCap() dovrebbe restituire null per un comune inesistente', () => {
    service.preload().subscribe();
    flushComuni();
    expect(service.getCap('Atlantide')).toBeNull();
  });

  // ---------------------------------------------------------------------------
  // getSigla(nomeProvincia) — sincrono, richiede cache pre-caricata
  // ---------------------------------------------------------------------------

  it('getSigla() dovrebbe restituire la sigla della provincia dopo il preload', () => {
    service.preload().subscribe();
    flushComuni();
    expect(service.getSigla('TORINO')).toBe('TO');
  });

  it('getSigla() dovrebbe restituire stringa vuota se il parametro e nullo', () => {
    service.preload().subscribe();
    flushComuni();
    expect(service.getSigla('')).toBe('');
  });

  // ---------------------------------------------------------------------------
  // preload()
  // ---------------------------------------------------------------------------

  it('preload() dovrebbe fare GET su data/comuni.json e completare senza valore', () => {
    let completed = false;
    service.preload().subscribe({ complete: () => completed = true });
    flushComuni();
    expect(completed).toBe(true);
  });

  // ---------------------------------------------------------------------------
  // getPaesi()
  // ---------------------------------------------------------------------------

  it('getPaesi() dovrebbe fare GET su data/paesi.json e restituire i paesi ordinati', () => {
    let result: { nome: string; codice: string }[] = [];
    service.getPaesi().subscribe(r => result = r);
    flushPaesi();
    expect(result[0].nome).toBe('Francia');
    expect(result[1].nome).toBe('Germania');
  });

  it('getPaesi() dovrebbe usare la cache alla seconda chiamata', () => {
    service.getPaesi().subscribe();
    flushPaesi();

    let result: { nome: string; codice: string }[] = [];
    service.getPaesi().subscribe(r => result = r);
    // Nessuna ulteriore richiesta HTTP
    expect(result.length).toBe(2);
  });
});
