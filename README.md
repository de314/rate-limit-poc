# Spring Boot Rate Limit Filter POC

We are investigating the trade offs of infrastructure vs application scope rate limiting.

# Installation

`git clone https://github.com/de314/rate-limit-poc.git`

# Server Configuration

Look at the configuration in `src/main/resources/application.yml`

`bc.rest.filters.rateLimit`

| Name | Default | Descrioption | Required |
| --- | --- | --- | --- |
| `enabled` | `false` | Whether to apply the rate limiting filter | false |
| `path` | `/*` | The ant matcher for which routes to apply the filter | false |
| `requesterIdHeaderKey` | `X-Rate-Limit-ID` | The request header holding the client bucket id | false |
| `constraints` | see global below | How to apply the filter buckets. Order matters. | false |
| `constraints.name` | N/A | The name to be used in the headers and logs | true |
| `constraints.value` | N/A | The threshold value. Use `-1` for none. | true |
| `constraints.timeUnit` | N/A | The threshold unit. `{ SECONDS, MINUTES, HOURS, DAYS }` | true |
| `constraints.rawIdPattern` | `${name}` | The regex pattern for matching requester id's to buckets. Defaults to exact match of `constraint.name`. | false |
| `constraints.rawIdPattern` | `${name}-${rawIdPattern}` | The header returned in responses. Defaults to exact match of `constraint.name`. | false |

### Defaults

If `bc.rest.filters.rateLimit.enabled = true` and constraints are not provided then the
following configs will be applied.

```yaml
bc.rest.filters.rateLimit:
  enabled: true
  path: /*
  requesterIdHeaderKey: 'X-Rate-Limit-ID'
  constraints:
    - { name: 'default-global', value: -1, timeUnit: DAYS, rawIdPattern: '.*' }
```

### Example Configuration

```yaml
bc.rest.filters.rateLimit:
  enabled: true
  path: /rl/*
  requesterIdHeaderKey: 'X-Test-Rate-Limit-ID'
  constraints:
    - name: assets
      value: 2
      timeUnit: MINUTES
      rawIdPattern: ^.*assets.*$
    - { name: directory, value: 8, timeUnit: MINUTES, rawIdPattern: ".*directory.*" }
    - { name: ledger, value: 2, timeUnit: MINUTES, rawIdPattern: "shared-ledger-micro" }
    - { name: workflows, value: 5, timeUnit: MINUTES, rawIdPattern: "(ae|wf|workflows?)" }
    # The following line allows any "core-*" requester unlimited requests
    - { name: core, value: -1, timeUnit: HOURS, rawIdPattern: "^core.*" }
    # It is a good practice to put a catch all at the end of the constraints list.
    - { name: global, value: 5, timeUnit: MINUTES, rawIdPattern: .* }
```

# Client Configuration

### HTTP Requests

Just apply the `bc.rest.filters.rateLimit.requesterIdHeaderKey` (defaults to `X-Rate-Limit-ID`) header
with the requester id.

```
$ curl -i --header "X-Rate-Limit-ID: wf-test-micro" http://localhost:9090/rl/fib/calc/1                                                                                                       ✔  1674  09:52:39

HTTP/1.1 200
X-Rate-Limit-ID: wf-test-micro
X-Rate-Limit-Group: workflows (/(ae|wf|workflows?)/)
X-Rate-Limit-Limit: 5
X-Rate-Limit-Remaining: 5
X-Rate-Limit-Reset: 51696
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: POST, GET, PUT, OPTIONS, DELETE, PATCH
Access-Control-Max-Age: 3600
Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept
Access-Control-Expose-Headers: Location
X-Application-Context: application:9090
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Fri, 12 Jan 2018 14:53:08 GMT

{"result":1,"startTime":1515768788304,"duration":0}
```

### Spring Boot Rest Template

Add a `spring.application.name` property to `src/main/application.yml`, 
or other property file.

```yaml
spring:
  application:
    name: my-test-client-micro
```

Look at the files in `src/main/java/com.bettercloud.platform.ratelimitpoc.client`
specifically `com.bettercloud.platform.ratelimitpoc.client.config.RateLimitClientTester`

```java
@Slf4j
@Component
public class RateLimitClientTester implements CommandLineRunner {

    // You can provide a Rest Template bean that will get decorated or you can just autowire
    // the provided one.
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) throws Exception {
        for (int i=0;i<100;i++) {
            // No need to do anything special. Just use it as normal and it will apply the correct Rate Limit headers.
            // See com.bettercloud.platform.ratelimitpoc.client.rest.RateLimitRequestInterceptor for more details
            ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:9090/rl/fib/calc/1", String.class);
            HttpStatus statusCode = res.getStatusCode();
            System.out.println();
            log.info("statusCode = " + statusCode);
            res.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().startsWith("X-Rate-Limit"))
                    .forEach(e -> log.info("{}: {}", e.getKey(), e.getValue()));
        }
    }
}
```