```bash
$ http :8080/balance
HTTP/1.1 401
```

```bash
$ http -a alice:secret :8080/balance
123.5

$ http -a bob:secret :8080/balance
23.5
```

```bash
$ echo -n '{"to":"bob", "amount": 23.50}' | http -a alice:secret post :8080/account-transfer
```

```bash
$ http -a alice:secret :8080/balance
123.0   

$ http -a bob:secret :8080/balance
47.0
```
