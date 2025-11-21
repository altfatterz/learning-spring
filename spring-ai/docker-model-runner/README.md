## Docker Model Runner

### Setup in Docker Desktop

- AI Settings, click the checkbox: `Enable host-side TCP support` for the `Enable Docker Model Runner`
- Leave the default port: 12434

### Pull a model

```bash
$ docker model version
Docker Model Runner version v0.1.46
Docker Engine Kind: Docker Desktop
$ docker model pull ai/gemmma3:latest
$ docker model list
MODEL NAME  PARAMETERS  QUANTIZATION   ARCHITECTURE  MODEL ID      CREATED      SIZE
gemma3      3.88 B      MOSTLY_Q4_K_M  gemma3        a353a8898c9d  8 weeks ago  2.31 GiB
```

This is the image: https://hub.docker.com/r/ai/gemma3

### Check if the Docker Model Runner is running

```bash
$ docker model status
```

### Run a model and interact with it using a submitted prompt or chat mode

```bash
$ docker model run gemma3
> Send a message (/? for help)
```

### Check logs

```bash
$ docker model logs
```

### Run the application

After starting up you can see the result in the logs for the given prompt.

You can check the model logs

```bash
$ docker model logs -f
```

### Cleanup

```bash
$ docker model stop-runner

Standalone uninstallation not supported with Docker Desktop
Use `docker desktop disable model-runner` instead


$ docker desktop disable model-runner

$ docker model list
Docker Model Runner is not running. Please start it and try again.
What's next:
    Enable Docker Model Runner via the CLI → docker desktop enable model-runner
    Enable Docker Model Runner via the GUI → Go to Settings->AI->Enable Docker Model Runner
```

## Resources:
- https://docs.docker.com/ai/model-runner/
- https://www.youtube.com/watch?v=6E6JFLMHcoQ