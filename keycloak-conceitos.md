# Valid redirect URIs 
ex: http://localhost:4200/  (precisa ter uma "/" pelo menos)

# Direct access grants
- deve estar desativado pois habilita o cliente (angular por exemplo) a conseguir um token passando username e senha de um usuario, o certo é só o próprio usuário fazer isso por meio do login diretamente no keycloak

# dentro do realm deve-se criar as roles dos usuários que o spring usa por exemplo, nesse caso "VISITANTE" e "ADMIN"

# keycloak possui uma api rest
- pra logar precisa passar username e senha do admin e vai receber um token jwt
- depois usa-se esse token pra consumir a api
- dá pra acessar pelos endpoints tudo o que tem dá pra acessar pela interface