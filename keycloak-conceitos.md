# NONCE: "number used once" (número usado uma vez)
- Seu propósito é garantir que uma solicitação ou resposta não possa ser reutilizada. Em um contexto de OAuth, um nonce é um valor aleatório gerado pelo cliente que é enviado para o servidor de autorização e deve ser único para cada requisição.
- ajuda a proteger contra ataques de repetição (replay attacks). O servidor guarda o nonce e verifica se ele já foi usado antes. Se tiver sido, a solicitação é rejeitada, aumentando assim a segurança do sistema.

# STATE:
- é uma string que o cliente pode enviar ao servidor de autorização. Seu principal objetivo é manter o estado entre a requisição e a resposta. O estado é geralmente um valor gerado aleatoriamente que pode servir para restaurar o estado da aplicação no momento em que a autorização é concluída.
-protege contra ataques de XSS (Cross-Site Scripting) e CSRF (Cross-Site Request Forgery). O cliente deve verificar o valor do "state" que retorna no redirecionamento, garantindo que corresponde ao que foi enviado originalmente. Isso previne que um atacante possa redirecionar o usuário de volta para a aplicação com tokens comprometidos.

# Valid redirect URIs 
ex: http://localhost:4200/  (precisa ter uma "/" pelo menos)

# Direct access grants
- deve estar desativado pois habilita o cliente (angular por exemplo) a conseguir um token passando username e senha de um usuario, o certo é só o próprio usuário fazer isso por meio do login diretamente no keycloak

# dentro do realm deve-se criar as roles dos usuários que o spring usa por exemplo, nesse caso "VISITANTE" e "ADMIN"

# keycloak possui uma api rest
- pra logar precisa passar username e senha do admin e vai receber um token jwt
- depois usa-se esse token pra consumir a api
- dá pra acessar pelos endpoints tudo o que tem dá pra acessar pela interface

# "Client authentication" deixar OFF
- o cliente é público, pois é o app angular e não há uma maneira segura de armazenar credenciais de cliente em um aplicativo do lado do cliente, importante ser o mais específico possível sobre as URL de redirecionamento pois pode causar vulnerabilidade de segurança.
Os clientes HTML5/JavaScript sempre precisam ser clientes públicos porque não há como transmitir o segredo do cliente a eles de maneira segura.

# "Capability config" -> "Authorization"
- Isto é para quando você cria um cliente para um aplicativo backend que serve como um servidor de recursos. Nesse caso, o cliente será confidencial.
- Se você quiser criar um cliente para um aplicativo frontend, autenticar um usuário e obter um JWT, não precisará disso.