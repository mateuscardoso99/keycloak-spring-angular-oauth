import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';

export function initializeKeycloak(keycloak: KeycloakService) {
    return () =>
        keycloak.init({
            config: {
                url: 'http://localhost:8090' + '/auth',
                realm: 'test', //nome do realm criado no keycloak
                clientId: 'frontend', //clientId criado no keycloak
            },
            /*initOptions: {
                onLoad: 'check-sso',
                silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
            }*/
        });
}