import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { jwtInterceptor } from './jwt.interceptor';
import { KeycloakService } from '../auth/keycloak.service';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let kcMock: { updateToken: ReturnType<typeof vi.fn>; getToken: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    kcMock = {
      updateToken: vi.fn().mockResolvedValue(undefined),
      getToken: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: KeycloakService, useValue: kcMock },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe aggiungere l\'header Authorization con il token Bearer se presente', async () => {
    kcMock.getToken.mockReturnValue('token.jwt.valido');

    http.get('/api/test').subscribe();
    await vi.waitFor(() => {
      const req = httpMock.expectOne('/api/test');
      expect(req.request.headers.get('Authorization')).toBe('Bearer token.jwt.valido');
      req.flush({});
    });
  });

  it('non dovrebbe aggiungere l\'header Authorization se il token è vuoto', async () => {
    kcMock.getToken.mockReturnValue('');

    http.get('/api/test').subscribe();
    await vi.waitFor(() => {
      const req = httpMock.expectOne('/api/test');
      expect(req.request.headers.has('Authorization')).toBe(false);
      req.flush({});
    });
  });

  it('dovrebbe chiamare updateToken() prima di ogni richiesta HTTP', async () => {
    kcMock.getToken.mockReturnValue('');

    http.get('/api/test').subscribe();
    await vi.waitFor(() => {
      httpMock.expectOne('/api/test').flush({});
    });

    expect(kcMock.updateToken).toHaveBeenCalledOnce();
  });
});
