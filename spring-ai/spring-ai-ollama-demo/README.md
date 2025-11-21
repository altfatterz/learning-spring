# Spring AI with Ollama

## Ollama
- Run language models on your laptop (two reasons: cost and security)

List of available models: https://ollama.com/search

```bash
$ brew install ollama
$ ollama --version
ollama version is 0.12.10

$ ollama serve # Start ollama
$ ollama list # list models

$ ollama run llama3.1:8b 

# model stored in ~/.ollama/models
$ ollama list 
NAME           ID              SIZE      MODIFIED
llama3.1:8b    46e0c10c039e    4.9 GB    46 seconds ago
```

### Testing the model

```bash
>>> List the cantos in Switzerland and their capital

Here are the 26 cantons in Switzerland, listed with their capitals:

1.  **Aargau** - Aarau
2.  **Appenzell Ausserrhoden** - Herisau
3.  **Appenzell Innerrhoden** - Appenzell
4.  **Basel-Landschaft** - Liestal
5.  **Basel-Stadt** - Basel
6.  **Bern** - Bern (federal capital)
7.  **Fribourg** - Fribourg
8.  **Geneva** - Geneva (officially in French: Genève)
9.  **Glarus** - Glarus
10. **Graubünden** - Chur
11. **Jura** - Delémont
12. **Lucerne** - Lucerne
13. **Neuchâtel** - Neuchâtel
14. **Nidwalden** - Stans
15. **Obwalden** - Sarnen
16. **Schaffhausen** - Schaffhausen
17. **Schwyz** - Schwyz
18. **Solothurn** - Solothurn
19. **St. Gallen** - St. Gallen
20. **Thurgau** - Frauenfeld
21. **Ticino** - Bellinzona
22. **Uri** - Altdorf
23. **Valais** - Sion
24. **Vaud** - Lausanne
25. **Zug** - Zug
26. **Zurich** - Zurich

These cantons form the foundation of Switzerland's federal system, each with its own unique culture, history, and regional identity.

Note: Some Swiss German dialects use slightly different names for some cantons (e.g., "Appenzell" instead of "Appenzell Ausserrhoden" or "Uri" instead of "Uris").
```

```bash
>>> what day is tomorrow?
However, I'm a large language model, I don't have real-time access to your current date and time.

But you can easily find out the answer by:

1.  Checking your computer or phone's calendar.
2.  Looking at the date displayed on your device.
3.  Asking a voice assistant like Siri, Google Assistant, or Alexa.

If you'd like, I can help you figure out which day of the week tomorrow is. Just let me know your current date (month and day), and I'll be happy to assist you.
```


### Open WebUI 

- Open WebUI is an extensible, feature-rich, and user-friendly self-hosted AI platform designed to operate entirely offline.

https://docs.openwebui.com/

Install with Docker with Ollama running on the same computer. More details: https://github.com/open-webui/open-webui?tab=readme-ov-file#installation-with-default-configuration

```bash
$ docker run -d -p 3000:8080 --add-host=host.docker.internal:host-gateway -v open-webui:/app/backend/data --name open-webui --restart always ghcr.io/open-webui/open-webui:main

$ docker ps

CONTAINER ID   IMAGE                                COMMAND           CREATED          STATUS                            PORTS                                         NAMES
a13db8fb810f   ghcr.io/open-webui/open-webui:main   "bash start.sh"   10 seconds ago   Up 9 seconds (health: starting)   0.0.0.0:3000->8080/tcp, [::]:3000->8080/tcp   open-webui
```

Access http://localhost:3000/

### Start the application

```bash
# make sure the `ollama serve` is running
$ http :8080/api/chat\?prompt="tell me a fact"

HTTP/1.1 200
Connection: keep-alive
Content-Length: 214
Content-Type: text/plain;charset=UTF-8
Date: Sun, 09 Nov 2025 14:24:54 GMT
Keep-Alive: timeout=60

Here's one:

**Butterflies taste with their feet.**

They have tiny sensors on their feet that help them detect the sweetness or bitterness of a substance, which guides their choice of food source. Isn't that cool?
```

```bash
# ollama serve log
[GIN] 2025/11/09 - 15:26:01 | 200 |  1.883827667s |       127.0.0.1 | POST     "/api/chat"
``` 

### Tool Calling

https://docs.spring.io/spring-ai/reference/api/tools.html

```bash
$ http :8080/cities\?message="What is the current weather like in Zurich Switzerland"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 163
Content-Type: text/plain;charset=UTF-8
Date: Sun, 09 Nov 2025 15:18:48 GMT
Keep-Alive: timeout=60

The current weather in Zurich, Switzerland is overcast with a temperature of 48.7°F (9.3°C) and a humidity level of 87%. The wind speed is approximately 2.2 mph.

```

In the logs we see that it calls out to the WeatherService 

```bash
2025-11-09T16:18:47.341+01:00  INFO 65201 --- [spring-ai-ollama-demo] [nio-8080-exec-3] o.e.s.functions.WeatherService           : Weather Request: Request[city=Zurich, Switzerland]
2025-11-09T16:18:47.544+01:00  INFO 65201 --- [spring-ai-ollama-demo] [nio-8080-exec-3] o.e.s.functions.WeatherService           : Weather API Response: Response[location=Location[name=Zurich, region=, country=Switzerland, lat=47, lon=8], current=Current[temp_f=48.7, condition=Condition[text=Overcast], wind_mph=2.2, humidity=87]]

```



### Resources
- https://www.youtube.com/watch?v=dffEF9ORVUg
- https://www.youtube.com/watch?v=BzFafshQkWw
- https://www.youtube.com/watch?v=n7IvE1VSbvI

