import { APP_INITIALIZER, ApplicationConfig, Provider, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { KeycloakBearerInterceptor, KeycloakService } from 'keycloak-angular';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

export function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
      keycloak.init({
          config: {
              url: 'http://localhost:8090',
              realm: 'teste', //nome do realm criado no keycloak
              clientId: 'frontend', //clientId criado no keycloak
          },
          initOptions: {
              //onLoad: 'check-sso',
              //silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
              checkLoginIframe: false,
              redirectUri: 'http://localhost:4200'
          },
          // Enables Bearer interceptor
          enableBearerInterceptor: true,
          // Prefix for the Bearer token
          bearerPrefix: 'Bearer',
          // URLs excluded from Bearer token addition (empty by default)
          //bearerExcludedUrls: [],
          shouldAddToken: (request) => {//excluir requisições que não devem ter o cabeçalho de autorização
            const { method, url } = request;
            const isGetRequest = 'GET' === method.toUpperCase();
            const acceptablePaths = ['/assets', '/clients/public'];
            const isAcceptablePathMatch = acceptablePaths.some((path) =>
              url.includes(path)
            );
            return !(isGetRequest && isAcceptablePathMatch);
          }
      });
}

// Provider for Keycloak Bearer Interceptor
const KeycloakBearerInterceptorProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: KeycloakBearerInterceptor,
  multi: true
};
 
// Provider for Keycloak Initialization
const KeycloakInitializerProvider: Provider = {
  provide: APP_INITIALIZER, //APP_INITIALIZER função que será executada antes de carregar o restante do aplicativo
  useFactory: initializeKeycloak,
  multi: true,
  deps: [KeycloakService]
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptorsFromDi()), // Provides HttpClient with interceptors
    KeycloakInitializerProvider, // Initializes Keycloak
    KeycloakBearerInterceptorProvider, // Provides Keycloak Bearer Interceptor
    KeycloakService,
    provideZoneChangeDetection({ eventCoalescing: true }), provideRouter(routes),
    provideAnimations()
  ]
};
