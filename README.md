# message-app-spring

API com Spring Boot para envio e recuperação de mensagens usando SDK da AWS com o serviço AWS SQS.

Essa implementação consiste no armazenamento de mensagens em uma fila primeiro a entrar, primeiro a sair (FIFO) que garante a ordem das mensagens seja consistente. O engajamento com as mensagens é disponibilizada por uma API REST desenvolvida com o framework Spring Boot, utilizando o SDK da AWS, e o Maven como gerenciador de dependências.
