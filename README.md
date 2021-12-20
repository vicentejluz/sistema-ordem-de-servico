# Projeto 2 Sistema Ordem De Serviço Tópicos Especiais em Informática

TIPO DE NEGÓCIO: EMPRESA DE DESENVOLVIMENTO DE SOFTWARES 

OBJETIVO: 
<p align="justify">Implementar um sistema para processar Ordens de Serviços prestados por uma empresa de desenvolvimento Software (o sistema pode ser composto de um, dois ou três aplicativos compartilhando o mesmo banco de dados). A equipe de análise e desenvolvimento usará esse registro como guia para entregar as tarefas, formalizar o trabalho prestado para o cliente e garantir os resultados tanto para a empresa, quanto para o consumidor. O sistema deve processar os dados do cliente, serviço a ser prestado (no nosso caso o desenvolvimento dos apps), o status do serviço (aberto finalizado, cancelado). O diagrama de classes em anexo apresenta alguns dados necessários.</p>

O sistema deve no mínimo implementar os seguintes requisitos funcionais: 
- Permitir cadastrar clientes, 
- Permitir cadastrar ordens de serviço, 
- Permitir processar ordem de serviço, definindo o status 
- Permitir consultar uma ordem de serviço, 
- Permitir declarar que o serviço descrito foi prestado e dado como aceito pelo cliente. 
- Permitir a identificação e autenticação de usuário

ENTREGA 02

A equipe deve desenvolver as seguintes tarefas: 
1. Criar pelo console do firebase um projeto (OrdemdeservicoADM) 
2. Associar OrdemdeservicoADM ao app em desenvolvimento no android studio 
3. Modelar a estrutura do banco de dados Cloud Firestore (coleções, documentos)
4. Programar a identificação e autenticação de usuário 
5. Programar as operações de incluir, pesquisar, remover e atualizar os dados do cliente no Cloud Firestore (Firebase) do projeto OrdemdeservicoADM

ENTREGA 03

Após implementar a operação de signup e signin (através de e-mail/senha) a equipe deve:
1. Desenhar as interfaces de usuário para as operações de manutenção de ordem de serviço
2. Modelar a estrutura do banco de dados Cloud Firestore (coleções, documentos) relacionada a entidade ordem de serviço
3. Programar as operações de incluir, remover e atualizar os dados da ordem de serviço no Cloud Firestore (Firebase) do projeto OrdemdeservicoADM
4. Pesquisas envolvendo as coleções clientes, ordens de serviço e comentários:

      - Pesquisar e visualizar as ordens de serviço de um determinado cliente
      - Visualizar os comentários de uma ordem de serviço
      - Pesquisar e visualizar todas as ordens de serviço cancelada
      - Pesquisar e visualizar todas as ordens de serviço finalizada
      - Pesquisar e visualizar todas as ordens de serviço abertas
      - Calcular  e visualizar o  montante total das ordens de serviço autorizadas


DIAGRAMA DE CLASSES
 
 ![image](https://user-images.githubusercontent.com/54014398/136382151-9794c1f1-cc5d-4e6d-8eb7-b910fcb3a869.png)

Figura 01: Fragmento do diagrama de classes do sistema

