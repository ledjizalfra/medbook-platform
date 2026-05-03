import { of, throwError, delay } from 'rxjs';
import { BaseStore } from './base-store';

/** Implementazione concreta per i test */
class TestStore extends BaseStore<string[]> {
  constructor(ttlMs = 5000) { super(ttlMs); }

  loadItems(fetcher: () => any) {
    return this.load(fetcher);
  }
}

describe('BaseStore', () => {
  let store: TestStore;

  beforeEach(() => {
    store = new TestStore(5000);
  });

  it('deve iniziare con data null', () => {
    expect(store.data()).toBeNull();
  });

  it('deve caricare i dati dal fetcher', (done) => {
    const items = ['a', 'b', 'c'];
    store.loadItems(() => of(items)).subscribe(result => {
      expect(result).toEqual(items);
      expect(store.data()).toEqual(items);
      done();
    });
  });

  it('deve restituire dati dalla cache se TTL non scaduto', (done) => {
    const items = ['cached'];
    let callCount = 0;
    const fetcher = () => { callCount++; return of(items); };

    store.loadItems(fetcher).subscribe(() => {
      store.loadItems(fetcher).subscribe(result => {
        expect(result).toEqual(items);
        expect(callCount).toBe(1); // fetcher chiamato solo una volta
        done();
      });
    });
  });

  it('deve ricaricare dopo invalidate()', (done) => {
    const items1 = ['first'];
    const items2 = ['second'];
    let call = 0;
    const fetcher = () => of(call++ === 0 ? items1 : items2);

    store.loadItems(fetcher).subscribe(() => {
      store.invalidate();
      expect(store.data()).toBeNull();

      store.loadItems(fetcher).subscribe(result => {
        expect(result).toEqual(items2);
        done();
      });
    });
  });

  it('deve restituire array vuoto in caso di errore', (done) => {
    const fetcher = () => throwError(() => new Error('HTTP error'));

    store.loadItems(fetcher).subscribe(result => {
      expect(result).toEqual([]);
      done();
    });
  });

  it('deve ricaricare dopo TTL scaduto', (done) => {
    const shortTtlStore = new TestStore(0); // TTL 0ms = scade subito
    let callCount = 0;
    const fetcher = () => { callCount++; return of(['data']); };

    shortTtlStore.loadItems(fetcher).subscribe(() => {
      shortTtlStore.loadItems(fetcher).subscribe(() => {
        expect(callCount).toBe(2); // fetcher chiamato due volte
        done();
      });
    });
  });

  it('invalidate() deve resettare tutto lo stato', () => {
    store.invalidate();
    expect(store.data()).toBeNull();
  });
});
