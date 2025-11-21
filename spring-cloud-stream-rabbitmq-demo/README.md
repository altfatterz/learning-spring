## Spring Cloud Stream RabbitMQ

```bash
$ docker compose up -d
```

```bash
$ rabbitmqctl list_queues 
$ rabbitmqctl delete_queue <name>

$ rabbitmqctl list_exchanges 
$ rabbitmqctl delete_exchange <name>

```

Access `http://localhost:15672/#/`

Resources:
- https://training.cloudamqp.com/course/1
